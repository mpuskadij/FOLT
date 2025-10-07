package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

/**
 * Klasa koja predstavlja FOLT tvrtku
 */
public class PosluziteljTvrtka {
	/** Mapa ID kuhinje i njezine vrste i naziva */
	public Map<Integer, String> kuhinje = new ConcurrentHashMap<>();

	/** Mapa vrste kuhinje i svih njezinih jelovnika */
	public Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

	/** Mapa s ključem ID karte pića i objekta {@link KartaPica} */
	public Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

	/** Mapa s ključem ID partnera i objekta {@link Partner} */
	public Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

	/** Polje sa zapisima {@link Obracun} */
	public CopyOnWriteArrayList<Obracun> obracuni = new CopyOnWriteArrayList<>();

	/** Lista svih virtualnih dretvi */
	public Map<Future<?>, Integer> dretveZaObraduZahtjeva = new ConcurrentHashMap<Future<?>, Integer>();
	
	/**
	 * Podatak koji broj predstavlja da je određeni dio poslužitelja u pauzi
	 */
	public final int pauzaStatus = 0;
	
	/**
	 * Podatak koji broj predstavlja da je određeni dio poslužitelja u aktivnom radu
	 */
	public final int aktivanStatus = 1;

	/**
	 * Podatak koji broj predstavlja dio poslužitelja za registraciju - metoda {@link #pokreniPosluziteljRegistracijaPartnera()}
	 */
	public final int dioRegistracija = 1;
	
	/**
	 * Podatak koji broj predstavlja dio poslužitelja za rad s partnerima - metoda {@link #pokreniPosluziteljRadPartnera()}
	 */
	public final int dioPartneri = 2;
	
	
	/**
	 * Kolekcija koja sadrži sve dijelove poslužitelja i postavlja ih u status {@link #aktivanStatus}
	 */
	public Map<Integer, Integer> dijeloviPosluzitelja = new ConcurrentHashMap<Integer, Integer>(
			Map.of(dioRegistracija, aktivanStatus, dioPartneri, aktivanStatus));

	/** Brava za zjučavanje */
	public ReentrantLock brava = new ReentrantLock();

	/** Konfiguracijski podaci */
	public Konfiguracija konfig;

	/** Pokretač dretvi */
	public ExecutorService executor = null;

	/** Pauza dretve. */
	public int pauzaDretve = 1000;

	/** Kod za kraj rada */
	public String kodZaKraj = "";

	/** Naziv JSON datoteke koja sadrži popis partnera */
	public String datotekaPartnera = "";

	/** Naziv JSON datoteke koja sadrži kartu pića */
	public String datotekaKartaPica = "";

	/** Naziv JSON datoteke koja sadrži sve obračune */
	public String datotekaObracuna = "";

	/** Broj vrata na kojem će se pokrenuti poslužitelj za kraj rada */
	public int mreznaVrataKraj;

	/** Broj vrata na kojem će se pokrenuti poslužitelj za registraciju partnera */
	public int mreznaVrataRegistracija;

	/** Broj vrata na kojem će se pokrenuti poslužitelj za rad s partnerima */
	public int mreznaVrataRad;

	/** Broj dozvoljenih čekača */
	public int brojCekaca = 0;
	
	/**
	 * Konfiguracijski podataka koji predstavlja admin kod pomoću kojega se aktiviraju komande SPAVA, STATUS, OSVJEŽI, START, PAUZA
	 */
	public String kodZaAdminTvrtke = "";
	
	/**
	 * Konfiguracijski podatak gdje se šalje HEAD /kraj/info kod KRAJ komande i POST /obracun kod OBRAČUN komande
	 */
	public String restAdresa = "";
	

	/** Zastavica za kraj rada */
	public AtomicBoolean kraj = new AtomicBoolean(false);

	/**
	 * Glavna funkcija koja se pokreće prilikom pokretanja poslužitelja
	 * 
	 * @param args polje argumenata s kojima je program pokrenut - prima samo 1 i to
	 *             je naziv konfiguracijske datoteke s JSON,XML,BIN,TXT ekstenzijom
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Broj argumenata nije 1.");
			return;
		}

		var program = new PosluziteljTvrtka();
		var nazivDatoteke = args[0];

		program.pripremiKreni(nazivDatoteke);
	}

	/**
	 * Metoda ručno zatvara sve virtualne dretve
	 * 
	 * @return broj prisilno zatvorenih virtualnih dretvi
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
	 * Metoda koja se izvrši bilo prisilnom završetkom ili normalnim završetkom
	 * programa
	 */
	public void zakaciPrisilnoZaustavljanje() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			int brojZatvorenihDretvi = this.prekiniSveDretve();
			System.out.println("Broj prisilno zatvorenih virtualnih dretvi: " + brojZatvorenihDretvi);
			this.executor.shutdownNow();
		}));
	}

	/**
	 * 
	 * @return true ako je datoteka obračuna postoji ili ne posotji pa je uspješno
	 *         kreirana, false ako nije ju ni moguće kreirati
	 */
	public boolean provjeriPostojanjeDatotekeObracuna() {
		var putanja = Path.of(this.datotekaObracuna);
		if (!Files.exists(putanja) || !Files.isReadable(putanja) || !Files.isRegularFile(putanja)) {
			try {
				Files.createFile(putanja);
				Files.writeString(putanja, "[]");
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Učitava obračune iz datoteke koja je zadana konfiguracijom te puni array svih
	 * obračuna. Koristi bravu {@link #bravaDatotekeObracuna} stoga samo jedna
	 * dretva može istovremeno ovu metodu izvršavati. Koristi GSON.
	 */
	public void ucitajObracuneIzDatoteke() {

		try (var br = Files.newBufferedReader(Path.of(this.datotekaObracuna))) {

			this.obracuni.clear();
			Gson gson = new Gson();
			var obracuni = gson.fromJson(br, Obracun[].class);
			for (var obracun : obracuni) {
				if (obracun.id() != null) {
					this.obracuni.add(obracun);
				}
			}

		} catch (JsonIOException ex) {
			return;
		} catch (JsonSyntaxException ex) {
			return;
		} catch (IOException ex) {
			return;
		}
	}

	/**
	 * Sprema sve obračune iz {@link #obracuni} polja u datoteku definiranom
	 * {@link #datotekaObracuna} koristeći GSON.
	 */
	public void spremiDatotekuObracuna() {
		try (var br = Files.newBufferedWriter(Path.of(this.datotekaObracuna))) {
			Gson gson = new Gson();
			gson.toJson(this.obracuni, br);

		} catch (JsonIOException ex) {
			return;
		} catch (JsonSyntaxException ex) {
			return;
		} catch (IOException ex) {
		}

	}

	/**
	 * Sprema sve obračune iz {@link #partneri} mape u datoteku definiranom
	 * {@link #datotekaPartnera} koristeći GSON.
	 */
	public void spremiDatotekuPartnera() {
		this.brava.lock();
		try (var br = Files.newBufferedWriter(Path.of(this.datotekaPartnera))) {
			Gson gson = new Gson();
			gson.toJson(this.partneri.values(), br);
		} catch (JsonIOException ex) {
			return;
		} catch (JsonSyntaxException ex) {
			return;
		} catch (IOException ex) {
		} finally {
			this.brava.unlock();
		}

	}

	/**
	 * Validira {@link #pauzaDretve}, {@link #kodZaKraj},
	 * {@link #datotekaKartaPica}, {@link #datotekaObracuna},
	 * {@link #datotekaPartnera}, {@link #mreznaVrataKraj}, {@link #mreznaVrataRad}
	 * {{@link #mreznaVrataRegistracija}
	 * 
	 * @return true ako su svi konfiguracijski podaci su u redu, false ako
	 *         {@link #pauzaDretve} nije broj, {@link #kodZaKraj} nije zadan,
	 *         {@link #datotekaKartaPica} nije zadana, {@link #datotekaObracuna}
	 *         nije zadana, {@link #datotekaPartnera} nije zadana,
	 *         {@link #mreznaVrataKraj} nije broj, {@link #mreznaVrataRad} nije
	 *         broj, {@link #mreznaVrataRegistracija} nije broj ili dvoja vrata
	 *         imaju isti broj
	 */
	public boolean validirajKonfiguracijskePodatke() {
		try {
			this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
		} catch (NumberFormatException greska) {
			return false;
		}
		this.kodZaKraj = this.konfig.dajPostavkuOsnovno("kodZaKraj", "");
		this.kodZaAdminTvrtke = this.konfig.dajPostavkuOsnovno("kodZaAdminTvrtke", "");
		this.datotekaPartnera = this.konfig.dajPostavkuOsnovno("datotekaPartnera", "");
		this.datotekaKartaPica = this.konfig.dajPostavkuOsnovno("datotekaKartaPica", "");
		this.datotekaObracuna = this.konfig.dajPostavkuOsnovno("datotekaObracuna", "");
		this.restAdresa = this.konfig.dajPostavkuOsnovno("restAdresa", "");
		
		if (this.kodZaAdminTvrtke.isBlank() || this.kodZaKraj.isBlank() || this.datotekaPartnera.isBlank()
				|| this.datotekaKartaPica.isBlank() || this.datotekaObracuna.isBlank() || this.restAdresa.isBlank()) {
			return false;
		}

		try {
			this.mreznaVrataKraj = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
			this.mreznaVrataRegistracija = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
			this.mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));

			if (this.mreznaVrataKraj == this.mreznaVrataRad || this.mreznaVrataKraj == this.mreznaVrataRegistracija
					|| this.mreznaVrataRad == this.mreznaVrataRegistracija) {
				return false;
			}
		} catch (NumberFormatException greska) {
			return false;
		}
		return true;
	}

	/**
	 * Učitava datoteku karte pića prema podatku iz {@link #datotekaKartaPica}
	 * koristeći GSON u mapu {@link #kartaPica}
	 * 
	 * @return true ako je uspješno učitana, false u suprotnom
	 */
	public boolean ucitajDatotekuKartePica() {
		this.kartaPica.clear();
		var putanja = Path.of(this.datotekaKartaPica);
		if (!Files.exists(putanja) || !Files.isReadable(putanja) || !Files.isRegularFile(putanja)) {
			return false;
		}
		try (var br = Files.newBufferedReader(Path.of(this.datotekaKartaPica))) {
			Gson gson = new Gson();
			var stavkeKartePica = gson.fromJson(br, KartaPica[].class);

			for (var stavka : stavkeKartePica) {
				if (stavka.naziv() != null) {
					this.kartaPica.putIfAbsent(stavka.id(), stavka);
				}
			}

		} catch (JsonIOException ex) {
			return false;
		} catch (JsonSyntaxException ex) {
			return false;
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Učitava datoteku partnera prema podatku iz {@link #datotekaKartaPica}
	 * koristeći GSON u mapu {@link #partneri}
	 * 
	 * @return true ako datoteka ne postoji i uspješno je kreirana ili ako je
	 *         uspješno pročitana, false ako ne može kreirati ili pročitati datoteku
	 */
	public boolean ucitajDatotekuPartnera() {
		var putanja = Path.of(this.datotekaPartnera);
		if (!Files.exists(putanja) || !Files.isReadable(putanja) || !Files.isRegularFile(putanja)) {
			try {
				Files.createFile(putanja);
				Files.writeString(putanja, "[]");
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		try (var br = Files.newBufferedReader(Path.of(this.datotekaPartnera))) {
			Gson gson = new Gson();
			var partneriIzDatoteke = gson.fromJson(br, Partner[].class);
			for (var partner : partneriIzDatoteke) {
				if (partner.naziv() != null) {
					this.partneri.putIfAbsent(partner.id(), partner);
				}
			}

		} catch (JsonIOException ex) {
			return false;
		} catch (JsonSyntaxException ex) {
			return false;
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Učitava sve jelovnike prema podatku iz konfiguracija koristeći GSON u mapu
	 * {@link #kuhinje} i {@link #jelovnici} Preskače kuhinje koje se nalaze u
	 * konfiguraciji, ali nemaju svoju JSON datoteku. Provjera samo kuhinje od 1 do
	 * 9. Nakon što su svi jelovnici učitani, briše partnere u memoriji koji su registrirani za nepostojeću kuhinju.
	 * 
	 * @return true ako je uspješno učitana, false u suprotnom
	 */
	public boolean ucitajJelovnike() {
		this.jelovnici.clear();
		for (int brojKuhinje = 1; brojKuhinje < 9; brojKuhinje++) {
			var graditelj = new StringBuilder();
			graditelj.append("kuhinja_");
			graditelj.append(brojKuhinje);
			var kuhinja = this.konfig.dajPostavku(graditelj.toString());
			if (kuhinja != null) {
				graditelj.append(".json");
				if (Files.exists(Path.of(graditelj.toString()))) {
					try (var br = Files.newBufferedReader(Path.of(graditelj.toString()))) {
						Gson gson = new Gson();
						var stavkeJelovnika = gson.fromJson(br, Jelovnik[].class);
						var puniJelovnik = new ConcurrentHashMap<String, Jelovnik>();
						for (Jelovnik stavka : stavkeJelovnika) {
							puniJelovnik.putIfAbsent(stavka.id(), stavka);
						}
						this.kuhinje.putIfAbsent(brojKuhinje, kuhinja);
						var vrstaKuhinje = kuhinja.split(";")[0];
						this.jelovnici.putIfAbsent(vrstaKuhinje, puniJelovnik);

					} catch (JsonIOException ex) {
						return true;
					} catch (JsonSyntaxException ex) {
						return true;
					} catch (IOException ex) {
						return false;
					}
				}

			}
		}
		this.partneri.values().removeIf(p -> !this.jelovnici.containsKey(p.vrstaKuhinje()));
		return true;
	}

	/**
	 * Funkcija uzima prvi argument koji je proslijeđen programu i pokušava se
	 * učitati konfiguracija u direktoriju iz koje je program pokrenut. Nakon svih
	 * validacija datoteka, pokreće 3 virtualne dretve, jednu za
	 * {@link #pokreniPosluziteljKraj()}, jednu za
	 * {@link #pokreniPosluziteljRadPartnera()} i jednu za
	 * {@link #pokreniPosluziteljRegistracijaPartnera()}. Čeka dok se zastavica
	 * {@link #kraj} ne postavi na {@code true} te prekida sve radne dretve, inače
	 * spava onoliko koliko {@link #pauzaDretve} određuje.
	 * 
	 * @param nazivDatoteke - naziv datoteke koja sadrži konfiguraciju
	 */
	public void pripremiKreni(String nazivDatoteke) {
		if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
			return;
		}
		

		if (!validirajKonfiguracijskePodatke()) {
			return;
		}
		;

		if (!ucitajDatotekuPartnera()) {
			return;
		}

		if (!ucitajJelovnike()) {
			return;
		}

		if (!ucitajDatotekuKartePica()) {
			return;
		}

		if (!provjeriPostojanjeDatotekeObracuna()) {
			return;
		}

		this.zakaciPrisilnoZaustavljanje();
		var builder = Thread.ofVirtual();
		var factory = builder.factory();
		this.executor = Executors.newThreadPerTaskExecutor(factory);

		var dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
		var dretvaZaRegistracijuPartnera = this.executor.submit(() -> this.pokreniPosluziteljRegistracijaPartnera());
		var dretvaZaRadPartnera = this.executor.submit(() -> this.pokreniPosluziteljRadPartnera());

		this.dretveZaObraduZahtjeva.put(dretvaZaKraj, this.mreznaVrataKraj);
		this.dretveZaObraduZahtjeva.put(dretvaZaRegistracijuPartnera, this.mreznaVrataRegistracija);
		this.dretveZaObraduZahtjeva.put(dretvaZaRadPartnera, this.mreznaVrataRad);

		while (!this.kraj.get()) {
			try {
				Thread.sleep(this.pauzaDretve);
			} catch (InterruptedException e) {
			}
		}

	}

	/**
	 * Kreira novu utičnicu za poslužitelja, čeka dolazak zahtjeva dok god je
	 * zastavica {@link #kraj} {@code false} te kreira novu utičnicu i virtualnu
	 * dretvu kojoj zadaje da obradi metodu
	 * {@link #obradiRegistracijuPartnera(Socket)}. Kada zastavica postane true,
	 * zatvara utičnicu.
	 */
	public void pokreniPosluziteljRegistracijaPartnera() {
		try (ServerSocket ss = new ServerSocket(this.mreznaVrataRegistracija, this.brojCekaca)) {
			while (!this.kraj.get() && !Thread.interrupted()) {
				var mreznaUticnica = ss.accept();
				var dretva = this.executor.submit(() -> this.obradiRegistracijuPartnera(mreznaUticnica));
				this.dretveZaObraduZahtjeva.put(dretva, mreznaUticnica.getLocalPort());
			}
			ss.close();

		} catch (IOException greska) {

		}

	}

	/**
	 * Svaka virtualna dretva poslužitelja za registraciju partnera ovo izvršava.
	 * Definira poruke greške te provjerava radi li se o komandi PARTNER, POPIS ili
	 * OBRIŠI. Nakon kraja rada, zatvara dobivenu utičnicu. Ako je poslužitelj u pauzi, vraća odgovarajuću poruku
	 * 
	 * @param mreznaUticnica - utičnica na kojoj je došla komanda i iz koje se čita
	 *                       i upisuje kasnije
	 */
	public void obradiRegistracijuPartnera(Socket mreznaUticnica) {
		String neispravanFormatPoruka = "ERROR 20 - Format komande nije ispravan";
		String partnerVecPostojiPoruka = "ERROR 21 - Već postoji partner s id u kolekciji partnera";
		String neispravanSigurnosniKodPoruka = "ERROR 22 - Neispravan sigurnosni kod partnera";
		String partnerNePostojiPoruka = "ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera";
		String pauzaPoruka = "ERROR 24 – Poslužitelj za registraciju partnera u pauzi";
		String nepostojecaKuhinja = "ERROR 29 - Registracija za nepostojeću kuhinju.";
	
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true);) {
			
			if (this.dijeloviPosluzitelja.get(this.dioRegistracija) != this.aktivanStatus) {
				out.println(pauzaPoruka);
				return;
			}
			String komanda = in.readLine();
			

			if (komanda.equals("POPIS")) {
				obradiKomanduPOPIS(out);
			}

			else if (komanda.startsWith("PARTNER")) {
				obradiKomanduPARTNER(neispravanFormatPoruka, partnerVecPostojiPoruka, nepostojecaKuhinja, out, komanda);
			} else if (komanda.startsWith("OBRIŠI")) {
				obradiKomanduOBRISI(neispravanFormatPoruka, neispravanSigurnosniKodPoruka, partnerNePostojiPoruka, out,
						komanda);
			} else {
				out.println(neispravanFormatPoruka);
			}
		} catch (

		IOException ex) {
		} catch (JsonIOException ex) {
		} finally {
			if (!mreznaUticnica.isClosed()) {
				try {
					mreznaUticnica.shutdownInput();
					mreznaUticnica.shutdownOutput();
					mreznaUticnica.close();
				} catch (IOException greska) {
				}
			}

		}

	}

	/**
	 * Obrađuje komandu POPIS iz zadaće koristeći GSON. Prolazi kroz popis svih
	 * partnera i za svakoga prevađa u zapis {@link PartnerPopis}
	 * 
	 * @param out - objekt koji zapisuje na neki izlaz (izlaz utičnice)
	 */
	public void obradiKomanduPOPIS(PrintWriter out) {
		out.println("OK");
		var sviPartneri = new ArrayList<PartnerPopis>();
		for (var partner : this.partneri.values()) {
			var partnerZaPopis = new PartnerPopis(partner.id(), partner.naziv(), partner.vrstaKuhinje(),
					partner.adresa(), partner.mreznaVrata(), partner.gpsSirina(), partner.gpsDuzina());
			sviPartneri.add(partnerZaPopis);
		}
		var gson = new Gson();
		gson.toJson(sviPartneri, out);
	}

	/**
	 * Obrađuje komandu PARTNER. Validira je li dobiven dovoljan broj argumenata i
	 * jesu li brojčani podaci brojevi. Kreira novi objekt zapisa {@link Partner},
	 * dodaje u mapu {@link #partneri} i poziva metodu
	 * {@link #spremiDatotekuPartnera()} te vraća OK. Ispisuje grešku ako nije
	 * ispravan format, partner već postoji s tim ID-om ili partner želi
	 * registrirati nepostojeću kuhinju. Generira sigurnosni kod spajanjem naziva i
	 * adrese partnera, pozivanjem metode {@link String#hashCode()} i zatim
	 * pozivanjem metode {@link Integer#toHexString(int)}
	 * 
	 * @param neispravanFormatPoruka  - poruka da je format neispravan
	 * @param partnerVecPostojiPoruka - poruka da partner već postoji
	 * @param nepostojecaKuhinja      - poruka za nepostojeću kuhinju
	 * @param out                     - objekt za zapis na neki izlaz
	 * @param komanda                 - originala komanda dobivena preko utičnice
	 */

	public void obradiKomanduPARTNER(String neispravanFormatPoruka, String partnerVecPostojiPoruka,
			String nepostojecaKuhinja, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
		if (dijeloviKomande.length == 10) {
			try {
				var id = Integer.parseInt(dijeloviKomande[1]);
				var nazivPartnera = dijeloviKomande[2].replace("\"", "");
				var vrstaKuhinje = dijeloviKomande[3];
				var adresa = dijeloviKomande[4];
				var mreznaVrata = Integer.parseInt(dijeloviKomande[5]);
				
				var gpsSirina = Float.parseFloat(dijeloviKomande[6]);
				var gpsDuzina = Float.parseFloat(dijeloviKomande[7]);
				var mreznaVrataKraj = Integer.parseInt(dijeloviKomande[8]);
				var adminKod = dijeloviKomande[9];
				
				if (this.partneri.containsKey(id)) {
					out.println(partnerVecPostojiPoruka);
				} else {
					if (!this.jelovnici.containsKey(vrstaKuhinje)) {
						out.println(nepostojecaKuhinja);
					} else {
						var spojenNazivIAdresa = nazivPartnera + adresa;
						var sigurnosniKod = Integer.toHexString(spojenNazivIAdresa.hashCode());
						var noviPartner = new Partner(id, nazivPartnera, vrstaKuhinje, adresa, mreznaVrata,
								mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod.toString(), adminKod);
						this.partneri.putIfAbsent(noviPartner.id(), noviPartner);
						this.spremiDatotekuPartnera();
						out.println("OK " + sigurnosniKod);
					}

				}
			} catch (NumberFormatException greska) {
				out.println(neispravanFormatPoruka);
			}
		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu izbriši. Provjerava postoji li partner s tim ID-om u mapi
	 * {@link #partneri} i odgovara li {@link Partner#sigurnosniKod()} dobivenom
	 * sigurnosnom kodu.
	 * 
	 * @param neispravanFormatPoruka        - poruka ako je format neispravan
	 * @param neispravanSigurnosniKodPoruka - poruka ako je sigurnosni kod
	 *                                      neispravan
	 * @param partnerNePostojiPoruka        - poruka ako partner ne postoji poruka
	 * @param out                           - objekt za zapis na neki izlaz
	 * @param komanda                       - komanda u punom zapisu
	 */
	public void obradiKomanduOBRISI(String neispravanFormatPoruka, String neispravanSigurnosniKodPoruka,
			String partnerNePostojiPoruka, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var id = Integer.parseInt(dijeloviKomande[1]);
			var sigurnosniKod = dijeloviKomande[2].replace("\n", "");
			if (this.partneri.containsKey(id)) {
				if (this.partneri.values().stream().anyMatch(p -> p.sigurnosniKod().equals(sigurnosniKod))) {
					this.partneri.remove(id);
					this.spremiDatotekuPartnera();
					out.println("OK");
				} else {
					out.println(neispravanSigurnosniKodPoruka);
				}

			} else {
				out.println(partnerNePostojiPoruka);
			}
		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Kreira novu utičnicu na vratima prema {@link #mreznaVrataRad} s
	 * {@link #brojCekaca} čekača. Za svaki zahtjev kreira novu utičnicu i predaje
	 * joj virtuallnoj dretvi koja kreće s metodom
	 * {@link #obradiRadPartnera(Socket)}
	 */
	public void pokreniPosluziteljRadPartnera() {
		try (ServerSocket ss = new ServerSocket(this.mreznaVrataRad, this.brojCekaca)) {
			while (!this.kraj.get() && !Thread.interrupted()) {
				var mreznaUticnica = ss.accept();
				var dretva = this.executor.submit(() -> this.obradiRadPartnera(mreznaUticnica));
				this.dretveZaObraduZahtjeva.put(dretva, mreznaUticnica.getLocalPort());
			}

			ss.close();

		} catch (IOException e) {
		}
	}

	/**
	 * Definira poruke greške te određuje koja komanda je pozvana te ovisno o tome
	 * poziva
	 * {@link #obradiKomanduJELOVNIK(String, String, String, PrintWriter, String)}
	 * {@link #obradiKomanduKARTAPICA(String, String, PrintWriter, String)} ili
	 * {@link #obradiKomanduOBRACUN(String, String, String, String, String, String, BufferedReader, PrintWriter, String,boolean)}.
	 * Ako je poslužitelj u pauzi, vraća odgovarajuću poruku greške.
	 * 
	 * @param mreznaUticnica - utičnica s koje se čita komanda i zapisuje rezultat
	 *                       obrade
	 */
	public void obradiRadPartnera(Socket mreznaUticnica) {
		var neispravanFormatPoruka = "ERROR 30 - Format komande nije ispravan";
		var partnerNePostojiIliNeispravanKodPoruka = "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera";
		var neispravanObracunPoruka = "ERROR 35 - Neispravan obračun";
		var nePostojiJelovnikSVrstomKuhinjePoruka = "ERROR 32 - Ne postoji jelovnik s vrstom kuhinje koju partner ima ugovorenu";
		var neispravanJelovnik = "ERROR 33 - Neispravan jelovnik";
		var neispravnaKartaPicaPoruka = "ERROR 34 - Neispravna karta pića";
		var restZahtjevNeuspjesan = "ERROR 37 – RESTful zahtjev nije uspješan";
		var pauzaPoruka = "ERROR 36 – Poslužitelj za partnere u pauzi"; 
		

		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
						true);) {
			if (this.dijeloviPosluzitelja.get(this.dioPartneri) != this.aktivanStatus) {
				out.println(pauzaPoruka);
				return;
			}
			String komanda = in.readLine();
			

			if (komanda.startsWith("JELOVNIK")) {
				obradiKomanduJELOVNIK(neispravanFormatPoruka, partnerNePostojiIliNeispravanKodPoruka,
						nePostojiJelovnikSVrstomKuhinjePoruka, out, komanda);
			}

			else if (komanda.startsWith("KARTAPIĆA")) {
				obradiKomanduKARTAPICA(neispravanFormatPoruka, partnerNePostojiIliNeispravanKodPoruka, out, komanda);
			}

			else if (komanda.startsWith("OBRAČUNWS")) {
				mreznaUticnica.setSoTimeout(5000);
				try {
					obradiKomanduOBRACUN(neispravanFormatPoruka, partnerNePostojiIliNeispravanKodPoruka,
							neispravanObracunPoruka, nePostojiJelovnikSVrstomKuhinjePoruka, neispravanJelovnik,
							neispravnaKartaPicaPoruka,restZahtjevNeuspjesan, in, out, komanda, false);
				}catch(SocketTimeoutException e) {
					out.println(neispravanFormatPoruka);
				}
			} else if (komanda.startsWith("OBRAČUN")) {
				mreznaUticnica.setSoTimeout(5000);
				try {
					obradiKomanduOBRACUN(neispravanFormatPoruka, partnerNePostojiIliNeispravanKodPoruka,
							neispravanObracunPoruka, nePostojiJelovnikSVrstomKuhinjePoruka, neispravanJelovnik,
							neispravnaKartaPicaPoruka,restZahtjevNeuspjesan, in, out, komanda, true);
				}catch(SocketTimeoutException e) {
					out.println(neispravanFormatPoruka);
				}
				
			}

			else {
				out.println(neispravanFormatPoruka);
			}

		} catch (IOException greska) {
		} finally {
			try {
				if (!mreznaUticnica.isClosed()) {
					mreznaUticnica.shutdownInput();
					mreznaUticnica.shutdownOutput();
					mreznaUticnica.close();
				}

			} catch (IOException greska) {
			}

		}
	}
	/** Obrađuje komandu OSVJEŽI
	 * 
	 * @param neispravanFormatPoruka - ako je dobiven premalen ili prevelik broj argumenata komande
	 * @param neispravanKodAdminTvrtkePoruka - ako je kod za admina tvrtke neispravan
	 * @param posluziteljZaPartnereUPauziPoruka - ako je je poslužitelj za rad s partnerima u pauzi i ne mogu se jelovnici učitati
	 * @param out - pisač
	 * @param komanda - sama komanda
	 */
	public void obradiKomanduOSVJEZI(String neispravanFormatPoruka, String neispravanKodAdminTvrtkePoruka,
			String posluziteljZaPartnereUPauziPoruka, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 2) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminTvrtke.equals(kod)) {
				var status = dijeloviPosluzitelja.get(dioPartneri);
				if (status == aktivanStatus) {
					this.brava.lock();
					this.ucitajDatotekuKartePica();
					this.ucitajJelovnike();
					if (this.brava.isHeldByCurrentThread()) {
						this.brava.unlock();
					}
					out.println("OK");

				}
				else {
					out.println(posluziteljZaPartnereUPauziPoruka);
				}
			} else {
				out.println(neispravanKodAdminTvrtkePoruka);
			}
		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu SPAVA
	 * @param neispravanFormatPoruka - ako je neispravan format komande
	 * @param neispravanAdminKod - ako je admin kod tvrtke neispravan
	 * @param dretvaPrekinutaPoruka - ako se dretva prekine tijekom spavanja
	 * @param out - pisač
	 * @param komanda - sama komanda
	 */
	public void obradiKomanduSPAVA(String neispravanFormatPoruka, String neispravanAdminKod, String dretvaPrekinutaPoruka, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			try {
				var vrijeme = Long.parseLong(dijeloviKomande[2]);
				var adminKod = dijeloviKomande[1];
				if (this.kodZaAdminTvrtke.equals(adminKod)) {
					Thread.sleep(vrijeme);
					out.println("OK");
				}
				else {
					out.println(neispravanAdminKod);
				}
				
			} catch (NumberFormatException ex) {
				out.println(neispravanFormatPoruka);
			} catch (InterruptedException e) {
				out.println(dretvaPrekinutaPoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu PAUZA ili START ovisno o posljednjem parametru
	 * @param neispravanFormatPoruka - ako je format neispravan
	 * @param neispravanKodAdminTvrtkePoruka - ako je admin kod tvrtke neispravan
	 * @param neispravanDioPosluzitelja - ako se želi pauzirati/pokrenuti nepostojeći dio poslužitelja
	 * @param pogresnaPromjenaPauzeIliStartaPoruka - ako je poslužitelj već u pauzi/aktivnom radu
	 * @param out - pisač
	 * @param komanda  -sama komanda
	 * @param noviStatus - novi status za postaviti - {@link #aktivanStatus} ili {@link #pauzaStatus}
	 */
	public void obradiKomanduPAUZAiliSTART(String neispravanFormatPoruka, String neispravanKodAdminTvrtkePoruka,
			String neispravanDioPosluzitelja,String  pogresnaPromjenaPauzeIliStartaPoruka, PrintWriter out, String komanda, int noviStatus) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminTvrtke.equals(kod)) {
				try {
					var dioPosluzitelja = Integer.parseInt(dijeloviKomande[2]);
					if (dijeloviPosluzitelja.containsKey(dioPosluzitelja)) {
						if (dijeloviPosluzitelja.get(dioPosluzitelja) != noviStatus) {
							dijeloviPosluzitelja.put(dioPosluzitelja, noviStatus);
							out.println("OK");
						} else {
							out.println(pogresnaPromjenaPauzeIliStartaPoruka);
						}
					} else {
						out.println(neispravanDioPosluzitelja);
					}
				} catch (NumberFormatException ex) {

				}

			} else {
				out.println(neispravanKodAdminTvrtkePoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Obrađuje komandu STATUS
	 * @param neispravanFormatPoruka - ako je format komande neispravan
	 * @param neispravanKodAdminTvrtkePoruka - ako je kod admina tvrtke neispravan
	 * @param neispravanDioPosluzitelja - ako je dio poslužitelja neispravan
	 * @param out - pisač
	 * @param komanda - sama komanda
	 */
	public void obradiKomanduStatus(String neispravanFormatPoruka, String neispravanKodAdminTvrtkePoruka,
			String neispravanDioPosluzitelja, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			var kod = dijeloviKomande[1];
			if (this.kodZaAdminTvrtke.equals(kod)) {
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
				out.println(neispravanKodAdminTvrtkePoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Provjerava postoje li duplikati dobivenih obračuna
	 * 
	 * @param obracuni - polje sivh obračuna iz utičnice
	 * @return true ako postoje duplikate, false inače
	 */
	public boolean postojiDuplikatObracuna(Obracun[] obracuni) {
		var idStavke = new HashSet<String>();
		for (var obracun : obracuni) {
			if (!idStavke.add(obracun.id())) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Obrađuje komandu OBRAČUNWS ili OBRAČUN. Čita sve retke dok ne dođe do ] znaka za kraj.
	 * Provjerava postoji li partner s poslanim ID-om te odgovara li sigurnosni kod.
	 * Zatim provjerava postoji li trenutačno jelovnik za kojega je partner
	 * registriran. Zatim za svaki poslani obračun provjerava se obračunava li
	 * partner nekog drugog partnera te postoji li jelovnik ili karta pića s
	 * dobivenim ID-om. Kad je sve u redu, učitava datoteku obračuna prema
	 * {@link #datotekaObracuna}, dodaje nove obračune i zapisuje u datoteku. Ako je saljiNaRest true, onda šalje POST /obracun te šalje
	 * OK.
	 * 
	 * @param neispravanFormatPoruka                 - poruka ako je nedovoljan broj
	 *                                               parametara
	 * @param partnerNePostojiIliNeispravanKodPoruka - poruka ako partner ne psotoji
	 *                                               ili je sigurnsni kod pogrešan
	 * @param neispravanObracunPoruka                - poruka ako obračun ima ID
	 *                                               drugog partnera ili se ne može
	 *                                               JSON polje pročitati
	 * @param nePostojiJelovnikSVrstomKuhinjePoruka  - poruka ako partner ima
	 *                                               registriranu kuhinju za koju
	 *                                               nema jelovnika
	 * @param neispravanJelovnik                     - poruka ako nema jelovnika s
	 *                                               ID-om dobivenim
	 * @param neispravnaKartaPicaPoruka              - poruka ako nema karte pića s
	 *                                               ID-om dobivenim
	 * @param in                                     - objekt za čitanje s ulaza
	 * @param out                                    - objekt za pisanje na izlaz
	 * @param komanda                                - originalna komanda
	 * @throws IOException - ako se javi greška kod
	 *                     {@link BufferedReader#readLine()} metode
	 */
	public void obradiKomanduOBRACUN(String neispravanFormatPoruka, String partnerNePostojiIliNeispravanKodPoruka,
			String neispravanObracunPoruka, String nePostojiJelovnikSVrstomKuhinjePoruka, String neispravanJelovnik,
			String neispravnaKartaPicaPoruka,String restZahtjevNeuspjesan, BufferedReader in, PrintWriter out, String komanda, boolean saljiNaRest)
			throws IOException {
		var graditelj = new StringBuilder();
		graditelj.append(komanda);
		graditelj.append("\n");
		String linija;
		while ((linija = in.readLine()) != null) {
		    graditelj.append(linija.trim()); 
		    if (linija.trim().endsWith("]")) {
		        break;
		    }
		}
		var redovi = graditelj.toString().split("\n", 2);
		if (redovi.length == 2) {
			var komandaUPrvomRedu = redovi[0].split(" ");
			try {
				var id = Integer.parseInt(komandaUPrvomRedu[1]);
				var sigurnosniKod = komandaUPrvomRedu[2];
				var partner = this.partneri.get(id);

				if (partner != null && partner.sigurnosniKod().equals(sigurnosniKod)) {
					if (this.jelovnici.containsKey(partner.vrstaKuhinje())) {
						redovi[1] = redovi[1].replace("\n", "");
						var jelovniciPartneroveKuhinje = this.jelovnici.get(partner.vrstaKuhinje());
						var gson = new Gson();
						var noviObracuni = gson.fromJson(redovi[1], Obracun[].class);
						if (!this.postojiDuplikatObracuna(noviObracuni)) {
							for (var noviObracun : noviObracuni) {
								if (noviObracun.partner() != partner.id()) {
									throw new JsonSyntaxException(neispravanObracunPoruka);
								}
								if (noviObracun.jelo()) {
									if (!jelovniciPartneroveKuhinje.containsKey(noviObracun.id())) {
										throw new JsonSyntaxException(neispravanJelovnik);
									}
								} else {
									if (!this.kartaPica.containsKey(noviObracun.id())) {
										throw new JsonSyntaxException(neispravnaKartaPicaPoruka);
									}
								}

							}
							this.brava.lock();
							this.ucitajObracuneIzDatoteke();
							
							for (var noviObracun : noviObracuni) {
								this.obracuni.add(noviObracun);
							}
							this.spremiDatotekuObracuna();
							this.brava.unlock();

							if (saljiNaRest) {
								var json = gson.toJson(noviObracuni);
						        if (posaljiNaRESTPOST(this.restAdresa + "/obracun", json, 201)) {
						        	out.println("OK");
						        	
						        }
						        else {
						        	out.println(restZahtjevNeuspjesan);
						        }
							}
							else {
								out.println("OK");

							}

						} else {
							out.println(neispravanObracunPoruka);
						}

					} else {
						out.println(nePostojiJelovnikSVrstomKuhinjePoruka);
					}

				} else {
					out.println(partnerNePostojiIliNeispravanKodPoruka);
				}

			} catch (NumberFormatException greska) {
				out.println(neispravanFormatPoruka);
			} catch (JsonSyntaxException greska) {
				if (greska.getMessage() != null && greska.getMessage().startsWith("ERROR")) {
					out.println(greska.getMessage());
				} else {
					out.println(neispravanObracunPoruka);
				}

			} finally {
				if (this.brava.isLocked() && this.brava.isHeldByCurrentThread()) {
					this.brava.unlock();
				}
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}
	
	/**
	 * Šalje POST metodom na adresu
	 * @param adresa - kamo se šalje
	 * @param json - tijelo zahtjeva
	 * @param ocekivaniOdgovor - očekivani HTTP status
	 * @return true ako je poslano i vraćen očekivani odgovor, inače false
	 */
	public boolean posaljiNaRESTPOST(String adresa, String json, int ocekivaniOdgovor) {
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest zahtjev;
			zahtjev = HttpRequest.newBuilder()
			        .uri(new URI(adresa))
			        .header("Content-Type", "application/json")
			        .POST(HttpRequest.BodyPublishers.ofString(json))
			        .build();
	        HttpResponse<Void> response = client.send(zahtjev, HttpResponse.BodyHandlers.discarding());
	        if (response.statusCode() == ocekivaniOdgovor) {
	        	return true;
	        }
	        return false;

		} catch (URISyntaxException | InterruptedException | IOException e) {
			return false;
		}
	}

	/**
	 * Obrađuje komandu KARTAPIĆA. Provjerava postoji li partner s ID-om i odgovara
	 * li sigurnosni kod. Ako da, vraća polje karti pića.
	 * 
	 * @param neispravanFormatPoruka                 - poruka ako nije dovoljan broj
	 *                                               argumenata
	 * @param partnerNePostojiIliNeispravanKodPoruka - poruka ako partner ne postoji
	 *                                               ili sigurnosni kod ne odgovara
	 * @param out                                    - objekt za zapisivanje na neki
	 *                                               izlaz
	 * @param komanda                                - originalna komanda
	 */
	public void obradiKomanduKARTAPICA(String neispravanFormatPoruka, String partnerNePostojiIliNeispravanKodPoruka,
			PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			try {
				var id = Integer.parseInt(dijeloviKomande[1]);
				var sigurnosniKod = dijeloviKomande[2].replace("\n", "");
				if (this.partneri.containsKey(id) && this.partneri.get(id).sigurnosniKod().equals(sigurnosniKod)) {
					out.println("OK");

					var gson = new Gson();
					gson.toJson(this.kartaPica.values(), out);
				} else {
					out.println(partnerNePostojiIliNeispravanKodPoruka);
				}

			} catch (NumberFormatException greska) {
				out.println(neispravanFormatPoruka);
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * Obrađuje komandu JELOVNIK. Provjerava postoji li partner i odgovara li
	 * sigurnosni kod te postoji li jelovnik za njegovu kuhinju.
	 * 
	 * @param neispravanFormatPoruka                 - poruka za neispravan format
	 *                                               komande
	 * @param partnerNePostojiIliNeispravanKodPoruka - poruka ako nema partnera s
	 *                                               dobivenim ID-om ili sigurnosni
	 *                                               kod ne odgovara
	 * @param nePostojiJelovnikSVrstomKuhinjePoruka  - poruka ako je partner
	 *                                               registriran za nepostojeću
	 *                                               kuhinju
	 * @param out                                    - objekt za zapis na izlaz
	 * @param komanda                                - originalna komanda
	 */
	public void obradiKomanduJELOVNIK(String neispravanFormatPoruka, String partnerNePostojiIliNeispravanKodPoruka,
			String nePostojiJelovnikSVrstomKuhinjePoruka, PrintWriter out, String komanda) {
		var dijeloviKomande = komanda.split(" ");
		if (dijeloviKomande.length == 3) {
			try {
				var id = Integer.parseInt(dijeloviKomande[1]);
				var sigurnosniKod = dijeloviKomande[2].replace("\n", "");
				if (this.partneri.containsKey(id) && this.partneri.get(id).sigurnosniKod().equals(sigurnosniKod)) {
					var vrstaKuhinje = this.partneri.get(id).vrstaKuhinje();
					var partnerovJelovnik = this.jelovnici.get(vrstaKuhinje);
					if (partnerovJelovnik != null) {
						out.println("OK");
						var gson = new Gson();
						gson.toJson(partnerovJelovnik.values(), out);
					} else {
						out.println(nePostojiJelovnikSVrstomKuhinjePoruka);
					}

				} else {
					out.println(partnerNePostojiIliNeispravanKodPoruka);
				}

			} catch (NumberFormatException greska) {
				out.println(neispravanFormatPoruka);
			} catch (JsonSyntaxException greska) {
			}

		} else {
			out.println(neispravanFormatPoruka);
		}
	}

	/**
	 * POkreće poslužitelja za kraj rada. Kreira utičnicu na vratima
	 * {@link #mreznaVrataKraj}. Radi u jednodretvenom načinu rada. Kad dođe
	 * zahtjev, obrađuje metodu {@link #obradiKraj(Socket)}
	 */
	public void pokreniPosluziteljKraj() {
		try (ServerSocket ss = new ServerSocket(this.mreznaVrataKraj, this.brojCekaca)) {
			while (!this.kraj.get() && !Thread.interrupted()) {
				var mreznaUticnica = ss.accept();
				this.obradiKraj(mreznaUticnica);
			}
			ss.close();

		} catch (IOException e) {
		}
	}

	/**
	 * Završava rad sustava ako je dobivena komanda KRAJ sa kodom {@link #kodZaKraj}
	 * i komanda dolazi sa iste IP adrese na kojoj je poslužitelj pokrenut.
	 * 
	 * @param mreznaUticnica - uticnica s koje je dobivena komanda
	 * @return kad je funkcija gotova
	 */
	public Boolean obradiKraj(Socket mreznaUticnica) {
		var neispravanFormatPoruka = "ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj";
		
		var neispravanKodAdminTvrtkePoruka = "ERROR 12 – Pogrešan kodZaAdminTvrtke";
		var pogresnaPromjenaPauzeIliStartaPoruka = "ERROR 13 – Pogrešna promjena pauze ili starta";
		var tvrtkaSeNeMozeZatvoritiPoruka = "ERROR 14 – Barem jedan partner nije završio rad";
		var posluziteljZaPartnereUPauziPoruka = "ERROR 15 – Poslužitelj za partnere u pauzi";
		var prekidSpavanjaDretvePoruke = "ERROR 16 – Prekid spavanja dretve";
		var neispravanDioPosluzitelja = "ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj";
		var restZahtjevNeuspjesanPoruka = "ERROR 17 – RESTful zahtjev nije uspješan";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"), true);
			String linija = in.readLine();
			mreznaUticnica.shutdownInput();
			var uzorak = Pattern.compile("KRAJ " + this.kodZaKraj);
			var podudaranje = uzorak.matcher(linija);
			if (podudaranje.find()) {
				obradiKomanduKRAJ(linija, neispravanFormatPoruka, tvrtkaSeNeMozeZatvoritiPoruka,restZahtjevNeuspjesanPoruka,  out, true);
			}

			else if (linija.startsWith("KRAJWS")) {
				obradiKomanduKRAJ(linija, neispravanFormatPoruka, tvrtkaSeNeMozeZatvoritiPoruka, restZahtjevNeuspjesanPoruka, out, false);

			} else if (linija.startsWith("STATUS")) {
				obradiKomanduStatus(neispravanFormatPoruka, neispravanKodAdminTvrtkePoruka, neispravanDioPosluzitelja,
						out, linija);

			} else if (linija.startsWith("PAUZA")) {
				obradiKomanduPAUZAiliSTART(neispravanFormatPoruka, neispravanKodAdminTvrtkePoruka,
						neispravanDioPosluzitelja,pogresnaPromjenaPauzeIliStartaPoruka, out, linija, this.pauzaStatus);
			} else if (linija.startsWith("START")) {
				obradiKomanduPAUZAiliSTART(neispravanFormatPoruka, neispravanKodAdminTvrtkePoruka,
						neispravanDioPosluzitelja,pogresnaPromjenaPauzeIliStartaPoruka, out, linija, this.aktivanStatus);
			} else if (linija.startsWith("SPAVA")) {
				obradiKomanduSPAVA(neispravanFormatPoruka, neispravanKodAdminTvrtkePoruka, prekidSpavanjaDretvePoruke, out, linija);
			} else if (linija.startsWith("OSVJEŽI")) {
				obradiKomanduOSVJEZI(neispravanFormatPoruka, neispravanKodAdminTvrtkePoruka,posluziteljZaPartnereUPauziPoruka, out, linija);

			}

			else {
				out.println(neispravanFormatPoruka);
			}

			mreznaUticnica.shutdownOutput();
			mreznaUticnica.close();
		} catch (Exception e) {

		}
		return Boolean.TRUE;
	}
	
	/**
	 * Obrađuje komandu za KRAJ na poslužitelju za kraj rada
	 * @param linija - komanda
	 * @param neispravanFormatPoruka - ako je format neispravan
	 * @param tvrtkaSeNeMozeZatvoritiPoruka - ako barem jedan od aktivnih partnera pošalje ERROR
	 * @param restZahtjevNeuspjesanPoruka - ako REST servis nije odgovorio sa OK
	 * @param out - pisač
	 * @param saljiNaREST - je li potrebno slati na REST ili ne
	 */
	public void obradiKomanduKRAJ(String linija, String neispravanFormatPoruka, String tvrtkaSeNeMozeZatvoritiPoruka,
			String restZahtjevNeuspjesanPoruka, PrintWriter out, boolean saljiNaREST) {
		var dijelovi = linija.split(" ");
		if (dijelovi.length == 2) {
			var kod = dijelovi[1];
			if (this.kodZaKraj.equals(kod)) {
				var moguPrestatiSRadom = this.posaljiKrajAktivnimPartnerima();
				if (moguPrestatiSRadom) {
					if (saljiNaREST) {
				        if (posaljiNaRESTHEAD(this.restAdresa + "/kraj/info", 200)) {
				        	out.println("OK");
							this.kraj.set(true);
				        }
				        else {
				        	out.println(restZahtjevNeuspjesanPoruka);
				        }
					}
					else {
						out.println("OK");
						this.kraj.set(true);

					}
					
				} else {
					out.println(tvrtkaSeNeMozeZatvoritiPoruka);
				}
			} else {
				out.println(neispravanFormatPoruka);
			}
		} else {
			out.println(neispravanFormatPoruka);
		}

	}
	
	/**
	 * Šalje HEAD metodom na adresu
	 * @param adresa - kamo se šalje
	 * @param ocekivaniOdgovor - očekivani HTTP status
	 * @return true ako je poslano i vraćen očekivani odgovor, inače false
	 */
	public boolean posaljiNaRESTHEAD(String adresa, int ocekivaniOdgovor) {
		
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest zahtjev;
			zahtjev = HttpRequest.newBuilder()
			        .uri(new URI(adresa))
			        .HEAD()
			        .build();
		   var odgovor = client.send(zahtjev, HttpResponse.BodyHandlers.discarding());
		   if (odgovor.statusCode() == ocekivaniOdgovor) {
			   return true;
			    
		   }
		    return false;
		    


		} catch (URISyntaxException | InterruptedException | IOException e) {
			return false;
		}
	}
	/**
	 * Šalje KRAJ kodZaKraj aktivnim partnerima.
	 * @return true ako su svi aktivni poslali OK, false ako barem jedan nije
	 */
	private boolean posaljiKrajAktivnimPartnerima() {
		for (var partner : this.partneri.values()) {
			try (var mreznaUticnica = new Socket(partner.adresa(), partner.mreznaVrataKraj());
					BufferedReader in = new BufferedReader(
							new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
					PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"),
							true)) {
				out.println("KRAJ " + this.kodZaKraj);
				var odgovor = in.readLine();
				if (odgovor.startsWith("ERROR")) {
					return false;
				}

			} catch (UnknownHostException e) {
				continue;
			} catch (IOException e) {
				continue;
			}
		}
		return true;

	}

	/**
	 * Ucitaj konfiguraciju.
	 *
	 * @param nazivDatoteke naziv datoteke
	 * @return true, ako je uspješno učitavanje konfiguracije
	 */
	public boolean ucitajKonfiguraciju(String nazivDatoteke) {
		try {
			this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
			return true;
		} catch (NeispravnaKonfiguracija ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
