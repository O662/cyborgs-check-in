package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInServer;

public class SessionWindow extends JFrame {

  private static final long serialVersionUID = 2232219714472239378L;

  private final DateFormat dateFormat;

  private JTextField nameField;

  private JTextField timeStartField;

  private JLabel timeStartLabel;

  private JTextField timeEndField;

  private JLabel timeEndLabel;

  private JLabel timeLengthLabel;

  public SessionWindow() {
    //dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    dateFormat = new SimpleDateFormat();
    setSize(600, 400);
    build();
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void build() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Check In - Cyborgs 3335", SwingConstants.CENTER), BorderLayout.NORTH);
    
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

    // Activity
    nameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));
    nameField.addActionListener(actionListener);
    nameField.addFocusListener(focusListener);

    // Start time
    long timeStart = System.currentTimeMillis();
    timeStartField = new JTextField(dateFormat.format(timeStart), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeStartField));
    timeStartField.addActionListener(actionListener);
    timeStartField.addFocusListener(focusListener);
    timeStartLabel = new JLabel("time=" + timeStart);
    parameterPanel.add(timeStartLabel);

    // End time
    long timeEnd = timeStart +3600L * 1000L;
    timeEndField = new JTextField(dateFormat.format(timeEnd), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeEndField));
    timeEndField.addActionListener(actionListener);
    timeEndField.addFocusListener(focusListener);
    timeEndLabel = new JLabel("time=" + (timeEnd));
    parameterPanel.add(timeEndLabel);

    // Time length
    timeLengthLabel = new JLabel("Time Length: " + timeFormat(timeEnd - timeStart));
    parameterPanel.add(timeLengthLabel);

    // Ok button
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new OkActionListener(this));
    JPanel buttonPanel = new JPanel();//new BorderLayout(5, 5));
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.WEST);
    buttonPanel.add(okButton);//, BorderLayout.CENTER);
    buttonPanel.add(Box.createHorizontalGlue());//, BorderLayout.EAST);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    CheckInActivity activity = CheckInServer.getInstance().getActivity();
    if (activity != null) {
      nameField.setText(activity.getName());
      timeStartField.setText(dateFormat.format(activity.getStartDate()));
      timeEndField.setText(dateFormat.format(activity.getEndDate()));
      update();
    }
    this.add(panel);
  }

  /**
   * Format time into a human readable format.
   * @param time time in milliseconds
   * @return time in human readable format
   */
  private String timeFormat(long time) {
    if (time < 1000L) {
      return String.format("%d msec", time);
    } else if (time < 60L * 1000L) {
      return String.format("%.3f sec", time/1000.0);
    } else if (time < 3600L * 1000L) {
      return String.format("%.3f min", time/(60L * 1000.0));
    } else if (time < 24L * 3600L * 1000L) {
      return String.format("%.3f hrs", time/(3600L * 1000.0));
    } else if (time < 365L * 24L * 3600L * 1000L) {
      return String.format("%.3f days", time/(24L * 3600L * 1000.0));
    }
    return String.format("%.3f yrs", time/(365L * 24L * 3600L * 1000.0));
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

  private void update() {
    String timeStartString = timeStartField.getText();
    String timeEndString = timeEndField.getText();
    try {
      long timeStart = dateFormat.parse(timeStartString).getTime();
      long timeEnd = dateFormat.parse(timeEndString).getTime();
      timeStartLabel.setText("time=" + timeStart);
      timeEndLabel.setText("time=" + timeEnd);
      timeLengthLabel.setText("Time Length: " + timeFormat(timeEnd - timeStart));
    } catch (ParseException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Date Format Parse Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new SessionWindow();
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
      String timeStartString = timeStartField.getText();
      String timeEndString = timeEndField.getText();
      try {
        long timeStart = dateFormat.parse(timeStartString).getTime();
        long timeEnd = dateFormat.parse(timeEndString).getTime();
        CheckInActivity activity = new CheckInActivity(name, timeStart, timeEnd);
        CheckInServer.getInstance().setActivity(activity);
        CheckInServer.getInstance().print();
        //setVisible(false);
        dispose();
      } catch (ParseException e1) {
        e1.printStackTrace();
        JOptionPane.showMessageDialog(parent, e1.getMessage(), "Date Format Parse Error", JOptionPane.ERROR_MESSAGE);
      }
    }

  }
}
