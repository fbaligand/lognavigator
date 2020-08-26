package org.lognavigator.controller;

import java.io.File;
import java.util.List;

import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.TableCell;
import org.lognavigator.exception.ConfigException;
import org.lognavigator.service.ConfigService;
import org.lognavigator.util.TableCellFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewFileController {
	
	@Autowired
	protected ConfigService configService;

	@RequestMapping("/logs/{logAccessConfigId}/view")
	public String viewFile(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="relativePath") String relativePath
	) throws ConfigException {
		
		File file = new File(relativePath);
		String fileName = file.getName();
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		List<TableCell> rowCells = TableCellFactory.createTableCellList(fileName, relativePath, false, null, null, logAccessConfig.getType());
		String commandUrl = rowCells.get(0).getLink();
		
		return "redirect:/logs/{logAccessConfigId}/" + commandUrl;
	}
	
}
