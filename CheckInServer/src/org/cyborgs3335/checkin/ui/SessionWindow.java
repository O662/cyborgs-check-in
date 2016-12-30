package org.cyborgs3335.checkin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInServer;

public class SessionWindow extends JFrame {

  private static final long serialVersionUID = 2232219714472239378L;

  private final DateFormat dateFormat;

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

    // Activity
    final JTextField nameField = new JTextField(20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Activity Name"), nameField));

    // Start time
    long currentTimeMillis = System.currentTimeMillis();
    final JTextField timeStartField = new JTextField(dateFormat.format(currentTimeMillis), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("Start Time"), timeStartField));
    parameterPanel.add(new JLabel("time=" + currentTimeMillis));

    // End time
    final JTextField timeEndField = new JTextField(dateFormat.format(currentTimeMillis + 3600L * 1000L), 20);
    parameterPanel.add(createSingleParameterPanel(new JLabel("End Time"), timeEndField));
    parameterPanel.add(new JLabel("time=" + (currentTimeMillis + 3600L * 1000L)));

    // Ok button
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {

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
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
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

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        new SessionWindow();
      }
    });
  }

}
