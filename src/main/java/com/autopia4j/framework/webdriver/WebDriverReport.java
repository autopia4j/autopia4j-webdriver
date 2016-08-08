package com.autopia4j.framework.webdriver;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.autopia4j.framework.reporting.Report;
import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.ReportTheme;
import com.autopia4j.framework.utils.FrameworkException;


/**
 * Class which extends the {@link Report} class with a Selenium specific override for taking screenshots
 * @author vj
 */
public class WebDriverReport extends Report {
	private WebDriver driver;
	//private IMobileDevice device;
	
	
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
			throw new FrameworkException("Report.driver is not initialized!");
		}
		
		if ("HtmlUnitDriver".equals(driver.getClass().getSimpleName()) || 
						"class org.openqa.selenium.htmlunit.HtmlUnitDriver"
						.equals(driver.getClass().getGenericSuperclass().toString())) {
			return;	// Screenshots not supported in headless mode
		}
		
		File scrFile;
		if ("RemoteWebDriver".equals(driver.getClass().getSimpleName())) {
			Capabilities capabilities = ((RemoteWebDriver) driver).getCapabilities();
			if ("htmlunit".equals(capabilities.getBrowserName())) {
				return;	// Screenshots not supported in headless mode
			}
			WebDriver augmentedDriver = new Augmenter().augment(driver);
	        scrFile = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
		} else {
			scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		}
		
		try {
			FileUtils.copyFile(scrFile, new File(screenshotPath), true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new FrameworkException("Error while writing screenshot to file");
		}
	}
}