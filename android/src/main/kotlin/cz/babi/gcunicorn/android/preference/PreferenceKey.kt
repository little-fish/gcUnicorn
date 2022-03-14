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

package cz.babi.gcunicorn.android.preference

/**
 * List of all available preferences.
 *
 * See res/xml/preferences.xml for all available preference keys.
 *
 * @param key String representation of preference key.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
enum class PreferenceKey(val key: String) {
    SECURE_KEY("secure_key"),
    GC_USERNAME("gc_username"),
    GC_PASSWORD("gc_password"),
    AUTO_CLOSE_NOTIFICATION("auto_close_notification");

    companion object {
        const val PRIVATE_PREFS = "private_prefs"
    }
}