<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page
	import="java.util.List, java.text.SimpleDateFormat, java.util.Date, edu.unizg.foi.nwtis.podaci.Obracun, edu.unizg.foi.nwtis.podaci.Partner"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Pregled obračuna određenog partnera</title>
<style type="text/css">
.poruka {
	color: red;
}
</style>


</head>

<body>
	<h1>Pregled obračuna određenog partnera</h1>

	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>
	<form method="get"
		action="${pageContext.request.contextPath}/mvc/tvrtka/privatno/obracuni/partner">
		<label for="od">Vrijeme od</label>
    <br>
    <input type="datetime-local" id="od" name="od" />
    <br>
    
    <label for="do">Vrijeme do:</label>
    <br>
    <input type="datetime-local" id="do" name="do" />
    <br>
    <label for="id">Partner:</label>
			<select name="id"
			id="id" required>
			
			
			<%
		List<Partner> partneri = (List<Partner>) request.getAttribute("partneri");
		if (partneri != null && !partneri.isEmpty()) {
			for (Partner partner : partneri) {
		%>
		
			<option value=<%= partner.id() %>><%= partner.naziv() %></option>
			
		
		
		<%} } else { %>
			<option value="Nema dostupnih partnera"></option>
			
		<% } %>
		</select>
	

		<button type="submit" <%if (partneri.isEmpty() || partneri == null) { %>disabled <%} %>>Dohvati obračune</button>
	</form>


	<%
	List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
	if (obracuni != null && !obracuni.isEmpty()) {
	%>
	<table border="1">
		<thead>
			<tr>
				<th>Partner</th>
				<th>ID</th>
				<th>Jelo</th>
				<th>Količina</th>
				<th>Cijena</th>
				<th>Vrijeme</th>
			</tr>
		</thead>
		<tbody>
			<%
			for (Obracun o : obracuni) {
			%>
			<tr>
				<td><%=o.partner()%></td>
				<td><%=o.id()%></td>
				<td><%=o.jelo() ? "Jelo" : "Piće"%></td>
				<td><%=o.kolicina()%></td>
				<td><%=o.cijena()%></td>
				<td><%= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(o.vrijeme())) %></td>
			</tr>
			<%
			}
			%>
		</tbody>
	</table>
	<%
	} else if (obracuni != null) {
	%>
	<p class="poruka">Nema rezultata.</p>
	<%
	}
	%>

</body>