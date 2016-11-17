package com.autopia4j.framework.webdriver.utils;

/**
 * Class to encapsulate the proxy server settings for WebDriver
 * @author vj
 */
public class WebDriverProxy {
	private String host;
	private int port;
	private Boolean authRequired;
	private String domain;
	private String userName;
	private String password;
	
	/**
	 * Function to get the proxy host
	 * @return The proxy host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * Function to set the proxy host
	 * @param host The proxy host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Function to get the proxy port
	 * @return The proxy port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * Function to set the proxy port
	 * @param port The proxy port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Function to get a Boolean variable indicating whether proxy authentication is required
	 * @return A Boolean variable indicating whether proxy authentication is required
	 */
	public Boolean isAuthRequired() {
		return authRequired;
	}
	/**
	 * Function to set a Boolean variable indicating whether proxy authentication is required
	 * @param authRequired A Boolean variable indicating whether proxy authentication is required
	 */
	public void setAuthRequired(Boolean authRequired) {
		this.authRequired = authRequired;
	}
	
	/**
	 * Function to get the proxy authentication domain
	 * @return The proxy authentication domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * Function to set the proxy authentication domain
	 * @param domain The proxy authentication domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * Function to get the proxy authentication username
	 * @return The proxy authentication username
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * Function to set the proxy authentication username
	 * @param userName The proxy authentication username
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Function to get the proxy authentication password
	 * @return The proxy authentication password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * Function to set the proxy authentication password
	 * @param password The proxy authentication password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}