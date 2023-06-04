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
 * A lightweight Device implementation suitable for use as support code.
 * Typically used to hold the output of a device resolution invocation.
 * @author Keith Donald
 * @author Roy Clarkson
 * @author Scott Rossillo
 * @author Onur Kagan Ozcan
 */
public class LiteDevice implements Device {

	public static final LiteDevice NORMAL_INSTANCE = new LiteDevice(DeviceType.NORMAL);

	public static final LiteDevice MOBILE_INSTANCE = new LiteDevice(DeviceType.MOBILE);

	public static final LiteDevice TABLET_INSTANCE = new LiteDevice(DeviceType.TABLET);

	private final DeviceType deviceType;

	private final DevicePlatform devicePlatform;

	public DevicePlatform getDevicePlatform() {
		return this.devicePlatform;
	}

	public DeviceType getDeviceType() {
		return this.deviceType;
	}

	/**
	 * Creates a LiteDevice with DeviceType of NORMAL and DevicePlatform UNKNOWN
	 */
	public LiteDevice() {
		this(DeviceType.NORMAL, DevicePlatform.UNKNOWN);
	}

	/**
	 * Creates a LiteDevice with DevicePlatform UNKNOWN
	 * @param deviceType the type of device i.e. NORMAL, MOBILE, TABLET
	 */
	public LiteDevice(DeviceType deviceType) {
		this(deviceType, DevicePlatform.UNKNOWN);
	}

	/**
	 * Creates a LiteDevice
	 * @param deviceType the type of device i.e. NORMAL, MOBILE, TABLET
	 * @param devicePlatform the platform of device, i.e. IOS or ANDROID
	 */
	public LiteDevice(DeviceType deviceType, DevicePlatform devicePlatform) {
		this.deviceType = deviceType;
		this.devicePlatform = devicePlatform;
	}

	public boolean isNormal() {
		return this.deviceType == DeviceType.NORMAL;
	}

	public boolean isMobile() {
		return this.deviceType == DeviceType.MOBILE;
	}

	public boolean isTablet() {
		return this.deviceType == DeviceType.TABLET;
	}

	public static Device from(DeviceType deviceType, DevicePlatform devicePlatform) {
		return new LiteDevice(deviceType, devicePlatform);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[LiteDevice ");
		builder.append("type").append("=").append(this.deviceType);
		builder.append("]");
		return builder.toString();
	}

}
