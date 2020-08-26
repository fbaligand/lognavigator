<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row">&nbsp;</div>
<div id="errorMessage" class="row-fluid <c:if test="${hideMessages}">hide</c:if>">
	<div class="col-md-offset-2 col-md-8">
		<div class="alert alert-danger">
			<div class="row">
				<div class="col-md-1">
					<i class="fa fa-exclamation-triangle fa-4"></i>
				</div>
				<div id="errorMessageText" class="col-md-11">
					<div><h4>${errorTitle}</h4></div>
					<div>${errorMessage}</div>
				</div>
			</div>
		</div>
	</div>
</div>
