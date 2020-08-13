function initLognavigatorTableView() {
	
	// Render results table (only if there are less than 1500 lines) 
	var resultsSize = $("#resultsSize").val();
	if (resultsSize && resultsSize < 1500) {
		renderResultsTable();
	}
}

function renderResultsTable() {
	
	// Render 'size' and 'file' column data
	var dataTableColumnDefs = [];
	$("#resultsTable").find("th").each(function(index) {
		// 'file' column
		if ($(this).text().match(/file/gi)) {
			dataTableColumnDefs.push({
				render: function ( data, type, row ) {
					if (type == "sort") {
						var $data = $(data);
						var sortedData = $data.hasClass("text-warning") ? "a" : "b";
						sortedData += $data.text();
						return sortedData;
					}
					else if (type == "filter" ) {
						return $(data).text();
					}
					else {
						return data;
					}
				},
				targets: index,
				type: "string"
			});
		}
		// 'size' column
		else if ($(this).text().match(/size/gi)) {
			dataTableColumnDefs.push({
				render: function ( data, type, row ) {
					if (type == "display" || type == "filter" ) {
						return (data == "-") ? data : numeral(data).format("0.[0] b");
					}
					else {
						return data;
					}
				},
				targets: index,
				type: "num"
			});
		}
		// 'actions' column
		else if ($(this).text().match(/actions/gi)) {
			dataTableColumnDefs.push({
				targets: index,
				orderable: false,
				searchable: false
			});
		}
	});
	
	// Don't sort "Parent Folder" line
	var fnDrawCallback = null;
	var parentFolderLink = $("#resultsTable a[title='Parent Folder']");
	if (parentFolderLink.length > 0) {
		var parentFolderLine = parentFolderLink.closest("tr").remove();
		fnDrawCallback = function() {
			$("#resultsTable tbody").prepend(parentFolderLine);
		}
	}

	// Fix table head line position using FixedHeader extension
	var fixedHeader = {
		headerOffset: $("#displayTypeTABLE").length > 0 ? 215 : 165
	};

	// Render table using DataTables plugin
	var datatable = $("#resultsTable").dataTable({
		fixedHeader: fixedHeader,
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
		columnDefs: dataTableColumnDefs,
		fnDrawCallback: fnDrawCallback
	});
}

$(initLognavigatorTableView);
