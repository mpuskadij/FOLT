package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
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
 * REST servis partnera
 */
@RegisterRestClient(configKey = "klijentPartner")
@Path("api/partner")
public interface ServisPartnerKlijent {
	/**
	 * Dohvaća status partnera
	 * @return HTTP odgovor
	 */
	@HEAD
	public Response headPosluziteljPartner();
	
	/**
	 * Dohvaća status određenog dijela (1 - kupci)
	 * @param id . dio
	 * @return HTTP odgovor
	 */
	@HEAD
	@Path("status/{id}")
	public Response headPosluziteljStatus(@PathParam("id") int id);
	
	/**
	 * Pauzira određeni dio
	 * @param id - dio
	 * @return HTTP odgovor
	 */
	@HEAD
	@Path("pauza/{id}")
	public Response headPauza(@PathParam("id") int id);
	
	/**
	 * Pokreće određeni dio
	 * @param id - dio
	 * @return HTTP odgovor
	 */
	@HEAD
	@Path("start/{id}")
	public Response headStart(@PathParam("id") int id);
	
	/**
	 * Završava partnera
	 * @return HTTP odgovor
	 */
	@HEAD
	@Path("kraj")
	public Response headKraj();
	
	/**
	 * Dodaje korisnika
	 * @param korisnik - novi korisnik
	 * @return HTTP odgovor
	 */
	@POST
	@Path("korisnik")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response postKorisnik(Korisnik korisnik);
	
	/**
	 * Dohvaća jelovnik partnera
	 * @param korisnik - korisik
	 * @param lozinka - lozinka
	 * @return HTTP odgovor
	 */
	@GET
	@Path("jelovnik")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getJelovnik(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	/**
	 * Dohvaća kartu pića partnera
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @return HTTP odgovor
	 */
	@GET
	@Path("kartapica")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getKartaPica(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	/**
	 * Kreira narudžbu
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @return HTTP odgovor
	 */
	@POST
	@Path("narudzba")
	public Response postNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	/**
	 * Kreira račun
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @return HTTP odgovor
	 */
	@POST
	@Path("racun")
	public Response postRacun(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	/**
	 * Dodaje jelo narudžbi
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @param narudzba - novo jelo
	 * @return HTTP odgovor
	 */
	@POST
	@Path("jelo")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response postJelo(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,Narudzba narudzba);
	
	/**
	 * Dodaje piće narudžbi
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @param narudzba - novo piće
	 * @return HTTP odgovor
	 */
	@POST
	@Path("pice")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response postPice(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka, Narudzba narudzba);
	
	/**
	 * Dohvaća otvorenu narudžbu
	 * @param korisnik - korisnik
	 * @param lozinka - lozinka
	 * @return HTTP odgovor
	 */
	@GET
	@Path("narudzba")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	/**
	 * Spava partnera
	 * @param vrijeme - milisekunde
	 * @return HTTP odgovor
	 */
	@GET
	@Path("spava")
	public Response getSpava(@QueryParam("vrijeme") Long vrijeme);
	
	/**
	 * Dohvaća korisnike
	 * @return HTTP odgovor
	 */
	@GET
	@Path("korisnik")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getKorisnici();
	
	/**
	 * Dohvaća detalje korisnika
	 * @param id - korisničko ime
	 * @return HTTP odgovor
	 */
	@GET
	@Path("korisnik/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getKorisnikID(@PathParam("id") String id);
	

}
