package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.jf.konfig;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

/**
 * Prati Factory uzorak dizajna za kreiranje grešaka
 */
public class UpravljanjeIznimkomFactory extends ExceptionHandlerFactory {

  private final ExceptionHandlerFactory parent;

  public UpravljanjeIznimkomFactory(ExceptionHandlerFactory parent) {
    super(parent);
    this.parent = parent;
  }

  @Override
  public ExceptionHandler getExceptionHandler() {
    return new UpravljanjeIznimkom(parent.getExceptionHandler());
  }
}

