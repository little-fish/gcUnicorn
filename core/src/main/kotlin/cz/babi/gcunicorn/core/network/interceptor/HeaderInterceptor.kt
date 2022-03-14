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

package cz.babi.gcunicorn.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor which adds additional header parameters into the request.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .header("Accept-Charset", "utf-8,iso-8859-1;q=0.8,utf-16;q=0.8,*;q=0.7")
                .header("Accept-Language", "en-US,*;q=0.9")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:9.0.1) Gecko/20100101 Firefox/9.0.1")
                .build()

        return chain.proceed(request)
    }
}