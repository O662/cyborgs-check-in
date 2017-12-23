/**
 * 
 */
package org.cyborgs3335.checkin.messenger;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Messenger composed of multiple messengers, such as an HttpMessenger and LocalMessenger.
 * Another CompositeMessenger could also be used.
 *
 * @author Brian Macy
 *
 */
public class CompositeMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(CompositeMessenger.class.getName());

  private final IMessenger messenger1;

  private final IMessenger messenger2;

  /**
   * Constructor specifying two messengers. The methods call the messengers in order.
   * @param firstMessenger the first messenger
   * @param secondMessenger the second messenger
   */
  public CompositeMessenger(IMessenger firstMessenger, IMessenger secondMessenger) {
    messenger1 = firstMessenger;
    messenger2 = secondMessenger;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkIn(long)
   */
  @Override
  public RequestResponse checkIn(long id) throws IOException {
    RequestResponse response1 = messenger1.checkIn(id);
    if (!response1.equals(RequestResponse.Ok)) {
      return response1;
    }
    RequestResponse response2 = messenger2.checkIn(id);
    return response2;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOut(long)
   */
  @Override
  public RequestResponse checkOut(long id) throws IOException {
    RequestResponse response1 = messenger1.checkOut(id);
    if (!response1.equals(RequestResponse.Ok)) {
      return response1;
    }
    RequestResponse response2 = messenger2.checkOut(id);
    return response2;
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
    IMessenger m1 = new HttpMessenger("http://localhost:8080/attendance/request");
    IMessenger m2 = new LocalMessenger();
    IMessenger m = new CompositeMessenger(m1, m2);
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
