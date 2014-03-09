package org.lognavigator.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

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
public class LogAccessServiceHttpd implements LogAccessService {
	
	private static final String CHARSET_PARAM = "charset=";
	private static final String TABLE_START = "<pre>";
	private static final String TABLE_END = "</pre>";
	private static final String LINK_HREF_END = "\"";
	private static final String LINK_HREF_START = "<a href=\"";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	private static final String LINK_END_TAG = "</a>";
	private static final String DIRECTORY_SEPARATOR = "/";
	
	
	@Autowired
	ConfigService configService;
	
	
	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Replace "curl -s <file>" occurences by "curl -s <full url>"
		String urlPrefix = logAccessConfig.getUrl();
		String fullUrlShellCommand = shellCommand.replaceFirst(Constants.HTTPD_FILE_VIEW_COMMAND_START, Constants.HTTPD_FILE_VIEW_COMMAND_START + urlPrefix);
		
		try {
			// Prepare shellCommand array (depending OS)
			String[] shellCommandArray = null;
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				shellCommandArray = new String[]{"cmd", "/C", fullUrlShellCommand};
			}
			else {
				shellCommandArray = new String[]{"/bin/sh", "-c", fullUrlShellCommand};
			}
			
			// Execute the command
			Process process = Runtime.getRuntime().exec(shellCommandArray);
			
			// Get and return the result stream
			InputStream resultStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			InputStream sequenceStream = new SequenceInputStream(resultStream, errorStream);
			return sequenceStream;
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing command " + shellCommand + " to " + logAccessConfig, e);
		}
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Define target url
		String targetUrl = logAccessConfig.getUrl() + DIRECTORY_SEPARATOR + fileName;

		// Execute the download
		try {
			URL url = new URL(targetUrl);
			InputStream remoteInputStream = url.openStream();
			FileCopyUtils.copy(remoteInputStream, downloadOutputStream);
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing downloading " + fileName + " on " + logAccessConfig, e);
		}
	}

	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Define target url
		String targetUrl = logAccessConfig.getUrl();
		if (subPath != null) {
			targetUrl += DIRECTORY_SEPARATOR + subPath;
		}

		// Connect to URL (provided by Apache Http Server)
		BufferedReader remoteReader;
		try {
			URL url = new URL(targetUrl);
			URLConnection urlConnection = url.openConnection();
			urlConnection.connect();
			String contentType = urlConnection.getContentType();
			String encoding = "ISO-8859-1";
			if (contentType != null && contentType.contains(CHARSET_PARAM)) {
				int encodingStartIndex = contentType.indexOf(CHARSET_PARAM) + CHARSET_PARAM.length();
				encoding = contentType.substring(encodingStartIndex);
			}
			InputStream remoteInputStream = url.openStream();
			remoteReader = new BufferedReader(new InputStreamReader(remoteInputStream, encoding));
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to " + logAccessConfig, e);
		}
		
		try {
			// Read until table head
			String currentLine;
			boolean isTableStartReached = false;
			while ( (currentLine = remoteReader.readLine()) != null) {
				if (currentLine.contains(TABLE_START)) {
					isTableStartReached = true;
					break;
				}
			}
			if (!isTableStartReached) {
				throw new LogAccessException("No valid content for log files list on " + logAccessConfig);
			}
			
			// Result meta-informations
			Set<FileInfo> fileInfos = new TreeSet<FileInfo>();
				
			// Extract files and directories : each line is a file/directory
			while ( (currentLine = remoteReader.readLine()) != null) {
				
				// Last Line
				if (currentLine.contains(TABLE_END)) {
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
				String dateAndSize = currentLine.substring(linkTagEnd).trim();
				String dateAsString = dateAndSize.substring(0, DATE_FORMAT.length());
				Date date = new SimpleDateFormat(DATE_FORMAT).parse(dateAsString);

				// Parse size
				long size = 0;
				if (!isDirectory) {
					String sizeAsString = dateAndSize.substring(DATE_FORMAT.length() + 1).trim();
					if (sizeAsString.matches("[0-9.]+[KMG]")) {
						BigDecimal sizeAsBigDecimal = new BigDecimal(sizeAsString.substring(0, sizeAsString.length()-1));
						char sizeLastChar = sizeAsString.charAt(sizeAsString.length()-1);
						switch (sizeLastChar) {
						case 'K':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(1000L));
							break;
						case 'M':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(1000L * 1000L));
							break;
						case 'G':
							sizeAsBigDecimal = sizeAsBigDecimal.multiply(BigDecimal.valueOf(1000L * 1000L * 1000L));
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
		catch (IOException e) {
			throw new LogAccessException("Error when parsing log files list on " + logAccessConfig, e);
		}
		catch (NumberFormatException e) {
			throw new LogAccessException("Error when parsing log files list on " + logAccessConfig, e);
		}
		catch (ParseException e) {
			throw new LogAccessException("Error when parsing log files list on " + logAccessConfig, e);
		}
	}
}
