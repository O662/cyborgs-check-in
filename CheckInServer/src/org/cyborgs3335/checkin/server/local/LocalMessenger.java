/**
 * 
 */
package org.cyborgs3335.checkin.server.local;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;


/**
 * Messenger to local memory in same application.
 * @author Brian Macy
 *
 */
public class LocalMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(LocalMessenger.class.getName());

  private final String databasePath;

  private final CheckInServer server;

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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
  public RequestResponse checkIn(long id) throws IOException, UnknownUserException {
    return id > 0 ? RequestResponse.Ok : RequestResponse.UnknownId;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOut(long)
   */
  @Override
  public RequestResponse checkOut(long id) throws IOException, UnknownUserException {
    return id > 0 ? RequestResponse.Ok : RequestResponse.UnknownId;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOutAll()
   */
  @Override
  public RequestResponse checkOutAll() throws IOException {
    server.checkOutAll();
    return RequestResponse.Ok;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#toggleCheckInStatus(long)
   */
  @Override
  public Status toggleCheckInStatus(long id) throws IOException, UnknownUserException {
    return server.accept(id) ? Status.CheckedIn : Status.CheckedOut;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getCheckInStatus(long)
   */
  @Override
  public Status getCheckInStatus(long id) throws IOException, UnknownUserException {
    AttendanceRecord record = server.getAttendanceRecord(id);
    if (record == null) {
      throw new IOException("No attendance record found for id " + id);
    }
    return record.getLastEvent().getStatus();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#findPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person findPerson(String firstName, String lastName) {
    return server.findPerson(firstName, lastName);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person addPerson(String firstName, String lastName) {
    return server.addUser(firstName, lastName);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) {
    CheckInActivity oldActivity = getActivity();
    server.setActivity(activity);
    pcs.firePropertyChange(ACTIVITY_PROPERTY, oldActivity, activity);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getActivity()
   */
  @Override
  public CheckInActivity getActivity() {
    return server.getActivity();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#lastCheckInEventToString()
   */
  @Override
  public String lastCheckInEventToString() {
    return server.printToString();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEventsSorted()
   */
  @Override
  public List<PersonCheckInEvent> getLastCheckInEventsSorted() {
    ArrayList<PersonCheckInEvent> recordList = new ArrayList<PersonCheckInEvent>(server.getIdSet().size());
    for (Long id : server.getIdSet()) {
      AttendanceRecord record = server.getAttendanceRecord(id);
      if (id != record.getPerson().getId()) {
        LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
            + " for attendance record from person " + record.getPerson());
      }
      recordList.add(new PersonCheckInEvent(record.getPerson(), record.getLastEvent()));
    }
    Collections.sort(recordList, new Comparator<PersonCheckInEvent>() {

      @Override
      public int compare(PersonCheckInEvent o1, PersonCheckInEvent o2) {
        String o1Name = o1.getPerson().getLastName() + " " + o1.getPerson().getFirstName();
        String o2Name = o2.getPerson().getLastName() + " " + o2.getPerson().getFirstName();
        return o1Name.compareToIgnoreCase(o2Name);
      }
    });
    return recordList;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEvent(long)
   */
  @Override
  public CheckInEvent getLastCheckInEvent(long id)
      throws IOException, UnknownUserException {
    AttendanceRecord record = server.getAttendanceRecord(id);
    if (record == null) {
      throw new IOException("No attendance record found for id " + id);
    }
    if (id != record.getPerson().getId()) {
      LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
          + " for attendance record from person " + record.getPerson());
    }
    return record.getLastEvent();
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
  public static void main(String[] args) throws IOException, UnknownUserException {
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
