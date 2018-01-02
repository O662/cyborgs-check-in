package org.cyborgs3335.checkin.messenger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
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
      Person personAdd = m.findPerson(firstName, lastName);
      assertEquals(firstName, personAdd.getFirstName());
      assertEquals(lastName, personAdd.getLastName());
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

}
