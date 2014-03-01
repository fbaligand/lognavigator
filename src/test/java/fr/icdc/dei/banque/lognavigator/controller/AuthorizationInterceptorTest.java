package fr.icdc.dei.banque.lognavigator.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;
import fr.icdc.dei.banque.lognavigator.exception.AuthorizationException;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationInterceptorTest {
	
	@Mock
	private ConfigService configServiceMock;
	
	@Mock
	private HttpServletRequest httpRequestMock;
	@Mock
	private Principal userPrincipalMock;
	
	@InjectMocks
	private AuthorizationInterceptor authorizationInterceptor;

	@Before
	public void setUp() throws Exception {
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
		when(httpRequestMock.getUserPrincipal()).thenReturn(userPrincipalMock);
		when(userPrincipalMock.getName()).thenReturn("oneuser");
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}

	@Test(expected=AuthorizationException.class)
	public void testPreHandle_UserNotAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-oneuser-authorized/list");
		when(httpRequestMock.getUserPrincipal()).thenReturn(userPrincipalMock);
		when(userPrincipalMock.getName()).thenReturn("not-authorized-user");
		
		authorizationInterceptor.preHandle(httpRequestMock, null, null);
	}
	
	@Test
	public void testPreHandle_RoleAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-onerole-authorized/list");
		when(httpRequestMock.isUserInRole("onerole")).thenReturn(true);
		
		boolean isAccessAuthorized = authorizationInterceptor.preHandle(httpRequestMock, null, null);
		
		assertTrue(isAccessAuthorized);
	}

	@Test(expected=AuthorizationException.class)
	public void testPreHandle_RoleNotAuthorized() throws Exception {
		
		when(httpRequestMock.getRequestURI()).thenReturn("/LogNavigator/logs/log-with-onerole-authorized/list");
		
		authorizationInterceptor.preHandle(httpRequestMock, null, null);
	}
	
}
