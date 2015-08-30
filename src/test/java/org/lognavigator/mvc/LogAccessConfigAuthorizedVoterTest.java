package org.lognavigator.mvc;

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
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;

@RunWith(MockitoJUnitRunner.class)
public class LogAccessConfigAuthorizedVoterTest {
	
	private static final ConfigAttribute GOOD_ATTRIBUTE = new SecurityConfig(LogAccessConfigAuthorizedVoter.IS_AUTHORIZED_LOG_ACCESS_CONFIG);
	private static final ConfigAttribute BAD_ATTRIBUTE = new SecurityConfig("BAD_ATTRIBUTE");

	@Spy
	private AuthorizationService authorizationService = new DefaultAuthorizationService();

	@Mock
	private ConfigService configService;
	
	@InjectMocks
	private LogAccessConfigAuthorizedVoter logAccessConfigAuthorizedVoter;


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
	public void testSupportsClass() throws Exception {
		boolean result = logAccessConfigAuthorizedVoter.supports(Void.class);
		Assert.assertTrue(result);
	}

	@Test
	public void testSupportsConfigAttribute_True() throws Exception {
		boolean result = logAccessConfigAuthorizedVoter.supports(GOOD_ATTRIBUTE);
		Assert.assertTrue(result);
	}
	
	@Test
	public void testSupportsConfigAttribute_False() throws Exception {
		boolean result = logAccessConfigAuthorizedVoter.supports(BAD_ATTRIBUTE);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testVote_BadAttribute() throws Exception {
		int resultAccess = logAccessConfigAuthorizedVoter.vote(null, new FilterInvocation(null, null), Arrays.asList(BAD_ATTRIBUTE));
		Assert.assertEquals(AccessDecisionVoter.ACCESS_ABSTAIN, resultAccess);
	}

	@Test
	public void testPreHandle_EveryOneIsAuthorized() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-everyone-authorized/list", "GET");
		
		int resultAccess = logAccessConfigAuthorizedVoter.vote(null, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
		
		Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, resultAccess);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testPreHandle_UserNotAuthenticated() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-oneuser-authorized/list", "GET");
		
		logAccessConfigAuthorizedVoter.vote(null, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
	}
	
	@Test
	public void testPreHandle_UserAuthorized() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-oneuser-authorized/list", "GET");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("oneuser", null);
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		int resultAccess = logAccessConfigAuthorizedVoter.vote(authenticatedUser, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
		
		Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, resultAccess);
	}

	@Test(expected=AccessDeniedException.class)
	public void testPreHandle_UserNotAuthorized() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-oneuser-authorized/list", "GET");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("not-authorized-user", null);
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		logAccessConfigAuthorizedVoter.vote(authenticatedUser, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
	}
	
	@Test
	public void testPreHandle_RoleAuthorized() throws Exception {
		
		// given
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-onerole-authorized/list", "GET");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		int resultAccess = logAccessConfigAuthorizedVoter.vote(authenticatedUser, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
		
		Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, resultAccess);
	}

	@Test(expected=AccessDeniedException.class)
	public void testPreHandle_RoleNotAuthorized() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-onerole-authorized/list", "GET");
		
		logAccessConfigAuthorizedVoter.vote(null, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
	}
	
	@Test
	public void testPreHandle_UserNotAuthorizedButRoleAuthorized() throws Exception {
		
		FilterInvocation filterInvocation = new FilterInvocation("/logs/log-with-onerole-and-oneuser-authorized/list", "GET");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		int resultAccess = logAccessConfigAuthorizedVoter.vote(authenticatedUser, filterInvocation, Arrays.asList(GOOD_ATTRIBUTE));
		
		Assert.assertEquals(AccessDecisionVoter.ACCESS_GRANTED, resultAccess);
	}

}
