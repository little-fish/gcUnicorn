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
 * Enumeration for the type of device that has been resolved
 * @author Roy Clarkson
 */
public enum DeviceType {
	
	/**
	 * Represents a normal device. i.e. a browser on a desktop or laptop computer
	 */
	NORMAL, 
	
	/**
	 * Represents a mobile device, such as an iPhone
	 */
	MOBILE, 
	
	/**
	 * Represents a tablet device, such as an iPad
	 */
	TABLET
}
