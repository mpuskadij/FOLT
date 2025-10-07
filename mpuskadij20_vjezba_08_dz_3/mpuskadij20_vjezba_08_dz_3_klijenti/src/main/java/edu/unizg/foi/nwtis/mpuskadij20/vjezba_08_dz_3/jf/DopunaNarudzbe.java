package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketPartneri;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za dopunanarudzbe.xhtml
 */
@ViewScoped
@Named("dopunaNarudzbe")
public class DopunaNarudzbe implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Sučelje za komunikaciju prema REST servisu
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
	 * Zrno koje sadrži poadtke korisnika /sesije
	 */
	@Inject
	private PrijavaKorisnika prijavaKorisnika;
	
	/**
	 * Zrno za dohvat jelovnika
	 */
	@Inject
	private PregledJelovnikaPartnera pregledJelovnika;
	
	/**
	 * Zrno za dohvati karte pića
	 */
	@Inject
	private PregledKartePicaPartnera pregledKartePica;
	
	/**
	 * Naziv odabranog jela
	 */
	private String odabranoJelo;
	
	/**
	 * Naziv odabrane karte pića
	 */
	private String odabranoPice;
	
	/**
	 * Količina odabranog jela
	 */
	private float kolicinaJela = 1;
	
	/**
	 * Količina odabranog pića
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
	
	private String poruka = "";
	
	public String getPoruka() {
		return this.poruka;
	}
	/**
	 * setter za {@link #poruka}
	 * @param novaPoruka - poruka za prikazati
	 */
	public void setPoruka(String novaPoruka) {
		this.poruka = novaPoruka;
	}
	

	/**
	 * getter za {@link #odabranoJelo}
	 * @return odabrano jelo
	 */
	public String getOdabranoJelo() {
		return odabranoJelo;
	}
	
	/**
	 * Setter za {@link #odabranoJelo}
	 * @param novoJelo - naziv novog odabranog jela
	 */
	public void setOdabranoJelo(String novoJelo) {
		this.odabranoJelo = novoJelo;
	}
	
	/**
	 * Getter za {@link #kolicinaJela}
	 * @return količina jela
	 */
	public float getKolicinaJela() {
		return kolicinaJela;
	}
	
	/**
	 * Setter za {@link #kolicinaJela}
	 * @param novaVrijednost
	 */
	public void setKolicinaJela(float novaVrijednost) {
		this.kolicinaJela = novaVrijednost;
	}
	/**
	 * Getter za {@link #odabranoPice}
	 * @return
	 */
	public String getOdabranoPice() {
		return odabranoPice;
	}
	
	/**
	 * Setter za {@link #odabranoPice}
	 * @param novoPice - novo odabrano piće
	 */
	public void setOdabranoPice(String novoPice) {
		this.odabranoPice = novoPice;
	}
	
	/**
	 * Getter za {@link #kolicinaPica}
	 * @return količinu pića
	 */
	public float getKolicinaPica() {
		return kolicinaPica;
	}
	
	/**
	 * Setter za {@link #kolicinaPica}
	 * @param novaVrijednost
	 */
	public void setKolicinaPica(float novaVrijednost) {
		this.kolicinaPica = novaVrijednost;
	}
	
	/**
	 * Getter za {@link #narucenaJela}
	 * @return naručena jela
	 */
	public List<Narudzba> getNarucenaJela() {
		return narucenaJela;
	}
	
	/**
	 * Getter za {@link #narucenaPica}
	 * @return
	 */
	public List<Narudzba> getNarucenaPica() {
		return narucenaPica;
	}
	
	
	
	/**
	 * Dohvaća postojeću otvorenu narudžbu
	 */
	@PostConstruct
	public void dohvatiOtvorenuNarudzbu() {
		if (prijavaKorisnika.isPartnerOdabran() && prijavaKorisnika.isAktivnaNarudzba()) {
			try {

		            var odgovor = servisPartner.getNarudzba(
		                this.prijavaKorisnika.getKorisnickoIme(),
		                this.prijavaKorisnika.getLozinka()
		            );

		            if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
	
		                prijavaKorisnika.zapisiRadnju("Dopuna narudžbe");
		                var sveNarudzbe = odgovor.readEntity(new GenericType<List<Narudzba>>() {
						});
		                
		                var grupe = sveNarudzbe.stream().collect(Collectors.groupingBy(Narudzba::jelo));
		                
		                this.narucenaJela = grupe.getOrDefault(true, new ArrayList<Narudzba>());
		                this.narucenaPica = grupe.getOrDefault(false,new ArrayList<Narudzba>());
		            }
		        }
		     catch (WebApplicationException ex) {
		        this.poruka = "Neuspješno dohvaćanje narudžbe: " + ex.getResponse().getStatus();
		    }
	} else {
		this.setPoruka("Nema odabranog partnera i/ili nema aktivne narudžbe!");
	}
		    
	}
	/**
	 * Šalje jelo na REST servis i ažurira popis naručenih jela
	 */
	public void naruciJelo() {
	var moguciJelovnik = pregledJelovnika.getJelovnici().stream().filter(j -> j.id().equals(odabranoJelo)).findFirst();
		
		if (moguciJelovnik.isPresent()) {
			try {
				var pronadeniJelovnik = moguciJelovnik.get();
				var narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), pronadeniJelovnik.id(), true, getKolicinaPica(), pronadeniJelovnik.cijena(), System.currentTimeMillis());
				
				var odgovor = servisPartner.postJelo(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(), narudzba);
				
				if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
					this.narucenaJela.add(narudzba);
				}
				else {
					this.setPoruka("Greška prilikom dodavanja jela na servis");
				}
			} catch (WebApplicationException e) {
				this.setPoruka("Greška prilikom dodavanja jela na servis");
			}
			
		}
		else {
			this.setPoruka("Nije moguće pronaći jelo!");
		}
	}
	/**
	 * Šalje piće na REST servis i ažurira popis naručenih pića
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
			}catch (WebApplicationException e) {
				this.setPoruka("Greška prilikom dodavanja pića na servis");
			}

			

		}
		else {
			this.setPoruka("Nije moguće pronaći piće!");
		}

	}
	
	/**
	 * Plaća se račun pomoću REST servisa i šalje se websocket poruka te zapis
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
	 * Šalje WS poruku na /ws/partneri
	 */
	private void posaljiWSPoruku() {
		
		String statusPartnera = "";
		try {
			var odgovorPartnera = this.servisPartner.headPosluziteljPartner();
			if (odgovorPartnera.getStatus() == Response.Status.OK.getStatusCode()) {
				statusPartnera = "RADI";
			}
			else {
				statusPartnera = "NE RADI";
			}
		}catch (WebApplicationException e) {
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
	 * Resetira formu na početno stanje
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
