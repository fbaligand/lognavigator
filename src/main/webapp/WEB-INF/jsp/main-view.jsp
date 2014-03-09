<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">

<%-- HEADER PART --%>
<head>
	<meta charset="utf-8">
	<title>LogNavigator</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="Log Navigator">

	<%-- STYLES --%>
	<link href="<c:url value="/css/bootstrap/css/bootstrap-2.3.1.css"/>" rel="stylesheet">
	<style type="text/css">
 	body {
 		padding-top: 60px;
 		padding-bottom: 40px;
 	}
	</style>
	<link href="<c:url value="/css/bootstrap/css/bootstrap-responsive-2.3.1.css"/>" rel="stylesheet">
	<link href="<c:url value="/css/datatables/DT_bootstrap.css"/>" rel="stylesheet">
	<%-- /STYLES --%>

	<%-- HTML5 shiv, for IE6-8 support of HTML5 elements --%>
	<!--[if lt IE 9]>
      <script src="<c:url value="/js/html5shiv.js"/>"></script>
    <![endif]-->

</head>
<%-- /HEADER PART --%>

<body>

	<%-- NAVBAR --%>
	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
				</button>
				<a class="brand" href="<c:url value="/"/>">LogNavigator</a>
				
				<div class="nav-collapse collapse">
					<ul class="nav">
						<li class="<c:if test="${isRootListView}">active</c:if>"><a href="list">Logs List</a></li>
					</ul>
					
					<form class="navbar-form pull-right">
						<select name="logAccessConfigId" id="logAccessConfigId">
							<c:forEach var="logAccessConfig" items="${logAccessConfigList}" >
								<option <c:if test="${logAccessConfig.id.equals(logAccessConfigId)}">selected="selected"</c:if>>${logAccessConfig.id}</option>
							</c:forEach>
						</select>
					</form>
				</div>
			</div>
		</div>
	</div>
	<%-- /NAVBAR --%>

	<%-- MAIN CONTAINER --%>
	<div class="container-full">

		<%-- COMMAND FORM --%>
		<div class="row-fluid">
			<div class="offset2 span8">
				<form class="form-search text-center well" method="get" action="command">

					<%-- COMMAND --%>
					<div class="row-fluid">
						<div class="span12 input-append">
							<input type="text" name="cmd" value="<c:out value="${param.cmd}"/>" class="span8 search-query" placeholder="Type command..."/>
							<button type="submit" class="btn btn-primary"><i class="icon-play icon-white"></i></button>
						</div>
					</div>

					<%-- OPTIONS --%>
					<c:if test="${showOptions}">
						<div>&nbsp;</div>
						<div class="row-fluid">

							<%-- ENCODING OPTION --%>
							<div class="span3 offset3">
								<div class="btn-group" data-toggle="buttons-radio" id="encoding-btn-group">
									<button class="btn" data-value="UTF-8">UTF8</button>
									<button class="btn" data-value="ISO-8859-1">ISO</button>
								</div>
								<input type="hidden" id="encoding" name="encoding" value="${encoding}"/>
							</div>

							<%-- DISPLAY TYPE OPTION --%>
							<div class="span3">
								<div class="btn-group" data-toggle="buttons-radio" id="displaytype-btn-group">
									<button class="btn" data-value="TABLE">TABLE</button>
									<button class="btn" data-value="RAW">RAW</button>
								</div>
								<input type="hidden" id="displayType" name="displayType" value="${displayType}"/>
							</div>

							<div class="span3"></div>
						</div>
					</c:if>

				</form>
			</div>
		</div>
		<%-- /COMMAND FORM --%>

		<br/>&nbsp;

		<c:choose>

			<%-- ERROR MESSAGE --%>
			<c:when test="${errorMessage != null}">
				<div class="row-fluid">
					<div class="offset2 span8">
						<div class="alert alert-error alert-block">
							<h4>Error!</h4>
							${errorMessage}
						</div>
					</div>
				</div>
			</c:when>

			<%-- RESULTS RAW --%>
			<c:when test="${rawContent != null}">
				<div class="row-fluid">
					<div class="span12">
						<pre><c:out value="${rawContent}"/></pre>
					</div>
				</div>
			</c:when>

			<%-- RESULTS TABLE --%>
			<c:otherwise>
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
													<td align="center"><a class="text-center ${tableCell.cssClass}" href="${tableCell.link}" title="${tableCell.content}"><i class="${tableCell.linkIcon}"></i></a></td>
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
				<%-- /RESULTS TABLE --%>
			</c:otherwise>
		</c:choose>

	</div>
	<%-- /MAIN CONTAINER --%>

	<%-- SCRIPTS --%>
	<script src="<c:url value="/js/jquery-1.9.1.min.js"/>"></script>
	<script src="<c:url value="/js/bootstrap-2.3.1.min.js"/>"></script>
	<script src="<c:url value="/js/jquery.dataTables.min.js"/>"></script>
	<script src="<c:url value="/js/DT_bootstrap.js"/>"></script>
	<script src="<c:url value="/js/lognavigator.js"/>"></script>
	<%-- /SCRIPTS --%>
</body>
</html>
