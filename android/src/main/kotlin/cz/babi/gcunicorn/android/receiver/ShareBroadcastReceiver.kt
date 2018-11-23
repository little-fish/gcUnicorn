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

package cz.babi.gcunicorn.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.`fun`.dismissNotification

/**
 * Receiver that handles action coming from notifications.
 *
 * Basically it shows options to the user about what to do with received data.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since
 */
class ShareBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val MIME_TYPE_GPX = "application/gpx"
        const val EXTRA_MIME_TYPE = "extra_mime_type"
        const val EXTRA_SHARE_TYPE = "extra_share_type"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val SHARE_TYPE_VIEW = "share_view"
        const val SHARE_TYPE_SEND = "share_send"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Check whether URI was passed or not.
        intent?.data?.let {
            val mimeType = intent.getStringExtra(EXTRA_MIME_TYPE)
            val intentShare: Intent
            val title: String

            when (intent.getStringExtra(EXTRA_SHARE_TYPE)) {
                SHARE_TYPE_SEND -> {
                    intentShare = generateSendIntent(it, mimeType ?: MIME_TYPE_GPX)
                    title = context?.getString(R.string.action_view_gpx) ?: ""
                }
                else -> {
                    // SHARE_TYPE_VIEW
                    intentShare = generateViewIntent(it, mimeType ?: MIME_TYPE_GPX)
                    title = context?.getString(R.string.action_view_gpx) ?: ""
                }
            }

            val chooseIntent = Intent.createChooser(intentShare, title).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }

            intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0).let { id ->
                context?.dismissNotification(id)
            }

            context?.startActivity(chooseIntent)
        }
    }

    /**
     * Generates [Intent] for [Intent.ACTION_VIEW] action.
     * @param uri [Uri] to be set as data to created [Intent].
     * @param type Type to be set as type to created [Intent].
     */
    private fun generateViewIntent(uri: Uri, type: String) = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, type)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /**
     * Generates [Intent] for [Intent.ACTION_SEND] action.
     * @param uri [Uri] to be set as data to created [Intent].
     * @param type Type to be set as type to created [Intent].
     */
    private fun generateSendIntent(uri: Uri, type: String) = Intent(Intent.ACTION_SEND).apply {
        setDataAndType(uri, type)
        putExtra(Intent.EXTRA_STREAM, uri)
    }
}