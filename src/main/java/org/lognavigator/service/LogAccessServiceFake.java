package org.lognavigator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.exception.LogAccessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * Log Access Service which simulates the log access results returned by a linux machine
 */
@Service
@Qualifier("fake")
public class LogAccessServiceFake implements LogAccessService {
	
	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {

		StringBuilder sb = new StringBuilder();

		if (shellCommand.equals("ls -l")) {
			sb.append("total 40\n");
			sb.append("-rw-rw-r-- 1 tomcat dexploit 1189 13 mars  12:22 ConsultationPortlet.xml\n");
			sb.append("-rw-r--r-- 1 tomcat dexploit 1091 24 avril 12:09 ContextePortlet.xml\n");
			sb.append("-rw-rw-r-- 1 tomcat dexploit 1788 11 avril 16:12 FluxDeMassePortlet.xml\n");
			sb.append("-rw-rw-r-- 1 tomcat dexploit  112 13 mars  12:22 LayoutTemplateTitanDeveloppement.xml\n");
			sb.append("-rw-rw-r-- 1 tomcat dexploit 1078 13 mars  12:22 MenuPortlet.xml\n");
			sb.append("-rw-rw-r-- 1 tomcat dexploit 2380 25 févr. 17:58 MessagerieInternePortlet.xml\n");
		}
		else if (shellCommand.startsWith("ls")) {
			sb.append("ConsultationPortlet.xml\n");
			sb.append("ContextePortlet.xml\n");
			sb.append("FluxDeMassePortlet.xml\n");
			sb.append("LayoutTemplateTitanDeveloppement.xml\n");
			sb.append("MenuPortlet.xml\n");
			sb.append("MessagerieInternePortlet.xml\n");
		}
		else {
			sb.append("INFO - message1\n");
			sb.append("INFO - message2\n");
			sb.append("INFO - message3\n");
			sb.append("INFO - message4\n");
			sb.append("INFO - message5\n");
		}

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		try {
			String fileContent = "content of the file " + fileName;
			downloadOutputStream.write(fileContent.getBytes());
		}
		catch (IOException e) {
			throw new LogAccessException("Error when executing downloading " + fileName + " on " + logAccessConfigId, e);
		}
	}

	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {

		Set<FileInfo> fileInfos = new TreeSet<FileInfo>();

		// First file
		FileInfo fileInfo1 = new FileInfo();
		fileInfo1.setFileName("file1.log");
		fileInfo1.setRelativePath("file1.log");
		fileInfo1.setDirectory(false);
		fileInfo1.setFileSize(10L);
		fileInfo1.setLastModified(new Date());
		fileInfo1.setLogAccessType(LogAccessType.LOCAL);
		fileInfos.add(fileInfo1);

		// Second file
		FileInfo fileInfo2 = new FileInfo();
		fileInfo2.setFileName("file2.log");
		fileInfo2.setRelativePath("file2.log");
		fileInfo2.setDirectory(false);
		fileInfo2.setFileSize(20L);
		fileInfo2.setLastModified(new Date());
		fileInfo2.setLogAccessType(LogAccessType.LOCAL);
		fileInfos.add(fileInfo2);
		
		// Return meta-informations about files and folders
		return fileInfos;
	}
}
