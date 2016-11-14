package com.autopia4j.framework.webdriver.core;

/**
 * Enumeration to represent the browser to be used for execution
 * @author vj
 */
public enum Browser {
	APPIUM_CHROME("Chrome"),
	APPIUM_DEFAULT("Browser"),
	APPIUM_SAFARI("Safari"),
	CHROME("chrome"),
	EDGE("edge"),
	FIREFOX("firefox"),
	FIREFOX_MARIONETTE("marionette"),
	GHOST_DRIVER("phantomjs"),
	HTML_UNIT("htmlunit"),
	INTERNET_EXPLORER("internet explorer"),
	OPERA("opera"),
	SAFARI("safari"),
	/**
	 * The native browser on the device (for e.g., native Android browser)
	 */
	PERFECTO_MOBILE("perfectoMobile"),
	/**
	 * The default browser configured on the device (usually Chrome for Android, Safari for iOS)
	 */
	PERFECTO_MOBILE_OS("mobileOS"),
	/**
	 * The default browser configured on the device (usually Chrome for Android, Safari for iOS)
	 */
	PERFECTO_MOBILE_DEFAULT("mobileDefault"),
	PERFECTO_MOBILE_CHROME("mobileChrome"),
	PERFECTO_MOBILE_SAFARI("mobileSafari");
	
	private String value;
	
	Browser(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}