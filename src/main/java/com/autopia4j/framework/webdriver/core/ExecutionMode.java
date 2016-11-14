package com.autopia4j.framework.webdriver.core;

import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Enumeration to represent the mode of execution
 * @author vj
 */
public enum ExecutionMode {
	/**
	 * Execute on the local machine
	 */
	LOCAL,
	
	/**
	 * Execute on a remote machine 
	 */
	REMOTE,
	
	/**
	 * Execute on an emulated device on the local machine
	 */
	LOCAL_EMULATED_DEVICE,
	
	/**
	 * Execute on an emulated device on a remote machine
	 */
	REMOTE_EMULATED_DEVICE,
	
	/**
	 * Execute on a selenium grid
	 */
	GRID,
	
	/**
	 * Execute on a Perfecto MobileCloud device using {@link RemoteWebDriver}
	 */
	PERFECTO_DEVICE,
	
	/**
	 * Execute on a mobile device using Appium
	 */
	APPIUM_DEVICE;
}