package com.autopia4j.framework.webdriver.core;

import java.net.URL;
import java.util.Properties;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.core.TestParameters;
import com.autopia4j.framework.utils.Util;

import org.openqa.selenium.Platform;
import org.openqa.selenium.ScreenOrientation;


/**
 * Class to encapsulate various input parameters required for each test script
 * @author vj
 */
public class WebDriverTestParameters extends TestParameters {
	private ExecutionMode executionMode;
	private Browser browser;
	private String browserVersion;
	private Platform platform;
	private DeviceType deviceType;
	private ScreenOrientation screenOrientation;
	private String deviceName;
	private URL remoteUrl;
	
	private String perfectoDeviceId;
	
	/**
	 * Constructor to initialize the {@link WebDriverTestParameters} object
	 * @param currentModule The current module
	 * @param currentTestcase The current test case
	 */
	public WebDriverTestParameters(String currentModule, String currentTestcase) {
		super(currentModule, currentTestcase);
	}
	
	/**
	 * Function to get the {@link ExecutionMode} for the test being executed
	 * @return The {@link ExecutionMode} for the test being executed
	 */
	public ExecutionMode getExecutionMode() {
		return executionMode;
	}
	
	/**
	 * Function to set the {@link ExecutionMode} for the test being executed
	 * @param executionMode The {@link ExecutionMode} for the test being executed
	 */
	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
		
		if (ExecutionMode.PERFECTO_DEVICE.equals(executionMode) &&
														this.browser == null) {
			this.browser = Browser.PERFECTO_MOBILE_DEFAULT;
		}
	}
	
	/**
	 * Function to get the {@link Browser} on which the test is to be executed
	 * @return The {@link Browser} on which the test is to be executed
	 */
	public Browser getBrowser() {
		return browser;
	}
	
	/**
	 * Function to set the {@link Browser} on which the test is to be executed
	 * @param browser The {@link Browser} on which the test is to be executed
	 */
	public void setBrowser(Browser browser) {
		this.browser = browser;
	}
	
	/**
	 * Function to get the Browser Version on which the test is to be executed
	 * @return The Browser Version on which the test is to be executed
	 */
	public String getBrowserVersion() {
		return browserVersion;
	}
	
	/**
	 * Function to set the Browser Version on which the test is to be executed
	 * @param version The Browser Version on which the test is to be executed
	 */
	public void setBrowserVersion(String version) {
		this.browserVersion = version;
	}
	
	/**
	 * Function to get the {@link Platform} on which the test is to be executed
	 * @return The {@link Platform} on which the test is to be executed
	 */
	public Platform getPlatform() {
		return platform;
	}
	
	/**
	 * Function to set the {@link Platform} on which the test is to be executed
	 * @param platform The {@link Platform} on which the test is to be executed
	 */
	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
	
	/**
	 * Function to get the browser and platform on which the test is to be executed
	 * @return The browser and platform on which the test is to be executed
	 */
	public String getBrowserAndPlatform() {
		if(this.browser == null) {
			throw new AutopiaException("The browser has not been initialized!");
		}
		
		String browserAndPlatform = this.browser.toString();
		if(this.browserVersion != null) {
			browserAndPlatform += " " + browserVersion;
		}
		if(this.platform != null) {
			browserAndPlatform += " on " + this.platform; 
		}
		
		return browserAndPlatform;
	}
	
	/**
	 * Function to get the {@link DeviceType} on which the test is to be executed
	 * @return The {@link DeviceType} on which the test is to be executed
	 */
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	/**
	 * Function to set the {@link DeviceType} on which the test is to be executed<br>
	 * This internally sets the Screen Orientation test parameter as well
	 * @param deviceType The {@link DeviceType} on which the test is to be executed
	 */
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
		setScreenOrientation();
	}
	
	/**
	 * Function to get the {@link ScreenOrientation} of the device on which the test is to be executed
	 * @return The {@link ScreenOrientation} of the device on which the test is to be executed
	 */
	public ScreenOrientation getScreenOrientation() {
		return screenOrientation;
	}
	
	private void setScreenOrientation() {
		switch (deviceType) {
		case MOBILE_PORTRAIT:
		case TABLET_PORTRAIT:
			this.screenOrientation = ScreenOrientation.PORTRAIT;
			break;
			
		case MOBILE_LANDSCAPE:
		case TABLET_LANDSCAPE:
			this.screenOrientation = ScreenOrientation.LANDSCAPE;
			break;
			
		default:
			this.screenOrientation = ScreenOrientation.PORTRAIT;
		}
	}
	
	/**
	 * Function to get the name of the mobile device on which the test is to be executed
	 * @return The name of the mobile device on which the test is to be executed
	 */
	public String getDeviceName() {
		return deviceName;
	}
	
	/**
	 * Function to set the name of the mobile device on which the test is to be executed<br><br>
	 * If the ExecutionMode is PERFECTO_REMOTEWEBDRIVER, this function also sets the device's Perfecto MobileCloud ID, 
	 * by accessing the Perfecto Device List within the Global Settings.properties file
	 * @param deviceName The name of the mobile device on which the test is to be executed
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
		
		if(ExecutionMode.PERFECTO_DEVICE.equals(this.executionMode)) {
			Properties properties = Settings.getInstance();
			this.perfectoDeviceId = properties.getProperty(deviceName);
		}
	}
	
	/**
	 * Function to get the URL of the Remote WebDriver Server on which this test is to be executed
	 * @return The Remote WebDriver Server URL
	 */
	public URL getRemoteUrl() {
		return remoteUrl;
	}
	
	/**
	 * Function to set the URL of the Remote WebDriver Server on which this test is to be executed
	 * @param remoteUrl The Remote WebDriver Server URL
	 */
	public void setRemoteUrl(URL remoteUrl) {
		this.remoteUrl = remoteUrl;
	}
	
	/**
	 * Function to set the URL of the Remote WebDriver Server on which this test is to be executed
	 * @param remoteUrl The Remote WebDriver Server URL
	 */
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = Util.getUrl(remoteUrl);
	}
	
	/**
	 * Function to get the ID of the Perfecto MobileCloud device on which the test is to be executed
	 * @return The ID of the Perfecto MobileCloud device on which the test is to be executed
	 */
	public String getPerfectoDeviceId() {
		return perfectoDeviceId;
	}
	
	/**
	 * Function to set the ID of the Perfecto MobileCloud device on which the test is to be executed
	 * @param perfectoDeviceId The ID of the Perfecto MobileCloud device on which the test is to be executed
	 */
	public void setPerfectoDeviceId(String perfectoDeviceId) {
		this.perfectoDeviceId = perfectoDeviceId;
	}
	
	@Override
	public String getAdditionalDetails() {
		String additionalDetails = super.getAdditionalDetails();
		
		if("".equals(additionalDetails)) {
			switch(this.executionMode) {
			case PERFECTO_DEVICE:
				additionalDetails = this.getPerfectoDeviceDetails();
				break;
				
			case LOCAL_EMULATED_DEVICE:
			case REMOTE_EMULATED_DEVICE:
				additionalDetails = this.getEmulatedDeviceDetails();
				break;
				
			default:
				additionalDetails = this.getBrowserAndPlatform();
			}
		}
		
		return additionalDetails;
	}
	
	private String getPerfectoDeviceDetails() {
		if (this.perfectoDeviceId == null) {
			throw new AutopiaException("The Perfecto Device ID has not been initialized!");
		}
		
		if(this.deviceName == null) {
			return getBrowserAndPlatform();
		} else {
			return this.deviceName + " on Perfecto MobileCloud";
		}
	}
	
	private String getEmulatedDeviceDetails() {
		return this.deviceName + " emulated on " + this.getBrowserAndPlatform();
	}
}