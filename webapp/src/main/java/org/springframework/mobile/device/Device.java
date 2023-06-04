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

/**
 * A model for the user agent or device that submitted the current request.
 * Callers may introspect this model to vary UI control or rendering logic by device type.
 * @author Keith Donald
 * @author Roy Clarkson
 * @author Scott Rossillo
 * @author Onur Kagan Ozcan
 */
public interface Device {

	/**
	 * True if this device is not a mobile or tablet device.
	 */
	default boolean isNormal() {
		return true;
	}

	/**
	 * True if this device is a mobile device such as an Apple iPhone or an Nexus One Android.
	 * Could be used by a pre-handle interceptor to redirect the user to a dedicated mobile web site.
	 * Could be used to apply a different page layout or stylesheet when the device is a mobile device.
	 */
	default boolean isMobile() {
		return false;
	}

	/**
	 * True if this device is a tablet device such as an Apple iPad or a Motorola Xoom.
	 * Could be used by a pre-handle interceptor to redirect the user to a dedicated tablet web site.
	 * Could be used to apply a different page layout or stylesheet when the device is a tablet device.
	 */
	default boolean isTablet() {
		return false;
	}

	/**
	 *
	 * @return resolved DevicePlatform
	 */
	default DevicePlatform getDevicePlatform() {
		return DevicePlatform.UNKNOWN;
	}

}
