package com.autopia4j.framework.webdriver.impl.keyword;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openqa.selenium.Platform;
import org.openqa.selenium.ScreenOrientation;

import com.autopia4j.framework.core.IterationOptions;
import com.autopia4j.framework.webdriver.core.Browser;
import com.autopia4j.framework.webdriver.core.DeviceType;
import com.autopia4j.framework.webdriver.core.ExecutionMode;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutopiaKeywordTest {

	public String moduleName();
	public String testName();
	public String currentTestInstance();
	public String currentTestDescription();
	public String additionalDetails();
	public IterationOptions iterationMode();
	public int startIteration();
	public int endIteration();
	
	public ExecutionMode executionMode();
	public Browser browser();
	public String browserVersion();
	public Platform platform();
	public DeviceType deviceType();
	public ScreenOrientation screenOrientation();
	public String deviceName();
	public String remoteUrl();
}
