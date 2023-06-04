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
import org.springframework.web.context.request.RequestAttributes;

/**
 * Static helper for accessing request-scoped Device values.
 * @author Keith Donald
 */
public class DeviceUtils {

	/**
	 * The name of the request attribute the current Device is indexed by.
	 * The attribute name is 'currentDevice'.
	 */
	public static final String CURRENT_DEVICE_ATTRIBUTE = "currentDevice";

	/**
	 * Static utility method that extracts the current device from the web request.
	 * Encapsulates the {@link HttpServletRequest#getAttribute(String)} lookup.
	 * @param request the servlet request
	 * @return the current device, or null if no device has been resolved for the request
	 */
	public static Device getCurrentDevice(HttpServletRequest request) {
		return (Device) request.getAttribute(CURRENT_DEVICE_ATTRIBUTE);
	}

	/**
	 * Static utility method that extracts the current device from the web request.
	 * Encapsulates the {@link HttpServletRequest#getAttribute(String)} lookup.
	 * Throws a runtime exception if the current device has not been resolved.
	 * @param request the servlet request
	 * @return the current device
	 */
	public static Device getRequiredCurrentDevice(HttpServletRequest request) {
		Device device = getCurrentDevice(request);
		if (device == null) {
			throw new IllegalStateException("No current device is set in this request and one is required - have you configured a DeviceResolverHandlerInterceptor?");
		}
		return device;
	}

	/**
	 * Static utility method that extracts the current device from the request attributes map.
	 * Encapsulates the {@link HttpServletRequest#getAttribute(String)} lookup.
	 * @param attributes the request attributes
	 * @return the current device, or null if no device has been resolved for the request
	 */
	public static Device getCurrentDevice(RequestAttributes attributes) {
		return (Device) attributes.getAttribute(CURRENT_DEVICE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
	}


}
