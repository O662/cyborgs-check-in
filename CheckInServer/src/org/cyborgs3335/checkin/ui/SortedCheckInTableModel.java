package org.cyborgs3335.checkin.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInServer;

public class SortedCheckInTableModel extends AbstractTableModel {

  private static final Logger LOG = Logger.getLogger(SortedCheckInTableModel.class.getName());

  private static final long serialVersionUID = 1L;

  private int rowCount;
  private int colCount;
  private long[] ids;
  private String[] firstNames;
  private String[] lastNames;
  private CheckInEvent[] lastEvents;
  private long[] timeStamps;
  private DateFormat dateFormat;
  private ArrayList<AttendanceRecord> recordList;

  public SortedCheckInTableModel() {
    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    CheckInServer server = CheckInServer.getInstance();
    CheckInActivity activity = server.getActivity();
    if (activity != null) {
      LOG.info("creating table for activity " + activity.getName());
    } else {
      LOG.info("creating table, but no activity set!");
    }
    rowCount = server.getIdSet().size();
    colCount = 5;
    recordList = new ArrayList<AttendanceRecord>(rowCount);
    ids = new long[rowCount];
    firstNames = new String[rowCount];
    lastNames = new String[rowCount];
    lastEvents = new CheckInEvent[rowCount];
    timeStamps = new long[rowCount];
    for (Long id : server.getIdSet()) {
      AttendanceRecord record = server.getAttendanceRecord(id);
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
    int irow = 0;
    for (AttendanceRecord record : recordList) {
      ids[irow] = record.getPerson().getId();
      firstNames[irow] = record.getPerson().getFirstName();
      lastNames[irow] = record.getPerson().getLastName();
      ArrayList<CheckInEvent> list = record.getEventList();
      CheckInEvent event = list.get(list.size()-1);
      lastEvents[irow] = event;
      timeStamps[irow] = event.getTimeStamp();
      irow++;
    }
    //return buffer;
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
        return ids[rowIndex];
      case 1:
        return firstNames[rowIndex];
      case 2:
        return lastNames[rowIndex];
      case 3:
        return lastEvents[rowIndex].getStatus();
      case 4:
        return dateFormat.format(new Date(timeStamps[rowIndex]));
    }
    return null;
  }

}
