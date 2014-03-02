package fr.icdc.dei.banque.lognavigator.controller;

import static fr.icdc.dei.banque.lognavigator.util.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.icdc.dei.banque.lognavigator.bean.FileInfo;
import fr.icdc.dei.banque.lognavigator.bean.TableCell;
import fr.icdc.dei.banque.lognavigator.exception.LogAccessException;
import fr.icdc.dei.banque.lognavigator.service.LogAccessService;
import fr.icdc.dei.banque.lognavigator.util.FileInfoFactory;
import fr.icdc.dei.banque.lognavigator.util.TableCellFactory;

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

		for (FileInfo fileInfo : fileInfos) {
			// Construct cells for one file/folder
			List<TableCell> lineCells = TableCellFactory.createTableCellList(fileInfo);
			tableLines.add(lineCells);
		}

		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, SIZE_TABLE_HEADER, DATE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_CENTERED);
		if (subPath == null) {
			model.addAttribute(IS_ROOT_LIST_VIEW_KEY, Boolean.TRUE);
		}

		// Return view to display
		return PREPARE_MAIN_VIEW;
	}
}
