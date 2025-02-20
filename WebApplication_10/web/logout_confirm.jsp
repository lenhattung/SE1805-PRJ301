<%-- 
    Document   : logout_confirm.jsp
    Created on : Feb 17, 2025, 1:44:48 PM
    Author     : tungi
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%@include file="header.jsp" %>
        <div style="min-height: 500px; padding: 20px">
            <h4>You are logged out!</h4>
            <a href="MainController">Back to login</a>
        </div>
        <%@include file="footer.jsp" %>

    </body>
</html>
