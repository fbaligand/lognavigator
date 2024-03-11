package org.lognavigator.service;

import static org.lognavigator.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.schmizz.sshj.common.IOUtils;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogNavigatorConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
public class DefaultConfigService implements ConfigService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigService.class);
	
	private static final String DEFAULT_LOGNAVIGATOR_CONFIG_LOCATION = "classpath:lognavigator.xml";
	private static final int DEFAULT_FILE_LIST_MAX_COUNT = 1000;
	private static final boolean DEFAULT_FILE_LIST_BLOCK_EXTERNAL_PATHS = false;
	

	/** All log access configurations loaded from lognavigator.xml */
	Set<LogAccessConfig> logAccessConfigs;
	
	/** last time that lognavigator.xml was modified */
	long logNavigatorConfigLastModified;

	/** JAXBContext used to load lognavigator.xml content */
	JAXBContext logNavigatorConfigJaxbContext;

	/** lognavigator main configuration location */
	@Value("${lognavigator.config:" + DEFAULT_LOGNAVIGATOR_CONFIG_LOCATION + "}")
	Resource logNavigatorConfigResource;
	
	/** max file count in screen listing files */
	@Value("${filelist.maxcount:" + DEFAULT_FILE_LIST_MAX_COUNT + "}")
	int fileListMaxCount; 
	
	/** is block external paths enabled */
	@Value("${filelist.blockexternalpaths:" + DEFAULT_FILE_LIST_BLOCK_EXTERNAL_PATHS + "}")
	private boolean fileListBlockExternalPaths;
	
	/** forbidden commands list */
	@Value("${forbidden.commands:" + DEFAULT_FORBIDDEN_COMMANDS + "}")
	String forbiddenCommands = DEFAULT_FORBIDDEN_COMMANDS;
	
	/** default encoding used to read command output */
	@Value("${default.encoding:" + DEFAULT_ENCODING_OPTION + "}")
	String defaultEncoding = DEFAULT_ENCODING_OPTION;


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
		throw new ConfigException("logAccessConfigId '" + id + "' doesn't correspond to any known log access config");
	}

	@Override
	public int getFileListMaxCount() {
		return fileListMaxCount;
	}

	@Override
	public boolean getFileListBlockExternalPaths() {
		return fileListBlockExternalPaths;
	}

	@Override
	public String getForbiddenCommands() {
		return forbiddenCommands;
	}

	@Override
	public String getDefaultEncoding(String logAccessConfigId) {
		LogAccessConfig logAccessConfig = getLogAccessConfig(logAccessConfigId);
		if (logAccessConfig.getDefaultEncoding() != null) {
			return logAccessConfig.getDefaultEncoding();
		}
		else {
			return defaultEncoding;
		}
	}
	
	/**
	 * Load or Reload the set of LogAccessConfig beans, from lognavigator config file
	 */
	synchronized void reloadLogNavigatorConfigIfNecessary() throws ConfigException {
		
		// Does config file exist ?
		if (!logNavigatorConfigResource.exists()) {
			throw new ConfigException("The config file '" + logNavigatorConfigResource + "' does not exist");
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
			throw new ConfigException("Error when trying to access lognavigator config file '" + logNavigatorConfigResource + "'", e);
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
			throw new ConfigException("I/O error when trying to load lognavigator config file '" + logNavigatorConfigResource + "'", e);
		}
		catch (JAXBException e) {
			throw new ConfigException("XML load error when trying to load lognavigator config file '" + logNavigatorConfigResource + "'", e);
		}
		finally {
			IOUtils.closeQuietly(logNavigatorConfigInputStream);
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
			throw new ConfigException("lognavigator config file '" + logNavigatorConfigResource + "' is empty: at least one configuration must be defined");
		}
		
		for (LogAccessConfig logAccessConfig : this.logAccessConfigs) {
			
			if (logAccessConfig.getType() == null) {
				throw new ConfigException("unknown type for log-access-config '" + logAccessConfig.getId() + "'. Valid values are: " + Arrays.asList(LogAccessType.values()));
			}

			switch (logAccessConfig.getType()) {
			case LOCAL:
				if (!StringUtils.hasText(logAccessConfig.getDirectory())) {
					throw new ConfigException("'directory' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				File directoryFile = new File(logAccessConfig.getDirectory());
				if (!directoryFile.isAbsolute()) {
					logAccessConfig.setDirectory(directoryFile.getAbsolutePath());
				}
				break;
			case HTTPD:
				if (!StringUtils.hasText(logAccessConfig.getUrl())) {
					throw new ConfigException("'url' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				if (!logAccessConfig.getUrl().endsWith("/")) {
					logAccessConfig.setUrl(logAccessConfig.getUrl() + "/");
				}
				break;
			case SSH:
				if (!StringUtils.hasText(logAccessConfig.getUser())) {
					throw new ConfigException("'user' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				if (!StringUtils.hasText(logAccessConfig.getHost())) {
					throw new ConfigException("'host' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				if (!StringUtils.hasText(logAccessConfig.getDirectory())) {
					throw new ConfigException("'directory' attribute must be defined for log-access-config '" + logAccessConfig.getId() + "'");
				}
				break;
			default:
				throw new IllegalStateException("unmanaged log access config type: '" + logAccessConfig.getType() + "'");
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
