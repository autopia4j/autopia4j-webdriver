package com.autopia4j.framework.webdriver.impl.modular;

import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.datatable.impl.ModularDatatable;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.DeviceType;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.TestBatchHarness;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;


/**
 * Abstract base class for all the test cases to be automated
 * @author vj
 */
public abstract class ModularTestScript {
	
	/**
	 * The {@link DeviceType} object (passed from the Driver script)
	 */
	protected DeviceType deviceType;
	
	/**
	 * The {@link ModularDatatable} object (passed from the Driver script)
	 */
	protected ModularDatatable dataTable;
	/**
	 * The {@link WebDriverReport} object (passed from the Driver script)
	 */
	protected WebDriverReport report;
	/**
	 * The {@link WebDriver} object (passed from the Driver script)
	 */
	protected WebDriver driver;
	/**
	 * The {@link WebDriverUtil} object (passed from the Driver script)
	 */
	protected WebDriverUtil driverUtil;
	/**
	 * The {@link GalenUtil} object (passed from the Driver script)
	 */
	protected GalenUtil galenUtil;
	
	/**
	 * The {@link ScriptHelper} object (required for calling one reusable library from another)
	 */
	protected ScriptHelper scriptHelper;
	
	/**
	 * The {@link FrameworkParameters} object
	 */
	protected FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
	
	/**
	 * The {@link Properties} object with settings loaded from the framework properties file
	 */
	protected Properties properties;
	
	/**
	 * Object synchronization timeout
	 */
	protected long objectSyncTimeout;
	/**
	 * Page load timeout
	 */
	protected long pageLoadTimeout;
	
	/**
	 * The current module (auto initialized during the test runner setup)
	 */
	protected String currentModule;
	/**
	 * The current test script (auto initialized during the test runner setup)
	 */
	protected String currentTest;
	
	private TestBatchHarness testBatchHarness;
	private ThreadLocal<ModularDriverScript> currentDriverScript = new ThreadLocal<>();
	
	
	/**
	 * Function to initialize the various objects that may need to be used with a test script <br>
	 * This is essentially a constructor substitute (designed this way since TestNG classes can only have default constructors)
	 * @param scriptHelper The {@link ScriptHelper} object
	 */
	public void initialize(ScriptHelper scriptHelper) {
		this.scriptHelper = scriptHelper;
		
		deviceType = scriptHelper.getDeviceType();
		dataTable = (ModularDatatable) scriptHelper.getDataTable();
		report = scriptHelper.getReport();
		driver = scriptHelper.getDriver();
		driverUtil = scriptHelper.getDriverUtil();
		galenUtil = scriptHelper.getGalenUtil();
		objectSyncTimeout = frameworkParameters.getObjectSyncTimeout();
		pageLoadTimeout = frameworkParameters.getPageLoadTimeout();
	}
	
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
		properties = Settings.getInstance();
		
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
		if(frameworkParameters.getStopExecution()) {
			tearDownTestSuite();
			
			// Throwing TestNG SkipException within a configuration method causes all subsequent test methods to be skipped/aborted
			throw new SkipException("Aborting all subsequent tests!");
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
	
	/**
	 * Function to do the required framework tear-down activities after executing the overall test suite
	 */
	@AfterSuite(alwaysRun=true)
	public void tearDownTestSuite() {
		testBatchHarness.wrapUp(true);
	}
}