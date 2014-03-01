package fr.icdc.dei.banque.lognavigator.test;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class SshTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SshTest.class);

	@Test
	public void testSshExec() throws Exception {
		
		System.setProperty("user.home", "C:/Users/Fabien");
		
		String shellCommand = "ls";
		
		SSHClient sshClient = new SSHClient();
		sshClient.loadKnownHosts();
		sshClient.connect("localhost");
		
		// Authenticate to the remote host
		sshClient.authPublickey("Fabien");
		
		Session session = sshClient.startSession();

		// Execute the shell command
		Command resultCommand = session.exec(shellCommand);
		
		// Get and print the result stream
		InputStream resultStream = new SequenceInputStream(resultCommand.getInputStream(), resultCommand.getErrorStream());
		FileCopyUtils.copy(resultStream, System.out);

		session.close();
		sshClient.disconnect();
	}

	@Test
	public void testSshLs() throws Exception {
		
		System.setProperty("user.home", "C:/Users/Fabien");
		
		SSHClient sshClient = new SSHClient();
		sshClient.loadKnownHosts();
		sshClient.connect("localhost");
		
		// Authenticate to the remote host
		sshClient.authPublickey("Fabien");
		
		// Create the SFTP client
		SFTPClient sftpClient = sshClient.newSFTPClient();
		
		// Execute the ls command
		
		System.out.println("canonicalize: " + sftpClient.canonicalize("./appcfg6706438110508548946.log"));
		List<RemoteResourceInfo> remoteResourceInfos = sftpClient.ls(".");
		
		// Print the results
		for (RemoteResourceInfo remoteResourceInfo : remoteResourceInfos) {
			LOGGER.debug("remoteResourceInfo: {} - {} - {} - {} - {} - {} - {} - {}", 
					remoteResourceInfo.getName(),
					remoteResourceInfo.getPath(),
					remoteResourceInfo.getParent(),
					remoteResourceInfo.isDirectory(),
					remoteResourceInfo.isRegularFile(),
					remoteResourceInfo.getAttributes().getType(),
					new Date(remoteResourceInfo.getAttributes().getMtime() * 1000L),
					remoteResourceInfo.getAttributes().getSize()
			);
		}
		
		sftpClient.close();
		sshClient.disconnect();
	}
}
