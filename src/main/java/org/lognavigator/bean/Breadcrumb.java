package org.lognavigator.bean;

public class Breadcrumb {

	private String label;
	private String link;


	public Breadcrumb(String label, String link) {
		this.label = label;
		this.link = link;
	}

	public Breadcrumb(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String getLink() {
		return link;
	}
}
