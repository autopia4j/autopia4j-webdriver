package com.autopia4j.framework.webdriver.core;

import java.util.Properties;

import org.openqa.selenium.WebDriver;

import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.datatable.DataTableType;
import com.autopia4j.framework.webdriver.core.DeviceType;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;


/**
 * Wrapper class for common framework objects, to be used across the entire test case and dependent libraries
 * @author vj
 */
public class ScriptHelper {
	private final DeviceType deviceType;
	private final DataTableType dataTable;
	private final WebDriverReport report;
	private final WebDriver driver;
	private final WebDriverUtil driverUtil;
	private final GalenUtil galenUtil;
	private final long objectSyncTimeout;
	private final long pageLoadTimeout;
	
	
	/**
	 * Constructor to initialize all the objects wrapped by the {@link ScriptHelper} class
	 * @param testParameters The {@link WebDriverTestParameters} object
	 * @param dataTable The {@link DataTableType} object
	 * @param report The {@link WebDriverReport} object
	 * @param driver The {@link WebDriver} object
	 */
	public ScriptHelper(WebDriverTestParameters testParameters, DataTableType dataTable,
						WebDriverReport report, WebDriver driver) {
		this.deviceType = testParameters.getDeviceType();
		this.dataTable = dataTable;
		this.report = report;
		this.driver = driver;
		
		Properties properties = Settings.getInstance();
		objectSyncTimeout = Long.parseLong(properties.get("timeout.object.sync").toString());
		pageLoadTimeout = Long.parseLong(properties.get("timeout.page.load").toString());
		
		driverUtil = new WebDriverUtil(driver, objectSyncTimeout, pageLoadTimeout);
		galenUtil = new GalenUtil(driver, report);
	}
	
	/**
	 * Function to get the {@link DeviceType} on which the test is being executed
	 * @return The {@link DeviceType} on which the test is being executed
	 */
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	/**
	 * Function to get the {@link DataTableType} object
	 * @return The {@link DataTableType} object
	 */
	public DataTableType getDataTable() {
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
	
	/**
	 * Function to get the object synchronization timeout configured by the user
	 * @return The object synchronization timeout (in seconds)
	 */
	public long getObjectSyncTimeout() {
		return objectSyncTimeout;
	}
	
	/**
	 * Function to get the page load timeout configured by the user
	 * @return The page load timeout (in seconds)
	 */
	public long getPageLoadTimeout() {
		return pageLoadTimeout;
	}
}