package org.lognavigator.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.schmizz.sshj.common.IOUtils;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;


/**
 * Service which manages HTTP connections and commands to remote hosts
 * where logs are exposed by an Apache Httpd server, using DirectoryIndex directive
 */
@Service
@Qualifier("httpd")
public class HttpdLogAccessService implements LogAccessService {
	
	private static final String TABLE_START = "<pre>";
	private static final String TABLE_END = "</pre>";
	private static final String LINK_HREF_END = "\"";
	private static final String LINK_HREF_START = "<a href=\"";
	private static final String DATE_FORMAT_NUMERIC = "yyyy-MM-dd HH:mm";
	private static final String DATE_FORMAT_LITTERAL = "dd-MMM-yyyy HH:mm";
	private static final String LINK_END_TAG = "</a>";
	private static final String DIRECTORY_SEPARATOR = "/";
	private static final String LAST_MODIFIED_SORT = "?C=M;O=D";
	private static final long ONE_KB = 1024L;
	
	
	@Autowired
	ConfigService configService;
	
	@Autowired
	@Qualifier("local")
	LogAccessService localLogAccessService;
	
	
	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Generate authentication option (if requested in configuration)
		String authenticationOption = "";
		if (logAccessConfig.getUser() != null && logAccessConfig.getPassword() != null) {
			authenticationOption = "-u \"" + logAccessConfig.getUser() + ":" + logAccessConfig.getPassword() + "\" ";
		}
		
		// Generate proxy option (if requested in configuration)
		String proxyOption = "";
		if (logAccessConfig.getProxy() != null) {
			proxyOption = "-x " + logAccessConfig.getProxy() + " ";
		}
		
		// Generate curl command with full url
		String urlPrefix = logAccessConfig.getUrl();
		String fullUrlShellCommand = shellCommand.replaceFirst(Constants.HTTPD_FILE_VIEW_COMMAND_START, Constants.HTTPD_FILE_VIEW_COMMAND_START + authenticationOption + proxyOption + urlPrefix);

		// Execute curl command
		return localLogAccessService.executeCommand(logAccessConfigId, fullUrlShellCommand);
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);

		// Define curl shell command
		String shellCommand = Constants.HTTPD_FILE_VIEW_COMMAND_START + fileName;

		// Execute the download
		try {
			InputStream remoteInputStream = executeCommand(logAccessConfigId, shellCommand);
			FileCopyUtils.copy(remoteInputStream, downloadOutputStream);
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing downloading '" + fileName + "' on '" + logAccessConfig + "'", e);
		}
	}

	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Define curl shell command
		String shellCommand = Constants.HTTPD_FILE_VIEW_COMMAND_START;
		if (subPath != null) {
			shellCommand += subPath + DIRECTORY_SEPARATOR;
		}
		shellCommand += LAST_MODIFIED_SORT;

		// Execute command to get file list
		BufferedReader remoteReader;
		try {
			InputStream remoteInputStream = executeCommand(logAccessConfigId, shellCommand);
			remoteReader = new BufferedReader(new InputStreamReader(remoteInputStream, Constants.ISO_ENCODING));
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to '" + logAccessConfig + "'", e);
		}
		
		String currentLine = null;
		try {
			// Read until table head
			StringBuilder startContent = new StringBuilder();
			boolean isTableStartReached = false;
			while ( (currentLine = remoteReader.readLine()) != null) {
				startContent.append(currentLine).append("\n");
				if (currentLine.contains(TABLE_START)) {
					isTableStartReached = true;
					break;
				}
			}
			if (!isTableStartReached) {
				throw new LogAccessException("Impossible to get log files list on URL '" + logAccessConfig.getUrl() + "':\n" + startContent.toString());
			}
			
			// Result meta-informations
			Set<FileInfo> fileInfos = new TreeSet<FileInfo>();
			int maxFileCount = configService.getFileListMaxCount();
			int fileCount = 0;
				
			// Extract files and directories : each line is a file/directory
			while ( (currentLine = remoteReader.readLine()) != null) {
				
				// Last Line
				if (currentLine.contains(TABLE_END)) {
					break;
				}
				
				// Check file count
				++fileCount;
				if (fileCount > maxFileCount) {
					break;
				}
				
				// Parse file name
				int linkHrefStart = currentLine.indexOf(LINK_HREF_START) + LINK_HREF_START.length();
				int linkHrefEnd = currentLine.indexOf(LINK_HREF_END, linkHrefStart);
				String fileName = currentLine.substring(linkHrefStart, linkHrefEnd);
				boolean isDirectory = fileName.endsWith(DIRECTORY_SEPARATOR);
				if (isDirectory) {
					fileName = fileName.substring(0, fileName.length() - 1);
				}
				
				// Parse date
				int linkTagEnd = currentLine.indexOf(LINK_END_TAG) + LINK_END_TAG.length();
				String[] dateAndSize = currentLine.substring(linkTagEnd).trim().replaceAll("\\s+", " ").split(" ");
				String dateAsString = dateAndSize[0] + " " + dateAndSize[1];
				
				Date date;
				if (dateAsString.length() == DATE_FORMAT_NUMERIC.length()) {
					date = new SimpleDateFormat(DATE_FORMAT_NUMERIC).parse(dateAsString);
				}
				else {
					date = new SimpleDateFormat(DATE_FORMAT_LITTERAL, Locale.US).parse(dateAsString);
				}

				// Parse size
				long size = 0;
				if (!isDirectory) {
					String sizeAsString = dateAndSize[2];
					if (sizeAsString.matches("[0-9.]+[KMG]")) {
						BigDecimal sizeAsBigDecimal = new BigDecimal(sizeAsString.substring(0, sizeAsString.length()-1));
						char sizeLastChar = sizeAsString.charAt(sizeAsString.length()-1);
						switch (sizeLastChar) {
						case 'K':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(ONE_KB));
							break;
						case 'M':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(ONE_KB * ONE_KB));
							break;
						case 'G':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(ONE_KB * ONE_KB * ONE_KB));
							break;
						}
						size = sizeAsBigDecimal.longValue();
					}
					else {
						size = Long.parseLong(sizeAsString);
					}
				}
				
				// Extract meta-informations
				FileInfo fileInfo = new FileInfo();
				fileInfo.setFileName(fileName);
				fileInfo.setRelativePath((subPath != null ? subPath + DIRECTORY_SEPARATOR : "") + fileName);
				fileInfo.setDirectory(isDirectory);
				fileInfo.setLastModified(date);
				fileInfo.setFileSize(size);
				fileInfo.setLogAccessType(LogAccessType.HTTPD);
				fileInfos.add(fileInfo);
			}
			
			// Return meta-informations about files and directories
			return fileInfos;
			
		}
		catch (RuntimeException e) {
			throw new LogAccessException("Error when parsing this line:\n" + currentLine, e);
		}
		catch (ParseException e) {
			throw new LogAccessException("Error when parsing this line:\n" + currentLine, e);
		}
		catch (IOException e) {
			throw new LogAccessException("I/O Error when parsing log files list:\n" + e.getMessage(), e);
		}
		finally {
			IOUtils.closeQuietly(remoteReader);
		}
	}
}
