package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za dohvat statusa
 */
@ViewScoped
@Named("statusPartnera")
public class StatusPartnera  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sučelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	
	/**
	 * Status partnera
	 */
	private String statusPosluziteljaPartner;
	
	/**
	 * Dohvaća status
	 */
	@PostConstruct
	    public void dohvatiStatusPartnera() {
		try {
	        statusPosluziteljaPartner = servisPartner.headPosluziteljPartner().getStatus() == Response.Status.OK.getStatusCode() ? "U aktivnom radu!" : "Ne radi!";

		} catch(WebApplicationException greska) {
			statusPosluziteljaPartner = "Ne radi!";
		}
	  }
	
	/**
	 * Getter za {@link #statusPosluziteljaPartner}
	 * @return status
	 */
	public String getStatusPosluziteljaPartner() {
		return statusPosluziteljaPartner;
	}
	
	
}
