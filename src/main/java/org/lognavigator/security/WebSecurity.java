package org.lognavigator.security;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.exception.ConfigException;
import org.lognavigator.service.AuthorizationService;
import org.lognavigator.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * Component that provides methods for LogNavigator app web security
 */
public class WebSecurity {

	@Autowired
	ConfigService configService;

	@Autowired
	AuthorizationService authorizationService;
	

	/**
	 * Computes and returns if current user is authorized to the requested log access config id.
	 * Returns true if user is authorized.
	 * Raises AccessDeniedException if user is not authorized.
	 */
	public boolean hasPermission(Authentication authentication, String logAccessConfigId) throws AccessDeniedException {
		
		try {
			// Check user authorizations for logAccessConfig
			LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
			authorizationService.checkUserAuthorizationFor(logAccessConfig, authentication);

			// Case where every user is authorized : access authorized
			return true;
		}
		catch (AuthorizationException e) {
			throw new AccessDeniedException(e.getMessage(), e);
		}
		catch (ConfigException e) {
			throw new AccessDeniedException("'" + logAccessConfigId + "' does not exist", e);
		}
	}
	
}
