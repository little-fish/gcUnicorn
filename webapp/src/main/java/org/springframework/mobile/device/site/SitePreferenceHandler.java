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

package org.springframework.mobile.device.site;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mobile.device.Device;

/**
 * Service interface for site preference management.
 * @author Keith Donald
 */
public interface SitePreferenceHandler {

	/**
	 * The name of the request attribute that holds the current user's site preference value.
	 */
	final String CURRENT_SITE_PREFERENCE_ATTRIBUTE = "currentSitePreference";

	/**
	 * Handle the site preference aspect of the web request.
	 * Implementations should check if the user has indicated a site preference.
	 * If so, the indicated site preference should be saved and remembered for future requests.
	 * If no site preference has been indicated, an implementation may derive a default site preference from the {@link Device} that originated the request.
	 * After handling, the resolved site preference is available as a {@link #CURRENT_SITE_PREFERENCE_ATTRIBUTE request attribute}.
	 * @param request the web request
	 * @param response the web response
	 * @return the resolved site preference for the user that originated the web request
	 */
	SitePreference handleSitePreference(HttpServletRequest request, HttpServletResponse response);

}
