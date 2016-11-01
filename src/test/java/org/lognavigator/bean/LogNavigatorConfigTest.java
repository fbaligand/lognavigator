package org.lognavigator.bean;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;

import org.junit.Test;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogNavigatorConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;

import static org.junit.Assert.*;



public class LogNavigatorConfigTest {

	@Test
	public void testXmlMarshalling() throws Exception {
		
		// Create jaxb data
		LogNavigatorConfig logNavigatorConfig = new LogNavigatorConfig();
		logNavigatorConfig.getLogAccessConfigs().add(new LogAccessConfig("log-access1", LogAccessType.LOCAL, "localhost", "/logs", "Fabien"));
		logNavigatorConfig.getLogAccessConfigs().add(new LogAccessConfig("log-access2", LogAccessType.LOCAL, "localhost", "/logs", "Fabien"));
		
		// Marshall to xml
		JAXBContext jaxbContext = JAXBContext.newInstance(LogNavigatorConfig.class);
		StringWriter xmlOutput = new StringWriter();
		jaxbContext.createMarshaller().marshal(logNavigatorConfig, xmlOutput);
		System.out.println(xmlOutput);
		
		// Assertions
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><lognavigator-config><log-access-config id=\"log-access1\" type=\"LOCAL\" user=\"Fabien\" host=\"localhost\" directory=\"/logs\" authorized-users=\"*\" authorized-roles=\"*\" trust=\"false\"/><log-access-config id=\"log-access2\" type=\"LOCAL\" user=\"Fabien\" host=\"localhost\" directory=\"/logs\" authorized-users=\"*\" authorized-roles=\"*\" trust=\"false\"/></lognavigator-config>";
		if (System.getProperty("java.version").startsWith("1.6.")) {
			expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><lognavigator-config><log-access-config trust=\"false\" authorized-roles=\"*\" authorized-users=\"*\" directory=\"/logs\" host=\"localhost\" user=\"Fabien\" type=\"LOCAL\" id=\"log-access1\"/><log-access-config trust=\"false\" authorized-roles=\"*\" authorized-users=\"*\" directory=\"/logs\" host=\"localhost\" user=\"Fabien\" type=\"LOCAL\" id=\"log-access2\"/></lognavigator-config>";
		}
		assertEquals(expectedXml, xmlOutput.toString());
	}

	@Test
	public void testXmlUnmarshalling() throws Exception {
		
		// Get XML stream
		InputStream xmlInputStream = getClass().getResourceAsStream("/lognavigator-with-authorized-users.xml");
		
		// Unmarshall to bean
		JAXBContext jaxbContext = JAXBContext.newInstance(LogNavigatorConfig.class);
		LogNavigatorConfig logNavigatorConfig = (LogNavigatorConfig) jaxbContext.createUnmarshaller().unmarshal(xmlInputStream);
		
		// Assertions
		assertNotNull(logNavigatorConfig);
		LogAccessConfig firstLogAccessConfig = logNavigatorConfig.getLogAccessConfigs().iterator().next();
		assertEquals("config-with-users", firstLogAccessConfig.getId());
		assertEquals(LogAccessType.LOCAL, firstLogAccessConfig.getType());
		assertEquals("target/test-classes/files", firstLogAccessConfig.getDirectory());
		assertEquals(2, firstLogAccessConfig.getAuthorizedUsers().size());
	}

}
