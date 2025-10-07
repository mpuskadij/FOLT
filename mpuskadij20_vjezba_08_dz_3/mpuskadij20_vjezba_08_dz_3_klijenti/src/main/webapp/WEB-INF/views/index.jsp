<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Početna stranice tvrtke</title>
    </head>
    <body>
        <h1>Početna stranica tvrtke</h1>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a>
            </li>
            
             <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/provjera">Provjera statusa poslužitelja Tvrtka</a>
            </li>
            
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled partnera </a>
            </li>
            
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">Pregled obračuna </a>
            </li>
            
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner">Pregled obračuna odabranog partnera </a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/noviPartner">Dodaj novog partnera</a>
            </li>
            
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">Uspavaj tvrtku</a>
            </li>
            
                                 <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna konzola Tvrtka </a>
            </li>
                 
        </ul>          
    </body>
</html>