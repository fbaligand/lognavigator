function initPage() {
	
	// Enlarge logAccessConfigId combobox if necessary
	if ($("#logAccessConfigId").width() < 200) {
		$("#logAccessConfigId").width(200);
	}
	
	// Process logAccessConfigId combobox change
	$("#logAccessConfigId").change(function(){
		window.location = "../" + $(this).val() + "/list";
	});
	
	// Placeholder in 'cmd' input
	$("#cmd").placeholder();
	
	// Encoding buttons
	var checkedEncodingId = $("input[name=encoding][checked]").attr("id");
	$("label[for=" + checkedEncodingId + "]").addClass("active");
	
	// DisplayType buttons
	var checkedDisplayTypeId = $("input[name=displayType][checked]").attr("id");
	$("label[for=" + checkedDisplayTypeId + "]").addClass("active");

	// Auto-Submit command form 
	$("#downloadButton").click(downloadCommandResult);
	$("#executeButton").click(submitCommandForm);
	$("#commandForm input:radio").change(submitCommandForm);
	
	// Auto-adapt RAW content width
	if ($("pre").length == 1 && $("pre").width() < $(window).width()) {
		$("pre").addClass("nofloat");
	}
	
	// Render results table
	var resultsSize = $("#resultsSize").val();
	if (resultsSize && resultsSize < 1500) {
		$("#resultsTable").dataTable({
			bPaginate: false,
			bStateSave: true,
			fnStateSaveParams: function (oSettings, oData) {
				oData.oSearch.sSearch = "";
			},
			language: {
			    info: "Showing _TOTAL_ entries",
			    infoEmpty: "Showing 0 entries",
			    sEmptyTable: "No entries to show"
			  }
		});
	}
}

function submitCommandForm() {
	if ($("#cmd").val() != "") {
		$("#commandForm").submit();
	}
	else {
		$("#executeButton").blur();
	}
}

function downloadCommandResult() {
	$(this).blur();
	if ($("#cmd").val() != "") {
		var fileName = $("ul.breadcrumb li.active").text();
		fileName = fileName.replace(/\.tar\.gz$|\.gz$/, ".log").replace(/\.log\.log/, ".log");
		window.location = "download?" + $("#cmd").serialize() + "&fileName=" + escape(fileName);
	}
}

$(initPage);
