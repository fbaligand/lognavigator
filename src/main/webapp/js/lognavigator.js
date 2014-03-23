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
	$("#commandForm button.btn-primary").click(submitCommandForm);
	$("#commandForm input:radio").change(submitCommandForm);
	
	// Auto-adapt RAW content width
	if ($("pre").length == 1 && $("pre").width() < $(window).width()) {
		$("pre").addClass("nofloat");
	}
	
	// Render results table
	$("#resultsTable").dataTable({
		bPaginate: false
//		sPaginationType: "bootstrap",
//		sDom: "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>"
	});

}

function submitCommandForm() {
	$("#commandForm").submit();
}

function oldInitPage() {
	
	// Process logAccessConfigId change
	$("#logAccessConfigId").change(function(){
		window.location = "../" + $(this).val() + "/list";
	});
	
	// Render results table
	$("#resultsTable").dataTable({
		bPaginate: false,
		sPaginationType: "bootstrap",
		sDom: "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>"
	});

	// Encoding buttons
	var encoding = $("#encoding").val();
	$("#encoding-btn-group button[data-value='"+encoding+"']").addClass("active");
	$("#encoding-btn-group button").click(function(){
		$("#encoding").val($(this).attr("data-value"));
	});

	// DisplayType buttons
	var displayType = $("#displayType").val();
	$("#displaytype-btn-group button[data-value='"+displayType+"']").addClass("active");
	$("#displaytype-btn-group button").click(function(){
		$("#displayType").val($(this).attr("data-value"));
	});
}

$(initPage);
