function initLognavigatorScrollerTableView() {
	renderResultsTable();
}

function renderFile(data, type, row) {
  if (type == "sort") {
    var sortedData = (row.directory) ? "d" : "f";
    sortedData += data;
    return sortedData;
  }
  else if (type == "display") {
    var linkClass = (row.directory) ? "text-warning" : "";
    var pathParam = (row.directory) ? "subPath" : "relativePath";
    var subPathParam = row.relativePath ? "?" + pathParam + "=" + escape(row.relativePath) : "";
    var linkUrl = (row.directory) ? "list" + subPathParam : "view" + subPathParam;
    return "<a class='" + linkClass + "' " + " href='" + linkUrl + "'>" + data + "</a>";
  }
  else {
    return data;
  }
}

function renderSize(data, type, row) {
  if (type == "display" || type == "filter" ) {
    return (row.directory) ? "-" : numeral(data).format("0.[0] b");
  }
  else {
    return (row.directory) ? -1 : data;
  }
}

function renderActions(data, type, row) {
  if (type == "display" && !row.directory) {
    return "<a class='' href='download?fileName=" + escape(row.relativePath) + "' title='Download'><span class='glyphicon glyphicon-download'></span></a>";
  }
  else {
    return "";
  }
}

function processAjaxResponse(response) {
  if (response.warnTitle) {
    $("#warnMessageText > strong").html(response.warnTitle);
    $("#warnMessageText > span").html(response.warnMessage);
    $("#warnMessage").removeClass("hide");
  }
  
  if (response.errorTitle) {
    $("#errorMessageText h4").html(response.errorTitle);
    $("#errorMessageText > div:last").html(response.errorMessage);
    $("#errorMessage").removeClass("hide");
    $("#resultsTableParent").hide();
  }
  
  return response.data || [];
}

function renderResultsTable() {
  var ajaxUrl = $("#ajaxUrl").val();
  var PAGE_HEAD_HEIGHT = 325;

   $("#resultsTable").DataTable({
     ajax: {
       url: ajaxUrl,
       dataSrc: processAjaxResponse
     },
     deferRender:    true,
     scrollCollapse: true,
     scroller:       true,
     orderCellsTop:  true,
     scrollY:        $(window).height() - PAGE_HEAD_HEIGHT,
     dom:            "<if>rt",
     language: {
       info: "Showing _TOTAL_ entries",
       infoEmpty: "Showing 0 entries",
       sEmptyTable: "No entries to show"
     },
     columns: [
       { data: "fileName", render: renderFile },
       { data: "fileSize", className: "dt-body-right", type: "num", render: renderSize },
       { data: "lastModified" },
       { data: "relativePath", orderable: false, searchable: false, render: renderActions }
     ]
 });


 $(window).resize(function() {
   $("div.dataTables_scrollBody").height( $(window).height() - PAGE_HEAD_HEIGHT );
 });

}

$(initLognavigatorScrollerTableView);
