package org.lognavigator.util;

import static org.lognavigator.util.Constants.*;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.TableCell;
import org.lognavigator.bean.LogAccessConfig.LogAccessType;



/**
 * Factory for creating TableCell or TableCell List
 * 
 * @author fbaligand
 */
public class TableCellFactory {

	/**
	 * Create a TableCell bean list from file informations
	 * @param fileInfo file informations
	 * @return a new TableCell list describing the file
	 */
	public static List<TableCell> createTableCellList(FileInfo fileInfo) {
		
		SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
		String lastModifiedFormated = (fileInfo.getLastModified() != null) ? dateFormater.format(fileInfo.getLastModified()) : "";
		String fileSize = !fileInfo.isDirectory() ? String.valueOf(fileInfo.getFileSize()) : "-";

		return createTableCellList(fileInfo.getFileName(), fileInfo.getRelativePath(), fileInfo.isDirectory(), fileSize, lastModifiedFormated, fileInfo.getLogAccessType());
	}
	
	/**
	 * Create a TableCell bean list from meta-informations of a file or folder
	 * @return a new TableCell list
	 */
	public static List<TableCell> createTableCellList(String fileName, String relativePath, boolean isDirectory, String fileSize, String lastModified, LogAccessType logAccessType) {

		List<TableCell> lineCells = new ArrayList<TableCell>();

		// File name
		if (!isDirectory) {
			
			// Compute command pattern
			String commandPattern = DEFAULT_FILE_VIEW_COMMAND;
			String commandArg = relativePath;
			if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
				commandPattern = TAR_GZ_FILE_VIEW_COMMAND;
			}
			else if (fileName.endsWith(".gz")) {
				commandPattern = GZ_FILE_VIEW_COMMAND;
			}

			// Process special case of tar.gz contents
			if (relativePath.contains(TAR_GZ_CONTENT_SPLIT)) {
				String targzFileName = relativePath.split("!")[0];
				String targzEntryPath = relativePath.split("!")[1];
				commandPattern = commandPattern.replace(" {0}", "");
				commandPattern = MessageFormat.format(TAR_GZ_CONTENT_FILE_VIEW_COMMAND, "{0}", targzEntryPath, commandPattern);
				commandArg = targzFileName;
			}

			// Process special case of log access type HTTP
			if (logAccessType == LogAccessType.HTTPD) {
				if (commandPattern.startsWith("tar ")) {
					commandPattern = commandPattern.replaceFirst("f", "");
				}
				commandPattern = commandPattern.replace(" {0}", "");
				commandPattern = HTTPD_FILE_VIEW_COMMAND_PREFIX + commandPattern;
			}
			
			// Compute view command
			String command = MessageFormat.format(commandPattern, commandArg);
			lineCells.add(new TableCell(fileName, FILE_VIEW_URL_PREFIX + UriUtil.encode(command)));
		}
		// Directory
		else {
			String cellContent = fileName;
			String link = relativePath != null ? FOLDER_VIEW_URL_PREFIX + UriUtil.encode(relativePath) : LOGS_LIST_URL;
			String linkIcon = null;
			if (fileName.equals("..")) {
				linkIcon =  "fa fa-reply";
				cellContent = "Parent Folder";
			}
			lineCells.add(new TableCell(cellContent, link, linkIcon, "text-warning"));
		}

		// File size
		if (fileSize != null) {
			lineCells.add(new TableCell(fileSize, null, null, "dt-body-right"));
		}

		// File date
		if (lastModified != null) {
			lineCells.add(new TableCell(lastModified));
		}

		// File actions
		if (!isDirectory) {
			lineCells.add(new TableCell("Download", "download?fileName=" + relativePath, "glyphicon glyphicon-download"));
		}
		else {
			lineCells.add(new TableCell(null));
		}
		
		// return new table line
		return lineCells;
	}
}
