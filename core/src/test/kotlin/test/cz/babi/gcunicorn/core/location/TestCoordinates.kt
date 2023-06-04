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

package test.cz.babi.gcunicorn.core.location

import cz.babi.gcunicorn.core.location.Coordinates
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Test class for [Coordinates].
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
class TestCoordinates {

    @Test
    fun constructor_provideValidCoordinates_objectIsCreated() {
        Assertions.assertNotNull(Coordinates(15.555, 179.115))
    }

    @Test
    fun constructor_provideInvalidCoordinates_exceptionIsThrown() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { Coordinates(86.156, 15.55) }
    }
}