package fr.icdc.dei.banque.lognavigator.service;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import fr.icdc.dei.banque.lognavigator.bean.LogNavigatorConfig;
import fr.icdc.dei.banque.lognavigator.exception.ConfigException;

public class ConfigServiceImplTest {
	
	private ConfigServiceImpl configService = new ConfigServiceImpl();
	
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
