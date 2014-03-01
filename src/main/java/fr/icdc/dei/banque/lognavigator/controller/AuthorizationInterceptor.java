package fr.icdc.dei.banque.lognavigator.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	
	private static String PROTECTED_URI_REGEX = "/logs/.+/(?!prepare-main-view).+";

	@Autowired
	ConfigService configService;

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		// Compute current URI
		String currentUri = request.getRequestURI().substring(request.getContextPath().length());
		
		// Case where current URI is unprotected : access authorized
		if (!currentUri.matches(PROTECTED_URI_REGEX)) {
			return true;
		}
		
		// Get log access config for current URI
		String logAccessConfigId = currentUri.substring("/logs/".length(), currentUri.lastIndexOf('/'));
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Check userName of the connected user
		if (!logAccessConfig.isEveryUserAuthorized()) {
			Principal userPrincipal = request.getUserPrincipal();
			String userName = (userPrincipal != null) ? userPrincipal.getName() : null;

			// Case where user is unauthenticated
			if (userName == null) {
				throw new AuthorizationException("You are not authenticated. Authentication is needed to access to these logs");
			}
			for (String authorizedUser : logAccessConfig.getAuthorizedUsers()) {
				if (authorizedUser.equalsIgnoreCase(userName)) {
					return true;
				}
			}
			// Case where user is not in the authorized users list
			if (logAccessConfig.isEveryRoleAuthorized()) {
				throw new AuthorizationException("Your userName is not authorized to access to these logs");
			}
		}
		
		// Check roles of the connected user
		if (!logAccessConfig.isEveryRoleAuthorized()) {
			for (String authorizedRole : logAccessConfig.getAuthorizedRoles()) {
				if (request.isUserInRole(authorizedRole)) {
					return true;
				}
			}
			// Case where user has no role in the authorized roles list
			throw new AuthorizationException("You don't have any role authorized to access to these logs");
		}
		
		// Case where every user and roles are authorized : access authorized
		return true;
	}

}
