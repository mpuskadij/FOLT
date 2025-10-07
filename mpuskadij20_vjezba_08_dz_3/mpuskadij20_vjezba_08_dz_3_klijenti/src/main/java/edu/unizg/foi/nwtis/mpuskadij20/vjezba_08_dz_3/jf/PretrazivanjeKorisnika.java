package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Zrno za pretraživanje korisnika
 */
@ViewScoped
@Named("pretrazivanjeKorisnika")
public class PretrazivanjeKorisnika implements Serializable {
	
	/**
	 * Facade za dohvat korisnika
	 */
	@Inject
	private KorisniciFacade korisniciFacade;

	private static final long serialVersionUID = 1L;
	
	/*
	 * Ime korisnika
	 */
	private String ime = "";
	
	/**
	 * Prezime korisnika
	 */
	private String prezime = "";
	
	/**
	 * Poruka greške
	 */
	private String poruka = "";
	
	/**
	 * Lista korisnika
	 */
	private List<Korisnik> korisnici = new ArrayList<>();
	
	/**
	 * Pronalazi korisnike prema prezimenu i imenu
	 */
	public void pretrazi() {
		var korisniciE = this.korisniciFacade.findAll(prezime, ime);
		
		this.korisnici = korisniciFacade.pretvori(korisniciE);
		
		if(this.korisnici.isEmpty()) {
			this.setPoruka("Nema korisnika pod time prezimenom i imenom!");
		}
		else {
			this.setPoruka("");
		}
		
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
	 * @param ime - novo ime
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
	 * Setter za {@link #prezime}
	 * @param prezime - novo prezime
	 */
	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}
	
	
	/**
	 * Getter za {@link #korisnici}
	 * @return lista korisnika
	 */
	public List<Korisnik> getKorisnici() {
		return korisnici;
	}
	
	/**
	 * Setter za {@link #korisnici}
	 * @param korisnici - nvi korisnici
	 */
	public void setKorisnici(List<Korisnik> korisnici) {
		this.korisnici = korisnici;
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
	 * @param poruka - nova poruka
	 */
	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}
	
	
	
	
	
	
	

}
