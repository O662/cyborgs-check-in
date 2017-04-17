package org.cyborgs3335.checkin;

/**
 * @deprecated This is probably not the best approach.  Will leave for the time being, but is not currently used.
 * @author brian
 *
 */
@Deprecated
public class ActivityAttendanceRecord extends AttendanceRecord {

  private static final long serialVersionUID = -252846051460270585L;

  private final CheckInActivity activity;

  @Deprecated
  public ActivityAttendanceRecord(Person person) {
    this(person, null);
  }

  @Deprecated
  public ActivityAttendanceRecord(Person person, CheckInActivity activity) {
    super(person);
    this.activity = activity;
  }

  @Deprecated
  public CheckInActivity getActivity() {
    return activity;
  }
}
