package org.cyborgs3335.checkin.ui;

import java.util.Calendar;
import java.util.List;

import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;


public class CyclingSpinnerMonthListModel extends SpinnerListModel {

  private static final long serialVersionUID = -3849101049569888305L;

  private SpinnerNumberModel linkedModel = null;

  private SpinnerNumberModel dayModel = null;

  private final boolean useLongNames;

  private static String[] monthsOfYearLong = new String[] { "January", "February", "March", "April",
      "May", "June", "July", "August", "September", "October", "November", "December" };
  private static String[] monthsOfYearShort = new String[] { "Jan", "Feb", "Mar", "Apr",
      "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

  public CyclingSpinnerMonthListModel() {
    this(false);
  }

  public enum Month {
    January,
    February,
    March,
    April,
    May,
    June,
    July,
    August,
    September,
    October,
    November,
    December
  }

  public enum MonthAbbrev {
    Jan,
    Feb,
    Mar,
    Apr,
    May,
    Jun,
    Jul,
    Aug,
    Sep,
    Oct,
    Nov,
    Dec
  }

  public CyclingSpinnerMonthListModel(boolean useLongNames) {
    super(useLongNames ? Month.values() : MonthAbbrev.values());
    this.useLongNames = useLongNames;
  }

  public void setYearModel(SpinnerNumberModel yearModel) {
    this.linkedModel = yearModel;
  }

  public void setDayModel(SpinnerNumberModel dayModel) {
    this.dayModel = dayModel;
    //dayModel.setMaximum(maximum);
  }

  public int getMonth() {
    Object o = getValue();
    return useLongNames ? ((Month) o).ordinal() : ((MonthAbbrev) o).ordinal();
  }

  public void setMonth(int month) {
    if (useLongNames) {
      setValue(Month.values()[month]);
    } else {
      setValue(MonthAbbrev.values()[month]);
    }
  }

  @Override
  public void setValue(Object elt) {
    super.setValue(elt);
    int month = useLongNames ? ((Month) elt).ordinal() : ((MonthAbbrev) elt).ordinal();
    Calendar cal = Calendar.getInstance();
    cal.set(linkedModel.getNumber().intValue(), month, 1);
    int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    dayModel.setMaximum(maxDays);
    if (dayModel.getNumber().intValue() > maxDays) {
      dayModel.setValue(maxDays);
    }
  }

  public Object getNextValue() {
    Object value = super.getNextValue();
    if (value == null) {
      List<?> list = getList();
      if (list.isEmpty()) {
        return value;
      }
      value = list.get(0);
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getNextValue());
      }
    }
    return value;
  }

  public Object getPreviousValue() {
    Object value = super.getPreviousValue();
    if (value == null) {
      List<?> list = getList();
      if (list.isEmpty()) {
        return value;
      }
      value = list.get(list.size() - 1);
      if (linkedModel != null) {
        linkedModel.setValue(linkedModel.getPreviousValue());
      }
    }
    return value;
  }
}
