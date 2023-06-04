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

package org.springframework.mobile.device;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for resolving Devices that originate web requests with the application.
 * @author Keith Donald
 */
public interface DeviceResolver {

	/**
	 * Resolve the device that originated the web request.
	 */
	default Device resolveDevice(HttpServletRequest request) {
		return null;
	}

}
