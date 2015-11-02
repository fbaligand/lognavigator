package org.lognavigator.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.OsType;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.LogAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;


/**
 * Service which manages access to logs on local host
 */
@Service
@Qualifier("local")
public class LocalLogAccessService extends AbstractShellLogAccessService implements LogAccessService {
	
	private static final String OS_NAME_SYSTEM_PROPERTY = "os.name";
	private static final String WINDOWS_OS_MARKER = "windows";
	private static final String AIX_OS_MARKER = "aix";

	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		try {
			// Add the precommand (if any)
			if (StringUtils.hasText(logAccessConfig.getPreCommand())) {
				shellCommand = logAccessConfig.getPreCommand() + " && " + shellCommand;
			}

			// Prepare shellCommand array (depending OS)
			String[] shellCommandArray = null;
			if (getOSType(logAccessConfig) == OsType.WINDOWS) {
				shellCommandArray = new String[]{"cmd", "/C", shellCommand};
			}
			else {
				shellCommandArray = new String[]{"/bin/sh", "-c", shellCommand};
			}
			
			// Execute the command
			File currentDirectory = (logAccessConfig.getDirectory() != null) ? new File(logAccessConfig.getDirectory()) : null;
			Process process = Runtime.getRuntime().exec(shellCommandArray, null, currentDirectory);
			
			// Get and return the result stream
			InputStream resultStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			InputStream sequenceStream = new SequenceInputStream(resultStream, errorStream);
			return sequenceStream;
		}
		catch (IOException e) {
			if (e.getMessage().matches("Cannot run program \".+\" \\(in directory \".*\"\\).*")) {
				throw new LogAccessException("Configuration is invalid : directory " + logAccessConfig.getDirectory() + " does not exist", e);
			}
			else {
				throw new LogAccessException("Error when executing command " + shellCommand + " to " + logAccessConfig, e);
			}
		}
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Execute the download
		try {
			File downloadFile = new File(logAccessConfig.getDirectory() + "/" + fileName);
			FileCopyUtils.copy(new FileInputStream(downloadFile), downloadOutputStream);
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing downloading " + fileName + " on " + logAccessConfig, e);
		}
	}

	@Override
	protected Set<FileInfo> listFilesUsingNativeSystem(LogAccessConfig logAccessConfig, String subPath) throws LogAccessException {
		
		// Define target directory
		String targetPath = logAccessConfig.getDirectory();
		if (subPath != null) {
			targetPath += "/" + subPath;
		}
		File targetDirectory = new File(targetPath);

		// Check target directory
		if (!targetDirectory.exists()) {
			throw new LogAccessException("Directory '" + targetPath + "' does not exist");
		}
		if (!targetDirectory.isDirectory()) {
			throw new LogAccessException("'" + targetPath + "' is not a directory");
		}

		// List sub files and folders
		File[] childrenFiles = targetDirectory.listFiles();
		
		// Extract meta-informations
		Set<FileInfo> fileInfos = new TreeSet<FileInfo>();
		for (File childrenFile : childrenFiles) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFileName(childrenFile.getName());
			fileInfo.setRelativePath(childrenFile.getPath().substring(logAccessConfig.getDirectory().length() + 1).replace('\\', '/'));
			fileInfo.setDirectory(childrenFile.isDirectory());
			fileInfo.setLastModified(new Date(childrenFile.lastModified()));
			fileInfo.setFileSize(childrenFile.isDirectory() ? 0L : childrenFile.length());
			fileInfo.setLogAccessType(LogAccessType.LOCAL);
			fileInfos.add(fileInfo);
		}
		
		// Return meta-informations about files and folders
		return fileInfos;
	}

	@Override
	protected OsType getOSType(LogAccessConfig logAccessConfig) {
		if (logAccessConfig.getOsType() == null) {
			OsType osType;
			String osName = System.getProperty(OS_NAME_SYSTEM_PROPERTY).toLowerCase();
			
			// Check if OS is windows
			if (osName.contains(WINDOWS_OS_MARKER)) {
				osType = OsType.WINDOWS;
			}
			// Check if OS is AIX
			else if (osName.contains(AIX_OS_MARKER)) {
				osType = OsType.AIX;
			}
			// By default, OS is considered linux-compliant
			else {
				osType = OsType.LINUX;
			}
			// Update logAccessConfig to cache the information (and not execute command every time)
			logAccessConfig.setOsType(osType);
		}
		return logAccessConfig.getOsType();
	}

}
