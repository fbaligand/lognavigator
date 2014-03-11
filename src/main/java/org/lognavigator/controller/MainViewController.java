package org.lognavigator.controller;

import static org.lognavigator.util.Constants.LOG_ACCESS_CONFIG_IDS_BY_DISPLAY_GROUP_KEY;
import static org.lognavigator.util.Constants.LOG_ACCESS_CONFIG_ID_KEY;
import static org.lognavigator.util.Constants.MAIN_VIEW;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.ConfigException;
import org.lognavigator.service.AuthorizationService;
import org.lognavigator.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainViewController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private AuthorizationService authorizationService;
	

	@RequestMapping("/{logAccessConfigId}/prepare-main-view")
	public String prepareMainView(Model model, @PathVariable String logAccessConfigId) throws ConfigException {
		
		try {
			
			// Get authorized log access configs for current user 
			Authentication authorizedUser = SecurityContextHolder.getContext().getAuthentication();
			Set<LogAccessConfig> allLogAccessConfigs = configService.getLogAccessConfigs();
			Set<LogAccessConfig> authorizedLogAccessConfigs = authorizationService.getAuthorizedLogAccessConfigs(allLogAccessConfigs, authorizedUser);
			model.addAttribute(authorizedLogAccessConfigs);
			
			// Create map <displayGroup> -> <logAccessConfig>
			Map<String, Set<LogAccessConfig>> logAccessConfigsMap = new HashMap<String, Set<LogAccessConfig>>();
			for (LogAccessConfig logAccessConfig : authorizedLogAccessConfigs) {
				String displayGroup = logAccessConfig.getDisplayGroup() != null ? logAccessConfig.getDisplayGroup() : "";
				Set<LogAccessConfig> logAccessConfigIds = logAccessConfigsMap.get(displayGroup);
				if (logAccessConfigIds == null) {
					logAccessConfigIds = new TreeSet<LogAccessConfig>();
					logAccessConfigsMap.put(displayGroup, logAccessConfigIds);
				}
				logAccessConfigIds.add(logAccessConfig);
			}
			
			
			// Inject current logAccessConfigId and logAccessConfigIds map
			model.addAttribute(LOG_ACCESS_CONFIG_ID_KEY, logAccessConfigId);
			model.addAttribute(LOG_ACCESS_CONFIG_IDS_BY_DISPLAY_GROUP_KEY, logAccessConfigsMap);
		}
		catch (ConfigException e) {
			LOGGER.error("Error while loading configuration", e);
		}
		
		// Launch main view
		return MAIN_VIEW;
	}
}
