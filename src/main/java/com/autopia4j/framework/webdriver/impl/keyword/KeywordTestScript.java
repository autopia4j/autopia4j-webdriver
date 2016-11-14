package com.autopia4j.framework.webdriver.impl.keyword;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.TestBatchHarness;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;


/**
 * Abstract base class for all the test cases to be automated
 * @author vj
 */
public abstract class KeywordTestScript {
	/**
	 * The current module (auto initialized during the test runner setup)
	 */
	protected String currentModule;
	/**
	 * The current test script (auto initialized during the test runner setup)
	 */
	protected String currentTest;
	
	private TestBatchHarness testBatchHarness;
	private ThreadLocal<KeywordDriverScript> currentDriverScript = new ThreadLocal<>();
	
	
	/**
	 * Function to do the required framework setup activities before executing the overall test suite
	 * @param testContext The TestNG {@link ITestContext} of the current test suite 
	 */
	@BeforeSuite
	public void setUpTestSuite(ITestContext testContext) {
		testBatchHarness = TestBatchHarness.getInstance();
		
		if (System.getProperty("autopia.run.configuration") == null) {
			System.setProperty("autopia.run.configuration", testContext.getSuite().getName());
		}
		testBatchHarness.initialize();
		
		int nThreads;
		if ("false".equalsIgnoreCase(testContext.getSuite().getParallel())) {
			nThreads = 1;
		} else {
			nThreads = testContext.getCurrentXmlTest().getThreadCount();
		}
		
		// Note: Separate threads may be spawned through usage of DataProvider
		// testContext.getSuite().getXmlSuite().getDataProviderThreadCount() will be at test case level (multiple instances on same test case in parallel)
		// This level of threading will not be reflected in the summary report
		
		testBatchHarness.initializeSummaryReport(nThreads);
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
			currentTest = this.getClass().getSimpleName();
		}
	}
	
	/**
	 * {@link Assert} that the test execution passed
	 * @param driverScript The {@link KeywordDriverScript} object
	 */
	protected void assertTestPassed(KeywordDriverScript driverScript) {
		currentDriverScript.set(driverScript);
		if("Failed".equalsIgnoreCase(driverScript.getTestStatus())) {
			Assert.fail(driverScript.getFailureDescription());
		}
	}
	
	/**
	 * Function to do the required framework teardown activities after executing each test case
	 */
	@AfterMethod(alwaysRun=true)
	protected synchronized void tearDownTestRunner() {
		KeywordDriverScript driverScript = currentDriverScript.get();
		WebDriverTestParameters testParameters = driverScript.getTestParameters();
		String testReportName = driverScript.getReportName();
		String executionTime = driverScript.getExecutionTime();
		String testStatus = driverScript.getTestStatus();
		
		testBatchHarness.updateResultSummary(testParameters, testReportName,
														executionTime, testStatus);
	}
	
	/**
	 * Function to do the required framework tear-down activities after executing the overall test suite
	 */
	@AfterSuite(alwaysRun=true)
	public void tearDownTestSuite() {
		testBatchHarness.wrapUp(true);
	}
}