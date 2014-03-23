package org.lognavigator.util;

import static org.lognavigator.util.Constants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
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

		return createTableCellList(fileInfo.getFileName(), fileInfo.getRelativePath(), fileInfo.isDirectory(), String.valueOf(fileInfo.getFileSize()), lastModifiedFormated, fileInfo.getLogAccessType());
	}
	
	/**
	 * Create a TableCell bean list from meta-informations of a file or folder
	 * @return a new TableCell list
	 */
	public static List<TableCell> createTableCellList(String fileName, String relativePath, boolean isDirectory, String fileSize, String lastModified, LogAccessType logAccessType) {

		List<TableCell> lineCells = new ArrayList<TableCell>();

		// File name
		try {
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
					commandPattern = commandPattern.replace(" {0}", "");
					commandPattern = MessageFormat.format(TAR_GZ_CONTENT_FILE_VIEW_COMMAND, "{0}", fileName, commandPattern);
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
				lineCells.add(new TableCell(fileName, FILE_VIEW_URL_PREFIX + URLEncoder.encode(command, URL_ENCODING)));
			}
			// Directory
			else {
				String link = relativePath != null ? FOLDER_VIEW_URL_PREFIX + URLEncoder.encode(relativePath, URL_ENCODING) : LOGS_LIST_URL;
				lineCells.add(new TableCell(fileName, link, null, "text-warning"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedCharsetException(URL_ENCODING);
		}

		// File size
		if (fileSize != null) {
			lineCells.add(new TableCell(fileSize));
		}

		// File date
		if (lastModified != null) {
			lineCells.add(new TableCell(lastModified));
		}

		// File actions
		if (!isDirectory) {
			lineCells.add(new TableCell("download", "download?fileName=" + relativePath, "glyphicon glyphicon-download"));
		}
		else {
			lineCells.add(new TableCell(null));
		}
		
		// return new table line
		return lineCells;
	}
}
