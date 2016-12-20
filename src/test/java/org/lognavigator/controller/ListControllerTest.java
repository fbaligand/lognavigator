package org.lognavigator.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.service.ConfigService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListControllerTest {
	@Mock
	private ConfigService configService;
	@InjectMocks
	private ListController listController;
	
	@Before
	public void setup() {
		Mockito.when(configService.getFileListBlockExternalPaths()).thenReturn(true);
	}

	@Test
	public void testCheckForbiddenSubPath_NotEnabled() throws Exception {
		
		// given
		String subPath = "..";
		Mockito.when(configService.getFileListBlockExternalPaths()).thenReturn(false);
		
		// when
		listController.checkForbiddenSubPath(subPath);
		
		// then
		// No Exception
	}

	@Test
	public void testCheckForbiddenSubPath_00() throws Exception {
		listController.checkForbiddenSubPath("backup/");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_10() throws Exception {
		listController.checkForbiddenSubPath("..");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_11() throws Exception {
		listController.checkForbiddenSubPath("folder1/../../other");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_20() throws Exception {
		listController.checkForbiddenSubPath("c:");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_21() throws Exception {
		listController.checkForbiddenSubPath("C:/");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_22() throws Exception {
		listController.checkForbiddenSubPath("c:\\");
	}
		
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_30() throws Exception {
		listController.checkForbiddenSubPath("/");
	}
	
	@Test(expected=AuthorizationException.class)
	public void testCheckForbiddenSubPath_31() throws Exception {
		listController.checkForbiddenSubPath("/other");
	}
		
}
