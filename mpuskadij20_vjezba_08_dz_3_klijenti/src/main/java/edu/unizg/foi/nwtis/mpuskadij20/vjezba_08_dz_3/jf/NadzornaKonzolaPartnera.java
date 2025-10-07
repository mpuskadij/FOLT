package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketPartneri;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Zrno za nadzornu konzolu
 */
@ViewScoped
@Named("nadzornaKonzolaPartnera")
public class NadzornaKonzolaPartnera implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Koji dio predstavlja poslužitelj za kupce
	 */
	private final int dioZaKupce = 1;
	
	/**
	 * Status dijela za kupce
	 */
	private String porukaZaDijelove = "";
	
	/**
	 * Status partnera
	 */
	private String statusPartnera = "";
	
	/**
	 * Globalni podaci
	 */
	@Inject
	private GlobalniPodaci globalniPodaci;
	
	/**
	 * Sučelje za REST servis
	 */
	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;
	
	/**
	 * Getter za status
	 * @return
	 */
	public String getStatusPartnera() {
		return statusPartnera;
	}

	/**
	 * Setter za status
	 * @param statusPartnera
	 */
	public void setStatusPartnera(String statusPartnera) {
		this.statusPartnera = statusPartnera;
	}

	/**
	 * Getter za sumiranje broja otvorenih narudžbi
	 * @return
	 */
	public int getBrojOtvorenihNarudzbi() {
		return globalniPodaci.getBrojOtvorenihNarudzbi().values().stream().mapToInt(Integer::intValue).sum();

	}


	/**
	 * Getter za sumiranje broja računa
	 * @return broj računa
	 */

	public int getBrojRacuna() {
		return globalniPodaci.getBrojRacuna().values().stream().mapToInt(Integer::intValue).sum();

	}



	/**
	 * Getter za poruku za dijelove
	 * @return
	 */
	public String getPorukaZaDijelove() {
		return porukaZaDijelove;
	}

	/**
	 * Setter za poruku za dijelove
	 * @param porukaZaDijelove
	 */
	public void setPorukaZaDijelove(String porukaZaDijelove) {
		this.porukaZaDijelove = porukaZaDijelove;
	}
	


	/**
	 * Šalje WS poruku
	 */
	private void posaljiWSPoruku() {
		String st = "";
		try {
			var odgovorTvrtke = this.servisPartner.headPosluziteljPartner();

			if (odgovorTvrtke.getStatus() == Response.Status.OK.getStatusCode()) {
				st = "RADI";
			}
			else {
				st = "NE RADI";
			}
		}
		catch (WebApplicationException e) {
			st = "NE RADI";
		}
		finally {
			WebSocketPartneri.send(st + ";"
					+ globalniPodaci.getBrojOtvorenihNarudzbi().values().stream().mapToInt(Integer::intValue).sum()
					+ ";" + 
					globalniPodaci.getBrojRacuna().values().stream().mapToInt(Integer::intValue).sum()
							);
		}
		
	
	}
	
	/**
	 * Dohvaća statuse
	 */
	@PostConstruct
	public void pripremiStranicu() {
		try {
			var prviOdgovor = this.servisPartner.headPosluziteljPartner();
			if (prviOdgovor.getStatus() == Response.Status.OK.getStatusCode()) {
				statusPartnera = "RADI";
			}
			else {
				statusPartnera = "NE RADI";
			}
		}catch (WebApplicationException e) {
			statusPartnera = "NE RADI";
		}
		
		var odgovorServisa = servisPartner.headPosluziteljStatus(dioZaKupce);
		if (odgovorServisa.getStatus() == Response.Status.OK.getStatusCode()) {
			this.setPorukaZaDijelove("U aktivnom radu!");
			
		}
		else if (odgovorServisa.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			this.setPorukaZaDijelove("Pauziran!");
		}
		else {
			this.setPorukaZaDijelove("Partner ne radi!");
		}

	}



	/**
	 * Pauzira dio
	 */
	public void pauziraj() {
		var odgovorServisa = servisPartner.headPauza(dioZaKupce);
		if (odgovorServisa.getStatus() == Response.Status.OK.getStatusCode() || odgovorServisa.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			this.setPorukaZaDijelove("Pauziran!");
			
		}
		else {
			this.setPorukaZaDijelove("Partner ne radi!");
		}
	}
	/**
	 * Pokreće dio
	 */
	public void pokreni() {
		var odgovorServisa = servisPartner.headPauza(dioZaKupce);
		if (odgovorServisa.getStatus() == Response.Status.OK.getStatusCode() || odgovorServisa.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			this.setPorukaZaDijelove("U aktivnom radu!");
			
		}
		else {
			this.setPorukaZaDijelove("Partner ne radi!");
		}
	}
	
	/**
	 * Završava partnera
	 */
	public void zavrsiPartnera() {
		var odgovorServisa = servisPartner.headKraj();
		
		if (odgovorServisa.getStatus() == Response.Status.OK.getStatusCode()) {
			this.setPorukaZaDijelove("Ugasnut!");
			this.posaljiWSPoruku();
			
		}
		else if (odgovorServisa.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			setPorukaZaDijelove("Nije moguće zatvoriti partnera!");
		}
		else {
			setPorukaZaDijelove("Nije moguće kontaktirati poslužitelja!");
		}
		
	
	}
	

}
