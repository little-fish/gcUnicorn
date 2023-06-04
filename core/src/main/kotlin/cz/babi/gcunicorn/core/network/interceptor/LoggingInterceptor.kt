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

package cz.babi.gcunicorn.core.network.interceptor

import cz.babi.gcunicorn.`fun`.logger
import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger
import java.io.IOException
import java.util.regex.Pattern

/**
 * Interceptors which logs oncoming requests.
 *
 * @since 1.0.0
 */
class LoggingInterceptor : Interceptor {

    companion object {
        private val LOG: Logger = logger<LoggingInterceptor>()
        private val PATTERN_PASSWORD = Pattern.compile("(?<=[?&])[Pp]ass(w(or)?d)?=[^&#$]+")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestMethod = request.method
        val requestUrl = hidePassword(request.url.toString())
        val now = System.currentTimeMillis()

        try {
            val response = chain.proceed(request)
            val responseProtocol = "${response.protocol}"
            val redirect = if(request.url == response.request.url) "" else " redirected to: '${response.request.url}'"

            if(response.isSuccessful || response.code ==302) {
                LOG.debug("{} - for request: '{} ({}): {}{}'; duration: {}.",
                    response.code, requestMethod, responseProtocol, requestUrl, redirect, getDuration(now))
            } else {
                LOG.warn("{} - for request: '{} ({}): {}{}'; with response: '{}'; duration: {}.",
                    response.code, requestMethod, responseProtocol, requestUrl, redirect, response.message, getDuration(now))
            }

            return response
        } catch (exception: IOException) {
            LOG.warn("Failure with request: '{}'.", requestUrl, exception)

            throw exception
        }
    }

    /**
     * Hides password from given message.
     * @param message Message to be obfuscated.
     * @return Obfuscated message.
     */
    private fun hidePassword(message: String) = PATTERN_PASSWORD.matcher(message).replaceAll("password=***")

    /**
     * Obtain duration between 'now' and passed time in milliseconds.
     * @param before Time in milliseconds.
     * @return Duration between 'now' and passed time.
     */
    private fun getDuration(before: Long) = "${System.currentTimeMillis() - before} ms"
}