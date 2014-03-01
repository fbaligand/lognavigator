package fr.icdc.dei.banque.lognavigator.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.ConfigException;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;
import fr.icdc.dei.banque.lognavigator.util.WebConstants;

@Controller
public class MainViewController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

	@Autowired
	private ConfigService configService;

	@RequestMapping("/{logAccessConfigId}/prepare-main-view")
	public String prepareMainView(Model model) throws ConfigException {
		
		// Set nav bar data
		try {
			Set<LogAccessConfig> logAccessConfigList = configService.getLogAccessConfigs();
			model.addAttribute(logAccessConfigList);
		}
		catch (ConfigException e) {
			LOGGER.error("Error while loading configuration", e);
		}
		
		// Launch main view
		return WebConstants.MAIN_VIEW;
	}
}
