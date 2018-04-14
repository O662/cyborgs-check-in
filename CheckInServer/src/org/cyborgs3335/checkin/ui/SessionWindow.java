package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.server.local.LocalMessenger;

public class SessionWindow extends JFrame {

  private static final long serialVersionUID = 2232219714472239378L;

  private String title;

  private JTextField nameField;

  //private JLabel timeStartLabel;

  //private SpinnerDateModel dateModelStart;

  //private JLabel timeEndLabel;

  //private SpinnerDateModel dateModelEnd;

  private JLabel timeLengthLabel;

  private DateTimeChooser dateTimeStart;

  private DateTimeChooser dateTimeEnd;

  private final IMessenger messenger;

  public SessionWindow(IMessenger messenger) {
    this(messenger, "Check-In Activity");
  }

  public SessionWindow(IMessenger messenger, String title) {
    this.messenger = messenger;
    this.title = title;
    setSize(600, 400);
    setTitle(title);
    build();
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void build() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);

    JPanel parameterPanel = new JPanel();
    parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
    panel.add(parameterPanel, BorderLayout.CENTER);

    ActionListener actionListener = e -> update();
    ChangeListener startChangeListener = e -> startUpdate();
    ChangeListener endChangeListener = e -> endUpdate();
    FocusListener focusListener = new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        update();
      }
    };
    FocusListener startFocusListener = new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        startUpdate();
      }
    };
    FocusListener endFocusListener = new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        endUpdate();
      }
    };

    // Activity
    nameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));
    nameField.addActionListener(actionListener);
    nameField.addFocusListener(focusListener);

    // Current date (truncate start time to exact minutes)
    long timeStart = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(timeStart));
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    timeStart = cal.getTimeInMillis();
    JCheckBox checkBox = new JCheckBox("Use Current Date");
    checkBox.addItemListener(e -> useCurrentDate(cal, e));
    parameterPanel.add(createSingleParameterPanel(checkBox,
        new JLabel(new SimpleDateFormat("MMM d, yyyy").format(new Date(timeStart)))));

    // Start time
    Calendar calStart = Calendar.getInstance();
    calStart.setTimeInMillis(timeStart);
    dateTimeStart = new DateTimeChooser(calStart, "Start Date: ", "Time: ");
    dateTimeStart.addChangeListener(startChangeListener);
    dateTimeStart.addFocusListener(startFocusListener);
    parameterPanel.add(createSingleParameterPanel(new JLabel(""), dateTimeStart, 10));

    // End time
    long timeEnd = timeStart +3600L * 1000L;
    Calendar calEnd = Calendar.getInstance();
    calEnd.setTimeInMillis(timeEnd);
    dateTimeEnd = new DateTimeChooser(calEnd, "End Date: ", "Time: ");
    dateTimeEnd.addChangeListener(endChangeListener);
    dateTimeEnd.addFocusListener(endFocusListener);
    parameterPanel.add(createSingleParameterPanel(new JLabel(""), dateTimeEnd, 10));

    // Time length
    timeLengthLabel = new JLabel(timeFormat(timeEnd - timeStart));
    parameterPanel.add(createSingleParameterPanel(new JLabel("Time Length"), timeLengthLabel));

    // Ok button
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new OkActionListener(this));

    // Cancel button
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new CancelActionListener());

    // Button panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalGlue());
    panel.add(buttonPanel, BorderLayout.SOUTH);

    CheckInActivity activity;
    try {
      activity = messenger.getActivity();
      if (activity != null) {
        nameField.setText(activity.getName());
        dateTimeStart.setDate(activity.getStartDate());
        dateTimeEnd.setDate(activity.getEndDate());
        update();
      }
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    this.add(panel);
  }

  private void useCurrentDate(Calendar calCurrent, ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      Calendar calStart = Calendar.getInstance();
      calStart.setTime(dateTimeStart.getDate());
      calStart.set(calCurrent.get(Calendar.YEAR), calCurrent.get(Calendar.MONTH), calCurrent.get(Calendar.DAY_OF_MONTH));
      dateTimeStart.setDate(calStart.getTime());
      dateTimeStart.setDateEnabled(false);

      Calendar calEnd = Calendar.getInstance();
      calEnd.setTime(dateTimeEnd.getDate());
      calEnd.set(calCurrent.get(Calendar.YEAR), calCurrent.get(Calendar.MONTH), calCurrent.get(Calendar.DAY_OF_MONTH));
      dateTimeEnd.setDate(calEnd.getTime());
      dateTimeEnd.setDateEnabled(false);
    } else {
      dateTimeStart.setDateEnabled(true);
      dateTimeEnd.setDateEnabled(true);
    }
    update();
  }

  /**
   * Format time into a human readable format.
   * @param time time in milliseconds
   * @return time in human readable format
   */
  private String timeFormat(long time) {
    if (Math.abs(time) < 1000L) {
      return String.format("%d msec", time);
    } else if (Math.abs(time) < 60L * 1000L) {
      return String.format("%.3f sec", time/1000.0);
    } else if (Math.abs(time) < 3600L * 1000L) {
      return String.format("%.3f min", time/(60L * 1000.0));
    } else if (Math.abs(time) < 24L * 3600L * 1000L) {
      return String.format("%.3f hrs", time/(3600L * 1000.0));
    } else if (Math.abs(time) < 365L * 24L * 3600L * 1000L) {
      return String.format("%.3f days", time/(24L * 3600L * 1000.0));
    }
    return String.format("%.3f yrs", time/(365L * 24L * 3600L * 1000.0));
  }

  private Box createSingleParameterPanel(JComponent label, JComponent field) {
    return createSingleParameterPanel(label, field, 5);
  }

  private Box createSingleParameterPanel(JComponent label, JComponent field, int centerStrutLength) {
    field.setMaximumSize(field.getPreferredSize());
    Box box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalStrut(5));
    box.add(label);
    box.add(Box.createHorizontalStrut(centerStrutLength));
    box.add(Box.createHorizontalGlue());
    box.add(Box.createHorizontalStrut(centerStrutLength));
    box.add(field);
    box.add(Box.createHorizontalStrut(5));
    return box;
  }

  private void update() {
    long timeStart = dateTimeStart.getDate().getTime();
    long timeEnd = dateTimeEnd.getDate().getTime();
    timeLengthLabel.setText(timeFormat(timeEnd - timeStart));
  }

  private void startUpdate() {
    long timeStart = dateTimeStart.getDate().getTime();
    long timeEnd = dateTimeEnd.getDate().getTime();
    if (timeStart > timeEnd) {
      Calendar calStart = Calendar.getInstance();
      calStart.setTime(dateTimeStart.getDate());
      Calendar calEnd = Calendar.getInstance();
      calEnd.setTime(dateTimeEnd.getDate());
      calEnd.set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH));
      dateTimeEnd.setDate(calEnd.getTime());
    }
    update();
  }

  private void endUpdate() {
    long timeStart = dateTimeStart.getDate().getTime();
    long timeEnd = dateTimeEnd.getDate().getTime();
    if (timeStart > timeEnd) {
      Calendar calStart = Calendar.getInstance();
      calStart.setTime(dateTimeStart.getDate());
      Calendar calEnd = Calendar.getInstance();
      calEnd.setTime(dateTimeEnd.getDate());
      calStart.set(calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH));
      dateTimeStart.setDate(calStart.getTime());
    }
    update();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        final String path = "/tmp/check-in-server-session-window-test.dump";
        IMessenger messenger;
        try {
          messenger = new LocalMessenger(path);
          new SessionWindow(messenger);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
  }

  private class OkActionListener implements ActionListener {

    private final Component parent;

    public OkActionListener(Component parentComponent) {
      parent = parentComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String name = nameField.getText();
      name = name.trim();
      if (name.isEmpty()) {
        JOptionPane.showMessageDialog(parent, "Activity Name must not be blank", "Invalid Activity Name", JOptionPane.ERROR_MESSAGE);
        return;
      }
      //try {
      long timeStart = dateTimeStart.getDate().getTime();
      long timeEnd = dateTimeEnd.getDate().getTime();
      if (timeStart > timeEnd) {
        JOptionPane.showMessageDialog(parent, "End Time must be greater than Start Time", "Invalid Time Range", JOptionPane.ERROR_MESSAGE);
        return;
      }
      CheckInActivity activity = new CheckInActivity(name, timeStart, timeEnd);
      try {
        messenger.setActivity(activity);
      } catch (IOException e1) {
        e1.printStackTrace();
        JOptionPane.showMessageDialog(parent, e1.getMessage(), "Error setting activity", JOptionPane.ERROR_MESSAGE);
      }
      try {
        System.out.println(messenger.lastCheckInEventToString());
      } catch (IOException e1) {
        System.out.println("Received IOException when fetching check-in events: " + e1.getMessage());
        e1.printStackTrace();
      }
      //setVisible(false);
      dispose();
      //} catch (ParseException e1) {
      //  e1.printStackTrace();
      //  JOptionPane.showMessageDialog(parent, e1.getMessage(), "Date Format Parse Error", JOptionPane.ERROR_MESSAGE);
      //}
    }

  }

  private class CancelActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      dispose();
    }
    
  }
}
