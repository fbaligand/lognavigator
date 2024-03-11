package org.lognavigator.service;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.AuthorizationException;
import org.springframework.security.authentication.TestingAuthenticationToken;


public class DefaultAuthorizationServiceTest {
	
	private AuthorizationService authorizationService = new DefaultAuthorizationService();
	
	@Test
	public void testCheckUserAuthorizationFor_Authorized() throws Exception {
		
		// given
		LogAccessConfig logAccessConfig = new LogAccessConfig("log-with-onerole-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		
		// when
		authorizationService.checkUserAuthorizationFor(logAccessConfig, authenticatedUser);
		
		// then
		// No AuthorizationException
	}

	@Test(expected=AuthorizationException.class)
	public void testCheckUserAuthorizationFor_NotAuthorized() throws Exception {
		
		// given
		LogAccessConfig logAccessConfig = new LogAccessConfig("log-with-onerole-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "not-authorized-role");
		
		// when
		authorizationService.checkUserAuthorizationFor(logAccessConfig, authenticatedUser);
		
		// then
		// raise AuthorizationException
	}
	
	@Test
	public void testGetAuthorizedLogAccessConfigs() throws Exception {
		
		// given
		Set<LogAccessConfig> allLogAccessConfigs = new HashSet<LogAccessConfig>();
		LogAccessConfig logAccessConfig = new LogAccessConfig("log-with-onerole-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		allLogAccessConfigs.add(logAccessConfig);
		logAccessConfig = new LogAccessConfig("log-with-oneuser-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedUsers(Arrays.asList("oneuser"));
		allLogAccessConfigs.add(logAccessConfig);
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");

		// when
		Set<LogAccessConfig> authorizedLogAccessConfigs = authorizationService.getAuthorizedLogAccessConfigs(allLogAccessConfigs, authenticatedUser);
		
		// then
		assertEquals(1, authorizedLogAccessConfigs.size());
		assertEquals("log-with-onerole-authorized", authorizedLogAccessConfigs.iterator().next().getId());
	}

}
