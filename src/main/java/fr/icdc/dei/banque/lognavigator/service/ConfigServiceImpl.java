package fr.icdc.dei.banque.lognavigator.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;
import fr.icdc.dei.banque.lognavigator.bean.LogNavigatorConfig;
import fr.icdc.dei.banque.lognavigator.exception.ConfigException;

@Service
public class ConfigServiceImpl implements ConfigService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);
	private static final String DEFAULT_LOGNAVIGATOR_CONFIG_LOCATION = "classpath:lognavigator.xml";
	
	Set<LogAccessConfig> logAccessConfigs;
	long logNavigatorConfigLastModified;

	@Value("${lognavigator/config:" + DEFAULT_LOGNAVIGATOR_CONFIG_LOCATION + "}")
	Resource logNavigatorConfigResource;
	JAXBContext logNavigatorConfigJaxbContext;

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
	 * Load or Reload the set of LogAccessConfig beans, from lognavigator config file
	 */
	synchronized void reloadLogNavigatorConfigIfNecessary() throws ConfigException {
		
		// Does config file exist ?
		if (!logNavigatorConfigResource.exists()) {
			throw new ConfigException("The config file " + logNavigatorConfigResource + " does not exist");
		}
		
		// Should we reload config file ? (because it has been modified since last reload)
		long lastModified;
		try {
			lastModified = logNavigatorConfigResource.lastModified();
			boolean needReload = (lastModified > this.logNavigatorConfigLastModified);
			if (!needReload) {
				return;
			}
		} catch (IOException e) {
			throw new ConfigException("Error when trying to access lognavigator config file " + logNavigatorConfigResource, e);
		}
		
		// Load lognavigator XML configuration
		InputStream logNavigatorConfigInputStream = null;
		try {
			logNavigatorConfigInputStream = logNavigatorConfigResource.getInputStream();
			Unmarshaller unmarshaller = logNavigatorConfigJaxbContext.createUnmarshaller();
			LogNavigatorConfig logNavigatorConfig = (LogNavigatorConfig) unmarshaller.unmarshal(logNavigatorConfigInputStream);
			this.logAccessConfigs = logNavigatorConfig.getLogAccessConfigs();
		}
		catch (IOException e) {
			throw new ConfigException("I/O error when trying to load lognavigator config file " + logNavigatorConfigResource, e);
		}
		catch (JAXBException e) {
			throw new ConfigException("XML load error when trying to load lognavigator config file " + logNavigatorConfigResource, e);
		}
		finally {
			if (logNavigatorConfigInputStream != null) {
				try {
					logNavigatorConfigInputStream.close();
				} catch (IOException e) {
					// Silently close the stream
				}
			}
		}
		
		validateConfiguration();
		
		// Update the lastModified date information for config file
		this.logNavigatorConfigLastModified = lastModified;
	}

	/**
	 * Validate loaded configuration
	 * @throws ConfigException if configuration is invalid
	 */
	synchronized void validateConfiguration() throws ConfigException {
		
		// Case where configuration is empty => config error
		if (this.logAccessConfigs.isEmpty()) {
			throw new ConfigException("lognavigator config file " + logNavigatorConfigResource + " is empty : at least one configuration must be defined");
		}
		
		for (LogAccessConfig logAccessConfig : this.logAccessConfigs) {
			
			if (logAccessConfig.getType() == null) {
				throw new ConfigException("unknown type for log-access-config '" + logAccessConfig.getId() + "'. Valid values are : " + Arrays.asList(LogAccessType.values()));
			}

			switch (logAccessConfig.getType()) {
			case LOCAL:
				if (StringUtils.isEmpty(logAccessConfig.getDirectory())) {
					throw new ConfigException("'directory' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				break;
			case HTTPD:
				if (StringUtils.isEmpty(logAccessConfig.getUrl())) {
					throw new ConfigException("'url' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				break;
			case SSH:
				if (StringUtils.isEmpty(logAccessConfig.getUser())) {
					throw new ConfigException("'user' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				if (StringUtils.isEmpty(logAccessConfig.getHost())) {
					throw new ConfigException("'host' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				if (StringUtils.isEmpty(logAccessConfig.getDirectory())) {
					throw new ConfigException("'directory' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				break;
			default:
				throw new IllegalStateException("unmanaged log access config type : " + logAccessConfig.getType() + "'");
			}
		}
	}
	
	/**
	 * Init the Spring Service
	 */
	@PostConstruct
	public synchronized void init() {
		try {
			logNavigatorConfigJaxbContext = JAXBContext.newInstance(LogNavigatorConfig.class);
			reloadLogNavigatorConfigIfNecessary();
		}
		catch (JAXBException e) {
			LOGGER.error("Error while loading configuration file {}", logNavigatorConfigResource, e);
		}
		catch (ConfigException e) {
			LOGGER.error("Error while loading configuration file {}", logNavigatorConfigResource, e);
		}
	}
}
