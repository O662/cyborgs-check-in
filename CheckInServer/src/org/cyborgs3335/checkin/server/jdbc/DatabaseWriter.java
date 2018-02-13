package org.cyborgs3335.checkin.server.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;

public class DatabaseWriter {

  private final Connection connection;

  public DatabaseWriter(String url) throws SQLException {
    // create a database connection
    connection = DriverManager.getConnection(url);
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    //createPersonTable(TABLE_PERSON, statement);
    //createCheckInActivityTable(TABLE_CHECKIN_ACTIVITY, statement);
    //createAttendanceRecordTable(TABLE_ATTENDANCE_RECORD, statement);
    statement.close();
  }

  public void printPersonTable() throws SQLException {
    /*
    String sql = "INSERT INTO warehouses(name,capacity) VALUES(?,?)";
    
    try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, name);
        pstmt.setDouble(2, capacity);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    */
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    ResultSet rs = statement.executeQuery("SELECT * FROM " + DatabaseCreator.TABLE_PERSON);
    while (rs.next()) {
      // read the result set
      System.out.println("person_id = " + rs.getLong("person_id")
          + " first_name = " + rs.getString("first_name")
          + " middle_name = " + rs.getString("middle_name")
          + " last_name = " + rs.getString("last_name")
          + " nick_name = " + rs.getString("nick_name"));
    }
    statement.close();
  }

  public Person getPerson(long personId) throws SQLException {
    Person person = null;
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_PERSON
        + " WHERE person_id = " + personId;
    ResultSet rs = statement.executeQuery(sql);
    int count = 0;
    while (rs.next()) {
      count++;
      if (count > 1) {
        statement.close();
        throw new SQLException("Found more than one current person with personId " + personId + "!");
      }
      String firstName = rs.getString("first_name");
      String middleName = rs.getString("middle_name");
      String lastName = rs.getString("last_name");
      String nickName = rs.getString("nick_name");
      person = new Person(personId, firstName, middleName, lastName, nickName);
    }
    statement.close();
    return person;
  }

  public ArrayList<Long> getPersonList() throws SQLException {
    ArrayList<Long> list = new ArrayList<Long>();
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    ResultSet rs = statement.executeQuery("SELECT person_id FROM " + DatabaseCreator.TABLE_PERSON);
    while (rs.next()) {
      list.add(rs.getLong("person_id"));
    }
    statement.close();
    return list;
  }

  public boolean insertPerson(Person person) throws SQLException {
    /*
    String sql = "INSERT INTO warehouses(name,capacity) VALUES(?,?)";
    
    try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, name);
        pstmt.setDouble(2, capacity);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    */
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    String sql = "SELECT person_id, first_name, middle_name, last_name, nick_name FROM "
        + DatabaseCreator.TABLE_PERSON + " WHERE person_id = " + person.getId();
    ResultSet rs = statement.executeQuery(sql);
    if (rs.next()) {
      System.out.println(
          " person_id " + rs.getLong("person_id") +
          " first_name " + rs.getString("first_name") +
          " middle_name " + rs.getString("middle_name") +
          " last_name " + rs.getString("last_name") +
          " nick_name " + rs.getString("nick_name")
          );
      statement.close();
      return false;
    }
    sql = "INSERT INTO " + DatabaseCreator.TABLE_PERSON + " VALUES("
        + person.getId()
        + ", '" + person.getFirstName() + "'"
        + ", '" + person.getMiddleName() + "'"
        + ", '" + person.getLastName() + "'"
        + ", '" + person.getNickName() + "'"
        + ")";
    statement.executeUpdate(sql);
    statement.close();
    return true;
  }

  /*
  public void update(int id, String name, double capacity) {
    String sql = "UPDATE warehouses SET name = ? , "
        + "capacity = ? "
        + "WHERE id = ?";

    try (Connection conn = this.connect();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // set the corresponding param
      pstmt.setString(1, name);
      pstmt.setDouble(2, capacity);
      pstmt.setInt(3, id);
      // update 
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  */

  public void printCurrentCheckInActivityTable() throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CURRENT_CHECKIN_ACTIVITY;
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) {
      System.out.println(
          " id " + rs.getLong("id") +
          " activity_id " + rs.getLong("activity_id"));
    }
    statement.close();
  }

  public CheckInActivity getCurrentCheckInActivity() throws SQLException {
    CheckInActivity activity = null;
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CURRENT_CHECKIN_ACTIVITY
        + " WHERE id = 1";
    ResultSet rs = statement.executeQuery(sql);
    int count = 0;
    while (rs.next()) {
      count++;
      if (count > 1) {
        statement.close();
        throw new SQLException("Found more than one current check-in activity!");
      }
      long activityId = rs.getLong("activity_id");
      activity = getCheckInActivity(activityId);
    }
    statement.close();
    return activity;
  }

  public boolean setCurrentCheckInActivityId(long activityId) throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CURRENT_CHECKIN_ACTIVITY;
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) { // returns after first insert
      sql = "UPDATE " + DatabaseCreator.TABLE_CURRENT_CHECKIN_ACTIVITY
          + " SET activity_id = " + activityId
          + " WHERE id = 1";
      int retval = statement.executeUpdate(sql);
      System.out.println("update returned " + retval);
      statement.close();
      return retval == 1;
    }
    sql = "INSERT INTO " + DatabaseCreator.TABLE_CURRENT_CHECKIN_ACTIVITY + " VALUES("
        + "1"
        + ", " + activityId
        + ")";
    int retval = statement.executeUpdate(sql);
    System.out.println("insert returned " + retval);
    statement.close();
    return retval == 1;
  }

  public void printCheckInActivityTable() throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CHECKIN_ACTIVITY;
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) {
      System.out.println(
          " activity_id " + rs.getLong("activity_id") +
          " activity_name " + rs.getString("activity_name") +
          " time_start " + rs.getLong("time_start") +
          " time_end " + rs.getLong("time_end"));
    }
    statement.close();
  }

  public CheckInActivity getCheckInActivity(long activityId) throws SQLException {
    CheckInActivity activity = null;
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CHECKIN_ACTIVITY + " WHERE "
        + "activity_id = " + activityId;
    ResultSet rs = statement.executeQuery(sql);
    int count = 0;
    while (rs.next()) {
      count++;
      if (count > 1) {
        statement.close();
        throw new SQLException("Found more than one check-in activity for activityId " + activityId);
      }
      String name = rs.getString("activity_name");
      long timeStart = rs.getLong("time_start");
      long timeEnd = rs.getLong("time_end");
      activity = new CheckInActivity(name, timeStart, timeEnd);
    }
    statement.close();
    return activity;
  }

  /**
   * Get the id for the specified check-in activity.
   * @param activity check-in activity to find id
   * @return id of specified check-in activity, or -1 if no activity found
   * @throws SQLException on SQL errors, or if more than one matching activity is found
   */
  public long getCheckInActivityId(CheckInActivity activity) throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    System.out.println("Searching for activity: name " + activity.getName()
        + " start time " + activity.getStartTime() + " end time " + activity.getEndTime());
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_CHECKIN_ACTIVITY + " WHERE "
        + "activity_name LIKE '" + activity.getName() + "' AND "
        + "time_start = " + activity.getStartTime() + " AND "
        + "time_end = " + activity.getEndTime();
    ResultSet rs = statement.executeQuery(sql);
    int count = 0;
    long activityId = -1;
    while (rs.next()) {
      count++;
      if (count > 1) {
        statement.close();
        throw new SQLException("Found more than one matching activities for activity " + activity);
      }
      activityId = rs.getLong("activity_id");
      System.out.println(
          " activity_id " + activityId +
          " activity_name " + rs.getString("activity_name") +
          " time_start " + rs.getLong("time_start") +
          " time_end " + rs.getLong("time_end")
          );
    }
    if (count == 0) {
      statement.close();
      System.out.println("Did not find any matching activities for activity " + activity);
    }
    statement.close();
    return activityId;
  }

  public long insertCheckInActivity(CheckInActivity activity) throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    long activityId = getCheckInActivityId(activity);
    if (activityId >= 1) {
      return activityId;
    }
    String sql = "INSERT INTO " + DatabaseCreator.TABLE_CHECKIN_ACTIVITY
        + " (activity_name, time_start, time_end) VALUES("
        + "'" + activity.getName() + "'"
        + ", " + activity.getStartTime()
        + ", " + activity.getEndTime()
        + ")";
    int retval = statement.executeUpdate(sql);
    System.out.println("insert check-in activity returned " + retval);
    statement.close();
    return getCheckInActivityId(activity);
  }

  public void printAttendanceRecordTable() throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_ATTENDANCE_RECORD;
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) {
      System.out.println(
          " person_id " + rs.getLong("person_id") +
          " activity_id " + rs.getLong("activity_id") +
          " status " + rs.getString("status") +
          " time_stamp " + rs.getLong("time_stamp"));
    }
    statement.close();
  }

  public AttendanceRecord getAttendanceRecord(long personId) throws SQLException {
    Person person = getPerson(personId);
    AttendanceRecord record = new AttendanceRecord(person);
    ArrayList<CheckInEvent> list = record.getEventList();
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_ATTENDANCE_RECORD + " WHERE "
        + "person_id = " + personId;
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) {
      long activityId = rs.getLong("activity_id");
      CheckInEvent.Status status = CheckInEvent.Status.valueOf(rs.getString("status"));
      long timeStamp = rs.getLong("time_stamp");
      CheckInEvent event = new CheckInEvent(getCheckInActivity(activityId), status, timeStamp);
      list.add(event);
    }
    statement.close();

    return record;
  }

  public boolean insertAttendanceRecord(Person person, CheckInEvent event) throws SQLException {
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    CheckInActivity activity = event.getActivity();
    if (activity == null) {
      activity = CheckInEvent.DEFAULT_ACTIVITY;
    }
    long activityId = insertCheckInActivity(activity);
    String sql = "SELECT * FROM " + DatabaseCreator.TABLE_ATTENDANCE_RECORD + " WHERE "
        + "person_id = " + person.getId() + " AND "
        + "activity_id = " + activityId + " AND "
        + "status LIKE '" + event.getStatus() + "' AND "
        + "time_stamp = " + event.getTimeStamp();
    ResultSet rs = statement.executeQuery(sql);
    while (rs.next()) { // returns after first insert
      System.out.println(
          " person_id " + rs.getLong("person_id") +
          " activity_id " + rs.getLong("activity_id") +
          " status " + rs.getString("status") +
          " time_stamp " + rs.getLong("time_stamp")
          );
      statement.close();
      return false;
    }
    sql = "INSERT INTO " + DatabaseCreator.TABLE_ATTENDANCE_RECORD
        + " (person_id, activity_id, status, time_stamp) VALUES("
        + person.getId()
        + ", " + activityId
        + ", '" + event.getStatus() + "'"
        + ", " + event.getTimeStamp()
        + ")";
    int retval = statement.executeUpdate(sql);
    System.out.println("insert attendance record returned " + retval);
    statement.close();
    //return true;
    return retval == 1;
  }

  public void close() throws SQLException {
    //connection.commit();
    connection.close();
  }

  public static void main(String[] args) {
    try {
      DatabaseWriter creator = new DatabaseWriter("jdbc:sqlite:checkin_test1.db");
      System.out.println("before: ");
      creator.printPersonTable();
      Person p = new Person(101, "John", "Doe");
      boolean success = creator.insertPerson(p);
      if (success) {
        System.out.println("Added person " + p);
      } else {
        System.out.println("Skipped person " + p);
      }
      System.out.println("after: ");
      creator.printPersonTable();
      creator.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
