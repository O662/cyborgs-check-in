package org.cyborgs3335.checkin;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Attendance record for a single person, consisting of a list of check-in events ({@link CheckInEvent}).
 * 
 * Assumes first check-in event has a status of CheckedOut and a timestamp of 0;
 *
 * @author brian
 *
 */
public class AttendanceRecord implements Serializable {

  private static final long serialVersionUID = -1254574907762887191L;
  private static final Logger LOG = Logger.getLogger(AttendanceRecord.class.getName());
  private final Person person;
  private final ArrayList<CheckInEvent> eventList;
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");

  /**
   * Constructor specifying person for attendance record, with an existing event list.
   * @param person person for this attendance record
   */
  public AttendanceRecord(Person person, ArrayList<CheckInEvent> eventList) {
    this.person = person;
    this.eventList = eventList;
  }

  /**
   * Constructor specifying person for attendance record.  Creates empty event list.
   * @param person person for this attendance record
   */
  public AttendanceRecord(Person person) {
    this(person, new ArrayList<CheckInEvent>());
  }

  public Person getPerson() {
    return person;
  }

  public ArrayList<CheckInEvent> getEventList() {
    return eventList;
  }

  public CheckInEvent getLastEvent() {
    return eventList.get(eventList.size()-1);
  }

  public boolean areEventsConsistent() {
    if (!eventList.get(0).getStatus().equals(CheckInEvent.Status.CheckedOut)) {
      LOG.info(this.getClass().getName() + ": Inconsistent event for " + person
          + ": first event does not have status of CheckedOut");
      return false;
    }
    if (eventList.size() == 1) {
      return true;
    }
    for (int i = 1; i < eventList.size() - 1; i++) {
      if (eventList.get(i).getStatus().equals(eventList.get(i+1).getStatus())) {
        LOG.info(this.getClass().getName() + ": Inconsistent event for " + person
            + ": event " + i + " and " + (i + 1) + " have the same status of "
            + eventList.get(i).getStatus());
        return false;
      }
    }
    //if (getLastEvent().getStatus().equals(CheckInEvent.Status.CheckedIn)) {
    //  LOG.info(this.getClass().getName() + ": Inconsistent event for " + person
    //      + ": last event has a status of CheckedIn");
    //  return false;
    //}
    return true;
  }

  /**
   * Compute the total attendance time for all events.
   * @return total attendance time in milliseconds
   */
  public long computeTotalAttendanceTime() {
    if (!areEventsConsistent()) {
      return Long.MIN_VALUE;
    }
    if (eventList.size() == 1) {
      return 0L;
    }
    long timeTotal = 0;
    for (int i = 1; i < eventList.size() - 1; i += 2) {
      if (eventList.get(i).getTimeStamp() < 0) {
        LOG.info("less than zero for " + person);
      }
      long eventTime = eventList.get(i + 1).getTimeStamp() - eventList.get(i).getTimeStamp();
      /**/
      if (eventTime > 12L * 60L * 60L * 1000L) {
        LOG.info(this.getClass().getName() + ": Event time for " + person
            + " exceeds 12 hours: event " + i + " time " + (eventTime/1000.0/60.0/60.0));
            /*
            + " exceeds 12 hours: event start "
            + dateFormat.format(new Date(eventList.get(i).getTimeStamp()))
            + " end " + dateFormat.format(new Date(eventList.get(i + 1).getTimeStamp())));
            */
      }
      /**/
      timeTotal += eventTime;
    }
    if (getLastEvent().getStatus().equals(CheckInEvent.Status.CheckedIn)) {
      long eventTime = System.currentTimeMillis() - getLastEvent().getTimeStamp();
      /**/
      if (eventTime > 12L * 60L * 60L * 1000L) {
        LOG.info(this.getClass().getName() + ": Event time for " + person
            + " exceeds 12 hours: event " + (eventList.size() - 1) + " time " + (eventTime/1000.0/60.0/60.0));
            /*
            + " exceeds 12 hours: event start " + getLastEvent().getTimeStamp() + " end " + System.currentTimeMillis());/*
            + dateFormat.format(new Date(getLastEvent().getTimeStamp()))
            + " end " + dateFormat.format(new Date(System.currentTimeMillis())));*/
      }
      /**/
      timeTotal += eventTime;
    }
    return timeTotal;
  }

  public float[] getHoursByDay(long timeStampBegin, int numDays) {
    float[] vals = new float[numDays];
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(timeStampBegin);
    int startDay = cal.get(Calendar.DAY_OF_YEAR);
    int startYear = cal.get(Calendar.YEAR);

    if (!areEventsConsistent()) {
      return vals;
    }
    if (eventList.size() == 1) {
      return vals;
    }
    for (int i = 1; i < eventList.size() - 1; i += 2) {
      long timeStamp = eventList.get(i).getTimeStamp();
      if (timeStamp < 0) {
        LOG.info("less than zero for " + person);
      }
      long eventTime = eventList.get(i + 1).getTimeStamp() - timeStamp;
      cal.setTimeInMillis(timeStamp);
      int day = cal.get(Calendar.DAY_OF_YEAR);
      int year = cal.get(Calendar.YEAR);
      int index = (year - startYear) * 365 + (day - startDay); // TODO account for leap years!!!
      vals[index] = (float) (eventTime / (1000.0 * 60 * 60));
      /*
      if (eventTime > 12L * 60L * 60L * 1000L) {
        LOG.info(this.getClass().getName() + ": Event time for " + person
            + " exceeds 12 hours: event " + i + " time " + (eventTime/1000.0/60.0/60.0));
            /*
            + " exceeds 12 hours: event start "
            + dateFormat.format(new Date(eventList.get(i).getTimeStamp()))
            + " end " + dateFormat.format(new Date(eventList.get(i + 1).getTimeStamp())));
            */
      /*
      }
      /**/
    }
    if (getLastEvent().getStatus().equals(CheckInEvent.Status.CheckedIn)) {
      long timeStamp = getLastEvent().getTimeStamp();
      long eventTime = System.currentTimeMillis() - timeStamp;
      cal.setTimeInMillis(timeStamp);
      int day = cal.get(Calendar.DAY_OF_YEAR);
      int year = cal.get(Calendar.YEAR);
      int index = (year - startYear) * 365 + (day - startDay); // TODO account for leap years!!!
      vals[index] = (float) (eventTime / (1000.0 * 60 * 60));
      /*
      if (eventTime > 12L * 60L * 60L * 1000L) {
        LOG.info(this.getClass().getName() + ": Event time for " + person
            + " exceeds 12 hours: event " + (eventList.size() - 1) + " time " + (eventTime/1000.0/60.0/60.0));
            /*
            + " exceeds 12 hours: event start " + getLastEvent().getTimeStamp() + " end " + System.currentTimeMillis());/*
            + dateFormat.format(new Date(getLastEvent().getTimeStamp()))
            + " end " + dateFormat.format(new Date(System.currentTimeMillis())));*/
      /*
      }
      /**/
    }
    return vals;
  }

  /*
  public double getHoursByDay(Date startDate, Date endDate) {
    Calendar scal = Calendar.getInstance();
    scal.setTime(startDate);
    int sday = scal.get(Calendar.DAY_OF_YEAR);
    Calendar ecal = Calendar.getInstance();
    ecal.setTime(endDate);
    int eday = ecal.get(Calendar.DAY_OF_YEAR);
    System.out.println("Start day is " + sday + " end day is " + eday);
    return 0;
  }
  */
}
