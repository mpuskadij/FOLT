/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.sql.Connection;
import java.sql.DriverManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 *
 * @author Marin Puškadija
 * Konfiguracija baze podataka
 */
@ApplicationScoped
public class RestConfiguration {
	/**
	 * Korisnik baze
	 */
  @Inject
  @ConfigProperty(name = "korisnickoImeBazaPodataka")
  private String korisnickoImeBazaPodataka;
  
  /**
   * Lozinka korisnika
   */
  @Inject
  @ConfigProperty(name = "lozinkaBazaPodataka")
  private String lozinkaBazaPodataka;
  
  /**
   * Driver
   */
  @Inject
  @ConfigProperty(name = "upravljacBazaPodataka")
  private String upravljacBazaPodataka;
  
  /**
   * URL baze
   */
  @Inject
  @ConfigProperty(name = "urlBazaPodataka")
  private String urlBazaPodataka;
  
  /**
   * dohvaća vezu
   * @return vezu
   * @throws Exception - ako veza se ne može uspostaviti
   */
  public Connection dajVezu() throws Exception {
    Class.forName(this.upravljacBazaPodataka);
    var vezaBazaPodataka = DriverManager.getConnection(this.urlBazaPodataka,
        this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka);
    return vezaBazaPodataka;
  }    
}
