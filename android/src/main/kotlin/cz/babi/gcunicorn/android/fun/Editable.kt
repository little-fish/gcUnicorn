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

package cz.babi.gcunicorn.android.`fun`

import android.text.Editable

/**
 * Extension method which convert underlying value into [Int] if possible.
 * @return [Int] value or _null_ if underlying value can not be casted to [Int].
 *
 * @author @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0.
 */
fun Editable?.toIntOrNull(): Int? = this?.toString()?.toIntOrNull()

/**
 * Extension method which convert underlying value into [Float] if possible.
 * @return [Float] value or _null_ if underlying value can not be casted to [Float].
 *
 * @author @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0.
 */
fun Editable?.toFloatOrNull(): Float? = this?.toString()?.toFloatOrNull()

/**
 * Extension method which convert underlying value into [Double] if possible.
 * @return [Double] value or _null_ if underlying value can not be casted to [Double].
 *
 * @author @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0.
 */
fun Editable?.toDoubleOrNull(): Double? = this?.toString()?.toDoubleOrNull()
