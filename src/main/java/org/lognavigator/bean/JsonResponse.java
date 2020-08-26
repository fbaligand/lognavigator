package org.lognavigator.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class JsonResponse<T> {

	private T data;
	private String warnTitle;
	private String warnMessage;
	private String errorTitle;
	private String errorMessage;

	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public String getWarnTitle() {
		return warnTitle;
	}
	public void setWarnTitle(String warnTitle) {
		this.warnTitle = warnTitle;
	}
	public String getWarnMessage() {
		return warnMessage;
	}
	public void setWarnMessage(String warnMessage) {
		this.warnMessage = warnMessage;
	}
	public String getErrorTitle() {
		return errorTitle;
	}
	public void setErrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
