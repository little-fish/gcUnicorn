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

package cz.babi.gcunicorn.core.location

/**
 * Coordinates data class.
 *
 * Supported ranges:
 * * latitude: <-85, 85>
 * * longitude: <-180, 180>
 *
 *
 * @param latitude Latitude.
 * @param longitude Longitude.
 * @throws [IllegalArgumentException] If given latitude or longitude are not within proper ranges.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
data class Coordinates(val latitude: Double, val longitude: Double) {

    init {
        if(latitude !in -85.0..85.0 || longitude !in -180.0..180.0) throw IllegalArgumentException("You have provided wrong coordinates. Latitude must be in range <-85, 85>, longitude must be in range <-180, 180>. Provided coordinates are: $latitude:$longitude.")
    }
}