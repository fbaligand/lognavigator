package fr.icdc.dei.banque.lognavigator.controller;

import java.text.MessageFormat;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;
import fr.icdc.dei.banque.lognavigator.service.AuthorizationService;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;
import fr.icdc.dei.banque.lognavigator.util.Constants;

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
