/**
 * 
 */
package org.cyborgs3335.checkin.server.local;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.messenger.IMessenger;


/**
 * Messenger to local memory in same application.
 * @author Brian Macy
 *
 */
public class LocalMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(LocalMessenger.class.getName());

  private final String databasePath;

  private final CheckInServer server;

  public LocalMessenger(String databasePath) throws IOException {
    this.databasePath = databasePath;
    server = CheckInServer.getInstance();
    File dir = new File(databasePath);
    if (dir.exists()) {
      LOG.info("Loading attendance records from " + databasePath);
      server.load(databasePath);
    } else {
      LOG.info("No attendance records found at path " + databasePath
          + ". Creating directory for saving database.");
      boolean success = dir.mkdirs();
      if (!success) {
        throw new RuntimeException("Could not create directory " + databasePath
            + "for saving database!");
      }
    }
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

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getCheckInStatus(long)
   */
  @Override
  public Status getCheckInStatus(long id) throws IOException {
    AttendanceRecord record = server.getAttendanceRecord(id);
    if (record == null) {
      throw new IOException("No attendance record found for id " + id);
    }
    return record.getLastEvent().getStatus();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) {
    server.setActivity(activity);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getActivity()
   */
  @Override
  public CheckInActivity getActivity() {
    return server.getActivity();
  }

  public void print() {
    server.print();
  }

  // TODO refactor name, since this returns last event for each id; add to interface
  public String printToString() {
    return server.printToString();
  }

  public void save() throws IOException {
    // TODO replace with queue and auto-save
    server.dump(databasePath);
  }

  public void save(String path) throws IOException {
    server.dump(path);
  }

  public void saveCsv(String path) throws IOException {
    server.dumpCsv(path);
  }

  public void saveHoursByDayCsv(String path) throws IOException {
    server.dumpHoursByDayCsv(path);
  }

  public void saveJson(String path) throws IOException {
    server.dumpJson(path);
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
    IMessenger m = new LocalMessenger("/tmp/check-in-server.db");
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
