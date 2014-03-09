package fr.icdc.dei.banque.lognavigator.service;

import java.util.Set;

import org.springframework.security.core.Authentication;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;

/**
 * Service which managers user authorizations to log access configurations
 */
public interface AuthorizationService {

	/**
	 * Check if <code>authenticatedUser</code> is authorized for <code>logAccessConfig</code>, and raises an exception if not
	 * @param logAccessConfig log access config that user wants to access
	 * @param authenticatedUser current authenticated user
	 * @throws AuthorizationException if user is not authorized for <code>logAccessConfig</code> 
	 */
	void checkUserAuthorizationFor(LogAccessConfig logAccessConfig, Authentication authenticatedUser) throws AuthorizationException;

	/**
	 * Filter <code>allLogAccessConfigs</code> set to return only authorized log access configs for <code>authenticatedUser</code>
	 * @param allLogAccessConfigs complete list of available log access configs  
	 * @param authenticatedUser current authenticated user
	 * @return only authorized log access configs
	 */
	Set<LogAccessConfig> getAuthorizedLogAccessConfigs(Set<LogAccessConfig> allLogAccessConfigs, Authentication authenticatedUser);
}
