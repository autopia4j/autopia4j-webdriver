package com.autopia4j.framework.webdriver.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.*;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.Browser;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;

import io.github.bonigarcia.wdm.Architecture;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.MarionetteDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;


/**
 * Factory class for creating the {@link WebDriver} object as required
 * @author vj
 */
public class WebDriverFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverFactory.class);
	private static Properties properties;
	
	private WebDriverFactory() {
		// To prevent external instantiation of this class
	}
	
	
	/**
	 * Function to return the appropriate {@link WebDriver} object based on the parameters passed
	 * @param browser The {@link Browser} to be used for the test execution
	 * @return The corresponding {@link WebDriver} object
	 */
	public static WebDriver getWebDriver(Browser browser) {
		WebDriver driver;
		
		switch(browser) {
		case CHROME:
			driver = getChromeDriver();
			break;
			
		case EDGE:
			driver = getEdgeDriver();
			break;
			
		case FIREFOX:
			driver = getFirefoxDriver();
			break;
			
		case FIREFOX_MARIONETTE:
			driver = getMarionetteDriver();
			break;
			
		case GHOST_DRIVER:
			driver = getPhantomJsDriver();
			break;
			
		case HTML_UNIT:
			// Does not take the system proxy settings automatically!
			
			properties = Settings.getInstance();
			boolean proxyRequired =
					Boolean.parseBoolean(properties.getProperty("ProxyRequired"));
			
			if (proxyRequired) {
				driver = getHtmlUnitDriverWithProxy();
			} else {
				driver = new HtmlUnitDriver(true);
			}
			break;
			
		case INTERNET_EXPLORER:
			driver = getInternetExplorerDriver();
			break;
			
		case OPERA:
			driver = getOperaDriver();
			break;
			
		case SAFARI:
			driver = getSafariDriver();
			break;
			
		default:
			throw new AutopiaException("Unhandled browser!");
		}
		
		return driver;
	}
	
	private static WebDriver getChromeDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		
		ChromeDriverManager.getInstance().setup();
		return new ChromeDriver(desiredCapabilities);
	}
	
	private static WebDriver getEdgeDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.edge();
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		
		EdgeDriverManager.getInstance().setup();
		return new EdgeDriver(desiredCapabilities);
	}
	
	private static WebDriver getFirefoxDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		// Sample code to specify path of Firefox binaries
		//System.setProperty("webdriver.firefox.bin",
		//		"C:\\Users\\vramas\\AppData\\Local\\Mozilla Firefox\\firefox.exe");
		
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setAcceptUntrustedCertificates(acceptAllSslCertificates);
		return new FirefoxDriver(firefoxProfile);
	}
	
	private static WebDriver getMarionetteDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		FirefoxProfile marionetteProfile = new FirefoxProfile();
		marionetteProfile.setAcceptUntrustedCertificates(acceptAllSslCertificates);
		
		MarionetteDriverManager.getInstance().setup();
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
		desiredCapabilities.setCapability(FirefoxDriver.MARIONETTE, true);
		desiredCapabilities.setCapability(FirefoxDriver.PROFILE, marionetteProfile);
		
		return new FirefoxDriver(desiredCapabilities);
	}
	
	private static WebDriver getPhantomJsDriver() {
		// Takes the system proxy settings automatically (I think!)
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		
		PhantomJsDriverManager.getInstance().setup();
		return new PhantomJSDriver(desiredCapabilities);
	}
	
	private static WebDriver getHtmlUnitDriverWithProxy() {
		WebDriver driver;
		boolean proxyAuthenticationRequired =
				Boolean.parseBoolean(properties.getProperty("ProxyAuthenticationRequired"));
		
		if(proxyAuthenticationRequired) {
			// NTLM authentication for proxy supported
			
			driver = new HtmlUnitDriver(true) {
			@Override
			protected WebClient modifyWebClient(WebClient client) {
				DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
				credentialsProvider.addNTLMCredentials(properties.getProperty("Username"),
														properties.getProperty("Password"),
														properties.getProperty("ProxyHost"),
														Integer.parseInt(properties.getProperty("ProxyPort")),
														"", properties.getProperty("Domain"));
				client.setCredentialsProvider(credentialsProvider);
				return client;
				}
			};
		} else {
			driver = new HtmlUnitDriver(true);
		}
		
		((HtmlUnitDriver) driver).setProxy(properties.getProperty("ProxyHost"),
									Integer.parseInt(properties.getProperty("ProxyPort")));
		return driver;
	}
	
	private static WebDriver getInternetExplorerDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		boolean introduceFlakiness =
				Boolean.parseBoolean(properties.getProperty("IntroduceFlakinessInternetExplorer"));
		boolean turnOffPopupBlocker =
				Boolean.parseBoolean(properties.getProperty("TurnOffPopupBlockerInternetExplorer"));
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.internetExplorer();
		//desiredCapabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
		//desiredCapabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);
		desiredCapabilities.setCapability("nativeEvents", false);
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		desiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, introduceFlakiness);
		//desiredCapabilities.setCapability("ignoreProtectedModeSettings", introduceFlakiness);
		
		if(turnOffPopupBlocker) {
			String cmd = "REG ADD \"HKEY_CURRENT_USER\\Software\\Microsoft\\Internet Explorer\\New Windows\" /F /V \"PopupMgr\" /T REG_SZ /D \"no\"";
			try {
			    Runtime.getRuntime().exec(cmd);
			} catch (Exception e) {
			    String errorDescription = "An error occurred while turning off "
			    			+ "the popup blocker in Internet Explorer: " + e.getMessage();
			    LOGGER.error(errorDescription, e);
				throw new AutopiaException(errorDescription);
			}
		}
		
		InternetExplorerDriverManager.getInstance().setup(Architecture.x32);	// The 64 bit driver works excruciatingly slow on 64 bit machines!
		return new InternetExplorerDriver(desiredCapabilities);
	}
	
	private static WebDriver getOperaDriver() {
		// Does not take the system proxy settings automatically!
		// NTLM authentication for proxy NOT supported
		
		properties = Settings.getInstance();
		boolean proxyRequired =
				Boolean.parseBoolean(properties.getProperty("ProxyRequired"));
		
		OperaDriverManager.getInstance().setup();
		WebDriver driver;
		if (proxyRequired) {
			DesiredCapabilities desiredCapabilities = getProxyCapabilities();
			driver = new OperaDriver(desiredCapabilities);
		} else {
			driver = new OperaDriver();
		}
		return driver;
	}
	
	private static WebDriver getSafariDriver() {
		// Takes the system proxy settings automatically
		
		properties = Settings.getInstance();
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.safari();
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		
		return new SafariDriver(desiredCapabilities);
	}
	
	private static DesiredCapabilities getProxyCapabilities() {
		properties = Settings.getInstance();
		String proxyUrl = properties.getProperty("ProxyHost") + ":" +
									properties.getProperty("ProxyPort");
		
		Proxy proxy = new Proxy();
		proxy.setProxyType(ProxyType.MANUAL);
		proxy.setHttpProxy(proxyUrl);
		proxy.setFtpProxy(proxyUrl);
		proxy.setSslProxy(proxyUrl);
		
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
		
		return desiredCapabilities;
	}
	
	/**
	 * Function to return the {@link RemoteWebDriver} object based on the parameters passed
	 * @param browser The {@link Browser} to be used for the test execution
	 * @param browserVersion The browser version to be used for the test execution
	 * @param platform The {@link Platform} to be used for the test execution
	 * @param remoteUrl The URL of the remote machine to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getRemoteWebDriver(Browser browser, String browserVersion,
												Platform platform, URL remoteUrl) {
		// For running RemoteWebDriver tests in Chrome and IE:
		// The ChromeDriver and IEDriver executables needs to be in the PATH of the remote machine
		// To set the executable path manually, use:
		// java -Dwebdriver.chrome.driver=/path/to/driver -jar selenium-server-standalone.jar
		// java -Dwebdriver.ie.driver=/path/to/driver -jar selenium-server-standalone.jar
		
		properties = Settings.getInstance();
		boolean proxyRequired =
				Boolean.parseBoolean(properties.getProperty("ProxyRequired"));
		boolean acceptAllSslCertificates =
				Boolean.parseBoolean(properties.getProperty("AcceptAllSslCertificates"));
		
		DesiredCapabilities desiredCapabilities;
		if ((browser.equals(Browser.HTML_UNIT) || browser.equals(Browser.OPERA))
																&& proxyRequired) {
			desiredCapabilities = getProxyCapabilities();
		} else {
			desiredCapabilities = new DesiredCapabilities();
		}
		
		desiredCapabilities.setBrowserName(browser.getValue());
		desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, acceptAllSslCertificates);
		if (browser.equals(Browser.INTERNET_EXPLORER)) {
			boolean introduceFlakiness =
					Boolean.parseBoolean(properties.getProperty("IntroduceFlakinessInternetExplorer"));
			desiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, introduceFlakiness);
		}
		
		if (browserVersion != null) {
			desiredCapabilities.setVersion(browserVersion);
		}
		if (platform != null) {
			desiredCapabilities.setPlatform(platform);
		}
		
		desiredCapabilities.setJavascriptEnabled(true);	// Pre-requisite for remote execution
		
		return new RemoteWebDriver(remoteUrl, desiredCapabilities);
	}
	
	/**
	 * Function to return the {@link RemoteWebDriver} object based on the parameters passed
	 * @param browser The {@link Browser} to be used for the test execution
	 * @param remoteUrl The URL of the remote machine to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getRemoteWebDriver(Browser browser, URL remoteUrl) {
		return getRemoteWebDriver(browser, null, null, remoteUrl);
	}
	
	/**
	 * Function to return the {@link ChromeDriver} object emulating the device specified by the user
	 * @param deviceName The name of the device to be emulated (check Chrome Dev Tools for a list of available devices)
	 * @return The corresponding {@link ChromeDriver} object
	 */
	public static WebDriver getEmulatedWebDriver(String deviceName) {
		DesiredCapabilities desiredCapabilities = getEmulatedChromeDriverCapabilities(deviceName);
		
		properties = Settings.getInstance();
		ChromeDriverManager.getInstance().setup();
		return new ChromeDriver(desiredCapabilities);
	}
	
	private static DesiredCapabilities getEmulatedChromeDriverCapabilities(String deviceName) {
		Map<String, String> mobileEmulation = new HashMap<>();
		mobileEmulation.put("deviceName", deviceName);
		//mobileEmulation.put("deviceOrientation", "portrait");
		
		Map<String, Object> chromeOptions = new HashMap<>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
		desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		
		return desiredCapabilities;
	}
	
	/**
	 * Function to return the {@link RemoteWebDriver} object emulating the device specified by the user
	 * @param deviceName The name of the device to be emulated (check Chrome Dev Tools for a list of available devices)
	 * @param remoteUrl The URL of the remote machine to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getEmulatedRemoteWebDriver(String deviceName, URL remoteUrl) {
		DesiredCapabilities desiredCapabilities = getEmulatedChromeDriverCapabilities(deviceName);
		desiredCapabilities.setJavascriptEnabled(true);	// Pre-requisite for remote execution
		
		return new RemoteWebDriver(remoteUrl, desiredCapabilities);
	}
	
	/**
	 * Function to return the {@link ChromeDriver} object emulating the device attributes specified by the user
	 * @param deviceWidth The width of the device to be emulated (in pixels)
	 * @param deviceHeight The height of the device to be emulated (in pixels)
	 * @param devicePixelRatio The device's pixel ratio
	 * @param userAgent The user agent string
	 * @return The corresponding {@link ChromeDriver} object
	 */
	public static WebDriver getEmulatedWebDriver(int deviceWidth, int deviceHeight,
											float devicePixelRatio, String userAgent) {
		DesiredCapabilities desiredCapabilities =
						getEmulatedChromeDriverCapabilities(deviceWidth, deviceHeight,
															devicePixelRatio, userAgent);
		
		properties = Settings.getInstance();
		ChromeDriverManager.getInstance().setup();
		return new ChromeDriver(desiredCapabilities);
	}
	
	private static DesiredCapabilities getEmulatedChromeDriverCapabilities(
			int deviceWidth, int deviceHeight, float devicePixelRatio, String userAgent) {
		Map<String, Object> deviceMetrics = new HashMap<>();
		deviceMetrics.put("width", deviceWidth);
		deviceMetrics.put("height", deviceHeight);
		deviceMetrics.put("pixelRatio", devicePixelRatio);
		
		Map<String, Object> mobileEmulation = new HashMap<>();
		mobileEmulation.put("deviceMetrics", deviceMetrics);
		//mobileEmulation.put("userAgent", "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
		mobileEmulation.put("userAgent", userAgent);
		
		Map<String, Object> chromeOptions = new HashMap<>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
		desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		return desiredCapabilities;
	}
	
	/**
	 * Function to return the {@link RemoteWebDriver} object emulating the device attributes specified by the user
	 * @param deviceWidth The width of the device to be emulated (in pixels)
	 * @param deviceHeight The height of the device to be emulated (in pixels)
	 * @param devicePixelRatio The device's pixel ratio
	 * @param userAgent The user agent string
	 * @param remoteUrl The URL of the remote machine to be used for the test execution
	 * @return The corresponding {@link RemoteWebDriver} object
	 */
	public static WebDriver getEmulatedRemoteWebDriver(int deviceWidth, int deviceHeight,
								float devicePixelRatio, String userAgent, String remoteUrl) {
		DesiredCapabilities desiredCapabilities =
				getEmulatedChromeDriverCapabilities(deviceWidth, deviceHeight,
													devicePixelRatio, userAgent);
		desiredCapabilities.setJavascriptEnabled(true);	// Pre-requisite for remote execution
		
		URL url = Util.getUrl(remoteUrl);
		
		return new RemoteWebDriver(url, desiredCapabilities);
	}
}