package org.cyborgs3335.checkin;

import java.io.IOException;
import java.util.Scanner;

import org.cyborgs3335.checkin.server.local.LocalMessenger;

public class IdScanner {

  private final CheckInClient client;
  private final Scanner scanner;

  public IdScanner(CheckInClient client) {
    this.client = client;
    scanner = new Scanner(System.in);
  }

  public long readId() {
    return scanner.nextLong();
  }

  public boolean sendId(long id) throws UnknownUserException {
    return client.accept(id);
  }

  public static void main(String[] args) throws IOException {
    String path = "/tmp/check-in-server2.dump";
    LocalMessenger messenger = new LocalMessenger(path);
    messenger.print();

    IdScanner idScanner = new IdScanner(new CheckInClient());
    while (true) {
      System.out.println("Enter ID (-1 to quit, -2 to print): ");
      long id = idScanner.readId();
      if (id == -1) {
        System.out.println("Exiting...");
        break;
      } else if (id == -2) {
        messenger.print();
        continue;
      }
      try {
        boolean checkIn = idScanner.sendId(id);
        if (checkIn) {
          System.out.println("Check in ID " + id);
        } else {
          System.out.println("Check out ID " + id);
        }
      } catch (UnknownUserException e) {
        System.out.println("Unknown user ID: " + id + "\nID will need to be added before check in is valid.");
      }
    }

    messenger.print();
    messenger.save();
  }

}
