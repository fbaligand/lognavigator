package fr.icdc.dei.banque.lognavigator.bean;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root of all XML lognavigator configuration 
 */
@XmlRootElement(name="lognavigator-config")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LogNavigatorConfig {

	private Set<LogAccessConfig> logAccessConfigs;

	
	@XmlElement(name="log-access-config")
	public Set<LogAccessConfig> getLogAccessConfigs() {
		if (logAccessConfigs == null) {
			logAccessConfigs = new TreeSet<LogAccessConfig>();
		}
		return logAccessConfigs;
	}
	
	public void setLogAccessConfigs(Set<LogAccessConfig> logAccessConfigs) {
		if (logAccessConfigs instanceof SortedSet) {
			this.logAccessConfigs = logAccessConfigs;
		}
		else {
			this.logAccessConfigs = new TreeSet<LogAccessConfig>(logAccessConfigs);
		}
	}
}
