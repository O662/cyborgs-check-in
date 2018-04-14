package org.cyborgs3335.checkin.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JTable;

public class CsvOutput {

  private static final Logger LOG = Logger.getLogger(CsvOutput.class.getName());

  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  /**
   * Dump "database" hours-by-day information to filesystem.
   * @param path CSV file to save "database" to
   * @param table table containing data to export
   * @throws IOException on I/O error dumping "database" to filesystem
   */
  public static void exportTableToCsv(String path, JTable table) throws IOException {
    File csvFile = new File(path);
    if (!csvFile.isFile()) {
      throw new IOException("Path " + path + " must be a file!");
    }

    int ncol = table.getColumnCount();
    int nrow = table.getRowCount();
    if (ncol <= 0 || nrow <= 0) {
      throw new IOException("Table is empty, no data to write!");
    }
    LOG.info(getDateStamp() + " Exporting table with " + ncol + " columns and " + nrow + " rows to CSV file " + csvFile);

    // TODO run data through a CSV filter to escape any non-permitted characters
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
      // Write header
      String hdrBuf = table.getColumnName(0);
      for (int icol = 0; icol < ncol; icol++) {
        hdrBuf += "," + table.getColumnName(icol);
      }
      writer.write(hdrBuf + "\n");

      // Write rows
      for (int irow = 0; irow < nrow; irow++) {
        String rowBuf = table.getValueAt(irow, 0).toString();
        for (int icol = 0; icol < ncol; icol++) {
          rowBuf += "," + table.getValueAt(irow, icol);
        }
        writer.write(rowBuf + "\n");
      }
    }
    LOG.info(getDateStamp() + " Export to CSV file complete");
  }

  public static String getDateStamp() {
    return "[" + dateFormat.format(new Date()) + "]";
  }

  public static String floatArrayToString(float[] hoursByDay, String fmt) {
    String val = "";
    for (int i = 0; i < hoursByDay.length; i++) {
      val += "," + String.format(fmt, hoursByDay[i]);
    }
    return val;
  }

}
