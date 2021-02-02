package org.lognavigator.bean;

import java.util.Date;

import org.lognavigator.bean.LogAccessConfig.LogAccessType;
import org.lognavigator.util.LocaleDateJsonSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Bean containing informations of a file or directory
 */
public class FileInfo implements Comparable<FileInfo> {

	private String fileName;
	private String relativePath;
	private boolean isDirectory;
	private Long fileSize;
	@JsonSerialize(using = LocaleDateJsonSerializer.class)
	private Date lastModified;
	private LogAccessType logAccessType;
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public boolean isDirectory() {
		return isDirectory;
	}
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public LogAccessType getLogAccessType() {
		return logAccessType;
	}
	public void setLogAccessType(LogAccessType logAccessType) {
		this.logAccessType = logAccessType;
	}
	
	@Override
	public String toString() {
		return "FileInfo [fileName=" + fileName + ", directoryPath=" + relativePath + "]";
	}
	
	@Override
	public int compareTo(FileInfo other) {
		if (this.isDirectory != other.isDirectory) {
			return (this.isDirectory ? -1 : 1);
		}
		return this.fileName.compareTo(other.fileName);
	}
}
