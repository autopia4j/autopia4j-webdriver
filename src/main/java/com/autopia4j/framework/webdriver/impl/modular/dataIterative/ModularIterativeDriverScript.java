package com.autopia4j.framework.webdriver.impl.modular.dataIterative;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.datatable.impl.IterativeDatatable;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.DriverScript;
import com.autopia4j.framework.webdriver.core.ExecutionMode;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.TestHarness;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Driver script class which encapsulates the core logic of the framework
 * @author vj
 */
public class ModularIterativeDriverScript extends DriverScript {
	private final Logger logger = LoggerFactory.getLogger(ModularIterativeDriverScript.class);
	
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public ModularIterativeDriverScript(WebDriverTestParameters testParameters) {
		super(testParameters);
	}
	
	@Override
	public void driveTestExecution() {
		TestHarness testHarness = new TestHarness();
		
		testHarness.setDefaultTestParameters(testParameters);
		String datatablePath = testHarness.getDatatablePath();
		initializeTestIterations(datatablePath);
		WebDriver driver = testHarness.initializeWebDriver(testParameters);
		report = testHarness.initializeTestReport(testParameters, driver);
		
		String runTimeDatatablePath =
				testHarness.getRuntimeDatatablePath(datatablePath, report, testParameters);
		IterativeDatatable dataTable = initializeDatatable(runTimeDatatablePath);
		ScriptHelper scriptHelper = new ScriptHelper(testParameters, dataTable, report, driver);
		executeTestScript(dataTable, scriptHelper);
		
		if (testParameters.getExecutionMode() == ExecutionMode.PERFECTO_DEVICE) {
			testHarness.downloadPerfectoResults(driver, report);
		}
		testHarness.quitWebDriver(driver);
		executionTime = testHarness.tearDown(scriptHelper);
		testHarness.closeTestReport(scriptHelper, executionTime);
	}
	
	@Override
	protected int getNumberOfIterations(String datatablePath) {
		ExcelDataAccess testDataAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		testDataAccess.setDatasheetName(properties.getProperty("datatable.default.sheet"));
		return testDataAccess.getRowCount(testParameters.getCurrentTestcase(), 0);
	}
	
	private IterativeDatatable initializeDatatable(String runTimeDatatablePath) {
		logger.info("Initializing datatable");
		
		IterativeDatatable dataTable =
				new IterativeDatatable(runTimeDatatablePath, testParameters.getCurrentModule());
		dataTable.setDataReferenceIdentifier(properties.getProperty("datatable.reference.identifier"));
		
		// Initialize the datatable row in case test data is required during the setUp()
		dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration);
		
		return dataTable;
	}
	
	private void executeTestScript(IterativeDatatable dataTable, ScriptHelper scriptHelper) {
		ModularIterativeTestScript testScript = getTestScriptInstance();
		testScript.initialize(scriptHelper);
		
		try {
			logger.info("Executing setup for the specified test script");
			testScript.setUp();
			executeTestIterations(testScript, dataTable);
		} catch (AutopiaException fx) {
			handleExceptionInCurrentIteration(fx, fx.getErrorName());
		}  catch (Exception ex) {
			handleExceptionInCurrentIteration(ex, "Error");
		} finally {
			logger.info("Executing tear-down for the specified test script");
			testScript.tearDown();	// tearDown will ALWAYS be called
		}
	}
	
	private ModularIterativeTestScript getTestScriptInstance() {
		Class<?> testScriptClass;
		try {
			testScriptClass = Class.forName(frameworkParameters.getBasePackageName() +
											".testscripts." +
											Util.unCapitalizeFirstLetter(
												testParameters.getCurrentModule()) +
											"." + testParameters.getCurrentTestcase());
		} catch (ClassNotFoundException e) {
			String errorDescription = "The specified test case is not found!";
			logger.error(errorDescription, e);
			throw new AutopiaException(errorDescription);
		}
		
		try {
			return (ModularIterativeTestScript) testScriptClass.newInstance();
		} catch (Exception e) {
			String errorDescription = "Error while instantiating the specified test script";
			logger.error(errorDescription, e);
			throw new AutopiaException(errorDescription);
		}
	}
	
	private void executeTestIterations(ModularIterativeTestScript testScript, IterativeDatatable dataTable) {
		while(currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: " + Integer.toString(currentIteration));
			
			// Evaluate each test iteration for any errors
			try {
				logger.info("Executing the specified test script");
				testScript.executeTest();
			} catch (AutopiaException fx) {
				logger.error("Error during test execution", fx);
				handleExceptionInCurrentIteration(fx, fx.getErrorName());
			}  catch (Exception ex) {
				logger.error("Error during test execution", ex);
				handleExceptionInCurrentIteration(ex, "Error");
			}
			
			currentIteration++;
			dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration);
		}
	}
}