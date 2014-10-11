package org.lognavigator.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.util.ScpStreamingSystemFile;
import org.lognavigator.util.SshCloseFilterInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;


/**
 * Service which manages SSH connections and commands to remote hosts
 */
@Service
@Qualifier("ssh")
public class SshLogAccessService implements LogAccessService {
	
	private static final String GET_OS_INFO_COMMAND = "uname -a";
	private static final String WINDOWS_OS_MARKER = "cygwin";
	
	@Autowired
	ConfigService configService;
	
	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Connect to the remote host
		SSHClient sshClient = new SSHClient();
		try {
			sshClient.loadKnownHosts();
			sshClient.connect(logAccessConfig.getHost());
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to " + logAccessConfig, e);
		}
		
		// Authenticate to the remote host
		Session session;
		try {
			sshClient.authPublickey(logAccessConfig.getUser());
			session = sshClient.startSession();
		}
		catch (SSHException e) {
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
			throw new LogAccessException("Error when authenticating to " + logAccessConfig, e);
		}

		// Execute the shell command
		Command resultCommand;
		try {
			resultCommand = session.exec("cd " + logAccessConfig.getDirectory() + " && " + shellCommand);
		}
		catch (SSHException e) {
            try {
    			session.close();
			}
            catch(SSHException sshe) {}
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
			throw new LogAccessException("Error when executing command " + shellCommand + " to " + logAccessConfig, e);
		}
		
		// Get and return the result stream
		InputStream resultStream = new SequenceInputStream(resultCommand.getInputStream(), resultCommand.getErrorStream());
		resultStream = new SshCloseFilterInputStream(resultStream, sshClient, session);
		return resultStream;
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Connect to the remote host
		SSHClient sshClient = new SSHClient();
		try {
			sshClient.loadKnownHosts();
			sshClient.connect(logAccessConfig.getHost());
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to " + logAccessConfig, e);
		}
		
		// Authenticate to the remote host
		try {
			sshClient.authPublickey(logAccessConfig.getUser());
		}
		catch (SSHException e) {
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
			throw new LogAccessException("Error when authenticating to " + logAccessConfig, e);
		}

		// Execute the download
		try {
			sshClient.newSCPFileTransfer().download(logAccessConfig.getDirectory() + "/" + fileName, new ScpStreamingSystemFile(downloadOutputStream));
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing downloading " + fileName + " on " + logAccessConfig, e);
		}
		finally {
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
		}
		
	}

	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Connect to the remote host
		SSHClient sshClient = new SSHClient();
		try {
			sshClient.loadKnownHosts();
			sshClient.connect(logAccessConfig.getHost());
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to " + logAccessConfig, e);
		}
		
		// Authenticate to the remote host
		try {
			sshClient.authPublickey(logAccessConfig.getUser());
		}
		catch (SSHException e) {
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
			throw new LogAccessException("Error when authenticating to " + logAccessConfig, e);
		}

		// Define target directory
		String targetPath = logAccessConfig.getDirectory();
		if (subPath != null) {
			targetPath += "/" + subPath;
		}

		// List files and directories
		SFTPClient sftpClient = null;
		List<RemoteResourceInfo> remoteResourceInfos;
		try {
			sftpClient = sshClient.newSFTPClient();
			remoteResourceInfos = sftpClient.ls(targetPath);
		}
		catch (IOException e) {
			throw new LogAccessException("Error when listing files and directories on " + logAccessConfig, e);
		}
		finally {
            try {
            	if (sftpClient != null) {
                	sftpClient.close();
            	}
			}
            catch(IOException ioe) {}
            try {
				sshClient.disconnect();
			}
            catch (IOException ioe) {}
		}
		
		// Extract meta-informations
		Set<FileInfo> fileInfos = new TreeSet<FileInfo>();
		for (RemoteResourceInfo remoteResourceInfo : remoteResourceInfos) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setFileName(remoteResourceInfo.getName());
			fileInfo.setRelativePath(remoteResourceInfo.getPath().substring(logAccessConfig.getDirectory().length() + 1).replace('\\', '/'));
			fileInfo.setDirectory(remoteResourceInfo.isDirectory());
			fileInfo.setLastModified(new Date(remoteResourceInfo.getAttributes().getMtime() * 1000L));
			fileInfo.setFileSize(remoteResourceInfo.isDirectory() ? 0L : remoteResourceInfo.getAttributes().getSize());
			fileInfo.setLogAccessType(LogAccessType.SSH);
			fileInfos.add(fileInfo);
		}
		
		// Return meta-informations about files and folders
		return fileInfos;
	}
	
	private boolean isWindowsOS(LogAccessConfig logAccessConfig) throws LogAccessException {
		if (logAccessConfig.isWindowsOS() == null) {
			try {
				InputStream resultStream = executeCommand(logAccessConfig.getId(), GET_OS_INFO_COMMAND);
				String result = FileCopyUtils.copyToString(new InputStreamReader(resultStream));
				boolean isWindowsOS = result.toLowerCase().contains(WINDOWS_OS_MARKER);
				logAccessConfig.setWindowsOS(isWindowsOS);
			}
			catch (IOException ioe) {
				throw new LogAccessException("Error while reading response of command : " + GET_OS_INFO_COMMAND, ioe);
			}
		}
		return logAccessConfig.isWindowsOS();
	}
}
