package com.autopia4j.framework.webdriver.mobile;

import java.net.URL;
import org.openqa.selenium.Platform;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.*;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.webdriver.core.Browser;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;


/**
 * Factory class for creating the {@link WebDriver} object as required
 * @author vj
 */
public class AppiumWebDriverFactory {
	private AppiumWebDriverFactory() {
		// To prevent external instantiation of this class
	}
	
	
	/**
	 * Function to return the Appium WebDriver object
	 * @param deviceName The name of the device
	 * @param screenOrientation The {@link ScreenOrientation} of the device
	 * @param browser The {@link Browser} to be launched on the device
	 * @param platform The device {@link Platform}
	 * @param remoteUrl The Appium Server URL
	 * @return The Appium {@link RemoteWebDriver} object
	 */
	public static WebDriver getAppiumWebDriver(String deviceName, ScreenOrientation screenOrientation,
											Browser browser, Platform platform, URL remoteUrl) {
		AppiumDriver<WebElement> driver;
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		
		//desiredCapabilities.setCapability("deviceName", deviceName);
		desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
		desiredCapabilities.setCapability(MobileCapabilityType.BROWSER_NAME, browser.getValue());
		String platformName = getAppiumPlatformName(platform);
		desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, platformName);
		
		// The capabilities below only work with emulators/simulators
		//desiredCapabilities.setCapability(MobileCapabilityType.ORIENTATION, screenOrientation);
		//desiredCapabilities.setCapability("orientation", screenOrientation);
		//desiredCapabilities.setCapability("deviceorientation", screenOrientation);
		
		switch(platform) {
		case ANDROID:
			driver = new AndroidDriver<>(remoteUrl, desiredCapabilities);
			driver.context("NATIVE_APP");
			driver.rotate(screenOrientation);
			driver.context("WEBVIEW_1");
			return driver;
			
		case ANY:
			driver = new IOSDriver<>(remoteUrl, desiredCapabilities);
			driver.rotate(screenOrientation);
			return driver;
			
		default:
			throw new AutopiaException("Unsupported Appium platform!");
		}
	}
	
	private static String getAppiumPlatformName(Platform platform) {
		switch(platform) {
		case ANDROID:
			return "Android";
			
		case ANY:
			return "iOS";
			
		default:
			throw new AutopiaException("Unsupported Appium platform:" + platform.toString());
		}
	}
}