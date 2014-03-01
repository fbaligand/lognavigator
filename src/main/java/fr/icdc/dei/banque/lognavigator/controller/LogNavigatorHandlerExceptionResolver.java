package fr.icdc.dei.banque.lognavigator.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import fr.icdc.dei.banque.lognavigator.util.WebConstants;

/**
 * Component which process all errors that controllers throw
 */
@Component
public class LogNavigatorHandlerExceptionResolver implements HandlerExceptionResolver {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogNavigatorHandlerExceptionResolver.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		LOGGER.error("Error when calling web action", ex);
		
		ModelAndView modelAndView = new ModelAndView(WebConstants.PREPARE_MAIN_VIEW);
		modelAndView.addObject(WebConstants.ERROR_MESSAGE_KEY, ex.toString());
		return modelAndView;
	}

}
