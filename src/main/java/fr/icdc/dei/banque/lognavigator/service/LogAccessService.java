package fr.icdc.dei.banque.lognavigator.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import fr.icdc.dei.banque.lognavigator.bean.FileInfo;
import fr.icdc.dei.banque.lognavigator.exception.LogAccessException;

/**
 * Service which lets you to execute shell commands on files and to download a file content
 */
public interface LogAccessService {

	/**
	 * Execute the shell command <code>shellCommand</code> on the remote host <code>logAccessConfigId</code>
	 * And return the command result stream
	 * @param logAccessConfigId log access config id
	 * @param shellCommand shell command to execute
	 * @return the command result stream
	 */
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException;

	/**
	 * Download a remote file, and stream the content into 'downloadOutputStream'
	 * @param logAccessConfigId log access config id
	 * @param fileName file to download
	 * @param downloadOutputStream outputStream where to put the downloaded content
	 */
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException;

	/**
	 * List files and directory present in requested logAccessConfig (optionaly in subPath)
	 * @param logAccessConfigId log access config id
	 * @param subPath sub-path relative to logAccessConfig directory (optional)
	 * @return list of informations about files and directories
	 */
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException;
}