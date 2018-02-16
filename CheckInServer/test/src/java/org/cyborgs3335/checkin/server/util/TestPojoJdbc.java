package org.cyborgs3335.checkin.server.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.MainApp;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.server.jdbc.DatabaseCreator;
import org.cyborgs3335.checkin.server.jdbc.DatabaseConnection;

public class TestPojoJdbc {

  public static void main(String[] args) throws IOException {
    // Get "original" record
    String appDirLocation = System.getProperty("user.home");
    if (appDirLocation == null) {
      appDirLocation = System.getProperty("user.dir");
    }
    String appDirName = appDirLocation + File.separator + MainApp.CHECK_IN_APP_DIR;
    String pathOrig = appDirName + File.separator + "data-2018-02-03a/check-in-server-2017-kickoff.dump/attendance-records.db";
    PojoInput pojoOrig = PojoInput.loadAttendanceRecords(pathOrig);

    // Verify POJO round-trip
    String pathPojo = "/tmp/check-in-pojo.dump";
    PojoOutput.dumpAttendanceRecords(pathPojo, pojoOrig.getCheckInActivity(), pojoOrig.getMap());
    PojoInput pojoInput = PojoInput.loadAttendanceRecords(pathPojo);
    boolean success = compare(pojoOrig.getCheckInActivity(), pojoInput.getCheckInActivity());
    if (!success) {
      System.out.println("POJO: failed on CheckInActivity!");
    } else {
      System.out.println("POJO: success on CheckInActivity!");
    }
    success = compare(pojoOrig.getMap(), pojoInput.getMap());
    if (!success) {
      System.out.println("POJO: failed on Map!");
    } else {
      System.out.println("POJO: success on Map!");
    }

    // Verify JSON round-trip
    String pathJson = "/tmp/check-in-json.dump";
    JsonOutput.dumpAttendanceRecords(pathJson, pojoOrig.getCheckInActivity(), pojoOrig.getMap());
    JsonInput jsonInput = JsonInput.loadAttendanceRecords(pathJson);
    success = compare(pojoOrig.getCheckInActivity(), jsonInput.getCheckInActivity());
    if (!success) {
      System.out.println("JSON: failed on CheckInActivity!");
    } else {
      System.out.println("JSON: success on CheckInActivity!");
    }
    success = compare(pojoOrig.getMap(), jsonInput.getMap());
    if (!success) {
      System.out.println("JSON: failed on Map!");
    } else {
      System.out.println("JSON: success on Map!");
    }

    // Verify JDBC round-trip
    String pathJdbc = "jdbc:sqlite:/tmp/check-in-jdbc.db";
    JdbcOutput.dumpAttendanceRecords(pathJdbc, pojoOrig.getCheckInActivity(), pojoOrig.getMap());
    JdbcInput jdbcInput = JdbcInput.loadAttendanceRecords(pathJdbc);
    success = compare(pojoOrig.getCheckInActivity(), jdbcInput.getCheckInActivity());
    if (!success) {
      System.out.println("JDBC: failed on CheckInActivity!");
    } else {
      System.out.println("JDBC: success on CheckInActivity!");
    }
    success = compare(pojoOrig.getMap(), jdbcInput.getMap());
    if (!success) {
      System.out.println("JDBC: failed on Map!");
    } else {
      System.out.println("JDBC: success on Map!");
    }
  }

  private static boolean compare(Map<Long, AttendanceRecord> map1, Map<Long, AttendanceRecord> map2) {
    // Check map size
    if (map1.size() != map2.size()) {
      System.out.println("Size of map1 (" + map1.size() + ") and map 2 ("
          + map2.size() + ") are not equal!");
      return false;
    }
    // Check keys
    Long[] keys1 = map1.keySet().toArray(new Long[] {});
    for (int i = 0; i < keys1.length; i++) {
      if (!map2.containsKey(keys1[i])) {
        System.out.println("Map2 does not contain map1 key " + keys1[i]);
        return false;
      }
    }
    // Check values
    for (Long key: keys1) {
      AttendanceRecord record1 = map1.get(key);
      AttendanceRecord record2 = map2.get(key);
      boolean success = compare(record1, record2);
      if (!success) {
        System.out.println("Inconsistent attendance records for key " + key);
        return false;
      }
    }
    return true;
  }

  private static boolean compare(AttendanceRecord record1, AttendanceRecord record2) {
    if (!record1.getPerson().equals(record2.getPerson())) {
      System.out.println("Person mismatch: person1 " + record1.getPerson().toString()
          + " person 2 " + record2.getPerson().toString());
      return false;
    }
    ArrayList<CheckInEvent> eventList1 = record1.getEventList();
    ArrayList<CheckInEvent> eventList2 = record2.getEventList();
    if (eventList1.size() != eventList2.size()) {
      System.out.println("Event list size mismatch for person " + record1.getPerson().toString()
          + ": " + " list1 " + eventList1.size() + " list2 " + eventList2.size());
      return false;
    }
    int size = eventList1.size();
    for (int i = 0; i < size; i++) {
      CheckInEvent event1 = eventList1.get(i);
      CheckInEvent event2 = eventList2.get(i);
      if (!event1.equals(event2)) {
        System.out.println("Mismatched events: event1 " + eventToString(event1)
            + " event2 " + eventToString(event2));
        return false;
      }
    }
    return true;
  }

  private static String eventToString(CheckInEvent event) {
    String buf = (event.getActivity() != null) ? event.getActivity().toString() : "null";
    buf += " " + event.getStatus() + " " + event.getTimeStamp();
    return buf;
  }

  private static boolean compare(CheckInActivity checkInActivity1, CheckInActivity checkInActivity2) {
    return checkInActivity1.equals(checkInActivity2);
  }

}
