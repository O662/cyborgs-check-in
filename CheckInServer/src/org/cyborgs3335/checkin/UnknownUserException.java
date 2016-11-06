package org.cyborgs3335.checkin;

public class UnknownUserException extends Exception {

  private static final long serialVersionUID = -6933186652158251378L;

  public UnknownUserException() {
    super();
  }

  public UnknownUserException(String message) {
    super(message);
  }

  public UnknownUserException(Throwable cause) {
    super(cause);
  }

  public UnknownUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownUserException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
