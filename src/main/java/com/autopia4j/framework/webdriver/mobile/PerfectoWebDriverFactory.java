package com.autopia4j.framework.webdriver.mobile;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.*;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.Browser;
import com.autopia4j.framework.webdriver.core.DeviceType;


/**
 * Factory class for creating the {@link WebDriver} object as required
 * @author vj
 */
public class PerfectoWebDriverFactory {
	private static Boolean acceptAllSslCertificates = false;
	private static String userName;
	private static String password;
	
	public static void setUserName(String userName) {
		PerfectoWebDriverFactory.userName = userName;
	}
	
	public static void setPassword(String password) {
		PerfectoWebDriverFactory.password = password;
	}
	
	public static void setAcceptAllSslCertificates(Boolean acceptAllSslCertificates) {
		PerfectoWebDriverFactory.acceptAllSslCertificates = acceptAllSslCertificates;
	}
	
	
	private PerfectoWebDriverFactory() {
		// To prevent external instantiation of this class
	}
	
	
	/**
	 * Function to return the Perfecto MobileCloud {@link RemoteWebDriver} object based on the parameters passed
	 * @param deviceId The ID of the Perfecto MobileCloud device to be used for the test execution
	 * @param deviceType The {@link DeviceType} corresponding to the Device ID passed as input
	 * @param browser The {@link Browser} to be used for the test execution
	 * @param remoteUrl The Perfecto MobileCloud URL to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getPerfectoRemoteWebDriver(String deviceId,
								DeviceType deviceType, Browser browser, URL remoteUrl) {
		DesiredCapabilities desiredCapabilities = getPerfectoExecutionCapabilities(browser);
		desiredCapabilities.setCapability("deviceName", deviceId);
		
		RemoteWebDriver driver = new RemoteWebDriver(remoteUrl, desiredCapabilities);
		
		Map<String, Object> params = new HashMap<>();
		params.put("method", "device");
		//params.put("method", "view");
		String orientation = getOrientation(deviceType);
		params.put("state", orientation);
		driver.executeScript("mobile:handset:rotate", params);
		
		return driver;
	}
	
	private static DesiredCapabilities getPerfectoExecutionCapabilities(Browser browser) {
		validatePerfectoSupports(browser);
		
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		desiredCapabilities.setBrowserName(browser.getValue());
		desiredCapabilities.setPlatform(Platform.ANY);
		desiredCapabilities.setJavascriptEnabled(true);	// Pre-requisite for remote execution
		
		desiredCapabilities.setCapability("user", userName);
		desiredCapabilities.setCapability("password", password);
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		
		return desiredCapabilities;
	}
	
	private static void validatePerfectoSupports(Browser browser) {
		switch (browser) {
		case INTERNET_EXPLORER:
		case FIREFOX:
		case HTML_UNIT:
		case OPERA:
			throw new AutopiaException("The browser " + browser.toString() +
											" is not supported on the Perfecto MobileCloud");
			
		default:
			break;
		}
	}
	
	private static String getOrientation(DeviceType deviceType) {
		switch (deviceType) {
		case MOBILE_PORTRAIT:
		case TABLET_PORTRAIT:
			return "portrait";
			
		case MOBILE_LANDSCAPE:
		case TABLET_LANDSCAPE:
		default:
			return "landscape";
		}
	}
	
	/**
	 * Function to return the Perfecto MobileCloud {@link RemoteWebDriver} object based on the parameters passed
	 * @param platformName The device platform to be used for the test execution (iOS, Android, etc.)
	 * @param platformVersion The device platform version to be used for the test execution
	 * @param browser The {@link Browser} to be used for the test execution
	 * @param remoteUrl The Perfecto MobileCloud URL to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getPerfectoRemoteWebDriverByDevicePlatform(String platformName,
								String platformVersion, Browser browser, String remoteUrl) {
		DesiredCapabilities desiredCapabilities = getPerfectoExecutionCapabilities(browser);
		desiredCapabilities.setCapability("platformName", platformName);
		desiredCapabilities.setCapability("platformVersion", platformVersion);
		
		URL url = Util.getUrl(remoteUrl);
		
		return new RemoteWebDriver(url, desiredCapabilities);
	}
	
	/**
	 * Function to return the Perfecto MobileCloud {@link RemoteWebDriver} object based on the parameters passed
	 * @param manufacturer The manufacturer of the device to be used for the test execution (Samsung, Apple, etc.)
	 * @param model The device model to be used for the test execution (Galaxy S6, iPad Air, etc.)
	 * @param browser The {@link Browser} to be used for the test execution
	 * @param remoteUrl The Perfecto MobileCloud URL to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getPerfectoRemoteWebDriverByDeviceModel(String manufacturer,
										String model, Browser browser, String remoteUrl) {
		DesiredCapabilities desiredCapabilities = getPerfectoExecutionCapabilities(browser);
		desiredCapabilities.setCapability("manufacturer", manufacturer);
		desiredCapabilities.setCapability("model", model);
		
		URL url = Util.getUrl(remoteUrl);
		
		return new RemoteWebDriver(url, desiredCapabilities);
	}
}