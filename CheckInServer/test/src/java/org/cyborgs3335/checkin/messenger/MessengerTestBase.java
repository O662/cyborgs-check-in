package org.cyborgs3335.checkin.messenger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;
import org.junit.Test;

/**
 * Classes extending this class should implement methods with the {@code @BeforeClass}
 * and {@code @AfterClass} junit annotations.
 *
 * @author brian
 *
 */
public abstract class MessengerTestBase {

  public abstract IMessenger getNewMessenger();

  @Test
  public final void testCheckInOutSuccess() {
    IMessenger m = getNewMessenger();
    try {
      Person person = m.addPerson("Jim", "Johnson");
      RequestResponse response = m.checkIn(person.getId());
      assertEquals(RequestResponse.Ok, response);
      response = m.checkOut(person.getId());
      assertEquals(RequestResponse.Ok, response);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testCheckInUnknownId() {
    IMessenger m = getNewMessenger();
    try {
      RequestResponse response = m.checkIn(-1);
      fail();
      //assertEquals(RequestResponse.UnknownId, response);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      // Expected response
      e.printStackTrace();
      //fail();
    }
  }

  @Test
  public final void testCheckOutAll() {
    IMessenger m = getNewMessenger();
    try {
      Person person1 = m.addPerson("Isaac", "Newton");
      Person person2 = m.addPerson("Albert", "Einstein");
      RequestResponse response = m.checkIn(person1.getId());
      assertEquals(RequestResponse.Ok, response);
      response = m.checkIn(person2.getId());
      assertEquals(RequestResponse.Ok, response);

      response = m.checkOutAll();
      assertEquals(RequestResponse.Ok, response);

      Status status = m.getCheckInStatus(person1.getId());
      assertEquals(Status.CheckedOut, status);
      status = m.getCheckInStatus(person2.getId());
      assertEquals(Status.CheckedOut, status);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testSetGetActivity() {
    IMessenger m = getNewMessenger();
    try {
      CheckInActivity activity = CheckInEvent.DEFAULT_ACTIVITY;
      m.setActivity(activity);
      CheckInActivity activityGet = m.getActivity();
      org.junit.Assert.assertEquals(activity, activityGet);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testAddPerson() {
    IMessenger m = getNewMessenger();
    try {
      String firstName = "John";
      String lastName = "Doe";
      Person personAdd = m.addPerson(firstName, lastName);
      assertEquals(firstName, personAdd.getFirstName());
      assertEquals(lastName, personAdd.getLastName());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testFindPerson() {
    IMessenger m = getNewMessenger();
    try {
      String firstName = "John1";
      String lastName = "Doe1";
      Person person = m.addPerson(firstName, lastName);
      assertEquals(firstName, person.getFirstName());
      assertEquals(lastName, person.getLastName());
      Person personAdd = m.findPerson(firstName, lastName);
      assertEquals(firstName, personAdd.getFirstName());
      assertEquals(lastName, personAdd.getLastName());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testGetCheckInStatus() {
    IMessenger m = getNewMessenger();
    try {
      Person person1 = m.addPerson("Enrico", "Fermi");
      RequestResponse response = m.checkIn(person1.getId());
      assertEquals(RequestResponse.Ok, response);
      Status status = m.getCheckInStatus(person1.getId());
      assertEquals(Status.CheckedIn, status);

      response = m.checkOut(person1.getId());
      assertEquals(RequestResponse.Ok, response);
      status = m.getCheckInStatus(person1.getId());
      assertEquals(Status.CheckedOut, status);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testToggleCheckInStatus() {
    IMessenger m = getNewMessenger();
    try {
      Person person1 = m.addPerson("Edward", "Teller");
      RequestResponse response = m.checkIn(person1.getId());
      assertEquals(RequestResponse.Ok, response);
      Status status = m.getCheckInStatus(person1.getId());
      assertEquals(Status.CheckedIn, status);

      status = m.toggleCheckInStatus(person1.getId());
      assertEquals(Status.CheckedOut, status);
      status = m.getCheckInStatus(person1.getId());
      assertEquals(Status.CheckedOut, status);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testLastCheckInEventToString() {
    IMessenger m = getNewMessenger();
    try {
      m.setActivity(CheckInEvent.DEFAULT_ACTIVITY);
      Person p = m.addPerson("Niels", "Bohr");
      RequestResponse response = m.checkIn(p.getId());
      assertEquals(RequestResponse.Ok, response);
      String string = m.lastCheckInEventToString();
      if (!string.startsWith("Activity DEFAULT Start Time 1969/12/31 18:00:00 -0600 End Time 292278994/08/17 01:12:55 -0600\nid ")) {
        System.out.println("lastCheckInEventToString: " + string);
        fail();
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testGetLastCheckInEventsSorted() {
    IMessenger m = getNewMessenger();
    try {
      m.setActivity(CheckInEvent.DEFAULT_ACTIVITY);
      Person p = m.addPerson("Werner", "Heisenberg");
      RequestResponse response = m.checkIn(p.getId());
      assertEquals(RequestResponse.Ok, response);
      response = m.checkOut(p.getId());
      assertEquals(RequestResponse.Ok, response);
      List<PersonCheckInEvent> list = m.getLastCheckInEventsSorted();
      for (PersonCheckInEvent pcie : list) {
        if (pcie.getPerson().equals(p)) {
          assertEquals(CheckInEvent.DEFAULT_ACTIVITY, pcie.getCheckInEvent().getActivity());
          assertEquals(Status.CheckedOut, pcie.getCheckInEvent().getStatus());          
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testGetLastCheckInEvent() {
    IMessenger m = getNewMessenger();
    //public CheckInEvent getLastCheckInEvent(long id) throws IOException, UnknownUserException;
    try {
      m.setActivity(CheckInEvent.DEFAULT_ACTIVITY);
      Person p = m.addPerson("Paul", "Dirac");
      RequestResponse response = m.checkIn(p.getId());
      assertEquals(RequestResponse.Ok, response);
      response = m.checkOut(p.getId());
      assertEquals(RequestResponse.Ok, response);
      CheckInEvent event = m.getLastCheckInEvent(p.getId());
      assertEquals(CheckInEvent.DEFAULT_ACTIVITY, event.getActivity());
      assertEquals(Status.CheckedOut, event.getStatus());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public final void testGetAttendanceRecord() {
    IMessenger m = getNewMessenger();
    try {
      m.setActivity(CheckInEvent.DEFAULT_ACTIVITY);
      Person p = m.addPerson("Marie", "Curie");
      RequestResponse response = m.checkIn(p.getId());
      assertEquals(RequestResponse.Ok, response);
      response = m.checkOut(p.getId());
      assertEquals(RequestResponse.Ok, response);
      AttendanceRecord record = m.getAttendanceRecord(p.getId());
      assertEquals(true, record.areEventsConsistent());
      assertEquals(p, record.getPerson());
      assertEquals(CheckInEvent.DEFAULT_ACTIVITY, record.getLastEvent().getActivity());
      assertEquals(Status.CheckedOut, record.getLastEvent().getStatus());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } catch (UnknownUserException e) {
      e.printStackTrace();
      fail();
    }
  }

}
