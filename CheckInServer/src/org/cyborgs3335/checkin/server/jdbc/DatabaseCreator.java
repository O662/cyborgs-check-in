package org.cyborgs3335.checkin.server.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCreator {

  public static final String TABLE_PERSON = "person";
  public static final String TABLE_CURRENT_CHECKIN_ACTIVITY = "current_checkin_activity";
  public static final String TABLE_CHECKIN_ACTIVITY = "checkin_activity";
  public static final String TABLE_ATTENDANCE_RECORD = "attendance_record";

  private final Connection connection;

  public DatabaseCreator(String url) throws SQLException {
    // create a database connection
    connection = DriverManager.getConnection(url);
    Statement statement = connection.createStatement();
    statement.setQueryTimeout(30);  // set timeout to 30 sec.
    statement.execute("PRAGMA foreign_keys = ON");
    deleteTables(statement);
    createPersonTable(TABLE_PERSON, statement);
    createCheckInActivityTable(TABLE_CHECKIN_ACTIVITY, statement);
    createCurrentCheckInActivityTable(TABLE_CURRENT_CHECKIN_ACTIVITY, statement);
    createAttendanceRecordTable(TABLE_ATTENDANCE_RECORD, statement);
    statement.close();
  }

  private void deleteTables(Statement statement) throws SQLException {
    // Order matters
    statement.execute("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE_RECORD);
    statement.execute("DROP TABLE IF EXISTS " + TABLE_CURRENT_CHECKIN_ACTIVITY);
    statement.execute("DROP TABLE IF EXISTS " + TABLE_CHECKIN_ACTIVITY);
    statement.execute("DROP TABLE IF EXISTS " + TABLE_PERSON);
  }

  private void createPersonTable(String tableName, Statement statement) throws SQLException {
    statement.execute("DROP TABLE IF EXISTS " + tableName);
    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        //+ "person_id INTEGER PRIMARY KEY, "
        + "person_id INTEGER UNIQUE NOT NULL, "
        + "first_name TEXT NOT NULL, "
        + "middle_name TEXT NOT NULL, "
        + "last_name TEXT NOT NULL, "
        + "nick_name TEXT NOT NULL)";
    statement.execute(sql);
  }

  private void createCheckInActivityTable(String tableName, Statement statement) throws SQLException {
    statement.execute("DROP TABLE IF EXISTS " + tableName);
    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        //+ "activity_id INTEGER UNIQUE NOT NULL, "
        + "activity_id INTEGER PRIMARY KEY, "
        + "activity_name TEXT NOT NULL, "
        + "time_start INTEGER NOT NULL, "
        + "time_end INTEGER NOT NULL)";
    statement.execute(sql);
  }

  private void createCurrentCheckInActivityTable(String tableName, Statement statement) throws SQLException {
    statement.execute("DROP TABLE IF EXISTS " + tableName);
    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        //+ "id INTEGER UNIQUE NOT NULL, "
        + "id INTEGER PRIMARY KEY, "
        + "activity_id INTEGER UNIQUE NOT NULL, "
        + "FOREIGN KEY(activity_id) REFERENCES " + TABLE_CHECKIN_ACTIVITY + "(activity_id) on DELETE RESTRICT"
        + ")";
    statement.execute(sql);
  }

  private void createAttendanceRecordTable(String tableName, Statement statement) throws SQLException {
    statement.execute("DROP TABLE IF EXISTS " + tableName);
    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        + "person_id INTEGER NOT NULL, "
        + "activity_id INTEGER NOT NULL, "
        + "status TEXT NOT NULL, "
        + "time_stamp INTEGER NOT NULL, "
        + "FOREIGN KEY(person_id) REFERENCES " + TABLE_PERSON + "(person_id) ON DELETE RESTRICT, "
        + "FOREIGN KEY(activity_id) REFERENCES " + TABLE_CHECKIN_ACTIVITY + "(activity_id) ON DELETE RESTRICT"
        + ")";
    statement.execute(sql);
  }

  private void insertSample() throws SQLException {
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
    statement.executeUpdate("INSERT INTO " + TABLE_PERSON + " VALUES(1, 'leo', '', 'brown', '')");
    statement.executeUpdate("INSERT INTO " + TABLE_PERSON + " VALUES(2, 'yui', '', 'gui', '')");
    ResultSet rs = statement.executeQuery("SELECT * FROM " + TABLE_PERSON);
    while(rs.next()) {
      // read the result set
      System.out.println("person_id = " + rs.getLong("person_id")
          + " first_name = " + rs.getString("first_name")
          + " middle_name = " + rs.getString("middle_name")
          + " last_name = " + rs.getString("last_name")
          + " nick_name = " + rs.getString("nick_name"));
    }
    statement.close();
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

  public void close() throws SQLException {
    //connection.commit();
    connection.close();
  }

  public static void main(String[] args) {
    try {
      DatabaseCreator creator = new DatabaseCreator("jdbc:sqlite:checkin_test1.db");
      //creator.insertSample();
      creator.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
