package com.autopia4j.framework.webdriver.modular;

import java.io.File;
import java.io.IOException;

import com.autopia4j.framework.datatable.impl.ModularDatatable;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.utils.FrameworkException;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.*;

import org.apache.commons.io.FileUtils;


/**
 * Driver script class which encapsulates the core logic of the framework
 * @author vj
 */
public class ModularDriverScript extends DriverScript {
	private TestCase testCase;
	
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public ModularDriverScript(WebDriverTestParameters testParameters) {
		super(testParameters);
		this.testCase = null;
	}
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 * @param testCase A {@link TestCase} object
	 */
	public ModularDriverScript(WebDriverTestParameters testParameters, TestCase testCase) {
		super(testParameters);
		this.testCase = testCase;
	}
	
	@Override
	public void driveTestExecution() {
		startUp();
		initializeTestIterations();
		initializeWebDriver();
		initializeTestReport();
		initializeDatatable();
		initializeTestCase();
		
		try {
			testCase.setUp();
			executeTestIterations();
		} catch (FrameworkException fx) {
			exceptionHandler(fx, fx.getErrorName());
		}  catch (Exception ex) {
			exceptionHandler(ex, "Error");
		} finally {
			testCase.tearDown();	// tearDown will ALWAYS be called
		}
		
		quitWebDriver();
		wrapUp();
	}
	
	protected int getNumberOfIterations() {
		String datatablePath = frameworkParameters.getBasePath() +
								Util.getFileSeparator() + "src" +
								Util.getFileSeparator() + "test" +
								Util.getFileSeparator() + "resources" +
								Util.getFileSeparator() + "datatables";
		ExcelDataAccess testDataAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		testDataAccess.setDatasheetName(properties.getProperty("DefaultDataSheet"));
		return testDataAccess.getRowCount(testParameters.getCurrentTestcase(), 0);
	}
	
	private void initializeDatatable() {
		String datatablePath = frameworkParameters.getBasePath() +
								Util.getFileSeparator() + "src" +
								Util.getFileSeparator() + "test" +
								Util.getFileSeparator() + "resources" +
								Util.getFileSeparator() + "datatables";
		
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
							e.printStackTrace();
							throw new FrameworkException("Error in creating run-time datatable: Copying the datatable failed...");
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
							e.printStackTrace();
							throw new FrameworkException("Error in creating run-time datatable: Copying the common datatable failed...");
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
	
	private void initializeTestCase() {
		driverUtil = new WebDriverUtil(driver, report);
		galenUtil = new GalenUtil(driver, report, reportSettings);
		scriptHelper = new ScriptHelper(testParameters, dataTable,
												report, driver, driverUtil, galenUtil);
		
		if(testCase == null) {
			testCase = getTestCaseInstance();
		}
		testCase.initialize(scriptHelper);
	}
	
	private TestCase getTestCaseInstance() {
		Class<?> testScriptClass;
		try {
			testScriptClass = Class.forName(frameworkParameters.getBasePackageName() +
											".testscripts." +
											Util.unCapitalizeFirstLetter(
												testParameters.getCurrentModule()) +
											"." + testParameters.getCurrentTestcase());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new FrameworkException("The specified test case is not found!");
		}
		
		try {
			return (TestCase) testScriptClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException("Error while instantiating the specified test script");
		}
	}
	
	private void executeTestIterations() {
		while(currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: " + Integer.toString(currentIteration));
			
			// Evaluate each test iteration for any errors
			try {
				testCase.executeTest();
			} catch (FrameworkException fx) {
				exceptionHandler(fx, fx.getErrorName());
			}  catch (Exception ex) {
				exceptionHandler(ex, "Error");
			}
			
			currentIteration++;
			dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration);
		}
	}
}