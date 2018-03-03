package org.cyborgs3335.checkin.server.http;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.messenger.IMessenger;

public class TestExample {

  private static final Logger LOG = Logger.getLogger(TestExample.class.getName());

  private static final String SERVER_URL = "http://localhost:8080/attendance";

  public static void main(String[] args) {
    String serverUrl = SERVER_URL;
    if (args.length == 1) {
      serverUrl = args[0];
    } else if (args.length > 1) {
      LOG.warning("Usage: " + TestExample.class.getName() + " <server url>\n  e.g., "
          + TestExample.class.getName() + " http://localhost:8080/attendance");
    }
    LOG.info("Connecting to " + serverUrl);
    IMessenger m = new HttpMessenger(serverUrl);
    try {
      LOG.info("Get Activity: " + m.getActivity());
      List<PersonCheckInEvent> list = m.getLastCheckInEventsSorted();
      for (PersonCheckInEvent e : list) {
        //e.getCheckInEvent();
        LOG.info("Person: " + e.getPerson());
      }
      //String firstName = "John1";
      //String lastName = "Doe1";
      //Person personAdd = m.findPerson(firstName, lastName);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
