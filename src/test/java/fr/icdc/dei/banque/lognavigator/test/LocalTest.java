package fr.icdc.dei.banque.lognavigator.test;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.SequenceInputStream;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

public class LocalTest {

	@Test
	public void testLocalCommand() throws Exception {
		
		String directory = "D:/Developpement/workspaces/workspaceTest/LogNavigator/src/test/resources/files";
//		String shellCommand = "gzip -dc test.txt.gz";
		String shellCommand = "gzip -dc test.txt.gz | tail -1000";
//		String shellCommand = "set";
//		String shellCommand = "echo %PATH";
//		String[] env = new String[]{ "CYGWIN=xterm"};
//		String[] env = new String[]{ "CYGWIN=rxvt"};
		String[] env = null;

		
		File tmpBatch = File.createTempFile("lognavigator-", ".bat");
		try {
			FileCopyUtils.copy(shellCommand, new FileWriter(tmpBatch));
//			String[] shellCommandArray = new String[]{"cmd.exe", "/C", tmpBatch.getAbsolutePath()};
			String[] shellCommandArray = new String[]{"cmd", "/C", "gzip -dc test.txt.gz | tail -1"}; // Windows
//			String[] shellCommandArray = new String[]{"/bin/sh", "-c", "gzip -dc test.txt.gz | tail -1"}; // Linux
	
			// Execute the command
			Process process = Runtime.getRuntime().exec(shellCommandArray, env, new File(directory));

			// Construct the result stream
			InputStream resultStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			InputStream sequenceStream = new SequenceInputStream(resultStream, errorStream);
			
			// Display result
			FileCopyUtils.copy(sequenceStream, System.out);
		}
		finally  {
			tmpBatch.delete();
		}
	}
}
