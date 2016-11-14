package com.autopia4j.framework.webdriver.impl.cucumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.webdriver.core.TestBatchHarness;
import com.autopia4j.framework.webdriver.core.TestHarness;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class CukeHooks extends MasterStepDefs {
	
	Logger logger = LoggerFactory.getLogger(CukeHooks.class);
	private TestHarness testHarness;
	
	private static Boolean runningFirstScenario = true;
	
	@Before
	public void setUp(Scenario scenario) {
		currentScenario = scenario;
		
		if(runningFirstScenario) {
			logger.info("Running global @Before hook...");
			TestBatchHarness testBatchHarness = TestBatchHarness.getInstance();
			testBatchHarness.initialize();
			runningFirstScenario = false;
		}
		
		logger.info("Running scenario @Before hook...");
		
		testHarness = new TestHarness();
		FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
		
		testParameters = testHarness.initializeTestParameters("Feature", scenario.getName());
		properties = Settings.getInstance();
		driver = testHarness.initializeWebDriver(testParameters);
		driverUtil = new WebDriverUtil(driver, frameworkParameters.getObjectSyncTimeout(),
												frameworkParameters.getPageLoadTimeout());
	}
	
	@After
	public void tearDown() {
		logger.info("Running scenario @After hook...");
		
		testHarness.quitWebDriver(driver);
	}
}