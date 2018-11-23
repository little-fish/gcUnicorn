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

package cz.babi.gcunicorn.core.network

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * In-memory implementation of Cookie policy.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class InMemoryCookieJar : DefaultCookieJar {

    private val storage = mutableMapOf<String, Cookie>()

    override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
        cookies?.forEach { cookie ->
            val key = cookie.domain() + ";" + cookie.name()
            storage[key] = cookie
        }
    }

    override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
        dumpOldCookies()

        val cookies = mutableListOf<Cookie>()

        url?.let {
            for (cookie: Cookie in storage.values) {
                if (cookie.matches(url)) {
                    cookies.add(cookie)
                }
            }
        }

        return cookies
    }

    override fun clearCookies() = storage.clear()

    /**
     * Dump old cookies.
     */
    private fun dumpOldCookies() {
        val iterator = storage.values.iterator()

        while (iterator.hasNext()) {
            val cookie = iterator.next()
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                iterator.remove()
            }
        }
    }
}