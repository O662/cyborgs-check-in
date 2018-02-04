package org.cyborgs3335.checkin.server.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.Person;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class JsonInput {

  private final JsonReader reader;
  private CheckInActivity activity = null;
  private Map<Long, AttendanceRecord> map = null;
  private final Gson gson;

  public JsonInput(JsonReader reader) {
    this.reader = reader;
    gson = new GsonBuilder().setPrettyPrinting().create();
  }

  private void junk() {
//    Reads a JSON (RFC 7159) encoded value as a stream of tokens. This stream includes both literal values (strings, numbers, booleans, and nulls) as well as the begin and end delimiters of objects and arrays. The tokens are traversed in depth-first order, the same order that they appear in the JSON document. Within JSON objects, name/value pairs are represented by a single token.
//    Parsing JSON
//    To create a recursive descent parser for your own JSON streams, first create an entry point method that creates a JsonReader.
//    Next, create handler methods for each structure in your JSON text. You'll need a method for each object type and for each array type.
//
//    Within array handling methods, first call beginArray to consume the array's opening bracket. Then create a while loop that accumulates values, terminating when hasNext is false. Finally, read the array's closing bracket by calling endArray.
//    Within object handling methods, first call beginObject to consume the object's opening brace. Then create a while loop that assigns values to local variables based on their name. This loop should terminate when hasNext is false. Finally, read the object's closing brace by calling endObject.
//    When a nested object or array is encountered, delegate to the corresponding handler method.
//
//    When an unknown name is encountered, strict parsers should fail with an exception. Lenient parsers should call skipValue() to recursively skip the value's nested tokens, which may otherwise conflict.
//
//    If a value may be null, you should first check using peek(). Null literals can be consumed using either nextNull() or skipValue().
//
//    Example
//    Suppose we'd like to parse a stream of messages such as the following:
//     [
//       {
//         "id": 912345678901,
//         "text": "How do I read a JSON stream in Java?",
//         "geo": null,
//         "user": {
//           "name": "json_newb",
//           "followers_count": 41
//          }
//       },
//       {
//         "id": 912345678902,
//         "text": "@json_newb just use JsonReader!",
//         "geo": [50.454722, -104.606667],
//         "user": {
//           "name": "jesse",
//           "followers_count": 2
//         }
//       }
//     ]}
//    This code implements the parser for the above structure:
//       public List<Message> readJsonStream(InputStream in) throws IOException {
//         JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
//         try {
//           return readMessagesArray(reader);
//         } finally {
//           reader.close();
//         }
//       }
//
//       public List readMessagesArray(JsonReader reader) throws IOException {
//         List messages = new ArrayList();
//
//         reader.beginArray();
//         while (reader.hasNext()) {
//           messages.add(readMessage(reader));
//         }
//         reader.endArray();
//         return messages;
//       }
//
//       public Message readMessage(JsonReader reader) throws IOException {
//         long id = -1;
//         String text = null;
//         User user = null;
//         List geo = null;
//
//         reader.beginObject();
//         while (reader.hasNext()) {
//           String name = reader.nextName();
//           if (name.equals("id")) {
//             id = reader.nextLong();
//           } else if (name.equals("text")) {
//             text = reader.nextString();
//           } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
//             geo = readDoublesArray(reader);
//           } else if (name.equals("user")) {
//             user = readUser(reader);
//           } else {
//             reader.skipValue();
//           }
//         }
//         reader.endObject();
//         return new Message(id, text, user, geo);
//       }
//
//       public List readDoublesArray(JsonReader reader) throws IOException {
//         List doubles = new ArrayList();
//
//         reader.beginArray();
//         while (reader.hasNext()) {
//           doubles.add(reader.nextDouble());
//         }
//         reader.endArray();
//         return doubles;
//       }
//
//       public User readUser(JsonReader reader) throws IOException {
//         String username = null;
//         int followersCount = -1;
//
//         reader.beginObject();
//         while (reader.hasNext()) {
//           String name = reader.nextName();
//           if (name.equals("name")) {
//             username = reader.nextString();
//           } else if (name.equals("followers_count")) {
//             followersCount = reader.nextInt();
//           } else {
//             reader.skipValue();
//           }
//         }
//         reader.endObject();
//         return new User(username, followersCount);
//       }}
//    Number Handling
//    This reader permits numeric values to be read as strings and string values to be read as numbers. For example, both elements of the JSON array [1, "1"] may be read using either nextInt or nextString. This behavior is intended to prevent lossy numeric conversions: double is JavaScript's only numeric type and very large values like 9007199254740993 cannot be represented exactly on that platform. To minimize precision loss, extremely large values should be written and read as strings in JSON.
//    Non-Execute Prefix
//    Web servers that serve private data using JSON may be vulnerable to Cross-site request forgery attacks. In such an attack, a malicious site gains access to a private JSON file by executing it with an HTML <script> tag.
//    Prefixing JSON files with ")]}'\n" makes them non-executable by <script> tags, disarming the attack. Since the prefix is malformed JSON, strict parsing fails when it is encountered. This class permits the non-execute prefix when lenient parsing is enabled.
//
//    Each JsonReader may be used to read a single JSON stream. Instances of this class are not thread safe.
//
//    Since:
//    1.6
//    Author:
//    Jesse Wilson
  }

  public CheckInActivity getCheckInActivity() {
    return activity;
  }

  public Map<Long, AttendanceRecord> getMap() {
    return map;
  }

  public void fromJson() throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("checkInActivity")) {
        activity = readCheckInActivity();
      } else if (name.equals("map")) {
        map = readMap();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    reader.endObject();
  }

  public CheckInActivity readCheckInActivity() throws IOException {
    CheckInActivity checkInActivity = null;
    String activityName = null;
    long timeStart = Long.MIN_VALUE;
    long timeEnd = Long.MIN_VALUE;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("name")) {
        activityName = reader.nextString();
      } else if (name.equals("timeStart")) {
        timeStart = reader.nextLong();
      } else if (name.equals("timeEnd")) {
        timeEnd = reader.nextLong();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    reader.endObject();
    if (activityName == null) {
      throw new IOException("Missing value for CheckInActivity name!");
    }
    if (timeStart == Long.MIN_VALUE) {
      throw new IOException("Missing value for CheckInActivity startTime!");
    }
    if (timeEnd == Long.MIN_VALUE) {
      throw new IOException("Missing value for CheckInActivity endTime!");
    }
    checkInActivity = new CheckInActivity(activityName, timeStart, timeEnd);
    return checkInActivity;
  }

  public Map<Long, AttendanceRecord> readMap() throws IOException {
    Map<Long, AttendanceRecord> m = Collections.synchronizedMap(new HashMap<Long, AttendanceRecord>());
    reader.beginArray();
    while (reader.hasNext()) {
      readMapEntry(m);
    }
    reader.endArray();
    return m;
  }

  private void readMapEntry(Map<Long, AttendanceRecord> m) throws IOException {
    long id = Long.MIN_VALUE;
    AttendanceRecord record = null;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("id")) {
        id = reader.nextLong();
      } else if (name.equals("attendanceRecord")) {
        record = readAttendanceRecord();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    reader.endObject();
    if (id != Long.MIN_VALUE && record != null) {
      m.put(id, record);
    } else {
      throw new IOException("Invalid map entry!");
    }
  }

  private AttendanceRecord readAttendanceRecord() throws IOException {
    AttendanceRecord record = null;
    Person person = null;
    ArrayList<CheckInEvent> eventList = null;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("person")) {
        person = readPerson();
      } else if (name.equals("eventList")) {
        eventList = readEventList();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    reader.endObject();
    if (person == null) {
      throw new IOException("Missing person for AttendanceRecord!");
    } else if (eventList == null || eventList.isEmpty()) {
      throw new IOException("Missing eventList for AttendanceRecord!");
    }
    record = new AttendanceRecord(person, eventList);
    return record;
  }

  private Person readPerson() throws IOException {
    Person person = null;
    long id = Long.MIN_VALUE;
    String firstName = null;
    String middleName = null;
    String lastName = null;
    String nickName = null;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("id")) {
        id = reader.nextLong();
      } else if (name.equals("firstName")) {
        firstName = reader.nextString();
      } else if (name.equals("middleName")) {
        middleName = reader.nextString();
      } else if (name.equals("lastName")) {
        lastName = reader.nextString();
      } else if (name.equals("nickName")) {
        nickName = reader.nextString();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    if (id == Long.MIN_VALUE) {
      throw new IOException("Missing id for Person!");
    } else if (firstName == null) {
      throw new IOException("Missing firstName for Person!");
    } else if (middleName == null) {
      throw new IOException("Missing middleName for Person!");
    } else if (lastName == null) {
      throw new IOException("Missing lastName for Person!");
    } else if (nickName == null) {
      throw new IOException("Missing nickName for Person!");
    }
    reader.endObject();
    person = new Person(id, firstName, middleName, lastName, nickName);
    return person;
  }

  private ArrayList<CheckInEvent> readEventList() throws IOException {
    ArrayList<CheckInEvent> eventList = new ArrayList<CheckInEvent>();
    reader.beginArray();
    while (reader.hasNext()) {
      eventList.add(readCheckInEvent());
    }
    reader.endArray();
    return eventList;
  }

  private CheckInEvent readCheckInEvent() throws IOException {
    CheckInEvent event = null;
    long timeStamp = Long.MIN_VALUE;
    CheckInEvent.Status status = null;
    CheckInActivity checkInActivity = null;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("timeStamp")) {
        timeStamp = reader.nextLong();
      } else if (name.equals("status")) {
        status = CheckInEvent.Status.valueOf(reader.nextString());
      } else if (name.equals("checkInActivity")) {
        checkInActivity = readCheckInActivity();
      } else {
        throw new IOException("Unknown JSON name: " + name);
      }
    }
    reader.endObject();
    if (timeStamp == Long.MIN_VALUE) {
      throw new IOException("Missing timeStamp for CheckInEvent!");
    } else if (status == null) {
      throw new IOException("Missing status for CheckInEvent!");
    } else if (checkInActivity == null) {
      throw new IOException("Missing checkInActivity for CheckInEvent!");
    }
    // Some early events had no activity, and were set to null
    if (checkInActivity.equals(CheckInEvent.DEFAULT_ACTIVITY)) {
      checkInActivity = null;
    }
    event = new CheckInEvent(checkInActivity, status, timeStamp);
    return event;
  }

  public static JsonInput loadAttendanceRecordsFromJson(String path) throws IOException {
    FileReader fileReader = new FileReader(path);
    JsonReader reader = new JsonReader(fileReader);
    JsonInput jsonInput = new JsonInput(reader);
    try {
      jsonInput.fromJson();
      return jsonInput;
    } finally {
      reader.close();
      fileReader.close();
    }
  }

}
