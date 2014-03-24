package org.lognavigator.util;


/**
 * Constants for the web tiers
 */
public class Constants {
	
	///////////////////////////////
	// CONTROLLER AND VIEW NAMES //
	///////////////////////////////
	
	public static final String REDIRECT_LOGS_LIST_CONTROLLER = "redirect:/logs/{0}/list";
	public static final String FOLDER_VIEW_URL_PREFIX = "list?subPath=";
	public static final String LOGS_LIST_URL = "list";
	
	public static final String FORWARD_COMMAND_CONTROLLER = "forward:command";
	public static final String FILE_VIEW_URL_PREFIX = "command?cmd=";

	public static final String MAIN_VIEW = "main-view";
	public static final String PREPARE_MAIN_VIEW = "forward:prepare-main-view";

	
	////////////////////
	// MODEL MAP KEYS //
	////////////////////

	public static final String LOG_ACCESS_CONFIG_ID_KEY = "logAccessConfigId";
	public static final String LOG_ACCESS_CONFIG_IDS_BY_DISPLAY_GROUP_KEY = "logAccessConfigIdsByDisplayGroup";
	public static final String ERROR_TITLE_KEY = "errorTitle";
	public static final String ERROR_MESSAGE_KEY = "errorMessage";
	public static final String SHOW_OPTIONS_KEY = "showOptions";
	public static final String ENCODING_KEY = "encoding";
	public static final String DISPLAY_TYPE_KEY = "displayType";
	public static final String TABLE_HEADERS_KEY = "tableHeaders";
	public static final String TABLE_LINES_KEY = "tableLines";
	public static final String RAW_CONTENT_KEY = "rawContent";
	public static final String TABLE_LAYOUT_CLASS_KEY = "tableLayoutClass";
	public static final String BREADCRUMBS_KEY = "breadcrumbs";

	
	///////////////////
	// TABLE HEADERS //
	///////////////////

	public static final String FILE_TABLE_HEADER = "File";
	public static final String DATE_TABLE_HEADER = "Date";
	public static final String SIZE_TABLE_HEADER = "Size";
	public static final String ACTIONS_TABLE_HEADER = "Actions";
	public static final String LINE_CONTENT_TABLE_HEADER = "Line Content";
	

	//////////////
	// COMMANDS //
	//////////////

	public static final String LIST_COMMAND_START = "ls ";
	public static final String DEFAULT_LIST_COMMAND = LIST_COMMAND_START + "-l";
	public static final String DEFAULT_FILE_VIEW_COMMAND = "tail -1000 {0}";
	
	public static final String GZ_FILE_VIEW_COMMAND = "gzip -dc {0} | tail -1000";
	public static final String TAR_GZ_FILE_VIEW_COMMAND_START = "tar -ztvf ";
	public static final String TAR_GZ_FILE_VIEW_COMMAND = TAR_GZ_FILE_VIEW_COMMAND_START + "{0}";
	public static final String TAR_GZ_FILE_VIEW_COMMAND_END = "| tar -ztv";

	public static final String TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START = "tar -O -zxf ";
	public static final String TAR_GZ_CONTENT_FILE_VIEW_COMMAND = TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START + "{0} {1} | {2}";

	public static final String HTTPD_FILE_VIEW_COMMAND_START = "curl -s ";
	public static final String HTTPD_FILE_VIEW_COMMAND_PREFIX = HTTPD_FILE_VIEW_COMMAND_START + "{0} | ";
	
	public static final String GREP_COMMAND_START = "grep ";

	
	////////////
	// OTHERS //
	////////////

	public static final String DIRECTORY_RIGHT = "d";
	public static final String DEFAULT_ENCODING_OPTION = "UTF-8";
	public static final String DEFAULT_DISPLAY_TYPE_OPTION = "RAW";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String URL_ENCODING = "UTF-8";
	public static final String TAR_GZ_CONTENT_SPLIT = ".tar.gz!";

	public static final String TABLE_LAYOUT_FULL_WIDTH = "col-md-13";
	public static final String TABLE_LAYOUT_CENTERED = "col-md-offset-2 col-md-8";

}
