function initLognavigatorRawView() {
	
	// Auto-adapt RAW content width, if there is no horizontal scrollbar
    if ($(document).width() <= $(window).width()) {
    	$("pre").addClass("nofloat");
	}
}

$(window).load(initLognavigatorRawView);
