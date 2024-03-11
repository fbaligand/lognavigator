package org.lognavigator.security;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.service.AuthorizationService;
import org.lognavigator.service.ConfigService;
import org.lognavigator.service.DefaultAuthorizationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;

@RunWith(MockitoJUnitRunner.class)
public class WebSecurityTest {

	@Spy
	private AuthorizationService authorizationService = new DefaultAuthorizationService();

	@Mock
	private ConfigService configService;
	
	@InjectMocks
	private WebSecurity webSecurity;

	@Before
	public void setUp() throws Exception {
		
		// Mock LogNavigator configuration
		LogAccessConfig logAccessConfig = new LogAccessConfig("log-with-everyone-authorized", LogAccessType.LOCAL, "localhost", "/log");
		when(configService.getLogAccessConfig("log-with-everyone-authorized")).thenReturn(logAccessConfig);

		logAccessConfig = new LogAccessConfig("log-with-oneuser-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedUsers(Arrays.asList("oneuser"));
		when(configService.getLogAccessConfig("log-with-oneuser-authorized")).thenReturn(logAccessConfig);
		
		logAccessConfig = new LogAccessConfig("log-with-onerole-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		when(configService.getLogAccessConfig("log-with-onerole-authorized")).thenReturn(logAccessConfig);
		
		logAccessConfig = new LogAccessConfig("log-with-onerole-and-oneuser-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		logAccessConfig.setAuthorizedUsers(Arrays.asList("oneuser"));
		when(configService.getLogAccessConfig("log-with-onerole-and-oneuser-authorized")).thenReturn(logAccessConfig);
	}

	@Test
	public void testHasPermission_EveryOneIsAuthorized() throws Exception {
		
		boolean isGranted = webSecurity.hasPermission(null, "log-with-everyone-authorized");
		
		Assert.assertTrue(isGranted);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testHasPermission_UserNotAuthenticated() throws Exception {
		
		webSecurity.hasPermission(null, "log-with-oneuser-authorized");
	}
	
	@Test
	public void testHasPermission_UserAuthorized() throws Exception {
		
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("oneuser", null);
		
		boolean isGranted = webSecurity.hasPermission(authenticatedUser, "log-with-oneuser-authorized");

		Assert.assertTrue(isGranted);
	}

	@Test(expected=AccessDeniedException.class)
	public void testHasPermission_UserNotAuthorized() throws Exception {
		
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("not-authorized-user", null);
		
		webSecurity.hasPermission(authenticatedUser, "log-with-oneuser-authorized");
	}
	
	@Test
	public void testHasPermission_RoleAuthorized() throws Exception {
		
		// given
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		
		boolean isGranted = webSecurity.hasPermission(authenticatedUser, "log-with-onerole-authorized");
		
		Assert.assertTrue(isGranted);
	}

	@Test(expected=AccessDeniedException.class)
	public void testHasPermission_RoleNotAuthorized() throws Exception {
		
		webSecurity.hasPermission(null, "log-with-onerole-authorized");
	}
	
	@Test
	public void testHasPermission_UserNotAuthorizedButRoleAuthorized() throws Exception {
		
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		
		boolean isGranted = webSecurity.hasPermission(authenticatedUser, "log-with-onerole-and-oneuser-authorized");
		
		Assert.assertTrue(isGranted);
	}
}

