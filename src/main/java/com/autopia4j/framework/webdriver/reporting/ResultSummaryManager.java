package com.autopia4j.framework.webdriver.reporting;

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
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;


/**
 * Singleton class that manages the result summary creation during a batch execution
 * @author vj
 */
public class ResultSummaryManager {
	private final Logger logger = LoggerFactory.getLogger(ResultSummaryManager.class);
	private WebDriverReport summaryReport;
	
	private ReportSettings reportSettings;
	private String reportPath;
	
	private Date overallStartTime;
	private Date overallEndTime;
	
	private Properties properties;
	private FrameworkParameters frameworkParameters =
									FrameworkParameters.getInstance();
	
	private static final ResultSummaryManager RESULT_SUMMARY_MANAGER =
													new ResultSummaryManager();
	
	private ResultSummaryManager() {
		// To prevent external instantiation of this class
	}
	
	/**
	 * Function to return the singleton instance of the {@link ResultSummaryManager} object
	 * @return Instance of the {@link ResultSummaryManager} object
	 */
	public static ResultSummaryManager getInstance() {
		return RESULT_SUMMARY_MANAGER;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Function to set the absolute path of the framework (to be used as a relative path) 
	 */
	public void setRelativePath() {
		String relativePath = new File(System.getProperty("user.dir")).getAbsolutePath();
		frameworkParameters.setBasePath(relativePath);
	}
	
	/**
	 * Function to initialize the test batch execution
	 * @param runConfiguration The run configuration to be executed
	 * @param executionEnvironment The environment on which the test batch is to be executed
	 */
	public void initializeTestBatch(String runConfiguration, String executionEnvironment) {
		overallStartTime = Util.getCurrentTime();
		
		properties = Settings.getInstance();
		
		frameworkParameters.setRunConfiguration(runConfiguration);
		frameworkParameters.setExecutionEnvironment(executionEnvironment);
	}
	
	/**
	 * Function to initialize the summary report
	 * @param nThreads The number of parallel threads configured for the test batch execution
	 */
	public void initializeSummaryReport(int nThreads) {
		initializeReportSettings();
		ReportTheme reportTheme =
				ReportThemeFactory.getReportsTheme(Theme.valueOf(properties.getProperty("ReportsTheme")));
		
		summaryReport = new WebDriverReport(reportSettings, reportTheme);
		
		summaryReport.initialize();
		summaryReport.initializeResultSummary();
		
		createResultSummaryHeader(nThreads);
	}
	
	private void initializeReportSettings() {
		if(System.getProperty("ReportPath") != null) {
			reportPath = System.getProperty("ReportPath");
		} else {
			reportPath = TimeStamp.getInstance();
		}
		
		reportSettings = new ReportSettings(reportPath, "");
		
		reportSettings.setDateFormatString(properties.getProperty("DateFormatString"));
		reportSettings.setProjectName(properties.getProperty("ProjectName"));
		reportSettings.setGenerateExcelReports(Boolean.parseBoolean(properties.getProperty("ExcelReport")));
		reportSettings.setGenerateHtmlReports(Boolean.parseBoolean(properties.getProperty("HtmlReport")));
		reportSettings.setLinkTestLogsToSummary(true);
	}
	
	private void createResultSummaryHeader(int nThreads) {
		summaryReport.addResultSummaryHeading(reportSettings.getProjectName() +
											" - Automation Execution Results Summary");
		summaryReport.addResultSummarySubHeading("Date & Time",
								": " + Util.getFormattedTime(overallStartTime,
								properties.getProperty("DateFormatString")),
								"OnError", ": " + properties.getProperty("OnError"));
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
		summaryReport.updateResultSummary(testParameters, testReportName,
													executionTime, testStatus);
	}
	
	/**
	 * Function to do the required wrap-up activities after completing the test batch execution
	 * @param testExecutedInUnitTestFramework Boolean variable indicating whether the test is executed in JUnit/TestNG
	 */
	public void wrapUp(Boolean testExecutedInUnitTestFramework) {
		overallEndTime = Util.getCurrentTime();
		String totalExecutionTime =
				Util.getTimeDifference(overallStartTime, overallEndTime);
		summaryReport.addResultSummaryFooter(totalExecutionTime);
		
		if(testExecutedInUnitTestFramework && System.getProperty("ReportPath") == null) {
			File testNgResultSrc = new File(frameworkParameters.getBasePath() +
											Util.getFileSeparator() +
											properties.getProperty("TestNgReportPath") +
											Util.getFileSeparator() +
											frameworkParameters.getRunConfiguration());		
			File testNgResultCssFile = new File(frameworkParameters.getBasePath() +
											Util.getFileSeparator() +
											properties.getProperty("TestNgReportPath") +
											Util.getFileSeparator() +
											"testng.css");
			File testNgResultDest =
								summaryReport.createResultsSubFolder("TestNG Results");
			
			try {
				FileUtils.copyDirectoryToDirectory(testNgResultSrc, testNgResultDest);
				FileUtils.copyFileToDirectory(testNgResultCssFile, testNgResultDest);
			} catch (IOException e) {
				logger.error("Error occurred while copying TestNG reports to the Results folder", e);
			}
		}
		
		summaryReport.copyLogFile();
	}
	
	/**
	 * Function to launch the summary report at the end of the test batch execution
	 */
	public void launchResultSummary() {
		if (reportSettings.shouldGenerateHtmlReports()) {
			try {
				Runtime.getRuntime().exec("RunDLL32.EXE shell32.dll,ShellExec_RunDLL " +
												reportPath + "\\HTML Results\\Summary.Html");
			} catch (IOException e) {
				logger.error("Error occurred while launching the result summary", e);
			}
		} else if (reportSettings.shouldGenerateExcelReports()) {
			try {
				Runtime.getRuntime().exec("RunDLL32.EXE shell32.dll,ShellExec_RunDLL " +
												reportPath + "\\Excel Results\\Summary.xls");
			} catch (IOException e) {
				logger.error("Error occurred while launching the result summary", e);
			}
		}
	}
}