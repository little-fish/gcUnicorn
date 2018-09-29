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

    for(i in 0 until this.length) {
        var c: Char = this[i]

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