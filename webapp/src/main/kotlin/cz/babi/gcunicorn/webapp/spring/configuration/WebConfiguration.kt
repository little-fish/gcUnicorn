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

package cz.babi.gcunicorn.webapp.spring.configuration

import cz.babi.gcunicorn.`fun`.logger
import cz.babi.gcunicorn.webapp.spring.validation.Validators
import cz.babi.gcunicorn.webapp.spring.web.advice.Advices
import cz.babi.gcunicorn.webapp.spring.web.controller.Controllers
import cz.babi.gcunicorn.webapp.spring.web.security.ServiceAuthenticationProvider
import io.undertow.Undertow
import io.undertow.server.DefaultByteBufferPool
import io.undertow.server.XnioBufferPoolAdaptor
import io.undertow.server.XnioByteBufferPool
import io.undertow.websockets.jsr.WebSocketDeploymentInfo
import jakarta.servlet.Filter
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor
import org.springframework.mobile.device.view.LiteDeviceDelegatingViewResolver
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.*
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.xnio.OptionMap
import org.xnio.Xnio
import java.util.*
import java.util.function.Supplier

/**
 * Web configuration.
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = [Controllers::class, Advices::class, Validators::class])
class WebConfiguration : WebMvcConfigurer {

    @Bean
    fun dispatcherServlet() = DispatcherServlet()

    @Bean
    fun undertowWebServerCustomizer() = WebServerFactoryCustomizer<UndertowServletWebServerFactory> { factory ->
        factory.addErrorPages(
                ErrorPage(HttpStatus.NOT_FOUND, "/error/404"),
                ErrorPage(HttpStatus.FORBIDDEN, "/error/403"),
                ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500")
        )

        factory.addDeploymentInfoCustomizers(UndertowDeploymentInfoCustomizer { deploymentInfo ->
                deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, WebSocketDeploymentInfo().apply {
                        worker = Supplier { Xnio.getInstance("nio", Undertow::class.java.classLoader).createWorker(OptionMap.builder().map) }
                        buffers = XnioByteBufferPool( XnioBufferPoolAdaptor(DefaultByteBufferPool(true, 16 * 1024)))
                })
        })
    }

    @Bean
    fun characterEncodingFilter(): Filter {
        return CharacterEncodingFilter().apply {
            encoding = Charsets.UTF_8.name()
            setForceEncoding(true)
        }
    }

    @Bean
    fun messageSource(): MessageSource {
        return ResourceBundleMessageSource().apply {
            setUseCodeAsDefaultMessage(true)
            setBasename("i18n/messages")
        }
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        return SessionLocaleResolver().apply {
            setDefaultLocale(Locale.ENGLISH)
        }
    }

    @Bean
    fun localeChangeInterceptor(): HandlerInterceptor {
        return LocaleChangeInterceptor().apply {
            paramName = "lang"
        }
    }

    @Bean
    fun deviceResolverInterceptor(): HandlerInterceptor {
        return DeviceResolverHandlerInterceptor()
    }

    @Bean
    fun templateResolver(): ClassLoaderTemplateResolver {
        return ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            templateMode = TemplateMode.HTML
            characterEncoding = Charsets.UTF_8.name()
        }
    }

    @Bean
    fun templateEngine(): SpringTemplateEngine {
        return SpringTemplateEngine().apply {
            setTemplateResolver(templateResolver())
        }
    }

    @Bean
    fun thymeleafViewResolver(): ThymeleafViewResolver {
        return ThymeleafViewResolver().apply {
            templateEngine = templateEngine()
            characterEncoding = Charsets.UTF_8.name()
        }
    }

    /**
     * Currently I have no device-specific pages, so we can point all pages to one location.<br>
     * Device specific views are handled by css rules.
     */
    @Bean
    fun liteDeviceDelegatingViewResolver(): LiteDeviceDelegatingViewResolver {
        return LiteDeviceDelegatingViewResolver(thymeleafViewResolver()).apply {
            setNormalPrefix("page/")
            setMobilePrefix("page/")
            setTabletPrefix("page/")
        }
    }

    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        registry.viewResolver(liteDeviceDelegatingViewResolver())
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
        registry.addInterceptor(deviceResolverInterceptor())
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("classpath:static/")
    }
}