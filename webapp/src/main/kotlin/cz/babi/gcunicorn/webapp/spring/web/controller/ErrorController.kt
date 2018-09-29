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

package cz.babi.gcunicorn.webapp.spring.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

/**
 * Error controller.
 *
 * Handles requests to __/error/&#42;&#42;__ paths.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@Controller
@RequestMapping(path = ["/error"])
class ErrorController {

    @RequestMapping(path = ["/403"])
    fun error403(request: HttpServletRequest) = "error/403"

    @RequestMapping(path = ["/404"])
    fun error404(request: HttpServletRequest) = "error/404"

    @RequestMapping(path = ["/500"])
    fun error500(request: HttpServletRequest) = "error/500"
}