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

package cz.babi.gcunicorn.core.location.parser

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.Coordinates

/**
 * Coordinates parser interface.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
interface Parser {

    /**
     * Parses latitude from given text.
     * @param text Text to be parsed.
     * @return Parsed latitude.
     * @throws CoordinateParseException If input text can not be parsed or parsed coordinate is not within proper ranges.
     */
    @Throws(CoordinateParseException::class)
    fun parseLatitude(text: String): Double

    /**
     * Parses longitude from given text.
     * @param text Text to be parsed.
     * @return Parsed longitude.
     * @throws CoordinateParseException If input text can not be parsed or parsed coordinate is not within proper ranges.
     */
    @Throws(CoordinateParseException::class)
    fun parseLongitude(text: String): Double

    /**
     * Parses coordinates from given text.
     * @param text Text to be parsed.
     * @return Parsed coordinates.
     * @throws CoordinateParseException If input text can not be parsed or parsed coordinates are not within proper ranges.
     */
    @Throws(CoordinateParseException::class)
    fun parse(text: String): Coordinates

    /**
     * Create latitude/longitude out of given degrees, minutes, seconds and sign.
     * @param signGroup A sign of the coordinate.
     * @param degreeGroup A degrees of the coordinate.
     * @param minuteGroup A minutes of the coordinate.
     * @param secondGroup A seconds of the coordinate.
     * @return The latitude/longitude calculated out of the given fields.
     * @throws [CoordinateParseException] If anything goes wrong.
     * @author c:geo
     */
    @Throws(CoordinateParseException::class)
    fun createCoordinate(signGroup: String, degreeGroup: String, minuteGroup: String, secondGroup: String): Double {
        try {
            val seconds = if(secondGroup.isEmpty()) 0.0 else secondGroup.replace(",", ".").toDouble()
            if(seconds>=60.0) throw CoordinateParseException("Given seconds are greater or equal to 60. Given value is '$seconds'.")

            val minutes = if(minuteGroup.isEmpty()) 0.0 else minuteGroup.replace(",", ".").toDouble()
            if(minutes>=60) throw CoordinateParseException("Given minutes are greater or equal to 60. Given value is '$minutes'.")

            val degrees = if(degreeGroup.isEmpty()) 0.0 else degreeGroup.replace(",", ".").toDouble()

            val sign = if(signGroup.equals("S", true) || signGroup.equals("W", true)) -1.0 else 1.0

            return sign * (degrees + minutes/60 + seconds/3600)
        } catch(e: NumberFormatException) {
            throw CoordinateParseException("Can not create coordinate from given values.", e)
        }
    }
}