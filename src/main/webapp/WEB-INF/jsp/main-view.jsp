<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>

<%-- HEADER PART --%>
<head>
    <meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Log Navigator">
	<title>LogNavigator</title>

	<%-- STYLES --%>
    <link rel="stylesheet" href="<c:url value="/css/bootstrap/css/bootstrap.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/css/font-awesome/css/font-awesome.css"/>">
	<link rel="stylesheet" href="<c:url value="/css/datatables/css/dataTables.bootstrap.css"/>">
	<link rel="stylesheet" href="<c:url value="/css/lognavigator.css"/>">
	<%-- /STYLES --%>

    <!-- HTML5 Shiv and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="<c:url value="/js/html5shiv.js"/>"></script>
      <script src="<c:url value="/js/respond.min.js"/>"></script>
    <![endif]-->

</head>
<%-- /HEADER PART --%>

<body>

	<%-- NAVBAR --%>
	<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
		<div class="container-fluid">

			<div class="navbar-header">
				<a href="<c:url value="/"/>" class="navbar-brand">LogNavigator</a>
			</div>

			<c:if test="${breadcrumbs != null}">
				<div class="navbar-header">
					<ul class="breadcrumb list-inline">
						<c:forEach var="breadcrumb" items="${breadcrumbs}">
							<c:if test="${breadcrumb.link != null}">
								<li><a href="${breadcrumb.link}">${breadcrumb.label}</a></li>
							</c:if>
							<c:if test="${breadcrumb.link == null}">
								<li class="active">${breadcrumb.label}</li>
							</c:if>
						</c:forEach>
					</ul>
				</div>
			</c:if>

			<div class="navbar-collapse collapse">
				<form class="navbar-form navbar-right">
					<select name="logAccessConfigId" id="logAccessConfigId" class="form-control">
						<c:forEach var="logAccessConfigIdsByDisplayGroupEntry" items="${logAccessConfigIdsByDisplayGroup}">
							<optgroup label="${logAccessConfigIdsByDisplayGroupEntry.key}">
								<c:forEach var="logAccessConfig" items="${logAccessConfigIdsByDisplayGroupEntry.value}">
									<option <c:if test="${logAccessConfig.id == logAccessConfigId}">selected="selected"</c:if> >${logAccessConfig.id}</option>
								</c:forEach>
							</optgroup>
						</c:forEach>
					</select>
				</form>
			</div>
			
		</div>
	</nav>
   	<%-- /NAVBAR --%>


	<%-- COMMAND FORM --%>
	<section class="container-fluid" role="command-form">
		<div class="row">
			<div class="col-md-offset-2 col-md-8">
				<form class="text-center well" method="get" action="command" id="commandForm">

					<%-- COMMAND --%>
					<div class="row-fluid">
						<div class="col-md-12">
							 <div class="input-group">
								<input type="text" id="cmd" name="cmd" value="<c:out value="${param.cmd}"/>" class="form-control rounded-left" placeholder="Type command..."/>
								<span class="input-group-btn">
									<button id="executeButton" class="btn btn-primary" type="button" title="Execute"><span class="glyphicon glyphicon-play"></span></button>
									<button id="downloadButton" class="btn btn-primary rounded-right" type="button" title="Download"><span class="glyphicon glyphicon-download"></span></button>
								</span>
							</div>
						</div>
					</div>

					<%-- OPTIONS --%>
					<c:if test="${showOptions}">
						<div>&nbsp;</div>
						<div class="row-fluid">

							<%-- ENCODING OPTION --%>
							<div class="col-md-3 col-md-offset-3">
								<div class="btn-group" data-toggle="buttons">
									<label class="btn btn-default" for="encodingUTF8">
										<input type="radio" name="encoding" id="encodingUTF8" value="UTF-8" <c:if test="${encoding == 'UTF-8'}">checked="checked"</c:if> > UTF8
									</label>
									<label class="btn btn-default" for="encodingISO">
										<input type="radio" name="encoding" id="encodingISO" value="ISO-8859-1" <c:if test="${encoding == 'ISO-8859-1'}">checked="checked"</c:if> > ISO
									</label>
								</div>
							</div>

							<%-- DISPLAY TYPE OPTION --%>
							<div class="col-md-3">
								<div class="btn-group" data-toggle="buttons">
									<label class="btn btn-default" for="displayTypeTABLE">
										<input type="radio" name="displayType" id="displayTypeTABLE" value="TABLE" <c:if test="${displayType == 'TABLE'}">checked="checked"</c:if> > TABLE
									</label>
									<label class="btn btn-default" for="displayTypeRAW">
										<input type="radio" name="displayType" id="displayTypeRAW" value="RAW" <c:if test="${displayType == 'RAW'}">checked="checked"</c:if> > RAW
									</label>
								</div>
							</div>

						</div>
					</c:if>

					<br clear="all"/>

				</form>
			</div>
		</div>
	</section>
	<%-- /COMMAND FORM --%>


	<%-- RESULTS --%>
	<section class="container-fluid" role="results">
	
		<c:choose>

			<%-- ERROR MESSAGE --%>
			<c:when test="${errorMessage != null}">
				<div class="row">&nbsp;</div>
				<div class="row-fluid">
					<div class="col-md-offset-2 col-md-8">
						<div class="alert alert-danger">
							<div class="row">
								<div class="col-md-1">
									<i class="fa fa-exclamation-triangle fa-4"></i>
								</div>
								<div class="col-md-11">
									<div><h4>${errorTitle}</h4></div>
									<div>${errorMessage}</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:when>

			<%-- RESULTS RAW --%>
			<c:when test="${rawContent != null}">
				<div class="row">
					<div class="col-md-13">
						<pre><c:out value="${rawContent}"/></pre>
					</div>
				</div>
			</c:when>

			<%-- RESULTS TABLE --%>
			<c:otherwise>
				<div class="row">&nbsp;</div>
				<div class="row-fluid">
					<div class="${tableLayoutClass}">
						<table class="table table-hover table-condensed" id="resultsTable">
							<thead>
								<tr>
									<th>#</th>
									<c:forEach var="tableHeader" items="${tableHeaders}">
										<th>${tableHeader}</th>
									</c:forEach>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="tableLine" items="${tableLines}" varStatus="status">
									<tr>
										<td>${status.count}</td>
										<c:forEach var="tableCell" items="${tableLine}">
											<c:choose>
												<c:when test="${tableCell.link != null and tableCell.linkIcon != null}">
													<td><a class="${tableCell.cssClass}" href="${tableCell.link}" title="${tableCell.content}"><span class="${tableCell.linkIcon}"></span></a></td>
												</c:when>
												<c:when test="${tableCell.link != null}">
													<td><a class="${tableCell.cssClass}" href="${tableCell.link}">${tableCell.content}</a></td>
												</c:when>
												<c:otherwise>
													<td class="${tableCell.cssClass}"><c:out value="${tableCell.content}"/></td>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
				<input type="hidden" id="resultsSize" value="${fn:length(tableLines)}"/>
				<%-- /RESULTS TABLE --%>
			</c:otherwise>
		</c:choose>

	</section>
	<%-- /RESULTS --%>
		
        
	<%-- SCRIPTS --%>
    <script src="<c:url value="/js/jquery-1.10.2.js"/>"></script>
    <script src="<c:url value="/js/bootstrap.min.js"/>"></script>
    <script src="<c:url value="/js/jquery.dataTables.js"/>"></script>
    <script src="<c:url value="/js/dataTables.bootstrap.js"/>"></script>
    <script src="<c:url value="/js/jquery.placeholder.js"/>"></script>
   	<script src="<c:url value="/js/lognavigator.js"/>"></script>
	<%-- /SCRIPTS --%>

</body>

</html>