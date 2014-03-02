package fr.icdc.dei.banque.lognavigator.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.icdc.dei.banque.lognavigator.exception.LogAccessException;
import fr.icdc.dei.banque.lognavigator.service.LogAccessService;
import static fr.icdc.dei.banque.lognavigator.util.Constants.*;

@Controller
public class DownloadController {
	
	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@RequestMapping("/{logAccessConfigId}/download")
	public void download(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="fileName") String fileName,
					  HttpServletResponse response
	) throws LogAccessException, IOException {

		// Special case : .tar.gz sub file
		InputStream resultContentStream = null;
		
		if (fileName.contains(TAR_GZ_CONTENT_SPLIT)) {
			
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
