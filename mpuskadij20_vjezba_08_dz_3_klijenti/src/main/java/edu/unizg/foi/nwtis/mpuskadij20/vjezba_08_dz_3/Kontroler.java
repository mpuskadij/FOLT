/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketPartneri;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws.WebSocketTvrtka;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Marin Puškadija
 * Kontroler za JSP stranice (tvrtku)
 */
@Controller
@Path("tvrtka")
@RequestScoped
public class Kontroler {
	
	/**
	 * Globalni podaci aplikacije, koristi se za dohvaćanje broja obračuna
	 */
	@Inject
	private GlobalniPodaci globalniPodaci;
	
	
	/**
	 * Model u MVC arhitekturi, služi za ubacivanje podataka dostupnih pogledu
	 */
	@Inject
	private Models model;
	

	/**
	 * Sučelje za komunikaciju prema REST servisu
	 */
	@Inject
	@RestClient
	ServisTvrtkaKlijent servisTvrtka;
	
	/**
	 * Pretvara string u long za vrijeme
	 * @param datum - datum iz formu u stringu
	 * @return - vrijeme u miliekundama od 1970-e
	 */
	private Long pretvoriUMilisekunde(String datum) {
		LocalDateTime localDateTime = LocalDateTime.parse(datum);
	    return localDateTime
	            .atZone(ZoneId.systemDefault()) 
	            .toInstant()
	            .toEpochMilli();
	}
	/**
	 * Šifra dijela poslužitelja za registraciju
	 */
	private int dioRegistracija = 1;
	
	/**
	 * Šifra dijela poslužitelja za rad s partnerima
	 */
	private int dioPartner = 2;
	
	/**
	 * Prikazuje početnu stranicu tvrtke
	 */
	@GET
	@Path("pocetak")
	@View("index.jsp")
	public void pocetak() {
	}

	
	/**
	 * Prikazuje popis partnera
	 */
	@GET
	@Path("partner")
	@View("partneri.jsp")
	public void partneri() {
		var odgovor = this.servisTvrtka.getPartneri();
		var status = odgovor.getStatus();
		if (status ==  Response.Status.OK.getStatusCode()) {
			var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {
			});
			this.model.put("status", status);
			this.model.put("partneri", partneri);
		}
	}
	
	/**
	 * Prikazuje podatke određenog partnera
	 * @param id - id partnera
	 */
	@GET
	@Path("partner/{id}")
	@View("odredeniPartner.jsp")
	public void odredeniPartner(@PathParam("id") int id) {
		var odgovor = this.servisTvrtka.getOdredeniPartner(id);
		var status = odgovor.getStatus();
		this.model.put("status", status);
		if (status ==  Response.Status.OK.getStatusCode()) {
			var partner = odgovor.readEntity(Partner.class);
			this.model.put("partner", partner);
		}
	}

	/**
	 * Prikazuje obračune svih partnera
	 * @param dobivenOd - datum od
	 * @param dobivenDo - datum do
	 * @param vrsta - pića,jela ili oboje
	 */
	@GET
	@Path("privatno/obracuni")
	@View("obracuni.jsp")
	public void obracuni(@QueryParam("od")  String dobivenOd, @QueryParam("do") String dobivenDo,
			@QueryParam("vrsta") String vrsta) {
		if(vrsta == null) {
			return;
		}
		Long vrijemeOd = null;
		Long vrijemeDo = null;
		if (dobivenOd == null) {
			vrijemeOd = this.pretvoriUMilisekunde(dobivenOd);
		}
		if (dobivenDo == null) {
			vrijemeDo = this.pretvoriUMilisekunde(dobivenDo);
		}
		
		Response odgovor = null;
		switch (vrsta) {

		case "sve":
			odgovor = this.servisTvrtka.getObracun(vrijemeOd, vrijemeDo);
			break;

		case "jelo":
			odgovor = this.servisTvrtka.getObracunJelo(vrijemeOd, vrijemeDo);
			break;

		case "pice":
			odgovor = this.servisTvrtka.getObracunPice(vrijemeOd, vrijemeDo);
			break;
		}
		
		this.model.put("status", odgovor.getStatus());
		
		if (odgovor.getStatus() ==  Response.Status.OK.getStatusCode()) {
			var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {
			});
			this.model.put("obracuni", obracuni);
			
		}

	}
	
	/**
	 * Dohvaća obračune određenog partnera
	 * @param idPartnera - odabrani partner iz forme
	 * @param dobivenOd - vrijeme od
	 * @param dobivenDo - vrijeme do
	 */
	@GET
	@Path("privatno/obracuni/partner")
	@View("obracuniPartnera.jsp")
	public void obracuniPartnera(@QueryParam("id") String idPartnera, @QueryParam("od")  String dobivenOd, @QueryParam("do") String dobivenDo) {
		var odgovorPartnera = this.servisTvrtka.getPartneri();
		
		if (odgovorPartnera.getStatus() ==  Response.Status.OK.getStatusCode()) {
			var partneri = odgovorPartnera.readEntity(new GenericType<List<Partner>>() {
			});
			this.model.put("partneri", partneri);
		}
		if(idPartnera == null) {
			return;
		}
		Long vrijemeOd = null;
		Long vrijemeDo = null;
		if (dobivenOd == null) {
			vrijemeOd = this.pretvoriUMilisekunde(dobivenOd);
		}
		if (dobivenDo == null) {
			vrijemeDo = this.pretvoriUMilisekunde(dobivenDo);
		}
		try {
			Response odgovor = this.servisTvrtka.getObracunPartner(Integer.parseInt(idPartnera), vrijemeOd, vrijemeDo);
			
			this.model.put("status", odgovor.getStatus());
			
			if (odgovor.getStatus() ==  Response.Status.OK.getStatusCode()) {
				var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {
				});
				this.model.put("obracuni", obracuni);
				
			}
		} catch(NumberFormatException e) {
			this.model.put("status", "Neispravan ID partnera");
		}
		
		
	}
	
	/**
	 * Prikazuje status tvrtke
	 */
	@GET
	@Path("provjera")
	@View("provjeraTvrtke.jsp")
	public void provjeraTvrtke() {
		try {
			var odgovor = this.servisTvrtka.headPosluzitelj();

			this.model.put("status", odgovor.getStatus() == Response.Status.OK.getStatusCode() ? "U aktivnom radu!" : "Ne radi");

			
		}catch(WebApplicationException greska) {
			if (greska.getResponse().getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
				this.model.put("status", "Ne radi");

		    } 
		}
	
	
		
	}
	
	/**
	 * Prikazuje formu za dodavanje novog partnera
	 */
	@GET
	@Path("admin/noviPartner")
	@View("noviPartner.jsp")
	public void getNoviPartner() {
		
	}
	
	/**
	 * Dodaje novog partnera koristeći REST servis
	 * @param id - id budućeg partnera
	 * @param naziv - naziv budućeg partnera
	 * @param vrstaKuhinje - vrsta kuhinje budućeg partnera
	 * @param adresa - adresa budućeg partnera
	 * @param mreznaVrata - mrežna vrata budućeg partnera
	 * @param mreznaVrataKraj - mrežna vrata za kraj budućeg partnera
	 * @param gpsSirina - širina budućeg partnera
	 * @param gpsDuzina - dužina budućeg partnera
	 * @param sigurnosniKod - sigurnosni kod budućeg partnera
	 * @param adminKod  -admin kod budućeg partnera
	 */
	
	@POST
	@Path("admin/noviPartner")
	@View("noviPartner.jsp")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void postNoviPartner(
			@FormParam("idPartnera") int id,
			@FormParam("naziv") String naziv,
			@FormParam("vrstaKuhinje") String vrstaKuhinje,
			@FormParam("adresa") String adresa,
			@FormParam("mreznaVrata") int mreznaVrata,
			@FormParam("mreznaVrataKraj") int mreznaVrataKraj,
			@FormParam("gpsSirina") float gpsSirina,
			@FormParam("gpsDuzina") int gpsDuzina,
			@FormParam("sigurnosniKod") String sigurnosniKod,
			@FormParam("adminKod") String adminKod

			) {
		var noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod);

		var odgovor = this.servisTvrtka.postPartner(noviPartner);

		this.model.put("status", odgovor.getStatus());

		
		if (odgovor.getStatus() == Response.Status.CREATED.getStatusCode()) {
			this.model.put("poruka", "Uspješno kreiran partner " + noviPartner.naziv());
			this.model.put("pogreska", false);

		}
		else {
			this.model.put("poruka", "Problem prilikom kreiranja partnera!");
			this.model.put("pogreska", true);
		}
		
		
	}
	
	/**
	 * Prikazuje formu za spavanje tvrtke
	 * @param vrijeme u milisekundama koje da tvrtka spava
	 */
	@GET
	@Path("admin/spavanje")
	@View("spavanje.jsp")
	public void spavanje(@QueryParam("vrijeme") Long vrijeme) {
		if (vrijeme != null && vrijeme > 0L) {
			try {
				this.servisTvrtka.getSpava(vrijeme);
				this.model.put("status", "Tvrtka uspješno spavala " + vrijeme / 1000 + " sekundi!");
				this.model.put("vrijeme", vrijeme);
			}
			
			catch(WebApplicationException greska) {
				if (greska.getResponse().getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
					this.model.put("status", "Tvrtka ne radi trenutačno!");


			    } 
			}
		}
	}
	

	/**
	 * Prikazuje nadzornu konzolu tvrtke
	 * @param dio - koji dio se starta/pauzira ili dohvaća status
	 * @param akcija - startanje,spauziranje ii dohvat statusa
	 */
	@GET
	@Path("admin/nadzornaKonzolaTvrtka")
	@View("nadzornaKonzolaTvrtka.jsp")
	public void nadzornaKonzolaTvrtka(
			@QueryParam("dio") String dio,
			@QueryParam("akcija") String akcija

			) {
		
		
		if (dio != null && akcija != null) {
			Supplier<Response> funkcijaZaPozvati = null;

			switch(dio) {
			case "registracija":
				 if (akcija.equals("pauza")) funkcijaZaPozvati = () -> this.servisTvrtka.headPosluziteljPauza(dioRegistracija);
				 else if (akcija.equals("start")) funkcijaZaPozvati = () -> this.servisTvrtka.headPosluziteljStart(dioRegistracija);

				break;
			case "partner":
				if (akcija.equals("pauza")) funkcijaZaPozvati = () -> this.servisTvrtka.headPosluziteljPauza(dioPartner);
				 else if (akcija.equals("start")) funkcijaZaPozvati = () -> this.servisTvrtka.headPosluziteljStart(dioPartner);
				
				break;
			default:
				break;
			}
		if (funkcijaZaPozvati != null) {
			funkcijaZaPozvati.get();	
		}

		}
		else if (dio == null && akcija != null) {
			if(akcija.equals("kraj")) {
				var odgovor  = this.servisTvrtka.headPosluziteljKraj();
				if (odgovor.getStatus() == Response.Status.OK.getStatusCode()) {
					WebSocketTvrtka.send("NE RADI;" + globalniPodaci.getBrojObracuna() + ";");
				}
			}
		}
		dohvatiStatuse();
		this.model.put("brojObracuna", globalniPodaci.getBrojObracuna());
	}
	
	/**
	 * Dohvaća statuse svih dijelova
	 */
	private void dohvatiStatuse() {
		try {
			this.model.put("samoOperacija", false);
			var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
			this.model.put("statusPosluzitelja", statusT);
			var statusT1 = this.servisTvrtka.headPosluziteljStatus(dioRegistracija).getStatus();
			this.model.put("statusRegistracija", statusT1);
			var statusT2 = this.servisTvrtka.headPosluziteljStatus(dioPartner).getStatus();
			this.model.put("statusPartner", statusT2);
		}catch(WebApplicationException greska) {
			this.model.put("statusPosluzitelja", 500);
			this.model.put("statusRegistracija", 500);
			this.model.put("statusPartner", 500);

		}
			
			
		
		
	}

}
