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
 * Klasa za slanje web socket poruka svima na /ws/partneri
 */
@ServerEndpoint("/ws/partneri")
public class WebSocketPartneri {
	
	/**
	 * Red poruka
	 */
  static Queue<Session> queue = new ConcurrentLinkedQueue<>();
  
  /**
   * Šalje svima poruku koji su pretplaćeni
   * @param poruka - tekstualna poruka
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
   * @param session
   * @param reason
   */
  @OnClose
  public void closedConnection(Session session, CloseReason reason) {
    queue.remove(session);
  }
  
  /**
   * Koristi {@link #send(String)}
   * @param session
   * @param poruka
   */
  @OnMessage
  public void Message(Session session, String poruka) {
    WebSocketPartneri.send(poruka);
  }
  
  /**
   * Miče sesiju ako je došlo do greške između veze
   * @param session
   * @param t
   */
  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
  }
}
