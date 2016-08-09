autopia4j is an open source automation framework, designed to work with any Java based automation tool. The most prominent implementation of the framework is on top of Selenium WebDriver. A SoapUI implementation is also available.

The autopia4j-webdriver project contains the implementation of the framework over Selenium WebDriver. The project also includes integration with Galen for automated UI layour testing, and Appium for mobile web application testing. Note that this project depends on the autopia4j-core libraries.

How to build the library from source:
1. Check out the latest version from BitBucket
2. Run "mvn clean install"

How to generate the documentation:
1. Run "mvn javadoc:javadoc"

How to develop automated scripts using the framework:
1. There are 2 variants of this framework - keyword driven and modular
2. Refer to the autopia4j-webdriver-demos repository to understand how to leverage the framework to automate web applications using Selenium WebDriver