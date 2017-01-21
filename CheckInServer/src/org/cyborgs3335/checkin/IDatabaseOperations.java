package org.cyborgs3335.checkin;

public interface IDatabaseOperations {

  /**
   * Load database from current default path location.
   */
  public void loadDatabase();

  /**
   * Save database to current path;
   */
  public void saveDatabase();

  /**
   * Save database to newPath and update path to newPath.
   * @param newPath new directory location for saving database; becomes default location as well
   */
  public void saveDatabase(String newPath);

  /**
   * Save database to newPath and optionally update path to newPath.
   * @param newPath new directory location for saving database
   * @param updatePath if true, update default path location to newPath; if false, save database
   *                   to newPath and keep default path location as is (useful for a temporary
   *                   copy)
   */
  public void saveDatabase(String newPath, boolean updatePath);

  /**
   * Save database to current path;
   * @param path path to output CSV file
   */
  public void saveDatabaseCsv(String path);

  /**
   * Print a message to the database logfile.
   * @param message message to print
   */
  public void logDatabase(String message);
}
