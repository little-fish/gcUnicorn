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

package cz.babi.gcunicorn.core.location.parser.impl

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.Coordinates

/**
 * Decimal degrees empty format parser.
 *
 * If there is not side specified, 'N' and 'E' are used.
 *
 * Parser supports following format:
 * * DD.DDDDDDD°,DD.DDDDDDD°
 *
 * Following are valid examples:
 * * 49,556, 18.555
 * * 49.556° , 18°
 *
 * @since 1.0.1
 */
class DecimalDegreeEmptySidesParser : DecimalDegreesParser() {

    companion object {
        @JvmField val REGEX_LAT_LON = "^$PATTERN$PATTERN_SEPARATOR$PATTERN$".toRegex(RegexOption.IGNORE_CASE)
    }

    override fun parse(text: String): Coordinates {
        val values = REGEX_LAT_LON.find(text.trim())?.groupValues

        try {
            if (values != null && values.size == 4 && values[1].isNotEmpty() && values[3].isNotEmpty()) {
                val latitude = parseLatitude("N" + values[1])
                val longitude = parseLongitude("E" + values[3])

                return Coordinates(latitude, longitude)
            }
        } catch(e: Exception) {
            throw CoordinateParseException("Can't parse coordinates.", e)
        }

        throw CoordinateParseException("Can't parse coordinates. Given input text '$text' doesn't match the pattern.")
    }
}