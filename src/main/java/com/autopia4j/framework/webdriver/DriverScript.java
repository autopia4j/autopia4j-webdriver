package com.autopia4j.framework.webdriver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

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

public abstract class DriverScript {
	
	protected int currentIteration;
	
	private Date startTime, endTime;
	protected String executionTime;
	
	protected DataTableType dataTable;
	protected ReportSettings reportSettings;
	protected WebDriverReport report;
	protected WebDriver driver;
	protected WebDriverUtil driverUtil;
	protected GalenUtil galenUtil;
	protected ScriptHelper scriptHelper;
	
	protected Properties properties;
	protected final FrameworkParameters frameworkParameters =
										FrameworkParameters.getInstance();
	
	private Boolean linkScreenshotsToTestLog = true;
	
	protected final WebDriverTestParameters testParameters;
	protected String reportPath;
	
	
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
			testParameters.setDeviceName(properties.getProperty("DefaultDevice"));
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
													properties.getProperty("RemoteUrl"));
			break;
			
		case LOCAL_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedWebDriver(testParameters.getDeviceName());
			break;
			
		case REMOTE_EMULATED_DEVICE:
			testParameters.setBrowser(Browser.CHROME);	// Mobile emulation supported only on Chrome
			driver = WebDriverFactory.getEmulatedRemoteWebDriver(testParameters.getDeviceName(), 
													properties.getProperty("RemoteUrl"));
			break;
			
		case GRID:
			driver = WebDriverFactory.getRemoteWebDriver(testParameters.getBrowser(),
													testParameters.getBrowserVersion(),
													testParameters.getPlatform(),
													properties.getProperty("RemoteUrl"));
			break;
			
		case PERFECTO_REMOTEWEBDRIVER:
			driver = PerfectoWebDriverFactory.getPerfectoRemoteWebDriver(testParameters.getPerfectoDeviceId(),
																testParameters.getDeviceType(),
																testParameters.getBrowser(),
																properties.getProperty("PerfectoHost"));
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
		driver.manage().window().maximize();
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
					"Executed on", ": " + "Remote Machine @ " + properties.getProperty("RemoteUrl"));
			break;
			
		case LOCAL_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Emulated Mobile Device on Local Machine");
			report.addTestLogSubHeading("Emulated Device Name", ": " + testParameters.getDeviceName(), "", "");
			break;
			
		case REMOTE_EMULATED_DEVICE:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Emulated Mobile Device on Remote Machine @ " + properties.getProperty("RemoteUrl"));
			report.addTestLogSubHeading("Emulated Device Name", ": " + testParameters.getDeviceName(), "", "");
			break;
			
		case GRID:
			report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
					"Executed on", ": " + "Grid @ " + properties.getProperty("RemoteUrl"));
			break;
			
		case PERFECTO_REMOTEWEBDRIVER:
            report.addTestLogSubHeading("Browser/Platform", ": " + testParameters.getBrowserAndPlatform(),
            		"Executed on", ": " + "Perfecto MobileCloud @ " + properties.getProperty("PerfectoHost")); 
            report.addTestLogSubHeading("Device Name/ID", ": " + testParameters.getDeviceName() +
            		" (" + testParameters.getPerfectoDeviceId() + ")",
            		"Perfecto User", ": " + properties.getProperty("PerfectoUser")); 
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
			// Stop option is not relevant when run from QC
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
		
		if (testParameters.getExecutionMode() == ExecutionMode.PERFECTO_REMOTEWEBDRIVER) {
			downloadPerfectoResults();
		}
		
		try {
			driver.quit();
			report.updateTestLog("Close browser", "Browser closed successfully", Status.DONE);
		} catch(Exception ex) {
			report.updateTestLog("Close browser", ex.getMessage(), Status.WARNING);
		}
	}
	
	private void downloadPerfectoResults() {
		Boolean perfectoHtmlReport =
					Boolean.parseBoolean(properties.getProperty("PerfectoHtmlReport"));
		Boolean perfectoPdfReport =
					Boolean.parseBoolean(properties.getProperty("PerfectoPdfReport"));
		Boolean perfectoVideoDownload =
					Boolean.parseBoolean(properties.getProperty("PerfectoVideoDownload"));
		
		File perfectoResultsFolder = null;
		if (perfectoHtmlReport | perfectoPdfReport | perfectoVideoDownload) {
			driver.close();
			perfectoResultsFolder = report.createResultsSubFolder("Perfecto Results");
		}
		if (perfectoHtmlReport) {
			downloadPerfectoReport("html", perfectoResultsFolder.getAbsolutePath());
		}
		if (perfectoPdfReport) {
			downloadPerfectoReport("pdf", perfectoResultsFolder.getAbsolutePath());
		}
		if (perfectoVideoDownload) {
			downloadPerfectoAttachment("video", "flv", perfectoResultsFolder.getAbsolutePath());
		}
	}
	
	/**
	 * Function to download the execution result from Perfecto
	 * @param reportType Specify any one format from "pdf", "html", "csv" or "xml"
	 * @param reportPath The path at which the report should be saved
	 */
	private void downloadPerfectoReport(String reportType, String reportPath) {
		String command = "mobile:report:download";
		Map<String, Object> params = new HashMap<>();
		params.put("type", reportType);
		String reportRawData = (String) ((RemoteWebDriver) driver).executeScript(command, params);
		
		try {
			File reportFile = new File(reportPath + Util.getFileSeparator() +
									reportSettings.getReportName() + "." + reportType);
			BufferedOutputStream outputStream =
							new BufferedOutputStream(new FileOutputStream(reportFile));
			byte[] reportBytes = OutputType.BYTES.convertFromBase64Png(reportRawData);
			outputStream.write(reportBytes);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new FrameworkException("Error occurred while downloading Perfecto report");
		}
	}
	
	/**
	* Function to download all the report attachments of the specified type
	* @param attachmentType Specify one from "video", "image", "vital" or "network"
	* @param fileExtension The extension of the attachment files to be saved
	* @param reportPath The path at which the report should be saved
	*/
	private void downloadPerfectoAttachment(String attachmentType, String fileExtension, String reportPath) {
		String command = "mobile:report:attachment";
		int index = 0;
		String attachmentRawData;
		
		while(true) {
			Map<String, Object> params = new HashMap<>();
		    params.put("type", attachmentType);
		    params.put("index", Integer.toString(index));
		    attachmentRawData =
		    		(String) ((RemoteWebDriver) driver).executeScript(command, params);
		    
		    if (attachmentRawData == null) {
		    	break;
		    }
		    
		    try {
				File attachmentFile = new File(reportPath + Util.getFileSeparator() +
											reportSettings.getReportName() + "_" +
											attachmentType + index + "." + fileExtension);
				BufferedOutputStream outputStream =
						new BufferedOutputStream(new FileOutputStream(attachmentFile)); 
				byte[] bytes = OutputType.BYTES.convertFromBase64Png(attachmentRawData);
				outputStream.write(bytes);
				outputStream.close();
				index ++;
	    	} catch (IOException e) {
				e.printStackTrace();
				throw new FrameworkException("Error occurred while downloading Perfecto attachment");
			}
		}
	}
	
	protected void wrapUp() {
		endTime = Util.getCurrentTime();
		closeTestReport();
	}
	
	private void closeTestReport() {
		galenUtil.exportGalenReports();
		
		if (reportSettings.shouldConsolidateScreenshotsInWordDoc()) {
			report.consolidateScreenshotsInWordDoc();
		}
		
		executionTime = Util.getTimeDifference(startTime, endTime);
		report.addTestLogFooter(executionTime);
	}
}