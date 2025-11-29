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

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager

/**
 * Web Socket Security configuration.
 *
 * @since 3.0.1
 */
@Configuration
@EnableWebSocketSecurity
class WebSocketSecurityConfiguration {

    @Bean
    fun authorizationManager(messages: MessageMatcherDelegatingAuthorizationManager.Builder): AuthorizationManager<Message<*>?> {
        return messages
//            .simpTypeMatchers(
//                SimpMessageType.CONNECT,
//            ).permitAll()

            // 1. Authorize connection and disconnection messages.
            // CONNECT, SUBSCRIBE, UNSUBSCRIBE, HEARTBEAT must be authenticated (via session context).
            .simpTypeMatchers(
                SimpMessageType.CONNECT,
                SimpMessageType.SUBSCRIBE,
                SimpMessageType.UNSUBSCRIBE,
                SimpMessageType.DISCONNECT
            ).authenticated()

            // 2. Authorize messages sent to the application.
            .simpDestMatchers("/ws/gcUnicorn/**").authenticated()

            // 3. Authorize messages sent to the Simple Broker.
            .simpDestMatchers("/topic/**").authenticated()

            // 4. Deny all other message types.
            .anyMessage().denyAll()
            .build()
    }

//    @Bean
//    fun csrfChannelInterceptor(): ChannelInterceptor? {
//        return object : ChannelInterceptor { }
//    }
}