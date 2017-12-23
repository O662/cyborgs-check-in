/**
 * 
 */
package org.cyborgs3335.checkin.messenger;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Messenger to local memory in same application.
 * @author Brian Macy
 *
 */
public class LocalMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(LocalMessenger.class.getName());

  /**
   * 
   */
  public LocalMessenger() {
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkIn(long)
   */
  @Override
  public RequestResponse checkIn(long id) throws IOException {
    return id > 0 ? RequestResponse.Ok : RequestResponse.UnknownId;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOut(long)
   */
  @Override
  public RequestResponse checkOut(long id) throws IOException {
    return id > 0 ? RequestResponse.Ok : RequestResponse.UnknownId;
  }

  private static void logResponse(RequestResponse response, String prefix) {
    switch (response) {
      case Ok:
        LOG.info(prefix + ": received ok");
        break;
      case UnknownId:
        LOG.info(prefix + ": received Unknown ID; exiting...");
        System.exit(1);
        break;
      case FailedRequest:
      default:
        LOG.info(prefix + ": failed request; aborting...");
        System.exit(2);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    IMessenger m = new LocalMessenger();
    try {
      RequestResponse response = m.checkIn(1);
      logResponse(response, "checkin");
      response = m.checkOut(1);
      logResponse(response, "checkout");
      response = m.checkIn(-1);
      logResponse(response, "checkout");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
