package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3;


import java.util.Map;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * Definira kako su lozinke hashiranje u bazi podataka (nisu)
 */
public class NoPasswordHash implements Pbkdf2PasswordHash {

	/**
	 * Vraća sirovu lozinku
	 */
  @Override
  public String generate(char[] password) {
    return password.toString();
  }
  
  /**
   * Provjerava unesenu i stvarnu lozinku korisnika
   */

  @Override
  public boolean verify(char[] password, String hashedPassword) {
    var npassword = new String(password);
    if (npassword.trim().compareTo(hashedPassword.trim()) == 0) {
      return true;
    }
    return false;
  }
  /**
   * Inicijalizacija objekta
   */
  @Override
  public void initialize(Map<String, String> parameters) {}

}
