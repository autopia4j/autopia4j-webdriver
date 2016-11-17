package com.autopia4j.framework.webdriver.core;

import java.net.URL;

import org.openqa.selenium.Platform;
import com.autopia4j.framework.webdriver.core.Browser;
import com.autopia4j.framework.webdriver.core.DeviceType;
import com.autopia4j.framework.webdriver.core.ExecutionMode;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

/**
 * Builder class for the {@link WebDriverTestParameters} object
 * @author vj
 */
public class WebDriverTestParamsBuilder {
	private final WebDriverTestParameters testParameters;
	
	/**
	 * Constructor to initialize the {@link WebDriverTestParamsBuilder} object
	 * @param module The current module
	 * @param test The current test case
	 */
	public WebDriverTestParamsBuilder(String module, String test) {
		this.testParameters = new WebDriverTestParameters(module, test);
	}
	
	/**
	 * Function to set the test instance
	 * @param testInstance The test instance
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder testInstance(String testInstance) {
		this.testParameters.setCurrentTestInstance(testInstance);
		return this;
	}
	
	/**
	 * Function to set the {@link ExecutionMode}
	 * @param executionMode The {@link ExecutionMode}
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder executionMode(ExecutionMode executionMode) {
		this.testParameters.setExecutionMode(executionMode);
		return this;
	}
	
	/**
	 * Function to set the {@link Browser}
	 * @param browser The {@link Browser}
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder browser(Browser browser) {
		this.testParameters.setBrowser(browser);
		return this;
	}
	
	/**
	 * Function to set the browser version
	 * @param browserVersion The browser version
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder browserVersion(String browserVersion) {
		this.testParameters.setBrowserVersion(browserVersion);
		return this;
	}
	
	/**
	 * Function to set the {@link Platform}
	 * @param platform The {@link Platform}
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder platform(Platform platform) {
		this.testParameters.setPlatform(platform);
		return this;
	}
	
	/**
	 * Function to set the {@link DeviceType}
	 * @param deviceType The {@link DeviceType}
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder deviceType(DeviceType deviceType) {
		this.testParameters.setDeviceType(deviceType);
		return this;
	}
	
	/**
	 * Function to set the device name
	 * @param deviceName The device name
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder deviceName(String deviceName) {
		this.testParameters.setDeviceName(deviceName);
		return this;
	}
	
	/**
	 * Function to set the remote {@link URL}
	 * @param remoteUrl The remote {@link URL}
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder remoteUrl(URL remoteUrl) {
		this.testParameters.setRemoteUrl(remoteUrl);
		return this;
	}
	
	/**
	 * Function to set the remote URL
	 * @param remoteUrl The remote URL
	 * @return The current {@link WebDriverTestParamsBuilder} object
	 */
	public WebDriverTestParamsBuilder remoteUrl(String remoteUrl) {
		this.testParameters.setRemoteUrl(remoteUrl);
		return this;
	}
	
	/**
	 * Function to build the {@link WebDriverTestParameters} object
	 * @return The {@link WebDriverTestParameters} object
	 */
	public WebDriverTestParameters build() {
		return this.testParameters;
	}
}