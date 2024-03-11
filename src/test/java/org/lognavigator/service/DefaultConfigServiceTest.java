package org.lognavigator.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.xml.bind.JAXBContext;

import org.junit.Before;
import org.junit.Test;
import org.lognavigator.bean.LogNavigatorConfig;
import org.lognavigator.exception.ConfigException;
import org.springframework.core.io.ClassPathResource;


public class DefaultConfigServiceTest {
	
	private DefaultConfigService configService = new DefaultConfigService();
	
	@Before
	public void setup() throws Exception {
		configService.logNavigatorConfigJaxbContext = JAXBContext.newInstance(LogNavigatorConfig.class);
	}

	@Test
	public void reloadLogNavigatorConfigIfNecessary_OK() {
		
		// Init data
		configService.logNavigatorConfigResource = new ClassPathResource("lognavigator.xml");

		// Execute test
		configService.reloadLogNavigatorConfigIfNecessary();

		// Assertions
		assertNotNull(configService.logAccessConfigs);
		assertFalse(configService.logAccessConfigs.isEmpty());
	}

	@Test(expected=ConfigException.class)
	public void reloadLogNavigatorConfigIfNecessary_ConfigNotFound() {
		configService.logNavigatorConfigResource = new ClassPathResource("lognavigator-unfound.xml");
		configService.reloadLogNavigatorConfigIfNecessary();
	}
	
	@Test(expected=ConfigException.class)
	public void reloadLogNavigatorConfigIfNecessary_InvalidOptionsInConfig() {
		configService.logNavigatorConfigResource = new ClassPathResource("lognavigator-invalid-ssh.xml");
		configService.reloadLogNavigatorConfigIfNecessary();
	}
	
	@Test(expected=ConfigException.class)
	public void reloadLogNavigatorConfigIfNecessary_InvalidTypeInConfig() {
		configService.logNavigatorConfigResource = new ClassPathResource("lognavigator-invalid-type.xml");
		configService.reloadLogNavigatorConfigIfNecessary();
	}
	
}
