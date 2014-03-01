package fr.icdc.dei.banque.lognavigator.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;
import fr.icdc.dei.banque.lognavigator.exception.ConfigException;

//@Service
public class ConfigServicePropsImpl implements ConfigService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServicePropsImpl.class);
	private static final String LOGNAVIGATOR_CONFIG_PATH = "/remote-logs.properties";
	
	private Set<LogAccessConfig> logAccessConfigs;
	private long logNavigatorConfigLastModified;

	@Override
	public synchronized Set<LogAccessConfig> getLogAccessConfigs() throws ConfigException {
		
		reloadLogNavigatorConfigIfNecessary();

		return logAccessConfigs;
	}
	
	@Override
	public synchronized LogAccessConfig getLogAccessConfig(String id) throws ConfigException {
		
		// Search the LogAccessConfig with the param id
		for (LogAccessConfig logAccessConfig : getLogAccessConfigs()) {
			if (logAccessConfig.getId().equals(id)) {
				return logAccessConfig;
			}
		}
		
		// No LogAccessConfig found
		throw new ConfigException("logAccessConfigId " + id + " doesn't correspond to any known log access config");
	}
	
	/**
	 * Load or Reload the set of LogAccessConfig beans, from file {@value #LOGNAVIGATOR_CONFIG_PATH}
	 */
	private synchronized void reloadLogNavigatorConfigIfNecessary() throws ConfigException {
		
		// Does config file exist ?
		URL logNavigatorConfigUrl = this.getClass().getResource(LOGNAVIGATOR_CONFIG_PATH);
		if (logNavigatorConfigUrl == null) {
			throw new ConfigException("The config file " + LOGNAVIGATOR_CONFIG_PATH + " does not exist in classpath");
		}
		
		// Should we reload config file ? (because it has changed since last load)
		File logNavigatorConfigFile = new File(logNavigatorConfigUrl.getPath());
		boolean needReload = (logNavigatorConfigFile.lastModified() > this.logNavigatorConfigLastModified);
		if (!needReload) {
			return;
		}
		
		// Load log navigator config file
		Properties logNavigatorConfigProps = new Properties();
		InputStream logNavigatorConfigIS = this.getClass().getResourceAsStream(LOGNAVIGATOR_CONFIG_PATH);
		try {
			logNavigatorConfigProps.load(logNavigatorConfigIS);
		} catch (IOException e) {
			throw new ConfigException("Impossible to load lognavigator config file " + LOGNAVIGATOR_CONFIG_PATH, e);
		}
		finally {
			try {
				logNavigatorConfigIS.close();
			} catch (IOException e) {}
		}

		// Case where configuration is empty => config error
		if (logNavigatorConfigProps.isEmpty()) {
			throw new ConfigException("config file " + LOGNAVIGATOR_CONFIG_PATH + " is empty : at least one configuration must be defined");
		}
		
		// Build set of LogAccessConfig beans
		this.logAccessConfigs = new TreeSet<LogAccessConfig>();
		for (String propertyName : logNavigatorConfigProps.stringPropertyNames()) {
			String connectionString = logNavigatorConfigProps.getProperty(propertyName);
			String user = connectionString.substring(0, connectionString.indexOf('@'));
			String hostOrUrl = connectionString.substring(connectionString.indexOf('@') + 1, connectionString.indexOf(':'));
			boolean isUrl = hostOrUrl.equals("http") || hostOrUrl.equals("https");
			LogAccessType type;
			if (hostOrUrl.equals("localhost")) {
				type = LogAccessType.LOCAL;
			}
			else if (isUrl) {
				hostOrUrl = connectionString.substring(connectionString.indexOf('@') + 1);
				type = LogAccessType.HTTPD;
			}
			else {
				type = LogAccessType.SSH;
			}
			String directory = connectionString.substring(connectionString.indexOf(':') + 1);
			if (isUrl) {
				directory = "";
			}
			if (directory.length() > 1 && (directory.endsWith("/") || directory.endsWith("\\"))) {
				directory = directory.substring(0, directory.length()-1);
			}
			LogAccessConfig logAccessConfig;
			if (isUrl) {
				logAccessConfig = new LogAccessConfig(propertyName, type, hostOrUrl, user);
			}
			else {
				logAccessConfig = new LogAccessConfig(propertyName, type, hostOrUrl, directory, user);
			}
			logAccessConfigs.add(logAccessConfig);
		}
		
		// Update the lastModified date information
		this.logNavigatorConfigLastModified = logNavigatorConfigFile.lastModified();
	}
	
	/**
	 * Init the Spring Service
	 */
	@PostConstruct
	public void init() {
		try {
			reloadLogNavigatorConfigIfNecessary();
		}
		catch (ConfigException e) {
			LOGGER.error("Error while loading configuration file {}", LOGNAVIGATOR_CONFIG_PATH, e);
		}
	}
}
