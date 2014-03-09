package org.lognavigator.exception;

/**
 * Exception thrown when a error occurs while accessing to a local or remote log 
 */
public class LogAccessException extends Exception {

	private static final long serialVersionUID = 1L;

	public LogAccessException(String message) {
		super(message);
	}

	public LogAccessException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
