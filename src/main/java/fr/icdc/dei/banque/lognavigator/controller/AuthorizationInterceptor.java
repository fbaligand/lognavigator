package fr.icdc.dei.banque.lognavigator.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;
import fr.icdc.dei.banque.lognavigator.service.AuthorizationService;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	
	private static String PROTECTED_URI_REGEX = "/logs/.+/(?!prepare-main-view).+";

	@Autowired
	ConfigService configService;

	@Autowired
	AuthorizationService authorizationService;
	

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws AuthorizationException {

		// Get current authenticated user information
		Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
		
		// Compute current URI
		String currentUri = request.getRequestURI().substring(request.getContextPath().length());
		
		// Case where current URI is unprotected : access authorized
		if (!currentUri.matches(PROTECTED_URI_REGEX)) {
			return true;
		}
		
		// Get log access config for current URI
		String logAccessConfigId = currentUri.substring("/logs/".length(), currentUri.lastIndexOf('/'));
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);

		// Check user authorizations for logAccessConfig
		authorizationService.checkUserAuthorizationFor(logAccessConfig, authenticatedUser);
		
		// Case where every user and roles are authorized : access authorized
		return true;
	}

}
