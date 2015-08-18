package org.lognavigator.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lognavigator.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;


/**
 * Component which process all errors that controllers throw
 */
@Component
public class LogNavigatorHandlerExceptionResolver implements HandlerExceptionResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogNavigatorHandlerExceptionResolver.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
		LOGGER.error("Error when calling web action", exception);
		
		// Compute error message to display
		String errorTitle = exception.getClass().getSimpleName().replaceFirst("(Error|Exception)", " Error");
		String errorMessage = exception.getMessage();
		
		ModelAndView modelAndView = new ModelAndView(Constants.VIEW_ERROR);
		modelAndView.addObject(Constants.ERROR_TITLE_KEY, errorTitle);
		modelAndView.addObject(Constants.ERROR_MESSAGE_KEY, errorMessage);
		return modelAndView;
	}

}
