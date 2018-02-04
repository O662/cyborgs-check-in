package org.cyborgs3335.checkin.server.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class JsonOutput {

  private final JsonWriter writer;
  private final CheckInActivity activity;
  private final Map<Long, AttendanceRecord> map;
  private final Gson gson;

  public JsonOutput(JsonWriter writer, CheckInActivity activity, Map<Long, AttendanceRecord> map) {
    this.writer = writer;
    this.activity = activity;
    this.map = map;
    gson = new GsonBuilder().setPrettyPrinting().create();
  }

  public void toJson() throws IOException {
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("checkInActivity");
    gson.toJson(activity, CheckInActivity.class, writer);
    writer.name("map").beginArray();
    synchronized (map) {
      for (long key : map.keySet()) {
        writeMapEntry(key, map.get(key));
      }
    }
    writer.endArray();
    writer.endObject();
  }

  private void writeMapEntry(long key, AttendanceRecord record) throws IOException {
    writer.beginObject();
    writer.name("id").value(key);
    writer.name("attendanceRecord");
    writeAttendanceRecord(record);
    writer.endObject();
  }

  private void writeAttendanceRecord(AttendanceRecord record) throws IOException {
    writer.beginObject();
    //writer.name("person").jsonValue(gson.toJson(record.getPerson()));
    writer.name("person");
    gson.toJson(record.getPerson(), Person.class, writer);
    writer.name("eventList").beginArray();
    for (CheckInEvent event: record.getEventList()) {
      writer.beginObject();
      writer.name("timeStamp").value(event.getTimeStamp());
      writer.name("status");
      gson.toJson(event.getStatus(), CheckInEvent.Status.class, writer);
      writer.name("checkInActivity");
      gson.toJson(activity, CheckInActivity.class, writer);
      //writeCheckInActivity(activity);
      writer.endObject();
    }
    writer.endArray();
    writer.endObject();
  }

  @SuppressWarnings("unused")
  private void writeCheckInActivity(CheckInActivity activity) throws IOException {
    writer.beginObject();
    writer.name("name").value(activity.getName());
    writer.name("startTime").value(activity.getStartTime());
    writer.name("endTime").value(activity.getEndTime());
    writer.endObject();
  }

  public static void dumpAttendanceRecordsToJson(String path, CheckInActivity activity, Map<Long, AttendanceRecord> map) throws IOException {
    FileWriter fw = new FileWriter(path);
    JsonWriter writer = new JsonWriter(fw);
    new JsonOutput(writer, activity, map).toJson();
    writer.close();
    fw.close();
  }

}
