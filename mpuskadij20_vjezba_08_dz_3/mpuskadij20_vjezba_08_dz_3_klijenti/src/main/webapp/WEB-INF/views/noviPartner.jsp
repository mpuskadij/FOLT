<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Dodavanje novog partnera</title>
        <style type="text/css">
.poruka {
	color: red;
}
        </style>
    </head>
    <body>
        <h1>Dodavanje partnera</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            <%
            if(request.getAttribute("poruka") != null) {
              String poruka = (String) request.getAttribute("poruka");
              Object oPogreska = request.getAttribute("pogreska");
              boolean pogreska = false;
              System.out.println(oPogreska);
              if(oPogreska != null) {
                pogreska = (Boolean) oPogreska;
              }
              if(poruka.length() > 0) {
                String klasa = "";
                if(pogreska) {
                  klasa = "poruka";
                }
                %>
                <li>
                <p class="<%= klasa%>">${poruka}</p>
                </li>
                <%
              }
            }
            %>  
            <li><p>Podaci partnera:</p>          
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/noviPartner">
                    <table>
                        <tr>
                            <td>ID partnera: </td>
                            <td><input name="idPartnera" size="20" type="number" value="${id}" required/>
                            </td>
                        </tr>
                        <tr>
                            <td>Naziv: </td>
                            <td><input type="text" maxlength="255" name="naziv" size="20" value="${naziv}" required/>
                            </td>
                        </tr>
                        <tr>
                            <td>Vrsta kuhinje: </td>
                            <td><input name="vrstaKuhinje" maxlength="2" size="20" value="${vrstaKuhinje}" required/></td>
                        </tr>
                        <tr>
                            <td>Adresa: </td>
                            <td><input name="adresa" maxlength="255" size="20" value="${adresa}" required/>
                            </td>
                        </tr>
                        <tr>
                            <td>Mrezna vrata: </td>
                            <td><input type="number" name="mreznaVrata" size="30" value="${mreznaVrata}" required/></td>
                        </tr>
                        
                        <tr>
                            <td>Mrezna vrata kraj: </td>
                            <td><input type="number" name="mreznaVrataKraj" min="1" size="30" value="${mreznaVrataKraj}" required/></td>
                        </tr>
                        
                        <tr>
                            <td>GPS širina: </td>
                            <td><input type="number" name="gpsSirina" step="0.01" min = "0" size="30" value="${gpsSirina}" required/></td>
                        </tr>
                        <tr>
                            <td>GPS dužina: </td>
                            <td><input type="number" name="gpsDuzina" step="0.01" min = "0" size="30" value="${gpsDuzina}" required/></td>
                        </tr>
                        
                        <tr>
                            <td>Sigurnosni kod: </td>
                            <td><input type="text" name="sigurnosniKod" maxlength="20" size="30" value="${sigurnosniKod}" required/></td>
                        </tr>
                        
                         <tr>
                            <td>Admin kod: </td>
                            <td><input type="text" name="adminKod" maxlength="20" size="30" value="${adminKod}" required/></td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td><input type="submit" value=" Dodaj partnera "></td>
                        </tr>                        
                    </table>
                </form>
            </li>                     
        </ul>   
    </body>
</html>
