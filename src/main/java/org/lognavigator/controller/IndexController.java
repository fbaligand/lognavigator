package org.lognavigator.controller;

import java.text.MessageFormat;
import java.util.Set;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.service.AuthorizationService;
import org.lognavigator.service.ConfigService;
import org.lognavigator.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {
	
	@Autowired
	private ConfigService configService;

	@Autowired
	private AuthorizationService authorizationService;
	
	
	@RequestMapping("/index.html")
	public String index(Model model) throws AuthorizationException {

		// Get only authorized log access configs
		Authentication authorizedUser = SecurityContextHolder.getContext().getAuthentication();
		Set<LogAccessConfig> allLogAccessConfigs = configService.getLogAccessConfigs();
		Set<LogAccessConfig> authorizedLogAccessConfigs = authorizationService.getAuthorizedLogAccessConfigs(allLogAccessConfigs, authorizedUser);

		// If no authorized log access config => show authorization error to client
		if (authorizedLogAccessConfigs.isEmpty()) {
			throw new AuthorizationException("You are not authorized to any log access configuration");
		}
		
		// Else redirect to first logAccessConfig
		String logAccessConfigId = authorizedLogAccessConfigs.iterator().next().getId();
		return MessageFormat.format(Constants.REDIRECT_LOGS_LIST_CONTROLLER, logAccessConfigId);
	}
}
