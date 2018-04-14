package org.cyborgs3335.checkin.ui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
//import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

public class DateTimeChooser extends JPanel {

  private static final long serialVersionUID = 134144424786502001L;

  private SpinnerNumberModel yearModel;
  private CyclingSpinnerMonthListModel monthModel;
  private CyclingSpinnerNumberModel dayModel;
  private CyclingSpinnerNumberModel hourModel;
  private CyclingSpinnerNumberModel minuteModel;
  //private SpinnerDateModel model;
  private final Calendar cal;
  private final String dateLabel;
  private final String timeLabel;
  private JSpinner[] spinners;

  public DateTimeChooser() {
    this(Calendar.getInstance());
  }

  public DateTimeChooser(Calendar cal) {
    this(cal, "Date: ", "Time: ");
  }

  public DateTimeChooser(Calendar cal, String dateLabel, String timeLabel) {
    super(new FlowLayout(FlowLayout.CENTER, 0, 0));
    this.cal = cal;
    this.dateLabel = dateLabel;
    this.timeLabel = timeLabel;
    build();
  }

  private void build() {
    // Remove fractional minutes
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    //model = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
    //JSpinner timeSpinner = new JSpinner(model);
    //JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "MM/dd/yyyy HH:mm");
    //timeSpinner.setEditor(timeEditor);

    yearModel = new SpinnerNumberModel();
    JSpinner yearSpinner = new JSpinner(yearModel);
    yearSpinner.setEditor(getNumberEditor(yearSpinner, "#"));
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
    daySpinner.setEditor(getNumberEditor(daySpinner, "#"));
    hourModel = new CyclingSpinnerNumberModel(0, 0, 23, 1);
    hourModel.setLinkedModel(dayModel);
    JSpinner hourSpinner = new JSpinner(hourModel);
    hourSpinner.setEditor(getNumberEditor(hourSpinner, "00"));
    minuteModel = new CyclingSpinnerNumberModel(0, 0, 55, 5);
    minuteModel.setLinkedModel(hourModel);
    JSpinner minuteSpinner = new JSpinner(minuteModel);
    minuteSpinner.setEditor(getNumberEditor(minuteSpinner, "00"));
    setValues(cal);

    //timeSpinner.addChangeListener(e -> updateFromOldDate());
    yearSpinner.addChangeListener(e -> updateFromNewDate());
    monthSpinner.addChangeListener(e -> updateFromNewDate());
    daySpinner.addChangeListener(e -> updateFromNewDate());
    hourSpinner.addChangeListener(e -> updateFromNewDate());
    minuteSpinner.addChangeListener(e -> updateFromNewDate());
    spinners = new JSpinner[] {
      yearSpinner, monthSpinner, daySpinner, hourSpinner, minuteSpinner
    };

    //add(timeSpinner);
    add(new JLabel(" "));
    add(new JLabel(dateLabel));
    add(monthSpinner);
    add(daySpinner);
    add(yearSpinner);
    add(new JLabel(" "));
    add(Box.createHorizontalStrut(5));
    add(new JLabel(timeLabel));
    add(hourSpinner);
    add(minuteSpinner);
  }

  private JSpinner.NumberEditor getNumberEditor(JSpinner spinner, String decimalFormatPattern) {
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, decimalFormatPattern);
    // Change bold font style back to plain
    spinner.setFont(new Font(spinner.getFont().getFamily(), Font.PLAIN, spinner.getFont().getSize()));
    return editor;
  }

  public void setDate(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    setValues(c);
  }

  public Date getDate() {
    Calendar cal = Calendar.getInstance();
    cal.set((int) yearModel.getNumber(), monthModel.getMonth(), (int) dayModel.getNumber(),
        (int) hourModel.getNumber(), (int) minuteModel.getNumber(), 0);
    //cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  @Override
  public synchronized void addFocusListener(FocusListener l) {
    for (JSpinner s : spinners) {
      s.addFocusListener(l);
    }
    super.addFocusListener(l);
  }

  public synchronized void addChangeListener(ChangeListener l) {
    for (JSpinner s : spinners) {
      s.addChangeListener(l);
    }
  }

  public void setDateEnabled(boolean b) {
    for (int i = 0; i < 3; i++) {
      spinners[i].setEnabled(b);
    }
    hourModel.setLinkedModel(b ? dayModel : null);
  }

  private void setValues(Calendar c) {
    yearModel.setValue(c.get(Calendar.YEAR));
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
      //UIManager.put("swing.boldMetal", Boolean.FALSE);
      createAndShowGUI();
    });
  }
}
