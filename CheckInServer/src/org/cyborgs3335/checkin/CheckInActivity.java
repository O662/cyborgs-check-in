package org.cyborgs3335.checkin;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckInActivity implements Serializable {

  private static final long serialVersionUID = 5464719124100199379L;

  private final String name;
  private final long timeStart;
  private final long timeEnd;

  public CheckInActivity(String name, long timeStart, long timeEnd) {
    this.name = name;
    this.timeStart = timeStart;
    this.timeEnd = timeEnd;
  }

  public String getName() {
    return name;
  }

  public long getStartTime() {
    return timeStart;
  }

  public long getEndTime() {
    return timeEnd;
  }

  public Date getStartDate() {
    return new Date(timeStart);
  }

  public Date getEndDate() {
    return new Date(timeEnd);
  }

  public void print() {
    print(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z"));
  }

  public void print(DateFormat dateFormat) {
    System.out.println("Activity " + name
        + " Start Time " + dateFormat.format(new Date(timeStart))
        + " End Time " + dateFormat.format(new Date(timeEnd)));
  }
}
