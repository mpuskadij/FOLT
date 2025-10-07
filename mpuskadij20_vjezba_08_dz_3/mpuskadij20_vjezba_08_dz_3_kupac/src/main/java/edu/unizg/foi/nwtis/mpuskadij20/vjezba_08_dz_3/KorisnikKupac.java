package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa koja predstavlja kupca koji radi s tvrtkom i partnerima
 */
public class KorisnikKupac {
  /**
   * Atribut za učitavanja konfiguracije
   */
  private Konfiguracija konfig;

  /**
   * Podatak iz kojeg je direktorija kupac pokrenut
   */
  private final String radniDirektorij = System.getProperty("user.dir") + "/";

  /**
   * Glavna metoda programa
   * 
   * @param args - duljina mora biti 2 - prvi član je naziv konfiguracijske datoteke, drugi je naziv
   *        csv datoteke
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Broj argumenata mora biti 2!");
      return;
    }
    var program = new KorisnikKupac();
    var nazivDatoteke = args[0];
    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    var komande = args[1];
    if (!komande.endsWith(".csv"))
      return;
    program.izvrsiKomande(args[1]);


  }

  /**
   * Metoda koja čita redak po redak .csv datoteke te pokreće
   * {@link #posaljiKomandu(String, String, int, long, String)}
   * 
   * @param datotekaPodataka - datoteka gdje se nalazi komande za poslati partneru i tvrtci
   */
  public void izvrsiKomande(String datotekaPodataka) {
    var putanja = Path.of(this.radniDirektorij, datotekaPodataka);
    if (!Files.exists(putanja) || !Files.isReadable(putanja) && !Files.isRegularFile(putanja)) {
      return;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(datotekaPodataka))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] dijeloviKomande = line.split(";");
        if (dijeloviKomande.length == 5) {
          var korisnik = dijeloviKomande[0];
          var adresa = dijeloviKomande[1];
          var port = Integer.parseInt(dijeloviKomande[2]);
          var pauza = Long.parseLong(dijeloviKomande[3]);
          var komandaZaPoslati = dijeloviKomande[4];
          var dijeloviKomandaKupac = komandaZaPoslati.split(" ");
          if (dijeloviKomandaKupac.length > 1) {
            if (!dijeloviKomandaKupac[1].equals(korisnik))
              continue;

          }
          this.posaljiKomandu(korisnik, adresa, port, pauza, komandaZaPoslati);
        }
      }
    } catch (FileNotFoundException e) {

      return;
    } catch (IOException e) {

      return;
    } catch (NumberFormatException e) {
      return;
    }
  }

  /**
   * Metoda spava određeno vrijeme, šalje komandu i ispisuje odgovore svake komande
   * 
   * @param korisnik - naziv korisnika pod kojim se komande izvršavaju
   * @param adresa - adresa partnera ili poslužitelja
   * @param port - port na kojem je poslužitelj za prijem zahtjeva kupaca pokrenut ili tvrtka
   * @param pauza - broj milisekudni koliko spava dretva prije slanja u utičnicu
   * @param komandaZaPoslati - sama komanda za poslati
   */
  private void posaljiKomandu(String korisnik, String adresa, int port, long pauza,
      String komandaZaPoslati) {
    try (var mreznaUticnica = new Socket(adresa, port);
        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"), true)) {
      try {
        Thread.sleep(pauza);
      } catch (InterruptedException e) {
      } finally {
        out.println(komandaZaPoslati);
      }

    } catch (UnknownHostException e) {

    } catch (IOException e) {
    }

  }

  /**
   * Učitava konfiguracijsku datoteku
   * 
   * @param nazivDatoteke - naziv konfiguracijske datoteke
   * @return true ako je datoteka uspješno učitana, false inače
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig =
          KonfiguracijaApstraktna.preuzmiKonfiguraciju(this.radniDirektorij + nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
