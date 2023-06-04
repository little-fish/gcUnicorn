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

package cz.babi.gcunicorn.`fun`

/**
 * Executes given [notNullCallback] callback if the receiver is not null. Otherwise executes [nullCallback].
 *
 * @param notNullCallback Callback to be executed if the receiver is not null.
 * @param nullCallback Callback to be executed if the receiver is null.
 *
 * @since 1.0.0
 */
inline fun <T : Any> T?.nullableExecute(notNullCallback: T.() -> Unit, nullCallback: () -> Unit) { also { if (it!=null) notNullCallback(it) else nullCallback() } }

/**
 * Executes given [notNullCallback] callback if the receiver is not null and returns its result. Otherwise executes [nullCallback] and returns it result.
 *
 * @param notNullCallback Callback to be executed if the receiver is not null.
 * @param nullCallback Callback to be executed if the receiver is null.
 * @return Result of one of the given callbacks.
 *
 * @since 1.0.0
 */
inline fun <T : Any, R> T?.nullableReturn(notNullCallback: T.() -> R?, nullCallback: () -> R?): R? = let { if (it!=null) notNullCallback(it) else nullCallback() }