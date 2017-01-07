package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInServer;
import org.cyborgs3335.checkin.IDatabaseOperations;
import org.cyborgs3335.checkin.MainApp;
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
  private JTextArea textArea;
  private JButton searchButton;
  private JButton checkInButton;
  private JButton checkOutButton;
  private JButton addButton;
  private JButton clearButton;

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

    int hgap = 5;
    int vgap = 5;
    int textFieldLength = 15;

    JPanel panel = new JPanel(new BorderLayout(hgap, vgap));
    //panel.add(new JLabel("Check In - CY-BORGS 3335", SwingConstants.CENTER), BorderLayout.NORTH);

    JPanel logoPanel = new JPanel(new BorderLayout(25, 25));
    logoPanel.add(Box.createHorizontalStrut(hgap), BorderLayout.EAST);
    logoPanel.add(Box.createHorizontalStrut(hgap), BorderLayout.WEST);
    logoPanel.add(Box.createVerticalStrut(hgap), BorderLayout.NORTH);
    logoPanel.add(Box.createVerticalStrut(hgap), BorderLayout.SOUTH);
    logoPanel.add(new JLabel(getIcon()), BorderLayout.CENTER);
    panel.add(logoPanel, BorderLayout.WEST);

    //JPanel parameterPanel = new JPanel();
    //parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
    Box parameterPanel = new Box(BoxLayout.Y_AXIS);
//    panel.add(parameterPanel, BorderLayout.CENTER);

    // Activity
    final JTextField nameField = new JTextField(textFieldLength);
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));
    nameField.setEditable(false);

    // Start time
    long currentTimeMillis = System.currentTimeMillis();
    final JTextField timeStartField = new JTextField(dateFormat.format(currentTimeMillis), textFieldLength);
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeStartField));
    timeStartField.setEditable(false);

    // End time
    final JTextField timeEndField = new JTextField(dateFormat.format(currentTimeMillis + 3600L * 1000L), textFieldLength);
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeEndField));
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(Box.createVerticalGlue());
    timeEndField.setEditable(false);

    // Name
    firstNameField = new JTextField(textFieldLength);
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(createSingleParameterPanel(new JLabel("First Name"), firstNameField));

    lastNameField = new JTextField(textFieldLength);
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(createSingleParameterPanel(new JLabel("Last Name"), lastNameField));

    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(Box.createVerticalGlue());

    personStatusField = new JLabel("  ");
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(personStatusField);

    checkInStatusField = new JLabel("  ");
    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(checkInStatusField);

    parameterPanel.add(Box.createVerticalStrut(vgap));
    parameterPanel.add(Box.createVerticalGlue());
    parameterPanel.add(Box.createVerticalStrut(vgap));

    Box parameterBox = new Box(BoxLayout.X_AXIS);
    parameterBox.add(Box.createHorizontalStrut(hgap));
    parameterBox.add(Box.createHorizontalGlue());
    parameterBox.add(Box.createHorizontalStrut(hgap));
    parameterBox.add(parameterPanel);
    parameterBox.add(Box.createHorizontalStrut(hgap));
    parameterBox.add(Box.createHorizontalGlue());
    parameterBox.add(Box.createHorizontalStrut(hgap));

    // Search button
    searchButton = new JButton("Search");
    searchButton.addActionListener(new SearchUserActionListener());

    // Check in button
    checkInButton = new JButton("Check In");
    checkInButton.addActionListener(new CheckInUserActionListener());
    checkInButton.setEnabled(false);

    // Check out button
    checkOutButton = new JButton("Check Out");
    checkOutButton.addActionListener(new CheckOutUserActionListener());
    checkOutButton.setEnabled(false);

    // Add button
    addButton = new JButton("Add New Person");
    addButton.addActionListener(new AddUserActionListener());
    addButton.setEnabled(false);

    // Clear button
    clearButton = new JButton("Done");
    clearButton.addActionListener(new ClearUserActionListener());
    clearButton.setEnabled(false);

    JPanel buttonPanel = new JPanel();//new BorderLayout(5, 5));
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(searchButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(checkInButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(checkOutButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(addButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.EAST);
    buttonPanel.add(clearButton);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.EAST);
    //panel.add(buttonPanel, BorderLayout.SOUTH);

    JPanel titlePanel = new JPanel();
    titlePanel.add(new JLabel("Check In - CY-BORGS 3335"));//, SwingConstants.CENTER));

    Box centerPanel = new Box(BoxLayout.Y_AXIS);
    centerPanel.add(Box.createVerticalStrut(vgap));
    centerPanel.add(titlePanel);//, SwingConstants.CENTER));
    centerPanel.add(Box.createVerticalStrut(vgap));
    //centerPanel.add(parameterPanel);
    centerPanel.add(parameterBox);
    centerPanel.add(Box.createVerticalStrut(vgap));
    centerPanel.add(Box.createVerticalGlue());
    centerPanel.add(Box.createVerticalStrut(vgap));
    centerPanel.add(buttonPanel);
    centerPanel.add(Box.createVerticalStrut(vgap));
    panel.add(centerPanel, BorderLayout.CENTER);

    JPanel textPanel = new JPanel();
    textArea = new JTextArea();//10, 80);
    textArea.setEditable(false);
    textArea.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        try {
          String newText = e.getDocument().getText(e.getOffset(), e.getLength());
          dbOperations.logDatabase(newText);
          //System.out.println(MainApp.getAndCreateCheckInAppDir());
        } catch (BadLocationException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        //System.out.println("insert");
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        //System.out.println("remove");
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        //System.out.println("change");
      }
    });
    JScrollPane pane = new JScrollPane(textArea);
    pane.setPreferredSize(new Dimension(880, 150));
    textPanel.add(pane);

    Box bottomBox = new Box(BoxLayout.Y_AXIS);
//    bottomBox.add(Box.createVerticalStrut(vgap));
//    bottomBox.add(buttonPanel);
    bottomBox.add(Box.createVerticalStrut(vgap));
    bottomBox.add(textPanel);
    bottomBox.add(Box.createVerticalStrut(vgap));
    panel.add(bottomBox, BorderLayout.SOUTH);

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
    loadMenuItem.setEnabled(false);
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

    // View menu
    final JMenu viewMenu = new JMenu("View");
    JMenuItem viewRecordsMenuItem = new JMenuItem("View records...");
    viewRecordsMenuItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        JTextArea recordArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(recordArea);
        scrollPane.setPreferredSize(new Dimension(880, 450));
        String buffer = CheckInServer.getInstance().printToString();
        recordArea.append(buffer);
        JOptionPane.showMessageDialog(viewMenu, scrollPane, "Attendance Records", JOptionPane.PLAIN_MESSAGE);
      }
    });
    viewMenu.add(viewRecordsMenuItem);
    menubar.add(viewMenu);

    return menubar;
  }

  private Box createSingleParameterPanel(JLabel label, JTextField field) {
    int hgap = 5;
    field.setMaximumSize(field.getPreferredSize());
    Box box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalStrut(hgap));
    box.add(label);
    box.add(Box.createHorizontalStrut(hgap));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(hgap));
    box.add(field);
    box.add(Box.createHorizontalStrut(hgap));
    return box;
  }

  private class SearchUserActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String firstName = firstNameField.getText();
      String newFirstName = firstName.trim();
      if (!firstName.equals(newFirstName)) {
        firstNameField.setText(newFirstName);
        firstName = newFirstName;
      }
      String lastName = lastNameField.getText();
      String newLastName = lastName.trim();
      if (!lastName.equals(newLastName)) {
        lastNameField.setText(newLastName);
        lastName = newLastName;
      }
      if (firstName.isEmpty() || lastName.isEmpty()) {
        String personText = "Both First Name and Last Name must be populated.";
        personStatusField.setText(personText);
        return;
      }
      CheckInServer server = CheckInServer.getInstance();
      Person person = server.findPerson(firstName, lastName);
      String personText = null;
      if (person == null) {
        personText = "New person: " + firstName + " " + lastName;
        personStatusField.setText(personText);
        addButton.setEnabled(true);
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        clearButton.setEnabled(true);
        return;
      } else {
        if (!firstName.equals(person.getFirstName()) || !lastName.equals(person.getLastName())) {
          firstName = person.getFirstName();
          firstNameField.setText(firstName);
          lastName = person.getLastName();
          lastNameField.setText(lastName);
        }
        personText = "Found existing person: " + firstName + " " + lastName + " ID " + person.getId();
      }
      //System.out.println(personText);
      //textArea.append(personText + "\n");
      personStatusField.setText(personText);
      addButton.setEnabled(false);
      CheckInEvent lastEvent = server.getAttendanceRecord(person.getId()).getLastEvent();
      CheckInEvent.Status status = lastEvent.getStatus();
      boolean checkInState = (status.equals(CheckInEvent.Status.CheckedIn));
      String checkInString = checkInState ? "Checked in" : "Checked out";
      checkInStatusField.setText("Status: " + checkInString + " at " + dateFormat.format(lastEvent.getTimeStamp()));
      checkInButton.setEnabled(!checkInState);
      checkOutButton.setEnabled(checkInState);
      clearButton.setEnabled(true);
    }
  }

  private class CheckInUserActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String firstName = firstNameField.getText();
      String lastName = lastNameField.getText();
      CheckInServer server = CheckInServer.getInstance();
      Person person = server.findPerson(firstName, lastName);
      String personText = null;
      if (person == null) {
        personText = "New person: " + firstName + " " + lastName;
        personStatusField.setText(personText);
        checkInStatusField.setText(firstName + " " + lastName + " does not exist in database. Add?");
        addButton.setEnabled(true);
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        clearButton.setEnabled(true);
        return;
      } else {
        personText = "Found existing person: " + firstName + " " + lastName + " ID " + person.getId();
      }
      //System.out.println(personText);
      //textArea.append(personText + "\n");
      personStatusField.setText(personText);
      addButton.setEnabled(false);
      clearButton.setEnabled(true);
      firstNameField.setEditable(false);
      lastNameField.setEditable(false);
      try {
        boolean checkIn = server.accept(person.getId());
        String status = checkIn ? "Checked in" : "Checked out";
        String text = status + " " + firstName + " " + lastName + " at " + dateFormat.format(new Date());
        System.out.println(text);
        textArea.append(text + "\n");
        checkInStatusField.setText(text);
        searchButton.setEnabled(false);
        if (!checkIn) {
          String statusText = "Expected to check in " + firstName + " " + lastName
              + ", but was checked out instead!";
          System.out.println(statusText);
          textArea.append(statusText);
          checkInButton.setEnabled(true);
          checkOutButton.setEnabled(false);
        } else {
          checkInButton.setEnabled(false);
          checkOutButton.setEnabled(true);
        }
      } catch (UnknownUserException e1) {
        // TODO Auto-generated catch block
        textArea.append(e1.getMessage());
        e1.printStackTrace();
      }
    }
  }

  private class CheckOutUserActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      String firstName = firstNameField.getText();
      String lastName = lastNameField.getText();
      CheckInServer server = CheckInServer.getInstance();
      Person person = server.findPerson(firstName, lastName);
      String personText = null;
      if (person == null) {
        personText = "New person: " + firstName + " " + lastName;
        personStatusField.setText(personText);
        checkInStatusField.setText(firstName + " " + lastName + " does not exist in database. Add?");
        addButton.setEnabled(true);
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        clearButton.setEnabled(true);
        return;
      } else {
        personText = "Found existing person: " + firstName + " " + lastName + " ID " + person.getId();
      }
      //System.out.println(personText);
      //textArea.append(personText + "\n");
      personStatusField.setText(personText);
      addButton.setEnabled(false);
      clearButton.setEnabled(true);
      firstNameField.setEditable(false);
      lastNameField.setEditable(false);
      try {
        boolean checkIn = server.accept(person.getId());
        String status = checkIn ? "Checked in" : "Checked out";
        String text = status + " " + firstName + " " + lastName + " at " + dateFormat.format(new Date());
        System.out.println(text);
        textArea.append(text + "\n");
        checkInStatusField.setText(text);
        searchButton.setEnabled(false);
        if (checkIn) {
          String statusText = "Expected to check out " + firstName + " " + lastName
              + ", but was checked in instead!";
          System.out.println(statusText);
          textArea.append(statusText);
          checkInButton.setEnabled(false);
          checkOutButton.setEnabled(true);
        } else {
          checkInButton.setEnabled(true);
          checkOutButton.setEnabled(false);
        }
      } catch (UnknownUserException e1) {
        // TODO Auto-generated catch block
        textArea.append(e1.getMessage());
        e1.printStackTrace();
      }
    }
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
        personText = "Added new person: " + firstName + " " + lastName + " ID " + person.getId();
        textArea.append(personText + "\n");
        addButton.setEnabled(false);
        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
      } else {
        personText = "Found existing person: " + firstName + " " + lastName + " ID " + person.getId();
      }
      searchButton.setEnabled(false);
      System.out.println(personText);
      personStatusField.setText(personText);
      firstNameField.setEditable(false);
      lastNameField.setEditable(false);
      try {
        boolean checkIn = server.accept(person.getId());
        String status = checkIn ? "Checked in" : "Checked out";
        String text = status + " " + firstName + " " + lastName + " at " + dateFormat.format(new Date());
        System.out.println(text);
        textArea.append(text + "\n");
        checkInStatusField.setText(text);
        checkInButton.setEnabled(!checkIn);
        checkOutButton.setEnabled(checkIn);
        clearButton.setEnabled(true);
      } catch (UnknownUserException e1) {
        // TODO Auto-generated catch block
        textArea.append(e1.getMessage());
        e1.printStackTrace();
      }
    }
  }

  private class ClearUserActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      firstNameField.setEditable(true);
      lastNameField.setEditable(true);
      firstNameField.setText("");
      lastNameField.setText("");
      personStatusField.setText("  ");
      checkInStatusField.setText("  ");
      searchButton.setEnabled(true);
      addButton.setEnabled(false);
      checkInButton.setEnabled(false);
      checkOutButton.setEnabled(false);
    }
  }

  protected Icon getIcon() {
    //String iconName = "resources/images/Cy-borgs-logo-402x402.png";
    String iconName = "resources/images/Cy-borgs-logo-301x301.png";
    //String iconName = "resources/images/Cy-borgs-logo-201x201.png";
    return new ImageIcon(getClass().getClassLoader()
        .getResource(iconName), "Cy-borgs logo");
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
