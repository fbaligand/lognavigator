package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lognavigator.bean.DisplayType;
import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.TableCell;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.service.LogAccessService;
import org.lognavigator.util.FileInfoFactory;
import org.lognavigator.util.TableCellFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
					  @RequestParam(value="displayType", required=false) DisplayType displayType
	) throws LogAccessException, IOException {
		
		// Define displayType when not given by client
		if (displayType == null) { 
			if (cmd.startsWith("ls ") || cmd.startsWith("tar -ztvf ") || cmd.endsWith("| tar -ztv")) {
				displayType = DisplayType.TABLE;
			}
			else {
				displayType = DisplayType.RAW;
			}
		}
		
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
				if (cmd.startsWith("ls ")) {
					processLs(resultReader, model);
				}
				else if (cmd.startsWith("tar -ztvf ") || cmd.endsWith("| tar -ztv")) {
					processTarGzList(resultReader, model, cmd);
				}
				else {
					processOtherCommand(resultReader, model);
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
	
	/**
	 * Display the result of a "ls" command as a convenient html table
	 */
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

	/**
	 * Display the result of a "tar.gz list" command as a convenient html table
	 */
	private void processTarGzList(BufferedReader resultReader, Model model, String cmd) throws IOException {
		
		// Compute archive filename
		Matcher matcher = Pattern.compile("[^ ]+\\.tar\\.gz").matcher(cmd);
		matcher.find();
		String targzFileName = matcher.group();
		
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>();

		// Add link to parent folder
		FileInfo parentFolderLink = FileInfoFactory.createParentFolderLink(targzFileName);
		List<TableCell> parentFolderCells = TableCellFactory.createTableCellList(parentFolderLink);
		tableLines.add(parentFolderCells);
		
		// Compute archive contents list
		String line;
		while ( (line = resultReader.readLine()) != null) {
			
			// directories are ignored
			boolean isDirectory = line.startsWith(DIRECTORY_RIGHT);
			if (isDirectory) {
				continue;
			}
			
			line = line.replaceAll(" +", " ");
			StringTokenizer stLine = new StringTokenizer(line, " ");
			
			// Skip 2 first columns
			stLine.nextToken();
			stLine.nextToken();
			
			// Get columns to display
			String fileSize = stLine.nextToken();
			String fileDate = stLine.nextToken();
			fileDate += " " + stLine.nextToken() + ":00";
			String filePath = stLine.nextToken();
			
			// Construct cells
			LogAccessType logAccessType = cmd.startsWith(HTTPD_FILE_VIEW_COMMAND_START) ? LogAccessType.HTTPD : LogAccessType.LOCAL;
			List<TableCell> lineCells = TableCellFactory.createTableCellList(filePath, targzFileName + "!" + filePath, isDirectory, fileSize, fileDate, logAccessType);
			tableLines.add(lineCells);
		}
		
		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(FILE_TABLE_HEADER, SIZE_TABLE_HEADER, DATE_TABLE_HEADER, ACTIONS_TABLE_HEADER));
		
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_CENTERED);
	}

	/**
	 * Display the result of any command as a default html table
	 */
	private void processOtherCommand(BufferedReader resultReader, Model model) throws IOException {
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
