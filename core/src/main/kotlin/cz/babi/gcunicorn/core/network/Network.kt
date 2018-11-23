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

import cz.babi.gcunicorn.`fun`.logger
import cz.babi.gcunicorn.core.exception.network.NetworkException
import cz.babi.gcunicorn.core.network.model.HttpParameters
import kotlinx.io.IOException
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.Logger
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Implementation making any HTTP requests.
 *
 * @param okHttpClient Underlying http client.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class Network(private val okHttpClient: OkHttpClient) {

    /**
     * Represents HTTP methods.
     */
    enum class Method { POST, GET }

    companion object {
        private val LOG: Logger = logger<Network>()
    }

    /**
     * Clears cookies.
     */
    fun clearCookies() {
        val cookieJar = okHttpClient.cookieJar()

        if(cookieJar is DefaultCookieJar) {
            cookieJar.clearCookies()
        }
    }

    /**
     * Creates new instance of [Request.Builder].
     * @return new Request builder.
     */
    private fun createBuilder() = Request.Builder()

    /**
     * Makes new [Method.POST] request to given uri.
     * @param uri URI to make new [Method.POST] request to.
     * @return An HTTP response.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun postRequest(uri: String): Response = postRequest(uri, null, null)

    /**
     * Makes new [Method.POST] request to given uri.
     * @param uri URI to make new [Method.POST] request to.
     * @param parameters HTTP parameters.
     * @param headers HTTP headers.
     * @return An HTTP response.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun postRequest(uri: String, parameters: HttpParameters?, headers: HttpParameters?) = request(Method.POST, uri, parameters, headers)

    /**
     * Makes new [Method.GET] request to given uri.
     * @param uri URI to make new [Method.GET] request to.
     * @return An HTTP response.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun getRequest(uri: String): Response = getRequest(uri, null, null)

    /**
     * Makes new [Method.GET] request to given uri.
     * @param uri URI to make new [Method.GET] request to.
     * @param parameters HTTP parameters.
     * @param headers HTTP headers.
     * @return An HTTP response.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun getRequest(uri: String, parameters: HttpParameters?, headers: HttpParameters?) = request(Method.GET, uri, parameters, headers)

    /**
     * Decode given text with [Charsets.UTF_8] encoding.
     * @param text Text to decode.
     * @return Decoded text or null if text can not be decoded.
     */
    fun decode(text: String): String? {
        return try {
            URLDecoder.decode(text, Charsets.UTF_8.displayName())
        } catch(e: UnsupportedEncodingException) {
            LOG.warn("Can not decode given text: '{}'.", text)
            null
        }
    }

    /**
     * Returns request.
     * @param method [Network.Method] method.
     * @param uri URI.
     * @param parameters HTTP parameters.
     * @param headers HTTP headers.
     * @return Response.
     * @throws [NetworkException] If the request has been already executed or if the request can not be executed due timeout, cancellation or network issue or if given uri can not be parsed.
     */
    @Throws(NetworkException::class)
    private fun request(method: Method, uri: String, parameters: HttpParameters?, headers: HttpParameters?): Response {
        val httpUrl = HttpUrl.parse(uri)

        if(httpUrl!=null) {
            val requestBuilder = createBuilder()

            when (method) {
                Method.GET -> {
                    val urlBuilder = httpUrl.newBuilder()

                    if (parameters != null) {
                        urlBuilder.encodedQuery(parameters.toQueryParam())
                    }

                    requestBuilder.url(urlBuilder.build())
                }
                Method.POST -> {
                    requestBuilder.url(uri)

                    val formBuilder = FormBody.Builder()
                    if (parameters != null) {
                        for (pair in parameters.getAll()) {
                            formBuilder.add(pair.first, pair.second)
                        }
                    }

                    requestBuilder.post(formBuilder.build())
                }
            }

            headers?.getAll()?.forEach { header ->
                requestBuilder.header(header.first, header.second)
            }

            try {
                return okHttpClient.newCall(requestBuilder.build()).execute()
            } catch (ioException: IOException) {
                throw NetworkException("The request can not be executed due timeout, cancellation or network issue.", ioException)
            } catch (illegalStateException: IllegalStateException) {
                throw NetworkException("The request has been executed already.", illegalStateException)
            }
        } else {
            throw NetworkException("Can not parse given URL: '$uri'.")
        }
    }

    /**
     * Tries to obtain response body from given [Response].
     * @param response Response obtained from http call.
     * @return Response body as plain string.
     * @throws NetworkException If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun getResponseStringBody(response: Response): String {
        if(!response.isSuccessful) {
            throw NetworkException("Request was not successful. Returned code is '${response.code()}'.")
        }

        try {
            return response.body()?.string() ?: throw NetworkException("Can't obtain response body.")
        } catch (e: Exception) {
            throw NetworkException("Can't obtain response body.", e)
        } finally {
            response.body()?.close()
        }
    }

    /**
     * Tries to obtain response body as byte stream from given [Response].
     *
     * __The caller is responsible for closing the stream.__
     * @param response Response obtained from http call.
     * @return Response body as byte stream.
     * @throws NetworkException If anything goes wrong.
     */
    @Throws(NetworkException::class)
    fun getResponseByteStreamBody(response: Response): InputStream {
        if(!response.isSuccessful) {
            throw NetworkException("Request was not successful. Returned code is '${response.code()}'.")
        }

        try {
            return response.body()?.byteStream() ?: throw NetworkException("Can't obtain response body.")
        } catch (e: Exception) {
            throw NetworkException("Can't obtain response body.")
        }
    }
}