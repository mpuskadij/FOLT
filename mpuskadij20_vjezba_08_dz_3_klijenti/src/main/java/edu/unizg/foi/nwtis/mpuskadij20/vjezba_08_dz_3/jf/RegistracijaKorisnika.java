package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za registraciju korisnika
 */
@ViewScoped
@Named("registracijaKorisnika")
public class RegistracijaKorisnika implements Serializable {
	
	/**
	 * Sučelje za REST servis partnera
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	
	/**
	 * Korisničko ime korisnika
	 */
	private String korisnik = "";
	
	/**
	 * Ime korisnika
	 */
	private String ime = "";
	
	/**
	 * Prezime korisnika
	 */
	private String prezime = "";
	
	
	/**
	 * Lozinka korisnika
	 */
	private String lozinka = "";
	
	/**
	 * Email korisnika
	 */
	private String email = "";

	private static final long serialVersionUID = 1L;
	
	/**
	 * Getter za {@link #korisnik}
	 * @return korisnik
	 */
	public String getKorisnik() {
		return korisnik;
	}
	
	/**
	 * Setter za {@link #korisnik}
	 * @param korisnik - kor ime
	 */
	public void setKorisnik(String korisnik) {
		this.korisnik = korisnik;
	}
	/**
	 * Getter za {@link #ime}
	 * @return ime
	 */
	public String getIme() {
		return ime;
	}
	/**
	 * Setter za {@link #ime}
	 * @param ime - ime
	 */
	public void setIme(String ime) {
		this.ime = ime;
	}
	
	/**
	 * Getter za {@link #prezime}
	 * @return prezime
	 */
	public String getPrezime() {
		return prezime;
	}
	
	/**
	 * Setter za {@link prezime}
	 * @param prezime
	 */
	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}
	
	/**
	 * Getter za {@link #lozinka}
	 * @return lozinka
	 */
	public String getLozinka() {
		return lozinka;
	}
	/**
	 * Setter za {@link #lozinka}
	 * @param lozinka
	 */
	public void setLozinka(String lozinka) {
		this.lozinka = lozinka;
	}
	/**
	 * Getter za email
	 * @return email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * Setter za email
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Registrira korisnika preko REST servisa
	 */
	public void registrirajSe() {
		var korisnikZaPoslati = new Korisnik(korisnik, lozinka, prezime, ime, email);
		try {
			var odgovor = this.servisPartner.postKorisnik(korisnikZaPoslati);
			if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
				 FacesContext.getCurrentInstance().addMessage(null,
					        new FacesMessage(FacesMessage.SEVERITY_INFO, "Registracija uspješna!", null));
				 
				 this.korisnik = "";
				 this.email = "";
				 this.lozinka = "";
				 this.prezime = "";
				 this.ime = "";

			}
		}catch(WebApplicationException greska) {
			 FacesContext.getCurrentInstance().addMessage(null,
				        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Greška prilikom registracije korisnika!", null));
		}

		
		
		
	}
	
	
	
	

}
