package org.cyborgs3335.checkin.server.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;


public class PojoInput {

  private final CheckInActivity activity;
  private final Map<Long, AttendanceRecord> map;

  private PojoInput(CheckInActivity activity, Map<Long, AttendanceRecord> map) {
    this.activity = activity;
    this.map = map;
  }

  public CheckInActivity getCheckInActivity() {
    return activity;
  }

  public Map<Long, AttendanceRecord> getMap() {
    return map;
  }

  public static PojoInput loadAttendanceRecords(String path) {
    ObjectInputStream ois = null;
    FileInputStream fin = null;
    CheckInActivity activity = null;
    Map<Long, AttendanceRecord> map = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());
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
      return new PojoInput(activity, map);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return null;
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
}
