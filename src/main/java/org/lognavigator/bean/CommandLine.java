package org.lognavigator.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean containing all elements of a linux command line : command, options and params
 */
public class CommandLine {
	
	private String line;
	private String command;
	private List<String> options = new ArrayList<String>();
	private List<String> params = new ArrayList<String>();
	
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}

	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public List<String> getOptions() {
		return options;
	}
	public void setOptions(List<String> options) {
		this.options = options;
	}
	public String getOption(int index) {
		return getOptions().get(index);
	}
	public void addOption(String option) {
		getOptions().add(option);
	}
	
	public List<String> getParams() {
		return params;
	}
	public void setParams(List<String> params) {
		this.params = params;
	}
	public String getParam(int index) {
		return getParams().get(index);
	}
	public void addParam(String param) {
		getParams().add(param);
	}
}
