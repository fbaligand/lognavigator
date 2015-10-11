package org.lognavigator.taglib;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import net.schmizz.sshj.common.IOUtils;

import org.lognavigator.util.Constants;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Taglib which writes to JSP output, a raw content inputstream.
 * It allows to write at the same rythm that we read the inputstream.
 */
public class RawContentTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doEndTag() throws JspException {
		
		// Get input and ouput variables
		Reader rawContent = (Reader) pageContext.getAttribute(Constants.RAW_CONTENT_KEY, PageContext.REQUEST_SCOPE);
		JspWriter out = pageContext.getOut();
		
		try {
			// Copy input (rawContent) to output (out)
			char[] buffer = new char[StreamUtils.BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = rawContent.read(buffer)) != -1) {
				String stringToWrite = new String(buffer, 0, bytesRead);
				stringToWrite = HtmlUtils.htmlEscape(stringToWrite);
				out.write(stringToWrite);
			}
			out.flush();

			return EVAL_PAGE;
		}
		catch (IOException e) {
			throw new JspException(e);
		}
		finally {
			IOUtils.closeQuietly(rawContent);
		}
	}
}
