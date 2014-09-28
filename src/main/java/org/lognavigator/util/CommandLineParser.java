package org.lognavigator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lognavigator.bean.CommandLine;

/**
 * Utility class which to parse a command line and build a bean containing parsed command, options and params.
 */
public class CommandLineParser {

	/**
	 * Regex for a command line token. Three cases :
	 * - token whithout single or double quotes
	 * - token enclosed in single quotes
	 * - token enclosed in double quotes
	 */
	private static final String COMMAND_TOKEN_REGEX = "(?:([^\\s'\"]+)|'((?:[^']|'\\\\'')*)'|\"((?:[^\"]|\"\\\\\"\")*)\")[\\s|]*";

	/**
	 * Parse 'commandLine' and returns CommandLine bean containing parsed command, options, and params
	 * @param commandLine command line to parse
	 * @return CommandLine bean containing parsed command, options, and params
	 */
	public static CommandLine parseCommandLine(String commandLine) {

		CommandLine commandLineBean = new CommandLine();
		commandLineBean.setLine(commandLine);

		Pattern pattern = Pattern.compile(COMMAND_TOKEN_REGEX);
		commandLine = commandLine.trim();
		Matcher result = pattern.matcher(commandLine);

		while (result.find()) {
			String lineElement = result.group(1);
			if (result.group(2) != null) {
				lineElement = result.group(2);
				lineElement = lineElement.replaceAll("'\\\\''", "'");
			}
			if (result.group(3) != null) {
				lineElement = result.group(3);
				lineElement = lineElement.replaceAll("\"\\\\\"\"", "\"");
			}

			if (commandLineBean.getCommand() == null) {
				commandLineBean.setCommand(lineElement);
			} else if (lineElement.startsWith("-")) {
				commandLineBean.addOption(lineElement);
			} else {
				commandLineBean.addParam(lineElement);
			}
		}

		return commandLineBean;
	}

}
