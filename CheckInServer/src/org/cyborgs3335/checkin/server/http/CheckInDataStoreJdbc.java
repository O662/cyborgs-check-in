package org.cyborgs3335.checkin.server.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.server.jdbc.DatabaseConnection;
import org.cyborgs3335.checkin.server.util.JdbcInput;
import org.cyborgs3335.checkin.server.util.JsonOutput;
import org.cyborgs3335.checkin.server.util.PojoOutput;
import org.cyborgs3335.checkin.CheckInEvent.Status;


/**
 * Singleton check-in server using a database via JDBC.
 *
 * @author brian
 *
 */
public class CheckInDataStoreJdbc implements ICheckInDataStore {

  private static final Logger LOG = Logger.getLogger(CheckInDataStoreJdbc.class.getName());

  //public static final String DB_ATTENDANCE_RECORDS = "attendance-records.db";

  //public static final String JSON_ATTENDANCE_RECORDS = "attendance-records.json";

  //private final Map<Long, AttendanceRecord> map = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());

  private DatabaseConnection dbConnection = null;

  private boolean isLoaded = false;

  private String dataStorePath = null;

  //private CheckInActivity activity = null;

  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");

  private DateFormat dateFormatCsv = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  private static class Singleton {
    private static final CheckInDataStoreJdbc INSTANCE = new CheckInDataStoreJdbc();
  }

  private CheckInDataStoreJdbc() {
    //addDefaultUsers();
  }

  /**
   * Return the check-in server instance.
   * @return check-in server instance
   */
  public static CheckInDataStoreJdbc getInstance() {
    return Singleton.INSTANCE;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#addUser(java.lang.String, java.lang.String)
   */
  @Override
  public Person addUser(String firstName, String lastName) throws IOException {
    try {
      return addUserWithId(getNewId(), firstName, lastName);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException when adding user.", e);
    }
  }

  /**
   * Add a user with a predefined ID.  A null user is returned if id already exists.
   * @param id predefined ID
   * @param firstName first name
   * @param lastName last name
   * @return added user, or null if id already exists
   * @throws IOException 
   */
  /* package */Person addUserWithId(long id, String firstName, String lastName) throws IOException {
    Person person = null;
    try {
      if (dbConnection.hasPersonId(id)) {
        return null;
      }
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
      if (activity == null) {
        activity = CheckInEvent.DEFAULT_ACTIVITY;
      }
      CheckInEvent event = new CheckInEvent(activity, Status.CheckedOut, 0);
      person = new Person(id, firstName, lastName);
      boolean success = dbConnection.insertPerson(person);
      success = dbConnection.insertAttendanceRecord(person, event);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException when adding user.", e);
    }
    return person;
  }

  private long getNewId() throws SQLException {
    Random random = new Random();
    for (int count = 0; count < 100; count++) {
      long id = Math.abs(random.nextLong());
      if (!dbConnection.hasPersonId(id)) {
        return id;
      }
    }
    throw new IllegalStateException("Cannot find a new unique ID!");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#findPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person findPerson(String firstName, String lastName) throws IOException {
    try {
      return dbConnection.findPerson(firstName, lastName);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during findPerson.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#containsId(long)
   */
  @Override
  public boolean containsId(long id) throws IOException {
    try {
      return dbConnection.hasPersonId(id);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during containsId.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#checkIn(long)
   */
  @Override
  public boolean checkIn(long id) throws UnknownUserException, IOException {
    return checkInOut(id, Status.CheckedIn);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#checkOut(long)
   */
  @Override
  public boolean checkOut(long id) throws UnknownUserException, IOException {
    return checkInOut(id, Status.CheckedOut);
  }

  /**
   * Check in or out the specified id.
   * @param id id of user to check out
   * @param desiredStatus desired status (CheckedIn to check in, CheckedOut to check out)
   * @return true if successful, false otherwise
   * @throws UnknownUserException if the user is unknown to the server
   * @throws IOException 
   */
  private boolean checkInOut(long id, Status desiredStatus) throws UnknownUserException, IOException {
    try {
      if (!dbConnection.hasPersonId(id)) {
        throw new UnknownUserException("Unknown user id " + id);
      }
      CheckInEvent event = dbConnection.getLastEvent(id);
      if (event.getStatus().equals(desiredStatus)) {
        return false;
      }
      long timeStamp = System.currentTimeMillis();
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
      if (activity == null) {
        activity = CheckInEvent.DEFAULT_ACTIVITY;
      }
      dbConnection.insertAttendanceRecord(dbConnection.getPerson(id),
          new CheckInEvent(activity, desiredStatus, timeStamp));
      return true;
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during checkInOut.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#accept(long)
   */
  @Override
  public boolean accept(long id) throws UnknownUserException, IOException {
    try {
      boolean checkedIn = false;
      if (!dbConnection.hasPersonId(id)) {
        throw new UnknownUserException("Unknown user id " + id);
      }
      CheckInEvent event = dbConnection.getLastEvent(id);
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
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
      if (activity == null) {
        activity = CheckInEvent.DEFAULT_ACTIVITY;
      }
      dbConnection.insertAttendanceRecord(dbConnection.getPerson(id),
          new CheckInEvent(activity, status, timeStamp));
      return checkedIn;
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during accept.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#getActivity()
   */
  @Override
  public CheckInActivity getActivity() throws IOException {
    try {
      return dbConnection.getCurrentCheckInActivity();
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during getActivity.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) throws IOException {
    try {
      long activityId = dbConnection.insertCheckInActivity(activity);
      dbConnection.setCurrentCheckInActivityId(activityId);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during setActivity.", e);
    }
  }

  /**
   * Load "database" from filesystem.
   * @param path directory containing "database"
   * @throws IOException on I/O error loading "database" from filesystem 
   */
  /*package*/synchronized CheckInDataStoreJdbc load(String path) throws IOException {
    if (isLoaded) {
      throw new IOException("Store is already loaded at path " + dataStorePath
          + ".  Cannot load again from path " + path);
    }
    File loadDir = new File(path);
    if (!loadDir.isDirectory()) {
      throw new IOException("Path " + path + " must be a directory!");
    }
    String recordsPath = path + File.separator + DB_ATTENDANCE_RECORDS;
    File recordsFile = new File(recordsPath);
    if (recordsFile.isFile() && recordsFile.canRead()) {
      try {
        dbConnection = new DatabaseConnection("jdbc:sqlite:" + recordsPath);
      } catch (SQLException e) {
        throw new IOException("Failed to open database connection to " + recordsPath, e);
      }
    // TODO implement a create method?
    //} else if (!recordsFile.exists()) {
    //  recordsFile.createNewFile();
    } else {
      throw new IOException("Unable to access a records file at " + recordsPath);
    }
    dataStorePath = path;
    isLoaded = true;
    return this;
  }

  /**
   * Dump "database" to filesystem in &quot;POJO&quot; format.
   * @param path directory to contain "database"
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  /*package*/void dumpPojo(String path) throws IOException {
    File dumpDir = new File(path);
    if (!dumpDir.isDirectory()) {
      throw new IOException("Path " + path + " must be a directory!");
    }
    JdbcInput input = JdbcInput.loadAttendanceRecords("jdbc:sqlite:"
        + dataStorePath + File.separator + DB_ATTENDANCE_RECORDS);
    PojoOutput.dumpAttendanceRecords(path + File.separator + DB_ATTENDANCE_RECORDS,
        input.getCheckInActivity(), input.getMap());
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
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
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
    } catch (IOException | SQLException e) {
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
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
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
          CheckInActivity cia = (event.getActivity() != null) ? event.getActivity() : CheckInEvent.DEFAULT_ACTIVITY;
          writer.write("," + cia.getName()
              + "," + cia.getStartDate()
              + "," + cia.getEndDate()
              + "," + event.getStatus()
              + "," + dateFormat.format(new Date(event.getTimeStamp())));
        }
        writer.write("\n");
      }
    } catch (IOException | SQLException e) {
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
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
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
          CheckInActivity cia = (event.getActivity() != null) ? event.getActivity() : CheckInEvent.DEFAULT_ACTIVITY;
          writer.write(personStr
              + "," + cia.getName()
              + "," + cia.getStartDate()
              + "," + cia.getEndDate()
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
    } catch (IOException | SQLException e) {
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
        //ArrayList<CheckInEvent> list = record.getEventList();
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
    JdbcInput input = JdbcInput.loadAttendanceRecords("jdbc:sqlite:"
        + dataStorePath + File.separator + DB_ATTENDANCE_RECORDS);
    JsonOutput.dumpAttendanceRecords(path + File.separator + JSON_ATTENDANCE_RECORDS,
        input.getCheckInActivity(), input.getMap());
  }

  private String floatArrayToString(float[] hoursByDay, String fmt) {
    String val = "";
    for (int i = 0; i < hoursByDay.length; i++) {
      val += "," + String.format(fmt, hoursByDay[i]);
    }
    return val;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#getSortedAttendanceRecords()
   */
  @Override
  public ArrayList<AttendanceRecord> getSortedAttendanceRecords() throws IOException {
    try {
      ArrayList<Long> personIds = dbConnection.getPersonList();
      int rowCount = personIds.size();
      ArrayList<AttendanceRecord> recordList = new ArrayList<AttendanceRecord>(rowCount);
      for (Long id : personIds) {
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
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during getSortedAttendanceRecords.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#getAttendanceRecord(long)
   */
  @Override
  public AttendanceRecord getAttendanceRecord(long id) throws IOException {
    try {
      return dbConnection.getAttendanceRecord(id);
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during getAttendanceRecord.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#checkOutAll()
   */
  @Override
  public void checkOutAll() throws IOException {
    try {
      ArrayList<Long> personIds = dbConnection.getPersonList();
      for (Long id : personIds) {
        AttendanceRecord record = getAttendanceRecord(id);
        CheckInEvent event = record.getLastEvent();
        if (event.getStatus().equals(CheckInEvent.Status.CheckedIn)) {
          CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
          if (activity == null) {
            activity = CheckInEvent.DEFAULT_ACTIVITY;
          }
          boolean success = checkOut(id);
          if (!success) {
            throw new IOException("Failed to checkout person " + dbConnection.getPerson(id));
          }
        }
        System.out.println("id " + id + " name " + record.getPerson() + " check out "
            + dateFormat.format(new Date(event.getTimeStamp())));
      }
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during checkOutAll.", e);
    } catch (UnknownUserException e) {
      throw new IOException("Caught UnknownUserException during checkOutAll."
          + "  Check database integrity!", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#printToString()
   */
  @Override
  public String printToString() throws IOException {
    try {
      StringWriter writer = new StringWriter();
      CheckInActivity activity = dbConnection.getCurrentCheckInActivity();
      if (activity != null) {
        writer.write(activity.printToString(dateFormat) + "\n");
      }
      for (Long id : dbConnection.getPersonList()) {
        AttendanceRecord record = dbConnection.getAttendanceRecord(id);
        ArrayList<CheckInEvent> list = record.getEventList();
        CheckInEvent event = list.get(list.size()-1);
        writer.write("id " + id + " name " + record.getPerson() + " check-in "
            + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())) + "\n");
      }
      return writer.toString();
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during printToString.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.server.http.ICheckInDataStore#getLastCheckInEventsSorted()
   */
  @Override
  public List<PersonCheckInEvent> getLastCheckInEventsSorted() throws IOException {
    try {
      ArrayList<Long> personList = dbConnection.getPersonList();
      ArrayList<PersonCheckInEvent> recordList = new ArrayList<PersonCheckInEvent>(personList.size());
      for (Long id : personList) {
        //AttendanceRecord record = dbConnection.getAttendanceRecord(id);
        //if (id != record.getPerson().getId()) {
        //  LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
        //      + " for attendance record from person " + record.getPerson());
        //}
        //recordList.add(new PersonCheckInEvent(record.getPerson(), record.getLastEvent()));
        recordList.add(new PersonCheckInEvent(dbConnection.getPerson(id), dbConnection.getLastEvent(id)));
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
    } catch (SQLException e) {
      throw new IOException("Caught SQLException during getLastCheckInEventsSorted.", e);
    }
  }
}
