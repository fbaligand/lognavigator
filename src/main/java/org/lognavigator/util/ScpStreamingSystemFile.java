package org.lognavigator.util;

import java.io.IOException;
import java.io.OutputStream;

import net.schmizz.sshj.xfer.InMemoryDestFile;

/**
 * Component implementing "LocalDestFile" sshj interface,
 * which allows to directly stream the downloaded ssh content into an output stream, without using a temp file 
 */
public class ScpStreamingSystemFile extends InMemoryDestFile {
	
	OutputStream outputStream;
	
	public ScpStreamingSystemFile(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.outputStream;
	}

}
