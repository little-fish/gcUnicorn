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

package cz.babi.gcunicorn.webapp.spring.configuration

import cz.babi.gcunicorn.core.location.parser.impl.*
import cz.babi.gcunicorn.core.network.InMemoryCookieJar
import cz.babi.gcunicorn.core.network.Network
import cz.babi.gcunicorn.core.network.interceptor.HeaderInterceptor
import cz.babi.gcunicorn.core.network.interceptor.LoggingInterceptor
import cz.babi.gcunicorn.core.network.service.geocachingcom.GeoCachingCom
import cz.babi.gcunicorn.webapp.desktop.Tray
import cz.babi.gcunicorn.webapp.desktop.TrayCondition
import cz.babi.gcunicorn.webapp.entity.task.JobsWrapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * Application configuration.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.2
 * @since 1.0.0
 */
@Configuration
class ApplicationConfiguration {

    @Bean
    fun headerInterceptor() = HeaderInterceptor()

    @Bean
    fun loggingInterceptor() = LoggingInterceptor()

    @Bean
    fun cookieJar() = InMemoryCookieJar()

    @Bean
    fun okHttpClient(headerInterceptor: Interceptor, loggingInterceptor: Interceptor, cookieJar: CookieJar): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .cookieJar(cookieJar)
            .addNetworkInterceptor(headerInterceptor)
            .addNetworkInterceptor(loggingInterceptor)
            .build()

    @Bean
    fun network() = Network(okHttpClient(headerInterceptor(), loggingInterceptor(), cookieJar()))

    @Bean
    fun degreesDecimalMinuteParser() = DegreesDecimalMinuteParser()

    @Bean
    fun decimalDegreesParser() = DecimalDegreesParser()

    @Bean
    fun decimalDegreeRightSideParser() = DecimalDegreesRightSideParser()

    @Bean
    fun decimalDegreesEmptySidesParser() = DecimalDegreeEmptySidesParser()

    @Bean(name = ["parser"])
    fun parsers() = ParserWrapper(decimalDegreesParser(), degreesDecimalMinuteParser(), decimalDegreesEmptySidesParser(), decimalDegreeRightSideParser())

    @Bean
    fun json() = Json(JsonConfiguration.Stable)

    @Bean
    fun service() = GeoCachingCom(network(), parsers(), json())

    /** The bean is a good candidate for session scoped component. But the component is used during WebSocket communication so there is no way to obtain it from the session. */
    @Bean
    fun jobsWrapper() = JobsWrapper()

    @Bean
    @Conditional(value = [TrayCondition::class])
    fun trayTooltip() = "${ApplicationConfiguration::class.java.`package`?.implementationTitle ?: "unknown"} v${ApplicationConfiguration::class.java.`package`?.implementationVersion ?: "unknown"}"

    @Bean
    @Conditional(value = [TrayCondition::class])
    fun trayImage(resourceLoader: ResourceLoader) = ImageIO.read(resourceLoader.getResource("classpath:img/tray.png").inputStream)!!

    @Bean
    @Conditional(value = [TrayCondition::class])
    fun tray(applicationContext: ApplicationContext, messageSource: MessageSource, resourceLoader: ResourceLoader) = Tray(trayImage(resourceLoader), trayTooltip(), applicationContext, messageSource)
}