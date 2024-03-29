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

package cz.babi.gcunicorn.webapp.entity

/**
 * Cache filer used for web UI.
 *
 * @param cacheType Cache type.
 * @param coordinates Coordinates.
 * @param count Max count.
 * @param distance Max distance.
 *
 * @since 1.0.0
 */
data class CacheFilterWeb(
        var cacheType: String? = null,
        var coordinates: String? = null,
        var count: Int? = null,
        var distance: Double? = null
)