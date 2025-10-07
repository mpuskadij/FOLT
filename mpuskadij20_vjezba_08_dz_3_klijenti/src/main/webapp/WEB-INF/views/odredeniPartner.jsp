<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Partner" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pregled određenog partnera</title>
        <style type="text/css">
table, th, td {
  border: 1px solid;
}       
th {
	text-align: center;
	font-weight: bold;
} 
.desno {
	text-align: right;
}
        </style>
    </head>
    <% Partner partner = (Partner) request.getAttribute("partner"); %>
    <body>
        <h1> Partner <%= partner.id() %> - <%= partner.naziv() %></h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled partnera</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>ID</th>
        <th>Naziv</th>
        <th>Adresa</th>
        <th>Mrežna vrata</th>
        <th>Mrežna vrata za kraj</th>
     
        <th>GPS širina</th>
        <th>GPS dužina</th>
        <th>Vrsta kuhinje</th>
        <th>Sigurnosni kod</th>
        <th>Admin kod</th>
        </tr>

       <tr>
       <td><%= partner.id() %></td>
       <td><%= partner.naziv() %></td>
       <td><%= partner.adresa() %></td>
       <td><%= partner.mreznaVrata() %></td>
       <td><%= partner.mreznaVrataKraj() %>
       <td><%= partner.gpsSirina() %></td>
       <td><%= partner.gpsDuzina() %></td>
       <td><%= partner.vrstaKuhinje() %></td>
        <td><%= partner.sigurnosniKod() %></td>
        </td><td><%= partner.adminKod() %></td>
       
       
       
       </tr>	  

        </table>	        
    </body>
</html>
