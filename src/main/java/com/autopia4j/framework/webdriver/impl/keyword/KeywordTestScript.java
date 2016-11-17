package com.autopia4j.framework.webdriver.impl.keyword;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.TestScript;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/**
 * Abstract base class for test scripts developed using the autopia4j keyword-driven implementation
 * @author vj
 */
public abstract class KeywordTestScript extends TestScript {
	/**
	 * The name of the current module
	 */
	protected String currentModule;
	/**
	 * The name of the current test script
	 */
	protected String currentTest;
	
	private ThreadLocal<KeywordDriverScript> currentDriverScript = new ThreadLocal<>();
	
	
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
	 * Function to do the required framework tear-down activities after executing each test case
	 */
	@AfterMethod(alwaysRun=true)
	public synchronized void tearDownTestRunner() {
		KeywordDriverScript driverScript = currentDriverScript.get();
		WebDriverTestParameters testParameters = driverScript.getTestParameters();
		String testReportName = driverScript.getReportName();
		String executionTime = driverScript.getExecutionTime();
		String testStatus = driverScript.getTestStatus();
		
		testBatchHarness.updateResultSummary(testParameters, testReportName,
														executionTime, testStatus);
	}
}