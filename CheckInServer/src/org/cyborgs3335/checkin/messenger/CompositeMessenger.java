/**
 * 
 */
package org.cyborgs3335.checkin.messenger;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;
import org.cyborgs3335.checkin.server.local.LocalMessenger;


/**
 * Messenger composed of multiple messengers, such as an HttpMessenger and LocalMessenger.
 * Another CompositeMessenger could also be used.
 *
 * @author Brian Macy
 *
 */
public class CompositeMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(CompositeMessenger.class.getName());

  private final IMessenger messenger1;

  private final IMessenger messenger2;

  /**
   * Constructor specifying two messengers. The methods call the messengers in order.
   * @param firstMessenger the first messenger
   * @param secondMessenger the second messenger
   */
  public CompositeMessenger(IMessenger firstMessenger, IMessenger secondMessenger) {
    messenger1 = firstMessenger;
    messenger2 = secondMessenger;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkIn(long)
   */
  @Override
  public RequestResponse checkIn(long id) throws IOException, UnknownUserException {
    RequestResponse response1 = messenger1.checkIn(id);
    if (!response1.equals(RequestResponse.Ok)) {
      return response1;
    }
    RequestResponse response2 = messenger2.checkIn(id);
    return response2;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOut(long)
   */
  @Override
  public RequestResponse checkOut(long id) throws IOException, UnknownUserException {
    RequestResponse response1 = messenger1.checkOut(id);
    if (!response1.equals(RequestResponse.Ok)) {
      return response1;
    }
    RequestResponse response2 = messenger2.checkOut(id);
    return response2;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOutAll()
   */
  @Override
  public RequestResponse checkOutAll() throws IOException {
    RequestResponse response1 = messenger1.checkOutAll();
    if (!response1.equals(RequestResponse.Ok)) {
      return response1;
    }
    RequestResponse response2 = messenger2.checkOutAll();
    return response2;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#toggleCheckInStatus(long)
   */
  @Override
  public Status toggleCheckInStatus(long id) throws IOException, UnknownUserException {
    Status status1 = messenger1.toggleCheckInStatus(id);
    Status status2 = messenger2.toggleCheckInStatus(id);
    if (status1.compareTo(status2) != 0) {
      throw new IllegalStateException("Status from messenger 1 (" + status1
          + ") does not match status from messenger 2 (" + status2 + ")");
    }
    return status1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getCheckInStatus(long)
   */
  @Override
  public Status getCheckInStatus(long id) throws IOException, UnknownUserException {
    Status status1 = messenger1.getCheckInStatus(id);
    Status status2 = messenger2.getCheckInStatus(id);
    if (status1.compareTo(status2) != 0) {
      throw new IllegalStateException("Status from messenger 1 (" + status1
          + ") does not match status from messenger 2 (" + status2 + ")");
    }
    return status1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#findPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person findPerson(String firstName, String lastName) {
    Person person1 = messenger1.findPerson(firstName, lastName);
    Person person2 = messenger2.findPerson(firstName, lastName);
    if (!person1.equals(person2)) {
      throw new IllegalStateException("Person from messenger 1 (" + person1
          + ") does not match person from messenger 2 (" + person2 + ")");
    }
    return person1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person addPerson(String firstName, String lastName) {
    Person person1 = messenger1.addPerson(firstName, lastName);
    Person person2 = messenger2.addPerson(firstName, lastName);
    if (!person1.equals(person2)) {
      throw new IllegalStateException("Person from messenger 1 (" + person1
          + ") does not match person from messenger 2 (" + person2 + ")");
    }
    return person1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) {
    messenger1.setActivity(activity);
    messenger2.setActivity(activity);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getActivity()
   */
  @Override
  public CheckInActivity getActivity() {
    CheckInActivity activity1 = messenger1.getActivity();
    CheckInActivity activity2 = messenger2.getActivity();
    if (!activity1.equals(activity2)) {
      throw new IllegalStateException("Activity from messenger 1 (" + activity1
          + ") does not match activity from messenger 2 (" + activity2 + ")");
    }
    return activity1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    messenger1.addPropertyChangeListener(listener);
    messenger2.addPropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    messenger1.removePropertyChangeListener(listener);
    messenger2.removePropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    messenger1.addPropertyChangeListener(propertyName, listener);
    messenger2.addPropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    messenger1.removePropertyChangeListener(propertyName, listener);
    messenger2.removePropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#lastCheckInEventToString()
   */
  @Override
  public String lastCheckInEventToString() {
    return messenger1.lastCheckInEventToString() + messenger2.lastCheckInEventToString();
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEventsSorted()
   */
  @Override
  public List<PersonCheckInEvent> getLastCheckInEventsSorted() {
    List<PersonCheckInEvent> list1 = messenger1.getLastCheckInEventsSorted();
    List<PersonCheckInEvent> list2 = messenger2.getLastCheckInEventsSorted();
    if (list1.size() != list2.size()) {
      throw new IllegalStateException("List size from messenger 1 (" + list1.size()
          + ") does not match list size from messenger 2 (" + list2.size() + ")");
    }
    for (int i = 0; i < list1.size(); i++) {
      // TODO not sure the equals test will be sufficient to detect equivalent objects
      PersonCheckInEvent ev1 = list1.get(i);
      PersonCheckInEvent ev2 = list2.get(i);
      if (!ev1.equals(ev2)) {
        throw new IllegalStateException("Event from messenger 1 (" + ev1
        + ") does not match event from messenger 2 (" + ev2 + ")");
      }
    }
    return list1;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEvent(long)
   */
  @Override
  public CheckInEvent getLastCheckInEvent(long id)
      throws IOException, UnknownUserException {
    CheckInEvent event1 = messenger1.getLastCheckInEvent(id);
    CheckInEvent event2 = messenger2.getLastCheckInEvent(id);
    if (!event1.equals(event2)) {
      throw new IllegalStateException("Event from messenger 1 (" + event1
      + ") does not match event from messenger 2 (" + event2 + ")");
    }
    return event1;
  }

  private static void logResponse(RequestResponse response, String prefix) {
    switch (response) {
      case Ok:
        LOG.info(prefix + ": received ok");
        break;
      case UnknownId:
        LOG.info(prefix + ": received Unknown ID; exiting...");
        System.exit(1);
        break;
      case FailedRequest:
      default:
        LOG.info(prefix + ": failed request; aborting...");
        System.exit(2);
    }
  }

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException, UnknownUserException {
    IMessenger m1 = new HttpMessenger("http://localhost:8080/attendance/request");
    IMessenger m2 = new LocalMessenger("/tmp/check-in-server.db");
    IMessenger m = new CompositeMessenger(m1, m2);
    try {
      RequestResponse response = m.checkIn(1);
      logResponse(response, "checkin");
      response = m.checkOut(1);
      logResponse(response, "checkout");
      response = m.checkIn(-1);
      logResponse(response, "checkout");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
