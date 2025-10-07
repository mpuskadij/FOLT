package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketPartneri;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Skoro identično ko i za dopunu
 */
@ViewScoped
@Named("novaNarudzba")
public class NovaNarudzba implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Sučelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	

	/**
	 * Globalni podaci
	 */
	@Inject
	private GlobalniPodaci globalniPodaci;
	
	/**
	 * Podaci sesije
	 */
	@Inject
	private PrijavaKorisnika prijavaKorisnika;
	
	/**
	 * Jelovnici
	 */
	@Inject
	private PregledJelovnikaPartnera pregledJelovnika;
	
	/**
	 * Karte pića
	 */
	@Inject
	private PregledKartePicaPartnera pregledKartePica;
	
	/**
	 * odabrano jelo
	 */
	private String odabranoJelo;
	
	/**
	 * Odabrano piće
	 */
	private String odabranoPice;
	
	/**
	 * Količina jela
	 */
	private float kolicinaJela = 1;
	
	/**
	 * Količina pića
	 */
	private float kolicinaPica = 1;
	
	/**
	 * Popis naručenih jela
	 */
	private List<Narudzba> narucenaJela = new ArrayList<Narudzba>();
	
	/**
	 * Popis naručenih pića
	 */
	private List<Narudzba> narucenaPica = new ArrayList<Narudzba>();
	
	/**
	 * Poruka
	 */
	private String poruka = "";
	/**
	 * getter
	 * @return poruka
	 */
	public String getPoruka() {
		return this.poruka;
	}
	/**
	 * setter
	 * @param novaPoruka - nova poruka
	 */
	public void setPoruka(String novaPoruka) {
		this.poruka = novaPoruka;
	}
	

	
	/**
	 * Getter
	 * @return odabrano jelo
	 */
	public String getOdabranoJelo() {
		return odabranoJelo;
	}
	
	/**
	 * setter za jelo
	 * @param novoJelo - novo jelo
	 */
	public void setOdabranoJelo(String novoJelo) {
		this.odabranoJelo = novoJelo;
	}
	/**
	 * getter
	 * @return količina jela
	 */
	public float getKolicinaJela() {
		return kolicinaJela;
	}
	/**
	 * setter
	 * @param novaVrijednost - nova količina jela
	 */
	public void setKolicinaJela(float novaVrijednost) {
		this.kolicinaJela = novaVrijednost;
	}
	/**
	 * Getter
	 * @return odabrano piće
	 */
	public String getOdabranoPice() {
		return odabranoPice;
	}
	/**
	 * setter 
	 * @param novoPice - novo piće
	 */
	public void setOdabranoPice(String novoPice) {
		this.odabranoPice = novoPice;
	}
	/**
	 * getter
	 * @return količina jela
	 */
	public float getKolicinaPica() {
		return kolicinaPica;
	}
	/**
	 * setter
	 * @param novaVrijednost - nova količina pića
	 */
	public void setKolicinaPica(float novaVrijednost) {
		this.kolicinaPica = novaVrijednost;
	}
	/**
	 * getter
	 * @return lista naručenih jela
	 */
	public List<Narudzba> getNarucenaJela() {
		return narucenaJela;
	}
	/**
	 * getter
	 * @return lista naručenih pića
	 */
	public List<Narudzba> getNarucenaPica() {
		return narucenaPica;
	}
	
	
	
	/**
	 * Kreira narudžbu
	 */
	@PostConstruct
	public void kreirajNarudzbu() {
		if (prijavaKorisnika.isPartnerOdabran() && !prijavaKorisnika.isAktivnaNarudzba()) {
			try {

		            var odgovor = servisPartner.postNarudzba(
		                this.prijavaKorisnika.getKorisnickoIme(),
		                this.prijavaKorisnika.getLozinka()
		            );

		            if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
	
		                prijavaKorisnika.setAktivnaNarudzba(true);
		                prijavaKorisnika.zapisiRadnju("Nova narudžba");
		                globalniPodaci.povecajBrojOtvorenihNarudzbiPartnera(prijavaKorisnika.getOdabraniPartner().id());
		                posaljiWSPoruku();
		            }
		        }
		     catch (WebApplicationException ex) {
		        this.poruka = "Neuspješno kreiranje narudžbe: " + ex.getResponse().getStatus();
		    }
	} else {
		this.setPoruka("Nema odabranog partnera i/ili nema aktivne narudžbe!");
	}
		    
	}
	/**
	 * Šalje WS poruku
	 */
	private void posaljiWSPoruku() {
		String statusPartnera = "";
		try {
			var odgovorTvrtke = this.servisPartner.headPosluziteljPartner();
			
			if (odgovorTvrtke.getStatus() == Response.Status.OK.getStatusCode()) {
				statusPartnera = "RADI";
			}
			else {
				statusPartnera = "NE RADI";
			}
		} catch(WebApplicationException e) {
			statusPartnera = "NE RADI";
		}
		finally {
			WebSocketPartneri.send(statusPartnera + ";"
					+ globalniPodaci.getBrojOtvorenihNarudzbi().values().stream().mapToInt(Integer::intValue).sum()
					+ ";" + 
					globalniPodaci.getBrojRacuna().values().stream().mapToInt(Integer::intValue).sum()
							);
		}
		
		
	}
	/**
	 * Dodaje jelo u narudžbu
	 */
	public void naruciJelo() {
	var moguciJelovnik = pregledJelovnika.getJelovnici().stream().filter(j -> j.id().equals(odabranoJelo)).findFirst();
		
		if (moguciJelovnik.isPresent()) {
			var pronadeniJelovnik = moguciJelovnik.get();
			var narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), pronadeniJelovnik.id(), true, getKolicinaPica(), pronadeniJelovnik.cijena(), System.currentTimeMillis());
			
			try {
				var odgovor = servisPartner.postJelo(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(), narudzba);
				
				if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
					this.narucenaJela.add(narudzba);
				}
				else {
					this.setPoruka("Greška prilikom dodavanja jela na servis");
				}
			} catch(WebApplicationException e) {
				this.setPoruka("Greška prilikom dodavanja jela na servis");
			}
			
		}
		else {
			this.setPoruka("Nije moguće pronaći jelo!");
		}
	}
	/**
	 * Dodaje piće u narudžbu
	 */
	public void naruciPice() {
		
		var mogucaKartaPica = pregledKartePica.getKartePica().stream().filter(p -> p.id().equals(odabranoPice)).findFirst();
		
		if (mogucaKartaPica.isPresent()) {
			try {

				var pronadenaKartaPica = mogucaKartaPica.get();
				var narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), pronadenaKartaPica.id(), false, getKolicinaPica(), pronadenaKartaPica.cijena(), System.currentTimeMillis());

				var odgovor = servisPartner.postPice(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(), narudzba);
				
				if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
					this.narucenaPica.add(narudzba);
				}
				else {
					this.setPoruka("Greška prilikom dodavanja pića na servis");
				}
			} catch (WebApplicationException e) {
				this.setPoruka("Greška prilikom dodavanja pića na servis");
			}


		}
		else {
			this.setPoruka("Nije moguće pronaći piće!");
		}

	}
	/**
	 * Kreira račun
	 */
	public void platiRacun( ) {
		
		if (prijavaKorisnika.isPartnerOdabran() && prijavaKorisnika.isAktivnaNarudzba()) {
			
			try {
				var odgovor = servisPartner.postRacun(this.prijavaKorisnika.getKorisnickoIme(),this.prijavaKorisnika.getLozinka());
				if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
					prijavaKorisnika.setAktivnaNarudzba(false);
					prijavaKorisnika.zapisiRadnju("Novi račun");
					this.globalniPodaci.smanjiBrojOtvorenihNarudzbiPartnera(prijavaKorisnika.getOdabraniPartner().id());
	                this.globalniPodaci.povecajBrojRacuna(prijavaKorisnika.getOdabraniPartner().id());
	                this.posaljiWSPoruku();
					this.setPoruka("Račun je plaćen!");
					resetirajFormu();
				}
				else {
					this.setPoruka("Neuspješno plaćanje računa!");

				}
			} catch (WebApplicationException e) {
				this.setPoruka("Neuspješno plaćanje računa!");
			}
			
			

		}
		
	}
	/**
	 * Resetira formu
	 */
	private void resetirajFormu() {
		this.kolicinaJela = 1;
		this.kolicinaPica = 1;
		this.narucenaPica.clear();
		this.narucenaJela.clear();
		this.odabranoJelo = null;
		this.odabranoPice = null;
	
	}
	

}
