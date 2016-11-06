package org.cyborgs3335.checkin;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Attendance record for a single person, consisting of a list of check-in events ({@link CheckInEvent}).
 *
 * @author brian
 *
 */
public class AttendanceRecord implements Serializable {

  private static final long serialVersionUID = -1254574907762887191L;
  private final Person person;
  private final ArrayList<CheckInEvent> eventList;

  /**
   * Constructor specifying person for attendance record.  Creates empty event list.
   * @param person person for this attendance record
   */
  public AttendanceRecord(Person person) {
    this.person = person;
    eventList = new ArrayList<CheckInEvent>();
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
}
