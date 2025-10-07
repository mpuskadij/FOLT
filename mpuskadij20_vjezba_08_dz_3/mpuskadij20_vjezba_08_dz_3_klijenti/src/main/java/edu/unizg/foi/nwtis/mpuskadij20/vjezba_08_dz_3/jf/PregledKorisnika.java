package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za pregled korisnika
 */
@ViewScoped
@Named("pregledKorisnika")
public class PregledKorisnika implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sučelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	
	/**
	 * Lista korisnika
	 */
	private List<Korisnik> korisnici = new ArrayList<Korisnik>();
	
	/**
	 * Korisnsik za kojeg se dohvaćaju detalji
	 */
	private Korisnik odabraniKorisnik = null;
	
	/**
	 * Poruka greške
	 */
	private String poruka = "";
	
	/**
	 * Getter za {@link #odabraniKorisnik}
	 * @return
	 */
	public Korisnik getOdabraniKorisnik() {
		return this.odabraniKorisnik;
	}
	/**
	 * Resetira odabranog korisnika
	 */
	public void resetirajOdabranogKorisnika() {
		this.odabraniKorisnik = null;
	}
	
	/**
	 * Dohvaća detalje korisnika iz baze podataka
	 * @param k - novi odabrano korisnik
	 */
	public void odaberiKorisnika(Korisnik k) {
		try {
			var odgovor = this.servisPartner.getKorisnikID(k.korisnik());
			
			if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
				this.odabraniKorisnik = odgovor.readEntity(Korisnik.class);
			}
			else {
				this.setPoruka("Greška prilikom dohvaćanja korisnika!");
			}
		}
		catch (Exception e) {
			this.setPoruka("Greška prilikom dohvaćanja korisnika!");
		}
		
		
	
		
	}
	/**
	 * Getter za {@link #korisnici}
	 * @return
	 */
	public List<Korisnik> getKorisnici() {
		return korisnici;
	}
	
	/**
	 * Getter za {@link #poruka}
	 * @return poruka
	 */
	public String getPoruka() {
		return poruka;
	}
	
	/**
	 * Setter za {@link #poruka}
	 * @param poruka - poruka greške
	 */
	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}
	
	/**
	 * Dohvaća sve korisnike iz REST servisa
	 */
	@PostConstruct
    public void dohvatiKorisnike() {
		try {
			var odgovor = this.servisPartner.getKorisnici();
			if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
				korisnici = odgovor.readEntity(new GenericType<List<Korisnik>>() {
				});
				
				if (this.korisnici.isEmpty()) {
					this.setPoruka("Nema korisnika u bazi!");
				}
			} else {
				this.setPoruka("Greška prilikom dohvaćanja korisnika!");
				
			}
		} catch (WebApplicationException e) {
			this.setPoruka("Greška prilikom dohvaćanja korisnika!");
		}
		
		
		
		
    }


}
