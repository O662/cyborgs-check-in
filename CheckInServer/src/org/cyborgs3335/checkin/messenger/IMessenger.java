package org.cyborgs3335.checkin.messenger;

import java.io.IOException;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;

public interface IMessenger {

  public enum Action { CheckIn, CheckOut }

  public enum RequestResponse { Ok, UnknownId, FailedRequest }

  public RequestResponse checkIn(long id) throws IOException, UnknownUserException;

  public RequestResponse checkOut(long id) throws IOException, UnknownUserException;

  public CheckInEvent.Status toggleCheckInStatus(long id) throws IOException, UnknownUserException;

  public CheckInEvent.Status getCheckInStatus(long id) throws IOException, UnknownUserException;

  public void setActivity(CheckInActivity activity);

  public CheckInActivity getActivity();
}