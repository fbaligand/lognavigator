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
import org.lognavigator.util.Constants;
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
			sb.append("-rw-rw-r-- 1 root root 1189 13 mars  12:22 file1.log\n");
			sb.append("-rw-r--r-- 1 root root 1091 24 avril 12:09 file2.log\n");
			sb.append("-rw-rw-r-- 1 root root 1788 11 avril 16:12 file3.log\n");
			sb.append("-rw-rw-r-- 1 root root  112 13 mars  12:22 file4.log\n");
			sb.append("-rw-rw-r-- 1 root root 1078 13 mars  12:22 file5.log\n");
			sb.append("-rw-rw-r-- 1 root root 2380 25 févr. 17:58 file6.log\n");
		}
		else if (shellCommand.startsWith("ls")) {
			sb.append("file1.log\n");
			sb.append("file2.log\n");
			sb.append("file3.log\n");
			sb.append("file4.log\n");
			sb.append("file5.log\n");
			sb.append("file6.log\n");
		}
		else if (shellCommand.startsWith(Constants.TAR_GZ_FILE_VIEW_COMMAND_START)) {
			sb.append("drwx------ Administrators/None     0 2014-03-29 15:33 backup/\n");
			sb.append("-rw-r--r-- Administrators/None   298 2014-03-02 11:25 backup/apache-access-3l.log.gz\n");
			sb.append("-rw-r--r-- Administrators/None   508 2014-03-02 11:25 backup/apache-access-10l.log.gz\n");
			sb.append("-rwx------ Administrators/None  2260 2014-03-02 10:02 backup/apache-access-100l.log.gz\n");
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
