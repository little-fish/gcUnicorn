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

package test.cz.babi.gcunicorn.core.location.parser.impl

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.core.location.parser.impl.DegreesDecimalMinuteParser
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

/**
 * Test class for [DegreesDecimalMinuteParser].
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
class TestDegreesDecimalMinuteParser {

    lateinit var parser: Parser
    lateinit var validCoordinates: List<String>
    lateinit var invalidCoordinates: List<String>

    @BeforeTest
    fun init() {
        parser = DegreesDecimalMinuteParser()
        validCoordinates = listOf(
                "N 18° 55', E 45° 55'",
                "N9° 55', E 45° 55",
                "s°55.55',w45°",
                "s°55.55' w45°"
        )

        invalidCoordinates = listOf(
                "N 189° 55', E 45° 55'",
                "N9° 555', E 45° 55",
                "s°55.55',45°"
        )
    }

    @Test
    fun parse_provideValidInputText_returnsCoordinates() {
        for(coordinate in validCoordinates) {
            Assert.assertNotNull(parser.parse(coordinate))
        }
    }

    @Test
    fun parse_provideInvalidInputText_exceptionIsThrown() {
        for(coordinate in invalidCoordinates) {
            Assert.expectThrows(CoordinateParseException::class.java, { parser.parse(coordinate) })
        }
    }
}