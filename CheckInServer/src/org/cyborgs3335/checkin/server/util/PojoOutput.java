package org.cyborgs3335.checkin.server.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;


public class PojoOutput {

  public static void dumpAttendanceRecords(String path, CheckInActivity activity, Map<Long, AttendanceRecord> map) throws IOException {
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
          fout.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } 
    }
  }
}
