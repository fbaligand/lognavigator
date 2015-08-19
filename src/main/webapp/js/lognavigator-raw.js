function initLognavigatorRawView() {
	
	// Auto-adapt RAW content width
	var $pre = $("pre");
	if ($pre.length == 1 && $pre.width() < $(window).width()) {
		$pre.addClass("nofloat");
	}
}

$(initLognavigatorRawView);
