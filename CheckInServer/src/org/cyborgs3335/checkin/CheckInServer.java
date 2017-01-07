package org.cyborgs3335.checkin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.cyborgs3335.checkin.CheckInEvent.Status;

import sun.misc.JavaAWTAccess;

/**
 * Singleton check-in server using a map in memory as the "database".  The map
 * can be persisted to a file, and can be loaded from a file.
 *
 * @author brian
 *
 */
public class CheckInServer {

  public static final String ACTIVITY_PROPERTY = "ACTIVITY_PROPERTY";

  public static final String DB_ATTENDANCE_RECORDS = "attendance-records.db";

  private final Map<Long, AttendanceRecord> map = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private CheckInActivity activity = null;

  private static class Singleton {
    private static final CheckInServer INSTANCE = new CheckInServer();
  }

  private CheckInServer() {
    //addDefaultUsers();
  }

  /*package */void addDefaultUsers() {
    char first = 'a';
    char last = 'z';
    for (long id = 0; id < 10; id++) {
      CheckInEvent event = new CheckInEvent(Status.CheckedOut, 0);
      Person person = new Person(id, ""+first+first+first, ""+last+last+last);
      AttendanceRecord record = new AttendanceRecord(person);
      record.getEventList().add(event);
      map.put(id, record);
      first++;
      last--;
    }

    // Card UID: 94 18 60 EC value 941860EC id 2484625644
    long id = 2484625644L;
    CheckInEvent event = new CheckInEvent(Status.CheckedOut, 0);
    Person person = new Person(id, "Blue1", "Token1");
    AttendanceRecord record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);

    // Card UID: 94 6C 56 EC value 946C56EC id 2490128108
    id = 2490128108L;
    event = new CheckInEvent(Status.CheckedOut, 0);
    person = new Person(id, "Blue2", "Token2");
    record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);

    // Card UID: 62 21 E4 D5 value 6221E4D5 id 1646388437
    id = 1646388437L;
    event = new CheckInEvent(Status.CheckedOut, 0);
    person = new Person(id, "White1", "Card1");
    record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);
  }

  public Person addUser(String firstName, String lastName) {
    long id = getNewId();
    CheckInEvent event = new CheckInEvent(Status.CheckedOut, 0);
    Person person = new Person(id, firstName, lastName);
    AttendanceRecord record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);
    return person;
  }

  private long getNewId() {
    Random random = new Random();
    for (int count = 0; count < 100; count++) {
      long id = Math.abs(random.nextLong());
      if (!map.containsKey(id)) {
        return id;
      }
    }
    throw new IllegalStateException("Cannot find a new unique ID!");
  }

  public Person findPerson(String firstName, String lastName) {
    Person person = null;
    for (Long id : map.keySet()) {
      Person p = map.get(id).getPerson();
      //if (p.getFirstName().equals(firstName) && p.getLastName().equals(lastName)) {
      if (p.getFirstName().equalsIgnoreCase(firstName) && p.getLastName().equalsIgnoreCase(lastName)) {
        person = p;
        break;
      }
    }
    return person;
  }

  /**
   * Return the check-in server instance.
   * @return check-in server instance
   */
  public static CheckInServer getInstance() {
    return Singleton.INSTANCE;
  }

  /**
   * Accept a check in or check out.
   * @param id id of user to check in or out
   * @return true if check in, false if check out
   * @throws UnknownUserException if the user is unknown to the server
   */
  public boolean accept(long id) throws UnknownUserException {
    boolean checkedIn = false;
    synchronized (map) {
      if (!map.containsKey(id)) {
        throw new UnknownUserException("Unknown user id " + id);
      }
      CheckInEvent event = map.get(id).getLastEvent();
      long timeStamp = System.currentTimeMillis();
      Status status;
      switch (event.getStatus()) {
      case CheckedIn:
        status = Status.CheckedOut;
        checkedIn = false;
        break;
      case CheckedOut:
      default:
        status = Status.CheckedIn;
        checkedIn = true;
        break;
      }
      map.get(id).getEventList().add(new CheckInEvent(status, timeStamp));
    }
    return checkedIn;
  }

  /**
   * Get the current activity.
   * @return current activity
   */
  public CheckInActivity getActivity() {
    return activity;
  }

  /**
   * Set the current activity; e.g., name, start time, end time, ...
   * @param activity current activity
   */
  public void setActivity(CheckInActivity activity) {
    CheckInActivity oldActivity = this.activity;
    this.activity = activity;
    pcs.firePropertyChange(ACTIVITY_PROPERTY, oldActivity, activity);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * Load "database" from filesystem.
   * @param path directory containing "database"
   * @throws IOException on I/O error loading "database" from filesystem 
   */
  /*package*/void load(String path) throws IOException {
    File loadDir = new File(path);
    if (!loadDir.isDirectory()) {
      throw new IOException("Path " + path + " must be a directory!");
    }
    loadAttendanceRecords(path + File.separator + DB_ATTENDANCE_RECORDS);
  }

  private void loadAttendanceRecords(String path) {
    ObjectInputStream ois = null;
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(path);
      ois = new ObjectInputStream(fin);
      Object o = ois.readObject();
      if (o instanceof CheckInActivity) {
        activity = (CheckInActivity) o;
      } else {
        throw new IllegalStateException("Expected to read CheckInActivity."
            + "However, encountered " + o.getClass() + " instead.");
      }
      o = ois.readObject();
      if (o instanceof Map) {
        Map<Long, AttendanceRecord> inmap = (Map<Long, AttendanceRecord>) o;
        synchronized (map) {
          map.putAll(inmap);
        }
      } else {
        throw new IllegalStateException("Expected to read attendance record Map<Long, AttendanceRecord>."
            + "However, encountered " + o.getClass() + " instead.");
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (ois != null) {
        try {
          ois .close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  /**
   * Dump "database" to filesystem.
   * @param path directory to contain "database"
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  /*package*/void dump(String path) throws IOException {
    File dumpDir = new File(path);
    if (!dumpDir.isDirectory()) {
      throw new IOException("Path " + path + " must be a directory!");
    }
    dumpAttendanceRecords(path + File.separator + DB_ATTENDANCE_RECORDS);
  }

  private void dumpAttendanceRecords(String path) {
    ObjectOutputStream oos = null;
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(path);
      oos = new ObjectOutputStream(fout);
      oos.writeObject(activity);
      synchronized (map) {
        oos.writeObject(map);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (oos != null) {
        try {
          oos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  public Set<Long> getIdSet() {
    return map.keySet();
  }

  public AttendanceRecord getAttendanceRecord(long id) {
    return map.get(id);
  }

  /**
   * Print the last check-in event for each attendance record.
   */
  public void print() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    if (activity != null) {
      activity.print(dateFormat);
    }
    synchronized (map) {
      for (Long id : map.keySet()) {
        AttendanceRecord record = map.get(id);
        ArrayList<CheckInEvent> list = record.getEventList();
        CheckInEvent event = list.get(list.size()-1);
        System.out.println("id " + id + " name " + record.getPerson() + " check-in "
            + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())));
      }
    }
  }
}