package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lognavigator.bean.Breadcrumb;
import org.lognavigator.bean.CommandLine;
import org.lognavigator.bean.DisplayType;
import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.bean.TableCell;
import org.lognavigator.exception.AuthorizationException;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.service.ConfigService;
import org.lognavigator.service.LogAccessService;
import org.lognavigator.util.BreadcrumbFactory;
import org.lognavigator.util.CommandLineParser;
import org.lognavigator.util.FileInfoFactory;
import org.lognavigator.util.TableCellFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;


@Controller
public class CommandController {
	
	@Autowired
	@Qualifier("facade")
	LogAccessService logAccessService;
	
	@Autowired
	ConfigService configService;
	

	@RequestMapping("/{logAccessConfigId}/command")
	public String executeCommand(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="cmd", required=false, defaultValue=DEFAULT_LIST_COMMAND) String cmd,
					  @RequestParam(value="encoding", required=false, defaultValue=DEFAULT_ENCODING_OPTION) String encoding,
					  @RequestParam(value="displayType", required=false) DisplayType displayType
	) throws AuthorizationException, LogAccessException, IOException {
		
		// Parse command line
		CommandLine commandLine = CommandLineParser.parseCommandLine(cmd);
		
		// Is command forbidden ?
		checkForbiddenCommand(commandLine);
		
		// Forward to 'list' action, if command is 'ls'
		if ((displayType == null || displayType == DisplayType.TABLE) && commandLine.getCommand().equals(DEFAULT_LIST_COMMAND) && !cmd.contains("|")) {
			if (commandLine.hasParams()) {
				return UrlBasedViewResolver.FORWARD_URL_PREFIX + FOLDER_VIEW_URL_PREFIX + commandLine.getParam(0);
			}
			else {
				return UrlBasedViewResolver.FORWARD_URL_PREFIX + LOGS_LIST_URL;
			}
		}
		
		// Define default displayType when not given by client
		if (displayType == null) {
			if (cmd.startsWith(TAR_GZ_FILE_VIEW_COMMAND_START) || cmd.endsWith(TAR_GZ_FILE_VIEW_COMMAND_END)) {
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
		
		// Generate Breadcrumbs
		generateBreadcrumbs(logAccessConfigId, commandLine, model);
		
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
				if (cmd.startsWith(TAR_GZ_FILE_VIEW_COMMAND_START) || cmd.endsWith(TAR_GZ_FILE_VIEW_COMMAND_END)) {
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
	 * Checks that command line doesn't contain any forbidden command (for security reasons)
	 * @param commandLine command line to check
	 * @throws AuthorizationException if command line contains a forbidden command 
	 */
	private void checkForbiddenCommand(CommandLine commandLine) throws AuthorizationException {
		
		String forbiddenCommandsRegex = "(" + configService.getForbiddenCommands().replace(',', '|') + ")";
		String forbiddenCommandLineRegex = MessageFormat.format(FORBIDDEN_COMMANDLINE_REGEX, forbiddenCommandsRegex);
		
		if (commandLine.getLine().matches(forbiddenCommandLineRegex)
			|| commandLine.getCommand().matches(forbiddenCommandsRegex)
			|| commandLine.getCommand().matches(">|>>") 
			|| commandLine.getParams().contains(">") || 
			commandLine.getParams().contains(">>")
		) {
			throw new AuthorizationException("This command is forbidden (" + configService.getForbiddenCommands() + ",>,>>)");
		}
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

		// Sort files by name
		Comparator<List<TableCell>> tableLinesComparator = new Comparator<List<TableCell>>() {
			@Override
			public int compare(List<TableCell> line1, List<TableCell> line2) {
				String fileName1 = line1.get(0).getContent();
				String fileName2 = line2.get(0).getContent();
				return fileName1.compareToIgnoreCase(fileName2);
			}
		};
		Collections.sort(tableLines, tableLinesComparator);
		
		// Update Model to display data
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
	 * @param commandLine current executed command
	 * @param model model where to add breadcrumbs
	 */
	private void generateBreadcrumbs(String logAccessConfigId, CommandLine commandLine, Model model) {
		
		List<Breadcrumb> breadcrumbs = BreadcrumbFactory.createBreadCrumbs(logAccessConfigId);
		
		String filePath = null;
		boolean lastElementIsLink = false;
		String targzFilePath = null;
		String targzSubFilename = null;
		
		// first argument
		if (commandLine.getParams().size() >= 1) {
			String firstParam = commandLine.getParam(0);
			if (commandLine.getLine().startsWith(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START)) {
				filePath = firstParam.contains("/") ? firstParam.substring(0, firstParam.lastIndexOf('/')) : null;
				lastElementIsLink = true;
				targzFilePath = firstParam;
			}
			else if (!commandLine.getCommand().matches(TWO_PARAMS_COMMAND_REGEX)) {
				filePath = firstParam;
			}
		}
		
		// second argument
		if (commandLine.getParams().size() >= 2) {
			String secondParam = commandLine.getParam(1);
			if (commandLine.getLine().startsWith(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START)) {
				targzSubFilename = secondParam.contains("/") ? secondParam.substring(secondParam.lastIndexOf('/') + 1) : secondParam;
			}
			else if (commandLine.getCommand().matches(TWO_PARAMS_COMMAND_REGEX)) {
				filePath = secondParam;
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
