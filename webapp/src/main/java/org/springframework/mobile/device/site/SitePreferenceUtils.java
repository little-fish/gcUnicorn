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
import org.springframework.web.context.request.RequestAttributes;

import static org.springframework.mobile.device.site.SitePreferenceHandler.CURRENT_SITE_PREFERENCE_ATTRIBUTE;

/**
 * Static helper for accessing request-scoped SitePreference values.
 * @author Keith Donald
 */
public class SitePreferenceUtils {

	/**
	 * Get the current site preference for the user that originated this web request.
	 * @return the site preference, or null if none has been set
	 */
	public static SitePreference getCurrentSitePreference(HttpServletRequest request) {
		return (SitePreference) request.getAttribute(CURRENT_SITE_PREFERENCE_ATTRIBUTE);
	}

	/**
	 * Get the current site preference for the user from the request attributes map.
	 * @return the site preference, or null if none has been set
	 */
	public static SitePreference getCurrentSitePreference(RequestAttributes attributes) {
		return (SitePreference) attributes.getAttribute(CURRENT_SITE_PREFERENCE_ATTRIBUTE,
				RequestAttributes.SCOPE_REQUEST);
	}

	private SitePreferenceUtils() {

	}

}
