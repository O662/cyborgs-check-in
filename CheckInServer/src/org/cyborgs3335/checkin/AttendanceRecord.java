package org.cyborgs3335.checkin;

import java.io.Serializable;
import java.util.ArrayList;

public class AttendanceRecord implements Serializable {

  private static final long serialVersionUID = -1254574907762887191L;
  private final Person person;
  private final ArrayList<CheckInEvent> eventList;

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
