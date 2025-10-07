package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Partner;
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
 * Sučelje za slanje i dobivanje podataka od REST servisa, koristi klijentTvrtkaInfo postavku gdje se nalazi servis
 */
@RegisterRestClient(configKey = "klijentTvrtka")
@Path("api/tvrtka")
public interface ServisTvrtkaKlijent {
	
	/**
	 * Dohvaća status tvrtke
	 * @return HTTP  odgovor od servisa
	 */
  @HEAD
  public Response headPosluzitelj();
  
  /**
   * Dohvaća status pojedinog dijela (1 - dio za registraciju, 2 - dio za rad s partnerima)
   * @param id - dio tvrtke
   * @return HTTP odgovor
   */
  @Path("status/{id}")
  @HEAD
  public Response headPosluziteljStatus(@PathParam("id") int id);
  
  /**
   * Aktivira pauza određenog dijela
   * @param id - dio tvrtke
   * @return HTTP odgovor
   */
  @Path("pauza/{id}")
  @HEAD
  public Response headPosluziteljPauza(@PathParam("id") int id);
  
  /**
   * Pokreće pauziran dio poslužitelja
   * @param id - dio tvrtke
   * @return HTTP odgovor
   */
  @Path("start/{id}")
  @HEAD
  public Response headPosluziteljStart(@PathParam("id") int id);
  
  /**
   * Šalje zahtjev za zatvaranje tvrtke
   * @return HTTP odgovor
   */
  @Path("kraj")
  @HEAD
  public Response headPosluziteljKraj();
  
  /**
   * Dohvaća sve partnere od servisa
   * @return HTTP odgovor
   */
  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartneri();
  
  /**
   * Dohvaća određenog partnera
   * @param id - id partnera
   * @return HTTP odgovor
   */
  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getOdredeniPartner(@PathParam("id") int id);
  
  /**
   * Dohvaća obračune
   * @param vrijemeOd - vrijeme od
   * @param vrijemeDo - vrijeme do
   * @return HTTP odgovor
   */
  @Path("obracun")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getObracun(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo);
  
  /**
   * Dohvaća obračune koji imaju jela
   * @param vrijemeOd - vijeme od
   * @param vrijemeDo - vrijeme do
   * @return HTTP odgovor
   */
  @Path("obracun/jelo")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getObracunJelo(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo);
  
  /**
   * Dohvaća obračune koji imaju pića
   * @param vrijemeOd - vrijeme od
   * @param vrijemeDo - vrijeme do
   * @return HTTP odgovor
   */
  @Path("obracun/pice")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getObracunPice(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo);
  
  /**
   * Vraća obračune određenog partnera
   * @param id - id partnera
   * @param vrijemeOd - vrijeme od
   * @param vrijemeDo - vrijeme do
   * @return HTTP odgovor
   */
  @Path("obracun/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getObracunPartner(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo);
  
  /**
   * Dodaje novog partnera
   * @param partner - objekt zapisa {@link Partner}
   * @return HTTP odgovor
   */
  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  public Response postPartner(Partner partner);
  
  /**
   * Aktivira spavanje tvrke
   * @param vrijeme - vrijeme u milisekundama
   * @return HTTP odgovor
   */
  @Path("spava")
  @GET
  public Response getSpava(@QueryParam("vrijeme") Long vrijeme);
  
  
  
  
}
