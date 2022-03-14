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

package cz.babi.gcunicorn.android.`fun`

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Extension method for checking permissions.
 * @param permissions Permissions to check.
 * @return True if all given permissions are granted. Otherwise, returns false.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    permissions.forEach {
        if (this.checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_DENIED) return false
    }

    return true
}

/**
 * Extension method for displaying notification.
 * @param id Notification id.
 * @param notification Notification to show.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
fun Context.showNotification(id: Int, notification: Notification) {
    notificationManager.notify(id, notification)
}

/**
 * Extension method for dismissing notification.
 * @param id Id of the notification to be dismissed.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
fun Context.dismissNotification(id: Int) {
    notificationManager.cancel(id)
}

/**
 * Extension property for easy access to underlying notification manager.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
val Context.notificationManager: NotificationManagerCompat
    get() = NotificationManagerCompat.from(this)

/**
 * Extension property for easy access to underlying local broadcast manager.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)