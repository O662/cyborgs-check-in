package org.cyborgs3335.checkin;

import org.cyborgs3335.checkin.messenger.IMessenger;

public interface IDatabaseOperations {

  /**
   * Get the messenger for communicating with the database.
   * @return database messenger
   */
  public IMessenger getMessenger();

  /**
   * Load database from current default path location.
   */
  public void loadDatabase();

  /**
   * Save database to current path;
   */
  public void saveDatabase();

  /**
   * Save database to current path;
   * @param path path to output CSV file
   */
  public void saveDatabaseCsv(String path);

  /**
   * Save hours-by-day information from database to current path;
   * @param path path to output CSV file
   */
  public void saveDatabaseHoursByDayCsv(String path);

  /**
   * Save database to specified path.
   * @param path path to output JSON file
   */
  public void saveDatabaseJson(String path);

  /**
   * Print a message to the database logfile.
   * @param message message to print
   */
  public void logDatabase(String message);
}
