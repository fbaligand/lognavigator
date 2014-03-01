package fr.icdc.dei.banque.lognavigator.controller;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.icdc.dei.banque.lognavigator.bean.LogAccessConfig;
import fr.icdc.dei.banque.lognavigator.exception.LogAccessException;
import fr.icdc.dei.banque.lognavigator.service.ConfigService;
import fr.icdc.dei.banque.lognavigator.service.LogAccessService;

@Controller
public class DownloadController {
	
	@Autowired
	private ConfigService configService;

	@Autowired
	@Qualifier("facade")
	private LogAccessService logAccessService;
	
	@RequestMapping("/{logAccessConfigId}/download")
	public void download(Model model, 
					  @PathVariable String logAccessConfigId, 
					  @RequestParam(value="fileName") String fileName,
					  HttpServletResponse response
	) throws LogAccessException, IOException {
		
		// Set the HTTP headers for content download
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		// Download the file
		logAccessService.downloadFile(logAccessConfigId, fileName, response.getOutputStream());
	}
	
}
