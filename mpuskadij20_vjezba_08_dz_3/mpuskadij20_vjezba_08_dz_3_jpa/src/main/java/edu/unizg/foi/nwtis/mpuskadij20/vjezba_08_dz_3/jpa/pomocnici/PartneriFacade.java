package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

/**
 * Facade za partnere
 */
@Stateless
public class PartneriFacade extends EntityManagerProducer implements Serializable {
	  private static final long serialVersionUID = 3595041786540495885L;
	  
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
	   * Kreira partnera
	   * @param partneri
	   */
	  public void create(Partneri partneri) {
		    getEntityManager().persist(partneri);
		  }
	  
	  /**
	   * Ažurira partnera
	   * @param partneri
	   */
		  public void edit(Partneri partneri) {
		    getEntityManager().merge(partneri);
		  }
		  /**
		   * Miče partnera
		   * @param partneri
		   */
		  public void remove(Partneri partneri) {
		    getEntityManager().remove(getEntityManager().merge(partneri));
		  }
		  
		  /**
		   * Pronalazi partnera
		   * @param id
		   * @return
		   */
		  public Partneri find(Object id) {
		    return getEntityManager().find(Partneri.class, id);
		  }
		  /**
		   * Pronalazi sve partnere
		   * @return lista partnera
		   */
		  public List<Partneri> findAll() {
			    CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
			    cq.select(cq.from(Partneri.class));
			    return getEntityManager().createQuery(cq).getResultList();
			  }
		  /**
		   * Pretvara listu entiteta u listu zapisa
		   * @param partneriE - entiteti
		   * @return lsita zapisa Partner
		   */
		  public List<Partner> pretvori(List<Partneri> partneriE) {
			    List<Partner> partneri = new ArrayList<>();
			    for (Partneri partnerE : partneriE) {
			      var pObjekt = this.pretvori(partnerE);

			      partneri.add(pObjekt);
			    }

			    return partneri;
			  }
		  /**
		   * Pretvara entitet u zapis
		   * @param p - entitet
		   * @return zapis
		   */
		  public Partner pretvori(Partneri p) {
			    if (p == null) {
			      return null;
			    }
			    var pObjekt =
			        new Partner(p.getId(),
			        		p.getNaziv(),
			        		p.getVrstakuhinje(),
			        		p.getAdresa(), 
			        		p.getMreznavrata(), 
			        		p.getMreznavratakraj(), 
			        		(float)p.getGpssirina(),
			        		(float)p.getGpsduzina(), 
			        		p.getSigurnosnikod(),
			        		p.getAdminkod()
			        		);

			    return pObjekt.partnerBezKodova();
			  }

}
