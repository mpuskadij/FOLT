package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.rest;


import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ServisTvrtkaKlijent;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketTvrtka;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST servis
 */
@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {
	
	/**
	 * Globalni podaci
	 */
	@Inject
	private GlobalniPodaci globalniPodaci;
	
	/**
	 * SUčelje za komunikaciju prema REST servisu iz dz 2
	 */
	@Inject
	@RestClient
	private ServisTvrtkaKlijent servisTvrtka;
	
	/**
	 * Trenutnis status tvrtke
	 */
	private String status = "";
	
	private void azurirajStatus( ) {
		try {
			var odgovor = servisTvrtka.headPosluzitelj();
			status = odgovor.getStatus() == Response.Status.OK.getStatusCode() ? "RADI" : "NE RADI";
		}catch (WebApplicationException e) {
			status = "NE RADI";
		}
		  
	}
	
	
	
	/**
	 * Šalje web socket poruku na /ws/tvrtka o novom statusu tvrtke
	 * @return HTTP odgovor
	 */
  @Path("kraj/info")
  @GET
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
          @APIResponse(responseCode = "204", description = "Pogrešna operacija") })
  @Counted(name = "brojZahtjeva_getPosluziteljKrajInfo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response getPosluziteljKrajInfo() {
	  azurirajStatus();
	  
      WebSocketTvrtka.send(status + ";" + globalniPodaci.getBrojObracuna() + ";");
      return Response.status(Response.Status.OK).build();
  }
  
  /**
   * Šalje web socket poruku oko novog broja obračuna na /ws/tvrtka
   * @return
   */
  @Path("obracun/ws")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Informacija o novom obračunu")
  @APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
          @APIResponse(responseCode = "204", description = "Pogrešna operacija") })
  @Counted(name = "brojZahtjeva_getObracunWs", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunWs", description = "Vrijeme trajanja metode")
  public Response getObracunWs() {
	globalniPodaci.povecajBrojObracuna();
	azurirajStatus();
    WebSocketTvrtka.send(status + ";" + globalniPodaci.getBrojObracuna() + ";");
    return Response.status(Response.Status.OK).build();

  }

}
