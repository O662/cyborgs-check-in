package org.cyborgs3335.checkin.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.server.jdbc.DatabaseCreator;
import org.cyborgs3335.checkin.server.jdbc.DatabaseWriter;


public class JdbcInput {

  private final CheckInActivity activity;
  private final Map<Long, AttendanceRecord> map;

  private JdbcInput(CheckInActivity activity, Map<Long, AttendanceRecord> map) {
    this.activity = activity;
    this.map = map;
  }

  public CheckInActivity getCheckInActivity() {
    return activity;
  }

  public Map<Long, AttendanceRecord> getMap() {
    return map;
  }

  public static JdbcInput loadAttendanceRecords(String path) throws IOException {
    CheckInActivity activity = null;
    Map<Long, AttendanceRecord> map = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());
    try {
      // Open database for populating with data
      //DatabaseWriter writer = new DatabaseWriter("jdbc:sqlite:checkin_test1.db");
      DatabaseWriter writer = new DatabaseWriter(path);
      //System.out.println("person table before: ");
      //writer.printPersonTable();
      //System.out.println("check-in activity table before: ");
      //writer.printCheckInActivityTable();
      //System.out.println("current check-in activity table before: ");
      //writer.printCurrentCheckInActivityTable();
      //System.out.println("attendance record table before: ");
      //writer.printAttendanceRecordTable();

      //int personCountInsert = 0;
      //int personCountSkip = 0;
      //int personCountTotal = 0;
      //int eventCountInsert = 0;
      //int eventCountSkip = 0;
      //int eventCountTotal = 0;
      ArrayList<Long> personList = writer.getPersonList();
      for (long personId: personList) {
        AttendanceRecord record = writer.getAttendanceRecord(personId);
        ArrayList<CheckInEvent> list = record.getEventList();
        for (int i = 0; i < list.size(); i++) {
          CheckInEvent event = list.get(i);
          if (event.getActivity().equals(CheckInEvent.DEFAULT_ACTIVITY)) {
            list.set(i, new CheckInEvent(null, event.getStatus(), event.getTimeStamp()));
          }
        }
        map.put(personId, record);
        // Add person
        //Person p = record.getPerson();
        //boolean success = writer.insertPerson(p);
        //personCountTotal++;
        //if (success) {
        //  personCountInsert++;
        //} else {
        //  personCountSkip++;
        //}
        // Add events
        //for (CheckInEvent event: record.getEventList()) {
          //success = writer.insertAttendanceRecord(p, event);
          //eventCountTotal++;
          //if (success) {
          //  eventCountInsert++;
          //} else {
          //  eventCountSkip++;
          //}
        //}
      }

      //System.out.println("after: ");
      //writer.printPersonTable();
      //System.out.println("Persons added " + personCountInsert + " Skipped " + personCountSkip + " Total " + personCountTotal);
      //System.out.println("Events added " + eventCountInsert + " Skipped " + eventCountSkip + " Total " + eventCountTotal);

      // TODO Write out map and all activities
      //long activityId = writer.insertCheckInActivity(activity);
      //System.out.println("after: (activityId = " + activityId + ")");
      //writer.printCheckInActivityTable();

      // TODO Write out activity
      //activityId = writer.getCheckInActivityId(activity);
      //System.out.println("Found activity id: " + activityId);
      activity = writer.getCurrentCheckInActivity();
      //System.out.println("after: (success = " + success + ")");
      //writer.printCurrentCheckInActivityTable();

      //System.out.println("person table after: ");
      //writer.printPersonTable();
      //System.out.println("check-in activity table after: ");
      //writer.printCheckInActivityTable();
      //System.out.println("current check-in activity table after: ");
      //writer.printCurrentCheckInActivityTable();
      //System.out.println("attendance record table after: ");
      //writer.printAttendanceRecordTable();

      writer.close();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IOException("Caught SQlException", e);
    }
//    try {
//    } catch (IOException | ClassNotFoundException e) {
//      e.printStackTrace();
//      return null;
//    } finally {
//      if (ois != null) {
//        try {
//          ois .close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      } 
//    }
    return new JdbcInput(activity, map);
  }
}
