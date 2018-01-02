package org.cyborgs3335.checkin.server.http;

import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;
import org.cyborgs3335.checkin.messenger.MessengerTestBase;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JTestHttpMessenger extends MessengerTestBase {

  private static final Logger LOG = Logger.getLogger(JTestHttpMessenger.class.getName());

  private static final String SERVER_URL = "http://localhost:8080/attendance";

  private static Server server = null;

  @BeforeClass
  public static final void setUp() throws Exception {
    // Start the jetty web server
    if (server == null) {
      SimpleJettyServer sjs = new SimpleJettyServer("/tmp/check-in-store.db");
      server = new Server(8080);
      server.setHandler(new CheckInHandler(sjs.getDataStore()));
      server.start();
      server.dumpStdErr();
      //server.join();
    }
  }

  @AfterClass
  public static final void tearDown() throws Exception {
    // Stop the jetty web server
    if (server != null) {
      server.stop();
    }
  }

  private static void checkResponseOk(RequestResponse response, String prefix) {
    switch (response) {
      case Ok:
        LOG.info(prefix + ": received ok");
        break;
      case UnknownId:
        LOG.info(prefix + ": received Unknown ID; exiting...");
        fail();
        break;
      case FailedRequest:
      default:
        LOG.info(prefix + ": failed request; aborting...");
        fail();
    }
  }

  @Override
  public IMessenger getNewMessenger() {
    return new HttpMessenger(SERVER_URL);
  }

}
