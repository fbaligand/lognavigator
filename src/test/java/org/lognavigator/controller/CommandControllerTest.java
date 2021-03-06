package org.lognavigator.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lognavigator.bean.Breadcrumb;
import org.lognavigator.bean.DisplayType;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.service.DefaultConfigService;
import org.lognavigator.service.FakeLogAccessService;
import org.lognavigator.util.Constants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

public class CommandControllerTest {

	private CommandController commandController;
	
	private MockHttpServletRequest request;

	@Before
	public void setup() throws Exception {
		commandController = new CommandController();
		commandController.logAccessService = new FakeLogAccessService();
		commandController.configService = new DefaultConfigService();
		commandController.listController = new ListController();
		commandController.listController.configService = new DefaultConfigService();
		
		request = new MockHttpServletRequest();
	}

	@Test
	public void testExecuteCommand_ModelMap() throws Exception {
			
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "cat file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;

		// when
		String viewName = commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		Assert.assertEquals(Constants.VIEW_RAW, viewName);
		Assert.assertEquals(true, request.getAttribute(Constants.SHOW_OPTIONS_KEY));
		Assert.assertEquals(encoding, request.getAttribute(Constants.ENCODING_KEY));
		Assert.assertEquals(displayType, request.getAttribute(Constants.DISPLAY_TYPE_KEY));
		Assert.assertNotNull(request.getAttribute(Constants.BREADCRUMBS_KEY));
		Assert.assertNotNull(model.get(Constants.RAW_CONTENT_KEY));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteCommand_BreadCrumbs_TailFile() throws Exception {
			
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "tail -1000 file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;

		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		List<Breadcrumb> breadcrumbs = (List<Breadcrumb>)  request.getAttribute(Constants.BREADCRUMBS_KEY);
		Assert.assertEquals(2, breadcrumbs.size());
		Assert.assertEquals(logAccessConfigId, breadcrumbs.get(0).getLabel());
		Assert.assertEquals("file.log", breadcrumbs.get(1).getLabel());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteCommand_BreadCrumbs_TailFolderFile() throws Exception {
			
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "tail -1000 folder/file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;

		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		List<Breadcrumb> breadcrumbs = (List<Breadcrumb>)  request.getAttribute(Constants.BREADCRUMBS_KEY);
		Assert.assertEquals(3, breadcrumbs.size());
		Assert.assertEquals(logAccessConfigId, breadcrumbs.get(0).getLabel());
		Assert.assertEquals("folder", breadcrumbs.get(1).getLabel());
		Assert.assertEquals("file.log", breadcrumbs.get(2).getLabel());
	}

		
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteCommand_BreadCrumbs_TailSubFolderFile() throws Exception {
			
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "tail -1000 folder1/folder2/file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;

		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		List<Breadcrumb> breadcrumbs = (List<Breadcrumb>)  request.getAttribute(Constants.BREADCRUMBS_KEY);
		Assert.assertEquals(4, breadcrumbs.size());
		Assert.assertEquals(logAccessConfigId, breadcrumbs.get(0).getLabel());
		Assert.assertEquals("folder1", breadcrumbs.get(1).getLabel());
		Assert.assertEquals("folder2", breadcrumbs.get(2).getLabel());
		Assert.assertEquals("file.log", breadcrumbs.get(3).getLabel());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteCommand_BreadCrumbs_ListTarGzEntries() throws Exception {
		
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "tar -ztvf backup/apache-access.tar.gz";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.TABLE;
		
		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		List<Breadcrumb> breadcrumbs = (List<Breadcrumb>)  request.getAttribute(Constants.BREADCRUMBS_KEY);
		Assert.assertEquals(3, breadcrumbs.size());
		Assert.assertEquals(logAccessConfigId, breadcrumbs.get(0).getLabel());
		Assert.assertEquals("backup", breadcrumbs.get(1).getLabel());
		Assert.assertEquals("apache-access.tar.gz", breadcrumbs.get(2).getLabel());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteCommand_BreadCrumbs_ListTarGzEntry() throws Exception {
		
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "tar -O -zxf backup/apache-access.tar.gz backup/apache-access-3l.log.gz | gzip -dc | tail -1000";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;
		
		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
		
		// then
		List<Breadcrumb> breadcrumbs = (List<Breadcrumb>)  request.getAttribute(Constants.BREADCRUMBS_KEY);
		Assert.assertEquals(4, breadcrumbs.size());
		Assert.assertEquals(logAccessConfigId, breadcrumbs.get(0).getLabel());
		Assert.assertEquals("backup", breadcrumbs.get(1).getLabel());
		Assert.assertEquals("apache-access.tar.gz", breadcrumbs.get(2).getLabel());
		Assert.assertEquals("apache-access-3l.log.gz", breadcrumbs.get(3).getLabel());
	}
	
	@Test(expected=AuthorizationException.class)
	public void testExecuteCommand_ForbiddenCommand1() throws Exception {
			
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "rm file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;

		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
	}

	@Test(expected=AuthorizationException.class)
	public void testExecuteCommand_ForbiddenCommand2() throws Exception {
		
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "ls | tail -1 | rm";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;
		
		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
	}
	
	@Test(expected=AuthorizationException.class)
	public void testExecuteCommand_ForbiddenCommand3() throws Exception {
		
		// given
		ExtendedModelMap model = new ExtendedModelMap();
		String logAccessConfigId ="one-id";
		String cmd = "> file.log";
		String encoding = Constants.DEFAULT_ENCODING_OPTION;
		DisplayType displayType = DisplayType.RAW;
		
		// when
		commandController.executeCommand(model, request, logAccessConfigId, cmd, encoding, displayType);
	}
	
}
