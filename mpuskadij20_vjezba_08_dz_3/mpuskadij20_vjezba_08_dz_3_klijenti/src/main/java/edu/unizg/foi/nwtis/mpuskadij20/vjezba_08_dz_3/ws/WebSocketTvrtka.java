package edu.unizg.foi.nwtis.mpuskadij20.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Klasa za slanje i primanje poruka na /ws/tvrtka
 */
@ServerEndpoint("/ws/tvrtka")
public class WebSocketTvrtka {
	
	/**
	 * Red sesija
	 */
  static Queue<Session> queue = new ConcurrentLinkedQueue<>();
  
  /**
   * Šalje poruku svim sesijama u redu
   * @param poruka
   */

  public static void send(String poruka) {
    try {
      for (Session session : queue) {
        if (session.isOpen()) {
          System.out.println("Šaljem poruku: " + poruka);
          session.getBasicRemote().sendText(poruka);
        }
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
  
  /**
   * Dodaje sesiju u red
   * @param session
   * @param conf
   */
  @OnOpen
  public void openConnection(Session session, EndpointConfig conf) {
    queue.add(session);
  }
  
  /**
   * Izbacuje sesiju iz reda
   */
  @OnClose
  public void closedConnection(Session session, CloseReason reason) {
    queue.remove(session);
  }
  /**
   * Šalje poruku svim sesijama
   * @param session
   * @param poruka
   */
  @OnMessage
  public void Message(Session session, String poruka) {
    System.out.println("Primljena poruka: " + poruka);
    WebSocketTvrtka.send(poruka);
  }
  /**
   * Izbacuje sesiju ako je došlo do greške
   * @param session
   * @param t
   */
  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
    System.out.println("Zatvorena veza zbog pogreške.");
  }
}
