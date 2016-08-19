package com.autopia4j.framework.webdriver.impl.modular;

import java.io.File;
import java.io.IOException;

import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.datatable.impl.ModularDatatable;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.DriverScript;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Driver script class which encapsulates the core logic of the framework
 * @author vj
 */
public class ModularDriverScript extends DriverScript {
	private final Logger logger = LoggerFactory.getLogger(ModularDriverScript.class);
	//private ModularTestScript testScript;
	private ThreadLocal<ModularTestScript> testScript = new ThreadLocal<>();
	
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public ModularDriverScript(WebDriverTestParameters testParameters) {
		super(testParameters);
		//this.testScript = null;
	}
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 * @param testScript A {@link ModularTestScript} object
	 */
	public ModularDriverScript(WebDriverTestParameters testParameters, ModularTestScript testScript) {
		super(testParameters);
		this.testScript.set(testScript);
	}
	
	@Override
	public void driveTestExecution() {
		startUp();
		initializeTestIterations();
		initializeWebDriver();
		initializeTestReport();
		initializeDatatable();
		initializeTestScript();
		
		try {
			logger.info("Executing setup for the specified test script");
			testScript.get().setUp();
			executeTestIterations();
		} catch (AutopiaException fx) {
			exceptionHandler(fx, fx.getErrorName());
		}  catch (Exception ex) {
			exceptionHandler(ex, "Error");
		} finally {
			logger.info("Executing tear-down for the specified test script");
			testScript.get().tearDown();	// tearDown will ALWAYS be called
		}
		
		quitWebDriver();
		wrapUp();
	}
	
	protected int getNumberOfIterations() {
		ExcelDataAccess testDataAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		testDataAccess.setDatasheetName(properties.getProperty("DefaultDataSheet"));
		return testDataAccess.getRowCount(testParameters.getCurrentTestcase(), 0);
	}
	
	private void initializeDatatable() {
		logger.info("Initializing datatable");
		String runTimeDatatablePath;
		Boolean includeTestDataInReport =
				Boolean.parseBoolean(properties.getProperty("IncludeTestDataInReport"));
		if (includeTestDataInReport) {
			runTimeDatatablePath = reportPath + Util.getFileSeparator() + "datatables";
			
			File runTimeDatatable = new File(runTimeDatatablePath + Util.getFileSeparator() +
												testParameters.getCurrentModule() + ".xls");
			if (!runTimeDatatable.exists()) {
				synchronized (ModularDriverScript.class) {
					if (!runTimeDatatable.exists()) {
						File datatable = new File(datatablePath + Util.getFileSeparator() +
													testParameters.getCurrentModule() + ".xls");
						
						try {
							FileUtils.copyFile(datatable, runTimeDatatable);
						} catch (IOException e) {
							String errorDescription = "Error in creating run-time datatable: Copying the datatable failed...";
							logger.error(errorDescription, e);
							throw new AutopiaException(errorDescription);
						}
					}
				}
			}
			
			File runTimeCommonDatatable = new File(runTimeDatatablePath +
													Util.getFileSeparator() +
													"Common Testdata.xls");
			if (!runTimeCommonDatatable.exists()) {
				synchronized (ModularDriverScript.class) {
					if (!runTimeCommonDatatable.exists()) {
						File commonDatatable = new File(datatablePath +
												Util.getFileSeparator() + "Common Testdata.xls");
						
						try {
							FileUtils.copyFile(commonDatatable, runTimeCommonDatatable);
						} catch (IOException e) {
							String errorDescription = "Error in creating run-time datatable: Copying the common datatable failed...";
							logger.error(errorDescription, e);
							throw new AutopiaException(errorDescription);
						}
					}
				}
			}
		} else {
			runTimeDatatablePath = datatablePath;
		}
		
		dataTable = new ModularDatatable(runTimeDatatablePath, testParameters.getCurrentModule());
		dataTable.setDataReferenceIdentifier(properties.getProperty("DataReferenceIdentifier"));
		
		// Initialize the datatable row in case test data is required during the setUp()
		dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration);
	}
	
	private void initializeTestScript() {
		scriptHelper = new ScriptHelper(testParameters, dataTable, report, driver);
		
		if(testScript.get() == null) {
			testScript.set(getTestScriptInstance());
		}
		testScript.get().initialize(scriptHelper);
	}
	
	private ModularTestScript getTestScriptInstance() {
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
			return (ModularTestScript) testScriptClass.newInstance();
		} catch (Exception e) {
			String errorDescription = "Error while instantiating the specified test script";
			logger.error(errorDescription, e);
			throw new AutopiaException(errorDescription);
		}
	}
	
	private void executeTestIterations() {
		while(currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: " + Integer.toString(currentIteration));
			
			// Evaluate each test iteration for any errors
			try {
				logger.info("Executing the specified test script");
				testScript.get().executeTest();
			} catch (AutopiaException fx) {
				exceptionHandler(fx, fx.getErrorName());
			}  catch (Exception ex) {
				exceptionHandler(ex, "Error");
			}
			
			currentIteration++;
			dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration);
		}
	}
}