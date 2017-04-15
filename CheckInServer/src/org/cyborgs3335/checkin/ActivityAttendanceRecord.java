package org.cyborgs3335.checkin;

public class ActivityAttendanceRecord extends AttendanceRecord {

  private static final long serialVersionUID = -252846051460270585L;

  private final CheckInActivity activity;

  public ActivityAttendanceRecord(Person person) {
    this(person, null);
  }

  public ActivityAttendanceRecord(Person person, CheckInActivity activity) {
    super(person);
    this.activity = activity;
  }

  public CheckInActivity getActivity() {
    return activity;
  }
}
