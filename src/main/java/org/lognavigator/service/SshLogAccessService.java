package org.lognavigator.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.bean.OsType;
import org.lognavigator.exception.LogAccessException;
import org.lognavigator.util.LastUpdatedRemoteResourceFilter;
import org.lognavigator.util.ScpStreamingSystemFile;
import org.lognavigator.util.SshCloseFilterInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;


/**
 * Service which manages SSH connections and commands to remote hosts
 */
@Service
@Qualifier("ssh")
public class SshLogAccessService extends AbstractShellLogAccessService implements LogAccessService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SshLogAccessService.class);
	private static final String GET_OS_INFO_COMMAND = "uname -a";
	private static final String WINDOWS_OS_MARKER = "cygwin";
	private static final String AIX_OS_MARKER = "aix";
	
	private static ThreadLocal<SSHClient> sshClientThreadLocal = new ThreadLocal<SSHClient>();

	private Config sshClientConfig = new DefaultConfig();

	
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
		
		// Define the precommand (if any)
		String precommand = "";
		if (StringUtils.hasText(logAccessConfig.getPreCommand())) {
			precommand = logAccessConfig.getPreCommand() + " && ";
		}

		// Log the command
		LOGGER.debug("execute ssh command on '{}': {}", logAccessConfigId, shellCommand);

		// Execute the shell command
		Session session = null;
		Command resultCommand;
		try {
			session = sshClient.startSession();
			resultCommand = session.exec("cd \"" + logAccessConfig.getDirectory() + "\" && " + precommand + shellCommand);
		}
		catch (SSHException e) {
			IOUtils.closeQuietly(session, sshClient);
			throw new LogAccessException("Error when executing command '" + shellCommand + "' to '" + logAccessConfig + "'", e);
		}
		
		// Get and return the result stream
		InputStream sequenceStream = new SequenceInputStream(resultCommand.getInputStream(), resultCommand.getErrorStream());
		InputStream resultStream = new SshCloseFilterInputStream(sequenceStream, resultCommand, session, (closeSshClient ? sshClient : null));
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
			throw new LogAccessException("Error when executing downloading '" + fileName + "' on '" + logAccessConfig, e);
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

		// List files and directories (keep only the 'fileListMaxCount' last modified resources)
		SFTPClient sftpClient = null;
		Collection<RemoteResourceInfo> remoteResourceInfos;
		try {
			sftpClient = sshClient.newSFTPClient();
			LastUpdatedRemoteResourceFilter remoteResourcefilter = new LastUpdatedRemoteResourceFilter(configService.getFileListMaxCount());
			sftpClient.ls(targetPath, remoteResourcefilter);
			remoteResourceInfos = remoteResourcefilter.getRemoteResourceInfos();
		}
		catch (IOException e) {
			throw new LogAccessException("Error when listing files and directories on '" + logAccessConfig + "'", e);
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
	protected OsType getOSType(LogAccessConfig logAccessConfig) throws LogAccessException {
		
		if (logAccessConfig.getOsType() == null) {
			OsType osType;
			
			try {
				// Execute command to know OS
				InputStream resultStream = executeCommand(logAccessConfig.getId(), GET_OS_INFO_COMMAND);
				
				// Get OS name
				String osName = FileCopyUtils.copyToString(new InputStreamReader(resultStream)).toLowerCase();

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
			catch (IOException ioe) {
				throw new LogAccessException("Error while reading response of command: " + GET_OS_INFO_COMMAND, ioe);
			}
		}
			
		return logAccessConfig.getOsType();
	}
	
	/**
	 * Create a ssh client to logAccessConfig host, and process authentication by ssh key
	 * @param logAccessConfig log access config to connect to
	 * @return the created and authenticated ssh client
	 * @throws LogAccessException if a technical error occurs
	 */
	private SSHClient connectAndAuthenticate(LogAccessConfig logAccessConfig) throws LogAccessException {
		// Connect to the remote host
		SSHClient sshClient = new SSHClient(sshClientConfig);
		try {
			if (logAccessConfig.isTrust()) {
				sshClient.addHostKeyVerifier(new PromiscuousVerifier());
			}
			else {
				sshClient.loadKnownHosts();
			}
			sshClient.connect(logAccessConfig.getHost());
		}
		catch (IOException e) {
			throw new LogAccessException("Error when connecting to '" + logAccessConfig + "'", e);
		}
		
		// Authenticate to the remote host
		try {
			if (logAccessConfig.getPrivatekey() != null && logAccessConfig.getPassword() != null) {
				KeyProvider keyProvider = sshClient.loadKeys(logAccessConfig.getPrivatekey(), logAccessConfig.getPassword());
				sshClient.authPublickey(logAccessConfig.getUser(), keyProvider);
			}
			else if (logAccessConfig.getPassword() != null) {
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
			throw new LogAccessException("Error when authenticating to '" + logAccessConfig + "'", e);
		}
		catch (IOException e) {
			IOUtils.closeQuietly(sshClient);
			throw new LogAccessException("Error when authenticating to '" + logAccessConfig + "'", e);
		}
		
		// Return the connnected and authenticated client
		return sshClient;
	}

}
