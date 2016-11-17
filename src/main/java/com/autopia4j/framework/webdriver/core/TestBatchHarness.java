package com.autopia4j.framework.webdriver.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.core.TimeStamp;
import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.ReportTheme;
import com.autopia4j.framework.reporting.ReportThemeFactory;
import com.autopia4j.framework.reporting.ReportThemeFactory.Theme;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;


/**
 * Singleton class that manages the result summary creation during a batch execution
 * @author vj
 */
public class TestBatchHarness {
	private final Logger logger = LoggerFactory.getLogger(TestBatchHarness.class);
	private WebDriverReport summaryReport;
	
	private Date overallStartTime;
	
	private FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
	private Properties properties;
	
	private static final TestBatchHarness TEST_BATCH_HARNESS = new TestBatchHarness();
	
	private TestBatchHarness() {
		// To prevent external instantiation of this class
	}
	
	/**
	 * Function to return the singleton instance of the {@link TestBatchHarness} object
	 * @return Instance of the {@link TestBatchHarness} object
	 */
	public static TestBatchHarness getInstance() {
		return TEST_BATCH_HARNESS;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	
	/**
	 * Function to initialize the test batch execution
	 */
	public void initialize() {
		overallStartTime = Util.getCurrentTime();
		
		logger.info("Starting test batch execution");
		initializeFrameworkParameters();
	}
	
	private void initializeFrameworkParameters() {
		logger.info("Initializing global framework parameters");
		
		initializeBasePath();
		properties = Settings.getInstance();
		initializeRunConfiguration();
		initializeExecutionEnvironment();
		initializeDateFormat();
	}
	
	private void initializeBasePath() {
		String basePath = new File(System.getProperty("user.dir")).getAbsolutePath();
		frameworkParameters.setBasePath(basePath);
	}
	
	private void initializeRunConfiguration() {
		String runConfiguration = System.getProperty("autopia.run.configuration");
		if (runConfiguration != null) {
			frameworkParameters.setRunConfiguration(runConfiguration);
		} else {
			frameworkParameters.setRunConfiguration(properties.getProperty("run.configuration"));
		}
	}
	
	private void initializeExecutionEnvironment() {
		String autopiaExecEnvironment = System.getProperty("autopia.execution.environment");
		if (autopiaExecEnvironment != null) {
			frameworkParameters.setExecutionEnvironment(autopiaExecEnvironment);
		} else {
			frameworkParameters.setExecutionEnvironment(properties.getProperty("execution.environment"));
		}
	}
	
	private void initializeDateFormat() {
		String dateFormatString = properties.getProperty("date.format.string");
		if(dateFormatString != null) {
			frameworkParameters.setDateFormatString(dateFormatString);
		}
	}
	
	/**
	 * Function to initialize the summary report
	 * @param nThreads The number of parallel threads configured for the test batch execution
	 */
	public void initializeSummaryReport(int nThreads) {
		ReportSettings reportSettings = initializeReportSettings();
		ReportTheme reportTheme =
				ReportThemeFactory.getReportsTheme(Theme.valueOf(properties.getProperty("report.theme")));
		
		summaryReport = new WebDriverReport(reportSettings, reportTheme);
		
		summaryReport.initialize();
		summaryReport.initializeResultSummary();
		
		createResultSummaryHeader(nThreads);
	}
	
	private ReportSettings initializeReportSettings() {
		String reportPath;
		if(System.getProperty("autopia.report.path") != null) {
			reportPath = System.getProperty("autopia.report.path");
		} else {
			reportPath = TimeStamp.getInstance();
		}
		
		ReportSettings reportSettings = new ReportSettings(reportPath, "Summary");
		
		reportSettings.setDateFormatString(properties.getProperty("date.format.string"));
		reportSettings.setProjectName(properties.getProperty("project.name"));
		reportSettings.setGenerateExcelReports(Boolean.parseBoolean(properties.getProperty("report.excel.enable")));
		reportSettings.setGenerateHtmlReports(Boolean.parseBoolean(properties.getProperty("report.html.enable")));
		reportSettings.setLinkTestLogsToSummary(true);
		
		return reportSettings;
	}
	
	private void createResultSummaryHeader(int nThreads) {
		summaryReport.addResultSummaryHeading(summaryReport.getReportSettings().getProjectName() +
											" - Automation Execution Results Summary");
		summaryReport.addResultSummarySubHeading("Date & Time",
								": " + Util.getFormattedTime(overallStartTime,
								properties.getProperty("date.format.string")),
								"OnError", ": " + properties.getProperty("on.error"));
		summaryReport.addResultSummarySubHeading("Run Configuration",
								": " + frameworkParameters.getRunConfiguration(),
								"No. of threads", ": " + nThreads);
		
		summaryReport.addResultSummaryTableHeadings();
	}
	
	/**
	 * Function to update the results summary with the status of the test instance which was executed
	 * @param testParameters The {@link WebDriverTestParameters} object containing the details of the test instance which was executed
	 * @param testReportName The name of the test report file corresponding to the test instance
	 * @param executionTime The time taken to execute the test instance
	 * @param testStatus The Pass/Fail status of the test instance
	 */
	public void updateResultSummary(WebDriverTestParameters testParameters, String testReportName,
												String executionTime, String testStatus) {
		logger.info("Updating summary report");
		
		summaryReport.updateResultSummary(testParameters, testReportName,
													executionTime, testStatus);
	}
	
	/**
	 * Function to do the required wrap-up activities after completing the test batch execution
	 * @param testExecutedInUnitTestFramework Boolean variable indicating whether the test is executed in JUnit/TestNG
	 */
	public void wrapUp(Boolean testExecutedInUnitTestFramework) {
		logger.info("Test batch execution complete");
		
		Date overallEndTime = Util.getCurrentTime();
		String totalExecutionTime = Util.getTimeDifference(overallStartTime, overallEndTime);
		summaryReport.addResultSummaryFooter(totalExecutionTime);
		
		if(testExecutedInUnitTestFramework && System.getProperty("autopia.report.path") == null) {
			copyTestNgResults();
		}
		
		summaryReport.copyLogFile();
	}
	
	private void copyTestNgResults() {
		File testNgResultSrc = new File(frameworkParameters.getBasePath() +
										Util.getFileSeparator() +
										properties.getProperty("report.testng.path") +
										Util.getFileSeparator() +
										frameworkParameters.getRunConfiguration());		
		File testNgResultCssFile = new File(frameworkParameters.getBasePath() +
										Util.getFileSeparator() +
										properties.getProperty("report.testng.path") +
										Util.getFileSeparator() +
										"testng.css");
		File testNgResultDest =
							summaryReport.createResultsSubFolder("TestNG Results");
		
		try {
			if(testNgResultSrc.exists()) {
				FileUtils.copyDirectoryToDirectory(testNgResultSrc, testNgResultDest);
				FileUtils.copyFileToDirectory(testNgResultCssFile, testNgResultDest);
			} else {
				logger.info("Unable to copy TestNG results, because they are not found @ " + testNgResultSrc);
			}
		} catch (IOException e) {
			logger.error("Error occurred while copying TestNG reports to the Results folder", e);
		}
	}
	
	/**
	 * Function to launch the summary report at the end of the test batch execution
	 */
	public void launchResultSummary() {
		if (summaryReport.getReportSettings().shouldGenerateHtmlReports()) {
			try {
				Runtime.getRuntime().exec("RunDLL32.EXE shell32.dll,ShellExec_RunDLL " +
											summaryReport.getReportSettings().getReportPath() +
											"\\HTML Results\\Summary.Html");
			} catch (IOException e) {
				logger.error("Error occurred while launching the result summary", e);
			}
		} else if (summaryReport.getReportSettings().shouldGenerateExcelReports()) {
			try {
				Runtime.getRuntime().exec("RunDLL32.EXE shell32.dll,ShellExec_RunDLL " +
											summaryReport.getReportSettings().getReportPath() +
											"\\Excel Results\\Summary.xls");
			} catch (IOException e) {
				logger.error("Error occurred while launching the result summary", e);
			}
		}
	}
}