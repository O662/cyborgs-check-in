package org.cyborgs3335.checkin.server.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.server.jdbc.DatabaseCreator;
import org.cyborgs3335.checkin.server.jdbc.DatabaseConnection;


public class JdbcOutput {

  public static void dumpAttendanceRecords(String path, CheckInActivity activity, Map<Long, AttendanceRecord> map) throws IOException {
    try {
      // Create database
      DatabaseCreator creator = new DatabaseCreator(path);
      creator.close();

      // Open database for populating with data
      //DatabaseWriter writer = new DatabaseWriter("jdbc:sqlite:checkin_test1.db");
      DatabaseConnection writer = new DatabaseConnection(path);
      System.out.println("person table before: ");
      writer.printPersonTable();
      System.out.println("check-in activity table before: ");
      writer.printCheckInActivityTable();
      System.out.println("current check-in activity table before: ");
      writer.printCurrentCheckInActivityTable();
      System.out.println("attendance record table before: ");
      writer.printAttendanceRecordTable();

      // Walk through map, populating person, check-in activity and attendance record tables
      int personCountInsert = 0;
      int personCountSkip = 0;
      int personCountTotal = 0;
      int eventCountInsert = 0;
      int eventCountSkip = 0;
      int eventCountTotal = 0;
      for (long key: map.keySet()) {
        AttendanceRecord record = map.get(key);
        // Add person
        Person p = record.getPerson();
        boolean success = writer.insertPerson(p);
        personCountTotal++;
        if (success) {
          personCountInsert++;
        } else {
          personCountSkip++;
        }
        // Add events
        for (CheckInEvent event: record.getEventList()) {
          success = writer.insertAttendanceRecord(p, event);
          eventCountTotal++;
          if (success) {
            eventCountInsert++;
          } else {
            eventCountSkip++;
          }
        }
      }

      //System.out.println("after: ");
      //writer.printPersonTable();
      System.out.println("Persons added " + personCountInsert + " Skipped " + personCountSkip + " Total " + personCountTotal);
      System.out.println("Events added " + eventCountInsert + " Skipped " + eventCountSkip + " Total " + eventCountTotal);

      // Ensure current activity is added to check-in activity table
      long activityId = writer.insertCheckInActivity(activity);
      System.out.println("after: (activityId = " + activityId + ")");
      writer.printCheckInActivityTable();

      // Set current activity in current check-in activity table
      activityId = writer.getCheckInActivityId(activity);
      System.out.println("Found activity id: " + activityId);
      boolean success = writer.setCurrentCheckInActivityId(activityId);
      System.out.println("after: (success = " + success + ")");
      writer.printCurrentCheckInActivityTable();

      System.out.println("person table after: ");
      writer.printPersonTable();
      System.out.println("check-in activity table after: ");
      writer.printCheckInActivityTable();
      System.out.println("current check-in activity table after: ");
      writer.printCurrentCheckInActivityTable();
      System.out.println("attendance record table after: ");
      writer.printAttendanceRecordTable();

      writer.close();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IOException("Caught SQlException", e);
    }
  }
}
