package com.autopia4j.framework.webdriver.impl.modular.basic;

import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.webdriver.core.ExecutionMode;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.TestHarness;
import com.autopia4j.framework.webdriver.core.TestScript;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;

/**
 * Abstract base class for test scripts developed using the autopia4j simple implementation
 * @author vj
 */
public abstract class ModularBasicTestScript extends TestScript {
	
	/**
	 * The {@link FrameworkParameters} object
	 */
	protected FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
	
	/**
	 * The {@link ThreadLocal} instance of the {@link TestHarness} for the current test method
	 */
	protected ThreadLocal<TestHarness> currentTestHarness = new ThreadLocal<>();
	/**
	 * The {@link ThreadLocal} instance of the {@link WebDriverTestParameters} for the current test method
	 */
	protected ThreadLocal<WebDriverTestParameters> currentTestParameters = new ThreadLocal<>();
	/**
	 * The {@link ThreadLocal} instance of the {@link ScriptHelper} object for the current test method
	 */
	protected ThreadLocal<ScriptHelper> currentScriptHelper = new ThreadLocal<>();
	/**
	 * The {@link ThreadLocal} instance of the {@link WebDriverReport} object for the current test method
	 */
	protected ThreadLocal<WebDriverReport> currentReport = new ThreadLocal<>();
	
	
	/**
	 * Function to do the required framework setup activities before executing each test case
	 * @param currentMethod The current test {@link Method} being executed
	 * @param currentMethodParams The array of parameters being passed into the current method
	 */
	@BeforeMethod
	public void setUpTestRunner(Method currentMethod, Object[] currentMethodParams) {
		if(frameworkParameters.getStopExecution()) {
			tearDownTestSuite();
			
			// Throwing TestNG SkipException within a configuration method causes all subsequent test methods to be skipped/aborted
			throw new SkipException("Test execution terminated by user! All subsequent tests aborted...");
		} else {
			String[] currentPackageSplit = this.getClass().getPackage().getName().split(".testscripts.");
			frameworkParameters.setBasePackageName(currentPackageSplit[0]);
			
			TestHarness testHarness = new TestHarness();
			currentTestHarness.set(testHarness);
			
			WebDriverTestParameters testParameters;
			if(currentMethodParams.length == 0) {
				String currentModule = this.getClass().getSimpleName();	//Alt: currentMethod.getDeclaringClass().getSimpleName()
				String currentTest = currentMethod.getName();
				currentTest = currentTest.substring(0, 1).toUpperCase().concat(currentTest.substring(1));
				testParameters = testHarness.initializeTestParameters(currentModule, currentTest);
				testParameters.setCurrentTestDescription(currentTest);
			} else {
				testParameters = (WebDriverTestParameters) currentMethodParams[0];
				testHarness.setDefaultTestParameters(testParameters);
			}
			currentTestParameters.set(testParameters);
			
			WebDriver driver = testHarness.initializeWebDriver(testParameters);
			
			WebDriverReport report = testHarness.initializeTestReport(testParameters, driver);
			currentReport.set(report);
			
			ScriptHelper scriptHelper = new ScriptHelper(testParameters, null, report, driver);
			currentScriptHelper.set(scriptHelper);
		}
	}
	
	/**
	 * Function to do the required framework tear-down activities after executing each test case
	 */
	@AfterMethod(alwaysRun=true)
	public synchronized void tearDownTestRunner() {
		TestHarness testHarness = currentTestHarness.get();
		ScriptHelper scriptHelper = currentScriptHelper.get();
		
		WebDriverTestParameters testParameters = scriptHelper.getTestParameters();
		WebDriver driver = scriptHelper.getDriver();
		WebDriverReport report = scriptHelper.getReport();
		
		if (testParameters.getExecutionMode() == ExecutionMode.PERFECTO_DEVICE) {
			testHarness.downloadPerfectoResults(driver, report);
		}
		testHarness.quitWebDriver(driver);
		String executionTime = testHarness.tearDown(scriptHelper);
		String testReportName = report.getReportSettings().getReportName();
		String testStatus = report.getTestStatus();
		
		testHarness.closeTestReport(scriptHelper, executionTime);
		testBatchHarness.updateResultSummary(testParameters, testReportName,
														executionTime, testStatus);
	}
}