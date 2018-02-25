package org.cyborgs3335.checkin.server.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;

public interface ICheckInDataStore {

  String DB_ATTENDANCE_RECORDS = "attendance-records.db";
  String JSON_ATTENDANCE_RECORDS = "attendance-records.json";

  Person addUser(String firstName, String lastName) throws IOException;

  Person findPerson(String firstName, String lastName) throws IOException;

  /**
   * Query whether an id is known to the server.
   * @param id of user to query
   * @return true if the id exists
   * @throws IOException 
   */
  boolean containsId(long id) throws IOException;

  /**
   * Check in the specified id.
   * @param id id of user to check in
   * @return true if checked in, false otherwise
   * @throws UnknownUserException if the user is unknown to the server
   * @throws IOException 
   */
  boolean checkIn(long id) throws UnknownUserException, IOException;

  /**
   * Check out the specified id.
   * @param id id of user to check out
   * @return true if checked out, false otherwise
   * @throws UnknownUserException if the user is unknown to the server
   * @throws IOException 
   */
  boolean checkOut(long id) throws UnknownUserException, IOException;

  /**
   * Accept a check in or check out.
   * @param id id of user to check in or out
   * @return true if check in, false if check out
   * @throws UnknownUserException if the user is unknown to the server
   * @throws IOException 
   */
  boolean accept(long id) throws UnknownUserException, IOException;

  /**
   * Get the current activity.
   * @return current activity
   * @throws IOException 
   */
  CheckInActivity getActivity() throws IOException;

  /**
   * Set the current activity; e.g., name, start time, end time, ...
   * @param activity current activity
   * @throws IOException 
   */
  void setActivity(CheckInActivity activity) throws IOException;

  ArrayList<AttendanceRecord> getSortedAttendanceRecords() throws IOException;

  AttendanceRecord getAttendanceRecord(long id) throws IOException;

  void checkOutAll() throws IOException;

  /**
   * Print the last check-in event for each attendance record.
   * @throws IOException
   */
  String printToString() throws IOException;

  List<PersonCheckInEvent> getLastCheckInEventsSorted() throws IOException;

}