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

package cz.babi.gcunicorn.`fun`

object Constant {
    // XML 1.0
    // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    const val XML_10 = "[^" +
            "\u0009\r\n" +
            "\u0020-\uD7FF" +
            "\uE000-\uFFFD" +
            "\ud800\udc00-\udbff\udfff" +
            "]"

    // XML 1.1
    // [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    const val XML_11 = "[^" +
            "\u0001-\uD7FF" +
            "\uE000-\uFFFD" +
            "\ud800\udc00-\udbff\udfff" +
            "]"
}

/**
 * Checks whether underlying string contains any HTML sequence.
 * @return True if underlying string contains any HTML sequence. Otherwise returns false.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
fun String.containsHtml() = "<[^>]+>|&+".toRegex().containsMatchIn(this)

/**
 * Decode/encode underlying string with ROT13 algorithm.
 * @return Decoded/encoded underlying string.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
fun String.rot13(): String {
    val output = StringBuilder()

    for(element in this) {
        var c: Char = element

        when(c) {
            in 'a'..'m' -> c += 13
            in 'A'..'M' -> c += 13
            in 'n'..'z' -> c -= 13
            in 'N'..'Z' -> c -= 13
        }

        output.append(c)
    }

    return output.toString()
}

/**
 * Removes invalid characters and return text compatible with XML 1.0 specification.
 * @return Text compatible with XML 1.0 specification.
 */
fun String.validateXml10(): String {
    return this.replace(Constant.XML_10, "")
}

/**
 * Removes invalid characters and return text compatible with XML 1.1 specification.
 * @return Text compatible with XML 1.1 specification.
 */
fun String.validateXml11(): String {
    return this.replace(Constant.XML_11, "")
}
