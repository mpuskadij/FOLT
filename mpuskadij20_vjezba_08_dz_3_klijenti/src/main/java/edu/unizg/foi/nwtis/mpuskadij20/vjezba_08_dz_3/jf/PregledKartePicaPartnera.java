package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.KartaPica;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za pregled karte pića partnera
 */
@ViewScoped
@Named("pregledKartePicaPartnera")
public class PregledKartePicaPartnera implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sučelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	
	/**
	 * Zrno koje sadrži podatke sesije
	 */
	@Inject
	private PrijavaKorisnika prijavaKorisnika;
	
	/**
	 * Popis stavki karte pića
	 */
	private List<KartaPica> kartePica = new ArrayList<>();
	
	/**
	 * Getter za {@link #kartePica}
	 * @return listu akrte pića
	 */
	public List<KartaPica> getKartePica() {
		return kartePica;
	}
	
	/**
	 * Dohvaća kartu pića od REST servisa
	 */
	@PostConstruct
	public void dohvatiKartuPicaPartneraPartnera() {
		if (prijavaKorisnika.isPartnerOdabran()) {
			try {
				var odgovor = servisPartner.getKartaPica(this.prijavaKorisnika.getKorisnickoIme(),this.prijavaKorisnika.getLozinka());
				if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
					kartePica = odgovor.readEntity(new GenericType<List<KartaPica>>() {
					});
				}
			} catch (WebApplicationException e) {
				kartePica = List.of();
			}
		
			
		}
		
		
	}

}
