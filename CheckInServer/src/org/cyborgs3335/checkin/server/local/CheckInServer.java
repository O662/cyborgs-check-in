package org.cyborgs3335.checkin.server.local;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.CheckInEvent.Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * Singleton check-in server using a map in memory as the "database".  The map
 * can be persisted to a file, and can be loaded from a file.
 *
 * @author brian
 *
 */
public class CheckInServer {

  private static final Logger LOG = Logger.getLogger(CheckInServer.class.getName());

  public static final String ACTIVITY_PROPERTY = "ACTIVITY_PROPERTY";

  public static final String DB_ATTENDANCE_RECORDS = "attendance-records.db";

  public static final String JSON_ATTENDANCE_RECORDS = "attendance-records.json";

  private final Map<Long, AttendanceRecord> map = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private CheckInActivity activity = null;

  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");

  private DateFormat dateFormatCsv = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  private static class Singleton {
    private static final CheckInServer INSTANCE = new CheckInServer();
  }

  private CheckInServer() {
    //addDefaultUsers();
  }

  /*package */void addDefaultUsers() {
    char first = 'a';
    char last = 'z';
    CheckInActivity defaultActivity = (activity != null) ? activity : CheckInEvent.DEFAULT_ACTIVITY;
    for (long id = 0; id < 10; id++) {
      CheckInEvent event = new CheckInEvent(defaultActivity, Status.CheckedOut, 0);
      Person person = new Person(id, ""+first+first+first, ""+last+last+last);
      AttendanceRecord record = new AttendanceRecord(person);
      record.getEventList().add(event);
      map.put(id, record);
      first++;
      last--;
    }

    // Card UID: 94 18 60 EC value 941860EC id 2484625644
    long id = 2484625644L;
    CheckInEvent event = new CheckInEvent(defaultActivity, Status.CheckedOut, 0);
    Person person = new Person(id, "Blue1", "Token1");
    AttendanceRecord record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);

    // Card UID: 94 6C 56 EC value 946C56EC id 2490128108
    id = 2490128108L;
    event = new CheckInEvent(defaultActivity, Status.CheckedOut, 0);
    person = new Person(id, "Blue2", "Token2");
    record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);

    // Card UID: 62 21 E4 D5 value 6221E4D5 id 1646388437
    id = 1646388437L;
    event = new CheckInEvent(defaultActivity, Status.CheckedOut, 0);
    person = new Person(id, "White1", "Card1");
    record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);
  }

  public Person addUser(String firstName, String lastName) {
    long id = getNewId();
    CheckInEvent event = null;
    if (activity != null) {
      event = new CheckInEvent(activity, Status.CheckedOut, 0);
    } else {
      event = new CheckInEvent(CheckInEvent.DEFAULT_ACTIVITY, Status.CheckedOut, 0);
    }
    Person person = new Person(id, firstName, lastName);
    AttendanceRecord record = new AttendanceRecord(person);
    record.getEventList().add(event);
    map.put(id, record);
    return person;
  }

  /**
   * Add a user with a predefined ID.  A null user is returned if id already exists.
   * @param id predefined ID
   * @param firstName first name
   * @param lastName last name
   * @return added user, or null if id already exists
   */
  /* package */Person addUserWithId(long id, String firstName, String lastName) {
    if (map.containsKey(id)) {
      return null;
    }
    CheckInEvent event = null;
    if (activity != null) {
      event = new CheckInEvent(activity, Status.CheckedOut, 0);
    } else {
      event = new CheckInEvent(CheckInEvent.DEFAULT_ACTIVITY, Status.CheckedOut, 0);
    }
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
      if (activity != null) {
        map.get(id).getEventList().add(new CheckInEvent(activity, status, timeStamp));
      } else {
        map.get(id).getEventList().add(new CheckInEvent(CheckInEvent.DEFAULT_ACTIVITY, status, timeStamp));
      }
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

  /*package */void loadJson(String path) {
    // TODO implement me!!!
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

  /**
   * Dump "database" to filesystem.
   * @param path CSV file to save "database" to
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  /*package*/void dumpCsv(String path) throws IOException {
    File csvFile = new File(path);
    if (!csvFile.isFile()) {
      throw new IOException("Path " + path + " must be a file!");
    }
    dumpAttendanceRecordsAllEventsCsv2(path);
  }

  /**
   * Dump "database" hours-by-day information to filesystem.
   * @param path CSV file to save "database" to
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  /*package*/void dumpHoursByDayCsv(String path) throws IOException {
    File csvFile = new File(path);
    if (!csvFile.isFile()) {
      throw new IOException("Path " + path + " must be a file!");
    }
    dumpAttendanceRecordsHoursPerDayCsv(path);
  }

  private void dumpAttendanceRecordsCsv(String path) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(path));
      writer.write("Activity Name,Start Date,End Date\n");
      writer.write(activity.getName() + "," + dateFormat.format(activity.getStartDate())
          + "," + dateFormat.format(activity.getEndDate()) + "\n");
      writer.write("ID,First Name,Last Name,Check-In Status,Date\n");
      ArrayList<AttendanceRecord> recordList = getSortedAttendanceRecords();
      for (AttendanceRecord record : recordList) {
        ArrayList<CheckInEvent> list = record.getEventList();
        CheckInEvent event = list.get(list.size()-1);
        writer.write(record.getPerson().getId()
            + "," + record.getPerson().getFirstName()
            + "," + record.getPerson().getLastName()
            + "," + event.getStatus()
            + "," + dateFormat.format(new Date(event.getTimeStamp())) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  private void dumpAttendanceRecordsAllEventsCsv(String path) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(path));
      writer.write("Activity Name,Start Date,End Date\n");
      writer.write(activity.getName() + "," + dateFormat.format(activity.getStartDate())
          + "," + dateFormat.format(activity.getEndDate()) + "\n");
      writer.write("ID,First Name,Last Name,Activity Name,Start Date,End Date,Check-In Status,Date\n");
      ArrayList<AttendanceRecord> recordList = getSortedAttendanceRecords();
      for (AttendanceRecord record : recordList) {
        ArrayList<CheckInEvent> list = record.getEventList();
        //CheckInEvent event = list.get(list.size()-1);
        writer.write(record.getPerson().getId()
            + "," + record.getPerson().getFirstName()
            + "," + record.getPerson().getLastName());
        for (CheckInEvent event : list) {
          CheckInActivity activity = (event.getActivity() != null) ? event.getActivity() : CheckInEvent.DEFAULT_ACTIVITY;
          writer.write("," + activity.getName()
              + "," + activity.getStartDate()
              + "," + activity.getEndDate()
              + "," + event.getStatus()
              + "," + dateFormat.format(new Date(event.getTimeStamp())));
        }
        writer.write("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  private void dumpAttendanceRecordsAllEventsCsv2(String path) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(path));
      writer.write("Activity Name,Start Date,End Date\n");
      writer.write(activity.getName() + "," + dateFormatCsv.format(activity.getStartDate())
          + "," + dateFormatCsv.format(activity.getEndDate()) + "\n");
      writer.write("ID,First Name,Last Name,Activity Name,Start Date,End Date,Check-In Status,Timestamp\n");
      ArrayList<AttendanceRecord> recordList = getSortedAttendanceRecords();
      long timeStampMin = Long.MAX_VALUE;
      long timeStampMax = Long.MIN_VALUE;
      for (AttendanceRecord record : recordList) {
        if (!record.areEventsConsistent()) {
          LOG.info("Found inconsistent attendance record for " + record.getPerson());
        }
        ArrayList<CheckInEvent> list = record.getEventList();
        //CheckInEvent event = list.get(list.size()-1);
        String personStr = record.getPerson().getId()
            + "," + record.getPerson().getFirstName()
            + "," + record.getPerson().getLastName();
        for (CheckInEvent event : list) {
          CheckInActivity activity = (event.getActivity() != null) ? event.getActivity() : CheckInEvent.DEFAULT_ACTIVITY;
          writer.write(personStr
              + "," + activity.getName()
              + "," + activity.getStartDate()
              + "," + activity.getEndDate()
              + "," + event.getStatus()
              + "," + dateFormatCsv.format(new Date(event.getTimeStamp())) + "\n");
          if (event.getTimeStamp() > 0) {
            timeStampMin = Math.min(timeStampMin, event.getTimeStamp());
          }
          if (event.getTimeStamp() < Long.MAX_VALUE) {
            timeStampMax = Math.max(timeStampMax, event.getTimeStamp());
          }
        }
        //System.out.println("For " + record.getPerson() + ": logged a total of " + record.computeTotalAttendanceTime()/(1000. * 60 * 60) + " hrs");
      }
      //System.out.println("timestamp min " + timeStampMin + " max " + timeStampMax);
      LOG.info("timestamp min " + dateFormatCsv.format(new Date(timeStampMin)) + " max " + dateFormatCsv.format(new Date(timeStampMax)));
      //recordList.get(0).getHoursByDay(new Date(timeStampMin), new Date(timeStampMax));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  private void dumpAttendanceRecordsHoursPerDayCsv(String path) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(path));
      //writer.write("Activity Name,Start Date,End Date\n");
      //writer.write(activity.getName() + "," + dateFormatCsv.format(activity.getStartDate())
      //    + "," + dateFormatCsv.format(activity.getEndDate()) + "\n");
      //writer.write("ID,First Name,Last Name,Activity Name,Start Date,End Date,Check-In Status,Timestamp\n");
      ArrayList<AttendanceRecord> recordList = getSortedAttendanceRecords();

      // Find min, max timestamp
      long timeStampMin = Long.MAX_VALUE;
      long timeStampMax = Long.MIN_VALUE;
      for (AttendanceRecord record : recordList) {
        if (!record.areEventsConsistent()) {
          LOG.info("Found inconsistent attendance record for " + record.getPerson());
        }
        ArrayList<CheckInEvent> list = record.getEventList();
        for (CheckInEvent event : list) {
          if (event.getTimeStamp() > 0) {
            timeStampMin = Math.min(timeStampMin, event.getTimeStamp());
          }
          if (event.getTimeStamp() < Long.MAX_VALUE) {
            timeStampMax = Math.max(timeStampMax, event.getTimeStamp());
          }
        }
      }
      DateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd");
      String dates = "";
      long dayMillis = 1000L * 60L * 60L * 24L;
      int ndays = 1 + (int) (0.5 + (double) (timeStampMax - timeStampMin) / (double) dayMillis);
      for (int i = 0; i < ndays; i++) {
        dates += "," + dateFmt.format(new Date(timeStampMin + i * dayMillis));
      }
      writer.write("ID,First Name,Last Name" + dates + ",Total Hours\n");

      //System.out.println("ndays " + ndays);
      //System.out.println("timestamp min " + timeStampMin + " max " + timeStampMax);
      LOG.info("timestamp min " + dateFormatCsv.format(new Date(timeStampMin)) + " max " + dateFormatCsv.format(new Date(timeStampMax)));
      //recordList.get(0).getHoursByDay(new Date(timeStampMin), new Date(timeStampMax));

      for (AttendanceRecord record : recordList) {
        if (!record.areEventsConsistent()) {
          LOG.info("Found inconsistent attendance record for " + record.getPerson());
        }
        ArrayList<CheckInEvent> list = record.getEventList();
        //CheckInEvent event = list.get(list.size()-1);
        String personStr = record.getPerson().getId()
            + "," + record.getPerson().getFirstName()
            + "," + record.getPerson().getLastName();
        String eventHrs = floatArrayToString(record.getHoursByDay(timeStampMin, ndays), "%.3f");
        writer.write(personStr + eventHrs + "," + String.format("%.3f", record.computeTotalAttendanceTime()/(1000. * 60 * 60)) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }

  /**
   * Dump "database" to filesystem in JSON format.
   * @param path directory to contain "database"
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  /*package*/void dumpJson(String path) throws IOException {
    File file = new File(path);
    if (!file.isDirectory()) {
      throw new IOException("Path " + path + " must be a directory!");
    }
    dumpAttendanceRecordsJson(path + File.separator + JSON_ATTENDANCE_RECORDS);
  }

  private void dumpAttendanceRecordsJson(String path) throws IOException {
    FileWriter fw = new FileWriter(path);
    //Gson gson = new Gson();
    // TODO nest activity and map into a top-level json object
    //gson.toJson(activity, fw);
    //synchronized (map) {
    //  gson.toJson(map, fw);
    //}
    JsonWriter writer = new JsonWriter(fw);
    //Gson gson = new Gson();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    writer.setIndent("  ");
    writer.beginObject();
    //writer.name("activity").value(gson.toJson(activity));
    writer.name("activity").jsonValue(gson.toJson(activity));
    synchronized (map) {
      //writer.name("map").value(gson.toJson(map));
      writer.name("map").jsonValue(gson.toJson(map));
    }
    writer.endObject();
    writer.close();
    fw.close();
  }

  private String floatArrayToString(float[] hoursByDay, String fmt) {
    String val = "";
    for (int i = 0; i < hoursByDay.length; i++) {
      val += "," + String.format(fmt, hoursByDay[i]);
    }
    return val;
  }

  public ArrayList<AttendanceRecord> getSortedAttendanceRecords() {
    ArrayList<AttendanceRecord> recordList;
    int rowCount = getIdSet().size();
    recordList = new ArrayList<AttendanceRecord>(rowCount);
    for (Long id : getIdSet()) {
      AttendanceRecord record = getAttendanceRecord(id);
      if (id != record.getPerson().getId()) {
        LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
            + " for attendance record from person " + record.getPerson());
      }
      recordList.add(record);
    }
    Collections.sort(recordList, new Comparator<AttendanceRecord>() {

      @Override
      public int compare(AttendanceRecord o1, AttendanceRecord o2) {
        String o1Name = o1.getPerson().getLastName() + " " + o1.getPerson().getFirstName();
        String o2Name = o2.getPerson().getLastName() + " " + o2.getPerson().getFirstName();
        return o1Name.compareToIgnoreCase(o2Name);
      }
    });

    return recordList;
  }

  public Set<Long> getIdSet() {
    return map.keySet();
  }

  public AttendanceRecord getAttendanceRecord(long id) {
    return map.get(id);
  }

  public void checkOutAll() {
    synchronized (map) {
      for (Long id : map.keySet()) {
        AttendanceRecord record = map.get(id);
        CheckInEvent event = record.getLastEvent();
        if (event.getStatus().equals(CheckInEvent.Status.CheckedIn)) {
          if (activity != null) {
            map.get(id).getEventList().add(new CheckInEvent(activity,
                CheckInEvent.Status.CheckedOut, System.currentTimeMillis()));
          } else {
            map.get(id).getEventList().add(new CheckInEvent(CheckInEvent.DEFAULT_ACTIVITY,
                CheckInEvent.Status.CheckedOut, System.currentTimeMillis()));
          }
        }
        System.out.println("id " + id + " name " + record.getPerson() + " check out "
            + dateFormat.format(new Date(event.getTimeStamp())));
      }
    }
  }

  /**
   * Print the last check-in event for each attendance record.
   */
  public void print() {
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

  /**
   * Print the last check-in event for each attendance record.
   */
  public String printToString() {
    String buffer = "";
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    if (activity != null) {
      buffer += activity.printToString(dateFormat) + "\n";
    }
    synchronized (map) {
      for (Long id : map.keySet()) {
        AttendanceRecord record = map.get(id);
        ArrayList<CheckInEvent> list = record.getEventList();
        CheckInEvent event = list.get(list.size()-1);
        buffer += "id " + id + " name " + record.getPerson() + " check-in "
            + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())) + "\n";
      }
    }
    return buffer;
  }
}
