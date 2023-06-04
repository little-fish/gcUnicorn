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

package org.springframework.mobile.device.util;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.site.SitePreference;

/**
 * Static helper for determining how to handle the combination of device and
 * site preference.
 * @author Roy Clarkson
 */
public class ResolverUtils {

	/**
	 * Should the combination of {@link Device} and {@link SitePreference} be handled
	 * as a normal device
	 * @param device the resolved device
	 * @param sitePreference the specified site preference
	 * @return true if normal
	 */
	public static boolean isNormal(Device device, SitePreference sitePreference) {
		return sitePreference == SitePreference.NORMAL ||
				(device == null || device.isNormal() && sitePreference == null);
	}

	/**
	 * Should the combination of {@link Device} and {@link SitePreference} be handled
	 * as a mobile device
	 * @param device the resolved device
	 * @param sitePreference the specified site preference
	 * @return true if mobile
	 */
	public static boolean isMobile(Device device, SitePreference sitePreference) {
		return sitePreference == SitePreference.MOBILE || device != null && device.isMobile() && sitePreference == null;
	}

	/**
	 * Should the combination of {@link Device} and {@link SitePreference} be handled
	 * as a tablet device
	 * @param device the resolved device
	 * @param sitePreference the specified site preference
	 * @return true if tablet
	 */
	public static boolean isTablet(Device device, SitePreference sitePreference) {
		return sitePreference == SitePreference.TABLET || device != null && device.isTablet() && sitePreference == null;
	}

	private ResolverUtils() {

	}

}
