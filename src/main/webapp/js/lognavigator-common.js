function initPage() {
	
	// Render logAccessConfigId combobox using select2 plugin
	$("#logAccessConfigId").select2({
		width: "element",
	    matcher: function(term, text, opt) {
	         return text.toUpperCase().indexOf(term.toUpperCase()) >= 0 || opt.parent("optgroup").attr("label").toUpperCase().indexOf(term.toUpperCase()) >= 0;
	    }
	});

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
