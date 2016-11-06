package org.cyborgs3335.checkin;

import java.io.Serializable;

public class CheckInEvent implements Serializable {

  private static final long serialVersionUID = 8447815623071049866L;

  public enum Status { CheckedIn, CheckedOut }

  private final Status status;
  private final long timeStamp;

  public CheckInEvent(Status status, long timeStamp) {
    this.status = status;
    this.timeStamp = timeStamp;
  }

  public Status getStatus() {
    return status;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

}
