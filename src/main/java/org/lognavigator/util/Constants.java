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
	
	public static final String FILE_VIEW_URL_PREFIX = "command?cmd=";

	public static final String VIEW_TABLE = "table";
	public static final String VIEW_SCROLLER_TABLE = "scroller-table";
	public static final String VIEW_RAW = "raw";
	public static final String VIEW_ERROR = "error";

	
	////////////////////
	// MODEL MAP KEYS //
	////////////////////

	public static final String LOG_ACCESS_CONFIG_ID_KEY = "logAccessConfigId";
	public static final String LOG_ACCESS_CONFIG_IDS_BY_DISPLAY_GROUP_KEY = "logAccessConfigIdsByDisplayGroup";
	public static final String ERROR_TITLE_KEY = "errorTitle";
	public static final String ERROR_MESSAGE_KEY = "errorMessage";
	public static final String BLOCKING_ERROR_KEY = "blockingError";
	public static final String WARN_TITLE_KEY = "warnTitle";
	public static final String WARN_MESSAGE_KEY = "warnMessage";
	public static final String SHOW_OPTIONS_KEY = "showOptions";
	public static final String ENCODING_KEY = "encoding";
	public static final String DISPLAY_TYPE_KEY = "displayType";
	public static final String TABLE_HEADERS_KEY = "tableHeaders";
	public static final String TABLE_LINES_KEY = "tableLines";
	public static final String RAW_CONTENT_KEY = "rawContent";
	public static final String TABLE_LAYOUT_CLASS_KEY = "tableLayoutClass";
	public static final String BREADCRUMBS_KEY = "breadcrumbs";
	public static final String AJAX_URL_KEY = "ajaxUrl";
	public static final String HIDE_MESSAGES_KEY = "hideMessages";
	
	///////////////////
	// TABLE HEADERS //
	///////////////////

	public static final String FILE_TABLE_HEADER = "File";
	public static final String DATE_TABLE_HEADER = "Date";
	public static final String SIZE_TABLE_HEADER = "Size";
	public static final String ACTIONS_TABLE_HEADER = "Actions";
	public static final String LINE_NUMBER_TABLE_HEADER = "#";
	public static final String LINE_CONTENT_TABLE_HEADER = "Line Content";
	

	//////////////
	// COMMANDS //
	//////////////

	public static final String DEFAULT_LIST_COMMAND = "ls";
	public static final String PERL_LIST_COMMAND_TEMPLATE = "perl -e ''foreach $filename (`<command>`) '{' chomp($filename); @stat=stat(\"{0}/$filename\"); printf \"%x %d %d000 %s\\n\", @stat[2] & 0040000, @stat[7], @stat[9], $filename; '}'''";
	public static final String PERL_LIST_COMMAND_LINUX_GROUP_DIRECTORIES_FIRST = PERL_LIST_COMMAND_TEMPLATE.replace("<command>", "ls -t --group-directories-first \"{0}\" | head -{1,number,#}");
	public static final String PERL_LIST_COMMAND_FALLBACK = PERL_LIST_COMMAND_TEMPLATE.replace("<command>", "ls -tF \"{0}\" | grep \"/\\$\" | head -{1,number,#} | sed \"s/\\\\/\\$//g\" && ls -tF \"{0}\" | grep -v \"/\\$\" | head -{1,number,#} | sed \"s/*\\$//g\"");
	
	public static final String DEFAULT_FILE_VIEW_COMMAND = "tail -1000 {0}";
	
	public static final String GZ_FILE_VIEW_COMMAND = "gzip -dc {0} | tail -1000";
	
	public static final String TAR_GZ_FILE_VIEW_COMMAND_START = "tar -ztvf ";
	public static final String TAR_GZ_FILE_VIEW_COMMAND = TAR_GZ_FILE_VIEW_COMMAND_START + "{0} | sort -r -k 4,5";
	public static final String TAR_GZ_FILE_VIEW_COMMAND_END = " | tar -ztv | sort -r -k 4,5";

	public static final String TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START = "tar -O -zxf ";
	public static final String TAR_GZ_CONTENT_FILE_VIEW_COMMAND = TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START + "{0} {1} | {2}";

	public static final String HTTPD_FILE_VIEW_COMMAND_START = "curl -ksS ";
	public static final String HTTPD_FILE_VIEW_COMMAND_PREFIX = HTTPD_FILE_VIEW_COMMAND_START + "{0} | ";
	public static final String TAR_GZ_CONTENT_FILE_VIEW_COMMAND_USING_CURL_REGEX = HTTPD_FILE_VIEW_COMMAND_START + "(.+.tar.gz) \\| " + TAR_GZ_CONTENT_FILE_VIEW_COMMAND_START.replace("f", "") + "(\\S+) \\| tail -1000";
	
	public static final String TWO_PARAMS_COMMAND_REGEX = "(grep|egrep|awk|gawk)";

	public static final String DEFAULT_FORBIDDEN_COMMANDS = "rm,rmdir,mkdir,mv,kill,killall,ssh,chmod,chown,vi,touch";
	public static final String FORBIDDEN_COMMANDLINE_REGEX = "\\s*{0}\\s+.*|.*[\\|;`]\\s*{0}(\\s+.*)?";

	
	////////////
	// OTHERS //
	////////////

	
	public static final String UTF8_ENCODING = "UTF-8";
	public static final String ISO_ENCODING = "ISO-8859-1";
	public static final String DIRECTORY_RIGHT = "d";
	public static final String DEFAULT_ENCODING_OPTION = UTF8_ENCODING;
	public static final String DEFAULT_DISPLAY_TYPE_OPTION = "RAW";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String TAR_GZ_DATE_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String URL_ENCODING = UTF8_ENCODING;
	public static final String TAR_GZ_CONTENT_SPLIT = ".tar.gz!";
	public static final String COMPRESSED_FILE_REGEX = ".*\\.(gz|tgz|zip|bz2)";

	public static final String TABLE_LAYOUT_FULL_WIDTH = "full-width";
	public static final String TABLE_LAYOUT_CENTERED = "col-md-offset-2 col-md-8";

}
