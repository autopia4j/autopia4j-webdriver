package com.autopia4j.framework.webdriver.core;

import java.util.Properties;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.core.FrameworkParameters;
import com.autopia4j.framework.core.OnError;
import com.autopia4j.framework.core.Settings;
import com.autopia4j.framework.reporting.Status;
import com.autopia4j.framework.webdriver.reporting.WebDriverReport;

/**
 * Abstract class that defines the execution structure for data iteration based implementations of autopia4j 
 * @author vj
 */
public abstract class DriverScript {
	protected final WebDriverTestParameters testParameters;
	protected WebDriverReport report;
	
	protected final FrameworkParameters frameworkParameters = FrameworkParameters.getInstance();
	protected Properties properties = Settings.getInstance();
	
	protected int currentIteration;
	
	protected String executionTime;
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public DriverScript(WebDriverTestParameters testParameters) {
		this.testParameters = testParameters;
	}
	
	/**
	 * Function to get the {@link WebDriverTestParameters} object
	 * @return The {@link WebDriverTestParameters} object
	 */
	public WebDriverTestParameters getTestParameters() {
		return testParameters;
	}
	
	/**
	 * Function to get the name of the test report
	 * @return The test report name
	 */
	public String getReportName() {
		return report.getReportSettings().getReportName();
	}
	
	/**
	 * Function to get the status of the test case executed
	 * @return The test status
	 */
	public String getTestStatus() {
		return report.getTestStatus();
	}
	
	/**
	 * Function to get the description of any failure that may occur during the script execution
	 * @return The failure description (relevant only if the test fails)
	 */
	public String getFailureDescription() {
		return report.getFailureDescription();
	}
	
	/**
	 * Function to get the execution time for the test case
	 * @return The test execution time
	 */
	public String getExecutionTime() {
		return executionTime;
	}
	
	/**
	 * Function to initialize the iteration settings for the given test case
	 * @param datatablePath The path where the datatable is stored
	 */
	protected void initializeTestIterations(String datatablePath) {
		switch(testParameters.getIterationMode()) {
		case RUN_ALL_ITERATIONS:
			int nIterations = getNumberOfIterations(datatablePath);
			testParameters.setEndIteration(nIterations);
			
			currentIteration = 1;
			break;
			
		case RUN_ONE_ITERATION_ONLY:
			currentIteration = 1;
			break;
			
		case RUN_RANGE_OF_ITERATIONS:
			if(testParameters.getStartIteration() > testParameters.getEndIteration()) {
				throw new AutopiaException("Error","StartIteration cannot be greater than EndIteration!");
			}
			currentIteration = testParameters.getStartIteration();
			break;
			
		default:
			throw new AutopiaException("Unhandled Iteration Mode!");
		}
	}
	
	/**
	 * Function to calculate the number of iterations configured for the given test case
	 * @param datatablePath The path where the datatable is stored
	 * @return The number of iterations configured for the given test case
	 */
	protected abstract int getNumberOfIterations(String datatablePath);
	
	/**
	 * Function to execute all iterations of the given test case
	 */
	public abstract void driveTestExecution();
	
	/**
	 * Function to handle any exception that occurs during a specific iteration of the given test case
	 * @param ex The {@link Exception} thrown
	 * @param exceptionName The name of the Exception to be reported
	 */
	protected void handleExceptionInCurrentIteration(Exception ex, String exceptionName) {
		// Error reporting
		String exceptionDescription = ex.getMessage();
		if(exceptionDescription == null) {
			exceptionDescription = ex.toString();
		}
		
		if(ex.getCause() != null) {
			report.updateTestLog(exceptionName, exceptionDescription + " <b>Caused by: </b>" +
																ex.getCause(), Status.FAIL, true);
		} else {
			report.updateTestLog(exceptionName, exceptionDescription, Status.FAIL, true);
		}
		
		// Error response
		report.addTestLogSubSection("ErrorResponse");
		if (frameworkParameters.getStopExecution()) {
			report.updateTestLog("Framework Info",
					"Test execution terminated by user! All subsequent tests aborted...",
					Status.DONE);
			currentIteration = testParameters.getEndIteration();
		} else {
			OnError onError = OnError.valueOf(properties.getProperty("on.error"));
			switch(onError) {
			// Stop option is not relevant when run from HP ALM
			case NEXT_ITERATION:
				report.updateTestLog("Framework Info",
						"Test case iteration terminated by user! Proceeding to next iteration (if applicable)...",
						Status.DONE);
				break;
				
			case NEXT_TESTCASE:
				report.updateTestLog("Framework Info",
						"Test case terminated by user! Proceeding to next test case (if applicable)...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;
				
			case STOP:
				frameworkParameters.setStopExecution(true);
				report.updateTestLog("Framework Info",
						"Test execution terminated by user! All subsequent tests aborted...",
						Status.DONE);
				currentIteration = testParameters.getEndIteration();
				break;
				
			default:
				throw new AutopiaException("Unhandled OnError option!");
			}
		}
	}
}