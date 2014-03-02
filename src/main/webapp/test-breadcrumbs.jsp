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

	<%-- OFFICIAL NAVBAR --%>
<!-- 	<div class="navbar navbar-inverse navbar-fixed-top"> -->
<!-- 		<div class="navbar-inner"> -->
<!-- 			<div class="container"> -->
<!-- 				<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse"> -->
<!-- 					<span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span> -->
<!-- 				</button> -->
<!-- 				<a class="brand" href="../..">LogNavigator</a> -->
				
<!-- 				<div class="nav-collapse collapse"> -->
<!-- 					<ul class="nav"> -->
<%-- 						<li class="<c:if test="${isRootListView}">active</c:if>"><a href="list">Logs List</a></li> --%>
<!-- 					</ul> -->
					
<!-- 					<form class="navbar-form pull-right"> -->
<!-- 						<select name="logAccessConfigId" id="logAccessConfigId"> -->
<%-- 							<c:forEach var="logAccessConfig" items="${logAccessConfigList}" > --%>
<%-- 								<option <c:if test="${logAccessConfig.id.equals(logAccessConfigId)}">selected="selected"</c:if>>${logAccessConfig.id}</option> --%>
<%-- 							</c:forEach> --%>
<!-- 						</select> -->
<!-- 					</form> -->
<!-- 				</div> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 	</div> -->
	<%-- /OFFICIAL NAVBAR --%>

	<%-- TEST NAVBAR --%>
	<div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">

                <div class="container">
                    <div class="row">
						<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
							<span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
						</button>
						<a class="brand" href="../..">LogNavigator</a>
						
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

                    <div class="row">
						<div class="nav-collapse collapse">
							<ul class="nav">
								<li><a href="#">Home</a> <span class="divider">/</span></li>
								<li><a href="#">Library</a> <span class="divider">/</span></li>
								<li class="active">Data</li>
							</ul>
						</div>	
                    </div>
                    
                </div>

            </div>
	</div>
	
	<!--                             <ul class="nav"> -->
<!--                                 <li><a href="#">Page 1</a></li> -->
<!--                                 <li class="divider-vertical"></li> -->
<!--                                 <li><a href="#">Page 2</a></li> -->
<!--                                 <li class="divider-vertical"></li> -->
<!--                                 <li><a href="#">Page 3</a></li> -->
<!--                                 <li class="divider-vertical"></li> -->
<!--                                 <li><a href="#">Page 4</a><li> -->
<!--                             </ul>                 -->
	
	<%-- /TEST NAVBAR --%>

	<%-- MAIN CONTAINER --%>
	<div class="container-full">
	coucou

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
