package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * Klasa za partnera
 */
public class PosluziteljPartner {

	/** Konfiguracijski podaci */
	private Konfiguracija konfig;

	/**
	 * Konfiguracijski podatak na kojoj adresi se izvršava poslužitelj tvrtka
	 */
	private String adresaPosluzitelja = "";

	/**
	 * Konfiguracijski podatak naziv partnera pod kojim se registrirati
	 */
	private String naziv = "";

	/**
	 * Konfiguracijski podatak na kojem portu se izvršava poslužitelj za
	 * registraciju partnera
	 */
	private int mreznaVrataRegistracija = 0;

	/**
	 * Konfiguracijski podatak na kojem portu se izvršava poslužitelj za kraj rada
	 */
	private int mreznaVrataKraj = 0;

	/**
	 * Konfiguracijski podatak na kojem portu se izvršava poslužitelj za rad s
	 * partnerima
	 */
	private int mreznaVrataRad = 0;

	/**
	 * Konfiguracijski podatak pod kojim ID-om je partner registriran ili će biti
	 * registriran
	 */
	private int id = 0;

	/**
	 * Konfiguracijski podatak pod koju kuhinje da se partner registrira
	 */
	private String vrstaKuhinje = "";

	/**
	 * Konfiguracijski podatak za registraciju partnera
	 */
	private float gpsSirina = 0;

	/**
	 * Konfiguracijski podatak za registraciju partnera
	 */
	private float gpsDuzina = 0;

	/**
	 * koji odgovor od poslužitelja je smatran OK
	 */
	private final String okOdgovor = "OK";

	/**
	 * Konfiguracijski podatak, koji je kod za kraj rada tvrtke
	 */
	private String kodZaKraj = "";

	/**
	 * Kod za dobivanje status poslužitelja, spavanje, stavljanje i vraćanje iz
	 * pauze
	 */
	private String kodZaAdminPartnera = "";

	/**
	 * Konfiguracijski podatak, koji je sigurnosni kod partnera kad se registrira
	 */
	private String sigurnosniKod = "";

	/**
	 * Vrata na kojima se poslužitelj za prijem zahtjeva kupaca pokrene
	 */
	private int mreznaVrata = 0;
	
	/**
	 * Mrežna vrata na kojima se pokreće poslužitelj za kraj rada partnera
	 */
	private int mreznaVrataKrajPartner = 0;

	/**
	 * Koliko da dretva spava nakon prihvata zahtjeva na {@link #mreznaVrata}
	 */
	private int pauzaDretve = 0;

	/**
	 * Nakon koliko narudžbi se šalju obračuni kod komande OBRAČUN
	 */
	private int kvotaNarudzbi = 0;

	/**
	 * Kolekcija stavki jelovnika partnera
	 */
	private Map<String, Jelovnik> jelovnici = new ConcurrentHashMap<String, Jelovnik>();

	/**
	 * Kolekcija stavki akrte pića partnera
	 */
	private Map<String, KartaPica> kartePica = new ConcurrentHashMap<String, KartaPica>();

	/**
	 * Kolekcija otvorenih narudžbi svakog kupca
	 */
	private Map<String, Queue<Narudzba>> narudzbe = new ConcurrentHashMap<String, Queue<Narudzba>>();

	/**
	 * Kolekcija plaćenih narudžbi
	 */
	private Queue<Queue<Narudzba>> placeneNarudzbe = new ConcurrentLinkedQueue<Queue<Narudzba>>();

	/**
	 * Kolekcija virtualnih dretvi i portova na kojima rade
	 */
	private Map<Future<?>, Integer> dretveZaObraduZahtjeva = new ConcurrentHashMap<Future<?>, Integer>();

	/**
	 * Konfiguracijski podatak, koliko čekača može biti na utičnici
	 * {@link #mreznaVrata}
	 */
	private int brojCekaca = 0;

	/**
	 * Stvara virtualne dretve
	 */
	private ExecutorService executor = null;

	/** Predložak za kraj */
	private Pattern predlozakKraj = Pattern.compile("^KRAJ$");

	/**
	 * Predložak za PARTNER
	 * 
	 */
	private Pattern predlozakPartner = Pattern.compile("^PARTNER$");

	/**
	 * Brava za zaključavanje kritičnih dijelova koda
	 */
	private ReentrantLock brava = new ReentrantLock();
	
	/**
	 * Predstavlja koji dio poslužitelja predstavlja dio za kupce
	 */
	private final int dioKupci = 1;
	
	/**
	 * Predstavlja koji broj znači da je dio poslužitelja "aktivan"
	 */

	private final int aktivanStatus = 1;
	
	/**
	 * Predstavlja koji broj znači da je dio poslužitelja "pauziran"
	 */

	private final int pauzaStatus = 0;
	
	/**
	 * Kolekcija svih dijelova poslužitelja partner koji su odmah u statusu {@link #aktivanStatus}
	 */
	private final Map<Integer, Integer> dijeloviPosluzitelja = new ConcurrentHashMap<Integer, Integer>(
			Map.of(dioKupci, aktivanStatus));

	private final AtomicBoolean zastavicaKrajRada = new AtomicBoolean(false);

	/**
	 * Zatvara sve virtualne dretve programa
	 * 
	 * @return broj prisilno prekinutih dretvi
	 */
	public int prekiniSveDretve() {
		var brojZatvorenihDretvi = 0;
		for (Future<?> dretva : this.dretveZaObraduZahtjeva.keySet()) {
			if (!dretva.isDone()) {
				dretva.cancel(true);
				brojZatvorenihDretvi++;
				var port = this.dretveZaObraduZahtjeva.get(dretva);
				System.out.println("Broj prisilno zatvorenih veza na utičnici " + port + ": 1");
			}
		}
		return brojZatvorenihDretvi;
	}

	/**
	 * Hook za hvatanje zatvaranja programa
	 */
	public void zakaciPrisilnoZaustavljanje() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			int brojZatvorenihDretvi = this.prekiniSveDretve();
			System.out.println("Broj prisilno zatvorenih virtualnih dretvi: " + brojZatvorenihDretvi);
			this.executor.shutdownNow();
		}));
	}

	/**
	 * Validira konfiguracijske podatke potrebne za registraciju partnera
	 * 
	 * @return true ako se može partner registrirati, false inače
	 */
	public boolean validirajKonfiguracijskePodatkeZaRegistraciju() {
		try {
			this.adresaPosluzitelja = this.konfig.dajPostavkuOsnovno("adresa", "");

			this.mreznaVrataRegistracija = Integer
					.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrataRegistracija", ""));
			this.id = Integer.parseInt(this.konfig.dajPostavkuOsnovno("id", ""));
			this.naziv = this.konfig.dajPostavkuOsnovno("naziv", "");
			this.vrstaKuhinje = this.konfig.dajPostavkuOsnovno("kuhinja", "");
			this.gpsSirina = Float.parseFloat(this.konfig.dajPostavkuOsnovno("gpsSirina", ""));
			this.gpsDuzina = Float.parseFloat(this.konfig.dajPostavkuOsnovno("gpsDuzina", ""));
			this.mreznaVrata = Integer.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrata", ""));
			this.mreznaVrataKrajPartner = Integer
					.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrataKrajPartner", ""));
			this.kodZaAdminPartnera = this.konfig.dajPostavkuOsnovno("kodZaAdmin", "");

			if (this.adresaPosluzitelja.isBlank() || this.naziv.isBlank() || this.vrstaKuhinje.isBlank()
					|| this.kodZaAdminPartnera.isBlank()) {
				return false;
			}

			return true;
		} catch (NumberFormatException greska) {
			return false;
		}
	}

	/**
	 * Šalje komandu PARTNER na poslužitelja za registraciju partnera, sprema
	 * sigurnosni kod u konfiguraciju
	 */
	public void registrirajSe() {
		if (this.validirajKonfiguracijskePodatkeZaRegistraciju()) {
			var komandaRegistracije = "PARTNER";
			try (var mreznaUticnica = new Socket(this.adresaPosluzitelja, this.mreznaVrataRegistracija);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
							true)) {
				var komanda = komandaRegistracije + " " + this.id + " \"" + this.naziv + "\" " + this.vrstaKuhinje + " "
						+ mreznaUticnica.getLocalAddress().getHostAddress() + " " + this.mreznaVrata + " "
						+ this.mreznaVrataKrajPartner + " " + this.gpsSirina + " " + this.gpsDuzina + " "
						+ this.kodZaAdminPartnera;
				out.println(komanda);
				var dobivenOdgovor = in.readLine();
				if (dobivenOdgovor.startsWith(this.okOdgovor)) {
					var sigurnosniKod = dobivenOdgovor.split(" ")[1];
					if (this.konfig.postojiPostavka("sigKod")) {
						this.konfig.azurirajPostavku("sigKod", sigurnosniKod);
					} else {
						this.konfig.spremiPostavku("sigKod", sigurnosniKod);
					}

					this.konfig.spremiKonfiguraciju();

				}
				mreznaUticnica.shutdownInput();
				mreznaUticnica.shutdownOutput();
				mreznaUticnica.close();

			} catch (UnknownHostException e) {
			} catch (IOException e) {
			} catch (IllegalArgumentException e) {
			} catch (NeispravnaKonfiguracija e) {
			}

		}

	}

	/**
	 * Pretvara JSON u objekte zapisa {@link Jelovnik} i stavlja ih u
	 * {@link #jelovnici}
	 * 
	 * @param jelovnici - JSON string jelovnika
	 * @return true ako su jelovnici uspješno učitani, false inače
	 */
	public boolean ucitajJelovnike(String jelovnici) {

		try {
			var gson = new Gson();
			var jelovniciPartnera = gson.fromJson(jelovnici, Jelovnik[].class);
			for (var jelovnik : jelovniciPartnera) {
				if (jelovnik.id() != null) {
					this.jelovnici.put(jelovnik.id(), jelovnik);
				}
			}
			return true;
		} catch (JsonSyntaxException e) {
			return false;
		}

	}

	/**
	 * Validira konfiguracijske podatke potrebne za pokretanje poslužitelja za
	 * prijam zahtjeva kupaca *
	 * 
	 * @return true ako se može poslužitelj pokrenuti, false inače
	 */
	public boolean validirajKonfiguracijskePodatkeZaPosluzitelja() {
		try {
			this.adresaPosluzitelja = this.konfig.dajPostavkuOsnovno("adresa", "");
			this.kodZaKraj = this.konfig.dajPostavkuOsnovno("kodZaKraj", "");
			this.kodZaAdminPartnera = this.konfig.dajPostavkuOsnovno("kodZaAdmin", "");
			this.mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrataRad", ""));
			this.id = Integer.parseInt(this.konfig.dajPostavkuOsnovno("id", ""));
			this.sigurnosniKod = this.konfig.dajPostavkuOsnovno("sigKod", "");
			this.brojCekaca = Integer.parseInt(this.konfig.dajPostavkuOsnovno("brojCekaca", ""));
			this.mreznaVrata = Integer.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrata", ""));
			this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavkuOsnovno("pauzaDretve", ""));
			this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavkuOsnovno("kvotaNarudzbi", ""));
			this.mreznaVrataKrajPartner = Integer
					.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrataKrajPartner", ""));

			if (this.adresaPosluzitelja.isBlank() || this.sigurnosniKod.isBlank() || this.kodZaAdminPartnera.isBlank()
					|| this.kodZaKraj.isBlank()) {
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}

	}

	/**
	 * Dohvaća kartu pića partnera od poslužitelja za rad s partnerima
	 * 
	 * @return true ako su uspješno učitanu u {@link #kartePica}, false inače
	 */
	public boolean dohvatiKartePica() {
		this.kartePica.clear();
		var komandaZaDohvatKartePica = "KARTAPIĆA";
		try (var mreznaUticnica = new Socket(this.adresaPosluzitelja, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true)) {
			var komanda = komandaZaDohvatKartePica + " " + String.valueOf(this.id) + " " + this.sigurnosniKod;
			out.println(komanda);
			var dobivenOdgovor = in.readLine();
			if (dobivenOdgovor.startsWith(this.okOdgovor)) {

				var kartePica = in.readLine();

				mreznaUticnica.shutdownInput();
				mreznaUticnica.shutdownOutput();
				mreznaUticnica.close();

				if (!this.ucitajKartePica(kartePica)) {
					return false;

				}
			}
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	/**
	 * Pretvara JSON u objekte zapisa {@link KartaPica}
	 * 
	 * @param kartePicaJSON - JSON string
	 * @return true ako su učitani, false inače
	 */
	private boolean ucitajKartePica(String kartePicaJSON) {
		try {
			var gson = new Gson();
			var kartaPicaObjekti = gson.fromJson(kartePicaJSON, KartaPica[].class);
			for (var kp : kartaPicaObjekti) {
				if (kp.id() != null) {
					this.kartePica.put(kp.id(), kp);
				}
			}
			return true;
		} catch (JsonSyntaxException e) {
			return false;
		}
	}

	/**
	 * Dohvaća jelovnike od poslužitelja za rad s partnerima
	 * 
	 * @return true ako su uspješno dohvaćeni, false inače
	 */
	public boolean dohvatiJelovnike() {
		this.jelovnici.clear();
		try (var mreznaUticnica = new Socket(this.adresaPosluzitelja, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true)) {
			var komandaZaDohvatJelovnika = "JELOVNIK";
			var komanda = komandaZaDohvatJelovnika + " " + String.valueOf(this.id) + " " + this.sigurnosniKod;
			out.println(komanda);
			var dobivenOdgovor = in.readLine();
			if (dobivenOdgovor.startsWith(this.okOdgovor)) {

				var jelovnici = in.readLine();

				mreznaUticnica.shutdownInput();
				mreznaUticnica.shutdownOutput();
				mreznaUticnica.close();

				if (!this.ucitajJelovnike(jelovnici)) {
					return false;

				}

			}
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;

		} catch (IllegalArgumentException e) {
			return false;

		}
		return true;
	}

	/**
	 * Pokreće poslužitelja na utičnici {@link #mreznaVrata} te za svaki zahtjev
	 * kreira novu virtualnu dretvu koristeći {@link #executor}
	 */
	public void pokreniPosluziteljaZaPrijemZahtjevaKupaca() {
		if (this.validirajKonfiguracijskePodatkeZaPosluzitelja() && this.dohvatiJelovnike()
				&& this.dohvatiKartePica()) {
			this.zakaciPrisilnoZaustavljanje();
			var builder = Thread.ofVirtual();
			var factory = builder.factory();
			this.executor = Executors.newThreadPerTaskExecutor(factory);

			var dretvaKrajRada = this.executor.submit(() -> this.pokreniPosluziteljaZaKrajRada());
			this.dretveZaObraduZahtjeva.put(dretvaKrajRada, this.mreznaVrataKrajPartner);

			var dretvaPrijemZahtjeva = this.executor.submit(() -> {
				try (ServerSocket ss = new ServerSocket(this.mreznaVrata, this.brojCekaca)) {

					while (!this.zastavicaKrajRada.get()) {
						try {
							Thread.sleep(this.pauzaDretve);
						} catch (InterruptedException ex) {
						}
						var mreznaUticnica = ss.accept();
						var dretva = this.executor.submit(() -> this.obradiZahtjevKupca(mreznaUticnica));
						this.dretveZaObraduZahtjeva.put(dretva, mreznaUticnica.getLocalPort());

					}

				} catch (IOException greska) {

				}
			});
			this.dretveZaObraduZahtjeva.put(dretvaPrijemZahtjeva, this.mreznaVrata);

			while (!this.zastavicaKrajRada.get()) {
				try {
					Thread.sleep(this.pauzaDretve);
				} catch (InterruptedException e) {
				}
			}

		} else {
			System.out.println("Partner nije uspio dohvatiti jelovnik i/ili kartu pića od tvrtke!");
		}
	}
	
	/**
	 * Pokreće poslužitelja za kraj rada na mrežnim vratima {@link #mreznaVrataKrajPartner}
	 */
	public void pokreniPosluziteljaZaKrajRada() {
		try (ServerSocket ss = new ServerSocket(this.mreznaVrataKrajPartner, this.brojCekaca)) {

			while (!this.zastavicaKrajRada.get()) {
				try {
					Thread.sleep(this.pauzaDretve);
				} catch (InterruptedException ex) {
				}
				var uticnica = ss.accept();
				this.obradiZahtjevZaKrajRada(uticnica);
			}

		} catch (IOException e) {

		}
	}
	
	/**
	 * Obrađuje komande KRAJ, OSVJEŽI, STATUS, START, PAUZA, SPAVA
	 * @param mreznaUticnica - utičnica od kud komanda dolazi
	 */
	public void obradiZahtjevZaKrajRada(Socket mreznaUticnica) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true)) {
			var neispravanFormatIliKodPoruka = "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj";
			var neispravanKodAdminPartneraPoruka = "ERROR 61 – Pogrešan kodZaAdminPartnera";
			var neuspjesnoPreuzimanjeJelovnikaPoruka = "ERROR 46 - Neuspješno preuzimanje jelovnika";
			var neuspjesnoPreuzimanjeKartePicaPoruka = "ERROR 47 - Neuspješno preuzimanje karte pića";
			var neispravanDioPosluzitelja = "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj";

			var prekidSpavanjaDretvePoruka = "ERROR 63 – Prekid spavanja dretve";
			var pogresnaPromjenaStartIliPauzaPoruka = "ERROR 62 – Pogrešna promjena pauze ili starta";

			var komanda = in.readLine();

			if (komanda.startsWith("KRAJ")) {
				obradiKomanduKRAJ(out, neispravanFormatIliKodPoruka, komanda);

			} else if (komanda.startsWith("OSVJEŽI")) {
				obradiKomanduOSVJEZI(out, neispravanFormatIliKodPoruka, neispravanKodAdminPartneraPoruka,
						neuspjesnoPreuzimanjeJelovnikaPoruka, neuspjesnoPreuzimanjeKartePicaPoruka, komanda);
			}

			else if (komanda.startsWith("STATUS")) {
				obradiKomanduSTATUS(out, neispravanFormatIliKodPoruka, neispravanDioPosluzitelja,
						neispravanKodAdminPartneraPoruka, komanda);
			} else if (komanda.startsWith("PAUZA")) {
				obradiKomanduPAUZAiliSTART(out, neispravanFormatIliKodPoruka, neispravanDioPosluzitelja,
						neispravanKodAdminPartneraPoruka, pogresnaPromjenaStartIliPauzaPoruka, komanda, pauzaStatus);

			} else if (komanda.startsWith("START")) {
				obradiKomanduPAUZAiliSTART(out, neispravanFormatIliKodPoruka, neispravanDioPosluzitelja,
						neispravanKodAdminPartneraPoruka,pogresnaPromjenaStartIliPauzaPoruka, komanda, aktivanStatus);

			} else if (komanda.startsWith("SPAVA")) {
				obradiKomanduSPAVA(out, neispravanFormatIliKodPoruka, prekidSpavanjaDretvePoruka,neispravanKodAdminPartneraPoruka, komanda);
			}

			else {
				out.println(neispravanFormatIliKodPoruka);
			}
		} catch (IOException e) {
		} finally {
			if (!mreznaUticnica.isClosed()) {
				try {
					mreznaUticnica.shutdownInput();
					mreznaUticnica.shutdownOutput();
					mreznaUticnica.close();
				} catch (IOException ex) {
				}
			}
		}

	}
	/**
	 * Postavlja {@link #zastavicaKrajRada} na true i završava se poslužitelj
	 * @param out - pisač
	 * @param neispravanFormatIliKodPoruka - ako je format neispravan ili kod za kraj
	 * @param komanda - sama komanda dobivena na utičnici
	 */
	private void obradiKomanduKRAJ(PrintWriter out, String neispravanFormatIliKodPoruka,
			String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var kod = dijeloviKomande[1];
			if (this.kodZaKraj.equals(kod)) {
				this.zastavicaKrajRada.set(true);
				out.println("OK");
			} else {
				out.println(neispravanFormatIliKodPoruka);
			}
		} else {
			out.println(neispravanFormatIliKodPoruka);
		}
	}

	/**
	 * Svaka virtualna dretva obrađuje ovu metodu, određuje o kojoj komandi je riječ
	 * 
	 * @param mreznaUticnica - utičnica na kojoj čeka zahtjev klijenta
	 */
	public void obradiZahtjevKupca(Socket mreznaUticnica) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true)) {
			var neispravanFormatPoruka = "ERROR 40 - Format komande nije ispravan";

			var narudbzaPostojiPoruka = "ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca";
			var narudzbaNePostojiPoruka = "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca";
			var nePostojiJeloPoruka = "ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera";
			var nePostojiPicePoruka = "ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera";
			var neuspjesnoSlanjeObracuna = "ERROR 45 - Neuspješno slanje obračuna";
			var pauzaPoruka = "ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi";

			var neispravnaKolicinaPoruka = "ERROR 49 - Količina ne može biti <= 0";
			
			
			if(this.dijeloviPosluzitelja.get(this.dioKupci) != this.aktivanStatus) {
				out.println(pauzaPoruka);
				return;
			}

			var komanda = in.readLine();
			if (komanda.startsWith("JELOVNIK")) {
				obradiKomanduJELOVNIK(out, neispravanFormatPoruka, komanda);
			} else if (komanda.startsWith("KARTAPIĆA")) {
				obradiKomanduKARTAPICA(out, neispravanFormatPoruka, komanda);
			}

			else if (komanda.startsWith("NARUDŽBA")) {
				obradiKomanduNARUDZBA(out, neispravanFormatPoruka, narudbzaPostojiPoruka, komanda);
			} else if (komanda.startsWith("JELO")) {
				obradiKomanduJELO(out, neispravanFormatPoruka, narudzbaNePostojiPoruka, nePostojiJeloPoruka,
						neispravnaKolicinaPoruka, komanda);
			} else if (komanda.startsWith("PIĆE")) {
				obradiKomanduPICE(out, neispravanFormatPoruka, narudzbaNePostojiPoruka, nePostojiPicePoruka,
						neispravnaKolicinaPoruka, komanda);
			}

			else if (komanda.startsWith("RAČUN")) {
				obradiKomanduRACUN(out, neispravanFormatPoruka, narudzbaNePostojiPoruka, neuspjesnoSlanjeObracuna,
						komanda);
			}
			else if (komanda.startsWith("STANJE")) {
				obradiKomanduSTANJE(out, neispravanFormatPoruka, narudzbaNePostojiPoruka, komanda);
			}

			else {
				out.println(neispravanFormatPoruka);
			}

		} catch (JsonIOException ex) {
		} catch (IOException ex) {
		} finally {
			if (!mreznaUticnica.isClosed()) {
				try {
					mreznaUticnica.shutdownInput();
					mreznaUticnica.shutdownOutput();
					mreznaUticnica.close();
				} catch (IOException ex) {
				}
			}
		}
		return;
	}
	
	/**
	 * Obrađuje komandu STANJE
	 * @param out - pisač
	 * @param neispravanFormatPoruka - ako je format komande neispravan
	 * @param narudzbaNePostojiPoruka - ako ne postoji otvorena narudžba
	 * @param komanda - sama komanda od utičnice
	 */
	private void obradiKomanduSTANJE(PrintWriter out, String neispravanFormatPoruka, String narudzbaNePostojiPoruka,
			String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var korisnik = dijeloviKomande[1];
			if (this.narudzbe.containsKey(korisnik)) {
				var korisnikoveNarudzbe = this.narudzbe.get(korisnik);
				var gson = new Gson();
				out.println("OK");
				gson.toJson(korisnikoveNarudzbe,out);
			}
			else {
				out.println(narudzbaNePostojiPoruka);
			}
			
		}
		else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu SPAVA
	 * @param out - pisač
	 * @param neispravanFormatPoruka - ako je format SPAVA komande neispravan
	 * @param prekidSpavanjaDretvePoruka - ako je dretva prekinuta usred spavanja
	 * @param neispravanKodAdminPartneraPoruka - ako je kod za admin partnera neispravan
	 * @param komanda - sama komanda
	 */
	private void obradiKomanduSPAVA(PrintWriter out, String neispravanFormatPoruka,String prekidSpavanjaDretvePoruka,String neispravanKodAdminPartneraPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var adminKod = dijeloviKomande[1];
			if (this.kodZaAdminPartnera.equals(adminKod)) {
				try {
					var vrijeme = Long.parseLong(dijeloviKomande[2]);
					Thread.sleep(vrijeme);
					out.println("OK");
				} catch (NumberFormatException ex) {
					out.println(neispravanFormatPoruka);
				} catch (InterruptedException e) {
					out.println(prekidSpavanjaDretvePoruka);
				}
			} else {
				out.println(neispravanKodAdminPartneraPoruka);
			}
			

		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu PAUZA ili START, ovisi koji je status poslan
	 * @param out - pisač
	 * @param neispravanFormatPoruka - ako je format neispravan
	 * @param neispravanDioPosluzitelja - ako se pokuša neispravan dio poslužitelja pauzirati/pokrenuti
	 * @param neispravanKodAdminPartneraPoruka - ako je neispravan admin kod partnera
	 * @param pogresnaPromjenaStartIliPauzaPoruka - ako je dio već u pauzi/aktivnom radu
	 * @param komanda  -sama komandu
	 * @param noviStatus - novi status za postaviti
	 */
	private void obradiKomanduPAUZAiliSTART(PrintWriter out, String neispravanFormatPoruka,
			String neispravanDioPosluzitelja, String neispravanKodAdminPartneraPoruka, String pogresnaPromjenaStartIliPauzaPoruka,String komanda, int noviStatus) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminPartnera.equals(kod)) {
				try {
					var dioPosluzitelja = Integer.parseInt(dijeloviKomande[2]);
					if (dijeloviPosluzitelja.containsKey(dioPosluzitelja)) {
						if (dijeloviPosluzitelja.get(dioPosluzitelja) != noviStatus) {
							dijeloviPosluzitelja.put(dioPosluzitelja, noviStatus);
							out.println("OK");
						}
						else {
							out.println(pogresnaPromjenaStartIliPauzaPoruka);
						}
						
					} else {
						out.println(neispravanDioPosluzitelja);
					}
				} catch (NumberFormatException ex) {

				}

			} else {
				out.println(neispravanKodAdminPartneraPoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu OSVJEŽI
	 * @param out - pisač
	 * @param neispravanFormatPoruka - ako je format neispravan
	 * @param neispravanKodAdminPartneraPoruka - ako je admin kod partnera neispravan
	 * @param neuspjesnoPreuzimanjeJelovnikaPoruka - ako je neuspješno preuzimanje jelovnika
	 * @param neuspjesnoPreuzimanjeKartePicaPoruka - ako je neuspješno preuzimanje karte pića
	 * @param pauzaPoruka - ako je partner za prijem u pauzi
	 * @param komanda - sama komanda
	 */
	public void obradiKomanduOSVJEZI(PrintWriter out, String neispravanFormatPoruka,
			String neispravanKodAdminPartneraPoruka, String neuspjesnoPreuzimanjeJelovnikaPoruka,
			String neuspjesnoPreuzimanjeKartePicaPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminPartnera.equals(kod)) {
					this.brava.lock();
					if (!this.dohvatiJelovnike()) {
						out.println(neuspjesnoPreuzimanjeJelovnikaPoruka);
					} else if (!this.dohvatiKartePica()) {
						out.println(neuspjesnoPreuzimanjeKartePicaPoruka);
					} else {
						out.println("OK");
					}

					if (this.brava.isHeldByCurrentThread()) {
						this.brava.unlock();
					}



			} else {
				out.println(neispravanKodAdminPartneraPoruka);
			}
		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu STATUS
	 * @param out - pisač
	 * @param neispravanFormatPoruka - ako je neispravan format
	 * @param neispravanDioPosluzitelja - ako je neispravan dio poslužitelja
	 * @param neispravanKodAdminPartneraPoruka - ako je neispravan admin kod tvrtke
	 * @param komanda - sama komanda
	 */
	public void obradiKomanduSTATUS(PrintWriter out, String neispravanFormatPoruka, String neispravanDioPosluzitelja,
			String neispravanKodAdminPartneraPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminPartnera.equals(kod)) {
				try {
					var dioPosluzitelja = Integer.parseInt(dijeloviKomande[2]);
					if (dijeloviPosluzitelja.containsKey(dioPosluzitelja)) {
						out.println("OK " + dijeloviPosluzitelja.get(dioPosluzitelja));
					} else {
						out.println(neispravanDioPosluzitelja);
					}
				} catch (NumberFormatException ex) {

				}

			} else {
				out.println(neispravanKodAdminPartneraPoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu RAČUN, koristi {@link #brava}
	 * 
	 * @param out                      - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka   - poruka neispravnog formata
	 * @param narudzbaNePostojiPoruka  - poruka nepostojeće narudžbe
	 * @param neuspjesnoSlanjeObracuna - poruka ako je neuspješno slanje
	 * @param komanda                  - originalna komanda
	 */
	public void obradiKomanduRACUN(PrintWriter out, String neispravanFormatPoruka, String narudzbaNePostojiPoruka,
			String neuspjesnoSlanjeObracuna, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var korisnik = dijeloviKomande[1];
			brava.lock();
			if (this.narudzbe.containsKey(korisnik)) {
				var korisnikoveNarudzbe = this.narudzbe.get(korisnik);
				this.placeneNarudzbe.add(new ConcurrentLinkedQueue<Narudzba>(korisnikoveNarudzbe));

				korisnikoveNarudzbe.clear();
				this.narudzbe.remove(korisnik);
				if (this.placeneNarudzbe.size() % this.kvotaNarudzbi == 0) {
					
					var obracuni = new ConcurrentHashMap<String, Obracun>();
					for (var narudzba : this.placeneNarudzbe) {
						for (var stavka : narudzba) {
							var kolicina = stavka.kolicina();
							if (obracuni.containsKey(stavka.id())) {
								var stavkaStara = obracuni.get(stavka.id());
								kolicina += stavkaStara.kolicina();
							}
							var obracun = new Obracun(this.id, stavka.id(), stavka.jelo(), kolicina, stavka.cijena(),
									stavka.vrijeme());
							obracuni.put(stavka.id(), obracun);
						}

					}
					var gson = new Gson();
					var obracuniJSON = gson.toJson(obracuni.values());
					if (this.posaljiObracune(obracuniJSON)) {
						this.placeneNarudzbe.clear();
						out.println("OK");
						out.println(obracuniJSON);

					} else {
						out.println(neuspjesnoSlanjeObracuna);
					}
				} else {
					out.println("OK");
				}

			} else {
				out.println(narudzbaNePostojiPoruka);
			}

			brava.unlock();

		} else

		{
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu PIĆE, koristi {@link #brava}
	 * 
	 * @param out                      - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka   - poruka neispravnog formata
	 * @param narudzbaNePostojiPoruka  - poruka nepostojeće narudžbe
	 * @param nePostojiPicePoruka      - poruka ako nema stavke karte pića s tom
	 *                                 šifrom
	 * @param neispravnaKolicinaPoruka - poruka ako je količina negativna ili 0
	 * @param komanda                  - originalna komanda
	 */
	public void obradiKomanduPICE(PrintWriter out, String neispravanFormatPoruka, String narudzbaNePostojiPoruka,
			String nePostojiPicePoruka, String neispravnaKolicinaPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 4) {
			var korisnik = dijeloviKomande[1];
			var idPica = dijeloviKomande[2];
			try {
				brava.lock();
				var kolicina = Float.parseFloat(dijeloviKomande[3]);
				if (kolicina > 0) {
					if (this.narudzbe.containsKey(korisnik)) {
						if (this.kartePica.containsKey(idPica)) {
							var stavkeKartePica = this.kartePica.get(idPica);
							var korisnikovaNarudzba = this.narudzbe.get(korisnik);
							var narudzba = new Narudzba(korisnik, idPica, false, kolicina, stavkeKartePica.cijena(),
									System.currentTimeMillis());
							korisnikovaNarudzba.add(narudzba);
							out.println("OK");
						} else {
							out.println(nePostojiPicePoruka);
						}

					} else {
						out.println(narudzbaNePostojiPoruka);
					}
				} else {
					out.println(neispravnaKolicinaPoruka);
				}

			} catch (NumberFormatException ex) {
				out.println(neispravanFormatPoruka);
			} finally {
				brava.unlock();
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu JELO, koristi {@link #brava}
	 * 
	 * @param out                     - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka  - poruka neispravnog formata
	 * @param narudzbaNePostojiPoruka - poruka nepostojeće narudžbe
	 * @param nePostojiJeloPoruka     - poruka ako nema stavke jelovnika pića s tom
	 *                                šifrom
	 * @param netocnaKolicinaPoruka   - poruka ako je količina negativna ili 0
	 * @param komanda                 - originalna komanda
	 */
	public void obradiKomanduJELO(PrintWriter out, String neispravanFormatPoruka, String narudzbaNePostojiPoruka,
			String nePostojiJeloPoruka, String netocnaKolicinaPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 4) {
			var korisnik = dijeloviKomande[1];
			var idJela = dijeloviKomande[2];
			try {
				brava.lock();
				var kolicina = Float.parseFloat(dijeloviKomande[3]);
				if (kolicina > 0) {
					if (this.narudzbe.containsKey(korisnik)) {
						if (this.jelovnici.containsKey(idJela)) {
							var stavkaJelovnika = this.jelovnici.get(idJela);
							var korisnikovaNarudzba = this.narudzbe.get(korisnik);

							var narudzba = new Narudzba(korisnik, idJela, true, kolicina, stavkaJelovnika.cijena(),
									System.currentTimeMillis());
							korisnikovaNarudzba.add(narudzba);
							out.println("OK");
						} else {
							out.println(nePostojiJeloPoruka);
						}

					} else {
						out.println(narudzbaNePostojiPoruka);
					}
				} else {
					out.println(netocnaKolicinaPoruka);
				}

			} catch (NumberFormatException ex) {
				out.println(neispravanFormatPoruka);
			} finally {
				brava.unlock();
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu NARUDŽBA, koristi {@link #brava}
	 * 
	 * @param out                    - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka - poruka neispravnog formata
	 * @param narudbzaPostojiPoruka  - poruka greške postojeće narudžbe
	 * @param komanda                - originalna komanda
	 */
	public void obradiKomanduNARUDZBA(PrintWriter out, String neispravanFormatPoruka, String narudbzaPostojiPoruka,
			String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var korisnik = dijeloviKomande[1];
			brava.lock();
			if (!this.narudzbe.containsKey(korisnik)) {
				this.narudzbe.put(korisnik, new ConcurrentLinkedQueue<Narudzba>());

				out.println("OK");

			} else {
				out.println(narudbzaPostojiPoruka);
			}
			brava.unlock();

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu KARTAPIĆA
	 * 
	 * @param out                    - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka - poruka neispravnog formata
	 * @param komanda                - originalna komanda
	 */
	public void obradiKomanduKARTAPICA(PrintWriter out, String neispravanFormatPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			out.println("OK");
			var gson = new Gson();
			gson.toJson(this.kartePica.values(), out);
		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu JELOVNIK
	 * 
	 * @param out                    - pisač gdje se zapisuje
	 * @param neispravanFormatPoruka - poruka neispravnog formata
	 * @param komanda                - originalna komanda
	 */
	public void obradiKomanduJELOVNIK(PrintWriter out, String neispravanFormatPoruka, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			out.println("OK");
			var gson = new Gson();
			gson.toJson(this.jelovnici.values().toArray(), out);
		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Šalje obračune na poslužitelja za rad s partnerima
	 * 
	 * @param obracuni - JSON string kolekcije obračuna
	 * @return true ako je poslužitelj vratio OK, false inače
	 */
	public boolean posaljiObracune(String obracuni) {
		try (var mreznaUticnica = new Socket(this.adresaPosluzitelja, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true)

		) {
			var komanda = "OBRAČUN " + this.id + " " + this.sigurnosniKod;
			out.println(komanda + "\n" + obracuni);

			var odgovor = in.readLine();

			mreznaUticnica.shutdownInput();
			mreznaUticnica.shutdownOutput();
			mreznaUticnica.close();

			if (odgovor != null && odgovor.startsWith("ERROR")) {
				return false;
			}

			return true;

		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {

			return false;
		}

	}

	/**
	 * Utvrđuje je li jar pokrenut u načinu za registraciju, kraj ili pokretanje
	 * poslužitelja za kupce
	 * 
	 * @param args - argumenti skojim je jar datoteka pokrenuta
	 */
	public static void main(String[] args) {
		if (args.length > 2 || args.length < 1) {
			System.out.println("Broj argumenata veći od 2 ili manji od 1.");
			return;
		}

		var program = new PosluziteljPartner();
		var nazivDatoteke = args[0];

		if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
			return;
		}

		if (args.length == 1) {

			program.registrirajSe();
			return;
		}
		var linija = args[1];

		var poklapanjeKraj = program.predlozakKraj.matcher(linija);
		var poklapanjePartner = program.predlozakPartner.matcher(linija);
		var statusKraj = poklapanjeKraj.matches();
		var statusPartner = poklapanjePartner.matches();
		if (statusKraj) {
			program.posaljiKraj();
		} else if (statusPartner) {

			program.pokreniPosluziteljaZaPrijemZahtjevaKupaca();

		}
	}

	/**
	 * Validira konfiguracijske podatke potrebne za način rada KRAJ
	 * 
	 * @return true ako su {@link #kodZaKraj}, {@link #adresaPosluzitelja},
	 *         {@link #mreznaVrataKraj} validni, false inače
	 */
	public boolean validirajKonfiguracijskePodatkeZaKraj() {
		try {
			this.kodZaKraj = this.konfig.dajPostavkuOsnovno("kodZaKraj", "");
			this.adresaPosluzitelja = this.konfig.dajPostavkuOsnovno("adresa", "");
			this.mreznaVrataKraj = Integer.parseInt(this.konfig.dajPostavkuOsnovno("mreznaVrataKraj", ""));

			if (this.kodZaKraj.isBlank() || this.adresaPosluzitelja.isBlank()) {
				return false;
			}

			return true;
		} catch (NumberFormatException e) {
			return false;
		}

	}

	/**
	 * Šalje komandu KRAJ poslužitelju za kraj rada
	 */
	public void posaljiKraj() {
		if (this.validirajKonfiguracijskePodatkeZaKraj()) {
			try {
				var mreznaUticnica = new Socket(this.adresaPosluzitelja, this.mreznaVrataKraj);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true);
				out.println("KRAJ " + kodZaKraj);
				mreznaUticnica.shutdownOutput();
				var linija = in.readLine();
				mreznaUticnica.shutdownInput();
				if (linija.equals("OK")) {
					System.out.println("Uspješan kraj poslužitelja.");
				}
				mreznaUticnica.close();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			} catch (IllegalArgumentException e) {
			}
		}

	}

	/**
	 * Ucitaj konfiguraciju.
	 *
	 * @param nazivDatoteke naziv datoteke
	 * @return true, ako je uspješno učitavanje konfiguracije
	 */
	private boolean ucitajKonfiguraciju(String nazivDatoteke) {
		try {
			this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
			return true;
		} catch (NeispravnaKonfiguracija ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
