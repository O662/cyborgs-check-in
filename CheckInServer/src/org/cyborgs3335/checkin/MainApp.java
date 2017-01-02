package org.cyborgs3335.checkin;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.cyborgs3335.checkin.ui.MainWindow;

import jssc.SerialPortException;

/**
 * Starts up server, then scans IDs from terminal and/or Arduino via serial port.
 *
 * @author brian
 *
 */
public class MainApp {

  private final DateFormat dateFormat;
  private String path;

  public MainApp() {
    //dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    dateFormat = new SimpleDateFormat();
  }

  /**
   * Scan IDs from Arduino via serial port, spawning task in a thread.
   * @param portName name of serial port (e.g., /dev/ttyACM0)
   * @param daemonThread if true, make spawned task thread a daemon thread
   */
  public static void scanIdsSerial(final String portName, boolean daemonThread) {
    SerialComm.printSerialPortNames();
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        SerialComm comm;
        try {
          comm = new SerialComm(portName, false/*true*/);
        } catch (SerialPortException e) {
          throw new RuntimeException("Caught serial port exception.  Exiting...", e);
        }
        CheckInClient checkInClient = new CheckInClient();
        while (true) {
          String readLine = comm.readLine();
          if (readLine.startsWith("Firmware Version") || readLine.startsWith("Scan PICC to see UID")) {
            // Firmware Version: 0x92 = v2.0
            // Scan PICC to see UID, SAK, type, and data blocks...
            continue;
          }
          String value = readLine.replace("Card UID:", "").trim().replaceAll("\\s+", "");
          //System.out.println("value " + value + " " + Long.parseLong(value, 16));
          long id = -1;
          try {
            id = Long.parseLong(value, 16);
          } catch (NumberFormatException e) {
            // Card UID: 62 21 E4 D5
            // Card UID: 94 18 60 EC;
            System.out.println("Expected to receive Card UID, but received: \"" + readLine + "\" + value " + value + " id " + id);
            continue;
          }
          System.out.println("Serial Port Read: \"" + readLine + "\" + value " + value + " id " + id);
          try {
            boolean checkIn = checkInClient.accept(id);
            if (checkIn) {
              System.out.println("Check in ID " + id);
              comm.writeString("" + 1);
            } else {
              System.out.println("Check out ID " + id);
              comm.writeString("" + 2);
            }
          } catch (UnknownUserException e) {
            System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
            comm.writeString("" + 3);
          }
        }
      }}, "SerialPortReader");
    t.setDaemon(daemonThread);
    t.start();
  }

  /**
   * Scan IDs from terminal, with special IDs for exiting (-1), and printing
   * the current "database" (-2).
   */
  public static void scanIdsTerminal() {
    IdScanner idScanner = new IdScanner(new CheckInClient());
    while (true) {
      System.out.println("Enter ID (-1 to quit, -2 to print): ");
      long id = idScanner.readId();
      if (id == -1) {
        System.out.println("Exiting...");
        break;
      } else if (id == -2) {
        CheckInServer.getInstance().print();
        continue;
      }
      try {
        boolean checkIn = idScanner.sendId(id);
        if (checkIn) {
          System.out.println("Check in ID " + id);
        } else {
          System.out.println("Check out ID " + id);
        }
      } catch (UnknownUserException e) {
        System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
      }
    }
  }

  public void scanIdsUi() {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new MainWindow(new MainAppWindowListener());
      }
    });
  }

  public void setPath(String dbPath) {
    path = dbPath;
  }

  private class MainAppWindowListener extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {
      System.out.println("Window closing received");
      exitApp();
    }

    @Override
    public void windowClosed(WindowEvent e) {
      System.out.println("Window closed received");
    }
  }

  private void exitApp() {
    CheckInServer server = CheckInServer.getInstance();
    server.print();
    try {
      server.dump(path);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.exit(0);
  }


  /**
   * Main application, with optional argument for serial port name.  Starts
   * up server, then scans IDs from terminal and/or Arduino via serial port.
   * @param args name of serial port (default is /dev/ttyACM0)
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    final String portName = (args.length == 1) ? args[0] : "/dev/ttyACM0";

    CheckInServer server = CheckInServer.getInstance();
    String path = "/tmp/check-in-server-main-app-test.dump";
    //final String path = "/tmp/check-in-server-new-user-test.dump";
    File dir = new File(path);
    if (dir.exists()) {
      server.load(path);
    } else {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new RuntimeException("Could not create directory " + path + "for saving database!");
      }
    }

    MainApp app = new MainApp();
    app.setPath(path);

    long timeStart = System.currentTimeMillis();
    long timeEnd = timeStart + 60L*60L*1000L;
    CheckInActivity activity = new CheckInActivity("Default", timeStart, timeEnd);
    server.setActivity(activity);

    server.print();
    app.scanIdsUi();
    scanIdsSerial(portName, true);
    scanIdsTerminal();
    app.exitApp();
    //server.print();
    //server.dump(path);
  }

}
