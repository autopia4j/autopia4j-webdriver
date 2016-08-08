package com.autopia4j.framework.webdriver.testrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.Platform;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.IterationOptions;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.webdriver.core.Browser;
import com.autopia4j.framework.webdriver.core.DeviceType;
import com.autopia4j.framework.webdriver.core.ExecutionMode;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.reporting.ResultSummaryManager;


/**
 * Class to manage the batch execution of test scripts within the framework
 * @author vj
 */
public class Allocator {
	private FrameworkParameters frameworkParameters =
												FrameworkParameters.getInstance();
	private Properties properties;
	private ResultSummaryManager resultSummaryManager =
												ResultSummaryManager.getInstance();
	
	
	/**
	 * Function to drive the batch execution of test cases
	 * based on the specified Run Configuration within the Run Manager file
	 * @return Returns a value of 0 if the test batch passes and 1 if the test batch fails
	 */
	public int driveBatchExecution() {
		resultSummaryManager.setRelativePath();
		properties = Settings.getInstance();
		
		String runConfiguration = getRunConfiguration();
		String executionEnvironment = getExecutionEnvironment();
		resultSummaryManager.initializeTestBatch(runConfiguration,
													executionEnvironment);
		
		int nThreads = Integer.parseInt(properties.getProperty("NumberOfThreads"));
		resultSummaryManager.initializeSummaryReport(nThreads);
		
		resultSummaryManager.setupErrorLog();
		
		int testBatchStatus = executeTestBatch(nThreads);
		
		resultSummaryManager.wrapUp(false);
		
		if (System.getProperty("ReportPath") == null) {	// No Report path specified from outside
			resultSummaryManager.launchResultSummary();
		}
		
		return testBatchStatus;
	}
	
	private String getRunConfiguration() {
		if (System.getProperty("RunConfiguration") != null) {
			return System.getProperty("RunConfiguration");
		} else {
			return properties.getProperty("RunConfiguration");
		}
	}
	
	private String getExecutionEnvironment() {
		if (System.getProperty("ExecutionEnvironment") != null) {
			return System.getProperty("ExecutionEnvironment");
		} else {
			return properties.getProperty("ExecutionEnvironment");
		}
	}
	
	private int executeTestBatch(int nThreads) {
		List<WebDriverTestParameters> testInstancesToRun =
							getRunInfo(frameworkParameters.getRunConfiguration());
		ExecutorService parallelExecutor = Executors.newFixedThreadPool(nThreads);
		ParallelRunner testRunner = null;
		
		for (int currentTestInstance = 0; currentTestInstance < testInstancesToRun.size() ; currentTestInstance++ ) {
			testRunner = new ParallelRunner(testInstancesToRun.get(currentTestInstance));
			parallelExecutor.execute(testRunner);
			
			if(frameworkParameters.getStopExecution()) {
				break;
			}
		}
		
		parallelExecutor.shutdown();
		while(!parallelExecutor.isTerminated()) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (testRunner == null) {
			return 0;	// All tests flagged as "No" in the Run Manager
		} else {
			return testRunner.getTestBatchStatus();
		}
	}
	
	private List<WebDriverTestParameters> getRunInfo(String sheetName) {
		ExcelDataAccess runManagerAccess =
					new ExcelDataAccess(frameworkParameters.getBasePath(), "test.run.configurations");			
		runManagerAccess.setDatasheetName(sheetName);
		
		int nTestInstances = runManagerAccess.getLastRowNum();
		List<WebDriverTestParameters> testInstancesToRun = new ArrayList<WebDriverTestParameters>();
		
		for (int currentTestInstance = 1; currentTestInstance <= nTestInstances; currentTestInstance++) {
			String executeFlag = runManagerAccess.getValue(currentTestInstance, "Execute");
			
			if ("Yes".equalsIgnoreCase(executeFlag)) {
				String currentScenario = runManagerAccess.getValue(currentTestInstance, "TestScenario");
				String currentTestcase = runManagerAccess.getValue(currentTestInstance, "TestCase");
				WebDriverTestParameters testParameters =
						new WebDriverTestParameters(currentScenario, currentTestcase);
				
				testParameters.setCurrentTestInstance("Instance" + runManagerAccess.getValue(currentTestInstance, "TestInstance"));
				testParameters.setCurrentTestDescription(runManagerAccess.getValue(currentTestInstance, "Description"));
				
				String iterationMode = runManagerAccess.getValue(currentTestInstance, "IterationMode");
				if (!"".equals(iterationMode)) {
					testParameters.setIterationMode(IterationOptions.valueOf(iterationMode));
				} else {
					testParameters.setIterationMode(IterationOptions.RUN_ALL_ITERATIONS);
				}
				
				String startIteration = runManagerAccess.getValue(currentTestInstance, "StartIteration");
				if (!"".equals(startIteration)) {
					testParameters.setStartIteration(Integer.parseInt(startIteration));
				}
				String endIteration = runManagerAccess.getValue(currentTestInstance, "EndIteration");
				if (!"".equals(endIteration)) {
					testParameters.setEndIteration(Integer.parseInt(endIteration));
				}
				
				String executionMode = runManagerAccess.getValue(currentTestInstance, "ExecutionMode");
				if (!"".equals(executionMode)) {
					testParameters.setExecutionMode(ExecutionMode.valueOf(executionMode));
				} else {
					testParameters.setExecutionMode(ExecutionMode.valueOf(properties.getProperty("DefaultExecutionMode")));
				}
				
				String remoteUrl = runManagerAccess.getValue(currentTestInstance, "RemoteUrl");
				if (!"".equals(remoteUrl) && !"N/A".equals(remoteUrl)) {
					testParameters.setRemoteUrl(remoteUrl);
				} else {
					testParameters.setRemoteUrl(properties.getProperty("DefaultRemoteUrl"));
				}
				
				String deviceType = runManagerAccess.getValue(currentTestInstance, "DeviceType");
				if (!"".equals(deviceType)) {
					testParameters.setDeviceType(DeviceType.valueOf(deviceType));
				} else {
					testParameters.setDeviceType(DeviceType.valueOf(properties.getProperty("DefaultDeviceType")));
				}
				
				String deviceName = runManagerAccess.getValue(currentTestInstance, "DeviceName");
				if (!"".equals(deviceName) && !"N/A".equals(deviceName)) {
					testParameters.setDeviceName(deviceName);
				} else {
					testParameters.setDeviceName(properties.getProperty("DefaultDeviceName"));
				}
				
				String browser = runManagerAccess.getValue(currentTestInstance, "Browser");
				if (!"".equals(browser)) {
					testParameters.setBrowser(Browser.valueOf(browser));
				} else {
					testParameters.setBrowser(Browser.valueOf(properties.getProperty("DefaultBrowser")));
				}
				String browserVersion = runManagerAccess.getValue(currentTestInstance, "BrowserVersion");
				if (!"".equals(browserVersion)) {
					testParameters.setBrowserVersion(browserVersion);
				}
				String platform = runManagerAccess.getValue(currentTestInstance, "Platform");
				if (!"".equals(platform)) {
					testParameters.setPlatform(Platform.valueOf(platform));
				} else {
					testParameters.setPlatform(Platform.valueOf(properties.getProperty("DefaultPlatform")));
				}
				
				testInstancesToRun.add(testParameters);
			}
		}
		
		return testInstancesToRun;
	}
}