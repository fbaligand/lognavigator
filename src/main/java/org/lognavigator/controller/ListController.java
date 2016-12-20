package org.lognavigator.controller;

import static org.lognavigator.util.Constants.ACTIONS_TABLE_HEADER;
import static org.lognavigator.util.Constants.BREADCRUMBS_KEY;
import static org.lognavigator.util.Constants.DATE_TABLE_HEADER;
import static org.lognavigator.util.Constants.FILE_TABLE_HEADER;
import static org.lognavigator.util.Constants.SIZE_TABLE_HEADER;
import static org.lognavigator.util.Constants.TABLE_HEADERS_KEY;
import static org.lognavigator.util.Constants.TABLE_LAYOUT_CENTERED;
import static org.lognavigator.util.Constants.TABLE_LAYOUT_CLASS_KEY;
import static org.lognavigator.util.Constants.TABLE_LINES_KEY;
import static org.lognavigator.util.Constants.VIEW_TABLE;
import static org.lognavigator.util.Constants.WARN_MESSAGE_KEY;
import static org.lognavigator.util.Constants.WARN_TITLE_KEY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
	
	@RequestMapping("/logs/{logAccessConfigId}/list")
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

		return renderFileList(model, subPath, fileInfos);
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
	 * Render a file list as a HTML table, using list view
	 * @param model model attributes
	 * @param subPath path which is listed
	 * @param fileInfos file list to render
	 * @return view name to process rendering
	 */
	String renderFileList(Model model, String subPath, Set<FileInfo> fileInfos) {
		
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

		// Return view to display
		return VIEW_TABLE;
	}
}
