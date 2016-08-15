package com.autopia4j.framework.webdriver.testrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.utils.FrameworkException;
import com.autopia4j.framework.webdriver.core.DriverScript;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import com.autopia4j.framework.webdriver.reporting.ResultSummaryManager;

/**
 * Class to facilitate parallel execution of test scripts
 * @author vj
 */
class ParallelRunner implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(ParallelRunner.class);
	private final WebDriverTestParameters testParameters;
	private static int testBatchStatus = 0;
	
	
	/**
	 * Constructor to initialize the details of the test case to be executed
	 * @param testParameters The {@link WebDriverTestParameters} object (passed from the {@link Allocator})
	 */
	ParallelRunner(WebDriverTestParameters testParameters) {
		super();
		
		this.testParameters = testParameters;
	}
	
	/**
	 * Function to get the overall test batch status
	 * @return The test batch status (0 = Success, 1 = Failure)
	 */
	public int getTestBatchStatus() {
		return testBatchStatus;
	}
	
	@Override
	public void run() {
		FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
		String testReportName;
		String executionTime;
		String testStatus;
		
		if(frameworkParameters.getStopExecution()) {
			testReportName = "N/A";
			executionTime = "N/A";
			testStatus = "Aborted";
			testBatchStatus = 1;	// Non-zero outcome indicates failure
		} else {
			DriverScript driverScript;
			
			switch(frameworkParameters.getFrameworkType()) {
			case KEYWORD_DRIVEN:
				driverScript = new com.autopia4j.framework.webdriver.impl.keyword.KeywordDriverScript(this.testParameters);
				break;
				
			case MODULAR:
				driverScript = new com.autopia4j.framework.webdriver.impl.modular.ModularDriverScript(
										this.testParameters);
				//TODO: Directly instantiate the test script class and pass it in here
				break;
				
			default:
				throw new FrameworkException("Unknown framework type!");	
			}
			
			try {
				driverScript.driveTestExecution();
				testReportName = driverScript.getReportName();
				executionTime = driverScript.getExecutionTime();
				testStatus = driverScript.getTestStatus();
			} catch(Exception ex) {
				logger.error("Error occurred during test execution!", ex);
				
				testReportName = "N/A";
				executionTime = "N/A";
				testStatus = "Failed";
			}
			
			if ("failed".equalsIgnoreCase(testStatus)) {
				testBatchStatus = 1;	// Non-zero outcome indicates failure
			}
		}
		
		ResultSummaryManager resultSummaryManager = ResultSummaryManager.getInstance();
		resultSummaryManager.updateResultSummary(testParameters, testReportName,
															executionTime, testStatus);
	}
}