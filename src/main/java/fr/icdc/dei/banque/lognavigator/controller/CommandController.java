package fr.icdc.dei.banque.lognavigator.controller;

import static fr.icdc.dei.banque.lognavigator.util.WebConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.icdc.dei.banque.lognavigator.bean.DisplayType;
import fr.icdc.dei.banque.lognavigator.bean.TableCell;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;
import fr.icdc.dei.banque.lognavigator.exception.LogAccessException;
import fr.icdc.dei.banque.lognavigator.service.LogAccessService;
import fr.icdc.dei.banque.lognavigator.util.TableCellFactory;

@Controller
public class CommandController {
	
	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@RequestMapping("/{logAccessConfigId}/command")
	public String executeCommand(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="cmd", required=false, defaultValue=DEFAULT_LIST_COMMAND) String cmd,
					  @RequestParam(value="encoding", required=false, defaultValue=DEFAULT_ENCODING_OPTION) String encoding,
					  @RequestParam(value="displayType", required=false, defaultValue=DEFAULT_DISPLAY_TYPE_OPTION) DisplayType displayType
	) throws LogAccessException, IOException {
		
		// Add options to model
		model.addAttribute(SHOW_OPTIONS_KEY, true);
		model.addAttribute(ENCODING_KEY, encoding);
		model.addAttribute(DISPLAY_TYPE_KEY, displayType);
		
		// Execute the command
		InputStream resultStream = logAccessService.executeCommand(logAccessConfigId, cmd);
		BufferedReader resultReader = new BufferedReader(new InputStreamReader(resultStream, encoding));
		
		// Process the result lines for raw display
		if (displayType == DisplayType.RAW) {
			String rawContent = FileCopyUtils.copyToString(resultReader);
			model.addAttribute(RAW_CONTENT_KEY, rawContent);
		}
		
		// Process the result lines for html table display
		else {
			try {
				if (cmd.startsWith("ls")) {
					processLs(resultReader, model);
				}
				else {
					processNonLs(resultReader, model);
				}
			}
			finally {
				try {
					resultReader.close();
				}
				catch (IOException e) {}
			}
		}
		
		// Define view to display
		return PREPARE_MAIN_VIEW;
	}
	
	private void processLs(BufferedReader resultReader, Model model) throws IOException {
		
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>();

		// Header line
		String line = resultReader.readLine();
		
		// No result
		if (line == null) {
			model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER));
			tableLines.add(Arrays.asList(new TableCell("No File")));
		}

		// Simple ls
		else if (!line.startsWith("total ")) {
			do {
				String fileName = line;
				List<TableCell> lineCells = TableCellFactory.createTableCellList(fileName, fileName, false, null, null, LogAccessType.LOCAL);
				tableLines.add(lineCells);
			}
			while ( (line = resultReader.readLine()) != null);
			model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		}
		
		// Detailed ls
		else {
			while ( (line = resultReader.readLine()) != null) {
				boolean isDirectory = line.startsWith(DIRECTORY_RIGHT);
				line = line.replaceAll(" +", " ");
				StringTokenizer stLine = new StringTokenizer(line, " ");
				// Skip 4 first columns
				stLine.nextToken();
				stLine.nextToken();
				stLine.nextToken();
				stLine.nextToken();
				// Get columns to display
				String fileSize = stLine.nextToken();
				String fileDate = stLine.nextToken();
				fileDate += " " + stLine.nextToken();
				fileDate += " " + stLine.nextToken();
				String fileName = stLine.nextToken();
				// Construct cells
				List<TableCell> lineCells = TableCellFactory.createTableCellList(fileName, fileName, isDirectory, fileSize, fileDate, LogAccessType.LOCAL);
				tableLines.add(lineCells);
			}
			model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, SIZE_TABLE_HEADER, DATE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		}
		
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_CENTERED);
	}

	private void processNonLs(BufferedReader resultReader, Model model) throws IOException {
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>();
		String line;
		while ( (line = resultReader.readLine()) != null) {
			tableLines.add(Arrays.asList(new TableCell(line)));
		}
		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(LINE_CONTENT_TABLE_HEADER));
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_FULL_WIDTH);
	}
}
