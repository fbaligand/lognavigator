package org.lognavigator.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lognavigator.bean.CommandLine;

public class CommandLineParserTest {

	@Test
	public void testParseCommandLine_Basic() throws Exception {
		
		// given
		String commandLine =  "grep -c toto 'titi' \"tata\"";
		
		// when 
		CommandLine commandLineBean = CommandLineParser.parseCommandLine(commandLine);
		
		// then
		assertEquals(commandLine, commandLineBean.getLine());
		assertEquals("grep", commandLineBean.getCommand());
		assertEquals(1, commandLineBean.getOptions().size());
		assertEquals("-c", commandLineBean.getOptions().get(0));
		assertEquals(3, commandLineBean.getParams().size());
		assertEquals("toto", commandLineBean.getParams().get(0));
		assertEquals("titi", commandLineBean.getParams().get(1));
		assertEquals("tata", commandLineBean.getParams().get(2));
	}

	@Test
	public void testParseCommandLine_QuoteInParam() throws Exception {
		
		// given
		String commandLine =  "grep 'to'\\''to' \"ti\"\\\"\"ti\"";
		
		// when 
		CommandLine commandLineBean = CommandLineParser.parseCommandLine(commandLine);
		
		// then
		assertEquals("grep", commandLineBean.getCommand());
		assertEquals(2, commandLineBean.getParams().size());
		assertEquals("to'to", commandLineBean.getParams().get(0));
		assertEquals("ti\"ti", commandLineBean.getParams().get(1));
	}

	@Test
	public void testParseCommandLine_EmptyParam() throws Exception {
		
		// given
		String commandLine =  "grep '' toto.txt";
		
		// when 
		CommandLine commandLineBean = CommandLineParser.parseCommandLine(commandLine);
		
		// then
		assertEquals("grep", commandLineBean.getCommand());
		assertEquals(2, commandLineBean.getParams().size());
		assertEquals("", commandLineBean.getParams().get(0));
		assertEquals("toto.txt", commandLineBean.getParams().get(1));
	}

	@Test
	public void testParseCommandLine_NoCommand() throws Exception {
		
		// given
		String commandLine =  "> toto.txt";
		
		// when 
		CommandLine commandLineBean = CommandLineParser.parseCommandLine(commandLine);
		
		// then
		assertEquals(">", commandLineBean.getCommand());
		assertEquals(1, commandLineBean.getParams().size());
		assertEquals("toto.txt", commandLineBean.getParams().get(0));
	}

}
