package org.cyborgs3335.checkin;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.server.local.LocalMessenger;
import org.cyborgs3335.checkin.ui.MainWindow;

import jssc.SerialPortException;

/**
 * Starts up server, then scans IDs from terminal and/or Arduino via serial port.
 *
 * @author brian
 *
 */
public class MainApp implements IDatabaseOperations {

  // TODO 1. Move loading into server
  // TODO 2. Move saving into server
  // TODO 2.1 Push events onto a queue
  // TODO 2.2 Save all events to disk as they come in, multiple events at a time if queued up

  private static final Logger LOG = Logger.getLogger(MainApp.class.getPackage().getName());

  public static final String CHECK_IN_APP_DIR = "CyborgsCheckIn";

  private final LocalMessenger localMessenger;
  private final DateFormat dateFormat;
  private String path;
  private FileWriter logWriter = null;
  private Component parent = null;

  public MainApp(LocalMessenger messenger) {
    localMessenger = messenger;
    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    //dateFormat = new SimpleDateFormat();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.IDatabaseOperations#getMessenger()
   */
  @Override
  public IMessenger getMessenger() {
    return localMessenger;
  }

  /**
   * Scan IDs from Arduino via serial port, spawning task in a thread.
   * @param messenger messenger to handle checkin events
   * @param portName name of serial port (e.g., /dev/ttyACM0)
   * @param daemonThread if true, make spawned task thread a daemon thread
   */
  public static void scanIdsSerial(final IMessenger messenger, final String portName, boolean daemonThread) {
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
            CheckInEvent.Status status = messenger.toggleCheckInStatus(id);
            switch (status) {
              case CheckedIn:
                System.out.println("Check in ID " + id);
                comm.writeString("" + 1);
                break;
              case CheckedOut:
                System.out.println("Check out ID " + id);
                comm.writeString("" + 2);
                break;
              default:
                System.out.println("Unknown status: " + status);
                break;
            }
          } catch (UnknownUserException e) {
            System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
            comm.writeString("" + 3);
          } catch (IOException e) {
            System.out.println("Caught unexpected IOException for id: " + id + ".");
            comm.writeString("" + 4);
          }
        }
      }}, "SerialPortReader");
    t.setDaemon(daemonThread);
    t.start();
  }

  /**
   * Scan IDs from terminal, with special IDs for exiting (-1), and printing
   * the current "database" (-2).
   * @param messenger
   * @throws IOException 
   */
  public static void scanIdsTerminal(IMessenger messenger) throws IOException {
    IdScanner idScanner = new IdScanner(messenger);
    while (true) {
      System.out.println("Enter ID (-1 to quit, -2 to print): ");
      long id = idScanner.readId();
      if (id == -1) {
        System.out.println("Exiting...");
        break;
      } else if (id == -2) {
        System.out.println(messenger.lastCheckInEventToString());
        continue;
      }
      try {
        CheckInEvent.Status status = idScanner.sendId(id);
        switch (status) {
          case CheckedIn:
            System.out.println("Check in ID " + id);
            break;
          case CheckedOut:
            System.out.println("Check out ID " + id);
            break;
          default:
            System.out.println("Unknown status: " + status);
            break;
        }
      } catch (UnknownUserException e) {
        System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
      }
    }
  }

  public void scanIdsUi() {
    final MainApp mainApp = this;
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        parent = new MainWindow(mainApp, new MainAppWindowListener());
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

  @Override
  public void loadDatabase() {
    // TODO implement me
  }

  @Override
  public void saveDatabase() {
    saveDatabase(path);
  }

  @Override
  public void saveDatabase(String newPath) {
    saveDatabase(newPath, true);
  }

  @Override
  public synchronized void saveDatabase(String newPath, boolean updatePath) {
    if (LOG.isLoggable(Level.FINE)) {
      String buffer = localMessenger.lastCheckInEventToString();
      System.out.print(buffer);
      logDatabase(buffer);
    }
    try {
      localMessenger.save(newPath);
      localMessenger.saveJson(newPath);
      if (updatePath) {
        path = newPath;
      }
      //System.out.println("Save complete.");
      LOG.info("Save complete (" + newPath + ").");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(parent, e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  @Override
  public void saveDatabaseCsv(String path) {
    try {
      localMessenger.saveCsv(path);
      LOG.info("CSV save complete (" + path + ").");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(parent, e.getMessage(), "CSV Save Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  @Override
  public void saveDatabaseHoursByDayCsv(String path) {
    try {
      localMessenger.saveHoursByDayCsv(path);
      LOG.info("CSV save complete (" + path + ").");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(parent, e.getMessage(), "CSV Save Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
  }

  @Override
  public void saveDatabaseJson(String path) {
    try {
      localMessenger.saveJson(path);
      LOG.info("JSON save complete (" + path + ").");
    } catch (IOException e) {
      JOptionPane.showMessageDialog(parent, e.getMessage(), "JSON Save Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void logDatabase(String message) {
    if (logWriter == null) {
      try {
        logWriter = new FileWriter(path + File.separator + "operations.journal", true);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    if (logWriter == null) {
      return;
    }
    try {
      String buf = dateFormat.format(System.currentTimeMillis()) + ": " + message;
      logWriter.write(buf);
      LOG.info(buf);
      logWriter.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void runAutoSave(final String autoSavePath, final long minIntervalMillis,
      final long saveIntervalMillis, final boolean daemonThread) throws IOException {
    File dir = new File(autoSavePath);
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new IOException("Could not create directory " + autoSavePath + " for auto-saving database!");
      }
    }
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        long startTime = System.currentTimeMillis();
        while (true) {
          while (System.currentTimeMillis() - startTime < saveIntervalMillis /* && CheckInServer.getInstance().hasChanged() */) {
            try {
              Thread.sleep(minIntervalMillis);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
          startTime = System.currentTimeMillis();
          saveDatabase(autoSavePath, false);
          startTime = System.currentTimeMillis();
        }
      }
    }, "AutoSave");
    t.setDaemon(daemonThread);
    t.start();
  }

  private void exitApp() {
    saveDatabase();
    System.exit(0);
  }

  public static String getAndCreateCheckInAppDir() throws IOException {
    String appDirLocation = System.getProperty("user.home");
    if (appDirLocation == null) {
      appDirLocation = System.getProperty("user.dir");
    }
    //System.out.println("user.home : " + System.getProperty("user.home"));
    String appDirName = appDirLocation + File.separator + CHECK_IN_APP_DIR;
    File appDir = new File(appDirName);
    if (!appDir.isDirectory()) {
      if (appDir.exists()) {
        throw new IOException("Application directory name (" + appDir 
            + ") already exists, but is not a directory!  Please fix before continuing.");
      }
      boolean success = appDir.mkdirs();
      if (!success) {
        throw new IOException("Failed to create application directory " + appDir);
      }
    }
    return appDir.getAbsolutePath();
  }

  /**
   * Main application, with optional argument for serial port name.  Starts
   * up server, then scans IDs from terminal and/or Arduino via serial port.
   * @param args name of serial port (default is /dev/ttyACM0)
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    final String portName = (args.length == 1) ? args[0] : "/dev/ttyACM0";
    boolean startSerialPortScan = false;

    FileHandler fh = new FileHandler(getAndCreateCheckInAppDir() + File.separator
        + CHECK_IN_APP_DIR + ".log", 200 * 1024 * 1024, 5, true);
    LOG.addHandler(fh);
    fh.setFormatter(new SimpleFormatter());

    String path = getAndCreateCheckInAppDir() + File.separator + "test_createdatabase_check-in-server.dump";
    LocalMessenger localMessenger = new LocalMessenger(path);

    MainApp app = new MainApp(localMessenger);
    app.setPath(path);

    long timeStart = System.currentTimeMillis();
    long timeEnd = timeStart + 60L*60L*1000L;
    if (localMessenger.getActivity() == null) {
      CheckInActivity activity = new CheckInActivity("Default", timeStart, timeEnd);
      localMessenger.setActivity(activity);
    }

    if (LOG.isLoggable(Level.FINE)) {
      String buffer = localMessenger.lastCheckInEventToString();
      System.out.print(buffer);
      app.logDatabase(buffer);
    }
    app.runAutoSave(path + "_auto_save", 60L * 1000L, 120L * 1000L, true);
    app.scanIdsUi();
    if (startSerialPortScan) {
      scanIdsSerial(localMessenger, portName, true);
    }
    scanIdsTerminal(localMessenger);
    app.exitApp();
    //server.print();
    //server.dump(path);
  }

}
