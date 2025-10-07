package edu.unizg.foi.nwtis.mpuskadij20.rest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * REST klijent za komunikaciju prema klijentima
 * @author Marin Puškadija
 */
@RegisterRestClient(configKey = "klijentTvrtkaInfo")
@Path("api/tvrtka")
public interface RestKlijent {
	
	/**
	 * Obaveštavanje da su tvrtka i partner završili
	 * @return odgovor od REST servisa
	 */
	@GET
    @Path("kraj/info")
    Response getKrajInfo();
	
	/**
	 * Aktivira se kad je dodan 1 novi obračun u bazu
	 * @return odgovor od REST servisa
	 */
	@GET
	@Path("obracun/ws")
	Response getObracunWS();

}
