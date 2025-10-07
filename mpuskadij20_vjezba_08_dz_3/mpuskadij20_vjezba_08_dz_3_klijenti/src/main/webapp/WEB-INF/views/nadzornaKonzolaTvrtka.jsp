<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Nadzorna konzola tvrtka</title>
    </head>
    
            <style type="text/css">
.kontejner {

}

.inlinePrikaz {
display: inline;
}

.inlineBlockPrikaz {
display: inline-block;
}

        .kutijaStatusa {
            width: 100px;
            height: 100px;
        }
        
        .zelena {
                    color: #04af2f;
        
        }
        .crvena {
        background-color: #DC143C;
        }
        </style>
    <body>
        <h1>Nadzorna konzola tvrtka</h1>
        <ul>
                    <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a>
            </li>
        </ul>
        <ul class="kontejner">
            <li>
               <p>Status poslužitelja za registraciju: <%= (int) request.getAttribute("statusRegistracija") == 200 ? "U aktivnom radu!" : "Pauziran!" %> </p>
               <form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka" method="get">
  				 <input type="hidden" name="dio" value="registracija">
  				<button type="submit" name="akcija" value="pauza">Pauziraj</button>
				</form>

               	<form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka" method="get">
  				<input type="hidden" name="dio" value="registracija">
  				<button type="submit" name="akcija" value="start">Pokreni</button>
				</form>
               
              
            </li>
            <li>
               <p>Status poslužitelja za partnere: <%= (int) request.getAttribute("statusPartner") == 200 ? "U aktivnom radu!" : "Pauziran!" %> </p>
               
               	<form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka" method="get">
  				 <input type="hidden" name="dio" value="partner">
  				<button type="submit" name="akcija" value="pauza">Pauziraj</button>
				</form>
			
               
               	<form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka" method="get">
  				<input type="hidden" name="dio" value="partner">
  				<button type="submit" name="akcija" value="start">Pokreni</button>
				</form>
				

            </li>

         	<li>
            
            	<form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka" method="get">
  				<button type="submit" name="akcija" value="kraj">Završi poslužitelja tvrtku</button>
				</form>
			</li>

          <li>
          <span class="inlinePrikaz">Status poslužitelja tvrtka:</span>
                    <span class="<% if ((int) request.getAttribute("statusPosluzitelja") == 200) { %>zelena<% } else { %>crvena<% } %>" id="status">
                    <%= (int) request.getAttribute("statusPosluzitelja") == 200 ? "RADI" : "NE RADI" %>
                    </span>  
          
          </li>
          
          
          
          <li>
          <span>Broj primljenih obračuna:</span>
          <span id="brojObracuna"><%= request.getAttribute("brojObracuna") %></span>
                    </li>
          
     
        </ul> 
        
		      
                                                
                               
                              
              
                        
                        
                        <div>
                        <p>
                                Poruka:
                        </p>
                        <span id="poruka"></span>
                       
                </div>
                
                <div>
                	<label for="interna_poruka">Upišite Vašu internu poruku</label>

					<textarea required id="interna_poruka" name="interna_poruka" rows="3" cols="20">
					
				</textarea>
				
				<button type="button" id="gumbSlanjePoruke">Pošalji poruku</button>
                </div>
                <script type="text/javascript">
                        var wsocket;
                        var najNovijaPoruka = "";
                        function connect() {
                                var adresa = window.location.pathname;
                                var dijelovi = adresa.split("/");
                                adresa = "ws://" + window.location.hostname + ":"
                                                + window.location.port + "/" + dijelovi[1]
                                                + "/ws/tvrtka";
                                if ('WebSocket' in window) {
                                        wsocket = new WebSocket(adresa);
                                } else if ('MozWebSocket' in window) {
                                        wsocket = new MozWebSocket(adresa);
                                } else {
                                        alert('WebSocket nije podržan od web preglednika.');
                                        return;
                                }
                                wsocket.onmessage = onMessage;
                                
                                const gumbSlanjePoruke = document.getElementById("gumbSlanjePoruke");
                                gumbSlanjePoruke.addEventListener("click", function(event) {
                                	event.preventDefault();
                                	const porukaZaPoslati = document.getElementById("interna_poruka");
                                	
                                	if (porukaZaPoslati.value.trim().length != 0) {
                                		const najnovijiStatusPosluzitelja = najNovijaPoruka.split(";")[0].trim().length == 0 ? " <%= (int) request.getAttribute("statusPosluzitelja") == 200 ? "RADI" : "NE RADI"%>" : najNovijaPoruka.split(";")[0];
                                		const najnovijiBrojObracuna = najNovijaPoruka.split(";")[1] ?? <%= (int) request.getAttribute("brojObracuna") %>;
                                		
                                		 const cijelaPorukaZaWebSocket = najnovijiStatusPosluzitelja + ";" + najnovijiBrojObracuna + ";" + porukaZaPoslati.value;
                                		wsocket.send(cijelaPorukaZaWebSocket);
                                		porukaZaPoslati.value = "";
                                	}
                                });

                        }

                        function onMessage(evt) {
                       
                                var poruka = evt.data;
                                najNovijaPoruka = evt.data;
                                var porukaElem = document.getElementById("poruka");
                                var dijeloviPoruke = poruka.split(";");

                                if (dijeloviPoruke[2]) {
                                    porukaElem.innerHTML = dijeloviPoruke[2] + "<br>" + porukaElem.innerHTML;

                                }
                                
                                var kutijaStatusa = document.getElementById("status");
                                var statusIzPoruke = dijeloviPoruke[0];
                                if (statusIzPoruke == "RADI") {
                                	kutijaStatusa.classList.remove("crvena");

                                	kutijaStatusa.classList.add("zelena");
                                	
                                	kutijaStatusa.innerHTML = "Status tvrtke: RADI";
                                }
                                else if (statusIzPoruke == "NE RADI"){
                                	kutijaStatusa.classList.remove("zelena");

                                	kutijaStatusa.classList.add("crvena");
                                	
                                	kutijaStatusa.innerHTML = "Status tvrtke: NE RADI";

                                }
                                
                                var brojObracuna = dijeloviPoruke[1];
                                
                                var elementObracuna = document.getElementById("brojObracuna");
                                elementObracuna.innerHTML = brojObracuna;
                                
                        }
                        

                        window.addEventListener("load", connect, false);
                </script>
                 
    </body>
</html>



