function initPage() {
	
	// Enlarge logAccessConfigId combobox if necessary
	if ($("#logAccessConfigId").width() < 200) {
		$("#logAccessConfigId").width(200);
	}

	// Render logAccessConfigId combobox
	$("#logAccessConfigId").select2();

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
	
	// Render results table (only if there are less than 1500 lines) 
	var resultsSize = $("#resultsSize").val();
	if (resultsSize && resultsSize < 1500) {

		// Render 'size' and 'file' column data
		var dataTableColumnDefs = [];
		$("#resultsTable").find("th").each(function(index) {
			// 'size' column
			if ($(this).text().match(/size/gi)) {
				dataTableColumnDefs.push({
	                render: function ( data, type, row ) {
	                	if (type == "display" || type == "filter" ) {
		                    return (data == "-") ? data : numeral(data).format("0.[0] b");
	                	}
	                	else {
	                		return data;
	                	}
	                },
	                targets: index
	            });
			}
			// 'file' column
			else if ($(this).text().match(/file/gi)) {
				dataTableColumnDefs.push({
					render: function ( data, type, row ) {
						if (type == "sort") {
							var $data = $(data);
							var sortedData = $data.hasClass("text-warning") ? "a" : "b";
							sortedData += $data.text();
							return sortedData;
						}
						else {
							return data;
						}
					},
					targets: index
				});
			}
		});
		
		// Render table using DataTables plugin
		var datatable = $("#resultsTable").dataTable({
			bPaginate: false,
			bStateSave: true,
			fnStateSaveParams: function (oSettings, oData) {
				oData.search.search = "";
			},
			language: {
				info: "Showing _TOTAL_ entries",
				infoEmpty: "Showing 0 entries",
				sEmptyTable: "No entries to show"
			},
			columnDefs: dataTableColumnDefs
		});
		
		// Enable DataTables FixedHeader extension
		var dataTableOffsetTop = $("#displayTypeTABLE").length > 0 ? 215 : 165;
		new $.fn.dataTable.FixedHeader(datatable, { offsetTop: dataTableOffsetTop } );
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
