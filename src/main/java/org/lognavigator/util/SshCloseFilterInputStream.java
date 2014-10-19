package org.lognavigator.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

/**
 * FilterInputStream whichs allow to auto-close SSH connection when the command result stream is closed
 */
public class SshCloseFilterInputStream extends FilterInputStream {
	
	private Command command;
	private Session session;
	private SSHClient sshClient;

	public SshCloseFilterInputStream(InputStream in, Command command, Session session, SSHClient sshClient) {
		super(in);
		this.command = command;
		this.session = session;
		this.sshClient = sshClient;
	}

	@Override
	public void close() throws IOException {
		
		// Close the stream, session and ssh client
		IOUtils.closeQuietly(super.in, command, session, sshClient);
	}
}
