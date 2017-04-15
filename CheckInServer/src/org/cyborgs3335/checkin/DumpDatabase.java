package org.cyborgs3335.checkin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import jssc.SerialPortException;

/**
 * Dumps all the records from the server database.
 *
 * @author brian
 *
 */
public class DumpDatabase {

  /**
   * Print the last check-in event for each attendance record.
   */
  public static void print(CheckInServer server) {
    CheckInActivity activity = server.getActivity();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    if (activity != null) {
      activity.print(dateFormat);
    }
    Set<Long> set = server.getIdSet();
    synchronized (set) {
      for (Long id : set) {
        AttendanceRecord record = server.getAttendanceRecord(id);
        System.out.print("id " + id + " \tname " + record.getPerson() + " \tevents: ");
        ArrayList<CheckInEvent> list = record.getEventList();
        for (CheckInEvent event : list) {
          if (event.getActivity() != null) {
            System.out.print(" " + event.getActivity().getName());
          } else {
            System.out.print(" " + event.getActivity());
          }
          System.out.print(" " + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())));
        }
        System.out.println();
      }
    }
  }

  /**
   * Main application starts up server, then dumps all records from specified database to stdout.
   * @param args path to database
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    //String path = (args.length == 1) ? path = args[0] : "/tmp/check-in-server2-test.dump";
    String path = (args.length == 1) ? path = args[0] : "/home/brian/CyborgsCheckIn/check-in-server-2017-kickoff.dump";

    CheckInServer server = CheckInServer.getInstance();
    File dir = new File(path);
    if (dir.exists()) {
      server.load(path);
    } else {
      throw new RuntimeException("Could not open database directory " + path + "!");
    }
    print(server);
    //server.print();
  }

}
