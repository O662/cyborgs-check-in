package org.cyborgs3335.checkin.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInServer;

@Deprecated
public class CheckInTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private int rowCount;
  private int colCount;
  private long[] ids;
  private String[] firstNames;
  private String[] lastNames;
  private CheckInEvent[] lastEvents;
  private long[] timeStamps;
  private DateFormat dateFormat;

  public CheckInTableModel() {
    String buffer = "";
    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    CheckInServer server = CheckInServer.getInstance();
    CheckInActivity activity = server.getActivity();
    if (activity != null) {
      buffer += activity.printToString(dateFormat) + "\n";
    }
    rowCount = server.getIdSet().size();
    colCount = 5;
    ids = new long[rowCount];
    firstNames = new String[rowCount];
    lastNames = new String[rowCount];
    lastEvents = new CheckInEvent[rowCount];
    timeStamps = new long[rowCount];
    int irow = 0;
    for (Long id : server.getIdSet()) {
      AttendanceRecord record = server.getAttendanceRecord(id);
      ids[irow] = id;
      firstNames[irow] = record.getPerson().getFirstName();
      lastNames[irow] = record.getPerson().getLastName();
      ArrayList<CheckInEvent> list = record.getEventList();
      CheckInEvent event = list.get(list.size()-1);
      lastEvents[irow] = event;
      timeStamps[irow] = event.getTimeStamp();
      buffer += "id " + id + " name " + record.getPerson() + " check-in "
          + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())) + "\n";
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
