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

package cz.babi.gcunicorn.webapp.spring.web.advice

import cz.babi.gcunicorn.core.network.service.Service
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * The advice adds following attributes to the model:
 * * __versionCore__ - core version (loaded from core's manifest file)
 * * __versionServer__ - webapp version (loaded from webapp's manifest file)
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@ControllerAdvice
class VersionControllerAdvice {

    @ModelAttribute(name = "versionCore")
    fun versionCore() = Service::class.java.`package`?.implementationVersion ?: "unknown"

    @ModelAttribute(name = "versionWebapp")
    fun versionWebapp() = VersionControllerAdvice::class.java.`package`?.implementationVersion ?: "unknown"
}