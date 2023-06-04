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

package org.springframework.mobile.device.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceUtils;
import org.springframework.mobile.device.util.ResolverUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

/**
 * A lightweight {@link AbstractDeviceDelegatingViewResolver} for adjusting a
 * view based on the combination of resolved {@link Device} and specified
 * {@link SitePreference}. View names can be augmented with a specified prefix
 * or suffix.
 *
 * <p>Specify the prefix for the different device types. Default prefixes are
 * empty strings. For the requested view name of "home", the following table
 * illustrates how the view name will be adjusted based on device type.</p>
 *
 * <table border=1 cellpadding=3 cellspacing=0>
 *   <tr>
 *     <td>Resolved Device</td>
 *     <td>Method</td>
 *     <td>Prefix</td>
 *     <td>Adjusted View</td>
 *   </tr>
 *   <tr>
 *     <td>Normal</td>
 *     <td>{@link #setNormalPrefix(String)}</td>
 *     <td>"normal/"</td>
 *     <td>"normal/home"</td>
 *   </tr>
 *   <tr>
 *     <td>Mobile</td>
 *     <td>{@link #setMobilePrefix(String)}</td>
 *     <td>"mobile/"</td>
 *     <td>"mobile/home"</td>
 *   </tr>
 *   <tr>
 *     <td>Tablet</td>
 *     <td>{@link #setTabletPrefix(String)}</td>
 *     <td>"tablet/"</td>
 *     <td>"tablet/home"</td>
 *   </tr>
 * </table>
 *
 * <p>Alternatively, you may want to have the views adjusted to use a suffix
 * for each device type. Again, using the requested view name of "home", the
 * following table shows the adjusted view names.</p>
 *
 * <table border=1 cellpadding=3 cellspacing=0>
 *   <tr>
 *     <td>Resolved Device</td>
 *     <td>Method</td>
 *     <td>Suffix</td>
 *     <td>Adjusted View</td>
 *   </tr>
 *   <tr>
 *     <td>Normal</td>
 *     <td>{@link #setNormalSuffix(String)}</td>
 *     <td>".normal"</td>
 *     <td>"home.normal"</td>
 *   </tr>
 *   <tr>
 *     <td>Mobile</td>
 *     <td>{@link #setMobileSuffix(String)}</td>
 *     <td>".mobile"</td>
 *     <td>"home.mobile"</td>
 *   </tr>
 *   <tr>
 *     <td>Tablet</td>
 *     <td>{@link #setTabletSuffix(String)}</td>
 *     <td>".tablet"</td>
 *     <td>"home.tablet"</td>
 *   </tr>
 * </table>
 *
 * <p>It is also possible to use a combination of prefix and suffix. The view
 * resolver will apply both to the adjusted view.</p>
 *
 * @author Scott Rossillo
 * @author Roy Clarkson
 * @since 1.1
 * @see ViewResolver
 * @see ContentNegotiatingViewResolver
 */
public class LiteDeviceDelegatingViewResolver extends AbstractDeviceDelegatingViewResolver {

	private String normalPrefix = "";

	private String mobilePrefix = "";

	private String tabletPrefix = "";

	private String normalSuffix = "";

	private String mobileSuffix = "";

	private String tabletSuffix = "";

	/**
	 * Creates a new LiteDeviceDelegatingViewResolver
	 * @param delegate the ViewResolver in which to delegate
	 */
	public LiteDeviceDelegatingViewResolver(ViewResolver delegate) {
		super(delegate);
	}

	/**
	 * Set the prefix that gets prepended to view names for normal devices.
	 */
	public void setNormalPrefix(String normalPrefix) {
		this.normalPrefix = (normalPrefix != null ? normalPrefix : "");
	}

	/**
	 * Return the prefix that gets prepended to view names for normal devices
	 */
	protected String getNormalPrefix() {
		return this.normalPrefix;
	}

	/**
	 * Set the prefix that gets prepended to view names for mobile devices.
	 */
	public void setMobilePrefix(String mobilePrefix) {
		this.mobilePrefix = (mobilePrefix != null ? mobilePrefix : "");
	}

	/**
	 * Return the prefix that gets prepended to view names for mobile devices
	 */
	protected String getMobilePrefix() {
		return this.mobilePrefix;
	}

	/**
	 * Set the prefix that gets prepended to view names for tablet devices.
	 */
	public void setTabletPrefix(String tabletPrefix) {
		this.tabletPrefix = (tabletPrefix != null ? tabletPrefix : "");
	}

	/**
	 * Return the prefix that gets prepended to view names for tablet devices
	 */
	protected String getTabletPrefix() {
		return this.tabletPrefix;
	}

	/**
	 * Set the suffix that gets appended to view names for normal devices.
	 */
	public void setNormalSuffix(String normalSuffix) {
		this.normalSuffix = (normalSuffix != null ? normalSuffix : "");
	}

	/**
	 * Return the suffix that gets appended to view names for normal devices
	 */
	protected String getNormalSuffix() {
		return this.normalSuffix;
	}

	/**
	 * Set the suffix that gets appended to view names for mobile devices
	 */
	public void setMobileSuffix(String mobileSuffix) {
		this.mobileSuffix = (mobileSuffix != null ? mobileSuffix : "");
	}

	/**
	 * Return the suffix that gets appended to view names for mobile devices
	 */
	protected String getMobileSuffix() {
		return this.mobileSuffix;
	}

	/**
	 * Set the suffix that gets appended to view names for tablet devices
	 */
	public void setTabletSuffix(String tabletSuffix) {
		this.tabletSuffix = (tabletSuffix != null ? tabletSuffix : "");
	}

	/**
	 * Return the suffix that gets appended to view names for tablet devices
	 */
	protected String getTabletSuffix() {
		return this.tabletSuffix;
	}

	@Override
	protected String getDeviceViewNameInternal(String viewName) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		Assert.isInstanceOf(ServletRequestAttributes.class, attrs);
		HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
		Device device = DeviceUtils.getCurrentDevice(request);
		SitePreference sitePreference = SitePreferenceUtils.getCurrentSitePreference(request);
		String resolvedViewName = viewName;
		if (ResolverUtils.isNormal(device, sitePreference)) {
			resolvedViewName = getNormalPrefix() + viewName + getNormalSuffix();
		} else if (ResolverUtils.isMobile(device, sitePreference)) {
			resolvedViewName = getMobilePrefix() + viewName + getMobileSuffix();
		} else if (ResolverUtils.isTablet(device, sitePreference)) {
			resolvedViewName = getTabletPrefix() + viewName + getTabletSuffix();
		}

		// MOBILE-63 "redirect:/" and "forward:/" can result in the view name containing multiple trailing slashes 
		return stripTrailingSlash(resolvedViewName);
	}

	private String stripTrailingSlash(String viewName) {
		if (viewName.endsWith("//")) {
			return viewName.substring(0, viewName.length() - 1);
		}
		return viewName;
	}

}
