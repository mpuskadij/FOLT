package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * @author Marin Puškadija
 * Klasa za rad s "obracuni" tablicom
 */
public class ObracunDAO {
	/**
	 * Aktivna veza na bazu ili h2 ili hsql
	 */
	private Connection vezaBP;
	
	/**
	 * Konstruktor
	 * @param vezaBP - preslikava konekciju
	 */
	public ObracunDAO(Connection vezaBP) {
		super();
		this.vezaBP = vezaBP;
	}
	
	/**
	 * Dohvaća sve obračune
	 * @return lista svih obračuna
	 */
	public List<Obracun> dohvatiSve() {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni";
		return this.dohvatiObracune(upit);

	}
	/**
	 * Dohvaća sve obračune do nekog vremena
	 * @param vrijemeDo - filter
	 * @return lista svih obračuna do nekog vremena
	 */
	public List<Obracun> dohvatiDo(long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme <= ?";

		return this.dohvatiObracune(upit, new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća sve obračune od nekog vremena
	 * @param vrijemeOd - filter
	 * @return listu obračuna od nekog vremena
	 */
	public List<Obracun> dohvatiOd(long vrijemeOd) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme >= ?";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd));
	}
	/**
	 * Dohvaća obračune od do vremena
	 * @param vrijemeOd - filter
	 * @param vrijemeDo - filter
	 * @return listu obračuna unutar nekog intervala
	 */
	public List<Obracun> dohvatiOdDo(long vrijemeOd, long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme BETWEEN ? AND ?";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća sve obračune kojima je jelo == true
	 * @return listu obračuna s jelo == true
	 */
	public List<Obracun> dohvatiSveJelo() {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE jelo=true";
		return this.dohvatiObracune(upit);

	}
	
	/**
	 * Dohvaća jelo obračune do nekog vremena
	 * @param vrijemeDo - filter
	 * @return listu obračuna gdje je jelo == true do nekog vremena
	 */
	public List<Obracun> dohvatiDoJelo(long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme <= ? AND jelo=true";

		return this.dohvatiObracune(upit, new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća jelo obračune od nekog vremena
	 * @param vrijemeOd - filter
	 * @return listu obračuna gdje je jelo == true od nekog vremena
	 */
	public List<Obracun> dohvatiOdJelo(long vrijemeOd) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme >= ? AND jelo=true";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd));
	}
	
	/**
	 * Dohvaća jelo obračune unutar intervala
	 * @param vrijemeOd - filter
	 * @param vrijemeDo - filter
	 * @return list obračuna unutar nekog intervala
	 */
	public List<Obracun> dohvatiOdDoJelo(long vrijemeOd, long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme BETWEEN ? AND ? AND jelo=true";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća sve obračune kojima je jelo == false
	 * @return list obračuna s jelo == false
	 */
	public List<Obracun> dohvatiSvePice() {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE jelo=false";
		return this.dohvatiObracune(upit);

	}
	
	/**
	 * Dohvaća piće obračune do nekog vremena
	 * @param vrijemeDo - filter
	 * @return list obračuna piće do nekog vremena
	 */
	public List<Obracun> dohvatiDoPice(long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme <= ? AND jelo=false";

		return this.dohvatiObracune(upit, new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća piće obračune od nekog vremena
	 * @param vrijemeOd - filter
	 * @return listu obračuna do nekog vremena
	 */
	public List<Obracun> dohvatiOdPice(long vrijemeOd) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme >= ? AND jelo=false";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd));
	}
	
	/**
	 * Dohvaća piće obračune unutar intervala
	 * @param vrijemeOd - filter
	 * @param vrijemeDo - filter
	 * @return listu obračuna unutar nekog intervala
	 */
	public List<Obracun> dohvatiOdDoPice(long vrijemeOd, long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE vrijeme BETWEEN ? AND ? AND jelo=false";
		return this.dohvatiObracune(upit, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo));
	}
	
	/**
	 * Vraća sve obračune nekog partnera
	 * @param id - id partnera
	 * @return listu obračuna nekog partnera
	 */
	public List<Obracun> dohvatiSve(int id) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE partner = ?";
		return this.dohvatiObracune(id, upit);

	}
	
	/**
	 *Dohvaća obračune nekog partnera do nekog vremena
	 * @param id - id partnera
	 * @param vrijemeDo - filter
	 * @return listu obračuna nekog partnera do nekog vremena
	 */
	public List<Obracun> dohvatiDo(int id, long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE partner = ? AND vrijeme <= ?";

		return this.dohvatiObracune(id, upit, new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dohvaća listu obračuna nekog partnera od nekog vremena
	 * @param id - id partnera
	 * @param vrijemeOd - filter
	 * @return listu obračuna partnera od nekog vremena
	 */
	public List<Obracun> dohvatiOd(int id, long vrijemeOd) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE partner = ? AND vrijeme >= ?";
		return this.dohvatiObracune(id, upit, new Timestamp(vrijemeOd));
	}
	
	/**
	 * Dohvaća listu obračuna nekog partnera unutar intervala
	 * @param id - id partnera
	 * @param vrijemeOd - filter
	 * @param vrijemeDo - filter
	 * @return listu obračuna partnera unutar intervala
	 */
	public List<Obracun> dohvatiOdDo(int id, long vrijemeOd, long vrijemeDo) {
		String upit = "SELECT id, jelo, kolicina, cijena, vrijeme, partner FROM obracuni WHERE partner = ? AND vrijeme BETWEEN ? AND ?";
		return this.dohvatiObracune(id, upit, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo));
	}
	
	/**
	 * Dodaje obračune u bazu podataka
	 * @param obracuni - lista obračuna
	 * @return true ako su svi dodani uspešno, false ako barem jedan je neuspješan
	 */
	public boolean dodaj(List<Obracun> obracuni) {
	    String upit = "INSERT INTO obracuni (id, jelo, kolicina, cijena, vrijeme, partner) "
	        + "VALUES (?,?,?,?,?,?)";

	    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
	    	for(var o : obracuni) {
	    	  s.setString(1, o.id());
	  	      s.setBoolean(2,o.jelo());
	  	      s.setFloat(3, o.kolicina());
	  	      s.setFloat(4, o.cijena());
	  	      s.setTimestamp(5, new Timestamp(o.vrijeme()));
	  	      s.setInt(6, o.partner());


	  	      int brojAzuriranja = s.executeUpdate();
	  	      if (brojAzuriranja != 1) return false;
	    	}

	      return true;

	    } catch (Exception ex) {
	      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    return false;
	  }
	
	/**
	 * Izvršava upit i pretvara retke u zapise {@link Obracun}
	 * @param upit - sql upit
	 * @param vremena - niz filtera
	 * @return listu obracuna koji su zadovoljili upit
	 */
	private List<Obracun> dohvatiObracune(String upit, Timestamp... vremena) {
		var obracuni = new ArrayList<Obracun>();

		try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
			for (int i = 0; i < vremena.length; i++) {
				s.setTimestamp(i + 1, vremena[i]);
			}

			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int partner = rs.getInt("partner");
				String id = rs.getString("id");
				boolean jelo = rs.getBoolean("jelo");
				float kolicina = rs.getFloat("kolicina");
				float cijena = rs.getFloat("cijena");
				long vrijeme = rs.getTimestamp("vrijeme").getTime();

				Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
				obracuni.add(obracun);
			}

		} catch (SQLException ex) {
			Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
		}

		return obracuni;

	}
	
	/**
	 * Izvršava upit i pretvara retke u zapise {@link Obracun}
	 * @param upit - sql upit
	 * @param partnerID - id partnera
	 * @param vremena - niz filtera
	 * @return listu obracuna koji su zadovoljili upit
	 */
	private List<Obracun> dohvatiObracune(int partnerID, String upit, Timestamp... vremena) {
		var obracuni = new ArrayList<Obracun>();

		try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
			s.setInt(1, partnerID);
			for (int i = 0; i < vremena.length; i++) {
				s.setTimestamp(i + 2, vremena[i]);
			}

			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int partner = rs.getInt("partner");
				String id = rs.getString("id");
				boolean jelo = rs.getBoolean("jelo");
				float kolicina = rs.getFloat("kolicina");
				float cijena = rs.getFloat("cijena");
				long vrijeme = rs.getTimestamp("vrijeme").getTime();

				Obracun obracun = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
				obracuni.add(obracun);
			}

		} catch (SQLException ex) {
			Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
		}

		return obracuni;

	}
}
