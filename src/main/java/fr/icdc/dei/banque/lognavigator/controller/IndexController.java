package fr.icdc.dei.banque.lognavigator.controller;

import java.text.MessageFormat;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;
import fr.icdc.dei.banque.lognavigator.util.WebConstants;

@Controller
public class IndexController {
	
	@Autowired
	private ConfigService configService;

	@RequestMapping("/index.html")
	public String index(Model model) {
		
		Set<LogAccessConfig> logAccessConfigList = configService.getLogAccessConfigs();
		String logAccessConfigId = logAccessConfigList.iterator().next().getId();
		
		return MessageFormat.format(WebConstants.REDIRECT_LOGS_LIST_CONTROLLER, logAccessConfigId);
	}
}
