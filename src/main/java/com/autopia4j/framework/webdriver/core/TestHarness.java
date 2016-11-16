package com.autopia4j.framework.webdriver.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.IterationOptions;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.core.TimeStamp;
import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.ReportTheme;
import com.autopia4j.framework.reporting.ReportThemeFactory;
import com.autopia4j.framework.reporting.ReportThemeFactory.Theme;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.mobile.AppiumWebDriverFactory;
import com.autopia4j.framework.webdriver.mobile.PerfectoWebDriverFactory;
import com.autopia4j.framework.webdriver.mobile.PerfectoWebDriverUtil;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverFactory;
import com.autopia4j.framework.webdriver.utils.WebDriverProxy;

public class TestHarness {
	
	private final Logger logger = LoggerFactory.getLogger(TestHarness.class);
	
	private FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
	private Properties properties = Settings.getInstance();
	
	private Date startTime;
	
	
	/**
	 * Constructor to initialize the {@link TestHarness} object
	 */
	public TestHarness() {
		startTime = Util.getCurrentTime();
		
		logger.info("Starting test execution");
	}
	
	/**
	 * Function to initialize the {@link WebDriverTestParameters} object
	 * @param currentModule The name of the current module
	 * @param currentTestcase The name of the current test case
	 * @return The {@link WebDriverTestParameters} object, initialized to default values
	 */
	public WebDriverTestParameters initializeTestParameters(String currentModule, String currentTestcase) {
		WebDriverTestParameters testParameters = new WebDriverTestParameters(currentModule, currentTestcase);
		
		logger.info("Initializing test parameters to default values");
		
		testParameters.setIterationMode(IterationOptions.RUN_ALL_ITERATIONS);
		
		String defaultExecutionMode = properties.getProperty("execution.mode.default");
		testParameters.setExecutionMode(ExecutionMode.valueOf(defaultExecutionMode));
		
		String defaultDeviceName = properties.getProperty("device.name.default");
		testParameters.setDeviceName(defaultDeviceName);
		
		String defaultBrowser = properties.getProperty("browser.default");
		testParameters.setBrowser(Browser.valueOf(defaultBrowser));
		
		String defaultPlatform = properties.getProperty("platform.default");
		testParameters.setPlatform(Platform.valueOf(defaultPlatform));
		
		String defaultDeviceType = properties.getProperty("device.type.default");
		testParameters.setDeviceType(DeviceType.valueOf(defaultDeviceType));
		
		String defaultRemoteUrl = properties.getProperty("remote.url.default");
		testParameters.setRemoteUrl(defaultRemoteUrl);
		
		return testParameters;
	}
	
	/**
	 * Function to set default values for the given {@link WebDriverTestParameters} object
	 * @param testParameters The {@link WebDriverTestParameters} object
	 */
	public void setDefaultTestParameters(WebDriverTestParameters testParameters) {
		if (testParameters.getCurrentTestDescription() == "") {
			logger.info("Test description unspecified. Setting to default value: <Test name>");
			testParameters.setCurrentTestDescription(testParameters.getCurrentTestcase());
		}
		
		if (testParameters.getIterationMode() == null) {
			logger.info("Iteration mode unspecified. Setting to default value: All Iterations");
			testParameters.setIterationMode(IterationOptions.RUN_ALL_ITERATIONS);
		}
		
		if (testParameters.getExecutionMode() == null) {
			String defaultExecutionMode = properties.getProperty("execution.mode.default");
			logger.info("Execution mode unspecified. Setting to default value: {}", defaultExecutionMode);
			testParameters.setExecutionMode(ExecutionMode.valueOf(defaultExecutionMode));
		}
		
		if (testParameters.getDeviceName() == null) {
			String defaultDeviceName = properties.getProperty("device.name.default");
			logger.info("Device name unspecified. Setting to default value: {}", defaultDeviceName);
			testParameters.setDeviceName(defaultDeviceName);
		}
		
		if (testParameters.getBrowser() == null) {
			String defaultBrowser = properties.getProperty("browser.default");
			logger.info("Browser unspecified. Setting to default value: {}", defaultBrowser);
			testParameters.setBrowser(Browser.valueOf(defaultBrowser));
		}
		
		if (testParameters.getPlatform() == null) {
			String defaultPlatform = properties.getProperty("platform.default");
			logger.info("Platform unspecified. Setting to default value: {}", defaultPlatform);
			testParameters.setPlatform(Platform.valueOf(defaultPlatform));
		}
		
		if (testParameters.getDeviceType() == null) {
			String defaultDeviceType = properties.getProperty("device.type.default");
			logger.info("Device Type unspecified. Setting to default value: {}", defaultDeviceType);
			testParameters.setDeviceType(DeviceType.valueOf(defaultDeviceType));
		}
		
		if(testParameters.getRemoteUrl() == null) {
			String defaultRemoteUrl = properties.getProperty("remote.url.default");
			logger.info("Remote URL unspecified. Setting to default value: {}", defaultRemoteUrl);
			testParameters.setRemoteUrl(defaultRemoteUrl);
		}
	}
	
	/**
	 * Function to return the absolute path where the datatables are stored
	 * @return The absolute path where the datatables are stored
	 */
	public String getDatatablePath() {
		return frameworkParameters.getBasePath() +
				Util.getFileSeparator() + "src" +
				Util.getFileSeparator() + "test" +
				Util.getFileSeparator() + "resources" +
				Util.getFileSeparator() + "datatables";
	}
	
	/**
	 * Function to initialize the {@link WebDriver} object based on the {@link WebDriverTestParameters} passed in
	 * @param testParameters The {@link WebDriverTestParameters} object
	 * @return The {@link WebDriver} object
	 */
	public WebDriver initializeWebDriver(WebDriverTestParameters testParameters) {
		logger.info("Initializing WebDriver");
		
		WebDriver driver;
		switch(testParameters.getExecutionMode()) {
		case LOCAL:
			initializeWebDriverFactory();
			driver = WebDriverFactory.getWebDriver(testParameters.getBrowser());
			break;
			
		case REMOTE:
			initializeWebDriverFactory();
			driver = WebDriverFactory.getRemoteWebDriver(testParameters.getBrowser(),
															testParameters.getRemoteUrl());
			break;
			
		case LOCAL_EMULATED_DEVICE:
			initializeWebDriverFactory();
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedWebDriver(testParameters.getDeviceName());
			break;
			
		case REMOTE_EMULATED_DEVICE:
			initializeWebDriverFactory();
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedRemoteWebDriver(testParameters.getDeviceName(), 
																	testParameters.getRemoteUrl());
			break;
			
		case GRID:
			initializeWebDriverFactory();
			driver = WebDriverFactory.getRemoteWebDriver(testParameters.getBrowser(),
													testParameters.getBrowserVersion(),
													testParameters.getPlatform(),
													testParameters.getRemoteUrl());
			break;
			
		case PERFECTO_DEVICE:
			initializePerfectoWebDriverFactory();
			driver = PerfectoWebDriverFactory.getPerfectoRemoteWebDriver(testParameters.getPerfectoDeviceId(),
																testParameters.getDeviceType(),
																testParameters.getBrowser(),
																testParameters.getRemoteUrl());
			break;
			
		case APPIUM_DEVICE:
			driver = AppiumWebDriverFactory.getAppiumWebDriver(testParameters.getDeviceName(),
														testParameters.getScreenOrientation(),
														testParameters.getBrowser(),
														testParameters.getPlatform(),
														testParameters.getRemoteUrl());
			break;
			
		default:
			throw new AutopiaException("Unhandled Execution Mode!");
		}
		
		long objectSyncTimeout =
				Long.parseLong(properties.get("timeout.object.sync").toString());
		frameworkParameters.setObjectSyncTimeout(objectSyncTimeout);
		driver.manage().timeouts().implicitlyWait(objectSyncTimeout, TimeUnit.SECONDS);
		
		long pageLoadTimeout =
				Long.parseLong(properties.get("timeout.page.load").toString());
		frameworkParameters.setPageLoadTimeout(pageLoadTimeout);
		driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		
		if(testParameters.getDeviceType().getValue().contains("desktop")) {
			driver.manage().window().maximize();
		}
		
		return driver;
	}
	
	private void initializeWebDriverFactory() {
		logger.info("Initializing WebDriverFactory");
		
		WebDriverFactory.setAcceptAllSslCertificates(Boolean.parseBoolean(properties.getProperty("ssl.certs.accept.all")));
		WebDriverFactory.setIntroduceFlakinessInternetExplorer(Boolean.parseBoolean(properties.getProperty("internet.explorer.introduce.flakiness")));
		WebDriverFactory.setTurnOffPopupBlockerInternetExplorer(Boolean.parseBoolean(properties.getProperty("internet.explorer.popupblocker.turnoff")));
		
		Boolean proxyRequired = Boolean.parseBoolean(properties.getProperty("proxy.required"));
		WebDriverFactory.setProxyRequired(proxyRequired);
		
		if (proxyRequired) {
			WebDriverProxy proxy = new WebDriverProxy();
			proxy.setHost(properties.getProperty("proxy.host"));
			proxy.setPort(Integer.parseInt(properties.getProperty("proxy.port")));
			
			Boolean authRequired = Boolean.parseBoolean(properties.getProperty("proxy.auth.required"));
			proxy.setAuthRequired(authRequired);
			if(authRequired) {
				proxy.setDomain(properties.getProperty("proxy.auth.domain"));
				proxy.setUserName(properties.getProperty("proxy.auth.username"));
				proxy.setPassword(properties.getProperty("proxy.auth.password"));
			}
			WebDriverFactory.setProxy(proxy);
		}
	}
	
	private void initializePerfectoWebDriverFactory() {
		logger.info("Initializing PerfectoWebDriverFactory");
		
		PerfectoWebDriverFactory.setAcceptAllSslCertificates(Boolean.parseBoolean(properties.getProperty("ssl.certs.accept.all")));
		PerfectoWebDriverFactory.setUserName(properties.getProperty("perfecto.username"));
		PerfectoWebDriverFactory.setPassword(properties.getProperty("perfecto.password"));
	}
	
	/**
	 * Function to initialize the {@link WebDriverReport} object
	 * @param testParameters The {@link WebDriverTestParameters} object
	 * @param driver The {@link WebDriver} object
	 * @return The {@link WebDriverReport} object
	 */
	public WebDriverReport initializeTestReport(WebDriverTestParameters testParameters, WebDriver driver) {
		logger.info("Initializing test log");
		
		ReportSettings reportSettings = initializeReportSettings(testParameters);
		ReportTheme reportTheme =
				ReportThemeFactory.getReportsTheme(Theme.valueOf(properties.getProperty("report.theme")));
		
		WebDriverReport report = new WebDriverReport(reportSettings, reportTheme);
		
		report.initialize();
		report.setDriver(driver);
		report.initializeTestLog();
		createTestLogHeader(report, testParameters);
		
		return report;
	}
	
	private ReportSettings initializeReportSettings(WebDriverTestParameters testParameters) {
		String reportPath;
		if(System.getProperty("autopia.report.path") != null) {
			reportPath = System.getProperty("autopia.report.path");
		} else {
			reportPath = TimeStamp.getInstance();
		}
		String reportName = testParameters.getCurrentModule() +
							"_" + testParameters.getCurrentTestcase() +
							"_" + testParameters.getCurrentTestInstance();
		
		ReportSettings reportSettings = new ReportSettings(reportPath, reportName);
		reportSettings.setDateFormatString(properties.getProperty("date.format.string"));
		reportSettings.setLogLevel(Integer.parseInt(properties.getProperty("report.level")));
		reportSettings.setProjectName(properties.getProperty("project.name"));
		reportSettings.setGenerateExcelReports(Boolean.parseBoolean(properties.getProperty("report.excel.enable")));
		reportSettings.setGenerateHtmlReports(Boolean.parseBoolean(properties.getProperty("report.html.enable")));
		reportSettings.setConsolidateScreenshotsInWordDoc(
				Boolean.parseBoolean(properties.getProperty("report.screenshots.consolidate.worddoc")));
		if (testParameters.getBrowser().equals(Browser.HTML_UNIT)) {
			// Screenshots not supported in headless mode
			reportSettings.setLinkScreenshotsToTestLog(false);
		} else {
			reportSettings.setLinkScreenshotsToTestLog(true);
		}
		
		return reportSettings;
	}
	
	private void createTestLogHeader(WebDriverReport report, WebDriverTestParameters testParameters) {
		ReportSettings reportSettings = report.getReportSettings();
		report.addTestLogHeading(reportSettings.getProjectName() +
									" - " + reportSettings.getReportName() +
									" Automation Execution Results");
		report.addTestLogSubHeading("Date & Time",
										": " + Util.getFormattedTime(startTime, properties.getProperty("date.format.string")),
										"Iteration Mode", ": " + testParameters.getIterationMode());
		report.addTestLogSubHeading("Start Iteration", ": " + testParameters.getStartIteration(),
									"End Iteration", ": " + testParameters.getEndIteration());
		
		switch(testParameters.getExecutionMode()) {
		case LOCAL:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Local Machine");
			break;
			
		case REMOTE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Remote Machine @ " + testParameters.getRemoteUrl());
			break;
			
		case LOCAL_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Emulated Mobile Device on Local Machine");
			report.addTestLogSubHeading("Emulated Device Name", ": " + testParameters.getDeviceName(), "", "");
			break;
			
		case REMOTE_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Emulated Mobile Device on Remote Machine @ " + testParameters.getRemoteUrl());
			report.addTestLogSubHeading("Emulated Device Name", ": " + testParameters.getDeviceName(), "", "");
			break;
			
		case GRID:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Grid @ " + testParameters.getRemoteUrl());
			break;
			
		case PERFECTO_DEVICE:
            report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
            		"Executed on", ": " + "Perfecto MobileCloud @ " + testParameters.getRemoteUrl()); 
            report.addTestLogSubHeading("Device Name/ID", ": " + testParameters.getDeviceName() +
            		" (" + testParameters.getPerfectoDeviceId() + ")",
            		"Perfecto User", ": " + properties.getProperty("perfecto.username")); 
            break;
            
		case APPIUM_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Mobile Device on Appium Server @ " + testParameters.getRemoteUrl());
			report.addTestLogSubHeading("Device Type", ": " + testParameters.getDeviceType(),
					"Device Name", ": " + testParameters.getDeviceName());
            break;
            
		default:
			throw new AutopiaException("Unhandled Execution Mode!");
		}
		
		report.addTestLogTableHeadings();
	}
	
	/**
	 * TODO
	 * @param datatablePath
	 * @param report
	 * @param testParameters
	 * @return
	 */
	public String getRuntimeDatatablePath(String datatablePath,WebDriverReport report,
													WebDriverTestParameters testParameters) {
		logger.info("Initializing runtime datatable path");
		String runTimeDatatablePath;
		Boolean includeTestDataInReport =
				Boolean.parseBoolean(properties.getProperty("report.datatable.include"));
		if (includeTestDataInReport) {
			runTimeDatatablePath = report.getReportSettings().getReportPath() +
											Util.getFileSeparator() + "datatables";
			
			File runTimeDatatable = new File(runTimeDatatablePath + Util.getFileSeparator() +
												testParameters.getCurrentModule() + ".xls");
			if (!runTimeDatatable.exists()) {
				synchronized (TestHarness.class) {
					if (!runTimeDatatable.exists()) {
						File datatable = new File(datatablePath + Util.getFileSeparator() +
													testParameters.getCurrentModule() + ".xls");
						
						try {
							FileUtils.copyFile(datatable, runTimeDatatable);
						} catch (IOException e) {
							String errorDescription = "Error in creating run-time datatable: Copying the datatable failed...";
							logger.error(errorDescription, e);
							throw new AutopiaException(errorDescription);
						}
					}
				}
			}
			
			File runTimeCommonDatatable = new File(runTimeDatatablePath +
													Util.getFileSeparator() +
													"Common Testdata.xls");
			if (!runTimeCommonDatatable.exists()) {
				synchronized (TestHarness.class) {
					if (!runTimeCommonDatatable.exists()) {
						File commonDatatable = new File(datatablePath +
												Util.getFileSeparator() + "Common Testdata.xls");
						
						try {
							FileUtils.copyFile(commonDatatable, runTimeCommonDatatable);
						} catch (IOException e) {
							String errorDescription = "Error in creating run-time datatable: Copying the common datatable failed...";
							logger.error(errorDescription, e);
							throw new AutopiaException(errorDescription);
						}
					}
				}
			}
		} else {
			runTimeDatatablePath = datatablePath;
		}
		
		return runTimeDatatablePath;
	}
	
	/**
	 * Function to download the PerfectoMobile run results
	 * @param driver The {@link WebDriver} object
	 * @param report The {@link WebDriverReport} object
	 */
	public void downloadPerfectoResults(WebDriver driver, WebDriverReport report) {
		PerfectoWebDriverUtil perfectoWebDriverUtil = new PerfectoWebDriverUtil(driver, report);
		perfectoWebDriverUtil.downloadPerfectoResults();
	}
	
	/**
	 * Function to tear-down the {@link WebDriver} object
	 * @param driver The {@link WebDriver} object
	 */
	public void quitWebDriver(WebDriver driver) {
		logger.info("Quitting WebDriver");
		
		try {
			driver.quit();
		} catch(Exception ex) {
			logger.error("Exception while closing the browser", ex);
		}
	}
	
	/**
	 * Function to tear-down the {@link TestHarness} object
	 * @param scriptHelper The {@link ScriptHelper} object
	 * @return The script execution time
	 */
	public String tearDown(ScriptHelper scriptHelper) {
		logger.info("Test execution complete");
		
		Date endTime = Util.getCurrentTime();
		return Util.getTimeDifference(startTime, endTime);
	}
	
	/**
	 * Function to close the test report
	 * @param scriptHelper The {@link ScriptHelper} object
	 * @param executionTime The script execution time
	 */
	public void closeTestReport(ScriptHelper scriptHelper, String executionTime) {
		GalenUtil galenUtil = scriptHelper.getGalenUtil();
		galenUtil.exportGalenReports();
		
		WebDriverReport report = scriptHelper.getReport();
		if (report.getReportSettings().shouldConsolidateScreenshotsInWordDoc()) {
			report.consolidateScreenshotsInWordDoc();
		}
		
		report.addTestLogFooter(executionTime);
	}
}