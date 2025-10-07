package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Klasa koja sadrži globalne podatke potrebne za klijenta
 * @author Marin Puškadija
 */
@ApplicationScoped
public class GlobalniPodaci {
	/**
	 * Broj obračuna kreiranih kroz postojanje web aplikacije
	 */
	private int brojObracuna = 0;
	
	/**
	 * Mapa koja sadrži sve partnere i broj njihovih trenutno otvorenih narudžbi
	 */
	private Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
	
	/**
	 * Mapa koja sadrži sve partnere i broj njihovih računa napravljenih kroz postojanje web aplikacije
	 */
	private Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();
	
	/**
	 * Dohvaća {@link #brojObracuna}
	 * @return {@link #brojObracuna}
	 */
	public int getBrojObracuna() {
		return brojObracuna;
	}
	
	/**
	 * Postavlja {@link #brojObracuna}
	 */
	public void setBrojObracuna(int brojObracuna) {
		this.brojObracuna = brojObracuna;
	}
	
	/**
	 * Dohvaća {@link #brojOtvorenihNarudzbi}
	 * @return {@link #brojOtvorenihNarudzbi}
	 */
	public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
		return brojOtvorenihNarudzbi;
	}
	
	/**
	 * Postavlja {@link #brojOtvorenihNarudzbi}
	 */
	public void setBrojOtvorenihNarudzbi(Map<Integer, Integer> brojOtvorenihNarudzbi) {
		this.brojOtvorenihNarudzbi = brojOtvorenihNarudzbi;
	}
	
	/**
	 * Dohvaća {@link #brojRacuna}
	 * @return {@link #brojRacuna}
	 */
	public Map<Integer, Integer> getBrojRacuna() {
		return brojRacuna;
	}
	
	/**
	 * Postavlja {@link #brojRacuna}
	 */
	public void setBrojRacuna(Map<Integer, Integer> brojRacuna) {
		this.brojRacuna = brojRacuna;
	}
	
	/**
	 * Povećava broj otvorenih narudžbi određenog partnera za 1
	 * @param idPartnera - partner kojemu treba povećati broj otvorenih narudžbi za 1
	 */
	public void povecajBrojOtvorenihNarudzbiPartnera(int idPartnera) {
		var postojiPartner = this.getBrojOtvorenihNarudzbi().containsKey(idPartnera);
		
		if (postojiPartner) {
			this.getBrojOtvorenihNarudzbi().put(idPartnera, this.getBrojOtvorenihNarudzbi().get(idPartnera) + 1); 
		}
		else {
			this.getBrojOtvorenihNarudzbi().put(idPartnera, 1);
		}
		
	}
	
	/**
	* Smanjuje broj otvorenih narudžbi određenog partnera za 1
	 * @param idPartnera - partner kojemu treba smanjiti broj otvorenih narudžbi za 1
	 */
	public void smanjiBrojOtvorenihNarudzbiPartnera(int idPartnera) {
		var postojiPartner = this.getBrojOtvorenihNarudzbi().containsKey(idPartnera);
		
		if (postojiPartner) {
			this.getBrojOtvorenihNarudzbi().put(idPartnera, Math.max(this.getBrojOtvorenihNarudzbi().get(idPartnera) - 1, 0)); 
		}
		
	}
	/**
	 * Povećava broj računa određenog partnera za 1
	 * @param idPartnera - partner kojemu treba povećati broj računa za 1
	 */
	public void povecajBrojRacuna(int idPartnera) {
		var postojiPartnerRacun = this.getBrojRacuna().containsKey(idPartnera);
		
		if (postojiPartnerRacun) {
			this.getBrojRacuna().put(idPartnera, this.getBrojRacuna().get(idPartnera) + 1);
		}
		else {
			this.getBrojRacuna().put(idPartnera, 1);
		}
		this.smanjiBrojOtvorenihNarudzbiPartnera(idPartnera);
		
		
	}
	
	/**
	 * Povećava broj obračuna za 1
	 */
	public void povecajBrojObracuna() {
		var trenutniBrojObracuna = this.getBrojObracuna();
		
		this.setBrojObracuna(trenutniBrojObracuna + 1);
	}



}
