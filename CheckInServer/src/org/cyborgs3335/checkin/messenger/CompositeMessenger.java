/**
 * 
 */
package org.cyborgs3335.checkin.messenger;

import java.io.IOException;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.server.local.LocalMessenger;


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

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getCheckInStatus(long)
   */
  @Override
  public Status getCheckInStatus(long id) throws IOException {
    Status status1 = messenger1.getCheckInStatus(id);
    Status status2 = messenger2.getCheckInStatus(id);
    if (status1.compareTo(status2) != 0) {
      throw new IllegalStateException("Status from messenger 1 (" + status1
          + ") does not match status from messenger 2 (" + status2 + ")");
    }
    return status1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) {
    messenger1.setActivity(activity);
    messenger2.setActivity(activity);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getActivity()
   */
  @Override
  public CheckInActivity getActivity() {
    CheckInActivity activity1 = messenger1.getActivity();
    CheckInActivity activity2 = messenger2.getActivity();
    if (!activity1.equals(activity2)) {
      throw new IllegalStateException("Activity from messenger 1 (" + activity1
          + ") does not match activity from messenger 2 (" + activity2 + ")");
    }
    return activity1;
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
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    IMessenger m1 = new HttpMessenger("http://localhost:8080/attendance/request");
    IMessenger m2 = new LocalMessenger("/tmp/check-in-server.db");
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
