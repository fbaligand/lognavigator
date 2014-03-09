package org.lognavigator.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;

/**
 * FilterInputStream whichs allow to auto-close SSH connection when the command result stream is closed
 */
public class SshCloseFilterInputStream extends FilterInputStream {
	
	private SSHClient sshClient;
	private Session session;

	public SshCloseFilterInputStream(InputStream is, SSHClient sshClient, Session session) {
		super(is);
		this.sshClient = sshClient;
		this.session = session;
	}

	@Override
	public void close() throws IOException {
		// Close the stream
		try {
			super.close();
		}
		catch (IOException e) {}
		// Close the session
		try {
			session.close();
		}
		catch (IOException e) {}
		// Disconnect the ssh client
		try {
			sshClient.disconnect();
		}
		catch (IOException e) {}
	}
}
