package com.autopia4j.framework.webdriver.mobile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.utils.FrameworkException;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;

/**
 * Utility class for Perfecto Mobile
 * @author vj
 */
public class PerfectoWebDriverUtil {
	private final Logger logger = LoggerFactory.getLogger(PerfectoWebDriverUtil.class);
	private final WebDriver driver;
	private final WebDriverReport report;
	private final ReportSettings reportSettings;
	
	private Boolean exportPerfectoHtmlReport;
	private Boolean exportPerfectoPdfReport;
	private Boolean exportPerfectoTestVideo;
	
	
	/**
	 * Constructor to initialize the {@link PerfectoWebDriverUtil} object
	 * @param driver The {@link WebDriver} object
	 * @param report The {@link WebDriverReport} object
	 */
	public PerfectoWebDriverUtil(WebDriver driver, WebDriverReport report) {
		this.driver = driver;
		this.report = report;
		this.reportSettings = report.getReportSettings();
		exportPerfectoHtmlReport = true;
		exportPerfectoPdfReport = false;
		exportPerfectoTestVideo = false;
	}
	
	/**
	 * Function to set a Boolean variable indicating whether Perfecto's HTML report should be exported
	 * @param exportPerfectoHtmlReport Boolean variable indicating whether Perfecto's HTML report should be exported
	 */
	public void setExportPerfectoHtmlReport(Boolean exportPerfectoHtmlReport) {
		this.exportPerfectoHtmlReport = exportPerfectoHtmlReport;
	}
	/**
	 * Function to set a Boolean variable indicating whether Perfecto's PDF report should be exported
	 * @param exportPerfectoPdfReport Boolean variable indicating whether Perfecto's PDF report should be exported
	 */
	public void setExportPerfectoPdfReport(Boolean exportPerfectoPdfReport) {
		this.exportPerfectoPdfReport = exportPerfectoPdfReport;
	}
	/**
	 * Function to set a Boolean variable indicating whether Perfecto's test video recording should be exported
	 * @param exportPerfectoTestVideo Boolean variable indicating whether Perfecto's test video recording should be exported
	 */
	public void setExportPerfectoTestVideo(Boolean exportPerfectoTestVideo) {
		this.exportPerfectoTestVideo = exportPerfectoTestVideo;
	}
	
	/**
	 * Function to download the Perfecto test results into the test results folder
	 */
	public void downloadPerfectoResults() {
		File perfectoResultsFolder = null;
		if (exportPerfectoHtmlReport || exportPerfectoPdfReport || exportPerfectoTestVideo) {
			driver.close();
			perfectoResultsFolder = report.createResultsSubFolder("Perfecto Results");
		}
		
		if (exportPerfectoHtmlReport) {
			downloadPerfectoReport("html", perfectoResultsFolder.getAbsolutePath());
		}
		if (exportPerfectoPdfReport) {
			downloadPerfectoReport("pdf", perfectoResultsFolder.getAbsolutePath());
		}
		if (exportPerfectoTestVideo) {
			downloadPerfectoAttachment("video", "flv", perfectoResultsFolder.getAbsolutePath());
		}
	}
	
	/**
	 * Function to download the execution result from Perfecto
	 * @param reportType Specify any one format from "pdf", "html", "csv" or "xml"
	 * @param reportPath The path at which the report should be saved
	 */
	private void downloadPerfectoReport(String reportType, String reportPath) {
		String command = "mobile:report:download";
		Map<String, Object> params = new HashMap<>();
		params.put("type", reportType);
		String reportRawData = (String) ((RemoteWebDriver) driver).executeScript(command, params);
		
		try {
			File reportFile = new File(reportPath + Util.getFileSeparator() +
									reportSettings.getReportName() + "." + reportType);
			BufferedOutputStream outputStream =
							new BufferedOutputStream(new FileOutputStream(reportFile));
			byte[] reportBytes = OutputType.BYTES.convertFromBase64Png(reportRawData);
			outputStream.write(reportBytes);
			outputStream.close();
		} catch (IOException e) {
			String errorDescription = "Error occurred while downloading Perfecto report";
			logger.error(errorDescription, e);
			throw new FrameworkException(errorDescription);
		}
	}
	
	/**
	* Function to download all the report attachments of the specified type
	* @param attachmentType Specify one from "video", "image", "vital" or "network"
	* @param fileExtension The extension of the attachment files to be saved
	* @param reportPath The path at which the report should be saved
	*/
	private void downloadPerfectoAttachment(String attachmentType, String fileExtension, String reportPath) {
		String command = "mobile:report:attachment";
		int index = 0;
		String attachmentRawData;
		
		while(true) {
			Map<String, Object> params = new HashMap<>();
		    params.put("type", attachmentType);
		    params.put("index", Integer.toString(index));
		    attachmentRawData =
		    		(String) ((RemoteWebDriver) driver).executeScript(command, params);
		    
		    if (attachmentRawData == null) {
		    	break;
		    }
		    
		    try {
				File attachmentFile = new File(reportPath + Util.getFileSeparator() +
											reportSettings.getReportName() + "_" +
											attachmentType + index + "." + fileExtension);
				BufferedOutputStream outputStream =
						new BufferedOutputStream(new FileOutputStream(attachmentFile)); 
				byte[] bytes = OutputType.BYTES.convertFromBase64Png(attachmentRawData);
				outputStream.write(bytes);
				outputStream.close();
				index ++;
	    	} catch (IOException e) {
				String errorDescription = "Error occurred while downloading Perfecto attachment";
				logger.error(errorDescription, e);
				throw new FrameworkException(errorDescription);
			}
		}
	}
}