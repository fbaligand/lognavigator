package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.lognavigator.bean.Breadcrumb;
import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.TableCell;
import org.lognavigator.exception.LogAccessException;
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
	
	@RequestMapping("/{logAccessConfigId}/list")
	public String list(Model model, 
			           @PathVariable String logAccessConfigId,
			           @RequestParam(value="subPath", required=false) String subPath
			           )
			           throws LogAccessException, IOException {

		// Get the log files list
		Set<FileInfo> fileInfos = logAccessService.listFiles(logAccessConfigId, subPath);

		// Add link to parent folder
		if (subPath != null) {
			FileInfo parentFolderLink = FileInfoFactory.createParentFolderLink(subPath);
			fileInfos.add(parentFolderLink);
		}
		
		// Prepare the table lines for HTML presentation
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>();

		// Construct cells for files and folders
		for (FileInfo fileInfo : fileInfos) {
			List<TableCell> lineCells = TableCellFactory.createTableCellList(fileInfo);
			tableLines.add(lineCells);
		}

		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, SIZE_TABLE_HEADER, DATE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_CENTERED);

		// Construct breadcrumbs
		if (subPath != null) {
			List<Breadcrumb> breadcrumbs = BreadcrumbFactory.createBreadCrumbs(logAccessConfigId);
			BreadcrumbFactory.addSubPath(breadcrumbs, subPath, false);
			model.addAttribute(BREADCRUMBS_KEY, breadcrumbs);
		}

		// Return view to display
		return PREPARE_MAIN_VIEW;
	}
}
