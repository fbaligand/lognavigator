package fr.icdc.dei.banque.lognavigator.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;
import fr.icdc.dei.banque.lognavigator.bean.LogNavigatorConfig;

public class LogNavigatorPropsConverter {

	public static void main(String[] args) throws Exception {
		
		File inputFile = new File("target/test-classes/remote-logs.properties");
		File outputFile = new File("target/lognavigator.xml");
		
		// Load input properties
		FileInputStream inputStream = new FileInputStream(inputFile);
		Properties inputProperties = new Properties();
		inputProperties.load(inputStream);
		inputStream.close();
		
		// Load bean configuration from input properties
		LogNavigatorConfig logNavigatorConfig = loadLogNavigatorConfig(inputProperties);
		
		// Serialize XML configuration
		Marshaller marshaller = JAXBContext.newInstance(LogNavigatorConfig.class).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(logNavigatorConfig, outputFile);
		
		// Convertion finished !
		System.out.println("Sucessfully converted " + inputFile + " to " + outputFile);
	}

	/**
	 * Build LogNavigator bean configuration from properties configuration
	 * @param logNavigatorConfigProps lognavigator properties configuration
	 * @return loaded configuration 
	 */
	private static LogNavigatorConfig loadLogNavigatorConfig(Properties logNavigatorConfigProps) {
		
		LogNavigatorConfig logNavigatorConfig = new LogNavigatorConfig();
		
		for (String propertyName : logNavigatorConfigProps.stringPropertyNames()) {
			String connectionString = logNavigatorConfigProps.getProperty(propertyName);
			String user = connectionString.substring(0, connectionString.indexOf('@'));
			String hostOrUrl = connectionString.substring(connectionString.indexOf('@') + 1, connectionString.indexOf(':'));
			boolean isUrl = hostOrUrl.equals("http") || hostOrUrl.equals("https");
			
			// Define log access type
			LogAccessType type;
			if (hostOrUrl.equals("localhost")) {
				type = LogAccessType.LOCAL;
				hostOrUrl = null;
				user = null;
			}
			else if (isUrl) {
				hostOrUrl = connectionString.substring(connectionString.indexOf('@') + 1);
				type = LogAccessType.HTTPD;
				user = null;
			}
			else {
				type = LogAccessType.SSH;
			}
			
			String directory = null;
			if (type != LogAccessType.HTTPD) {
				directory = connectionString.substring(connectionString.indexOf(':') + 1);
				if (directory.length() > 1 && (directory.endsWith("/") || directory.endsWith("\\"))) {
					directory = directory.substring(0, directory.length()-1);
				}
			}
			
			// Create and add log access config bean
			LogAccessConfig logAccessConfig;
			if (type == LogAccessType.HTTPD) {
				logAccessConfig = new LogAccessConfig(propertyName, type, hostOrUrl, user);
			}
			else {
				logAccessConfig = new LogAccessConfig(propertyName, type, hostOrUrl, directory, user);
			}
			logNavigatorConfig.getLogAccessConfigs().add(logAccessConfig);
		}
		
		// return loaded configuration
		return logNavigatorConfig;
	}
}
