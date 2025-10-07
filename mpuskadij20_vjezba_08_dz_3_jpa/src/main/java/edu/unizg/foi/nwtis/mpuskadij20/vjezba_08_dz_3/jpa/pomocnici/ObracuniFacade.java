package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Obracuni_;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Facade za rad s obračunima
 */
@Stateless
public class ObracuniFacade extends EntityManagerProducer implements Serializable  {

	private static final long serialVersionUID = 1L;
	
	 private CriteriaBuilder cb;
	 /**
	  * Init
	  */
	  @PostConstruct
	  private void init() {
	    cb = getEntityManager().getCriteriaBuilder();
	  }
	  /**
	   * Kreiraj obračun
	   * @param obracuni
	   */
	  public void create(Obracuni obracuni) {
		    getEntityManager().persist(obracuni);
		  }
	  /**
	   * Ažurira obračun
	   * @param obracuni
	   */
		  public void edit(Obracuni obracuni) {
		    getEntityManager().merge(obracuni);
		  }
		  /**
		   * Miče obračun
		   * @param obracuni
		   */
		  public void remove(Obracuni obracuni) {
		    getEntityManager().remove(getEntityManager().merge(obracuni));
		  }
		  /**
		   * Pronalazi obračun
		   * @param id
		   * @return
		   */
		  public Obracuni find(Object id) {
		    return getEntityManager().find(Obracuni.class, id);
		  }
		  /**
		   * Pronalaiz sve obračune
		   * @param vrijemeOd - od
		   * @param vrijemeDo - do
		   * @param partner - koji partner
		   * @return
		   */
		  public List<Obracuni> findAll(Date vrijemeOd, Date vrijemeDo, Integer partner) {
			    CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
			    Root<Obracuni> obracuni = cq.from(Obracuni.class);

			    List<Predicate> predicates = new ArrayList<>();
		    	Expression<Timestamp> vrijemeExpr = obracuni.get(Obracuni_.vrijeme);

			    
			    if(vrijemeOd != null && vrijemeDo == null) {
				        predicates.add(cb.greaterThanOrEqualTo(vrijemeExpr, new Timestamp(vrijemeOd.getTime())));
				    
			    }
			    
			    else if(vrijemeDo != null && vrijemeOd == null) {
			        predicates.add(cb.lessThanOrEqualTo(vrijemeExpr, new Timestamp(vrijemeDo.getTime())));
			    }
			    
			    else if(vrijemeOd != null && vrijemeDo != null) {
			        predicates.add(cb.between(vrijemeExpr, new Timestamp(vrijemeOd.getTime()), new Timestamp(vrijemeDo.getTime())));

			    }
			    
			    if (partner != null) {
			    	Path<Partneri> partnerPath = obracuni.get(Obracuni_.partneri);
			        predicates.add(cb.equal(partnerPath.get("id"), partner));
			    }
			    
		 
			    cq.where(cb.and(predicates.toArray(new Predicate[0])));
			    TypedQuery<Obracuni> query = getEntityManager().createQuery(cq);
			    return query.getResultList();
			  }
		  
		  /**
		   * Pretvara listu entiteta u zapise
		   * @param obracuniE - entiteti
		   * @return lista zapisa Obracun
		   */
		  public List<Obracun> pretvori(List<Obracuni> obracuniE) {
			    List<Obracun> obracuni = new ArrayList<>();
			    for (Obracuni obracunE : obracuniE) {
			      var oObjekt = this.pretvori(obracunE);

			      obracuni.add(oObjekt);
			    }

			    return obracuni;
			  }
		  
		  public Obracun pretvori(Obracuni o) {
			    if (o == null) {
			      return null;
			    }
			    var oObjekt =
			        new Obracun(o.getPartneri().getId(),o.getId(),o.getJelo(),(float) o.getKolicina(), (float) o.getCijena(),o.getVrijeme().getTime());

			    return oObjekt;
			  }
		  

		  
		  


}
