package com.autopia4j.framework.webdriver.keyword;

import java.util.Properties;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.*;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;


/**
 * Abstract base class for all the test cases to be automated
 * @author vj
 */
public abstract class TestCase {
	/**
	 * The current scenario
	 */
	protected String currentModule;
	/**
	 * The current test case
	 */
	protected String currentTestcase;
	
	private ResultSummaryManager resultSummaryManager =
										ResultSummaryManager.getInstance();
	
	
	/**
	 * Function to do the required framework setup activities before executing the overall test suite
	 * @param testContext The TestNG {@link ITestContext} of the current test suite 
	 */
	@BeforeSuite
	public void setUpTestSuite(ITestContext testContext) {
		resultSummaryManager.setRelativePath();
		
		String runConfiguration = getRunConfiguration(testContext);
		String executionEnvironment = getExecutionEnvironment();
		resultSummaryManager.initializeTestBatch(runConfiguration,
													executionEnvironment);
		
		int nThreads;
		if ("false".equalsIgnoreCase(testContext.getSuite().getParallel())) {
			nThreads = 1;
		} else {
			nThreads = testContext.getCurrentXmlTest().getThreadCount();
		}
		
		// Note: Separate threads may be spawned through usage of DataProvider
		// testContext.getSuite().getXmlSuite().getDataProviderThreadCount();
		// This will be at test case level (multiple instances on same test case in parallel)
		// This level of threading will not be reflected in the summary report
		
		resultSummaryManager.initializeSummaryReport(nThreads);
		resultSummaryManager.setupErrorLog();
	}
	
	private String getRunConfiguration(ITestContext testContext) {
		if (System.getProperty("RunConfiguration") != null) {
			return System.getProperty("RunConfiguration");
		} else {
			return testContext.getSuite().getName();
		}
	}
	
	private String getExecutionEnvironment() {
		if (System.getProperty("ExecutionEnvironment") != null) {
			return System.getProperty("ExecutionEnvironment");
		} else {
			Properties properties = Settings.getInstance();
			return properties.getProperty("ExecutionEnvironment");
		}
	}
	
	/**
	 * Function to do the required framework setup activities before executing each test case
	 */
	@BeforeMethod
	public void setUpTestRunner() {
		FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
		if(frameworkParameters.getStopExecution()) {
			tearDownTestSuite();
			
			// Throwing TestNG SkipException within a configuration method causes all subsequent test methods to be skipped/aborted
			throw new SkipException("Test execution terminated by user! All subsequent tests aborted...");
		} else {
			String[] currentPackageSplit = this.getClass().getPackage().getName().split(".testscripts.");
			
			frameworkParameters.setBasePackageName(currentPackageSplit[0]);
			currentModule = Util.capitalizeFirstLetter(currentPackageSplit[1]);
			currentTestcase = this.getClass().getSimpleName();
		}
	}
	
	/**
	 * Function to do the required framework teardown activities after executing each test case
	 * @param testParameters The {@link WebDriverTestParameters} object passed from the test case
	 * @param driverScript The {@link KeywordDriverScript} object passed from the test case
	 */
	protected synchronized void tearDownTestRunner(WebDriverTestParameters testParameters,
													KeywordDriverScript driverScript) {
		String testReportName = driverScript.getReportName();
		String executionTime = driverScript.getExecutionTime();
		String testStatus = driverScript.getTestStatus();
		
		resultSummaryManager.updateResultSummary(testParameters, testReportName,
														executionTime, testStatus);
		
		if("Failed".equalsIgnoreCase(testStatus)) {
			Assert.fail(driverScript.getFailureDescription());
		}
	}
	
	/**
	 * Function to do the required framework teardown activities after executing the overall test suite
	 */
	@AfterSuite
	public void tearDownTestSuite() {
		resultSummaryManager.wrapUp(true);
		//resultSummaryManager.launchResultSummary();
	}
}