package org.lognavigator.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

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

import static org.lognavigator.util.Constants.*;

@Controller
public class DownloadController {
	
	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@RequestMapping("/{logAccessConfigId}/download")
	public void download(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="fileName", required=false) String fileName,
					  @RequestParam(value="cmd", required=false) String cmd,
					  HttpServletResponse response
	) throws LogAccessException, IOException {
		
		// Validate input
		if (fileName == null && cmd == null) {
			throw new LogAccessException("You must specify 'fileName' or 'cmd' parameter");
		}

		InputStream resultContentStream = null;
		
		// Special case : Content to download is the result of 'command'
		if (cmd != null) {
			fileName = "result.log";
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
		
		// Set the HTTP headers for content download
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		// Download the file
		if (resultContentStream != null) {
			FileCopyUtils.copy(resultContentStream, response.getOutputStream());
		}
		else {
			logAccessService.downloadFile(logAccessConfigId, fileName, response.getOutputStream());
		}
	}
	
}
