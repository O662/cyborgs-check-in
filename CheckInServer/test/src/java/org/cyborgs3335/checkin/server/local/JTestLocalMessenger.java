package org.cyborgs3335.checkin.server.local;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;
import org.cyborgs3335.checkin.messenger.MessengerTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JTestLocalMessenger extends MessengerTestBase {

  private static final Logger LOG = Logger.getLogger(JTestLocalMessenger.class.getName());

  private static LocalMessenger messenger = null;

  @BeforeClass
  public static final void setUp() throws IOException {
    if (messenger == null) {
      messenger = new LocalMessenger("/tmp/check-in-server.db");
    }
  }

  @AfterClass
  public static final void tearDown() throws Exception {
    if (messenger != null) {
      messenger.close();
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
    return messenger;
  }

}
