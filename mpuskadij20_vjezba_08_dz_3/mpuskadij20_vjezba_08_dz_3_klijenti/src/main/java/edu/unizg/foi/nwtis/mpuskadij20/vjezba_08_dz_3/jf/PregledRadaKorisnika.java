package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Zrno za pregled zapisa
 */
@ViewScoped
@Named("pregledRadaKorisnika")
public class PregledRadaKorisnika implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Facade za dohvat zapisa
	 */
	@Inject
	private ZapisiFacade zapisiFacade;
	
	/**
	 * Vrijeme od
	 */
	private Date vrijemeOd;
	
	/**
	 * Vrijeme do
	 */
	private Date vrijemeDo;
	
	/**
	 * Odabrani korisik na kojeg se zapisi odnose
	 */
	private String odabraniKorisnik = "";
	
	/**
	 * Poruka greške
	 */
	private String poruka = "";
	/**
	 * Lista zapisa iz baze podataka
	 */
	private List<Zapisi> zapisi = new ArrayList<Zapisi>();
	
	
	
	/**
	 * Getter za {@link #vrijemeOd}
	 * @return vrijeme od
	 */
	public Date getVrijemeOd() {
		return vrijemeOd;
	}



	/**
	 * Setter za {@link #vrijemeOd}
	 * @param vrijemeOd - novo vrijemeOd
	 */
	public void setVrijemeOd(Date vrijemeOd) {
		this.vrijemeOd = vrijemeOd;
	}



	/**
	 * Getter za {@link #vrijemeDo}
	 * @return vrijemeDo
	 */
	public Date getVrijemeDo() {
		return vrijemeDo;
	}



	/**
	 * Setter za {@link #vrijemeDo}
	 * @param vrijemeDo - vrijemeDo
	 */
	public void setVrijemeDo(Date vrijemeDo) {
		this.vrijemeDo = vrijemeDo;
	}



/**
 * Getter za {@link #odabraniKorisnik}
 * @return odabrani korisnik
 */
	public String getOdabraniKorisnik() {
		return odabraniKorisnik;
	}

	/**
	 * Setter za {@link #odabraniKorisnik}
	 * @param noviKorisnik - novi korisnik
	 */
	public void setOdabraniKorisnik(String noviKorisnik) {
		this.odabraniKorisnik = noviKorisnik;
	}



	/**
	 * Getter za{@link #poruka}
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
	 * Getter za {@link #zapisi}
	 * @return lista zapisa
	 */
	public List<Zapisi> getZapisi() {
		return zapisi;
	}



	/**
	 * Setter za {@link #zapisi}
	 * @param zapisi - nova lista zapisa
	 */
	public void setZapisi(List<Zapisi> zapisi) {
		this.zapisi = zapisi;
	}



	/**
	 * Dohvaća zapisa po odabranim kriterijima
	 */
	public void pretrazi() {
		this.zapisi = this.zapisiFacade.findAll(vrijemeOd, vrijemeDo, this.odabraniKorisnik.equals("Svi") ? null : this.odabraniKorisnik );
		
		
	}
	
	

}
