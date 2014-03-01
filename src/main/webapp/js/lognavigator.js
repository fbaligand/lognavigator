function initPage() {
	
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
