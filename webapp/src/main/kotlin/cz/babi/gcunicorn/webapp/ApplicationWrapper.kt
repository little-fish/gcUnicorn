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

package cz.babi.gcunicorn.webapp

import cz.babi.gcunicorn.webapp.spring.configuration.ApplicationConfiguration
import cz.babi.gcunicorn.webapp.spring.configuration.SecurityConfiguration
import cz.babi.gcunicorn.webapp.spring.configuration.WebConfiguration
import cz.babi.gcunicorn.webapp.spring.configuration.WebSocketConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * Application wrapper.
 *
 * @since 1.0.0
 */
@Configuration
@Import(value = [ApplicationConfiguration::class, WebConfiguration::class, WebSocketConfiguration::class, SecurityConfiguration::class])
@EnableAutoConfiguration(
        exclude = [ThymeleafAutoConfiguration::class, WebSocketServletAutoConfiguration::class]
)
class ApplicationWrapper

/**
 * Entry point for gcUnicorn.
 * @param args Program arguments.
 */
fun main(args: Array<String>) {
    val springApplicationBuilder = SpringApplicationBuilder(ApplicationWrapper::class.java)
    springApplicationBuilder.headless(false).run(*args)
}