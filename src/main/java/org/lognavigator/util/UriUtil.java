package org.lognavigator.util;

import static org.lognavigator.util.Constants.URL_ENCODING;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Utility functions for URI processing
 */
public class UriUtil {

	/**
	 * URI encode and return the parameter
	 */
	public static String encode(String param) {
		try {
			return URLEncoder.encode(param, URL_ENCODING);
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedCharsetException(URL_ENCODING);
		}
	}
}
