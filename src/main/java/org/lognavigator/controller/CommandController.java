package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lognavigator.bean.Breadcrumb;
import org.lognavigator.bean.DisplayType;
import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.bean.TableCell;
import org.lognavigator.exception.AuthorizationException;
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
	) throws AuthorizationException, LogAccessException, IOException {
		
		// Is command authorized ?
		if (cmd.matches(FORBIDDEN_COMMANDS_REGEX)) {
			throw new AuthorizationException("This command is forbidden");
		}
		
		// Define default displayType when not given by client
		if (displayType == null) { 
			if (cmd.startsWith(LIST_COMMAND_START) || cmd.startsWith(TAR_GZ_FILE_VIEW_COMMAND_START) || cmd.endsWith(TAR_GZ_FILE_VIEW_COMMAND_END)) {
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
			model.addAttribute(RAW_CONTENT_KEY, resultReader);
		}
		
		// Process the result lines for html table display
		else {
			try {
				if (cmd.startsWith(LIST_COMMAND_START)) {
					processLs(resultReader, model);
				}
				else if (cmd.startsWith(TAR_GZ_FILE_VIEW_COMMAND_START) || cmd.endsWith(TAR_GZ_FILE_VIEW_COMMAND_END)) {
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
		
		// Generate Breadcrumbs
		generateBreadcrumbs(logAccessConfigId, cmd, model);
		
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

	/**
	 * Generate Breadcrumbs containing path to current file (for navigation)
	 * @param logAccessConfigId current logAccessConfigId 
	 * @param cmd current executed command
	 * @param model model where to add breadcrumbs
	 */
	private void generateBreadcrumbs(String logAccessConfigId, String cmd, Model model) {
		
		List<Breadcrumb> breadcrumbs = BreadcrumbFactory.createBreadCrumbs(logAccessConfigId);
		
		StringTokenizer stCmd = new StringTokenizer(cmd, " |");
		boolean isCommandPassed = false;
		boolean isFirstArgumentPassed = false;
		String quotedString = null;
		String filePath = null;
		boolean lastElementIsLink = false;
		String targzFilePath = null;
		String targzSubFilename = null;
		
		while (stCmd.hasMoreTokens()) {
			String token = stCmd.nextToken();
			if (!isCommandPassed) {
				isCommandPassed = true;
			}
			else if (!token.startsWith("-")) {
				if (token.startsWith("\"") && token.endsWith("\"")) {
					token = token.substring(1, token.length()-1);
				}
				else if (token.startsWith("\"")) {
					quotedString = token.substring(1);
					continue;
				}
				else if (quotedString != null) {
					if (token.endsWith("\"")) {
						quotedString += " " + token.substring(0, token.length()-1);
						token = quotedString;
						quotedString = null;
					}
					else {
						quotedString += " " + token;
						continue;
					}
				}
				// first argument
				if (!isFirstArgumentPassed) {
					isFirstArgumentPassed = true;
					if (cmd.startsWith(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START)) {
						filePath = token.contains("/") ? token.substring(0, token.lastIndexOf('/')) : null;
						lastElementIsLink = true;
						targzFilePath = token;
						continue;
					}
					else if (!cmd.matches(TWO_PARAMS_COMMAND_REGEX)) {
						filePath = token;
						break;
					}
				}
				// second argument
				else {
					if (cmd.startsWith(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START)) {
						targzSubFilename = token.contains("/") ? token.substring(token.lastIndexOf('/') + 1) : token;
						break;
					}
					else {
						filePath = token;
						break;
					}
				}
			}
		}
		
		if (filePath != null) {
			BreadcrumbFactory.addSubPath(breadcrumbs, filePath, lastElementIsLink);
		}
		if (targzFilePath != null) {
			try {
				int filenameIndex = targzFilePath.indexOf("/") + 1;
				String targzFilename = filenameIndex > 0 ? targzFilePath.substring(filenameIndex) : targzFilePath;
				String command = TAR_GZ_FILE_VIEW_COMMAND_START + targzFilePath;
				String link = FILE_VIEW_URL_PREFIX + URLEncoder.encode(command, URL_ENCODING);
				breadcrumbs.add(new Breadcrumb(targzFilename, link));
				breadcrumbs.add(new Breadcrumb(targzSubFilename));
			}
			catch (UnsupportedEncodingException e) {
				throw new UnsupportedCharsetException(URL_ENCODING);
			}
		}
		
		model.addAttribute(BREADCRUMBS_KEY, breadcrumbs);
	}
}
