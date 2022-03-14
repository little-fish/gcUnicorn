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

package cz.babi.gcunicorn.core.network.model

import okhttp3.HttpUrl

/**
 * Class representing HTTP parameters.
 *
 * @throws [IllegalArgumentException] If given parameters contains odd number of items.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class HttpParameters(vararg items: String) {

    private val parameters: MutableList<Pair<String, String>> = mutableListOf()

    init {
        put(*items)
    }

    /**
     * Stores given items as parameters. It stores parameters in form: i=0: i -> key, i+1 -> value.
     * @param items even count of items.
     * @throws [IllegalArgumentException] If given parameters contains odd number of items.
     */
    fun put(vararg items: String) {
        if(items.size%2 != 0) throw IllegalArgumentException("You have provided odd number of parameters.")
        for(i in 0 until items.size-1 step 2) {
            parameters.add(Pair(items[i], items[i+1]))
        }
    }

    /**
     * Returns an item on given index.
     * @param index Index of requested item.
     * @return Item on given index.
     */
    fun get(index: Int) = parameters[index]

    /**
     * Returns all parameters.
     * @return All parameters.
     */
    fun getAll() = parameters

    /**
     * Returns count of current parameters.
     * @return Count of current parameters.
     */
    fun size() = parameters.size

    /**
     * Returns string of encoded parameters.
     * @return String of encoded parameters.
     */
    fun toQueryParam(): String {
        val queryBuilder = HttpUrl.parse("http://gcunicorn.com/")?.newBuilder()
        for(parameter in parameters) {
            queryBuilder?.addQueryParameter(parameter.first, parameter.second)
        }

        return queryBuilder?.build()?.encodedQuery() ?: ""
    }
}