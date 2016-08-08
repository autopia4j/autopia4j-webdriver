package com.autopia4j.framework.webdriver;

/**
 * Enumeration to represent the type of device on which a test is executed
 * @author vj
 */
public enum DeviceType {
	/**
	 * Execution on a larger desktop
	 */
	LARGE_DESKTOP("large_desktop"),
	/**
	 * Execution on a desktop or laptop machine
	 */
	DESKTOP("desktop"),
	
	/**
	 * Execution on a tablet (portrait orientation)
	 */
	TABLET_PORTRAIT("tablet_portrait"),
	/**
	 * Execution on a tablet (landscape orientation)
	 */
	TABLET_LANDSCAPE("tablet_landscape"),
	
	/**
	 * Execution on a mobile phone (portrait orientation)
	 */
	MOBILE_PORTRAIT("mobile_portrait"),
	/**
	 * Execution on a mobile phone (landscape orientation)
	 */
	MOBILE_LANDSCAPE("mobile_landscape");
	
	private String value;
	
	DeviceType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}