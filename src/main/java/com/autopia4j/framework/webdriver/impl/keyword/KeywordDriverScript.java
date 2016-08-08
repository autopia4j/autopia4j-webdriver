package com.autopia4j.framework.webdriver.impl.keyword;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.autopia4j.framework.datatable.impl.KeywordDatatable;
import com.autopia4j.framework.utils.ExcelDataAccess;
import com.autopia4j.framework.utils.FrameworkException;
import com.autopia4j.framework.utils.Util;
import com.autopia4j.framework.webdriver.core.DriverScript;
import com.autopia4j.framework.webdriver.core.ReusableLibrary;
import com.autopia4j.framework.webdriver.core.ScriptHelper;
import com.autopia4j.framework.webdriver.core.WebDriverTestParameters;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import org.apache.commons.io.FileUtils;
import java.lang.reflect.*;


/**
 * Driver script class which encapsulates the core logic of the framework
 * @author vj
 */
public class KeywordDriverScript extends DriverScript {
	private List<String> businessFlowData;
	private int currentSubIteration;
	
	
	/**
	 * DriverScript constructor
	 * @param testParameters A {@link WebDriverTestParameters} object
	 */
	public KeywordDriverScript(WebDriverTestParameters testParameters) {
		super(testParameters);
	}
	
	@Override
	public void driveTestExecution() {
		startUp();
		initializeTestIterations();
		initializeWebDriver();
		initializeTestReport();
		initializeDatatable();
		initializeTestScript();
		
		executeTestIterations();
		
		quitWebDriver();
		wrapUp();
	}
	
	@Override
	protected int getNumberOfIterations() {
		ExcelDataAccess testDataAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		testDataAccess.setDatasheetName(properties.getProperty("DefaultDataSheet"));
		
		int startRowNum = testDataAccess.getRowNum(testParameters.getCurrentTestcase(), 0);
		int nTestcaseRows = testDataAccess.getRowCount(testParameters.getCurrentTestcase(), 0, startRowNum);
		int nSubIterations = testDataAccess.getRowCount("1", 1, startRowNum);	// Assumption: Every test case will have at least one iteration
		return nTestcaseRows / nSubIterations;
	}
	
	private void initializeDatatable() {
		String runTimeDatatablePath;
		Boolean includeTestDataInReport =
				Boolean.parseBoolean(properties.getProperty("IncludeTestDataInReport"));
		if (includeTestDataInReport) {
			runTimeDatatablePath = reportPath + Util.getFileSeparator() + "datatables";
			
			File runTimeDatatable = new File(runTimeDatatablePath + Util.getFileSeparator() +
												testParameters.getCurrentModule() + ".xls");
			if (!runTimeDatatable.exists()) {
				synchronized (KeywordDriverScript.class) {
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
				synchronized (KeywordDriverScript.class) {
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
		
		dataTable = new KeywordDatatable(runTimeDatatablePath, testParameters.getCurrentModule());
		dataTable.setDataReferenceIdentifier(properties.getProperty("DataReferenceIdentifier"));
	}
	
	private void initializeTestScript() {
		scriptHelper = new ScriptHelper(testParameters, dataTable, report, driver);
		
		initializeBusinessFlow();
	}
	
	private void initializeBusinessFlow() {
		ExcelDataAccess businessFlowAccess =
				new ExcelDataAccess(datatablePath, testParameters.getCurrentModule());
		businessFlowAccess.setDatasheetName("Business_Flow");
		
		int rowNum = businessFlowAccess.getRowNum(testParameters.getCurrentTestcase(), 0);
		if (rowNum == -1) {
			throw new FrameworkException("The test case \"" + testParameters.getCurrentTestcase() + "\" is not found in the Business Flow sheet!");
		}
		
		String dataValue;
		businessFlowData = new ArrayList<String>();
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
			throw new FrameworkException("No business flow found against the test case \"" + testParameters.getCurrentTestcase() + "\"");
		}
	}
	
	private void executeTestIterations() {
		while(currentIteration <= testParameters.getEndIteration()) {
			report.addTestLogSection("Iteration: " + Integer.toString(currentIteration));
			
			// Evaluate each test iteration for any errors
			try {
				executeTestcase(businessFlowData);
			} catch (FrameworkException fx) {
				exceptionHandler(fx, fx.getErrorName());
			} catch (InvocationTargetException ix) {
				exceptionHandler((Exception)ix.getCause(), "Error");
			} catch (Exception ex) {
				exceptionHandler(ex, "Error");
			}
			
			currentIteration++;
		}
	}
	
	private void executeTestcase(List<String> businessFlowData)
			throws IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, InstantiationException {
		Map<String, Integer> keywordDirectory = new HashMap<String, Integer>();
		
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
				
				invokeBusinessComponent(currentKeyword);
			}
		}
	}
	
	private void invokeBusinessComponent(String currentKeyword) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Boolean isTestComponentFound = false;
		String pagesPackage = frameworkParameters.getBasePackageName() + ".pages";
		String flowsPackage = frameworkParameters.getBasePackageName() + ".flows";
		List<String> classNames =
				new FastClasspathScanner(pagesPackage, flowsPackage)
			    .scan()
			    .getNamesOfSubclassesOf(ReusableLibrary.class);
			    //.getNamesOfAllClasses();
		
		for(String className : classNames) {
			Class<?> testLibrary = Class.forName(className);
			Method testComponent;
			
			try {
				currentKeyword = Util.unCapitalizeFirstLetter(currentKeyword);
				testComponent = testLibrary.getMethod(currentKeyword, (Class<?>[]) null);
			} catch(NoSuchMethodException ex) {
				// If the method is not found in this class, search the next class
				continue;
			}
			
			isTestComponentFound = true;
			
			Constructor<?> ctor = testLibrary.getDeclaredConstructors()[0];
			Object testLibraryInstance = ctor.newInstance(scriptHelper);
			testComponent.invoke(testLibraryInstance, (Object[]) null);
			
			break;
		}
		
		if(!isTestComponentFound) {
			throw new FrameworkException("Keyword " + currentKeyword + 
											" not found within the test library!");
		}
	}
}