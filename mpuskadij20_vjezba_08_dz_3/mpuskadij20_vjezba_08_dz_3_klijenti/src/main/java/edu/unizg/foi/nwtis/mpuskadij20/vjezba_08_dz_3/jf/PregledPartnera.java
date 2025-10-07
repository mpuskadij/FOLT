package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Zrno za pregled partnera iz baze
 */
@ViewScoped
@Named("pregledPartnera")
public class PregledPartnera implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Facade za rad s bazom partnera
	 */
	@Inject
	private PartneriFacade partneriFacade;
	
	/**
	 * Lista partnera
	 */
	private List<Partner> partneri = new ArrayList<Partner>();
	
	/**
	 * Trenutno odabrani partner
	 */
	private Partner odabraniPartner = null;
	
	/**
	 * Getter za {@link #odabraniPartner}
	 * @return
	 */
	public Partner getOdabraniPartner() {
		return this.odabraniPartner;
	}
	
	/**
	 * Resetira partnera
	 */
	public void resetirajOdabranogPartnera() {
		this.odabraniPartner = null;
	}
	/**
	 * Postavlja {@link #odabraniPartner}
	 * @param p
	 */
	public void odaberiPartnera(Partner p) {
		this.odabraniPartner = p;
	}
	
	/**
	 * Getter za {@link #partneri}
	 * @return
	 */
	public List<Partner> getPartneri() {
		return partneri;
	}
	
	/**
	 * Dohvaća partnere iz baze
	 */
	@PostConstruct
    public void dohvatiPartnere() {
		var partneriEntiteti = partneriFacade.findAll();
		this.partneri = partneriFacade.pretvori(partneriEntiteti);
    }


}
