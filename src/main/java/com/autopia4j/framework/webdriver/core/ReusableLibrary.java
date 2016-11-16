package com.autopia4j.framework.webdriver.core;

import java.util.Properties;
import org.openqa.selenium.WebDriver;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.datatable.DataTableType;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;
import com.autopia4j.framework.webdriver.utils.GalenUtil;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;


/**
 * Abstract base class for reusable libraries created by the user
 * @author vj
 */
public abstract class ReusableLibrary {
	/**
	 * The {@link ScriptHelper} object (required for calling one reusable library from another)
	 */
	protected final ScriptHelper scriptHelper;
	
	/**
	 * The {@link WebDriverTestParameters} object (passed from the test script)
	 */
	protected final WebDriverTestParameters testParameters;
	/**
	 * The {@link DataTableType} object (passed from the test script)
	 */
	protected final DataTableType dataTable;
	/**
	 * The {@link WebDriverReport} object (passed from the test script)
	 */
	protected final WebDriverReport report;
	/**
	 * The {@link WebDriver} object (passed from the test script)
	 */
	protected final WebDriver driver;
	/**
	 * The {@link WebDriverUtil} object (passed from the test script)
	 */
	protected final WebDriverUtil driverUtil;
	/**
	 * The {@link GalenUtil} object (passed from the test script)
	 */
	protected final GalenUtil galenUtil;
	
	/**
	 * The {@link FrameworkParameters} object
	 */
	protected final FrameworkParameters frameworkParameters;
	/**
	 * The {@link Properties} object with settings loaded from the framework properties file
	 */
	protected final Properties properties;
	
	
	/**
	 * Constructor to initialize the {@link ScriptHelper} object and in turn the objects wrapped by it
	 * @param scriptHelper The {@link ScriptHelper} object
	 */
	public ReusableLibrary(ScriptHelper scriptHelper) {
		this.scriptHelper = scriptHelper;
		
		testParameters = scriptHelper.getTestParameters();
		dataTable = scriptHelper.getDataTable();
		report = scriptHelper.getReport();
		driver = scriptHelper.getDriver();
		driverUtil = scriptHelper.getDriverUtil();
		galenUtil = scriptHelper.getGalenUtil();
		
		properties = Settings.getInstance();
		frameworkParameters = FrameworkParameters.getInstance();
	}
}