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

package cz.babi.gcunicorn.webapp.spring.web.security

import cz.babi.gcunicorn.core.exception.network.LogoutException
import cz.babi.gcunicorn.core.network.service.Service
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Component

/**
 * Logout handler.
 *
 * @param service Service to log out with.
 *
 * @since 1.0.0
 */
@Component
class ServiceLogoutHandler(private val service: Service) : LogoutHandler {

    override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        try {
            runBlocking {
                service.logout()
            }
        } catch(e: LogoutException) { /* Swallow the exception. */ }
    }
}