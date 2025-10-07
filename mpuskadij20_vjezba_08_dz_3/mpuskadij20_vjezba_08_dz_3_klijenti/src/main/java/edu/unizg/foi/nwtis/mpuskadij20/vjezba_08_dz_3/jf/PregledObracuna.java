package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import edu.unizg.foi.nwtis.podaci.Obracun;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Zrno z pregled obračuna
 */
@ViewScoped
@Named("pregledObracuna")
public class PregledObracuna implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Facade za dohvat obračuna
	 */
	@Inject
	private ObracuniFacade obracuniFacade;
	
	/**
	 * Vrijeme od
	 */
	private Date vrijemeOd;
	/**
	 * Vrijeme do
	 */
	private Date vrijemeDo;
	
	/**
	 * ID odabranog partnera za dohvatiti obračune od
	 */
	private int idOdabranogPartnera;
	
	/**
	 * Poruka greške
	 */
	private String poruka = "";
	
	/**
	 * Lista obračuna
	 */
	private List<Obracun> obracuni = new ArrayList<Obracun>();
	
	
	
	/**
	 * Getter za {@link #vrijemeOd}
	 * @return vrijeme od
	 */
	public Date getVrijemeOd() {
		return vrijemeOd;
	}



	/**
	 * Setter za {@link #vrijemeOd}
	 * @param vrijemeOd - novo vrijeme od
	 */
	public void setVrijemeOd(Date vrijemeOd) {
		this.vrijemeOd = vrijemeOd;
	}


	/**
	 * Getter za {@link #vrijemeDo}
	 * @return vrijemeDo
	 */
	public Date getVrijemeDo() {
		return vrijemeDo;
	}



	/**
	 * Setter za {@link #vrijemeDo}
	 * @param vrijemeDo
	 */
	public void setVrijemeDo(Date vrijemeDo) {
		this.vrijemeDo = vrijemeDo;
	}

	/**
	 * Getter za {@link #idOdabranogPartnera}
	 * @return id odabranog partnera
	 */
	public int getIdOdabranogPartnera() {
		return idOdabranogPartnera;
	}


	
/**
 * Setter za {@link #idOdabranogPartnera}
 * @param idOdabranogPartnera - novi id
 */
	public void setIdOdabranogPartnera(int idOdabranogPartnera) {
		this.idOdabranogPartnera = idOdabranogPartnera;
	}



	/**
	 * Getter za {@link #poruka}
	 * @return poruka
	 */
	public String getPoruka() {
		return poruka;
	}



	/**
	 * Setter za {@link #poruka}
	 * @param poruka - nova poruka
	 */
	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}



	/**
	 * Getter za {@link #obracuni}
	 * @return lista obračuna
	 */
	public List<Obracun> getObracuni() {
		return obracuni;
	}



	/**
	 * Setter za {@link #obracuni}
	 * @param obracuni - novi obračuni
	 */
	public void setObracuni(List<Obracun> obracuni) {
		this.obracuni = obracuni;
	}



	/**
	 * Pretražuje obračune iz baze prema kriterijima
	 */
	public void pretrazi() {
		var obracuniE = this.obracuniFacade.findAll(vrijemeOd, vrijemeDo, this.idOdabranogPartnera == -1 ? null : this.idOdabranogPartnera );
		this.obracuni = obracuniFacade.pretvori(obracuniE);
		
		
	}
	
	/**
	 * Pretvara milisekunde u Date
	 * @param vrijeme - milisekunde
	 * @return Date
	 */
	public Date pretvoriUDatum(long vrijeme) {
		return new Date(vrijeme);
	}
	
	

}
