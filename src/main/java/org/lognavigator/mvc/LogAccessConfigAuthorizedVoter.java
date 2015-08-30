package org.lognavigator.mvc;

import java.util.Collection;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.exception.ConfigException;
import org.lognavigator.service.AuthorizationService;
import org.lognavigator.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

/**
 * AccessDecisionVoter (Spring Security Component) which computes and returns
 * if current user is authorized to the requested log access config (which id is in URI).
 * If user is not authorized, AccessDeniedException is thrown. 
 */
public class LogAccessConfigAuthorizedVoter implements AccessDecisionVoter<FilterInvocation> {

	public static final String IS_AUTHORIZED_LOG_ACCESS_CONFIG = "IS_AUTHORIZED_LOG_ACCESS_CONFIG";
	

	@Autowired
	ConfigService configService;

	@Autowired
	AuthorizationService authorizationService;
	
	
	@Override
	public boolean supports(ConfigAttribute attribute) {
		if (IS_AUTHORIZED_LOG_ACCESS_CONFIG.equals(attribute.getAttribute())) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> attributes) {

		for (ConfigAttribute attribute : attributes) {
			if (this.supports(attribute)) {
				
				// Compute current request URI
				String requestUri = filterInvocation.getRequestUrl();
				
				// Get log access config id for current request URI
				String logAccessConfigId = requestUri.substring("/logs/".length(), requestUri.lastIndexOf('/'));
		
				// Check user authorizations for logAccessConfig
				try {
					LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
					authorizationService.checkUserAuthorizationFor(logAccessConfig, authentication);
				}
				catch (AuthorizationException e) {
					throw new AccessDeniedException(e.getMessage(), e);
				}
				catch (ConfigException e) {
					return ACCESS_ABSTAIN;
				}
				
				// Case where every user and roles are authorized : access authorized
				return ACCESS_GRANTED;
			}
		}
		
		// Case where no 'IS_AUTHORIZED_LOG_ACCESS_CONFIG' attribute was found
		return ACCESS_ABSTAIN;
	}

}
