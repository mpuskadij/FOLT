package edu.unizg.foi.nwtis.mpuskadij20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa za putanju nwtis/v1/api/partner
 */
@Path("api/partner")
public class PartnerResource {
	/**
	 * Instanca klase {@link RestConfiguration}
	 */
	@Inject
	RestConfiguration restConfiguration;
	
	/**
	 * Adresa pokrenutog PoslužiteljPartner
	 */
	@Inject
	@ConfigProperty(name = "adresaPartner")
	private String adresaPartner;
	
	/**
	 * Mrežna vrata na kojima je poslužitelj za kraj rada partnera
	 */
	@Inject
	@ConfigProperty(name = "mreznaVrataKrajPartner")
	private String mreznaVrataKrajPartner;
	
	/**
	 * Mrežna vrata na kojima je poslužitelj za prijem zahtjeva kupaca
	 */
	@Inject
	@ConfigProperty(name = "mreznaVrataRadPartner")
	private String mreznaVrataRadPartner;
	
	/**
	 * Admin kod partnera
	 */
	@Inject
	@ConfigProperty(name = "kodZaAdminPartnera")
	private String kodZaAdminPartnera;
	
	/**
	 * Kod za kraj partnera
	 */
	@Inject
	@ConfigProperty(name = "kodZaKraj")
	private String kodZaKraj;
	
	/**
	 * Aktivira se na HEAD /tvrtka
	 * @return OK ako partner radi, 500 ako ne
	 */
	@HEAD
	@Operation(summary = "Provjera statusa poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
	public Response headPosluzitelj() {
		var status = posaljiKomandu("KRAJ xxx", this.mreznaVrataKrajPartner);
		if (status != null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Aktivira se na /status/{id}
	 * @param id - id dijela poslužitelja
	 * @return 200 ako je dohvaćen status, 204 ako nije
	 */
	@Path("status/{id}")
	@HEAD
	@Operation(summary = "Provjera statusa dijela poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljStatus", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStatus(@PathParam("id") int id) {
		var status = posaljiKomandu("STATUS " + this.kodZaAdminPartnera + " " + id, this.mreznaVrataKrajPartner);
		if (status != null && status.startsWith("OK 1")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Aktivira se na /pauza/{id}
	 * @param id - id dijela poslužitelja
	 * @return 200 ako je pauziran dio, 204 ako nije
	 */
	@Path("pauza/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja partner u pauzu")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljPauza", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
	public Response headPosluziteljPauza(@PathParam("id") int id) {
		var status = posaljiKomandu("PAUZA " + this.kodZaAdminPartnera + " " + id, this.mreznaVrataKrajPartner);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Aktivira se na /start/{id}
	 * @param id - id dijela poslužitelja
	 * @return 200 ako je pokrenut dio, 204 ako nije
	 */
	@Path("start/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljStart", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStart(@PathParam("id") int id) {
		var status = posaljiKomandu("START " + this.kodZaAdminPartnera + " " + id, this.mreznaVrataKrajPartner);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Aktivira se na HEAD /kraj
	 * @return 200 ako je završen partner, 204 ako nije
	 */
	@Path("kraj")
	@HEAD
	@Operation(summary = "Zaustavljanje poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljKraj", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
	public Response headPosluziteljKraj() {
		var status = posaljiKomandu("KRAJ " + this.kodZaKraj, this.mreznaVrataKrajPartner);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}
	
	/**
	 * Dohvaća jelovnike od partnera
	 * @param korisnik - korisnik za kojeg dohvaća
	 * @param lozinka - lozinka korisnika za autentifikaciju
	 * @return OK ako je dohvaćen, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška
	 */
	@Path("jelovnik")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jelovnik od partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravan korisnik"),
			@APIResponse(responseCode = "500", description = "Greška na servisu") })
	@Counted(name = "brojZahtjeva_getJelovnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
	public Response getJelovnik(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			var status = posaljiKomanduDobivenJson("JELOVNIK " + korisnik, this.mreznaVrataRadPartner);
			if (status.containsKey("json")) {
		        var tipListaJelovnik = new TypeToken<List<Jelovnik>>() {}.getType();
				var gson = new Gson();
				var json = status.get("json");
				List<Jelovnik> jelovnici = gson.fromJson(json, tipListaJelovnik);
				return Response.ok(jelovnici).status(Response.Status.OK).build();
			}
			return Response.ok(List.of()).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	

	/**
	 * Dohvaća kartu pića od partnera
	 * @param korisnik - korisnik za kojeg dohvaća
	 * @param lozinka - lozinka korisnika za autentifikaciju
	 * @return OK ako je dohvaćena, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška
	 */
	@Path("kartapica")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat karte pića od partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravan korisnik"),
			@APIResponse(responseCode = "500", description = "Greška na servisu") })
	@Counted(name = "brojZahtjeva_getKartaPica", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
	public Response getKartaPica(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			var status = posaljiKomanduDobivenJson("KARTAPIĆA " + korisnik, this.mreznaVrataRadPartner);
			if (status.containsKey("json")) {
				var tipListaKartaPica = new TypeToken<List<KartaPica>>() {}.getType();
				var gson = new Gson();
				var json = status.get("json");
				List<KartaPica> kartepica = gson.fromJson(json, tipListaKartaPica);
				return Response.ok(kartepica).status(Response.Status.OK).build();
			}
			return Response.ok(List.of()).status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	

	/**
	 * Dohvaća stavke otvorene narudžbe partnera
	 * @param korisnik - korisnik za kojeg dohvaća otvorenu narudžbu
	 * @param lozinka - lozinka korisnika za autentifikaciju
	 * @return OK ako je otvorena narudžba, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška
	 */
	@Path("narudzba")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvaćanje otvorene narudžbe")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješno dohvaćanje otvorene narudžbe"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "401", description = "Neispravan korisnik")})
	@Counted(name = "brojZahtjeva_getNarudzba", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getNarudzba", description = "Vrijeme trajanja metode")
	public Response getNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			var status = posaljiKomanduDobivenJson("STANJE " + korisnik, this.mreznaVrataRadPartner);
			if (status.containsKey("odgovor") && status.get("odgovor").startsWith("OK")) {
				var tipListaNarudzbi= new TypeToken<List<Narudzba>>() {}.getType();
				var gson = new Gson();
				var json = status.get("json");
				List<Narudzba> narudzbeKorisnika = gson.fromJson(json, tipListaNarudzbi);
				return Response.ok(narudzbeKorisnika).status(Response.Status.OK).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	/**
	 * Otvara narudžu za partnera
	 * @param korisnik - korisnik za kojeg otvara narudžbu
	 * @param lozinka - lozinka korisnika za autentifikaciju
	 * @return OK ako je otvorena narudžba, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška
	 */
	@Path("narudzba")
	@POST
	@Operation(summary = "Kreiranje narudzbe")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "401", description = "Neispravan korisnik"),
			@APIResponse(responseCode = "409", description = "Narudžba već otvorena!") })
	@Counted(name = "brojZahtjeva_postNarudzba", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postNarudzba", description = "Vrijeme trajanja metode")
	public Response postNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			var status = posaljiKomandu("NARUDŽBA " + korisnik, this.mreznaVrataRadPartner);
			if (status != null && status.startsWith("OK")) {
				return Response.status(Response.Status.CREATED).build();
			}
			return Response.status(Response.Status.CONFLICT).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	/**
	 * Dodaje jelo u otvorenu narudžbu
	 * @param korisnik - korisnik za autentifikaciju
	 * @param lozinka - lozinka za autentifikaciju
	 * @param narudzba - narudžba u tijelu zahtjeva
	 * @return OK ako je dodano jelo, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška, CONFLICT kao nije otvorena narudžba
	 */
	@Path("jelo")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje jela u narudžbu")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "409", description = "Narudžba nije otvorena"),
			@APIResponse(responseCode = "401", description = "Neispravno tijelo zahtjeva") })
	@Counted(name = "brojZahtjeva_postJelo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postJelo", description = "Vrijeme trajanja metode")
	public Response postJelo(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			var validanKorisnik = narudzba.korisnik().equals(korisnik);
			var jelo = narudzba.jelo() == true;
			var validnaKolicina = narudzba.kolicina() > 0;
			var validnaCijena = narudzba.cijena() > 0;
			
			if (validanKorisnik && jelo && validnaCijena && validnaKolicina) {
				var status = this.posaljiKomandu("JELO " + korisnik + " " + narudzba.id() + " " + narudzba.kolicina(),
						mreznaVrataRadPartner);
				if (status != null && status.startsWith("OK")) {
					return Response.status(Response.Status.CREATED).build();

				}
				return Response.status(Response.Status.CONFLICT).build();
			}
			return Response.status(Response.Status.CONFLICT).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	/**
	 * Dodaje piće u otvorenu narudžbu
	 * @param korisnik - korisnik za autentifikaciju
	 * @param lozinka - lozinka za autentifikaciju
	 * @param narudzba - narudžba u tijelu zahtjeva
	 * @return OK ako je dodano piće, UNAUTHORIZED ako nije autentificiran, INTERNAL SERVER ERROR ako je neka druga greška, CONFLICT kao nije otvorena narudžba
	 */
	@Path("pice")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje pića u narudžbu")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "409", description = "Neispravna narudžba"),
			@APIResponse(responseCode = "401", description = "Neispravan korisnik") })
	@Counted(name = "brojZahtjeva_postPice", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPice", description = "Vrijeme trajanja metode")
	public Response postPice(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			var validanKorisnik = narudzba.korisnik().equals(korisnik);
			var jelo = narudzba.jelo() == false;
			var validnaKolicina = narudzba.kolicina() > 0;
			var validnaCijena = narudzba.cijena() > 0;

			if (validanKorisnik && jelo && validnaCijena && validnaKolicina) {
				var status = this.posaljiKomandu("PIĆE " + korisnik + " " + narudzba.id() + " " + narudzba.kolicina(),
						mreznaVrataRadPartner);
				if (status != null && status.startsWith("OK")) {
					return Response.status(Response.Status.CREATED).build();

				}
				return Response.status(Response.Status.CONFLICT).build();
			}
			return Response.status(Response.Status.CONFLICT).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	/**
	 * Stavara račun za korisnika
	 * @param korisnik - korisnik za autentifikaciju
	 * @param lozinka - lozinka za autentifikaciju
	 * @return OK ako je račun stvoren, CONFLICT ako je neka greška kod stvaranja računa bila, INTERNAL SERVER za grešku servisa, UNAUTHORIZED za neispravnog korisnsika
	 */
	@Path("racun")
	@POST
	@Operation(summary = "Izdavanje računa")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška"),
			@APIResponse(responseCode = "409", description = "Problem kod stvaranja računa"), 
			@APIResponse(responseCode = "401", description = "Neispravan korisnik")})
	@Counted(name = "brojZahtjeva_postRacun", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postRacun", description = "Vrijeme trajanja metode")
	public Response postRacun(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnikBaza = korisnikDAO.dohvati(korisnik, lozinka, true);
			if (korisnikBaza == null) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			var status = this.posaljiKomandu("RAČUN " + korisnik,
					mreznaVrataRadPartner);
			if (status != null && status.startsWith("OK")) {
				return Response.status(Response.Status.CREATED).build();

			}
			return Response.status(Response.Status.CONFLICT).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}


	}
	
	/**
	 * DOhvaća sve korisnike trenutačno u bazi
	 * @return listu korisnika u json formatu
	 */
	@Path("korisnik")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat svih korisnika u bazi podataka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "getKorisnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKorisnik", description = "Vrijeme trajanja metode")
	public Response getKorisnik() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnici = korisnikDAO.dohvatiSve();
			return Response.ok(korisnici).status(Response.Status.OK).build();
			

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Dohvaća korisnika po korisničkom imenu
	 * @param id - korisničko ime korisnika
	 * @return tog korisnika ili grešku ako ne postoji
	 */
	@Path("korisnik/{id}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Operation(summary = "Dohvat korisnika pod ID-ju")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "404", description = "Nije pronađen korisnik s tim ID-om"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "getKorisnikID", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKorisnikID", description = "Vrijeme trajanja metode")
	public Response getKorisnikID(@PathParam("id") String id) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnik = korisnikDAO.dohvati(id,"",false);
			if(korisnik == null) {
				 return Response.status(Response.Status.NOT_FOUND).build();
			}
			
			return Response.ok(korisnik).status(Response.Status.OK).build();
			

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Stvara novog korisnika
	 * @param korisnik - objekt klase {@link Korisnik}	 
	 *  @return 201 ako je kreiran, 409 ako već postoji u bazi, 500 za grešku servisa
	 */
	@Path("korisnik")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Operation(summary = "Kreiranje novog korisnika")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran korisnik"),
			@APIResponse(responseCode = "409", description = "Već postoji korisnik"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "postKorisnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postKorisnik", description = "Vrijeme trajanja metode")
	public Response postKorisnik(Korisnik korisnik) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var status = korisnikDAO.dodaj(korisnik);
			if(status) {
				 return Response.status(Response.Status.CREATED).build();
			}
			
			return Response.status(Response.Status.CONFLICT).build();
			

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Aktivira se na GET /spava?vrijeme
	 * @param vrijeme - vrijeme u milisekundama koliko spavati
	 * @return OK ako je partner odspavao uspješno, INTERNAL SERVER ERROR ako nije
	 */
	@Path("spava")
	@GET
	@Operation(summary = "Spavanje poslužitelja partnera na određeno vrijeme")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "getSpava", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
	public Response getSpava(@QueryParam("vrijeme") Long vrijeme) {
		if (vrijeme == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		var status = this.posaljiKomandu("SPAVA " + this.kodZaAdminPartnera + " " + vrijeme, mreznaVrataKrajPartner);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}
	
	/**
	 * Šalje komandu na {@link #adresaPartner}
	 * @param komanda  -komanda za poslati
	 * @param mreznaVrata - vrata kamo poslati
	 * @return odgovor od partnera, null ako nije odgovorio
	 */
	private String posaljiKomandu(String komanda, String mreznaVrata) {
		try {
			var mreznaUticnica = new Socket(this.adresaPartner, Integer.parseInt(mreznaVrata));
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
	 * Šalje komande i očekuje višelinijski odgovor, gdje je drugi redak JSON
	 * @param komanda - komanda za poslati
	 * @param mreznaVrata - na koja vrata poslati na adresi {@link #adresaPartner} 
	 * @return mapa gdje je "odgovor" ključ odgovor partnera, a "json" ključ sadrži dobiven json, inače praznu mapu
	 */
	private Map<String, String> posaljiKomanduDobivenJson(String komanda, String mreznaVrata) {
		try {
			var mreznaUticnica = new Socket(this.adresaPartner, Integer.parseInt(mreznaVrata));
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

}