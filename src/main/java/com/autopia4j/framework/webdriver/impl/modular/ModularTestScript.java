package com.autopia4j.framework.webdriver.impl.modular;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.datatable.impl.ModularDatatable;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.TestScript;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;


/**
 * Abstract base class for all the test cases to be automated
 * @author vj
 */
public abstract class ModularTestScript extends TestScript {
	/**
	 * The {@link ScriptHelper} object (required for calling one reusable library from another)
	 */
	protected ScriptHelper scriptHelper;
	/**
	 * The {@link ModularDatatable} object (passed from the Driver script)
	 */
	protected ModularDatatable dataTable;
	/**
	 * The {@link WebDriverReport} object (passed from the Driver script)
	 */
	protected WebDriverReport report;
	
	/**
	 * The name of the current module
	 */
	protected String currentModule;
	/**
	 * The name of the current test script
	 */
	protected String currentTest;
	
	private ThreadLocal<ModularDriverScript> currentDriverScript = new ThreadLocal<>();
	
	
	/**
	 * Function to initialize the various objects that may need to be used with a test script <br>
	 * This is essentially a constructor substitute (designed this way since TestNG classes can only have default constructors)
	 * @param scriptHelper The {@link ScriptHelper} object
	 */
	public void initialize(ScriptHelper scriptHelper) {
		this.scriptHelper = scriptHelper;
		
		this.dataTable = (ModularDatatable) scriptHelper.getDataTable();
		this.report = scriptHelper.getReport();
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
	 * Function to handle any pre-requisite steps required before beginning the test case execution <br>
	 * <u>Note</u>: This function can be left blank if not applicable
	 */
	public abstract void setUp();
	
	/**
	 * Function to handle the core test steps required as part of the test case
	 */
	public abstract void executeTest();
	
	/**
	 * Function to handle any clean-up steps required after completing the test case execution <br>
	 * <u>Note</u>: This function can be left blank if not applicable
	 */
	public abstract void tearDown();
	
	/**
	 * {@link Assert} that the test execution passed
	 * @param driverScript The {@link ModularDriverScript} object
	 */
	protected void assertTestPassed(ModularDriverScript driverScript) {
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
		ModularDriverScript driverScript = currentDriverScript.get();
		WebDriverTestParameters testParameters = driverScript.getTestParameters();
		String testReportName = driverScript.getReportName();
		String executionTime = driverScript.getExecutionTime();
		String testStatus = driverScript.getTestStatus();
		
		testBatchHarness.updateResultSummary(testParameters, testReportName,
														executionTime, testStatus);
	}
}