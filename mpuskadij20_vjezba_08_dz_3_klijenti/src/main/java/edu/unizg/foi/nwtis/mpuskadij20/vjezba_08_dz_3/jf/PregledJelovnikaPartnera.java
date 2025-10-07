package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Jelovnik;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za pregled jelovnika odabranog partnera
 */
@RequestScoped
@Named("pregledJelovnikaPartnera")
public class PregledJelovnikaPartnera implements Serializable {
	
	
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
	 * Lista stavki jelovnika
	 */
	private List<Jelovnik> jelovnici = new ArrayList<>();
	
	/**
	 * Getter za {@link #jelovnici}
	 * @return lsita jelovnika
	 */
	public List<Jelovnik> getJelovnici() {
		return jelovnici;
	}
	
	/**
	 * Dohvaća jelovnike od REST servisa
	 */
	@PostConstruct
	public void dohvatiJelovnikPartnera() {
		if (prijavaKorisnika.isPartnerOdabran()) {
			try {
				var odgovor = servisPartner.getJelovnik(this.prijavaKorisnika.getKorisnickoIme(),this.prijavaKorisnika.getLozinka());
				if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
					jelovnici = odgovor.readEntity(new GenericType<List<Jelovnik>>() {
					});
				}
			}
			catch(WebApplicationException greska) {
				jelovnici = List.of();
			}
		
			
		}
		
		
	}

}
