package com.autopia4j.framework.webdriver.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.IterationOptions;
import com.autopia4j.framework.core.OnError;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.core.TimeStamp;
import com.autopia4j.framework.datatable.DataTableType;
import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.ReportTheme;
import com.autopia4j.framework.reporting.ReportThemeFactory;
import com.autopia4j.framework.reporting.Status;
import com.autopia4j.framework.reporting.ReportThemeFactory.Theme;
import com.autopia4j.framework.utils.FrameworkException;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.mobile.AppiumWebDriverFactory;
import com.autopia4j.framework.webdriver.mobile.PerfectoWebDriverFactory;
import com.autopia4j.framework.webdriver.mobile.PerfectoWebDriverUtil;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverFactory;

public abstract class DriverScript {
	
	protected int currentIteration;
	
	private Date startTime, endTime;
	protected String executionTime;
	
	protected DataTableType dataTable;
	protected ReportSettings reportSettings;
	protected WebDriverReport report;
	protected WebDriver driver;
	protected ScriptHelper scriptHelper;
	
	protected Properties properties;
	protected final FrameworkParameters frameworkParameters =
										FrameworkParameters.getInstance();
	
	private Boolean linkScreenshotsToTestLog = true;
	
	protected final WebDriverTestParameters testParameters;
	protected String datatablePath, reportPath;
	
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public DriverScript(WebDriverTestParameters testParameters) {
		this.testParameters = testParameters;
	}
	
	/**
	 * Function to configure the linking of screenshots to the corresponding test log
	 * @param linkScreenshotsToTestLog Boolean variable indicating whether screenshots should be linked to the corresponding test log
	 */
	public void setLinkScreenshotsToTestLog(Boolean linkScreenshotsToTestLog) {
		this.linkScreenshotsToTestLog = linkScreenshotsToTestLog;
	}
	
	/**
	 * Function to get the name of the test report
	 * @return The test report name
	 */
	public String getReportName() {
		return reportSettings.getReportName();
	}
	
	/**
	 * Function to get the status of the test case executed
	 * @return The test status
	 */
	public String getTestStatus() {
		return report.getTestStatus();
	}
	
	/**
	 * Function to get the description of any failure that may occur during the script execution
	 * @return The failure description (relevant only if the test fails)
	 */
	public String getFailureDescription() {
		return report.getFailureDescription();
	}
	
	/**
	 * Function to get the execution time for the test case
	 * @return The test execution time
	 */
	public String getExecutionTime() {
		return executionTime;
	}
	
	/**
	 * Function to execute the given test case
	 */
	public abstract void driveTestExecution();
	
	protected void startUp() {
		startTime = Util.getCurrentTime();
		
		properties = Settings.getInstance();
		
		datatablePath = frameworkParameters.getBasePath() +
							Util.getFileSeparator() + "src" +
							Util.getFileSeparator() + "test" +
							Util.getFileSeparator() + "resources" +
							Util.getFileSeparator() + "datatables";
		
		setDefaultTestParameters();
	}
	
	private void setDefaultTestParameters() {
		if (testParameters.getIterationMode() == null) {
			testParameters.setIterationMode(IterationOptions.RUN_ALL_ITERATIONS);
		}
		
		if (testParameters.getExecutionMode() == null) {
			testParameters.setExecutionMode(ExecutionMode.valueOf(properties.getProperty("DefaultExecutionMode")));
		}
		
		if (testParameters.getDeviceName() == null) {
			testParameters.setDeviceName(properties.getProperty("DefaultDeviceName"));
		}
		
		if (testParameters.getBrowser() == null) {
			testParameters.setBrowser(Browser.valueOf(properties.getProperty("DefaultBrowser")));
		}
		
		if (testParameters.getPlatform() == null) {
			testParameters.setPlatform(Platform.valueOf(properties.getProperty("DefaultPlatform")));
		}
		
		if (testParameters.getDeviceType() == null) {
			testParameters.setDeviceType(DeviceType.valueOf(properties.getProperty("DefaultDeviceType")));
		}
		
		if(testParameters.getRemoteUrl() == null) {
			testParameters.setRemoteUrl(properties.getProperty("DefaultRemoteUrl"));
		}
	}
	
	protected void initializeTestIterations() {
		switch(testParameters.getIterationMode()) {
		case RUN_ALL_ITERATIONS:
			int nIterations = getNumberOfIterations();
			testParameters.setEndIteration(nIterations);
			
			currentIteration = 1;
			break;
			
		case RUN_ONE_ITERATION_ONLY:
			currentIteration = 1;
			break;
			
		case RUN_RANGE_OF_ITERATIONS:
			if(testParameters.getStartIteration() > testParameters.getEndIteration()) {
				throw new FrameworkException("Error","StartIteration cannot be greater than EndIteration!");
			}
			currentIteration = testParameters.getStartIteration();
			break;
			
		default:
			throw new FrameworkException("Unhandled Iteration Mode!");
		}
	}
	
	protected abstract int getNumberOfIterations();
	
	protected void initializeWebDriver() {
		switch(testParameters.getExecutionMode()) {
		case LOCAL:
			driver = WebDriverFactory.getWebDriver(testParameters.getBrowser());
			break;
			
		case REMOTE:
			driver = WebDriverFactory.getRemoteWebDriver(testParameters.getBrowser(),
															testParameters.getRemoteUrl());
			break;
			
		case LOCAL_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedWebDriver(testParameters.getDeviceName());
			break;
			
		case REMOTE_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedRemoteWebDriver(testParameters.getDeviceName(), 
																	testParameters.getRemoteUrl());
			break;
			
		case GRID:
			driver = WebDriverFactory.getRemoteWebDriver(testParameters.getBrowser(),
													testParameters.getBrowserVersion(),
													testParameters.getPlatform(),
													testParameters.getRemoteUrl());
			break;
			
		case PERFECTO_DEVICE:
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
			throw new FrameworkException("Unhandled Execution Mode!");
		}
		
		long objectSyncTimeout =
				Long.parseLong(properties.get("ObjectSyncTimeout").toString());
		long pageLoadTimeout =
				Long.parseLong(properties.get("PageLoadTimeout").toString());
		driver.manage().timeouts().implicitlyWait(objectSyncTimeout, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		
		if(testParameters.getDeviceType().getValue().contains("desktop")) {
			driver.manage().window().maximize();
		}
	}
	
	protected void initializeTestReport() {
		initializeReportSettings();
		ReportTheme reportTheme =
				ReportThemeFactory.getReportsTheme(Theme.valueOf(properties.getProperty("ReportsTheme")));
		
		report = new WebDriverReport(reportSettings, reportTheme);
		
		report.initialize();
		report.setDriver(driver);
		report.initializeTestLog();
		createTestLogHeader();
	}
	
	private void initializeReportSettings() {
		if(System.getProperty("ReportPath") != null) {
			reportPath = System.getProperty("ReportPath");
		} else {
			reportPath = TimeStamp.getInstance();
		}
		
		reportSettings = new ReportSettings(reportPath,
											testParameters.getCurrentModule() +
											"_" + testParameters.getCurrentTestcase() +
											"_" + testParameters.getCurrentTestInstance());
		
		reportSettings.setDateFormatString(properties.getProperty("DateFormatString"));
		reportSettings.setLogLevel(Integer.parseInt(properties.getProperty("LogLevel")));
		reportSettings.setProjectName(properties.getProperty("ProjectName"));
		reportSettings.setGenerateExcelReports(Boolean.parseBoolean(properties.getProperty("ExcelReport")));
		reportSettings.setGenerateHtmlReports(Boolean.parseBoolean(properties.getProperty("HtmlReport")));
		reportSettings.setConsolidateScreenshotsInWordDoc(
				Boolean.parseBoolean(properties.getProperty("ConsolidateScreenshotsInWordDoc")));
		if (testParameters.getBrowser().equals(Browser.HTML_UNIT)) {
			// Screenshots not supported in headless mode
			reportSettings.setLinkScreenshotsToTestLog(false);
		} else {
			reportSettings.setLinkScreenshotsToTestLog(this.linkScreenshotsToTestLog);
		}
	}
	
	private void createTestLogHeader() {
		report.addTestLogHeading(reportSettings.getProjectName() +
									" - " + reportSettings.getReportName() +
									" Automation Execution Results");
		report.addTestLogSubHeading("Date & Time",
										": " + Util.getFormattedTime(startTime, properties.getProperty("DateFormatString")),
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
            		"Executed on", ": " + "Perfecto MobileCloud @ " + properties.getProperty("PerfectoHost")); 
            report.addTestLogSubHeading("Device Name/ID", ": " + testParameters.getDeviceName() +
            		" (" + testParameters.getPerfectoDeviceId() + ")",
            		"Perfecto User", ": " + properties.getProperty("PerfectoUser")); 
            break;
            
		case APPIUM_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Mobile Device on Appium Server @ " + testParameters.getRemoteUrl());
			report.addTestLogSubHeading("Device Type", ": " + testParameters.getDeviceType(),
					"Device Name", ": " + testParameters.getDeviceName());
            break;
            
		default:
			throw new FrameworkException("Unhandled Execution Mode!");
		}
		
		report.addTestLogTableHeadings();
	}
	
	protected void exceptionHandler(Exception ex, String exceptionName) {
		// Error reporting
		String exceptionDescription = ex.getMessage();
		if(exceptionDescription == null) {
			exceptionDescription = ex.toString();
		}
		
		if(ex.getCause() != null) {
			report.updateTestLog(exceptionName, exceptionDescription + " <b>Caused by: </b>" +
																ex.getCause(), Status.FAIL, true);
		} else {
			report.updateTestLog(exceptionName, exceptionDescription, Status.FAIL, true);
		}
		
		// Print stack trace for detailed debug information
		ex.printStackTrace();
		
		StringWriter stringWriter = new StringWriter();
		ex.printStackTrace(new PrintWriter(stringWriter));
		String stackTrace = stringWriter.toString();
		report.updateTestLog("Exception stack trace", stackTrace, Status.DEBUG);
		
		// Error response
		report.addTestLogSubSection("ErrorResponse");
		if (frameworkParameters.getStopExecution()) {
			report.updateTestLog("Framework Info",
					"Test execution terminated by user! All subsequent tests aborted...",
					Status.DONE);
			currentIteration = testParameters.getEndIteration();
		} else {
			OnError onError = OnError.valueOf(properties.getProperty("OnError"));
			switch(onError) {
			// Stop option is not relevant when run from HP ALM
			case NEXT_ITERATION:
				report.updateTestLog("Framework Info",
						"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
						Status.DONE);
				break;
				
			case NEXT_TESTCASE:
				report.updateTestLog("Framework Info",
						"Test case terminated by user! Proceeding to next test case (if applicable)...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;
				
			case STOP:
				frameworkParameters.setStopExecution(true);
				report.updateTestLog("Framework Info",
						"Test execution terminated by user! All subsequent tests aborted...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;
				
			default:
				throw new FrameworkException("Unhandled OnError option!");
			}
		}
	}
	
	protected void quitWebDriver() {
		report.addTestLogSubSection("CloseBrowser");
		
		if (testParameters.getExecutionMode() == ExecutionMode.PERFECTO_DEVICE) {
			PerfectoWebDriverUtil perfectoWebDriverUtil = new PerfectoWebDriverUtil(driver, report);
			perfectoWebDriverUtil.downloadPerfectoResults();
		}
		
		try {
			driver.quit();
			report.updateTestLog("Close browser", "Browser closed successfully", Status.DONE);
		} catch(Exception ex) {
			report.updateTestLog("Close browser", ex.getMessage(), Status.WARNING);
		}
	}
	
	protected void wrapUp() {
		endTime = Util.getCurrentTime();
		closeTestReport();
	}
	
	private void closeTestReport() {
		GalenUtil galenUtil = scriptHelper.getGalenUtil();
		galenUtil.exportGalenReports();
		
		if (reportSettings.shouldConsolidateScreenshotsInWordDoc()) {
			report.consolidateScreenshotsInWordDoc();
		}
		
		executionTime = Util.getTimeDifference(startTime, endTime);
		report.addTestLogFooter(executionTime);
	}
}