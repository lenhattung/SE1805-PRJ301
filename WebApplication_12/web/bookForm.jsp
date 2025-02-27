<%-- 
    Document   : bookForm.jsp
    Created on : Feb 27, 2025, 1:33:52 PM
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
        <form action="MainController" action="post">
            <input type="hidden" name="action" value="add"/>
            Book ID <input type="text" name="txtBookID"/> <br/>
            Title <input type="text" name="txtTitle"/> <br/>
            Author <input type="text" name="txtAuthor"/> <br/>
            Publish Year <input type="number" name="txtPublishYear"/> <br/>
            Price <input type="number" name="txtPrice"/> <br/>
            Quantity <input type="number" name="txtQuantity"/> <br/>
            <input type="submit" value ="Save"/>
            <input type="reset" value="Reset"/>
        </form>
    </body>
</html>
