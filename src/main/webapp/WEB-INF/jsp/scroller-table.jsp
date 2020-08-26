<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="error.jsp">  
	<jsp:param name="hideErrorMessage" value="true" />  
</jsp:include>

<div class="row-fluid">
	<div class="${tableLayoutClass}" id="resultsTableParent">
		<table class="table table-hover table-condensed" id="resultsTable">
			<thead>
				<tr>
					<c:forEach var="tableHeader" items="${tableHeaders}">
						<th>${tableHeader}</th>
					</c:forEach>
				</tr>
				<c:forEach var="tableLine" items="${tableLines}">
					<tr>
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
			</thead>
		</table>
	</div>
</div>

<input type="hidden" id="ajaxUrl" value="<c:out value="${ajaxUrl}"/>"/>