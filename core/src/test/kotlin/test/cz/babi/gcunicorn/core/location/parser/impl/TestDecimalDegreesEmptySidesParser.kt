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

package test.cz.babi.gcunicorn.core.location.parser.impl

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.core.location.parser.impl.DecimalDegreeEmptySidesParser
import cz.babi.gcunicorn.core.location.parser.impl.DecimalDegreesParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Test class for [DecimalDegreesParser].
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
class TestDecimalDegreesEmptySidesParser {

    lateinit var parser: Parser
    lateinit var validCoordinates: List<String>
    lateinit var invalidCoordinates: List<String>

    @BeforeEach
    fun init() {
        parser = DecimalDegreeEmptySidesParser()
        validCoordinates = listOf(
                "18,556°, 45.555°",
                "18, 45.555",
                " 18,556 , 45°",
                " 18.556° , 45°"
        )

        invalidCoordinates = listOf(
                "N 18,556°. E 45.555°",
                "e18, 45.555",
                " 18,556 , E 45°",
                " 18.556° , °"
        )
    }

    @Test
    fun parse_provideValidInputText_returnsCoordinates() {
        for(coordinate in validCoordinates) {
            Assertions.assertNotNull(parser.parse(coordinate))
        }
    }

    @Test
    fun parse_provideInvalidInputText_exceptionIsThrown() {
        for(coordinate in invalidCoordinates) {
            Assertions.assertThrowsExactly(CoordinateParseException::class.java) { parser.parse(coordinate) }
        }
    }
}