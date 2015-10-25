package org.lognavigator.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.schmizz.sshj.common.IOUtils;

import org.lognavigator.exception.ConfigException;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * UserDetailsService based on a properties file, which is reloaded each time it is updated. 
 * Properties file format is :
 * username=password,grantedAuthority[,grantedAuthority][,enabled|disabled]
 */
public class ReloadableInMemoryUserDetailsService implements UserDetailsService {
	
	/** Link to users properties file */
	private Resource usersPropertiesResource;

	/** Last time that 'usersPropertiesResource' has been modified */
	private long usersFileLastModified;
	
	/** Users InMemory Store */
	private InMemoryUserDetailsManager inMemoryUserDetailsManager;
	
	/**
	 * Load or Reload the users from users file, if necessary
	 */
	synchronized void reloadUsersIfNecessary() throws ConfigException {
		
		// Should we reload users file ? (because it has been modified since last reload)
		long lastModified;
		try {
			lastModified = usersPropertiesResource.lastModified();
			boolean needReload = (lastModified > this.usersFileLastModified);
			if (!needReload) {
				return;
			}
		} catch (IOException e) {
			throw new ConfigException("Error when trying to access users file " + usersPropertiesResource, e);
		}
		
		// Update the lastModified date information for users file
		this.usersFileLastModified = lastModified;
		
		// Load users file
		InputStream usersFileInputStream = null;
		try {
			usersFileInputStream = usersPropertiesResource.getInputStream();
			Properties usersProperties = new Properties();
			usersProperties.load(usersFileInputStream);
			this.inMemoryUserDetailsManager = new InMemoryUserDetailsManager(usersProperties);
		}
		catch (IOException e) {
			throw new ConfigException("I/O error when trying to load users file " + usersPropertiesResource, e);
		}
		finally {
			IOUtils.closeQuietly(usersFileInputStream);
		}
	}

	
	@Override
	public synchronized UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		reloadUsersIfNecessary();
		return inMemoryUserDetailsManager.loadUserByUsername(username);
	}

	/**
	 * Init the Spring Service
	 */
	@PostConstruct
	public synchronized void init() throws ConfigException {

		// Does users file is configured ?
		if (usersPropertiesResource == null) {
			throw new ConfigException("'users' property must be set");
		}
		
		// Does users file exist ?
		if (!usersPropertiesResource.exists()) {
			throw new ConfigException("The users file " + usersPropertiesResource + " does not exist");
		}
		
		reloadUsersIfNecessary();
	}

	public void setProperties(Resource usersProperties) {
		this.usersPropertiesResource = usersProperties;
	}
}
