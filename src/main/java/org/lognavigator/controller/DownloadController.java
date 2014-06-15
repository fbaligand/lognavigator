package org.lognavigator.controller;

import static org.lognavigator.util.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lognavigator.exception.LogAccessException;
import org.lognavigator.service.LogAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadController {
	
	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@RequestMapping("/{logAccessConfigId}/download")
	public void download(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="fileName") String fileName,
					  @RequestParam(value="cmd", required=false) String cmd,
					  HttpServletRequest request,
					  HttpServletResponse response
	) throws LogAccessException, IOException {
		
		InputStream resultContentStream = null;
		
		// Special case : Content to download is the result of 'command'
		if (cmd != null) {
			resultContentStream = logAccessService.executeCommand(logAccessConfigId, cmd);
		}
		
		// Special case : Content to download is a .tar.gz file entry
		else if (fileName.contains(TAR_GZ_CONTENT_SPLIT)) {
			
			// Compute tar.gz sub file download command
			String[] fileNameSplit = fileName.split("!");
			String tarGzFilename = fileNameSplit[0];
			fileName = fileNameSplit[1];
			String downloadCommand = MessageFormat.format(TAR_GZ_CONTENT_FILE_VIEW_COMMAND, tarGzFilename, fileName, "cat");
			
			// Launch command for download
			resultContentStream = logAccessService.executeCommand(logAccessConfigId, downloadCommand);
		}
		
		// Do we gzip the result stream ?
		OutputStream responseStream = response.getOutputStream();
		String acceptEncodingHeader = request.getHeader("Accept-Encoding");
		if (acceptEncodingHeader != null && acceptEncodingHeader.toLowerCase().contains("gzip")) {
			if (cmd != null || !fileName.matches(COMPRESSED_FILE_REGEX)) {
				response.setHeader("Content-Encoding", "gzip");
				responseStream = new GZIPOutputStream(responseStream);
			}
		}
		
		// Set the HTTP headers for content download
		String attachmentFilename = fileName.contains("/") ? fileName.substring(fileName.lastIndexOf('/')+1) : fileName;
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + attachmentFilename);
		
		// Download the file
		if (resultContentStream != null) {
			FileCopyUtils.copy(resultContentStream, responseStream);
		}
		else {
			logAccessService.downloadFile(logAccessConfigId, fileName, responseStream);
		}
	}
	
}
