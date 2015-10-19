package org.lognavigator.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import net.schmizz.sshj.common.IOUtils;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;

/**
 * Abstract class defining common methods for LogAccessService based on shell commands 
 */
public abstract class AbstractShellLogAccessService implements LogAccessService {
	
	private static final String DIRECTORY_MARKER = "4000";
	private static final String GET_PERL_INFO_COMMAND = "echo DIRECTORY_OK && perl -v";
	private static final String DIRECTORY_OK_MARKER = "DIRECTORY_OK";
	private static final String PERL_INSTALLED_MARKER = "this is perl";

	@Autowired
	protected ConfigService configService;
	
	/**
	 * Check and return if host referenced by 'logAccessConfig' is running under Windows OS
	 * @param logAccessConfig log access config to test
	 * @return true if referenced host is running under Windows OS
	 * @throws LogAccessException if a technical error occurs
	 */
	protected abstract boolean isWindowsOS(LogAccessConfig logAccessConfig) throws LogAccessException;
	
	
	/**
	 * Check and return if host referenced by 'logAccessConfig' has perl installed
	 * @param logAccessConfig log access config to test
	 * @return true if referenced host has perl installed
	 * @throws LogAccessException if a technical error occurs
	 */
	protected boolean isPerlInstalled(LogAccessConfig logAccessConfig) throws LogAccessException {
		if (logAccessConfig.isPerlInstalled() == null) {
			try {
				// Execute command to know if perl is installed
				InputStream resultStream = executeCommand(logAccessConfig.getId(), GET_PERL_INFO_COMMAND);
				// Check if perl is installed
				String result = FileCopyUtils.copyToString(new InputStreamReader(resultStream));
				if (!result.contains(DIRECTORY_OK_MARKER)) {
					throw new LogAccessException("Configuration is invalid : directory " + logAccessConfig.getDirectory() + " does not exist");
				}
				boolean isPerlInstalled = result.toLowerCase().contains(PERL_INSTALLED_MARKER);
				// Update logAccessConfig to cache the information (and not execute command every time)
				logAccessConfig.setPerlInstalled(isPerlInstalled);
			}
			catch (IOException ioe) {
				throw new LogAccessException("Error while reading response of command : " + GET_PERL_INFO_COMMAND, ioe);
			}
		}
		return logAccessConfig.isPerlInstalled();
	}


	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);

		// If Perl is not installed : execute a simple 'ls' command
		if (!isPerlInstalled(logAccessConfig)) {
			return listFilesUsingNativeSystem(logAccessConfig, subPath);
		}

		
		// Construct perl list command based path and maximum file count
		String path = (subPath != null) ? subPath : ".";
		String listCommand = MessageFormat.format(Constants.PERL_LIST_COMMAND, path, configService.getFileListMaxCount());
		if (isWindowsOS(logAccessConfig)) {
			listCommand = listCommand.replace("\"", "\\\"");
			listCommand = listCommand.replace('\'', '"');
		}

		// Execute perl command
		InputStream resultStream = executeCommand(logAccessConfigId, listCommand);
		BufferedReader resultReader = new BufferedReader(new InputStreamReader(resultStream, Charset.forName("UTF-8")));
		
		// Parse the result lines to build FileInfo list
		Set<FileInfo> fileInfos = new TreeSet<FileInfo>();
		String line = null;
		try {
			while ((line = resultReader.readLine()) != null) {
				
				// Parse the line
				String[] lineTokens = line.split(" ");
				boolean isDirectory = lineTokens[0].equals(DIRECTORY_MARKER);
				long fileSize = Long.parseLong(lineTokens[1]);
				Date lastModified = new Date(Long.parseLong(lineTokens[2]));
				String fileName = lineTokens[3];
				int tokenIndex = 4;
				while (tokenIndex < lineTokens.length) {
					fileName += " " + lineTokens[tokenIndex];
					++tokenIndex;
				}
				
				// Build FileInfo bean
				FileInfo fileInfo = new FileInfo();
				fileInfo.setDirectory(isDirectory);
				fileInfo.setFileSize(isDirectory ? 0L : fileSize);
				fileInfo.setLastModified(lastModified);
				fileInfo.setFileName(fileName);
				fileInfo.setLogAccessType(logAccessConfig.getType());
				fileInfo.setRelativePath((subPath != null) ? subPath + "/" + fileName : fileName);
				fileInfos.add(fileInfo);
			}
			
			// Return list of files
			return fileInfos;
		}
		catch (NumberFormatException e) {
			throw new LogAccessException("Error while executing list command : " + line, e);
		}
		catch (IOException e) {
			throw new LogAccessException("I/O Error while listing files in path '" + subPath + "' in log access config : "  + logAccessConfig, e);
		}
		finally {
			IOUtils.closeQuietly(resultReader);
		}
	}
	
	/**
	 * list files of a path, using native underlying system
	 * @see #listFiles(String, String)
	 */
	protected abstract Set<FileInfo> listFilesUsingNativeSystem(LogAccessConfig logAccessConfig, String subPath) throws LogAccessException;

}
