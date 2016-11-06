package org.cyborgs3335.checkin;

public class CheckInClient {

  private final CheckInServer server;

  public CheckInClient() {
    this(CheckInServer.getInstance());
  }

  public CheckInClient(CheckInServer server) {
    this.server = server;
  }

  /**
   * Accept a check in or check out.
   * @param id id of user to check in or out
   * @return true if check in, false if check out
   * @throws UnknownUserException if the user is unknown to the server
   */
  public boolean accept(long id) throws UnknownUserException {
    return server.accept(id);
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
