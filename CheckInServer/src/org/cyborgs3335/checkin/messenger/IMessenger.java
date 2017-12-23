package org.cyborgs3335.checkin.messenger;

import java.io.IOException;

public interface IMessenger {

  public enum Action { CheckIn, CheckOut }

  public enum RequestResponse { Ok, UnknownId, FailedRequest }

  public RequestResponse checkIn(long id) throws IOException;

  public RequestResponse checkOut(long id) throws IOException;

}