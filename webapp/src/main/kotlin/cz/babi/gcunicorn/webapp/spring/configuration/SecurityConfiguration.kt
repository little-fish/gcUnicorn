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

import cz.babi.gcunicorn.webapp.spring.web.security.Securities
import cz.babi.gcunicorn.webapp.spring.web.security.ServiceAuthenticationProvider
import cz.babi.gcunicorn.webapp.spring.web.security.ServiceLogoutHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.Filter

/**
 * Security configuration.
 *
 * @param serviceAuthenticationProvider Service authentication provider.
 * @param serviceLogoutHandler Service logout handler.
 * @param characterEncodingFilter Character encoding filter.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = [Securities::class])
class SecurityConfiguration(@Autowired private val serviceAuthenticationProvider: ServiceAuthenticationProvider,
                            @Autowired private val serviceLogoutHandler: ServiceLogoutHandler,
                            @Autowired private val characterEncodingFilter: Filter) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth
                ?.authenticationProvider(serviceAuthenticationProvider)
    }

    override fun configure(http: HttpSecurity?) {
        http
                ?.headers()
                    ?.frameOptions()
                        ?.sameOrigin()
                        ?.and()
                ?.addFilterBefore(characterEncodingFilter, CsrfFilter::class.java)
                ?.authorizeRequests()
                    ?.antMatchers("/gcUnicorn/**")?.hasAuthority(ServiceAuthenticationProvider.ROLE)
                    ?.antMatchers("/login", "/logout", "/error/**", "/resources/**")?.permitAll()
                    ?.anyRequest()?.authenticated()
                    ?.and()
                ?.formLogin()
                    ?.loginPage("/login")
                    ?.defaultSuccessUrl("/gcUnicorn/search", true)
                    ?.failureUrl("/login?error")
                    ?.and()
                ?.logout()
                    ?.logoutRequestMatcher(AntPathRequestMatcher("/logout"))
                    ?.addLogoutHandler(serviceLogoutHandler)
                    ?.logoutSuccessUrl("/")
    }
}