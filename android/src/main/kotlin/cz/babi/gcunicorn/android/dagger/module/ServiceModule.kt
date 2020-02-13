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

package cz.babi.gcunicorn.android.dagger.module

import android.os.Build
import android.util.Log
import cz.babi.gcunicorn.android.network.Tls12SocketFactory
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.core.location.parser.impl.*
import cz.babi.gcunicorn.core.network.InMemoryCookieJar
import cz.babi.gcunicorn.core.network.Network
import cz.babi.gcunicorn.core.network.interceptor.HeaderInterceptor
import cz.babi.gcunicorn.core.network.interceptor.LoggingInterceptor
import cz.babi.gcunicorn.core.network.service.Service
import cz.babi.gcunicorn.core.network.service.geocachingcom.GeoCachingCom
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.security.KeyStore
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Module that provides service related dependencies.
 *
 * @see [Service]
 * @see [GeoCachingCom]
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@Module
abstract class ServiceModule {

    @Binds
    abstract fun bindsService(geoCachingCom: GeoCachingCom): Service

    @Binds
    abstract fun bindsParser(parserWrapper: ParserWrapper): Parser

    @Module
    companion object {

        @Provides
        @Singleton
        @JvmStatic
        fun providesService(network: Network, parserWrapper: ParserWrapper, json: Json) = GeoCachingCom(network, parserWrapper, json)

        @Provides
        @Singleton
        @JvmStatic
        fun providesOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .cookieJar(InMemoryCookieJar())
                    .addNetworkInterceptor(HeaderInterceptor())
                    .addNetworkInterceptor(LoggingInterceptor())

            applyTlsPatch(builder)

            return builder.build()
        }

        @Provides
        @Singleton
        @JvmStatic
        fun providesNetwork(okHttpClient: OkHttpClient) = Network(okHttpClient)

        @Provides
        @Singleton
        @JvmStatic
        fun providesParserWrapper() = ParserWrapper(
                DecimalDegreeEmptySidesParser(),
                DecimalDegreesRightSideParser(),
                DecimalDegreesParser(),
                DegreesDecimalMinuteParser()
        )

        @Provides
        @Singleton
        @JvmStatic
        fun providesJson() = Json(JsonConfiguration.Stable)

        private fun applyTlsPatch(builder: OkHttpClient.Builder) {
            if (Build.VERSION.SDK_INT in Build.VERSION_CODES.JELLY_BEAN..Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // Trusted manager.
                    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                        init(null as KeyStore?)
                    }

                    val trustManagers = trustManagerFactory.trustManagers
                    if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                        throw IllegalStateException("Unexpected default trust managers: ${Arrays.toString(trustManagers)}.")
                    }

                    // SSL context.
                    val sslContext = SSLContext.getInstance(Tls12SocketFactory.TLS_V12).apply {
                        init(null, arrayOf(trustManagers[0]), null)
                    }

                    builder.sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustManagers[0] as X509TrustManager)

                    val connectionSpecModern = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build()

                    builder.connectionSpecs(listOf(
                            connectionSpecModern,
                            ConnectionSpec.COMPATIBLE_TLS,
                            ConnectionSpec.CLEARTEXT
                    ))
                } catch (e: Exception) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", e)
                }
            }
        }
    }
}