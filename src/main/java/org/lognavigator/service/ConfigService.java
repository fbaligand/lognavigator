package org.lognavigator.service;

import java.util.Set;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.ConfigException;


/**
 * Service which manages application configuration
 */
public interface ConfigService {

	/**
	 * First reload logAccessConfigs if necessary.
	 * Then return the set of LogAccessConfig beans
	 * @return set of all LogAccessConfig beans
	 * @throws ConfigException when an error occurs while loading configuration (ex: bad configuration)
	 */
	public Set<LogAccessConfig> getLogAccessConfigs() throws ConfigException;

	/**
	 * Gets and return the LogAccessConfig corresponding to <code>id</code> param
	 * @param id LogAccessConfig id wanted
	 * @return LogAccessConfig bean corresponding to <code>id</code> param
	 * @throws ConfigException when no LogAccessConfig corresponds to the requested id
	 */
	public LogAccessConfig getLogAccessConfig(String id) throws ConfigException;
	
	/**
	 * Return configured maximum file count displayed in the file list screen
	 */
	public int getFileListMaxCount();
	
	/**
	 * Return if external paths are blocked in the file list screen
	 */
	public boolean getFileListBlockExternalPaths();

	/**
	 * Return configured forbidden commands list
	 */
	public String getForbiddenCommands();
	
	/**
	 * Retrieves and returns the default encoding for provided log access config.
	 * First looks in logAccessConfig, then in environment configuration, and by default returns UTF-8
	 * @param id LogAccessConfig id wanted
	 * @return requested default encoding (UTF-8 or ISO-8859-1)
	 */
	public String getDefaultEncoding(String logAccessConfigId);
}