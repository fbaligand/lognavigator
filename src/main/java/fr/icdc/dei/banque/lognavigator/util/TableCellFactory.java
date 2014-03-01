package fr.icdc.dei.banque.lognavigator.util;

import static fr.icdc.dei.banque.lognavigator.util.WebConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.icdc.dei.banque.lognavigator.bean.FileInfo;
import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig.LogAccessType;

import fr.icdc.dei.banque.lognavigator.bean.TableCell;

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
				String commandPattern = DEFAULT_FILE_VIEW_COMMAND;
				if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
					commandPattern = TAR_GZ_FILE_VIEW_COMMAND;
				}
				else if (fileName.endsWith(".gz")) {
					commandPattern = GZ_FILE_VIEW_COMMAND;
				}
				if (logAccessType == LogAccessType.HTTPD) {
					commandPattern = commandPattern.replace(" {0}", "");
					commandPattern = HTTPD_FILE_VIEW_COMMAND_PREFIX + commandPattern;
				}
				String command = MessageFormat.format(commandPattern, relativePath);
				lineCells.add(new TableCell(fileName, FILE_VIEW_URL_PREFIX + URLEncoder.encode(command, URL_ENCODING)));
			}
			else {
				String link = relativePath != null ? FOLDER_VIEW_URL_PREFIX + URLEncoder.encode(relativePath, URL_ENCODING) : LOGS_LIST_URL;
				lineCells.add(new TableCell(fileName, link, null, "text-warning"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedCharsetException(URL_ENCODING);
		}

		// File size
		if (fileSize != null) {
			lineCells.add(new TableCell(String.valueOf(fileSize)));
		}

		// File date
		if (lastModified != null) {
			lineCells.add(new TableCell(lastModified));
		}

		// File actions
		if (!isDirectory) {
			lineCells.add(new TableCell("download", "download?fileName=" + relativePath, "icon-download-alt"));
		}
		else {
			lineCells.add(new TableCell(null));
		}
		
		// return new table line
		return lineCells;
	}
}
