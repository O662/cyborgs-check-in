package org.cyborgs3335.checkin.ui;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
//import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cyborgs3335.checkin.ui.CyclingSpinnerMonthListModel.Month;

public class DateTimeChooser extends JPanel {

  private static final long serialVersionUID = 134144424786502001L;

  //private static String[] monthsOfYear = new String[] { "January", "February", "March", "April",
  //    "May", "June", "July", "August", "September", "October", "November", "December" };
  private static String[] monthsOfYear = new String[] { "Jan", "Feb", "Mar", "Apr",
      "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
  private SpinnerNumberModel yearModel;
  private CyclingSpinnerMonthListModel monthModel;
  private CyclingSpinnerNumberModel dayModel;
  private CyclingSpinnerNumberModel hourModel;
  private CyclingSpinnerNumberModel minuteModel;
  //private SpinnerDateModel model;
  private final Calendar cal;

  public DateTimeChooser() {
    this(Calendar.getInstance());
  }

  public DateTimeChooser(Calendar cal) {
    super(new FlowLayout(FlowLayout.CENTER, 0, 5));
    this.cal = cal;
    build();
  }

  private void build() {
    //Calendar cal = Calendar.getInstance();
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    //model = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
    //JSpinner timeSpinner = new JSpinner(model);
    //JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy HH:mm");
    //timeSpinner.setEditor(timeEditor);

    yearModel = new SpinnerNumberModel();
    JSpinner yearSpinner = new JSpinner(yearModel);
    yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
    monthModel = new CyclingSpinnerMonthListModel();//monthsOfYear);
    monthModel.setYearModel(yearModel);
    JSpinner monthSpinner = new JSpinner(monthModel);
    if (monthSpinner.getEditor() instanceof JSpinner.DefaultEditor) {
      JTextField tf = ((JSpinner.DefaultEditor) monthSpinner.getEditor()).getTextField();
      tf.setColumns(3);
      tf.setHorizontalAlignment(JTextField.LEFT);
    }
    dayModel = new CyclingSpinnerNumberModel(1, 1, 31, 1);
    dayModel.setLinkedModel(monthModel);
    monthModel.setDayModel(dayModel);
    JSpinner daySpinner = new JSpinner(dayModel);
    hourModel = new CyclingSpinnerNumberModel(0, 0, 23, 1);
    hourModel.setLinkedModel(dayModel);
    JSpinner hourSpinner = new JSpinner(hourModel);
    hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00"));
    minuteModel = new CyclingSpinnerNumberModel(0, 0, 55, 5);
    minuteModel.setLinkedModel(hourModel);
    JSpinner minuteSpinner = new JSpinner(minuteModel);
    minuteSpinner.setEditor(new JSpinner.NumberEditor(minuteSpinner, "00"));
    setValues(cal);

    //timeSpinner.addChangeListener(e -> updateFromOldDate());
    yearSpinner.addChangeListener(e -> updateFromNewDate());
    monthSpinner.addChangeListener(e -> updateFromNewDate());
    daySpinner.addChangeListener(e -> updateFromNewDate());
    hourSpinner.addChangeListener(e -> updateFromNewDate());
    minuteSpinner.addChangeListener(e -> updateFromNewDate());

    //add(timeSpinner);
    add(new JLabel(" "));
    add(new JLabel("Date: "));
    add(monthSpinner);
    add(daySpinner);
    add(yearSpinner);
    add(new JLabel(" "));
    add(new JLabel("Time: "));
    add(hourSpinner);
    add(minuteSpinner);
  }

  public void setDate(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    setValues(c);
  }

  public Date getDate() {
    Calendar cal = Calendar.getInstance();
    //cal.set((int) yearModel.getNumber(), getMonthFromString((String) monthModel.getValue()),
    //    (int) dayModel.getNumber(), (int) hourModel.getNumber(), (int) minuteModel.getNumber(), 0);
    cal.set((int) yearModel.getNumber(), monthModel.getMonth(),
        (int) dayModel.getNumber(), (int) hourModel.getNumber(), (int) minuteModel.getNumber(), 0);
    //cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  private void setValues(Calendar c) {
    yearModel.setValue(c.get(Calendar.YEAR));
    //monthModel.setValue(monthsOfYear[c.get(Calendar.MONTH)]);
    monthModel.setMonth(c.get(Calendar.MONTH));
    dayModel.setValue(c.get(Calendar.DAY_OF_MONTH));
    hourModel.setValue(c.get(Calendar.HOUR_OF_DAY));
    minuteModel.setValue((c.get(Calendar.MINUTE) / 5) * 5);
  }

  private void updateFromNewDate() {
    Date date = getDate();
    System.out.println("new date: " + date);
    //model.setValue(date);
  }

  /*
  private void updateFromOldDate() {
    Date date = model.getDate();
    System.out.println("old date: " + date);
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    setValues(c);
  }
  */

  private int getMonthFromString(String month) {
    int mon = 0;
    for (int i = 0; i < 12; i++) {
      if (month.equalsIgnoreCase(monthsOfYear[i])) {
        return i;
      }
    }
    return mon;
  }

  /*
  public class CyclingSpinnerListModel extends SpinnerListModel {
    private static final long serialVersionUID = -8316177225260661649L;
    Object firstValue, lastValue;
    SpinnerModel linkedModel = null;

    public CyclingSpinnerListModel(Object[] values) {
      super(values);
      firstValue = values[0];
      lastValue = values[values.length - 1];
    }

    public void setLinkedModel(SpinnerModel linkedModel) {
      this.linkedModel = linkedModel;
    }

    public Object getNextValue() {
      Object value = super.getNextValue();
      if (value == null) {
        value = firstValue;
        if (linkedModel != null) {
          linkedModel.setValue(linkedModel.getNextValue());
        }
      }
      return value;
    }

    public Object getPreviousValue() {
      Object value = super.getPreviousValue();
      if (value == null) {
        value = lastValue;
        if (linkedModel != null) {
          linkedModel.setValue(linkedModel.getPreviousValue());
        }
      }
      return value;
    }
  }
  */

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event dispatch thread.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("Date and Time Chooser Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Add content to the window.
    DateTimeChooser chooser = new DateTimeChooser();
    frame.add(chooser);

    // Print out info as application frame is closing.
    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        //System.out.println("Date 1 is " + chooser.model.getDate() + " " + chooser.model.getDate().getTime());
        System.out.println("Date 1 is " + chooser.getDate() + " " + chooser.getDate().getTime());
        super.windowClosing(e);
      }
    });

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      //Turn off metal's use of bold fonts
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      createAndShowGUI();
    });
  }
}
