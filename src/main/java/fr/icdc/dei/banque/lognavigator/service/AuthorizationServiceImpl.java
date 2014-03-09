package fr.icdc.dei.banque.lognavigator.service;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

	@Override
	public void checkUserAuthorizationFor(LogAccessConfig logAccessConfig, Authentication authenticatedUser) throws AuthorizationException {
		
		// Check userName of the connected user
		if (!logAccessConfig.isEveryUserAuthorized()) {
			String userName = (authenticatedUser != null) ? authenticatedUser.getName() : null;

			// Case where user is unauthenticated
			if (userName == null) {
				throw new AuthorizationException("You are not authenticated. Authentication is needed to access to these logs");
			}
			for (String authorizedUser : logAccessConfig.getAuthorizedUsers()) {
				if (authorizedUser.equalsIgnoreCase(userName)) {
					return;
				}
			}
			// Case where user is not in the authorized users list
			if (logAccessConfig.isEveryRoleAuthorized()) {
				throw new AuthorizationException("Your userName is not authorized to access to these logs");
			}
		}
		
		// Check roles of the connected user
		if (!logAccessConfig.isEveryRoleAuthorized()) {
			if (authenticatedUser != null) {
				for (String authorizedRole : logAccessConfig.getAuthorizedRoles()) {
					for (GrantedAuthority userRole : authenticatedUser.getAuthorities()) {
						if (authorizedRole.equals(userRole.getAuthority())) {
							return;
						}
					}
				}
			}
			// Case where user has no role in the authorized roles list
			throw new AuthorizationException("You don't have any role authorized to access to these logs");
		}
	}
	
	@Override
	public Set<LogAccessConfig> getAuthorizedLogAccessConfigs(Set<LogAccessConfig> allLogAccessConfigs, Authentication authenticatedUser) {
		
		Set<LogAccessConfig> authorizedLogAccessConfigs = new TreeSet<LogAccessConfig>();
		
		for (LogAccessConfig logAccessConfig : allLogAccessConfigs) {
			try {
				checkUserAuthorizationFor(logAccessConfig, authenticatedUser);
				authorizedLogAccessConfigs.add(logAccessConfig);
			}
			catch (AuthorizationException e) {
				// Do not add to authorized log access configs
			}
		}
		
		// Return filtered authorized log access configs for authenticatedUser
		return authorizedLogAccessConfigs;
	}

}
