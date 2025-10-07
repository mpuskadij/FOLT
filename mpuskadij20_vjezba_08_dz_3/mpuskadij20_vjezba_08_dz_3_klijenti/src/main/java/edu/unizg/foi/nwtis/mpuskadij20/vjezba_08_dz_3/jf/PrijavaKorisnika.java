package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.sql.Timestamp;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Podaci sesije korisnika
 */
@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {
  private static final long serialVersionUID = -1826447622277477398L;
  /**
   * Korisnik koji je u sesiji
   */
  private Korisnik korisnik = null;
  
  /**
   * Korisničko ime korisnika
   */
  private String korisnickoIme;
  
  /**
   * Lozinka korisnika
   */
  private String lozinka;
  
  /**
   * Ime korisnika
   */
  private String ime;
  
  /**
   * Prezime korisnika
   */
  private String prezime;
  
  /**
   * Email korisnika
   */
  private String email;
  
  /**
   * Status prijave
   */
  private boolean prijavljen = false;
  
  /**
   * Poruka greške
   */
  private String poruka = "";
  
  /**
   * Partner kojega ima odabrano trenutno
   */
  private Partner odabraniPartner;
  
  /**
   * Je li partner uspješno odabran
   */
  private boolean partnerOdabran = false;
  
  /**
   * Ima li trenutno aktivnu narudžbu korisnik
   */
  private boolean aktivnaNarudzba;
  
  /**
   * Getter za {@link #aktivnaNarudzba}
   * @return aktivna narudžba
   */
  public boolean isAktivnaNarudzba() {
	  return aktivnaNarudzba;
  }
  /**
   * Setter za {@link #aktivnaNarudzba}
   * @param vrijednost - jel otvorena
   */
  public void setAktivnaNarudzba(boolean vrijednost) {
	  aktivnaNarudzba = vrijednost;
  }

  /**
   * Facade za kreiranje zapisa
   */
  @Inject
  private ZapisiFacade zapisiFacade;
  
  /**
   * Facade za dohvat korisnika;
   */
  @Inject
  KorisniciFacade korisniciFacade;
  
  /**
   * Jakarta security
   */
  @Inject
  private SecurityContext securityContext;
  /**
   * Getter za {@link #korisnickoIme}
   * @return korisničko ime
   */
  public String getKorisnickoIme() {
    return korisnickoIme;
  }
  
  /**
   * Setter za {@link #korisnickoIme}
   * @param korisnickoIme
   */
  public void setKorisnickoIme(String korisnickoIme) {
    this.korisnickoIme = korisnickoIme;
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
   * @param lozinka - nova lozinka
   */
  public void setLozinka(String lozinka) {
    this.lozinka = lozinka;
  }
  
  /**
   * Getter za {@link #ime}
   * @return ime
   */
  public String getIme() {
    return ime;
  }
  
  /**
   * Setter za {@link ime}
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
   * Getter za {@link email}
   * @return email
   */
  public String getEmail() {
    return email;
  }
  
  /**
   * Setter za {@link email}
   * @param email - novi email
   */
  public void setEmail(String email) {
    this.email = email;
  }
  
  /**
   * Setter za {@link #prijavljen}
   * @return jel prijavljen
   */
  public boolean isPrijavljen() {
    if (!this.prijavljen) {
      provjeriPrijavuKorisnika();
    }
    return this.prijavljen;
  }
  
  /**
   * Odjavljuje korisnika, kreira zapis
   * @return index.xhtml
   */
    public String odjavaKorisnika() {
    if (this.prijavljen) {
      this.prijavljen = false;
      this.zapisiRadnju("Odjava korisnika");
      FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

      return "/index.xhtml?faces-redirect=true";
    }
    return "";
  }
    /**
     * Provjerava je li korisnika prijavljen
     */
    @PostConstruct
    private void provjeriPrijavuKorisnika() {
      if (this.securityContext.getCallerPrincipal() != null) {
        var korIme = this.securityContext.getCallerPrincipal().getName();
        this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(korIme));
        if (this.korisnik != null) {
          this.prijavljen = true;
          this.korisnickoIme = korIme;
          this.ime = this.korisnik.ime();
          this.prezime = this.korisnik.prezime();
          this.lozinka = this.korisnik.lozinka();
          
        }
      }
    }
    
    /**
     * Getter za {@link #poruka}
     * @return poruka
     */
  public String getPoruka() {
    return poruka;
  }
  
  /**
   * Getter za {@link #odabraniPartner}
   * @return odabrani partner
   */
  public Partner getOdabraniPartner() {
    return odabraniPartner;
  }
  
  /**
   * Setter za {@link #odabraniPartner}
   * @param odabraniPartner - novi odabrani partner
   */
  public void setOdabraniPartner(Partner odabraniPartner) {
    this.odabraniPartner = odabraniPartner;
  }
  
  /**
   * Getter za {@link #partnerOdabranr}
   * @return jel odabran
   */
  public boolean isPartnerOdabran() {
    return partnerOdabran;
  }
  
  /**
   * Setter za {@link #partnerOdabran}
   * @param partnerOdabran
   */
  public void setPartnerOdabran(boolean partnerOdabran) {
    this.partnerOdabran = partnerOdabran;
  }
  
 /**
  * Prijavljuje korisnika
  * @return kamo proslijediti
  */
  public String prijavaKorisnika() {
    if (this.korisnickoIme != null && this.korisnickoIme.trim().length() > 3 && this.lozinka != null
        && this.lozinka.trim().length() > 5) {

      this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(this.korisnickoIme));
      if (this.korisnik != null) {
        this.prijavljen = true;
        this.korisnickoIme = this.korisnik.korisnik();
        this.lozinka = this.korisnik.lozinka();
        this.poruka = "";
        return "/index.xhtml";
      }
    }
    this.prijavljen = false;
    this.poruka = "Neuspješna prijava korisnika.";
    return "/prijavaKorisnika.xhtml";
  }
  
  /**
   * Kreira zapis
   * @param opisRada - koja radnja se dogodila
   */
  public void zapisiRadnju(String opisRada) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      var zahtjev =  (HttpServletRequest) facesContext.getExternalContext().getRequest();
      var vrijeme = new Timestamp(System.currentTimeMillis());
      zapisiFacade.kreirajZapis(this.getKorisnickoIme(), zahtjev.getRemoteHost(), zahtjev.getRemoteAddr(), opisRada, vrijeme);
	  
  }
  

}
