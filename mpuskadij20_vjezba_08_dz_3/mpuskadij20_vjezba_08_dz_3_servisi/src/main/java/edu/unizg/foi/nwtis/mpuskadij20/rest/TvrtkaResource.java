package edu.unizg.foi.nwtis.mpuskadij20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.dao.PartnerDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa za putanju nwtis/v1/api/tvrtka
 */
@Path("api/tvrtka")
public class TvrtkaResource {
	
	/**
	 * Adresa PoslužiteljTvrtka
	 */
	@Inject
	@ConfigProperty(name = "adresa")
	private String tvrtkaAdresa;
	
	/**
	 * Mrežna vrata na kojima je poslužitelj za kraj tvrtke
	 */
	@Inject
	@ConfigProperty(name = "mreznaVrataKraj")
	private String mreznaVrataKraj;
	
	/**
	 * Mrežna vrata na kojima je poslužitelj za registraciju
	 */
	@Inject
	@ConfigProperty(name = "mreznaVrataRegistracija")
	private String mreznaVrataRegistracija;
	
	/**
	 * Mrenža vrata na kojima je poslužitelj za rad s partnerima
	 */
	@Inject
	@ConfigProperty(name = "mreznaVrataRad")
	private String mreznaVrataRad;
	
	/**
	 * Admin kod tvrtke
	 */
	@Inject
	@ConfigProperty(name = "kodZaAdminTvrtke")
	private String kodZaAdminTvrtke;
	
	/**
	 * Kod za kraj tvrtke
	 */
	@Inject
	@ConfigProperty(name = "kodZaKraj")
	private String kodZaKraj;
	
	/**
	 * Id partnera
	 */
	@Inject
	@ConfigProperty(name = "idPartner")
	private String idPartnera;
	
	/**
	 * Instance klase {@link RestConfiguration}
	 */
	@Inject
	RestConfiguration restConfiguration;
	
	/**
	 * Rest klijent za komunikaciju prema drugom klijentima
	 */
	@Inject
	@RestClient
    private RestKlijent restKlijent;
	
	
	/**
	 * Provjerava je li tvrtka aktivna
	 * @return 200 ako je, 500 ako nije
	 */
	@HEAD
	@Operation(summary = "Provjera statusa poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
	public Response headPosluzitelj() {
		var status = posaljiKomandu("KRAJ xxx", this.mreznaVrataKraj);
		if (status != null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća status nekog dijela poslužitelja
	 * @param id - dio za kojeg se dohvaća status
	 * @return OK ako je dohvaćen dio, 204 ako nije
	 */
	@Path("status/{id}")
	@HEAD
	@Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljStatus", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStatus(@PathParam("id") int id) {
		var status = posaljiKomandu("STATUS " + this.kodZaAdminTvrtke + " " + id, this.mreznaVrataKraj);
		if (status != null && status.startsWith("OK 1")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Pauzira određen dio poslužitelja tvrtke
	 * @param id - dio koji se pauzira
	 * @return OK ako je pauziran, 204 ako nije
	 */
	@Path("pauza/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljPauza", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
	public Response headPosluziteljPauza(@PathParam("id") int id) {
		var status = posaljiKomandu("PAUZA " + this.kodZaAdminTvrtke + " " + id, this.mreznaVrataKraj);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Pokreće određeni dio poslužitelja
	 * @param id - dio koji se stavlja u aktivan rad
	 * @return OK ako je u stavljen u aktivan rad, 204 ako nije
	 */
	@Path("start/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljStart", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStart(@PathParam("id") int id) {
		var status = posaljiKomandu("START " + this.kodZaAdminTvrtke + " " + id, this.mreznaVrataKraj);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Šalje KRAJ tvrtci
	 * @return OK ako je tvrtka zaustavljena, 204 ako nije
	 */
	@Path("kraj")
	@HEAD
	@Operation(summary = "Zaustavljanje poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljKraj", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
	public Response headPosluziteljKraj() {
		var status = posaljiKomandu("KRAJWS " + this.kodZaKraj, this.mreznaVrataKraj);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	/**
	 * Vraća OK kad tvrtka javi da je završila i kad klijenti vrate OK
	 * @return OK ako je dobiven 200, 204 inače
	 */
	@Path("kraj/info")
	@HEAD
	@Operation(summary = "Šalje REST servisu kod klijenata zahtjev za kraj rada, ako ona vrati OK onda ovaj REST vrati OK, inače 204")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljKrajInfo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
	public Response headPosluziteljKrajInfo() {
		var odgovorDrugogRESTservisa = restKlijent.getKrajInfo();
		
		if (odgovorDrugogRESTservisa.getStatus() == Response.Status.OK.getStatusCode()) {
			return Response.ok().build();
		}
		return Response.noContent().build();	
	}
	
	/**
	 * Dohvaća jelovnike svih partnera iz baze
	 * @return mapa svih vrsta kuhinja i jelovnika
	 */
	@Path("jelovnik")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jelovnika svih partnera u bazi podataka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška")})
	@Counted(name = "brojZahtjeva_getJelovnici", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getJelovnici", description = "Vrijeme trajanja metode")
	public Response getJelovnici() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneri = partnerDAO.dohvatiSve(false);
			if (partneri.size() == 0) {
				return Response.ok(Map.of()).status(Response.Status.OK).build();

			}
			var gson = new Gson();
			var sviJelovnici = new ConcurrentHashMap<String,List<Jelovnik>>();
	        var tipListaJelovnik = new TypeToken<List<Jelovnik>>() {}.getType();
			for (var partner : partneri) {
				if (sviJelovnici.containsKey(partner.vrstaKuhinje())) continue;
				var komanda = "JELOVNIK " + partner.id() + " " + partner.sigurnosniKod();
				var rezultat = this.posaljiKomanduDobivenJson(komanda, this.mreznaVrataRad);
				if (rezultat.containsKey("json")) {
					List<Jelovnik> jelovnici = gson.fromJson(rezultat.get("json"),tipListaJelovnik);
					sviJelovnici.putIfAbsent(partner.vrstaKuhinje(), jelovnici);
						
				}
				
			}
			return Response.ok(sviJelovnici).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća jelovnik od određenog prtnera
	 * @param id - id partnera
	 * @return json niz jelovnika s 200 ili 404 ako nema tog partnera ili nemoga njegovog jelovnika
	 */
	@Path("jelovnik/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jelovnika od partnera po ID-ju")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "404", description = "Nije moguće pronaći jelovnik partnera") })
	@Counted(name = "brojZahtjeva_getJelovnikId", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getJelovnikId", description = "Vrijeme trajanja metode")
	public Response getJelovnikId(@PathParam("id") int id) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partner = partnerDAO.dohvati(id, false);
			if (partner != null) {
				var komanda = "JELOVNIK " + partner.id() + " " + partner.sigurnosniKod();
				var rezultat = this.posaljiKomanduDobivenJson(komanda, mreznaVrataRad);
				if (rezultat.containsKey("odgovor") && rezultat.get("odgovor").startsWith("OK")) {
					var json = rezultat.get("json");
					var tipListaJelovnik = new TypeToken<List<Jelovnik>>() {}.getType();
					var gson = new Gson();
					List<Jelovnik> jelovniciPartnera = gson.fromJson(json,tipListaJelovnik);
					return Response.ok(jelovniciPartnera).status(Response.Status.OK).build();
				}
			}

			return Response.status(Response.Status.NOT_FOUND).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća kartu pića partnera
	 * @return json niz karte pića
	 */
	@Path("kartapica")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat karte pića")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getKartaPica", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
	public Response getKartaPica() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneri = partnerDAO.dohvatiSve(false);
			for (var partner : partneri) {
				var komanda = "KARTAPIĆA " + partner.id() + " " + partner.sigurnosniKod();
				var rezultat = this.posaljiKomanduDobivenJson(komanda, mreznaVrataRad);
				if (rezultat.containsKey("odgovor") && rezultat.get("odgovor").startsWith("OK")) {
					var tipListaKartaPica = new TypeToken<List<KartaPica>>() {}.getType();
					var gson = new Gson();
					var json = rezultat.get("json");
					List<KartaPica> kartaPica = gson.fromJson(json,tipListaKartaPica);
					return Response.ok(kartaPica).status(Response.Status.OK).build();
				}

			}
			return Response.ok(List.of()).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća sve partnere iz baze
	 * @return json niz partnera
	 */
	@Path("partner")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat svih partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartneri", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
	public Response getPartneri() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneri = partnerDAO.dohvatiSve(true);
			return Response.ok(partneri).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća listu partnera koji su i u tvrtci i u bazi
	 * @return listu partnera
	 */
	@Path("partner/provjera")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat svih partnera koji su i u bazi i na PoslužiteljTvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartneriProvjera", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartneriProvjera", description = "Vrijeme trajanja metode")
	public Response getPartneriProvjera() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneriIzBaze = partnerDAO.dohvatiSve(true);
			var odgovor = this.posaljiKomanduDobivenJson("POPIS", mreznaVrataRegistracija);
			var zajednicki = new ArrayList<Partner>();
			if (odgovor.containsKey("json")) {
				var json = odgovor.get("json");
				var gson = new Gson();
				
				
				var partneriIzTvrtke = gson.fromJson(json, PartnerPopis[].class);
				for (var partner : partneriIzTvrtke) {
					var pronadenPartner = partneriIzBaze.stream().filter(pBaze ->  pBaze.id() == partner.id()).findFirst();
					if (pronadenPartner.isPresent()) {
						zajednicki.add(pronadenPartner.get());
					}
				}
				

			}

			return Response.ok(zajednicki).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohaća partnera po ID-ju
	 * @param id - id traženog partnera
	 * @return json objekt tog partnera ili 404 ako ga nema
	 */
	@Path("partner/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jednog partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "404", description = "Ne postoji resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
	public Response getPartner(@PathParam("id") int id) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partner = partnerDAO.dohvati(id, true);
			if (partner != null) {
				return Response.ok(partner).status(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dodaje novog partnera u bazu
	 * @param partner - JSOn objekt zahtjeva koji se pretvori u {@link Partner}
	 * @return 201 ako je kreiran, 409 ako već postoji, inače 500
	 */
	@Path("partner")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jednog partnera")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
			@APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
	public Response postPartner(Partner partner) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var status = partnerDAO.dodaj(partner);
			if (status) {
				return Response.status(Response.Status.CREATED).build();
			} else {
				return Response.status(Response.Status.CONFLICT).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	/**
	 * Dohvaća sve obračune
	 * @param vrijemeOd - filter od
	 * @param vrijemeDo - filter do
	 * @return json niz obračuna
	 */
	@Path("obracun")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat svih obračuna, opcionalno po vrijemeOd i vrijemeDO")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracun", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracun", description = "Vrijeme trajanja metode")
	public Response getObracun(@QueryParam("od") Long vrijemeOd,@QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			
			var obracuni = this.dohvatiObracune(vrijemeOd, vrijemeDo, () -> obracunDAO.dohvatiSve(), () -> obracunDAO.dohvatiOdDo(vrijemeOd, vrijemeDo), () -> obracunDAO.dohvatiOd(vrijemeOd), () -> obracunDAO.dohvatiDo(vrijemeDo));

			return Response.ok(obracuni).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	
	}
	
	/**
	 * Dohvaća obračune jela
	 * @param vrijemeOd - filter od
	 * @param vrijemeDo - filter od
	 * @return json niz obračuna kojima je jelo == true
	 */
	@Path("obracun/jelo")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat obračuna s jelima, opcionalno po vrijemeOd i vrijemeDO")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracunJelo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracunJelo", description = "Vrijeme trajanja metode")
	public Response getObracunJelo(@QueryParam("od") Long vrijemeOd,@QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			
			var obracuni = this.dohvatiObracune(vrijemeOd, vrijemeDo, () -> obracunDAO.dohvatiSveJelo(), () -> obracunDAO.dohvatiOdDoJelo(vrijemeOd, vrijemeDo), () -> obracunDAO.dohvatiOdJelo(vrijemeOd), () -> obracunDAO.dohvatiDoJelo(vrijemeDo));

			return Response.ok(obracuni).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	
	}
	
	/**
	 * Dohvaća obračune pića
	 * @param vrijemeOd - filter od
	 * @param vrijemeDo - filter od
	 * @return json niz obračuna kojima je jelo == false
	 */
	@Path("obracun/pice")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat obračuna s pićima, opcionalno po vrijemeOd i vrijemeDO")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracunPice", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracunPice", description = "Vrijeme trajanja metode")
	public Response getObracunPice(@QueryParam("od") Long vrijemeOd,@QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			
			var obracuni = this.dohvatiObracune(vrijemeOd, vrijemeDo, () -> obracunDAO.dohvatiSvePice(), () -> obracunDAO.dohvatiOdDoPice(vrijemeOd, vrijemeDo), () -> obracunDAO.dohvatiOdPice(vrijemeOd), () -> obracunDAO.dohvatiDoPice(vrijemeDo));

			return Response.ok(obracuni).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	
	}
	/**
	 * Dohvaća obračune od određenog partnera
	 * @param vrijemeOd - filter od
	 * @param vrijemeDo - filter od
	 * @param id - id partnera
	 * @return json niz obračuna kojima je partner == id
	 */
	@Path("obracun/{id}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat obračuna s ID-om partnera, opcionalno po vrijemeOd i vrijemeDO")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjevagetObracunID", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracunID", description = "Vrijeme trajanja metode")
	public Response getObracunID(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd,@QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			
			var obracuni = this.dohvatiObracune(vrijemeOd, vrijemeDo, () -> obracunDAO.dohvatiSve(id), () -> obracunDAO.dohvatiOdDo(id,vrijemeOd, vrijemeDo), () -> obracunDAO.dohvatiOd(id,vrijemeOd), () -> obracunDAO.dohvatiDo(id,vrijemeDo));

			return Response.ok(obracuni).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	
	}
	
	
	/**
	 * Dodaje nove obračune u bazu podataka i šalje za svaki obračun obavijest klijentima koristeći {@link #restKlijent}
	 * @param obracuni - json niz obračuna
	 * @return 201 ako su dodani, 500 ako je greška
	 */
	@Path("obracun")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje obračuna u bazu")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postObracun", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
	public Response postObracun(List<Obracun> obracuni) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);

			var status = obracunDAO.dodaj(obracuni);
			if (status) {
				for (int i = 0; i < obracuni.size(); i++) {
					restKlijent.getObracunWS();
				}
				return Response.status(Response.Status.CREATED).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		

	}
	/**
	 * Dodaje nove obračune u bazu i šalje ih tvrtci
	 * @param obracuni - json niz obračuna
	 * @return 201 ako su dodani, 500 ako je neka greška
	 */
	@Path("obracun/ws")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje obračuna u PoslužiteljTvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postObracunWs", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postObracunWs", description = "Vrijeme trajanja metode")
	public Response postObracunWs(List<Obracun> obracuni) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);

			var status = obracunDAO.dodaj(obracuni);
			if (status) {
				var partnerDAO = new PartnerDAO(vezaBP);

					var gson = new Gson();
					
					var grupe = obracuni.stream().collect(Collectors.groupingBy(Obracun::partner));
					for(var obracuniJednogPartnera : grupe.entrySet()) {
						var partner = partnerDAO.dohvati(obracuniJednogPartnera.getKey(),false);
						var json = gson.toJson(obracuniJednogPartnera.getValue());
						var statusTvrtke = this.posaljiKomandu("OBRAČUNWS " + partner.id() + " " + partner.sigurnosniKod() + "\n" + json,
								mreznaVrataRad);
						if (statusTvrtke == null || statusTvrtke.startsWith("ERROR")) {
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
						}					

				
				}
					return Response.status(Response.Status.CREATED).build();

			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	/**
	 * Šalje SPAVA tvrtci
	 * @param vrijeme - vrijeme u milisekundama koliko spavati
	 * @return OK ako je tvrtka spavala, 500 ako je bila greška
	 */
	@Path("spava")
	@GET
	@Operation(summary = "Spavanje poslužitelja na određeno vrijeme")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "getSpava", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
	public Response getSpava(@QueryParam("vrijeme") Long vrijeme) {
		if (vrijeme == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		var status = this.posaljiKomandu("SPAVA " + this.kodZaAdminTvrtke + " " + vrijeme, mreznaVrataKraj);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Šalje komandu na određena vrata
	 * @param komanda - sama komanda
	 * @param mreznaVrata - na koja vrata tvrtke poslati komandu
	 * @return odgovor
	 */
	private String posaljiKomandu(String komanda, String mreznaVrata) {
		try {
			var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(mreznaVrata));
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();
			var linija = in.readLine();
			mreznaUticnica.shutdownInput();
			mreznaUticnica.close();
			return linija;
		} catch (IOException e) {
		}
		return null;
	}
	/**
	 * Šalje komandu na određena vrata tvrtke i očekuje višelinijski odgovor
	 * @param komanda - sama komanda
	 * @param mreznaVrata - vrata tvrtke kamo se šalje
	 * @return mapu gdje je "odgovor" ključ dobiven odgovor, a "json" je json koji je dobiven
	 */
	private Map<String, String> posaljiKomanduDobivenJson(String komanda, String mreznaVrata) {
		try {
			var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(mreznaVrata));
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"), true);
			out.println(komanda);
			mreznaUticnica.shutdownOutput();
			var json = new HashMap<String, String>();
			var linija = in.readLine();
			json.put("odgovor", linija);
			if (linija.startsWith("OK")) {
				linija = "";
				while (!linija.endsWith("]")) {
					linija += in.readLine();

				}

				json.put("json", linija);
			}

			mreznaUticnica.shutdownInput();
			mreznaUticnica.close();
			return json;
		} catch (IOException e) {
		}
		return Map.of();
	}
	/**
	 * Poziva određene funkcije ovisno o tome koji parameri su prisutni
	 * @param vrijemeOd - filter
	 * @param vrijemeDo - filter
	 * @param bezParametara - funkcija ako nema filtera
	 * @param odDo - funkcija ako su oba filtera
	 * @param od - funkcija ako je vrijemeOd samo dobiveno
	 * @param doFunkcija - funkcija ako je vrijemeDo samo dobiveno
	 * @return
	 */
	private List<Obracun> dohvatiObracune(Long vrijemeOd, Long vrijemeDo,Supplier<List<Obracun>> bezParametara,Supplier<List<Obracun>> odDo, Supplier<List<Obracun>> od, Supplier<List<Obracun>> doFunkcija) {
		if (vrijemeDo == null && vrijemeOd == null) return bezParametara.get();
		else if(vrijemeDo != null && vrijemeOd != null) return odDo.get();
		else if (vrijemeOd != null && vrijemeDo == null) return od.get();
		else if (vrijemeOd == null && vrijemeDo != null) return doFunkcija.get();
		return List.of();
	}
}
