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
import net.schmizz.sshj.common.IOUtils;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;


/**
 * Service which manages SSH connections and commands to remote hosts
 */
@Service
@Qualifier("ssh")
public class SshLogAccessService extends AbstractShellLogAccessService implements LogAccessService {
	
	private static final String GET_OS_INFO_COMMAND = "uname -a";
	private static final String WINDOWS_OS_MARKER = "cygwin";
	
	private static ThreadLocal<SSHClient> sshClientThreadLocal = new ThreadLocal<SSHClient>();

	
	/**
	 * Override listFiles() method to share the ssh client among the different executed commands
	 */
	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);

		// Create ssh client, authenticate and put it into thread local for a shared use
		SSHClient sshClient = connectAndAuthenticate(logAccessConfig);
		sshClientThreadLocal.set(sshClient);

		// List files using prepared ssh client
		try {
			return super.listFiles(logAccessConfigId, subPath);
		}
		finally {
			sshClientThreadLocal.remove();
			try {
				sshClient.disconnect();
			} catch (IOException e) {}
		}
	}

	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);

		// Create ssh client and authenticate
		SSHClient sshClient = sshClientThreadLocal.get();
		boolean closeSshClient = false;
		if (sshClient == null) {
			sshClient = connectAndAuthenticate(logAccessConfig);
			closeSshClient = true;
		}

		// Execute the shell command
		Session session = null;
		Command resultCommand;
		try {
			session = sshClient.startSession();
			resultCommand = session.exec("cd \"" + logAccessConfig.getDirectory() + "\" && " + shellCommand);
		}
		catch (SSHException e) {
			IOUtils.closeQuietly(session, sshClient);
			throw new LogAccessException("Error when executing command " + shellCommand + " to " + logAccessConfig, e);
		}
		
		// Get and return the result stream
		InputStream resultStream = new SequenceInputStream(resultCommand.getInputStream(), resultCommand.getErrorStream());
		resultStream = new SshCloseFilterInputStream(resultStream, resultCommand, session, (closeSshClient ? sshClient : null));
		return resultStream;
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		// Create ssh client and authenticate
		SSHClient sshClient = connectAndAuthenticate(logAccessConfig);

		// Execute the download
		try {
			String filePath = fileName.startsWith("/") ? fileName : logAccessConfig.getDirectory() + "/" + fileName;
			sshClient.newSCPFileTransfer().download(filePath, new ScpStreamingSystemFile(downloadOutputStream));
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
	protected Set<FileInfo> listFilesUsingNativeSystem(LogAccessConfig logAccessConfig, String subPath) throws LogAccessException {
		
		// Get ssh client
		SSHClient sshClient = sshClientThreadLocal.get();

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
			IOUtils.closeQuietly(sftpClient, sshClient);
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
	
	@Override
	protected boolean isWindowsOS(LogAccessConfig logAccessConfig) throws LogAccessException {
		if (logAccessConfig.isWindowsOS() == null) {
			try {
				// Execute command to know OS
				InputStream resultStream = executeCommand(logAccessConfig.getId(), GET_OS_INFO_COMMAND);
				
				// Check if OS is windows
				String result = FileCopyUtils.copyToString(new InputStreamReader(resultStream));
				boolean isWindowsOS = result.toLowerCase().contains(WINDOWS_OS_MARKER);

				// Update logAccessConfig to cache the information (and not execute command every time)
				logAccessConfig.setWindowsOS(isWindowsOS);
			}
			catch (IOException ioe) {
				throw new LogAccessException("Error while reading response of command : " + GET_OS_INFO_COMMAND, ioe);
			}
		}
		return logAccessConfig.isWindowsOS();
	}

	/**
	 * Create a ssh client to logAccessConfig host, and process authentication by ssh key
	 * @param logAccessConfig log access config to connect to
	 * @return the created and authenticated ssh client
	 * @throws LogAccessException if a technical error occurs
	 */
	private SSHClient connectAndAuthenticate(LogAccessConfig logAccessConfig) throws LogAccessException {
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
			if (logAccessConfig.getPassword() != null) {
				sshClient.authPassword(logAccessConfig.getUser(), logAccessConfig.getPassword());
			}
			else if (logAccessConfig.getPrivatekey() != null) {
				sshClient.authPublickey(logAccessConfig.getUser(), logAccessConfig.getPrivatekey());
			}
			else {
				sshClient.authPublickey(logAccessConfig.getUser());
			}
		}
		catch (SSHException e) {
			IOUtils.closeQuietly(sshClient);
			throw new LogAccessException("Error when authenticating to " + logAccessConfig, e);
		}
		
		// Return the connnected and authenticated client
		return sshClient;
	}

}
