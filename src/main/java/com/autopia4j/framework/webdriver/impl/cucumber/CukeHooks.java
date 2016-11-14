package com.autopia4j.framework.webdriver.impl.cucumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.webdriver.core.TestHarness;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class CukeHooks extends MasterStepDefs {
	
	Logger logger = LoggerFactory.getLogger(CukeHooks.class);
	private TestHarness testHarness;
	
	@Before
	public void setUp(Scenario scenario) {
		currentScenario = scenario;
		
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
		testHarness.quitWebDriver(driver);
	}
}