package com.autopia4j.framework.webdriver.reporting;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.reporting.Report;
import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.ReportTheme;
import com.autopia4j.framework.webdriver.utils.WebDriverUtil;


/**
 * Class which extends the {@link Report} class with a Selenium specific override for taking screenshots
 * @author vj
 */
public class WebDriverReport extends Report {
	private final Logger logger = LoggerFactory.getLogger(WebDriverReport.class);
	private WebDriver driver;
	
	/**
	 * Constructor to initialize the Report object
	 * @param reportSettings The {@link ReportSettings} object
	 * @param reportTheme The {@link ReportTheme} object
	 */
	public WebDriverReport(ReportSettings reportSettings, ReportTheme reportTheme) {
		super(reportSettings, reportTheme);
	}
	
	/**
	 * Function to set the {@link WebDriver} object
	 * @param driver The {@link WebDriver} object
	 */
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	@Override
	protected void takeScreenshot(String screenshotPath) {
		if (driver == null) {
			throw new AutopiaException("The driver object is not initialized!");
		}
		WebDriverUtil driverUtil = new WebDriverUtil(driver, 0, 0);
		File scrFile = driverUtil.captureScreenshotAsFile();
		
		try {
			FileUtils.copyFile(scrFile, new File(screenshotPath), true);
		} catch (IOException e) {
			String errorDescription = "Error while writing screenshot to file";
			logger.error(errorDescription, e);
			throw new AutopiaException(errorDescription);
		}
	}
}