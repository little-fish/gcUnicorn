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

import cz.babi.gcunicorn.webapp.spring.web.security.Securities
import cz.babi.gcunicorn.webapp.spring.web.security.ServiceAuthenticationProvider
import cz.babi.gcunicorn.webapp.spring.web.security.ServiceLogoutHandler
import jakarta.servlet.Filter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher

/**
 * Security configuration.
 *
 * @param serviceAuthenticationProvider Service authentication provider.
 * @param serviceLogoutHandler Service logout handler.
 * @param characterEncodingFilter Character encoding filter.
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = [Securities::class])
class WebSecurityConfiguration(
    @param:Autowired private val serviceAuthenticationProvider: ServiceAuthenticationProvider,
    @param:Autowired private val serviceLogoutHandler: ServiceLogoutHandler,
    @param:Autowired private val characterEncodingFilter: Filter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain? {
        return http
            .headers { headers ->
                headers.frameOptions {
                    it.sameOrigin()
                }
            }
            .addFilterBefore(characterEncodingFilter, CsrfFilter::class.java)
            .authorizeHttpRequests {
                it.requestMatchers("/ws/gcUnicorn/**").permitAll()
                    .requestMatchers("/gcUnicorn/**").hasAuthority(ServiceAuthenticationProvider.ROLE)
                    .requestMatchers("/login", "/logout", "/error/**", "/resources/**").permitAll()
                    .anyRequest().authenticated()
            }
            .csrf { _ ->
//                csrf.ignoringRequestMatchers("/ws/gcUnicorn/**")
            }
            .formLogin {
                it.loginPage("/login")
                    .defaultSuccessUrl("/gcUnicorn/search", true)
                    .failureUrl("/login?error")
            }
            .logout {
                it.logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                    .addLogoutHandler(serviceLogoutHandler)
                    .logoutSuccessUrl("/")
            }
            .authenticationProvider(serviceAuthenticationProvider)
            .build()
    }
}