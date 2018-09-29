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

package cz.babi.gcunicorn.webapp.spring.web.security

import cz.babi.gcunicorn.`fun`.loggerFor
import cz.babi.gcunicorn.core.exception.network.LoginException
import cz.babi.gcunicorn.core.network.model.Credentials
import cz.babi.gcunicorn.core.network.service.Service
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

/**
 * Authentication provider which tries to log in into given service.
 *
 * @param service Service to log in with.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class ServiceAuthenticationProvider(private val service: Service) : AuthenticationProvider {

    companion object {
        private val LOG = loggerFor<ServiceAuthenticationProvider>()
        const val ROLE = "gcuser"
    }

    override fun authenticate(authentication: Authentication?): Authentication? {
        authentication?.let {
            try {
                service.login(Credentials(it.name, it.credentials.toString()))

                return UsernamePasswordAuthenticationToken(it.name, it.credentials, listOf(SimpleGrantedAuthority(ROLE)))
            } catch(e: LoginException) {
                LOG.warn("Can not log in with provided credentials: '${it.name}:***'.", e)
            } catch(e: IllegalArgumentException) {
                LOG.warn("Can not log in with invalid credentials.", e)
            }
        }

        return null
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication?.equals(UsernamePasswordAuthenticationToken::class.java) ?: false
    }
}