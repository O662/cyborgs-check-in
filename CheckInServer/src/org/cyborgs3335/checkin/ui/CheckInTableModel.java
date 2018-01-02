package org.cyborgs3335.checkin.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.messenger.IMessenger;

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

  public CheckInTableModel(IMessenger messenger) throws IOException {
    String buffer = "";
    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    CheckInActivity activity = messenger.getActivity();
    if (activity != null) {
      buffer += activity.printToString(dateFormat) + "\n";
    }

    List<PersonCheckInEvent> recordList = messenger.getLastCheckInEventsSorted();
    rowCount = recordList.size();
    colCount = 5;
    ids = new long[rowCount];
    firstNames = new String[rowCount];
    lastNames = new String[rowCount];
    lastEvents = new CheckInEvent[rowCount];
    timeStamps = new long[rowCount];
    int irow = 0;
    for (PersonCheckInEvent event : recordList) {
      ids[irow] = event.getPerson().getId();
      firstNames[irow] = event.getPerson().getFirstName();
      lastNames[irow] = event.getPerson().getLastName();
      lastEvents[irow] = event.getCheckInEvent();
      timeStamps[irow] = event.getCheckInEvent().getTimeStamp();
      buffer += "id " + event.getPerson().getId() + " name " + event.getPerson() + " check-in "
          + event.getCheckInEvent().getStatus() + " " + dateFormat.format(new Date(event.getCheckInEvent().getTimeStamp())) + "\n";
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
