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
import cz.babi.gcunicorn.core.location.parser.Parser

/**
 * Wrapper which goes through available [Parser]s and tries one by one.
 *
 * Once a parser parses given text without errors, it returns parsed coordinates.
 *
 * @param parsers Parses that the wrapper should use for parsing coordinates.
 *
 * @since 1.0.0
 */
class ParserWrapper(vararg parsers: Parser) : Parser {
    private val parsers = parsers.asList()

    /**
     * Goes through available parsers and tries one by one.
     *
     * Once a parser parses given text without errors, it returns parsed latitude.
     * @param text Text to be parsed.
     * @return Parsed latitude.
     * @throws [CoordinateParseException] If no parsers are available or if no parser was able to parse given text.
     */
    @Throws(CoordinateParseException::class)
    override fun parseLatitude(text: String): Double {
        for(parser in parsers) {
            try {
                return parser.parseLatitude(text)
            } catch(e: CoordinateParseException) {}
        }

        throw CoordinateParseException("Can not parse given text with provided parsers.")
    }

    /**
     * Goes through available parsers and tries one by one.
     *
     * Once a parser parses given text without errors, it returns parsed longitude.
     * @param text Text to be parsed.
     * @return Parsed longitude.
     * @throws [CoordinateParseException] If no parsers are available or if no parser was able to parse given text.
     */
    @Throws(CoordinateParseException::class)
    override fun parseLongitude(text: String): Double {
        for(parser in parsers) {
            try {
                return parser.parseLongitude(text)
            } catch(e: CoordinateParseException) {}
        }

        throw CoordinateParseException("Can not parse given text with provided parsers.")
    }

    /**
     * Goes through available parsers and tries one by one.
     *
     * Once a parser parses given text without errors, it returns parsed coordinates.
     * @param text Text to be parsed.
     * @return Parsed coordinate.
     * @throws [CoordinateParseException] If no parsers are available or if no parser was able to parse given text.
     */
    @Throws(CoordinateParseException::class)
    override fun parse(text: String): Coordinates {
        for(parser in parsers) {
            try {
                return parser.parse(text)
            } catch(e: CoordinateParseException) {}
        }

        throw CoordinateParseException("Can not parse given text with provided parsers.")
    }
}