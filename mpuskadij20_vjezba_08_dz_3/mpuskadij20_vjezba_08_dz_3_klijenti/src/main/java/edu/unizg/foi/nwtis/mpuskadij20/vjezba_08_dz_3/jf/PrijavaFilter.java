package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf;

import java.io.IOException;
import java.sql.Timestamp;

import edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Filter za svaki HTTP zahtjev
 */
@WebFilter("/*")
public class PrijavaFilter implements Filter {
	
	/**
	 * Facade za kreiranje zapisa
	 */
	@Inject
	private ZapisiFacade zapisiFacade;
	
	/**
	 * Provjerava ako je prijava korisnika uspješna. ako je, kreira zapis
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest zahtjev = (HttpServletRequest) request;
        HttpSession session = zahtjev.getSession(false);

        if (session != null && zahtjev.getUserPrincipal() != null) {
            if (session.getAttribute("zapisKreiran") == null) {
                String korisnik = zahtjev.getUserPrincipal().getName();
                
                var vrijeme = new Timestamp(System.currentTimeMillis());
                
                this.zapisiFacade.kreirajZapis(korisnik, zahtjev.getRemoteHost(), zahtjev.getRemoteAddr(), "Prijava korisnika", vrijeme);
                
               
                session.setAttribute("zapisKreiran", Boolean.TRUE);
            }
        }

        chain.doFilter(request, response);
		
	}

}
