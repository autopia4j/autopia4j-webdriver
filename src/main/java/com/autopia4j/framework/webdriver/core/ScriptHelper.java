package com.autopia4j.framework.webdriver.core;

import org.openqa.selenium.WebDriver;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.datatable.BaseDatatable;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;


/**
 * Wrapper class for common framework objects, to be used across the entire test case and dependent libraries
 * @author vj
 */
public class ScriptHelper {
	private final WebDriverTestParameters testParameters;
	private final BaseDatatable dataTable;
	private final WebDriverReport report;
	private final WebDriver driver;
	private final WebDriverUtil driverUtil;
	private final GalenUtil galenUtil;
	
	
	/**
	 * Constructor to initialize all the objects wrapped by the {@link ScriptHelper} class
	 * @param testParameters The {@link WebDriverTestParameters} object
	 * @param dataTable The {@link BaseDatatable} object
	 * @param report The {@link WebDriverReport} object
	 * @param driver The {@link WebDriver} object
	 */
	public ScriptHelper(WebDriverTestParameters testParameters, BaseDatatable dataTable,
						WebDriverReport report, WebDriver driver) {
		this.testParameters = testParameters;
		this.dataTable = dataTable;
		this.report = report;
		this.driver = driver;
		
		FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
		long objectSyncTimeout = frameworkParameters.getObjectSyncTimeout();
		long pageLoadTimeout = frameworkParameters.getPageLoadTimeout();
		
		driverUtil = new WebDriverUtil(driver, objectSyncTimeout, pageLoadTimeout);
		galenUtil = new GalenUtil(driver, report);
	}
	
	/**
	 * Function to get the {@link WebDriverTestParameters} object
	 * @return The {@link WebDriverTestParameters} object
	 */
	public WebDriverTestParameters getTestParameters() {
		return testParameters;
	}
	
	/**
	 * Function to get the {@link BaseDatatable} object
	 * @return The {@link BaseDatatable} object
	 */
	public BaseDatatable getDataTable() {
		return dataTable;
	}
	
	/**
	 * Function to get the {@link WebDriverReport} object
	 * @return The {@link WebDriverReport} object
	 */
	public WebDriverReport getReport() {
		return report;
	}
	
	/**
	 * Function to get the {@link WebDriver} object
	 * @return The {@link WebDriver} object
	 */
	public WebDriver getDriver() {
		return driver;
	}
	
	/**
	 * Function to get the {@link WebDriverUtil} object
	 * @return The {@link WebDriverUtil} object
	 */
	public WebDriverUtil getDriverUtil() {
		return driverUtil;
	}
	
	/**
	 * Function to get the {@link GalenUtil} object
	 * @return The {@link GalenUtil} object
	 */
	public GalenUtil getGalenUtil() {
		return galenUtil;
	}
}