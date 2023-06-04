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

/**
 * Possible site preference values.
 * @author Keith Donald
 * @author Roy Clarkson
 */
public enum SitePreference {
	
	/**
	 * The user prefers the 'normal' site.
	 */
	NORMAL {
		public boolean isNormal() {
			return true;
		}
	},
	
	/**
	 * The user prefers the 'mobile' site.
	 */
	MOBILE {		
		public boolean isMobile() {
			return true;
		}
	},
	
	/**
	 * The user prefers the 'tablet' site.
	 */
	TABLET {		
		public boolean isTablet() {
			return true;
		}
	};
	
	/**
	 * Tests if this is the 'normal' SitePreference.
	 * Designed to support concise SitePreference boolean expressions e.g. &lt;c:if test="${currentSitePreference.normal}"&gt;&lt;/c:if&gt;.
	 */
	public boolean isNormal() {
		return (!isMobile() && !isTablet());
	}

	/**
	 * Tests if this is the 'mobile' SitePreference.
	 * Designed to support concise SitePreference boolean expressions e.g. &lt;c:if test="${currentSitePreference.mobile}"&gt;&lt;/c:if&gt;.
	 */
	public boolean isMobile() {
		return false;
	}
	
	/**
	 * Tests if this is the 'tablet' SitePreference.
	 * Designed to support concise SitePreference boolean expressions e.g. &lt;c:if test="${currentSitePreference.tablet}"&gt;&lt;/c:if&gt;.
	 */
	public boolean isTablet() {
		return false;
	}
	
}
