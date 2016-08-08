package com.autopia4j.framework.webdriver;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.autopia4j.framework.utils.FrameworkException;

import org.openqa.selenium.TimeoutException;


/**
 * Class containing useful WebDriver utility functions
 * @author vj
 */
public class WebDriverUtil {
	private WebDriver driver;
	private final long objectSyncTimeout, pageLoadTimeout;
	
	/**
	 * Constructor to initialize the {@link WebDriverUtil} object
	 * @param driver The {@link WebDriver} object
	 * @param objectSyncTimeout The object synchronization timeout
	 * @param pageLoadTimeout The page load timeout
	 */
	public WebDriverUtil(WebDriver driver, long objectSyncTimeout, long pageLoadTimeout) {
		this.driver = driver;
		this.objectSyncTimeout = objectSyncTimeout;
		this.pageLoadTimeout = pageLoadTimeout;
	}
	
	/**
	 * Function to pause the execution for the specified time period
	 * @param milliSeconds The wait time in milliseconds
	 */
	public void waitFor(long milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to wait until the page loads completely
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	@Deprecated
	public void waitUntilPageLoaded(long timeOutInSeconds) {
		WebElement oldPage = driver.findElement(By.tagName("html"));
		
		(new WebDriverWait(driver, timeOutInSeconds))
									.until(ExpectedConditions.stalenessOf(oldPage));
		
	}
	
	/**
	 * Function to wait until the page readyState equals 'complete'
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	public void waitUntilPageReadyStateComplete(long timeOutInSeconds) {
		ExpectedCondition<Boolean> pageReadyStateComplete =
			new ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver driver) {
	                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
	            }
	        };
		    
		(new WebDriverWait(driver, timeOutInSeconds)).until(pageReadyStateComplete);
	}
	
	/**
	 * Function to wait until the page readyState equals 'complete'
	 */
	public void waitUntilPageReadyStateComplete() {
		waitUntilPageReadyStateComplete(pageLoadTimeout);
	}
	
	/**
	 * Function to wait until the specified element is located
	 * @param by The {@link WebDriver} locator used to identify the element
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	public void waitUntilElementLocated(By by, long timeOutInSeconds) {
		(new WebDriverWait(driver, timeOutInSeconds))
							.until(ExpectedConditions.presenceOfElementLocated(by));
	}
	
	/**
	 * Function to wait until the specified element is located
	 * @param by The {@link WebDriver} locator used to identify the element
	 */
	public void waitUntilElementLocated(By by) {
		waitUntilElementLocated(by, objectSyncTimeout);
	}
	
	/**
	 * Function to wait until the specified element is visible
	 * @param by The {@link WebDriver} locator used to identify the element
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	public void waitUntilElementVisible(By by, long timeOutInSeconds) {
		(new WebDriverWait(driver, timeOutInSeconds))
							.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	/**
	 * Function to wait until the specified element is visible
	 * @param by The {@link WebDriver} locator used to identify the element
	 */
	public void waitUntilElementVisible(By by) {
		waitUntilElementVisible(by, objectSyncTimeout);
	}
	
	/**
	 * Function to wait until the specified element is enabled
	 * @param by The {@link WebDriver} locator used to identify the element
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	public void waitUntilElementEnabled(By by, long timeOutInSeconds) {
		(new WebDriverWait(driver, timeOutInSeconds))
							.until(ExpectedConditions.elementToBeClickable(by));
	}
	
	/**
	 * Function to wait until the specified element is enabled
	 * @param by The {@link WebDriver} locator used to identify the element
	 */
	public void waitUntilElementEnabled(By by) {
		waitUntilElementEnabled(by, objectSyncTimeout);
	}
	
	/**
	 * Function to wait until the specified element is disabled
	 * @param by The {@link WebDriver} locator used to identify the element
	 * @param timeOutInSeconds The wait timeout in seconds
	 */
	public void waitUntilElementDisabled(By by, long timeOutInSeconds) {
		(new WebDriverWait(driver, timeOutInSeconds))
			.until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(by)));
	}
	
	/**
	 * Function to wait until the specified element is disabled
	 * @param by The {@link WebDriver} locator used to identify the element
	 */
	public void waitUntilElementDisabled(By by) {
		waitUntilElementDisabled(by, objectSyncTimeout);
	}
	
	/**
	 * Function to select the specified value from a listbox
	 * @param by The {@link WebDriver} locator used to identify the listbox
	 * @param itemText The value to be selected within the listbox
	 */
	public void selectListItem(By by, String itemText) {
		Select dropDownList = new Select(driver.findElement(by));
		dropDownList.selectByVisibleText(itemText);
	}
	
	/**
	 * Function to select the specified value from a listbox
	 * @param by The {@link WebDriver} locator used to identify the listbox
	 * @param itemIndex The index of the value to be selected within the listbox
	 */
	public void selectListItem(By by, int itemIndex) {
		Select dropDownList = new Select(driver.findElement(by));
		dropDownList.selectByIndex(itemIndex);
	}
	
	/**
	 * Function to select a random item from a listbox
	 * @param by The {@link WebDriver} locator used to identify the listbox
	 */
	public void selectRandomListItem(By by) {
		Select dropDownList = new Select(driver.findElement(by));
		List<WebElement> listOptions = dropDownList.getOptions();
		dropDownList.selectByIndex(randomInteger(0, listOptions.size()-1));
	}
	
	/**
	 * Function to select random items from a multi-select listbox
	 * @param by The {@link WebDriver} locator used to identify the listbox
	 */
	public void selectRandomListItems(By by) {
		Select multiSelectDropDownList = new Select(driver.findElement(by));
		List<WebElement> listOptions = multiSelectDropDownList.getOptions();
		for(int i=0; i<randomInteger(0, listOptions.size()-1); i++) {
			multiSelectDropDownList.selectByIndex(randomInteger(0, listOptions.size()-1));
		}
	}
	
	private int randomInteger(int min, int max) {
	    Random rand = new Random();
	    
	    // nextInt excludes the top value so we have to add 1 to include the top value
	    return rand.nextInt((max - min) + 1) + min;
	}
	
	/**
	 * Function to do a mouseover on top of the specified element
	 * @param by The {@link WebDriver} locator used to identify the element
	 */
	public void mouseOver(By by) {
		Actions actions = new Actions(driver);
		actions.moveToElement(driver.findElement(by)).build().perform();
	}
	
	/**
	 * Function to verify whether the specified object exists within the current page
	 * @param by The {@link WebDriver} locator used to identify the element
	 * @return Boolean value indicating whether the specified object exists
	 */
	public Boolean objectExists(By by) {
		int numberOfMatches = driver.findElements(by).size();		
		if(numberOfMatches == 1) {
			return true;
		}
		else		{
			// 0 matches OR more than 1 match
			return false;	
		}
	}
	
	/**
	 * 
	 * Function to verify whether the specified text is present within the current page
	 * @param textPattern The text to be verified
	 * @return Boolean value indicating whether the specified test is present
	 */
	public Boolean isTextPresent(String textPattern) {
		return driver.findElement(By.cssSelector("BODY")).getText().matches(textPattern);
	}
	
	/**
	 * Function to check if an alert is present on the current page
	 * @param timeOutInSeconds The number of seconds to wait while checking for the alert
	 * @return Boolean value indicating whether an alert is present
	 */
	public Boolean isAlertPresent(long timeOutInSeconds) {
		try {
			new WebDriverWait(driver, timeOutInSeconds).until(ExpectedConditions.alertIsPresent());
			return true;
		} catch (TimeoutException ex) {
			return false;
		}
	}
	
	/**
	 * Function to switch to the most recently opened pop-up window
	 * @param nPopupsAlreadyOpen The number of pop-ups which are already open
	 * @param timeOutInSeconds The number of seconds to wait for the pop-up window to open and load
	 * @return The window handle of the parent window
	 */
	public String switchToPopup(int nPopupsAlreadyOpen, long timeOutInSeconds) {
		String mainWindowHandle = driver.getWindowHandle();
		String popupWindowHandle = getPopupWindowHandle(nPopupsAlreadyOpen, timeOutInSeconds);
		driver.switchTo().window(popupWindowHandle);
		waitUntilPageReadyStateComplete(timeOutInSeconds);
		
		return mainWindowHandle;
	}
	
	/**
	 * Function to switch to the most recently opened pop-up window
	 * @param nPopupsAlreadyOpen The number of pop-ups which are already open
	 * @return The window handle of the parent window
	 */
	public String switchToPopup(int nPopupsAlreadyOpen) {
		return switchToPopup(nPopupsAlreadyOpen, pageLoadTimeout);
	}
	
	private String getPopupWindowHandle(int nPopupsAlreadyOpen, long timeOutInSeconds) {
		Object[] openWindowHandles = driver.getWindowHandles().toArray();
		int milliSecondsWaited = 0;
		while(milliSecondsWaited < timeOutInSeconds*1000) {
			if(openWindowHandles.length > nPopupsAlreadyOpen+1) {
				break;
			} else {
				waitFor(100);
				milliSecondsWaited += 100;
				openWindowHandles = driver.getWindowHandles().toArray();
			}
		}
		if(openWindowHandles.length < nPopupsAlreadyOpen+2) {
			throw new FrameworkException("The pop-up window did not open as expected!");
		}
		
		return openWindowHandles[openWindowHandles.length - 1].toString();
	}
}