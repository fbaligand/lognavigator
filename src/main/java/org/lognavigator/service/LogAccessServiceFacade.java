package org.lognavigator.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.lognavigator.bean.FileInfo;
import org.lognavigator.bean.LogAccessConfig;
import org.lognavigator.exception.LogAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Service Facade to the different implementations of LogAccessService (ssh, local, fake)
 */
@Service
@Qualifier("facade")
public class LogAccessServiceFacade implements LogAccessService {
	
	@Value("${fake:false}")
	private boolean isFakeEnabled;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	@Qualifier("ssh")
	private LogAccessService logAccessServiceSsh;
	
	@Autowired
	@Qualifier("local")
	private LogAccessService logAccessServiceLocal;
	
	@Autowired
	@Qualifier("httpd")
	private LogAccessService logAccessServiceHttpd;
	
	@Autowired
	@Qualifier("fake")
	private LogAccessService logAccessServiceFake;
	
	@Override
	public InputStream executeCommand(String logAccessConfigId, String shellCommand) throws LogAccessException {
		return getLogAccessServiceDelegate(logAccessConfigId).executeCommand(logAccessConfigId, shellCommand);
	}

	@Override
	public void downloadFile(String logAccessConfigId, String fileName, OutputStream downloadOutputStream) throws LogAccessException {
		getLogAccessServiceDelegate(logAccessConfigId).downloadFile(logAccessConfigId, fileName, downloadOutputStream);
	}

	@Override
	public Set<FileInfo> listFiles(String logAccessConfigId, String subPath) throws LogAccessException {
		return getLogAccessServiceDelegate(logAccessConfigId).listFiles(logAccessConfigId, subPath);
	}

	/**
	 * Return the suitable LogAccessService delegate service :
	 * - fake : if isFakeEnabled=true
	 * - local : if logAccessConfig type is LOCAL
	 * - httpd : if logAccessConfig type is HTTPD
	 * - ssh : if logAccessConfig type is SSH
	 * 
	 * @param logAccessConfigId log access config id
	 */
	private LogAccessService getLogAccessServiceDelegate(String logAccessConfigId) {
		if (isFakeEnabled) {
			return logAccessServiceFake;
		}
		
		// Get the LogAccessConfig
		LogAccessConfig logAccessConfig = configService.getLogAccessConfig(logAccessConfigId);
		
		switch (logAccessConfig.getType()) {
		case LOCAL:
			return logAccessServiceLocal;
		case HTTPD: 
			return logAccessServiceHttpd;
		case SSH: 
			return logAccessServiceSsh;
		default:
			throw new IllegalArgumentException("Unknown type for log access config " + logAccessConfigId);
		}
	}
}
