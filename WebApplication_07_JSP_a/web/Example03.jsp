<%-- 
    Document   : Example03
    Created on : Feb 10, 2025, 1:25:55 PM
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
       <%! int a = 100; %>
       <%
           if(a%2==0){
               %> 
                    <%=a%> là số chẵn!
                <%
           }else{
               %>
                    <%=a%> là số lẻ!
                <%
           }
       %>
    </body>
</html>
