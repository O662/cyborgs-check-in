package org.cyborgs3335.checkin;

import java.io.Serializable;

public class Person implements Serializable {

  private static final long serialVersionUID = -8485645972688896641L;

  private final long id;
  private final String firstName;
  private final String middleName;
  private final String lastName;
  private final String nickName;

  public Person(long id, String firstName, String lastName) {
    this(id, firstName, "", lastName, firstName);
  }

  public Person(long id, String firstName, String middleName, String lastName, String nickName) {
    this.id = id;
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.nickName = nickName;
  }

  public long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getNickName() {
    return nickName;
  }

  @Override
  public String toString() {
    if (nickName != null && !nickName.isEmpty()) {
      return nickName + " " + lastName;
    }
    return firstName + " " + lastName;
  }
}
