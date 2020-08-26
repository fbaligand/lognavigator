package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.lognavigator.bean.JsonResponse;
import org.lognavigator.bean.Breadcrumb;
import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.TableCell;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.service.ConfigService;
import org.lognavigator.service.LogAccessService;
import org.lognavigator.util.BreadcrumbFactory;
import org.lognavigator.util.FileInfoFactory;
import org.lognavigator.util.TableCellFactory;
import org.lognavigator.util.UriUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class ListController {
	
	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@Autowired
	ConfigService configService;

	@RequestMapping({"/logs/{logAccessConfigId}", "/logs/{logAccessConfigId}/"})
	public String redirectToList() {
		return "redirect:/logs/{logAccessConfigId}/list";
	}
	
	@RequestMapping(path="/logs/{logAccessConfigId}/list", params="format=json")
	@ResponseBody
	public JsonResponse<Set<FileInfo>> listAsJson (
		@PathVariable String logAccessConfigId,
		@RequestParam(value="subPath", required=false) String subPath
   )
   throws LogAccessException, AuthorizationException {
		
		// Is command forbidden ?
		checkForbiddenSubPath(subPath);

		// List files contained in requested logAccessConfigId/subPath
		Set<FileInfo> fileInfos = logAccessService.listFiles(logAccessConfigId, subPath);
		JsonResponse<Set<FileInfo>> jsonResponse = new JsonResponse<Set<FileInfo>>();
		jsonResponse.setData(fileInfos);

		// Add a warning if too many files
		checkFileListMaxCount(fileInfos, jsonResponse);
		
		// return json response
		return jsonResponse;
	}

	@RequestMapping("/logs/{logAccessConfigId}/classic-list")
	public String list(Model model,
	           		   HttpServletRequest request,
			           @PathVariable String logAccessConfigId,
			           @RequestParam(value="subPath", required=false) String subPath
			           )
			           throws LogAccessException, IOException, AuthorizationException {
		
		// Is command forbidden ?
		checkForbiddenSubPath(subPath);

		// List files contained in requested logAccessConfigId/subPath
		Set<FileInfo> fileInfos = logAccessService.listFiles(logAccessConfigId, subPath);

		// Construct breadcrumbs
		if (subPath != null) {
			List<Breadcrumb> breadcrumbs = BreadcrumbFactory.createBreadCrumbs(logAccessConfigId);
			BreadcrumbFactory.addSubPath(breadcrumbs, subPath, false);
			request.setAttribute(BREADCRUMBS_KEY, breadcrumbs);
		}

		return renderFileList(model, subPath, fileInfos, null);
	}


	@RequestMapping("/logs/{logAccessConfigId}/list")
	public String listWithScroller(Model model,
	           		   HttpServletRequest request,
			           @PathVariable String logAccessConfigId,
			           @RequestParam(value="subPath", required=false) String subPath
			           )
			           throws LogAccessException, IOException, AuthorizationException {
		
		// Is command forbidden ?
		checkForbiddenSubPath(subPath);

		// Construct breadcrumbs
		if (subPath != null) {
			List<Breadcrumb> breadcrumbs = BreadcrumbFactory.createBreadCrumbs(logAccessConfigId);
			BreadcrumbFactory.addSubPath(breadcrumbs, subPath, false);
			request.setAttribute(BREADCRUMBS_KEY, breadcrumbs);
		}

		return renderFileList(model, subPath, new TreeSet<FileInfo>(), computeListAjaxUrl(subPath));
	}


	/**
	 * Checks that sub path doesn't contain any forbidden path (for security reasons)
	 * @param subPath sub path to check
	 * @throws AuthorizationException if subPath contains a forbidden path 
	 */
	void checkForbiddenSubPath(String subPath) throws AuthorizationException {
		
		// Must we check subPath ?
		boolean mustCheckSubPath = (subPath != null && configService.getFileListBlockExternalPaths());
		if (!mustCheckSubPath) {
			return;
		}
		
		// Check if subPath is forbidden
		String forbiddenSubPathRegex = "^[A-Za-z]:.*|.*(\\.\\.).*|^/.*";
		if (subPath.matches(forbiddenSubPathRegex)) {
			throw new AuthorizationException("This sub path is forbidden : " + subPath);
		}
	}
	
	/**
	 * Check fileInfos size, and if list max count is reached, add a warning message in json response
	 */
	void checkFileListMaxCount(Set<FileInfo> fileInfos, JsonResponse<Set<FileInfo>> jsonResponse) {
		if (fileInfos.size() >= configService.getFileListMaxCount()) {
			jsonResponse.setWarnTitle("Too many files");
			jsonResponse.setWarnMessage("Only the " + configService.getFileListMaxCount() + " last updated files are displayed.");
		}
	}


	/**
	 * Render a file list as a HTML table, using list view
	 * @param model model attributes for JSP
	 * @param subPath path which is listed
	 * @param fileInfos file list to render
	 * @param ajaxUrl if view is scroller-table, provides url for datatables ajax loading. <code>null</code> indicates that classic table view should be used (without ajax)
	 * @return view name to process rendering
	 */
	String renderFileList(Model model, String subPath, Set<FileInfo> fileInfos, String ajaxUrl) {
		
		// Add a warning if too many files
		if (fileInfos.size() >= configService.getFileListMaxCount()) {
			model.addAttribute(WARN_TITLE_KEY, "Too many files");
			model.addAttribute(WARN_MESSAGE_KEY, "Only the " + configService.getFileListMaxCount() + " last updated files are displayed.");
		}
		
		// Add link to parent folder
		if (subPath != null && !subPath.equals("/")) {
			FileInfo parentFolderLink = FileInfoFactory.createParentFolderLink(subPath);
			fileInfos.add(parentFolderLink);
		}
		
		// Prepare the table lines for HTML presentation
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>(fileInfos.size());

		// Construct cells for files and folders
		for (FileInfo fileInfo : fileInfos) {
			List<TableCell> lineCells = TableCellFactory.createTableCellList(fileInfo);
			tableLines.add(lineCells);
		}

		// Add table cells to model so that view can render file list
		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, SIZE_TABLE_HEADER, DATE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_CENTERED);
		
		if (ajaxUrl != null) {
			// define table data ajax url
			model.addAttribute(AJAX_URL_KEY, ajaxUrl);
			model.addAttribute(HIDE_MESSAGES_KEY, true);

			// Return view to display
			return VIEW_SCROLLER_TABLE;
		}
		else {
			// Return view to display
			return VIEW_TABLE;
		}
	}

	/**
	 * Compute and return list ajax url for datatables
	 */
	String computeListAjaxUrl(String subPath) {
		String ajaxUrl = LOGS_LIST_URL + "?format=json";
		if (subPath != null) {
			ajaxUrl += "&subPath=" + UriUtil.encode(subPath);
		}
		return ajaxUrl;
	}
	
}
