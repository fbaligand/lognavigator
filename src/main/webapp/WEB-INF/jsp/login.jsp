<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container login">
	<div class="card card-container">
	
		<img id="profile-img" class="profile-img-card" src="<c:url value="/resources/${appVersion}/images/avatar.png"/>" />
		<p id="profile-name" class="profile-name-card"></p>

		<br clear="all"/>

		<form class="form-login" method="post" action="<c:url value="/authenticate"/>">
			<div class="row-fluid <c:if test="${!param.authenticationError}">hide</c:if>">
				<div class="alert alert-danger">
					<div class="row">
						<div class="col-md-11">
							<div>Incorrect username or password.</div>
						</div>
					</div>
				</div>
			</div>
	
			<input type="text" id="inputUsername" name="username" class="form-control" placeholder="Username" required autofocus> 
			<input type="password" id="inputPassword" name="password" class="form-control" placeholder="Password" required>
			
			<br clear="all"/>
			
			<button class="btn btn-lg btn-primary btn-block" type="submit">Log in</button>
		</form>
	</div>
</div>