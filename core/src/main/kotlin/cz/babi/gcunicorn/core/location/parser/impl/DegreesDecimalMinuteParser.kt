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

package cz.babi.gcunicorn.core.location.parser.impl

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.Coordinates
import cz.babi.gcunicorn.core.location.parser.Parser

/**
 * Degrees decimal minutes format parser.
 *
 * Parser supports following format:
 * * X DD° MM.MMM'
 *
 * Following are valid examples:
 * * N 18° 55', E 45° 55'
 * * N9° 55', E 45° 55
 * * N 18,556 , E 45°
 * * N 18°, E 45° 55'
 * * S°55.55',W45°
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class DegreesDecimalMinuteParser : Parser {

    companion object {
        //                               ( 2 & 6  )    (    3 & 7     )
        private const val PATTERN = "\\s?(\\d+°?|°)\\s?(\\d+(?:.\\d+)?)?'?"
        //                                    ( 1  )
        private const val PATTERN_LATITUDE = "([NS])$PATTERN"
        //                                     ( 5  )
        private const val PATTERN_LONGITUDE = "([EW])$PATTERN"
        //                                     (    4    )
        private const val PATTERN_SEPARATOR = "(\\s?[,|\\s+]\\s?)"

        @JvmField val REGEX_LATITUDE = PATTERN_LATITUDE.toRegex(RegexOption.IGNORE_CASE)
        @JvmField val REGEX_LONGITUDE = PATTERN_LONGITUDE.toRegex(RegexOption.IGNORE_CASE)
        @JvmField val REGEX_LAT_LON = "$PATTERN_LATITUDE$PATTERN_SEPARATOR$PATTERN_LONGITUDE".toRegex(RegexOption.IGNORE_CASE)    }

    override fun parseLatitude(text: String): Double {
        val values = REGEX_LATITUDE.find(text.trim())?.groupValues

        if(values!=null && values.size==4) {
            return createCoordinate(values[1], values[2].removeSuffix("°"), values[3], "")
        }

        throw CoordinateParseException("Can't parse latitude. Given input text '$text' doesn't match the pattern.")
    }

    override fun parseLongitude(text: String): Double {
        val values = REGEX_LONGITUDE.find(text.trim())?.groupValues

        if(values!=null && values.size==4) {
            return createCoordinate(values[1], values[2].removeSuffix("°"), values[3], "")
        }

        throw CoordinateParseException("Can't parse longitude. Given input text '$text' doesn't match the pattern.")
    }

    override fun parse(text: String): Coordinates {
        val values = REGEX_LAT_LON.find(text.trim())?.groupValues

        try {
            if(values!=null && values.size==8) {
                val latitude = parseLatitude(values[1] + values[2] + values[3])
                val longitude = parseLongitude(values[5] + values[6] + values[7])

                return Coordinates(latitude, longitude)
            }
        } catch(e: Exception) {
            throw CoordinateParseException("Can't parse coordinates.", e)
        }

        throw CoordinateParseException("Can't parse coordinates. Given input text '$text' doesn't match the pattern.")
    }
}