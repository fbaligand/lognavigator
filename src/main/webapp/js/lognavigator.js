function initPage() {
	
	// Enlarge logAccessConfigId combobox if necessary
	if ($("#logAccessConfigId").width() < 200) {
		$("#logAccessConfigId").width(200);
	}
	
	// Process logAccessConfigId combobox change
	$("#logAccessConfigId").change(function(){
		window.location = "../" + $(this).val() + "/list";
	});
	
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
	$("#resultsTable").dataTable({
		bPaginate: false
	});

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
	window.location = "download?" + $("#cmd").serialize();
}

$(initPage);
