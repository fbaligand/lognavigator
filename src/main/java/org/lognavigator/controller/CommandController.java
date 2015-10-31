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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.schmizz.sshj.common.IOUtils;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
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
	
	@Autowired
	ListController listController;
	

	@RequestMapping("/logs/{logAccessConfigId}/command")
	public String executeCommand(Model model,
					  HttpServletRequest request,
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
		request.setAttribute(SHOW_OPTIONS_KEY, true);
		request.setAttribute(ENCODING_KEY, encoding);
		request.setAttribute(DISPLAY_TYPE_KEY, displayType);
		
		// Generate Breadcrumbs
		generateBreadcrumbs(logAccessConfigId, commandLine, request);
		
		// Execute the command
		InputStream resultStream = logAccessService.executeCommand(logAccessConfigId, cmd);
		BufferedReader resultReader = new BufferedReader(new InputStreamReader(resultStream, encoding));
		
		// Process the result lines for raw display
		if (displayType == DisplayType.RAW) {
			model.addAttribute(RAW_CONTENT_KEY, resultReader);
			return VIEW_RAW;
		}
		
		// Process the result lines for html table display
		else {
			try {
				if (cmd.startsWith(TAR_GZ_FILE_VIEW_COMMAND_START) || cmd.endsWith(TAR_GZ_FILE_VIEW_COMMAND_END)) {
					return processTarGzList(resultReader, model, cmd);
				}
				else {
					processOtherCommand(resultReader, model);
				}
			}
			finally {
				IOUtils.closeQuietly(resultReader);
			}
			return VIEW_TABLE;
		}
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
	 * @return view name to display
	 */
	private String processTarGzList(BufferedReader resultReader, Model model, String cmd) throws IOException {
		
		// Compute archive filename
		Matcher matcher = Pattern.compile("[^ ]+\\.tar\\.gz").matcher(cmd);
		matcher.find();
		String targzFileName = matcher.group();
		
		Set<FileInfo> archiveEntryList = new TreeSet<FileInfo>();

		// Compute archive contents list
		StringBuilder potentialErrorMessage = new StringBuilder();
		String line;
		int remainingFileCount = configService.getFileListMaxCount();
		SimpleDateFormat targzDateFormat = new SimpleDateFormat(TAR_GZ_DATE_FORMAT);
		LogAccessType logAccessType = cmd.startsWith(HTTPD_FILE_VIEW_COMMAND_START) ? LogAccessType.HTTPD : LogAccessType.LOCAL;
		
		try {
			while ( (line = resultReader.readLine()) != null && remainingFileCount > 0) {
				
				potentialErrorMessage.append(line).append("\n");
				
				// directories are ignored
				boolean isDirectory = line.startsWith(DIRECTORY_RIGHT);
				if (isDirectory) {
					continue;
				}
				
				--remainingFileCount;
				line = line.replaceAll(" +", " ");
				StringTokenizer stLine = new StringTokenizer(line, " ");
	
				// Skip 2 first columns
				stLine.nextToken();
				stLine.nextToken();
				
				// Get columns to display
				Long fileSize = Long.parseLong(stLine.nextToken());
				String sFileDate = stLine.nextToken() + " " + stLine.nextToken();
				Date fileDate = targzDateFormat.parse(sFileDate);
				String filePath = stLine.nextToken();
				String fileName = filePath.replaceAll(".*/", "");
				
				// Construct FileInfo bean
				FileInfo fileInfo = new FileInfo();
				fileInfo.setFileName(fileName);
				fileInfo.setRelativePath(targzFileName + "!" + filePath);
				fileInfo.setDirectory(false);
				fileInfo.setFileSize(fileSize);
				fileInfo.setLastModified(fileDate);
				fileInfo.setLogAccessType(logAccessType);
				archiveEntryList.add(fileInfo);
			}
		}
		catch (RuntimeException e) {
			potentialErrorMessage.append(FileCopyUtils.copyToString(resultReader));
			throw new IOException("Error while listing tar.gz entries.\n" + potentialErrorMessage.toString(), e);
		}
		catch (ParseException e) {
			throw new IOException("Error while listing tar.gz entries. " + e.getMessage(), e);
		}

		// Render archive entry list in HTML
		return listController.renderFileList(model, targzFileName, archiveEntryList);
	}

	/**
	 * Display the result of any command as a default html table
	 */
	private void processOtherCommand(BufferedReader resultReader, Model model) throws IOException {
		List<List<TableCell>> tableLines = new ArrayList<List<TableCell>>();
		String line;
		int lineNumber = 0;
		while ( (line = resultReader.readLine()) != null) {
			++lineNumber;
			tableLines.add(Arrays.asList(new TableCell(String.valueOf(lineNumber)), new TableCell(line)));
		}
		model.addAttribute(TABLE_HEADERS_KEY, Arrays.asList(LINE_NUMBER_TABLE_HEADER, LINE_CONTENT_TABLE_HEADER));
		model.addAttribute(TABLE_LINES_KEY, tableLines);
		model.addAttribute(TABLE_LAYOUT_CLASS_KEY, TABLE_LAYOUT_FULL_WIDTH);
	}

	/**
	 * Generate Breadcrumbs containing path to current file (for navigation)
	 * @param logAccessConfigId current logAccessConfigId 
	 * @param commandLine current executed command
	 * @param request request where to add breadcrumbs as a request attribute
	 */
	private void generateBreadcrumbs(String logAccessConfigId, CommandLine commandLine, HttpServletRequest request) {
		
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
		
		// special case : view content file included in a tar.gz archive, using curl
		if (commandLine.getLine().matches(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_USING_CURL_REGEX)) {
			targzFilePath = filePath;
			filePath = targzFilePath.contains("/") ? targzFilePath.substring(0, targzFilePath.lastIndexOf('/')) : null;
			lastElementIsLink = true;
			targzSubFilename = commandLine.getLine().replaceFirst(TAR_GZ_CONTENT_FILE_VIEW_COMMAND_USING_CURL_REGEX, "$2");
			targzSubFilename = targzSubFilename.contains("/") ? targzSubFilename.substring(targzSubFilename.lastIndexOf('/') + 1) : targzSubFilename;
		}
		
		if (filePath != null) {
			BreadcrumbFactory.addSubPath(breadcrumbs, filePath, lastElementIsLink);
		}
		if (targzFilePath != null) {
			try {
				int filenameIndex = targzFilePath.lastIndexOf("/") + 1;
				String targzFilename = filenameIndex > 0 ? targzFilePath.substring(filenameIndex) : targzFilePath;
				String command = MessageFormat.format(TAR_GZ_FILE_VIEW_COMMAND, targzFilePath);
				if (commandLine.getLine().startsWith(HTTPD_FILE_VIEW_COMMAND_START)) {
					command = HTTPD_FILE_VIEW_COMMAND_START + targzFilePath + TAR_GZ_FILE_VIEW_COMMAND_END;
				}
				String link = FILE_VIEW_URL_PREFIX + URLEncoder.encode(command, URL_ENCODING);
				breadcrumbs.add(new Breadcrumb(targzFilename, link));
				breadcrumbs.add(new Breadcrumb(targzSubFilename));
			}
			catch (UnsupportedEncodingException e) {
				throw new UnsupportedCharsetException(URL_ENCODING);
			}
		}
		
		request.setAttribute(BREADCRUMBS_KEY, breadcrumbs);
	}
}
