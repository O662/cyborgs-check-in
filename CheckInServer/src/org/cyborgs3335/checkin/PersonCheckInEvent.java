package org.cyborgs3335.checkin;

public class PersonCheckInEvent {

  private final Person person;

  private final CheckInEvent event;

  public PersonCheckInEvent(Person person, CheckInEvent event) {
    this.person = person;
    this.event = event;
  }

  /**
   * Get person for the event.
   * @return person for the event
   */
  public Person getPerson() {
    return person;
  }

  /**
   * Get check-in event.
   * @return check-in event
   */
  public CheckInEvent getCheckInEvent() {
    return event;
  }
}
