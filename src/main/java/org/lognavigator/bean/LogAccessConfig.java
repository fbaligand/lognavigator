package org.lognavigator.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean containing access config information to access one set of logs.
 * Parameters to define by type :
 * - LOCAL : directory
 * - SSH : user, host, directory
 * - HTTPD: url
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"id", "type", "user", "privatekey", "password", "host", "directory", "url", "proxy", "authorizedUsers", "authorizedRoles", "displayGroup", "trust", "preCommand", "defaultEncoding"})
public class LogAccessConfig implements Comparable<LogAccessConfig> {
	
	public static final String EVERYONE_IS_AUTHORIZED = "*";

	public static enum LogAccessType {SSH, LOCAL, HTTPD};
	
	@XmlAttribute(required=true)
	private String id;
	
	@XmlAttribute(required=true)
	private LogAccessType type;

	@XmlAttribute
	private String host;

	@XmlAttribute
	private String user;

	@XmlAttribute
	private String privatekey;
	
	@XmlAttribute
	private String password;
	
	@XmlAttribute
	private String directory;

	@XmlAttribute
	private String url;
	
	@XmlAttribute(name="authorized-users")
	private List<String> authorizedUsers;
	
	@XmlAttribute(name="authorized-roles")
	private List<String> authorizedRoles;
	
	@XmlAttribute(name="display-group")
	private String displayGroup;
	
	@XmlAttribute(name="trust")
	private boolean trust;
	
	@XmlAttribute(name="proxy")
	private String proxy;
	
	@XmlAttribute(name="pre-command")
	private String preCommand;
	
	@XmlAttribute(name="default-encoding")
	private String defaultEncoding;
	
	@XmlTransient
	private OsType osType;
	
	@XmlTransient
	private Boolean isPerlInstalled;
	
	@XmlTransient
	private Boolean isLsWithGroupDirectoriesFirstSupported;
	
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public LogAccessConfig() {
		authorizedUsers = new ArrayList<String>();
		authorizedUsers.add(EVERYONE_IS_AUTHORIZED);
		
		authorizedRoles = new ArrayList<String>();
		authorizedRoles.add(EVERYONE_IS_AUTHORIZED);
	}
	
	public LogAccessConfig(String id, LogAccessType type, String host, String directory, String user) {
		this();
		this.id = id;
		this.type = type;
		this.host = host;
		this.directory = directory;
		this.user = user;
	}

	public LogAccessConfig(String id, LogAccessType type, String host, String directory) {
		this();
		this.id = id;
		this.type = type;
		this.directory = directory;
	}
	
	public LogAccessConfig(String id, LogAccessType type, String url) {
		this();
		this.id = id;
		this.type = type;
		this.url = url;
	}
	

	/////////////
	// METHODS //
	/////////////
	
	public boolean isEveryUserAuthorized() {
		return this.authorizedUsers.isEmpty() || this.authorizedUsers.get(0).equals(EVERYONE_IS_AUTHORIZED);
	}
	
	public boolean isEveryRoleAuthorized() {
		return this.authorizedRoles.isEmpty() || this.authorizedRoles.get(0).equals(EVERYONE_IS_AUTHORIZED);
	}
	
	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public LogAccessType getType() {
		return type;
	}
	public void setType(LogAccessType type) {
		this.type = type;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPrivatekey() {
		return privatekey;
	}
	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<String> getAuthorizedUsers() {
		return authorizedUsers;
	}
	public void setAuthorizedUsers(List<String> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}
	
	public List<String> getAuthorizedRoles() {
		return authorizedRoles;
	}
	public void setAuthorizedRoles(List<String> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	public String getDisplayGroup() {
		return displayGroup;
	}
	public void setDisplayGroup(String displayGroup) {
		this.displayGroup = displayGroup;
	}

	public boolean isTrust() {
		return trust;
	}
	public void setTrust(boolean trust) {
		this.trust = trust;
	}

	public String getProxy() {
		return proxy;
	}
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getPreCommand() {
		return preCommand;
	}
	public void setPreCommand(String preCommand) {
		this.preCommand = preCommand;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public OsType getOsType() {
		return osType;
	}
	public void setOsType(OsType osType) {
		this.osType = osType;
	}

	public Boolean isPerlInstalled() {
		return isPerlInstalled;
	}
	public void setPerlInstalled(Boolean isPerlInstalled) {
		this.isPerlInstalled = isPerlInstalled;
	}
	
	public Boolean isLsWithGroupDirectoriesFirstSupported() {
		return isLsWithGroupDirectoriesFirstSupported;
	}
	public void setLsWithGroupDirectoriesFirstSupported(Boolean isLsWithGroupDirectoriesFirstSupported) {
		this.isLsWithGroupDirectoriesFirstSupported = isLsWithGroupDirectoriesFirstSupported;
	}

	
	//////////////////////////////////////////////
	// TOSTRING / COMPARETO / EQUALS / HASHCODE // 
	//////////////////////////////////////////////
	
	@Override
	public String toString() {
		switch (type) {
		case SSH:
			return "LogAccessConfig [" + id + ": SSH " + user + "@" + host + ":" + directory + "]";
		case LOCAL:
			return "LogAccessConfig [" + id + ": LOCAL " + directory + "]";
		case HTTPD:
			return "LogAccessConfig [" + id + ": HTTPD " + url + "]";
		default:
			throw new IllegalStateException("Unknown LogAccessConfig type : " + type);
		}
	}
	
	@Override
	public int compareTo(LogAccessConfig other) {
		return this.id.compareTo(other.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogAccessConfig other = (LogAccessConfig) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
