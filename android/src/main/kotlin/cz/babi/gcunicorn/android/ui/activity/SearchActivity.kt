/*
 * gcUnicorn
 * Copyright (C) 2018  Martin Misiarz
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

package cz.babi.gcunicorn.android.ui.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.JobIntentService
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.google.android.gms.location.places.ui.PlacePicker
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.`fun`.ACTION_NOTIFY
import cz.babi.gcunicorn.android.`fun`.ACTIVITY_PLACE_PICKER
import cz.babi.gcunicorn.android.`fun`.FILTER_DEFAULT_CACHE_COUNT
import cz.babi.gcunicorn.android.`fun`.FILTER_MAX_CACHE_COUNT
import cz.babi.gcunicorn.android.`fun`.FILTER_MAX_DISTANCE
import cz.babi.gcunicorn.android.`fun`.NOTIFICATION_CHANNEL_ID
import cz.babi.gcunicorn.android.`fun`.PERMISSION_LOCATION_COARSE
import cz.babi.gcunicorn.android.`fun`.PERMISSION_LOCATION_FINE
import cz.babi.gcunicorn.android.`fun`.PERMISSION_WAKE_LOCK
import cz.babi.gcunicorn.android.`fun`.PERMISSION_WRITE_EXTERNAL_STORAGE
import cz.babi.gcunicorn.android.`fun`.SERVICE_JOB_ID
import cz.babi.gcunicorn.android.`fun`.hasPermissions
import cz.babi.gcunicorn.android.`fun`.localBroadcastManager
import cz.babi.gcunicorn.android.`fun`.rootView
import cz.babi.gcunicorn.android.`fun`.snack
import cz.babi.gcunicorn.android.`fun`.toDoubleOrNull
import cz.babi.gcunicorn.android.`fun`.toEditable
import cz.babi.gcunicorn.android.`fun`.toFloatOrNull
import cz.babi.gcunicorn.android.`fun`.toIntOrNull
import cz.babi.gcunicorn.android.preference.PreferenceKey
import cz.babi.gcunicorn.android.service.UnicornService
import cz.babi.gcunicorn.android.ui.dialog.Error
import cz.babi.gcunicorn.android.ui.dialog.LocationAcquiringDialogFragment
import cz.babi.gcunicorn.android.ui.dialog.OnError
import cz.babi.gcunicorn.android.ui.dialog.OnLocationAcquired
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheType
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location
import java.security.SecureRandom
import kotlinx.android.synthetic.main.activity_search.search_toolbar as toolbar
import kotlinx.android.synthetic.main.layout_search.search_allow_disabled as allowDisabledSwitch
import kotlinx.android.synthetic.main.layout_search.search_cache_type as cacheTypeSpinner
import kotlinx.android.synthetic.main.layout_search.search_download as downloadBtn
import kotlinx.android.synthetic.main.layout_search.search_gps as gpsBtn
import kotlinx.android.synthetic.main.layout_search.search_include_own as includeOwnSwitch
import kotlinx.android.synthetic.main.layout_search.search_map as mapBtn
import kotlinx.android.synthetic.main.layout_search.search_max_distance as maxDistanceEditText
import kotlinx.android.synthetic.main.layout_search.search_skip_premium as skipPremiumSwitch

/**
 * Search activity.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class SearchActivity : BaseAppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val TAG = "SEARCH_ACTIVITY"
    }

    private val maxCountEditText: AppCompatEditText by lazy {
        findViewById<AppCompatEditText>(R.id.search_max_count)
    }

    private val maxCountLayoutWrapper: TextInputLayout by lazy {
        findViewById<TextInputLayout>(R.id.search_max_count_wrapper)
    }

    private val latitudeEditText: TextInputEditText by lazy {
        findViewById<TextInputEditText>(R.id.search_lat)
    }

    private val latitudeLayoutWrapper: TextInputLayout by lazy {
        findViewById<TextInputLayout>(R.id.search_lat_wrapper)
    }

    private val longitudeEditText: AppCompatEditText by lazy {
        findViewById<AppCompatEditText>(R.id.search_lon)
    }

    private val longitudeLayoutWrapper: TextInputLayout by lazy {
        findViewById<TextInputLayout>(R.id.search_lon_wrapper)
    }

    private val maxCountTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidMaxCount()) {
                        maxCountLayoutWrapper.isErrorEnabled = true
                        maxCountLayoutWrapper.error = resources.getString(R.string.validator_error_max_count)
                    } else {
                        maxCountLayoutWrapper.error = null
                        maxCountLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val latitudeTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidLatitude()) {
                        latitudeLayoutWrapper.isErrorEnabled = true
                        latitudeLayoutWrapper.error = resources.getString(R.string.validator_error_latitude)
                    } else {
                        latitudeLayoutWrapper.error = null
                        latitudeLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val longitudeTextWatcherListener =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!isValidLongitude()) {
                        longitudeLayoutWrapper.isErrorEnabled = true
                        longitudeLayoutWrapper.error = resources.getString(R.string.validator_error_longitude)
                    } else {
                        longitudeLayoutWrapper.error = null
                        longitudeLayoutWrapper.isErrorEnabled = false
                    }

                    validateInputs()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationAcquiringDialog: LocationAcquiringDialogFragment by lazy {
        LocationAcquiringDialogFragment().apply {
            onLocationAcquired = object : OnLocationAcquired {
                override fun onLocationAcquired(location: android.location.Location) {
                    latitudeEditText.text = location.latitude.toEditable()
                    longitudeEditText.text = location.longitude.toEditable()
                }
            }

            onError = object : OnError {
                override fun onError(error: Error) {
                    val message = when (error) {
                        Error.NO_COARSE_PERMISSION -> R.string.text_no_permission_coarse_location
                        Error.NO_FINE_PERMISSION -> R.string.text_no_permission_fine_location
                        Error.NO_PROVIDER_AVAILABLE -> R.string.text_no_location_provider
                        Error.INTERNAL_ERROR -> R.string.text_can_not_obtain_location
                    }

                    rootView.snack(message)
                }
            }
        }
    }

    private val notificationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra(UnicornService.NOTIFICATION_MESSAGE)?.let {
                rootView.snack(it)
            }
        }
    }

    private val notificationIntentFilter by lazy {
        IntentFilter(ACTION_NOTIFY)
    }

    override fun onAfterCreate(savedInstanceState: Bundle?) {
        bindView()
        prepareNotificationChannel()
        checkFirstRun()
        setDefaultValuesFromLocus()
    }

    override fun onResume() {
        super.onResume()

        latitudeEditText.addTextChangedListener(latitudeTextWatcherListener)
        longitudeEditText.addTextChangedListener(longitudeTextWatcherListener)
        maxCountEditText.addTextChangedListener(maxCountTextWatcherListener)

        localBroadcastManager.registerReceiver(notificationBroadcastReceiver, notificationIntentFilter)

        validateInputs()
    }

    override fun onPause() {
        super.onPause()

        latitudeEditText.removeTextChangedListener(latitudeTextWatcherListener)
        longitudeEditText.removeTextChangedListener(longitudeTextWatcherListener)
        maxCountEditText.removeTextChangedListener(maxCountTextWatcherListener)

        localBroadcastManager.unregisterReceiver(notificationBroadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_PLACE_PICKER) {
            val place = PlacePicker.getPlace(this, data)

            latitudeEditText.text = place.latLng.latitude.toEditable()
            longitudeEditText.text = place.latLng.longitude.toEditable()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_WAKE_LOCK -> {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish()
                }
            }
        }
    }

    /**
     * Binds view to this activity and set-up widgets.
     */
    private fun bindView() {
        setContentView(R.layout.activity_search)

        setSupportActionBar(toolbar)

        // Fill the spinner with available cache types.
        cacheTypeSpinner.adapter = ArrayAdapter(this, R.layout.item_spinner, CacheType.values().map { it.pattern })
                .apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

        gpsBtn.setOnClickListener {
            acquireLocationFromGpsOrNetwork()
        }

        mapBtn.setOnClickListener {
            acquireLocationFromMaps()
        }

        downloadBtn.setOnClickListener {
            startDownloadProcess()
        }
    }

    /**
     * It is necessary for newer version of Android to set-up notification channel.
     */
    private fun prepareNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.notification_channel_desc)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether the application runs for the first time. If so, it creates secret key property.
     *
     * It also checks whether the application has necessary permissions.
     *
     * @see [cz.babi.gcunicorn.android.security.Security]
     */
    private fun checkFirstRun() {
        // Check if we already have secure key generated.
        if (!privateSharedPreferences.contains(PreferenceKey.SECURE_KEY.key)) {
            val key = ByteArray(16)
            SecureRandom().nextBytes(key)

            privateSharedPreferences
                    .edit()
                    .putString(PreferenceKey.SECURE_KEY.key, Base64.encodeToString(key, Base64.DEFAULT))
                    .apply()

            key.fill(0)
        }

        // Request write permission.
        if (!this.hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_EXTERNAL_STORAGE)
        }

        // Request wake lock permission.
        if (!this.hasPermissions(Manifest.permission.WAKE_LOCK)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WAKE_LOCK), PERMISSION_WAKE_LOCK)
        }
    }

    /**
     * Checks whether the activity has been started from Locus. If so, it will try to obtain location passed from Locus and fills text widgets.
     */
    private fun setDefaultValuesFromLocus() {
        if (LocusUtils.isLocusAvailable(this)) {
            // Obtain location from Locus.
            when {
                LocusUtils.isIntentMainFunctionGc(intent) -> obtainLocusLocationFromIntent(intent)
                LocusUtils.isIntentPointTools(intent) -> LocusUtils.handleIntentPointTools(this, intent).location
                else -> null
            }?.also {
                latitudeEditText.text = it.latitude.toEditable()
                longitudeEditText.text = it.longitude.toEditable()
            }
        }
    }

    /**
     * Activity could be started from different Locus locations. Therefore we need to obtain location comming from different [Intent]s a different way.
     * @param intent [Intent] sent from Locus.
     * @return Location if exists, otherwise returns null.
     */
    private fun obtainLocusLocationFromIntent(intent: Intent): Location? = when {
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_GPS) -> LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS)
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER) -> LocusUtils.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
        else -> null
    }

    /**
     * Acquires location from GPS or network if providers are available.
     *
     * First it tries to obtain location from GPS and if no GPS provider is available (GPS is turned off) then it tries to obtain location from network.
     */
    private fun acquireLocationFromGpsOrNetwork() {
        val gpsProviderEnabled: Boolean = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: IllegalArgumentException) {
            false
        }

        val networkProviderEnabled: Boolean = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: IllegalArgumentException) {
            false
        }

        when {
            gpsProviderEnabled -> requestGpsLocation()
            networkProviderEnabled -> requestNetworkLocation()
            else -> {
                rootView.snack(R.string.text_no_location_provider)
            }
        }
    }

    /**
     * Starts new activity with Google maps and let an user to pick the location.
     */
    private fun acquireLocationFromMaps() {
        try {
            startActivityForResult(PlacePicker.IntentBuilder().build(this), ACTIVITY_PLACE_PICKER)
        } catch (e: ActivityNotFoundException) {
            rootView.snack(getString(R.string.error_can_not_start_place_picker), 3000)
            Log.e(TAG, "Can not create place picker.", e)
        }
    }

    /**
     * Requests GPS location.
     *
     * If the application has no permission to access to GPS, it asks for that permission.
     */
    private fun requestGpsLocation() {
        if (this.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationAcquiringDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_FINE)
        }
    }

    /**
     * Requests network location.
     *
     * If the application has no permission to access to network location, it asks for that permission.
     */
    private fun requestNetworkLocation() {
        if (this.hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showLocationAcquiringDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_LOCATION_COARSE)
        }
    }

    /**
     * Displays new dialog displaying location acquiring.
     */
    private fun showLocationAcquiringDialog() {
        locationAcquiringDialog.show(supportFragmentManager, "location")
    }

    /**
     * If GC credentials has been provided before, it starts new [JobIntentService] which will try to download geocaches.
     */
    private fun startDownloadProcess() {
        if (sharedPreferences.getString(PreferenceKey.GC_USERNAME.key, "").isNullOrEmpty() ||
                security.decryptWithBase64(
                        privateSharedPreferences.getString(PreferenceKey.SECURE_KEY.key, "")!!,
                        sharedPreferences.getString(PreferenceKey.GC_PASSWORD.key, "")!!).isEmpty()) {
            rootView.snack(R.string.text_invalid_gc_credentials, 2500)
        } else {
            JobIntentService.enqueueWork(this, UnicornService::class.java, SERVICE_JOB_ID, Intent().apply {
                putExtra(UnicornService.COORDINATION_LAT, latitudeEditText.text.toString())
                putExtra(UnicornService.COORDINATION_LON, longitudeEditText.text.toString())
                putExtra(UnicornService.FILTER_CACHE_TYPE, cacheTypeSpinner.selectedItem.toString())
                putExtra(UnicornService.FILTER_MAX_CACHE_COUNT, maxCountEditText.text.toIntOrNull()
                        ?: FILTER_DEFAULT_CACHE_COUNT)
                putExtra(UnicornService.FILTER_MAX_DISTANCE, maxDistanceEditText.text.toDoubleOrNull()
                        ?: FILTER_MAX_DISTANCE)
                putExtra(UnicornService.FILTER_ALLOW_DISABLED, allowDisabledSwitch.isChecked)
                putExtra(UnicornService.FILTER_INCLUDE_OWN_AND_FOUND, includeOwnSwitch.isChecked)
                putExtra(UnicornService.FILTER_SKIP_PREMIUM, skipPremiumSwitch.isChecked)
            })
        }
    }

    /**
     * Validates all inputs and set enable/disable download button accordingly.
     */
    private fun validateInputs() {
        downloadBtn.isEnabled = isValidMaxCount() && isValidLatitude() && isValidLongitude()
    }

    /**
     * Checks whether latitude text view contains correct value.
     * @return True if latitude is correct, otherwise returns false.
     */
    private fun isValidLatitude(): Boolean {
        val latitude = latitudeEditText.text.toFloatOrNull()

        return latitude != null && latitude in -85.0..85.0
    }

    /**
     * Checks whether longitude text view contains correct value.
     * @return True if longitude is correct, otherwise returns false.
     */
    private fun isValidLongitude(): Boolean {
        val longitude = longitudeEditText.text.toFloatOrNull()

        return longitude != null && longitude in -180.0..180.0
    }

    /**
     * Checks whether max count text view contains correct value.
     * @return True if max count is correct, otherwise returns false.
     */
    private fun isValidMaxCount(): Boolean {
        if (maxCountEditText.text?.isEmpty() == true) return true

        val maxCount = maxCountEditText.text.toIntOrNull()

        return maxCount != null && maxCount <= FILTER_MAX_CACHE_COUNT
    }
}
