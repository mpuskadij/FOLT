<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Status tvrtke</title>
    </head>
    <body>
        <h1>Status tvrtke</h1>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
<%
if(request.getAttribute("status") != null) {
%>
            <li>
               <p>Status tvrtke: <%= request.getAttribute("status") %> </p>
            </li>  
            <%} %>         
        </ul>          
    </body>
</html>
