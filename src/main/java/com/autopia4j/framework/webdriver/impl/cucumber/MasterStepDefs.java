package com.autopia4j.framework.webdriver.impl.cucumber;

import java.util.Properties;

import org.openqa.selenium.WebDriver;

import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;

import cucumber.api.Scenario;

public abstract class MasterStepDefs {
	protected static Scenario currentScenario;
	protected static WebDriverTestParameters testParameters;
	
	protected static WebDriver driver;
	protected static WebDriverUtil driverUtil;
	protected static Properties properties;
}