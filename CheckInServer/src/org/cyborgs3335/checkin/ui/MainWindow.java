package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInServer;
import org.cyborgs3335.checkin.IDatabaseOperations;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.UnknownUserException;

public class MainWindow extends JFrame {

  private static final long serialVersionUID = 1926718100094660447L;
  private final DateFormat dateFormat;
  private JTextField firstNameField;
  private JTextField lastNameField;
  private JLabel personStatusField;
  private JLabel checkInStatusField;
  private final IDatabaseOperations dbOperations;
  private final WindowListener windowListener;

  public MainWindow() {
    this(null, null);
  }

  public MainWindow(IDatabaseOperations databaseOperations, WindowListener listener) {
    //dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    dateFormat = new SimpleDateFormat();
    dbOperations = databaseOperations;
    windowListener = (listener != null) ? listener : new DefaultWindowListener();
    setSize(600, 400);
    build();
    setTitle("Check-In App");
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void build() {
    setJMenuBar(createMenuBar());
    
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Check In - Cyborgs 3335", SwingConstants.CENTER), BorderLayout.NORTH);

    JPanel parameterPanel = new JPanel();
    parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
    panel.add(parameterPanel, BorderLayout.CENTER);

    // Activity
    final JTextField nameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));

    // Start time
    long currentTimeMillis = System.currentTimeMillis();
    final JTextField timeStartField = new JTextField(dateFormat.format(currentTimeMillis), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeStartField));

    // End time
    final JTextField timeEndField = new JTextField(dateFormat.format(currentTimeMillis + 3600L * 1000L), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeEndField));

    // Name
    firstNameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("First Name"), firstNameField));

    lastNameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Last Name"), lastNameField));

    personStatusField = new JLabel("  ");
    parameterPanel.add(personStatusField);

    checkInStatusField = new JLabel("  ");
    parameterPanel.add(checkInStatusField);

    // Add button
    JButton addButton = new JButton("Add");
    addButton.addActionListener(new AddUserActionListener());

    JPanel buttonPanel = new JPanel();//new BorderLayout(5, 5));
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(addButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.EAST);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    // Set initial activity
    CheckInActivity activity = CheckInServer.getInstance().getActivity();
    if (activity != null) {
      nameField.setText(activity.getName());
      timeStartField.setText(dateFormat.format(activity.getStartDate()));
      timeEndField.setText(dateFormat.format(activity.getEndDate()));
    }
    // Listen for changes to activity
    CheckInServer.getInstance().addPropertyChangeListener(CheckInServer.ACTIVITY_PROPERTY, new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        CheckInActivity activity = CheckInServer.getInstance().getActivity(); 
        if (activity != null) {
          nameField.setText(activity.getName());
          timeStartField.setText(dateFormat.format(activity.getStartDate()));
          timeEndField.setText(dateFormat.format(activity.getEndDate()));
        }
      }
    });
    this.add(panel);
    this.addWindowListener(windowListener);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menubar = new JMenuBar();

    // File menu
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem loadMenuItem = new JMenuItem("Load...");
    loadMenuItem.setMnemonic(KeyEvent.VK_L);
    loadMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        loadDatabase();
      }
    });
    fileMenu.add(loadMenuItem);
    JMenuItem saveMenuItem = new JMenuItem("Save");
    saveMenuItem.setMnemonic(KeyEvent.VK_S);
    saveMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        saveDatabase();
      }
    });
    fileMenu.add(saveMenuItem);
    JMenuItem exitMenuItem = new JMenuItem("Exit");//, KeyEvent.VK_X);
    exitMenuItem.setMnemonic(KeyEvent.VK_X);
    exitMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        exitApp();
      }
    });
    fileMenu.add(exitMenuItem);
    menubar.add(fileMenu);

    // Edit menu
    JMenu editMenu = new JMenu("Edit");
    JMenuItem activityMenuItem = new JMenuItem("Set activity...");
    activityMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        new SessionWindow();
      }
    });
    editMenu.add(activityMenuItem);
    menubar.add(editMenu);

    return menubar;
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

  private class AddUserActionListener implements ActionListener {

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
    }
  }

  private void loadDatabase() {
    JOptionPane.showMessageDialog(this, "Load... not yet implemented", "Load... not yet implemented", JOptionPane.INFORMATION_MESSAGE);
    //throw new UnsupportedOperationException("loadDatabase() not yet implemented!");
  }

  private void saveDatabase() {
    dbOperations.saveDatabase();
    //JOptionPane.showMessageDialog(this, "Save not yet implemented", "Save not yet implemented", JOptionPane.INFORMATION_MESSAGE);
    //throw new UnsupportedOperationException("saveDatabase() not yet implemented!");
  }

  private void exitApp() {
    System.out.println("Sending window closing...");
    WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
    this.dispatchEvent(e);
    System.out.println("done");
    //System.exit(0);
  }

  private class DefaultWindowListener extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {
      System.out.println("Window closing received");
    }

    @Override
    public void windowClosed(WindowEvent e) {
      System.out.println("Window closed received");
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new MainWindow();
      }
    });
  }

}
