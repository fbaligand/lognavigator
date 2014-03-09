package org.lognavigator.bean;

/**
 * Bean containing informations of a cell of a log line for pretty display in a HTML table
 */
public class TableCell {

	private String content;
	private String link;
	private String linkIcon;
	private String cssClass;
	
	
	public TableCell(String content) {
		this.content = content;
	}

	public TableCell(String content, String link) {
		this.content = content;
		this.link = link;
	}
	
	public TableCell(String content, String link, String linkIcon) {
		this.content = content;
		this.link = link;
		this.linkIcon = linkIcon;
	}
	
	public TableCell(String content, String link, String linkIcon, String cssClass) {
		this.content = content;
		this.link = link;
		this.linkIcon = linkIcon;
		this.cssClass = cssClass;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

	public String getLinkIcon() {
		return linkIcon;
	}

	public void setLinkIcon(String linkIcon) {
		this.linkIcon = linkIcon;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
}
