package org.lognavigator.exception;

/**
 * Exception thrown when connected user is not authorized to access to a specific log set
 */
public class AuthorizationException extends Exception {

	private static final long serialVersionUID = 1L;

	public AuthorizationException(String message) {
		super(message);
	}

}
