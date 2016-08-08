package com.autopia4j.framework.webdriver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import com.autopia4j.framework.reporting.ReportSettings;
import com.autopia4j.framework.reporting.Status;
import com.autopia4j.framework.utils.Util;
import com.galenframework.api.Galen;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import com.galenframework.reports.TestReport;
import com.galenframework.reports.model.FileTempStorage;
import com.galenframework.reports.model.LayoutReport;

/**
 * Class containing useful WebDriver utility functions
 * @author vj
 */
public class GalenUtil {
	private WebDriver driver;
	private WebDriverReport report;
	private ReportSettings reportSettings;
	private List<GalenTestInfo> galenTests;
	
	
	/**
	 * Constructor to initialize the {@link GalenUtil} object
	 * @param driver The {@link WebDriver} object
	 * @param report The {@link WebDriverReport} object
	 * @param reportSettings The {@link ReportSettings} object
	 */
	public GalenUtil(WebDriver driver, WebDriverReport report, ReportSettings reportSettings) {
		this.driver = driver;
		this.report = report;
		this.reportSettings = reportSettings;
		
		galenTests = new LinkedList<GalenTestInfo>();
	}
	
	/**
	 * Function to validate the layout of the current page against the Galen specs specified
	 * @param pageName The name of the page whose layout is to be validated
	 * @param specFilePath The path of the Galen spec file against which the page is to be validated
	 * @param includedTag The Galen tag to be included as part of the spec validation
	 */
	public void checkPageLayout(String pageName, String specFilePath, String includedTag) {
		String reportTitle = "Validate specs '" + specFilePath + "' against " +
								pageName + " on " + includedTag;
		
		try {
			GalenTestInfo galenTestInfo = GalenTestInfo.fromString(pageName);
			galenTests.add(galenTestInfo);
			
			LayoutReport layoutReport =
						Galen.checkLayout(driver, specFilePath, Arrays.asList(includedTag));
			TestReport testReport = galenTestInfo.getReport();
			testReport.layout(layoutReport, reportTitle);
			
			if (reportSettings.shouldLinkScreenshotsToTestLog()) {
				reportTitle += ". Refer " + "<a href='..\\Galen Reports\\" +
								reportSettings.getReportName() + "\\report.html' "
								+ "target='about_blank'>Galen reports</a> for more details.";
			} else {
				reportTitle += ". Refer Galen reports for more details.";
			}
			
			if(layoutReport.errors() > 0) {
				report.updateTestLog("Galen Test", reportTitle, Status.FAIL);
			} else {
				report.updateTestLog("Galen Test", reportTitle, Status.PASS);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.updateTestLog("Galen Test", reportTitle + ". An error occured!", Status.FAIL);
		}
	}
	
	/**
	 * Function to export the Galen reports generated during the test execution 
	 */
	public void exportGalenReports() {
		if (!galenTests.isEmpty()) {
			File galenReportsFolder = report.createResultsSubFolder("Galen Reports" +
														Util.getFileSeparator() +
														reportSettings.getReportName());
			HtmlReportBuilder htmlReportBuilder = new HtmlReportBuilder();
			
			try {
				htmlReportBuilder.build(galenTests, galenReportsFolder.getAbsolutePath());
				cleanGalenReportsData();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void cleanGalenReportsData() {
		for (GalenTestInfo galenTestInfo: galenTests) {
			if (galenTestInfo.getReport() != null) {
				FileTempStorage tempStorage = galenTestInfo.getReport().getFileStorage();
				if (tempStorage != null) {
					tempStorage.cleanup();
				}
			}
		}
	}
}