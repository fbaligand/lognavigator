package org.lognavigator.controller;

import org.lognavigator.util.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class LoginController {
	
	@RequestMapping("/login")
	public String login(Model model)  {
		
		model.addAttribute(Constants.LOGIN_VIEW_KEY, true);
		
		return Constants.VIEW_LOGIN;
	}
}
