package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;

/**
 * Zrno za spavanje partnera
 */
@ViewScoped
@Named("spavanjePartnera")
public class SpavanjePartnera implements Serializable {
	
	/**
	 * Sućelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;

	private static final long serialVersionUID = 1L;
	
	/**
	 * Vrijeme spavanje
	 */
	private Long vrijeme = 1000L;
	
	/**
	 * Poruka statusa
	 */
	private String poruka = "";
	/**
	 * Getter za {@link #vrijeme}
	 * @return vrijeme
	 */
	public Long getVrijeme() {
		return vrijeme;
	}
	/**
	 * Setter za {@link #vrijeme}
	 * @param vrijeme - novo vrijeme spavanja
	 */
	public void setVrijeme(Long vrijeme) {
		this.vrijeme = vrijeme;
	}
	/**
	 * Getter za {@link #poruka}
	 * @return porua
	 */
	public String getPoruka() {
		return poruka;
	}
	
	/**
	 * Setter za {@link #poruka}
	 * @param poruka - nova poruka
	 */
	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}
	/**
	 * Uspavljuje partnera na određeno vrijeme
	 */
	public void uspavaj() {
		try {
			this.setPoruka("Partner spava " + this.getVrijeme() / 1000.0 + " sekundi");
			servisPartner.getSpava(this.getVrijeme());
			
			
			
			this.setVrijeme(1L);
		}
		catch (WebApplicationException e) {
			this.setPoruka("Partner ne radi!");
		};
	}
}
