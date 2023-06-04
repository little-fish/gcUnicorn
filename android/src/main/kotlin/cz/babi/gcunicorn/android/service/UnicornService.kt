/*
 * gcUnicorn
 * Copyright (C) 2023  Martin Misiarz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cz.babi.gcunicorn.android.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.dagger.qualifier.Named
import cz.babi.gcunicorn.android.dagger.qualifier.Named.Companion.PREFERENCES_NORMAL
import cz.babi.gcunicorn.android.dagger.qualifier.Named.Companion.PREFERENCES_PRIVATE
import cz.babi.gcunicorn.android.`fun`.ACTION_CANCEL_DOWNLOADING
import cz.babi.gcunicorn.android.`fun`.ACTION_EXPORT_GPX
import cz.babi.gcunicorn.android.`fun`.ACTION_NOTIFY
import cz.babi.gcunicorn.android.`fun`.DATETIME_PATTERN_GPX
import cz.babi.gcunicorn.android.`fun`.DATETIME_PATTERN_UI
import cz.babi.gcunicorn.android.`fun`.FILTER_DEFAULT_CACHE_COUNT
import cz.babi.gcunicorn.android.`fun`.NOTIFICATION_CHANNEL_ID
import cz.babi.gcunicorn.android.`fun`.NOTIFICATION_ID_DOWNLOADING
import cz.babi.gcunicorn.android.`fun`.androidApplication
import cz.babi.gcunicorn.android.`fun`.dismissNotification
import cz.babi.gcunicorn.android.`fun`.localBroadcastManager
import cz.babi.gcunicorn.android.`fun`.showNotification
import cz.babi.gcunicorn.android.preference.PreferenceKey
import cz.babi.gcunicorn.android.receiver.ShareBroadcastReceiver
import cz.babi.gcunicorn.android.security.Security
import cz.babi.gcunicorn.android.storage.FileWorker
import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.exception.network.LoginException
import cz.babi.gcunicorn.core.exception.network.LogoutException
import cz.babi.gcunicorn.core.exception.network.ServiceException
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.core.network.model.Credentials
import cz.babi.gcunicorn.core.network.service.GeocacheLoadedListener
import cz.babi.gcunicorn.core.network.service.Service
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheFilter
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheType
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Geocache
import cz.babi.gcunicorn.`fun`.format
import cz.babi.gcunicorn.`fun`.nullableExecute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import cz.babi.gcunicorn.android.`fun`.FILTER_MAX_DISTANCE as CONST_FILTER_MAX_DISTANCE

/**
 * Service which handles all do network related job. Each job starts with _login_ process and ends with _logout_ process.
 *
 * It creates and updates notification during the process of obtaining geocaches.
 *
 * Once the process of downloading is done, it shows a notification allowed an user to view/share created GPX file.
 *
 * @since 1.0.0
 */
class UnicornService : JobIntentService() {

    companion object {
        const val COORDINATION_LAT = "latitude"
        const val COORDINATION_LON = "longitude"
        const val FILTER_CACHE_TYPE = "cache_type"
        const val FILTER_MAX_CACHE_COUNT = "max_count"
        const val FILTER_MAX_DISTANCE = "max_distance"
        const val FILTER_ALLOW_DISABLED = "allow_disable"
        const val FILTER_EXCLUDE_OWN = "include_own"
        const val FILTER_EXCLUDE_FOUND = "include_found"
        const val FILTER_SKIP_PREMIUM = "skip_premium"
        const val NOTIFICATION_MESSAGE = "notification_message"
        const val TAG = "UnicornService"
    }

    @Inject
    protected lateinit var fileWorker: FileWorker

    @Inject
    protected lateinit var service: Service

    @Inject
    protected lateinit var security: Security

    @Inject
    protected lateinit var parser: Parser

    @Inject
    @field:Named(PREFERENCES_NORMAL)
    protected lateinit var sharedPreferences: SharedPreferences

    @Inject
    @field:Named(PREFERENCES_PRIVATE)
    protected lateinit var privateSharedPreferences: SharedPreferences

    private val notificationBroadcastReceiver = NotificationBroadcastReceiver(object : NotificationBroadcastReceiver.Receiver {
        override fun orReceive(context: Context?, intent: Intent?) {
            canContinue = false

            if (this@UnicornService::downloadJob.isInitialized && downloadJob.isActive) {
                downloadJob.cancel()
            }

            serviceNotificationBuilder.apply {
                setStyle(null)
                setContentText(getString(R.string.text_cancelling))
                setOngoing(false)
                refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())
            }

            sendLocalBroadcast(createNotificationIntent(getString(R.string.text_cancelling)))
        }
    })

    private val cancelNotificationIntentFilter by lazy {
        IntentFilter(ACTION_CANCEL_DOWNLOADING)
    }

    private val serviceNotificationBuilder by lazy {
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setPriority(NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setSmallIcon(R.drawable.notification_downloading)
                .setOngoing(true)
                .addAction(0, getString(R.string.text_cancel), PendingIntent.getBroadcast(this, 0, Intent(ACTION_CANCEL_DOWNLOADING), createPendingIntentFlags()))
    }

    private val shareNotificationBuilder by lazy {
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setPriority(NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setSmallIcon(R.drawable.notification_downloaded)
    }

    private var maxCount = FILTER_DEFAULT_CACHE_COUNT
    private var actualCount = 0
    private var canContinue = true
    private var downloadNotificationAlreadySent = false
    private lateinit var downloadJob: Deferred<List<Geocache>>

    override fun onCreate() {
        super.onCreate()

        androidApplication.applicationComponent().inject(this)

        registerReceiver(notificationBroadcastReceiver, cancelNotificationIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(notificationBroadcastReceiver)
    }

    override fun onHandleWork(intent: Intent) {
        onPreExecute(intent)

        try {
            runBlocking {
                service.login(Credentials(
                    sharedPreferences.getString(PreferenceKey.GC_USERNAME.key, "")!!,
                    security.decryptWithBase64(
                        privateSharedPreferences.getString(PreferenceKey.SECURE_KEY.key, "")!!,
                        sharedPreferences.getString(PreferenceKey.GC_PASSWORD.key, "")!!)
                ))
            }

            if (canContinue) {
                val jobStarted = Date()

                serviceNotificationBuilder.apply {
                    setContentTitle(jobStarted.format(DATETIME_PATTERN_UI, Locale.US))
                    setContentText(getText(R.string.notification_searching_for_caches))
                    refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())
                }

                runBlocking {
                    downloadJob = async {
                        service.lookForCaches(
                                parser.parse("${intent.getStringExtra(COORDINATION_LAT)}, ${intent.getStringExtra(COORDINATION_LON)}"),
                                CacheFilter(
                                        listOf(CacheType.findByPattern(intent.getStringExtra(FILTER_CACHE_TYPE)!!)),
                                        intent.getDoubleExtra(FILTER_MAX_DISTANCE, CONST_FILTER_MAX_DISTANCE),
                                        intent.getBooleanExtra(FILTER_ALLOW_DISABLED, false),
                                        intent.getBooleanExtra(FILTER_EXCLUDE_OWN, false),
                                        intent.getBooleanExtra(FILTER_EXCLUDE_FOUND, false),
                                        intent.getBooleanExtra(FILTER_SKIP_PREMIUM, true)
                                ),
                                maxCount,
                                object : GeocacheLoadedListener {
                                    override fun geocacheLoaded(geocache: Geocache) {
                                        if (this@UnicornService::downloadJob.isInitialized && downloadJob.isActive) {
                                            serviceNotificationBuilder.apply {
                                                setStyle(NotificationCompat.BigTextStyle().bigText(
                                                        "${getText(R.string.notification_downloading_caches)} (${++actualCount}/$maxCount)"
                                                ))
                                                setProgress(maxCount, actualCount, false)
                                                refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())

                                            }

                                            if (!downloadNotificationAlreadySent) {
                                                sendLocalBroadcast(createNotificationIntent(getText(R.string.notification_downloading_caches)))
                                                downloadNotificationAlreadySent = true
                                            }
                                        }
                                    }
                                }
                        )
                    }

                    val geocaches = downloadJob.await()

                    if (canContinue) {
                        serviceNotificationBuilder.apply {
                            setStyle(null)
                            setContentText(getText(R.string.notification_generating_gpx))
                            setProgress(0, 0, true)
                            refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())
                        }

                        sendLocalBroadcast(createNotificationIntent(getText(R.string.notification_generating_gpx)))

                        val gpxOutput = service.createGpx(geocaches, false)

                        fileWorker.writeToExternalStorage(this@UnicornService, "${jobStarted.format(DATETIME_PATTERN_GPX, Locale.US)}.gpx", gpxOutput).nullableExecute({
                            val notificationId = Random.nextInt()
                            shareNotificationBuilder.apply {
                                setContentTitle("${jobStarted.format(DATETIME_PATTERN_GPX, Locale.US)}.gpx")
                                setContentText(getString(R.string.notification_gpx_exported))
                                addAction(0, getString(R.string.action_view_gpx), PendingIntent.getBroadcast(this@UnicornService, Random.nextInt(), createGpxShareBroadcastIntent(this@nullableExecute, notificationId).apply { putExtra(ShareBroadcastReceiver.EXTRA_SHARE_TYPE, ShareBroadcastReceiver.SHARE_TYPE_VIEW) }, createPendingIntentFlags()))
                                addAction(0, getString(R.string.action_send_gpx), PendingIntent.getBroadcast(this@UnicornService, Random.nextInt(), createGpxShareBroadcastIntent(this@nullableExecute, notificationId).apply { putExtra(ShareBroadcastReceiver.EXTRA_SHARE_TYPE, ShareBroadcastReceiver.SHARE_TYPE_SEND) }, createPendingIntentFlags()))
                            }
                            showNotification(notificationId, shareNotificationBuilder.build())
                        }, {
                            Log.e(TAG, "Can not export GPX file.")
                        })
                    }
                }
            }
        } catch (loginException: LoginException) {
            Log.e(TAG, "Can not log in.", loginException)
        } catch (coordinateParseException: CoordinateParseException) {
            Log.e(TAG, "Can not parse coordinates.", coordinateParseException)
        } catch (serviceException: ServiceException) {
            Log.e(TAG, "Can not download caches.", serviceException)
        } catch (cancellationException: CancellationException) {
            Log.w(TAG, "Scheduled job has been cancelled.", cancellationException)
        } catch (e: Exception) {
            Log.e(TAG, "This exception should never occurred during downloading caches.", e)
        } catch (outOfMemoryError: OutOfMemoryError) {
            Log.e(TAG, "This is very bad ;(. How much memory do you need?", outOfMemoryError)
        } finally {
            try {
                serviceNotificationBuilder.apply {
                    setStyle(null)
                    setContentTitle(getText(R.string.text_app_name))
                    setContentText(getText(R.string.notification_logging_out))
                    setProgress(0, 0, true)
                    setOngoing(false)
                    refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())
                }

                sendLocalBroadcast(createNotificationIntent(getText(R.string.notification_logging_out)))

                runBlocking {
                    service.logout()
                }
            } catch (logoutException: LogoutException) {
                Log.e(TAG, "Can not log out.", logoutException)
            } catch (e: Exception) {
                Log.e(TAG, "This exception should never occurred during logout process.", e)
            }
        }

        onPostExecute()
    }

    /**
     * Creates pending intents default flags.
     */
    private fun createPendingIntentFlags(): Int {
        var pendingIntentFlags = PendingIntent.FLAG_ONE_SHOT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }

        return pendingIntentFlags
    }

    /**
     * Do some stuff before the process of downloading could start.
     * @param intent Service's intent.
     */
    private fun onPreExecute(intent: Intent) {
        maxCount = intent.getIntExtra(FILTER_MAX_CACHE_COUNT, FILTER_DEFAULT_CACHE_COUNT)
        actualCount = 0
        canContinue = true

        serviceNotificationBuilder.apply {
            setContentTitle(getText(R.string.text_app_name))
            setContentText(getText(R.string.notification_logging_in))
            setProgress(0, 0, true)
            refreshNotification(NOTIFICATION_ID_DOWNLOADING, this.build())
        }

        sendLocalBroadcast(createNotificationIntent(getText(R.string.notification_logging_in)))
    }

    /**
     * Do some stuff once the service is about to finish.
     */
    private fun onPostExecute() {
        cancelNotification(NOTIFICATION_ID_DOWNLOADING)
    }

    /**
     * Refresh service notification.
     *
     * Basically you want to call this method if you update given notification and want to refresh it.
     *
     * @param notificationId Notification Id.
     * @param notification Notification to be refreshed.
     */
    private fun refreshNotification(notificationId: Int, notification: Notification) {
        showNotification(notificationId, notification)
    }

    /**
     * Dismiss notification of given Id.
     * @param notificationId Id of the notification to be dismissed.
     */
    private fun cancelNotification(notificationId: Int) {
        dismissNotification(notificationId)
    }

    /**
     * Sends given [Intent] through [android.support.v4.content.LocalBroadcastManager].
     * @param intent Intent to be send.
     */
    private fun sendLocalBroadcast(intent: Intent) {
        localBroadcastManager.sendBroadcast(intent)
    }

    /**
     * Creates notification [Intent]. It puts given message into [NOTIFICATION_MESSAGE] extra.
     * @param message Message to be send.
     * @return Created [Intent] with given message.
     */
    private fun createNotificationIntent(message: CharSequence) = Intent(ACTION_NOTIFY).apply { putExtra(NOTIFICATION_MESSAGE, message.toString()) }

    /**
     * Creates [Intent] to be send once the GPX file is created and we need to provide some feedback back to an user.
     * @param file Created GPX file.
     * @param notificationId Notification Id.
     * @return Created [Intent].
     */
    private fun createGpxShareBroadcastIntent(file: File, notificationId: Int) = Intent(this, ShareBroadcastReceiver::class.java).apply {
        action = ACTION_EXPORT_GPX
        putExtra(ShareBroadcastReceiver.EXTRA_MIME_TYPE, ShareBroadcastReceiver.MIME_TYPE_GPX)
        putExtra(ShareBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        putExtra(ShareBroadcastReceiver.EXTRA_AUTO_CLOSE_NOTIFICATION, sharedPreferences.getBoolean(PreferenceKey.AUTO_CLOSE_NOTIFICATION.key, false))
        data = FileProvider.getUriForFile(applicationContext, applicationContext.packageName, file)
        flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    /**
     * Receiver providing callback mechanism once new message is received.
     *
     * @param receiver Receiver to be notified once new message arrives.
     */
    class NotificationBroadcastReceiver(private val receiver: Receiver?) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            receiver?.orReceive(context, intent)
        }

        /**
         * Simple receiver.
         */
        interface Receiver {
            fun orReceive(context: Context?, intent: Intent?)
        }
    }
}