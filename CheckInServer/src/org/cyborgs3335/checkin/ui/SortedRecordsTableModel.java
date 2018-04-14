package org.cyborgs3335.checkin.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.messenger.IMessenger;

public class SortedRecordsTableModel extends AbstractTableModel {

  private static final Logger LOG = Logger.getLogger(SortedRecordsTableModel.class.getName());

  private static final long serialVersionUID = 1L;

  private final int rowCount;
  private final int colCount;
  private final int baseColCount;
  private final DateFormat colHeaderDateFormat;
  private AttendanceData attendanceData;

  //TODO keep 1st 4 columns fixed when scrolling?
  //TODO highlight cells with hours > 12?

  public SortedRecordsTableModel(IMessenger messenger) {
    colHeaderDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    try {
      attendanceData = getAttendanceRecordsHoursPerDay(messenger);
    } catch (IOException e) {
      attendanceData = new AttendanceData(0, 0, 0);
      LOG.warning("Received IOException when fetching check-in events: " + e.getMessage());
      e.printStackTrace();
    } catch (UnknownUserException e) {
      attendanceData = new AttendanceData(0, 0, 0);
      LOG.warning("Received IOException when fetching check-in events: " + e.getMessage());
      e.printStackTrace();
    }
    rowCount = attendanceData.nrecords;
    baseColCount = 4;
    colCount = attendanceData.ndays + baseColCount;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "ID";
      case 1:
        return "First Name";
      case 2:
        return "Last Name";
      case 3:
        return "Total";
    }
    return colHeaderDateFormat.format(new Date(attendanceData.timeStampMin + (column - baseColCount) * getDayMillis()));
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Number.class;
      case 1:
        return String.class;
      case 2:
        return String.class;
      case 3:
        return Number.class;
    }
    return Number.class;
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public int getColumnCount() {
    return colCount;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return attendanceData.person[rowIndex].getId();
      case 1:
        return attendanceData.person[rowIndex].getFirstName();
      case 2:
        return attendanceData.person[rowIndex].getLastName();
      case 3:
        return String.format("%.2f",attendanceData.totalHours[rowIndex]);
    }
    if (columnIndex - baseColCount >= attendanceData.ndays) {
      return null;
    }
    float val = attendanceData.dayHours[rowIndex][columnIndex - baseColCount];
    if (val == 0) {
      return "";
    }
    return String.format("%.2f", val);
  }

  private AttendanceData getAttendanceRecordsHoursPerDay(IMessenger messenger) throws IOException, UnknownUserException {
    AttendanceData attendanceData = null;
    ArrayList<AttendanceRecord> recordList = getSortedAttendanceRecords(messenger);

    // Find min, max timestamp
    long timeStampMin = Long.MAX_VALUE;
    long timeStampMax = Long.MIN_VALUE;
    for (AttendanceRecord record : recordList) {
      if (!record.areEventsConsistent()) {
        LOG.info("Found inconsistent attendance record for " + record.getPerson());
      }
      ArrayList<CheckInEvent> list = record.getEventList();
      for (CheckInEvent event : list) {
        if (event.getTimeStamp() > 0) {
          timeStampMin = Math.min(timeStampMin, event.getTimeStamp());
        }
        if (event.getTimeStamp() < Long.MAX_VALUE) {
          timeStampMax = Math.max(timeStampMax, event.getTimeStamp());
        }
      }
    }
    int ndays = getNumberOfDays(timeStampMin, timeStampMax);
    attendanceData = new AttendanceData(timeStampMin, ndays, recordList.size());
    int i = 0;
    for (AttendanceRecord record : recordList) {
      if (!record.areEventsConsistent()) {
        LOG.info("Found inconsistent attendance record for " + record.getPerson());
      }
      attendanceData.person[i] = record.getPerson();
      attendanceData.dayHours[i] = record.getHoursByDay(timeStampMin, ndays);
      attendanceData.totalHours[i] = record.computeTotalAttendanceTime() / (1000.0 * 60.0 * 60.0);
      i++;
    }
    return attendanceData;
  }

  /**
   * Return number of days for the defined time span
   * @param timeStampMin minimum time stamp, in milliseconds
   * @param timeStampMax maximum time stamp, in milliseconds
   * @return number of days for the defined time span
   */
  private int getNumberOfDays(long timeStampMin, long timeStampMax) {
    return 1 + (int) (0.5 + (double) (timeStampMax - timeStampMin) / (double) getDayMillis());
  }

  private long getDayMillis() {
    return 1000L * 60L * 60L * 24L;
  }

  public ArrayList<AttendanceRecord> getSortedAttendanceRecords(IMessenger messenger) throws IOException, UnknownUserException {
    ArrayList<AttendanceRecord> recordList;
    List<PersonCheckInEvent> lastList = messenger.getLastCheckInEventsSorted();
    int rowCount = lastList.size();
    recordList = new ArrayList<AttendanceRecord>(rowCount);
    for (PersonCheckInEvent event : lastList) {
      Long id = event.getPerson().getId();
      AttendanceRecord record = messenger.getAttendanceRecord(id);
      if (id != record.getPerson().getId()) {
        LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
            + " for attendance record from person " + record.getPerson());
      }
      recordList.add(record);
    }
    Collections.sort(recordList, new Comparator<AttendanceRecord>() {

      @Override
      public int compare(AttendanceRecord o1, AttendanceRecord o2) {
        String o1Name = o1.getPerson().getLastName() + " " + o1.getPerson().getFirstName();
        String o2Name = o2.getPerson().getLastName() + " " + o2.getPerson().getFirstName();
        return o1Name.compareToIgnoreCase(o2Name);
      }
    });

    return recordList;
  }

  public class AttendanceData {
    final long timeStampMin;
    final int ndays;
    final int nrecords;
    final Person[] person;
    final float[][] dayHours;
    final double[] totalHours;

    public AttendanceData(long timeStampMin, int ndays, int nrecords) {
      this.timeStampMin = timeStampMin;
      this.ndays = ndays;
      this.nrecords = nrecords;
      person = new Person[nrecords];
      dayHours = new float[nrecords][];
      totalHours = new double[nrecords];
    }
  }
}
