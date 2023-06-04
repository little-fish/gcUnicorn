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

package cz.babi.gcunicorn.android.`fun`

import android.view.View
import com.google.android.material.snackbar.Snackbar

/**
 * Extension method for showing Snackbar on given view.
 * @param resourceId String resource id to be displayed.
 * @param length Length (in milliseconds) of the Snackbar to be visible. Default value is [Snackbar.LENGTH_LONG].
 *
 * @see [Snackbar]
 *
 * @author @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0.
 */
fun View.snack(resourceId: Int, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, context.getText(resourceId), length).apply {
        show()
    }
}

/**
 * Extension method for showing Snackbar on given view.
 * @param message Message to be displayed.
 * @param length Length (in milliseconds) of the Snackbar to be visible. Default value is [Snackbar.LENGTH_LONG].
 *
 * @see [Snackbar]
 *
 * @author @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0.
 */
fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).apply {
        show()
    }
}
