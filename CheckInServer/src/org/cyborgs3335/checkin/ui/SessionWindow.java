package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.server.local.LocalMessenger;

public class SessionWindow extends JFrame {

  private static final long serialVersionUID = 2232219714472239378L;

  private final DateFormat dateFormat;

  private JTextField nameField;

  private JLabel timeStartLabel;

  private SpinnerDateModel dateModelStart;

  private JLabel timeEndLabel;

  private SpinnerDateModel dateModelEnd;

  private JLabel timeLengthLabel;

  private final IMessenger messenger;

  public SessionWindow(IMessenger messenger) {
    //dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    dateFormat = new SimpleDateFormat();
    this.messenger = messenger;
    setSize(600, 400);
    build();
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void build() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Check In - CY-BORGS 3335", SwingConstants.CENTER), BorderLayout.NORTH);
    
    JPanel parameterPanel = new JPanel();
    parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
    panel.add(parameterPanel, BorderLayout.CENTER);

    ActionListener actionListener = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        update();
      }
    };
    FocusListener focusListener = new FocusListener() {

      @Override
      public void focusGained(FocusEvent e) {
        // Do nothing
      }

      @Override
      public void focusLost(FocusEvent e) {
        update();
      }
    };
    ChangeListener changeListener = new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        update();
      }
    };

    // Activity
    nameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));
    nameField.addActionListener(actionListener);
    nameField.addFocusListener(focusListener);

    // Start time (truncate to exact minutes)
    long timeStart = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(timeStart));
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    timeStart = cal.getTimeInMillis();
    dateModelStart = new SpinnerDateModel(new Date(timeStart), null, null, Calendar.MINUTE);
    JSpinner timeSpinnerStart = new JSpinner(dateModelStart);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeSpinnerStart));
    timeSpinnerStart.addChangeListener(changeListener);
    timeSpinnerStart.addFocusListener(focusListener);
    timeStartLabel = new JLabel("time=" + timeStart);
    parameterPanel.add(timeStartLabel);

    // End time
    long timeEnd = timeStart +3600L * 1000L;
    dateModelEnd = new SpinnerDateModel(new Date(timeEnd), null, null, Calendar.MINUTE);
    JSpinner timeSpinnerEnd = new JSpinner(dateModelEnd);
    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeSpinnerEnd));
    timeSpinnerEnd.addChangeListener(changeListener);
    timeSpinnerEnd.addFocusListener(focusListener);
    timeEndLabel = new JLabel("time=" + (timeEnd));
    parameterPanel.add(timeEndLabel);

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
        dateModelStart.setValue(activity.getStartDate());
        dateModelEnd.setValue(activity.getEndDate());
        update();
      }
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    this.add(panel);
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

  private Box createSingleParameterPanel(JLabel label, JComponent field) {
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

  private void update() {
    //try {
    long timeStart = dateModelStart.getDate().getTime();
    long timeEnd = dateModelEnd.getDate().getTime();
    timeStartLabel.setText("time=" + timeStart);
    timeEndLabel.setText("time=" + timeEnd);
    timeLengthLabel.setText(timeFormat(timeEnd - timeStart));
    //} catch (ParseException e) {
    //  e.printStackTrace();
    //  JOptionPane.showMessageDialog(this, e.getMessage(), "Date Format Parse Error", JOptionPane.ERROR_MESSAGE);
    //}
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
      long timeStart = dateModelStart.getDate().getTime();
      long timeEnd = dateModelEnd.getDate().getTime(); 
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
      System.out.println(messenger.lastCheckInEventToString());
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
