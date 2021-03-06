package com.autopia4j.framework.webdriver.core;

import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Abstract base class for test scripts developed using the autopia4j framework
 * @author vj
 */
public abstract class TestScript {
	
	/**
	 * The {@link TestBatchHarness} object managing the current test batch execution
	 */
	protected TestBatchHarness testBatchHarness = TestBatchHarness.getInstance();
	
	
	/**
	 * Function to do the required framework setup activities before executing the overall test suite
	 * @param testContext The TestNG {@link ITestContext} of the current test suite 
	 */
	@BeforeSuite
	public void setUpTestSuite(ITestContext testContext) {
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
	 * Function to do the required framework tear-down activities after executing the overall test suite
	 */
	@AfterSuite(alwaysRun=true)
	public void tearDownTestSuite() {
		testBatchHarness.wrapUp(true);
	}
}