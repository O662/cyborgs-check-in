package org.cyborgs3335.checkin.server.http;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

public class SimpleJettyServer {

  public SimpleJettyServer() {
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args) throws Exception {
    Server server = new Server(8080);
    server.setHandler(new CheckInHandler());
    server.start();
    server.dumpStdErr();
    server.join();
  }
}