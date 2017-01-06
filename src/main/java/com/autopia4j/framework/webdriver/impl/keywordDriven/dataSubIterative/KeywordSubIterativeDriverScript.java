package com.autopia4j.framework.webdriver.impl.keywordDriven.dataSubIterative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.autopia4j.framework.core.AutopiaException;
import com.autopia4j.framework.datatable.impl.SubIterativeDatatable;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.DriverScript;
import com.autopia4j.framework.webdriver.core.ExecutionMode;
import com.autopia4j.framework.webdriver.core.ReusableLibrary;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.TestHarness;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;


/**
 * Driver script class which encapsulates the core logic of the framework
 * @author vj
 */
public class KeywordSubIterativeDriverScript extends DriverScript {
	private final Logger logger = LoggerFactory.getLogger(KeywordSubIterativeDriverScript.class);
	
	private int currentSubIteration;
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public KeywordSubIterativeDriverScript(WebDriverTestParameters testParameters) {
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
		SubIterativeDatatable dataTable = initializeDatatable(runTimeDatatablePath);
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
		
		int startRowNum = testDataAccess.getRowNum(testParameters.getCurrentTestcase(), 0);
		int nTestcaseRows = testDataAccess.getRowCount(testParameters.getCurrentTestcase(), 0, startRowNum);
		int nSubIterations = testDataAccess.getRowCount("1", 1, startRowNum);	// Assumption: Every test case will have at least one iteration
		return nTestcaseRows / nSubIterations;
	}
	
	private SubIterativeDatatable initializeDatatable(String runTimeDatatablePath) {
		logger.info("Initializing datatable");
		
		SubIterativeDatatable dataTable =
				new SubIterativeDatatable(runTimeDatatablePath, testParameters.getCurrentModule());
		dataTable.setDataReferenceIdentifier(properties.getProperty("datatable.reference.identifier"));
		
		return dataTable;
	}
	
	private void executeTestScript(SubIterativeDatatable datatable, ScriptHelper scriptHelper) {
		List<String> businessFlowData = getBusinessFlowData(datatable.getDatatablePath());
		executeTestIterations(businessFlowData, datatable, scriptHelper);
	}
	
	private List<String> getBusinessFlowData(String datatablePath) {
		logger.info("Initializing the business flow for the specified test script");
		ExcelDataAccess businessFlowAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		businessFlowAccess.setDatasheetName("Business_Flow");
		
		int rowNum = businessFlowAccess.getRowNum(testParameters.getCurrentTestcase(), 0);
		if (rowNum == -1) {
			String errorDescription = "The test case \"" + testParameters.getCurrentTestcase() + "\" is not found in the Business Flow sheet!";
			logger.error(errorDescription);
			throw new AutopiaException(errorDescription);
		}
		
		String dataValue;
		List<String> businessFlowData = new ArrayList<>();
		int currentColumnNum = 1;
		while (true) {
			dataValue = businessFlowAccess.getValue(rowNum, currentColumnNum);
			if ("".equals(dataValue)) {
				break;
			}
			businessFlowData.add(dataValue);
			currentColumnNum++;
		}
		
		if (businessFlowData.isEmpty()) {
			String errorDescription = "No business flow found against the test case \"" + testParameters.getCurrentTestcase() + "\"";
			logger.error(errorDescription);
			throw new AutopiaException(errorDescription);
		}
		
		return businessFlowData;
	}
	
	private void executeTestIterations(List<String> businessFlowData, SubIterativeDatatable datatable, ScriptHelper scriptHelper) {
		while(currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: " + Integer.toString(currentIteration));
			
			// Evaluate each test iteration for any errors
			try {
				logger.info("Executing the business flow for the specified test script");
				processBusinessFlow(businessFlowData, datatable, scriptHelper);
			} catch (AutopiaException fx) {
				logger.error("Error during test execution", fx);
				handleExceptionInCurrentIteration(fx, fx.getErrorName());
			} catch (Exception ex) {
				logger.error("Error during test execution", ex);
				handleExceptionInCurrentIteration(ex, "Error");
			}
			
			currentIteration++;
		}
	}
	
	private void processBusinessFlow(List<String> businessFlowData, SubIterativeDatatable dataTable, ScriptHelper scriptHelper)
			throws IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		Map<String, Integer> keywordDirectory = new HashMap<>();
		
		for (int currentKeywordNum = 0; currentKeywordNum < businessFlowData.size(); currentKeywordNum++) {
			String[] currentFlowData = businessFlowData.get(currentKeywordNum).split(",");
			String currentKeyword = currentFlowData[0];
			
			int nKeywordIterations;
			if(currentFlowData.length > 1) {
				nKeywordIterations = Integer.parseInt(currentFlowData[1]);
			} else {
				nKeywordIterations = 1;
			}
			
			for (int currentKeywordIteration = 0; currentKeywordIteration < nKeywordIterations; currentKeywordIteration++) {
				if(keywordDirectory.containsKey(currentKeyword)) {
					keywordDirectory.put(currentKeyword, keywordDirectory.get(currentKeyword) + 1);
				} else {
					keywordDirectory.put(currentKeyword, 1);
				}
				currentSubIteration = keywordDirectory.get(currentKeyword);
				
				dataTable.setCurrentRow(testParameters.getCurrentTestcase(), currentIteration, currentSubIteration);
				
				if (currentSubIteration > 1) {
					report.addTestLogSubSection(currentKeyword + " (Sub-Iteration: " + currentSubIteration + ")");
				} else {
					report.addTestLogSubSection(currentKeyword);
				}
				
				invokeBusinessComponent(currentKeyword, scriptHelper);
			}
		}
	}
	
	private void invokeBusinessComponent(String currentKeyword, ScriptHelper scriptHelper) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Boolean isTestComponentFound = false;
		String pagesPackage = frameworkParameters.getBasePackageName() + ".pages";
		String flowsPackage = frameworkParameters.getBasePackageName() + ".flows";
		List<String> classNames =
				new FastClasspathScanner(pagesPackage, flowsPackage)
			    .scan()
			    .getNamesOfSubclassesOf(ReusableLibrary.class);
		
		for(String className : classNames) {
			Class<?> testLibrary = Class.forName(className);
			Method testComponent;
			
			try {
				currentKeyword = Util.unCapitalizeFirstLetter(currentKeyword);
				testComponent = testLibrary.getMethod(currentKeyword, (Class<?>[]) null);
			} catch(NoSuchMethodException ex) {
				// If the method is not found in this class, search the next class
				logger.trace("Method " + currentKeyword + " not found. Continuing search...", ex);
				continue;
			}
			
			isTestComponentFound = true;
			
			Constructor<?> ctor = testLibrary.getDeclaredConstructors()[0];
			Object testLibraryInstance = ctor.newInstance(scriptHelper);
			testComponent.invoke(testLibraryInstance, (Object[]) null);
			
			break;
		}
		
		if(!isTestComponentFound) {
			String errorDescription = "Keyword " + currentKeyword + 
											" not found within the test library!";
			logger.error(errorDescription);
			throw new AutopiaException(errorDescription);
		}
	}
}