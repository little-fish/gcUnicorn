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

import jakarta.servlet.ServletContext;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Locale;

/**
 * Abstract {@link ViewResolver} implementation, providing a device
 * aware {@link ViewResolver} wrapper that delegates to another view resolver
 * implementation, allowing for resolution of device specific view names without
 * the need for a dedicated mapping to be defined for each view.</p>
 * @author Roy Clarkson
 * @since 1.1
 * @see LiteDeviceDelegatingViewResolver
 */
public abstract class AbstractDeviceDelegatingViewResolver extends
		WebApplicationObjectSupport implements ViewResolver, Ordered {

	/**
	 * Prefix for special view names that specify a redirect URL (usually
	 * to a controller after a form has been submitted and processed).
	 */
	public static final String REDIRECT_URL_PREFIX = "redirect:";

	/**
	 * Prefix for special view names that specify a forward URL (usually
	 * to a controller after a form has been submitted and processed).
	 */
	public static final String FORWARD_URL_PREFIX = "forward:";

	private final ViewResolver delegate;

	private int order = Ordered.LOWEST_PRECEDENCE;

	private boolean enableFallback = false;

	/**
	 * Creates a new AbstractDeviceDelegatingViewResolver
	 * @param delegate the ViewResolver in which to delegate
	 */
	protected AbstractDeviceDelegatingViewResolver(ViewResolver delegate) {
		Assert.notNull(delegate, "delegate is required");
		this.delegate = delegate;
	}

	/**
	 * Returns the delegate view resolver
	 */
	public ViewResolver getViewResolver() {
		return this.delegate;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Enables support for fallback resolution, meaning if the adjusted view
	 * name cannot be resolved, and attempt will be made to resolve the
	 * original view name. This may be helpful in situations where not all
	 * views within a web site have device specific implementations.
	 * 
	 * <p>Note: fallback resolution will only work when delegating to a view 
	 * resolver which returns null from 
	 * {@link #resolveViewName(String, Locale)} if it cannot resolve a view. 
	 * For example, {@link InternalResourceViewResolver} never returns null,
	 * so fallback resolution will not be available.
	 */
	public void setEnableFallback(boolean enableFallback) {
		this.enableFallback = enableFallback;
	}

	/**
	 * Return whether fallback view resolution is enabled
	 * @see #setEnableFallback(boolean)
	 */
	protected boolean getEnableFallback() {
		return this.enableFallback;
	}

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		String deviceViewName = getDeviceViewName(viewName);
		View view = delegate.resolveViewName(deviceViewName, locale);
		if (enableFallback && view == null) {
			view = delegate.resolveViewName(viewName, locale);
		}
		if (logger.isDebugEnabled() && view != null) {
			logger.debug("Resolved View: " + view.toString());
		}
		return view;
	}

	/**
	 * Returns the adjusted view name as determined by subclass implementation.
	 * In the case where a requested URL is prefixed with "redirect:" or
	 * "forward:", the view name will not be adjusted.
	 * @param viewName the name of the view before device resolution
	 * @return the adjusted view name
	 * @see #getDeviceViewNameInternal(String)
	 */
	protected String getDeviceViewName(String viewName) {
		// Check for special "redirect:" prefix.
		if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
			return viewName;
		}
		// Check for special "forward:" prefix.
		if (viewName.startsWith(FORWARD_URL_PREFIX)) {
			return viewName;
		}
		return getDeviceViewNameInternal(viewName);
	}

	/**
	 * Subclasses must implement this method, adjusting the device view name
	 * based on device resolution used within the subclass.
	 * @param viewName the name of the view before device resolution
	 * @return the adjusted view name
	 * @see #getDeviceViewName(String)
	 */
	protected abstract String getDeviceViewNameInternal(String viewName);

	@Override
	protected void initServletContext(ServletContext servletContext) {
		String name = delegate.getClass().getName();
		getApplicationContext().getAutowireCapableBeanFactory().initializeBean(delegate, name);
	}

}
