package org.lognavigator.mvc;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogNavigatorWebInitializer {
	
	private static final String APP_VERSION_ATTRIBUTE_NAME = "appVersion";

	@Autowired
	private ServletContext servletContext;

	@Value("${Implementation-Version}")
	private String appVersion;
	
	
	@PostConstruct
	public void initWebAppConfig() {
		servletContext.setAttribute(APP_VERSION_ATTRIBUTE_NAME, appVersion);
	}
}
