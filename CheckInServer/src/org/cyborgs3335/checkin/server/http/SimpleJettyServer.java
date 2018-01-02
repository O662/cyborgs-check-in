package org.cyborgs3335.checkin.server.http;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;

public class SimpleJettyServer {

  private static final Logger LOG = Logger.getLogger(SimpleJettyServer.class.getName());

  private final CheckInDataStore dataStore;

  public SimpleJettyServer(String databasePath) throws IOException {
    dataStore = CheckInDataStore.getInstance();
    File dir = new File(databasePath);
    if (dir.exists()) {
      LOG.info("Loading attendance records from " + databasePath);
    } else {
      LOG.info("No attendance records found at path " + databasePath
          + ". Creating directory for saving database.");
      boolean success = dir.mkdirs();
      if (!success) {
        throw new IOException("Could not create directory " + databasePath
            + "for saving database!");
      }
    }
    dataStore.load(databasePath);
  }

  public CheckInDataStore getDataStore() {
    return dataStore;
  }

  public static void main(String[] args) throws Exception {
    SimpleJettyServer sjs = new SimpleJettyServer("/tmp/check-in-store.db");
    Server server = new Server(8080);
    server.setHandler(new CheckInHandler(sjs.getDataStore()));
    server.start();
    server.dumpStdErr();
    server.join();
  }
}