package org.cyborgs3335.checkin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 * Starts up server, then accepts IDs from the UI.
 *
 * @author Brian Macy
 *
 */
public class NewUserApp extends JFrame {

  private static final long serialVersionUID = 1926718100094660447L;
  private final DateFormat dateFormat;

  public NewUserApp() {
    //dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    dateFormat = new SimpleDateFormat();
    setSize(600, 400);
    build();
    setTitle("Check-In App: New Person");
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void build() {
    JMenuBar menubar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    //fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem exitMenuItem = new JMenuItem("Exit");//, KeyEvent.VK_X);
    fileMenu.add(exitMenuItem);
    menubar.add(fileMenu);
    setJMenuBar(menubar);
    
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Check In - Cyborgs 3335", SwingConstants.CENTER), BorderLayout.NORTH);
    
    JPanel parameterPanel = new JPanel();
    parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
    panel.add(parameterPanel, BorderLayout.CENTER);

    // Name
    final JTextField firstNameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("First Name"), firstNameField));

    final JTextField lastNameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Last Name"), lastNameField));

    final JLabel personStatusField = new JLabel("  ");
    parameterPanel.add(personStatusField);

    final JLabel checkInStatusField = new JLabel("  ");
    parameterPanel.add(checkInStatusField);

    // Start time
//    long currentTimeMillis = System.currentTimeMillis();
//    final JTextField timeStartField = new JTextField(dateFormat.format(currentTimeMillis), 20);
//    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeStartField));
//    parameterPanel.add(new JLabel("time=" + currentTimeMillis));

    // End time
//    final JTextField timeEndField = new JTextField(dateFormat.format(currentTimeMillis + 3600L * 1000L), 20);
//    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeEndField));
//    parameterPanel.add(new JLabel("time=" + (currentTimeMillis + 3600L * 1000L)));

    // Ok button
    JButton okButton = new JButton("Add");
    okButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        CheckInServer server = CheckInServer.getInstance();
        Person person = server.findPerson(firstName, lastName);
        String personText = null;
        if (person == null) {
          person = server.addUser(firstName, lastName);
          personText = "Added new person: " + firstName + " " + lastName + " id " + person.getId();
        } else {
          personText = "Found existing person: " + firstName + " " + lastName + " id " + person.getId();
        }
        System.out.println(personText);
        personStatusField.setText(personText);
        try {
          boolean checkIn = server.accept(person.getId());
          String status = checkIn ? "Checked in" : "Checked out";
          String text = status + " " + firstName + " " + lastName + " at " + dateFormat.format(new Date());
          System.out.println(text);
          checkInStatusField.setText(text);
        } catch (UnknownUserException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
//        String timeStartString = timeStartField.getText();
//        String timeEndString = timeEndField.getText();
//        try {
//          long timeStart = dateFormat.parse(timeStartString).getTime();
//          long timeEnd = dateFormat.parse(timeEndString).getTime();
//          CheckInActivity activity = new CheckInActivity(name, timeStart, timeEnd);
//          CheckInServer.getInstance().setActivity(activity);
//          CheckInServer.getInstance().print();
//          //setVisible(false);
//          dispose();
//        } catch (ParseException e1) {
//          // TODO Auto-generated catch block
//          e1.printStackTrace();
//        }
      }
    });
    JPanel buttonPanel = new JPanel();//new BorderLayout(5, 5));
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(okButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.EAST);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    this.add(panel);
  }

  private Box createSingleParameterPanel(JLabel label, JTextField field) {
    field.setMaximumSize(field.getPreferredSize());
    Box box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalStrut(5));
    box.add(label);
    box.add(Box.createHorizontalStrut(5));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(5));
    box.add(field);
    box.add(Box.createHorizontalStrut(5));
    return box;
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

  /**
   * Main application, with optional argument for serial port name.  Starts
   * up server, then scans IDs from terminal and/or Arduino via serial port.
   * @param args name of serial port (default is /dev/ttyACM0)
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    CheckInServer server = CheckInServer.getInstance();
    //String path = "/tmp/check-in-server-new-user-"+System.currentTimeMillis()+".dump";
    //String path = "/tmp/check-in-server-new-user.dump";
    String path = "/tmp/check-in-server-new-user-test.dump";
    File dir = new File(path);
    if (dir.exists()) {
      server.load(path);
    } else {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new RuntimeException("Could not create directory " + path + "for saving database!");
      }
    }

    long timeStart = System.currentTimeMillis();
    //long timeEnd = timeStart + 60L*60L*1000L;
    long timeEnd = timeStart + 5L*60L*60L*1000L;
    CheckInActivity activity = new CheckInActivity("Default", timeStart, timeEnd);
    server.setActivity(activity);

    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new NewUserApp();
      }
    });

    server.print();
    scanIdsTerminal();
    server.print();
    server.dump(path);

  }

}
