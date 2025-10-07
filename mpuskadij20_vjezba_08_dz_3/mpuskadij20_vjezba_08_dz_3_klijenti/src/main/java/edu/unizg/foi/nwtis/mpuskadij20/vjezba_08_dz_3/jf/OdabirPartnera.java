package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za odabir partnera
 */
@RequestScoped
@Named("odabirParnera")
public class OdabirPartnera implements Serializable {

  private static final long serialVersionUID = -524581462819739622L;

  /**
   * Zrno koje sadrži podatke korisnika / sesije
   */
  @Inject
  PrijavaKorisnika prijavaKorisnika;
  
  /**
   * Lista partnera iz baze
   */
  private List<Partner> partneri = new ArrayList<>();
  
  /**
   * Id odabranog partnera
   */
  private int partner;
  
  /**
   * Facade za rad s bazom
   */
  @Inject
  private PartneriFacade partneriFacade;
  
  /**
   * Sučelje za komunikaciju s REST servisom
   */
  @Inject
  @RestClient
  private ServisPartnerKlijent servisPartner;
  
  /**
   * Getter za {@link #partner}
   * @return id partnera
   */
  public int getPartner() {
    return partner;
  }
  /**
   * Setter za {@link #partner}
   * @param partner
   */
  public void setPartner(int partner) {
    this.partner = partner;
  }
  
  /**
   * Getter za {@link #partneri}
   * @return lista partnera
   */
  public List<Partner> getPartneri() {
    return partneri;
  }
  
  /**
   * Dohvaća sve partnere iz baze
   */
  @PostConstruct
  public void ucitajPartnere() {
    var partneriE = this.partneriFacade.findAll();
    this.partneri = this.partneriFacade.pretvori(partneriE);
  }
  /**
   * Ažurira odabranog partnera u sesiji korisnika
   * @return
   */
  public String odaberiPartnera() {
    if (this.partner > 0) {
      Optional<Partner> partnerO = this.partneri.stream()
          .filter((p) -> p.id() == this.partner).findFirst();
      if (partnerO.isPresent()) {
        this.prijavaKorisnika.setOdabraniPartner(partnerO.get());
        this.prijavaKorisnika.setPartnerOdabran(true);
        
        try {
        	var odgovor = this.servisPartner.getNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka());
        	if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
        		this.prijavaKorisnika.setAktivnaNarudzba(true);
        	}
        	else {
        		this.prijavaKorisnika.setAktivnaNarudzba(false);

        	}
        }catch (WebApplicationException e) {
    		this.prijavaKorisnika.setAktivnaNarudzba(false);
		}
        
        
      } else {
        this.prijavaKorisnika.setPartnerOdabran(false);
      }
    } else {
      this.prijavaKorisnika.setPartnerOdabran(false);
    }
    return "/index.html?faces-redirect=true";
  }

}
