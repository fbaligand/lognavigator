package org.lognavigator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ErrorController {

	@RequestMapping("/error/403")
	public void errorAuthorization(HttpServletRequest request) throws AccessDeniedException {
		AccessDeniedException accessDeniedException = (AccessDeniedException) request.getAttribute("SPRING_SECURITY_403_EXCEPTION");
		throw accessDeniedException;
	}

	@RequestMapping("/error/500")
	public void errorTechnical(HttpServletRequest request) throws Throwable {
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
		while (exception.getCause() != null) {
			exception = exception.getCause();
		}
		throw exception;
	}
	
	@RequestMapping("/error/401")
	@ResponseBody
	public String errorAuthentication() {
		return "You must be authenticated to access LogNavigator";
	}
}
