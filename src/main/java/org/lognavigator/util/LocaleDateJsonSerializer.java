package org.lognavigator.util;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ser.std.DateSerializer;

public class LocaleDateJsonSerializer extends DateSerializer {
	
	private static final long serialVersionUID = 1L;

	public LocaleDateJsonSerializer() {
		super(false, new SimpleDateFormat(Constants.DATE_FORMAT));
	}
	
}
