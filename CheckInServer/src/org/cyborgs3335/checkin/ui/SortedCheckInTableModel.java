package org.cyborgs3335.checkin.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

//import org.cyborgs3335.checkin.ActivityAttendanceRecord;
//import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.messenger.IMessenger;
//import org.cyborgs3335.checkin.server.local.CheckInServer;

public class SortedCheckInTableModel extends AbstractTableModel {

  private static final Logger LOG = Logger.getLogger(SortedCheckInTableModel.class.getName());

  private static final long serialVersionUID = 1L;

  private int rowCount;
  private int colCount;
//  private long[] ids;
//  private String[] firstNames;
//  private String[] lastNames;
//  private CheckInEvent[] lastEvents;
//  private long[] timeStamps;
  private DateFormat dateFormat;
//  private ArrayList<AttendanceRecord> recordList;
  private List<PersonCheckInEvent> recordListNew;

  public SortedCheckInTableModel(IMessenger messenger) {
    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
//    CheckInServer server = CheckInServer.getInstance();
    CheckInActivity activity;
    try {
      activity = messenger.getActivity();
    } catch (IOException e) {
      activity = null;
      e.printStackTrace();
    }
    if (activity != null) {
      LOG.info("creating table for activity " + activity.getName());
    } else {
      LOG.info("creating table, but no activity set!");
    }
    try {
      recordListNew = messenger.getLastCheckInEventsSorted();
    } catch (IOException e) {
      recordListNew = new ArrayList<PersonCheckInEvent>();
      LOG.warning("Received IOException when fetching check-in events: " + e.getMessage());
      e.printStackTrace();
    }
    rowCount = recordListNew.size();
//    rowCount = server.getIdSet().size();
    colCount = 6;
//    recordList = new ArrayList<AttendanceRecord>(rowCount);
//    ids = new long[rowCount];
//    firstNames = new String[rowCount];
//    lastNames = new String[rowCount];
//    lastEvents = new CheckInEvent[rowCount];
//    timeStamps = new long[rowCount];
//    for (Long id : server.getIdSet()) {
//      AttendanceRecord record = server.getAttendanceRecord(id);
//      if (id != record.getPerson().getId()) {
//        LOG.info("ID " + id + " does not match ID " + record.getPerson().getId()
//            + " for attendance record from person " + record.getPerson());
//      }
//      recordList.add(record);
//    }
//    Collections.sort(recordList, new Comparator<AttendanceRecord>() {
//
//      @Override
//      public int compare(AttendanceRecord o1, AttendanceRecord o2) {
//        String o1Name = o1.getPerson().getLastName() + " " + o1.getPerson().getFirstName();
//        String o2Name = o2.getPerson().getLastName() + " " + o2.getPerson().getFirstName();
//        return o1Name.compareToIgnoreCase(o2Name);
//      }
//    });
//    int irow = 0;
//    for (AttendanceRecord record : recordList) {
//      ids[irow] = record.getPerson().getId();
//      firstNames[irow] = record.getPerson().getFirstName();
//      lastNames[irow] = record.getPerson().getLastName();
//      ArrayList<CheckInEvent> list = record.getEventList();
//      CheckInEvent event = list.get(list.size()-1);
//      lastEvents[irow] = event;
//      timeStamps[irow] = event.getTimeStamp();
//      irow++;
//    }
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
      return "Activity";
    case 4:
      return "Status";
    case 5:
      return "Time";
    }
    return "" + (char) ('A' + column);
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
    //ArrayList<CheckInEvent> evlist = recordList.get(rowIndex).getEventList();
    switch (columnIndex) {
      case 0:
        //return ids[rowIndex];
        return recordListNew.get(rowIndex).getPerson().getId();
      case 1:
        //return firstNames[rowIndex];
        return recordListNew.get(rowIndex).getPerson().getFirstName();
      case 2:
        //return lastNames[rowIndex];
        return recordListNew.get(rowIndex).getPerson().getLastName();
      case 3:
        //CheckInActivity activity = evlist.get(evlist.size()-1).getActivity();
        CheckInActivity activity = recordListNew.get(rowIndex).getCheckInEvent().getActivity();
        if (activity != null) {
          return activity.toString();
        }
        //AttendanceRecord record = recordList.get(rowIndex);
        //if (record instanceof ActivityAttendanceRecord) {
        //  CheckInActivity activity = ((ActivityAttendanceRecord) record).getActivity();
        //  if (activity != null) {
        //    return activity.toString();
        //  }
        //}
        return CheckInEvent.DEFAULT_ACTIVITY.toString();
      case 4:
        //return lastEvents[rowIndex].getStatus();
        //ArrayList<CheckInEvent> list = recordList.get(rowIndex).getEventList();
        //return evlist.get(evlist.size()-1).getStatus();
        return recordListNew.get(rowIndex).getCheckInEvent().getStatus();
      case 5:
        //return dateFormat.format(new Date(timeStamps[rowIndex]));
        //ArrayList<CheckInEvent> listTS = recordList.get(rowIndex).getEventList();
        //return dateFormat.format(new Date(evlist.get(evlist.size()-1).getTimeStamp()));
        return dateFormat.format(new Date(recordListNew.get(rowIndex).getCheckInEvent().getTimeStamp()));
    }
    return null;
  }

}
