package org.cyborgs3335.checkin.messenger;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;

public interface IMessenger {

  public enum Action { CheckIn, CheckOut }

  public enum RequestResponse { Ok, UnknownId, FailedRequest }

  public static final String ACTIVITY_PROPERTY = "ACTIVITY_PROPERTY";

  public RequestResponse checkIn(long id) throws IOException, UnknownUserException;

  public RequestResponse checkOut(long id) throws IOException, UnknownUserException;

  public RequestResponse checkOutAll() throws IOException;

  public CheckInEvent.Status toggleCheckInStatus(long id) throws IOException, UnknownUserException;

  public CheckInEvent.Status getCheckInStatus(long id) throws IOException, UnknownUserException;

  public Person findPerson(String firstName, String lastName);

  public Person addPerson(String firstName, String lastName);

  /**
   * Set the current activity; e.g., name, start time, end time, ...
   * The {@link IMessenger#ACTIVITY_PROPERTY} property is fired when
   * a new activity is set.
   * @param activity current activity
   */
  public void setActivity(CheckInActivity activity);

  public CheckInActivity getActivity();

  public void addPropertyChangeListener(PropertyChangeListener listener);

  public void removePropertyChangeListener(PropertyChangeListener listener);

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  public String lastCheckInEventToString();

  public List<PersonCheckInEvent> getLastCheckInEventsSorted();

  public CheckInEvent getLastCheckInEvent(long id) throws IOException, UnknownUserException;

  public AttendanceRecord getAttendanceRecord(long id) throws IOException, UnknownUserException;

  public void close() throws IOException;
}