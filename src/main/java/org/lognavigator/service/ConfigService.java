package org.lognavigator.service;

import java.util.Set;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.ConfigException;


/**
 * Service which manages application configuration
 */
public interface ConfigService {

	/**
	 * First Reload logAccessConfigs if necessary.
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

}