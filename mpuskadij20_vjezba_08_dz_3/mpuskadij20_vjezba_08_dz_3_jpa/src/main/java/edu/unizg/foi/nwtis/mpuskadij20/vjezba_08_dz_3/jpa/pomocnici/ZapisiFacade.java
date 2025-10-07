package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Zapisi_;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class ZapisiFacade extends EntityManagerProducer implements Serializable  {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Criteria API
	 */
	private CriteriaBuilder cb;
	
	/**
	 * Init
	 */
	 @PostConstruct
	  private void init() {
	    cb = getEntityManager().getCriteriaBuilder();
	  }
	 
	 /**
	  * Kreira zapis
	  * @param zapisi
	  */
	 public void create(Zapisi zapisi) {
		    getEntityManager().persist(zapisi);
	}
	 /**
	  * Kreira zapis sa svim podacima
	  * @param korisnik - korisničko ime
	  * @param adresaRacunala - adresa klijenta
	  * @param ipAdresaRacunala - ip adresa klijenta
	  * @param opisRada - koja radnja
	  * @param vrijeme - kad
	  */
	 public void kreirajZapis(String korisnik, String adresaRacunala, String ipAdresaRacunala, String opisRada, Timestamp vrijeme) {
		 var zapis = new Zapisi();
		 zapis.setKorisnickoime(korisnik);
		 zapis.setAdresaracunala(adresaRacunala);
		 zapis.setIpadresaracunala(ipAdresaRacunala);
		 zapis.setVrijeme(vrijeme);
		 zapis.setOpisrada(opisRada);
		 
		 this.create(zapis);
		 
	 }
	 /**
	  * Pronađi sve zapise
	  * @param vrijemeOd - vrijeme od
	  * @param vrijemeDo - vrijeme do
	  * @param korisnik - korisničkok ime
	  * @return lista zapisa sa zadovoljenim kriterijima
	  */
	  public List<Zapisi> findAll(Date vrijemeOd, Date vrijemeDo, String korisnik) {
		    CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
		    Root<Zapisi> zapisi = cq.from(Zapisi.class);

		    List<Predicate> predicates = new ArrayList<>();
	    	Expression<Timestamp> vrijemeExpr = zapisi.get(Zapisi_.vrijeme);

		    
		    if(vrijemeOd != null && vrijemeDo == null) {
			        predicates.add(cb.greaterThanOrEqualTo(vrijemeExpr, new Timestamp(vrijemeOd.getTime())));
			    
		    }
		    
		    else if(vrijemeDo != null && vrijemeOd == null) {
		        predicates.add(cb.lessThanOrEqualTo(vrijemeExpr, new Timestamp(vrijemeDo.getTime())));
		    }
		    
		    else if(vrijemeOd != null && vrijemeDo != null) {
		        predicates.add(cb.between(vrijemeExpr, new Timestamp(vrijemeOd.getTime()), new Timestamp(vrijemeDo.getTime())));

		    }
		    
		    if (korisnik != null) {
		        predicates.add(cb.equal(zapisi.get("korisnickoime"),korisnik));
		    }
		    
	 
		    cq.where(cb.and(predicates.toArray(new Predicate[0])));
		    TypedQuery<Zapisi> query = getEntityManager().createQuery(cq);
		    return query.getResultList();
		  }
	  

}
