<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

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
    <link rel="stylesheet" href="<c:url value="/resources/webjars/bootstrap/3.3.1/css/bootstrap.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/resources/webjars/font-awesome/4.2.0/css/font-awesome.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/resources/webjars/select2/3.5.2/select2.css"/>">
    <link rel="stylesheet" href="<c:url value="/resources/webjars/select2-bootstrap-css/1.4.4/select2-bootstrap.css"/>">
	<link rel="stylesheet" href="<c:url value="/resources/webjars/datatables-plugins/9dcbecd42ad/integration/bootstrap/3/dataTables.bootstrap.css"/>">
	<link rel="stylesheet" href="<c:url value="/resources/webjars/datatables-fixedheader/2.1.2/css/dataTables.fixedHeader.css"/>">
	<link rel="stylesheet" href="<c:url value="/resources/${appVersion}/css/lognavigator.css"/>">
	<%-- /STYLES --%>

    <!-- HTML5 Shiv and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="<c:url value="/resources/webjars/html5shiv/3.7.2/html5shiv.min.js"/>"></script>
      <script src="<c:url value="/resources/webjars/respond/1.4.2/dest/respond.min.js"/>"></script>
    <![endif]-->

</head>
<%-- /HEADER PART --%>

<body>

	<%-- NAVBAR --%>
	<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
		<div class="container-fluid">

			<div class="navbar-header">
				<a href="<c:url value="/"/>" class="navbar-brand" title="v${appVersion}">LogNavigator</a>
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
					<select name="logAccessConfigId" id="logAccessConfigId" class="form-control select2">
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
	<section class="command-fixed-top" role="command-form">
		<div class="container-fluid">
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
		</div>
	</section>
	<%-- /COMMAND FORM --%>


	<%-- RESULTS --%>
	<section class="container-fluid <c:if test="${!showOptions}">no-options</c:if>" role="results">

		<%-- WARN MESSAGE --%>
		<c:if test="${warnMessage != null}">
			<div class="row-fluid">
				<div class="col-md-offset-2 col-md-8">
					<div class="alert alert-warning alert-dismissible" role="alert">
						<button type="button" class="close" data-dismiss="alert">
							<span aria-hidden="true">&times;</span><span class="sr-only">Close</span>
						</button>
						<strong>${warnTitle}:</strong> ${warnMessage}
					</div>
				</div>
			</div>
		</c:if>
	
		<%-- BODY CONTENT --%>
		<tiles:insertAttribute name="body" />

	</section>
	<%-- /RESULTS --%>
		
        
	<%-- SCRIPTS --%>
    <script src="<c:url value="/resources/webjars/jquery/1.11.1/jquery.min.js"/>"></script>
    <script src="<c:url value="/resources/webjars/bootstrap/3.3.1/js/bootstrap.min.js"/>"></script>
    <script src="<c:url value="/resources/webjars/datatables/1.10.4/js/jquery.dataTables.min.js"/>"></script>
    <script src="<c:url value="/resources/webjars/datatables-plugins/9dcbecd42ad/integration/bootstrap/3/dataTables.bootstrap.min.js"/>"></script>
    <script src="<c:url value="/resources/webjars/datatables-fixedheader/2.1.2/js/dataTables.fixedHeader.js"/>"></script>
    <script src="<c:url value="/resources/webjars/numeral-js/1.5.3-1/min/numeral.min.js"/>"></script>
    <script src="<c:url value="/resources/webjars/jquery-placeholder/2.0.7/jquery.placeholder.min.js"/>"></script>
   	<script src="<c:url value="/resources/webjars/select2/3.5.2/select2.min.js"/>"></script>
   	<script src="<c:url value="/resources/${appVersion}/js/lognavigator-common.js"/>"></script>
   	<tiles:importAttribute name="viewName"/>
   	<script src="<c:url value="/resources/${appVersion}/js/lognavigator-${viewName}.js"/>"></script>
	<%-- /SCRIPTS --%>

</body>

</html>
