package org.cyborgs3335.checkin;

import java.io.IOException;
import java.util.Scanner;

import org.cyborgs3335.checkin.messenger.IMessenger;
import org.cyborgs3335.checkin.server.local.LocalMessenger;

public class IdScanner {

  private final IMessenger client;
  private final Scanner scanner;

  public IdScanner(IMessenger client) {
    this.client = client;
    scanner = new Scanner(System.in);
  }

  public long readId() {
    return scanner.nextLong();
  }

  public CheckInEvent.Status sendId(long id) throws IOException, UnknownUserException {
    return client.toggleCheckInStatus(id);
  }

  public static void main(String[] args) throws IOException {
    String path = "/tmp/check-in-server2.dump";
    IMessenger messenger = new LocalMessenger(path);
    System.out.println(messenger.lastCheckInEventToString());

    IdScanner idScanner = new IdScanner(messenger);
    while (true) {
      System.out.println("Enter ID (-1 to quit, -2 to print): ");
      long id = idScanner.readId();
      if (id == -1) {
        System.out.println("Exiting...");
        break;
      } else if (id == -2) {
        System.out.println(messenger.lastCheckInEventToString());
        continue;
      }
      try {
        CheckInEvent.Status status = idScanner.sendId(id);
        switch (status) {
          case CheckedIn:
            System.out.println("Check in ID " + id);
            break;
          case CheckedOut:
            System.out.println("Check out ID " + id);
            break;
          default:
            System.out.println("Unknown status: " + status);
            break;
        }
      } catch (UnknownUserException e) {
        System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
      }
    }

    System.out.println(messenger.lastCheckInEventToString());
    messenger.close();
  }

}
