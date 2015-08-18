package org.lognavigator.mvc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.mvc.AuthorizationInterceptor;
import org.lognavigator.service.DefaultAuthorizationService;
import org.lognavigator.service.ConfigService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


@RunWith(MockitoJUnitRunner.class)
public class AuthorizationInterceptorTest {
	
	@Mock
	private ConfigService configServiceMock;
	
	@Mock
	private HttpServletRequest httpRequestMock;
	
	@InjectMocks
	private AuthorizationInterceptor authorizationInterceptor;

	@Before
	public void setUp() throws Exception {
		
		// Inject authorization service
		authorizationInterceptor.authorizationService = new DefaultAuthorizationService();
		
		// Mock context path
		when(httpRequestMock.getContextPath()).thenReturn("/LogNavigator");
		
		// Mock LogNavigator configuration
		LogAccessConfig logAccessConfig = new LogAccessConfig("log-with-everyone-authorized", LogAccessType.LOCAL, "localhost", "/log");
		when(configServiceMock.getLogAccessConfig("log-with-everyone-authorized")).thenReturn(logAccessConfig);

		logAccessConfig = new LogAccessConfig("log-with-oneuser-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedUsers(Arrays.asList("oneuser"));
		when(configServiceMock.getLogAccessConfig("log-with-oneuser-authorized")).thenReturn(logAccessConfig);
		
		logAccessConfig = new LogAccessConfig("log-with-onerole-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		when(configServiceMock.getLogAccessConfig("log-with-onerole-authorized")).thenReturn(logAccessConfig);
		
		logAccessConfig = new LogAccessConfig("log-with-onerole-and-oneuser-authorized", LogAccessType.LOCAL, "localhost", "/log");
		logAccessConfig.setAuthorizedRoles(Arrays.asList("onerole"));
		logAccessConfig.setAuthorizedUsers(Arrays.asList("oneuser"));
		when(configServiceMock.getLogAccessConfig("log-with-onerole-and-oneuser-authorized")).thenReturn(logAccessConfig);
		
		// Force no authenticated user
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testPreHandle_UriIsUnprotected() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-oneuser-authorized/prepare-main-view");
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}
	
	@Test
	public void testPreHandle_EveryOneIsAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-everyone-authorized/list");
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}
	
	@Test(expected=AuthorizationException.class)
	public void testPreHandle_UserNotAuthenticated() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-oneuser-authorized/list");
		
		authorizationInterceptor.preHandle(httpRequestMock, null, null);
	}
	
	@Test
	public void testPreHandle_UserAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-oneuser-authorized/list");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("oneuser", null);
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}

	@Test(expected=AuthorizationException.class)
	public void testPreHandle_UserNotAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-oneuser-authorized/list");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("not-authorized-user", null);
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		authorizationInterceptor.preHandle(httpRequestMock, null, null);
	}
	
	@Test
	public void testPreHandle_RoleAuthorized() throws Exception {
		
		// given
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-onerole-authorized/list");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}

	@Test(expected=AuthorizationException.class)
	public void testPreHandle_RoleNotAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-onerole-authorized/list");
		
		authorizationInterceptor.preHandle(httpRequestMock, null, null);
	}
	
	@Test
	public void testPreHandle_UserNotAuthorizedButRoleAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-onerole-and-oneuser-authorized/list");
		TestingAuthenticationToken authenticatedUser = new TestingAuthenticationToken("anyuser", null, "onerole");
		SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}

}
