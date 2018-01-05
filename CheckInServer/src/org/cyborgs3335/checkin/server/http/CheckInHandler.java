package org.cyborgs3335.checkin.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;
import org.cyborgs3335.checkin.messenger.IMessenger.Action;
import org.cyborgs3335.checkin.messenger.IMessenger.RequestResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class CheckInHandler extends AbstractHandler {
  private static final String jsonContentType = "application/json; charset=utf-8";

  private static final String htmlContentType = "text/html; charset=utf-8";

  private final CheckInDataStore dataStore;

  private final String greeting = "Attendance System";

  private final String body = null;

  public CheckInHandler(CheckInDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType(htmlContentType);
    response.setStatus(HttpServletResponse.SC_OK);

    switch (target) {
      case "/attendance/request":
        handleAttendanceRequest(target, request, response);
        break;
      case "/attendance/checkOutAll":
        handleCheckOutAll(target, request, response);
        break;
      case "/attendance/toggleCheckInStatus":
        handleToggleCheckInStatus(target, request, response);
        break;
      case "/attendance/getCheckInStatus":
        handleGetCheckInStatus(target, request, response);
        break;
      case "/attendance/getActivity":
        handleGetActivity(target, request, response);
        break;
      case "/attendance/setActivity":
        handleSetActivity(target, request, response);
        break;
      case "/attendance/addPerson":
        handleAddPerson(target, request, response);
        break;
      case "/attendance/findPerson":
        handleFindPerson(target, request, response);
        break;
      case "/attendance/lastCheckInEventToString":
        handleLastCheckInEventToString(target, request, response);
        break;
      case "/attendance/getLastCheckInEventsSorted":
        handleGetLastCheckInEventsSorted(target, request, response);
        break;
      case "/attendance/getLastCheckInEvent":
        handleGetLastCheckInEvent(target, request, response);
        break;
      case "/attendance/getAttendanceRecord":
        handleGetAttendanceRecord(target, request, response);
        break;
      case "/":
        handleRoot(target, response);
        break;
      default:
        handleDefault(target, response);
        break;
    }

    baseRequest.setHandled(true);
  }

  /**
   * Default response to a GET request.
   * @param target
   * @param response
   * @throws IOException 
   */
  private void handleDefault(String target, HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Unknown resource.</title></head>");
    out.println("<body>");
    out.println("<h1>" + greeting + "</h1>");
    if (body != null) {
      out.println(body);
    } else {
      out.println("<p>Unknown resource.</p>");
      out.println("<p>You accessed path: " + target + "</p>");
    }
    out.println("</body></html>");
  }

  /**
   * Response to a GET request for '/' (root).
   * @param target
   * @param response
   * @throws IOException 
   */
  private void handleRoot(String target, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>" + greeting + "</title></head>");
    out.println("<body>");
    out.println("<h1>" + greeting + "</h1>");
    if (body != null) {
      out.println(body);
    } else {
      out.println("<p>This is the root of the " + greeting + " handler</p>");
      out.println("<p>You accessed path: " + target + "</p>");
    }
    out.println("</body></html>");
  }

  /**
   * Process an attendance request.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleAttendanceRequest(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    long id = -1;
    Action action = null;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          id = reader.nextLong();
          break;
        case "action":
          try {
            action = gson.fromJson(reader.nextString(), Action.class);
          } catch (JsonSyntaxException e) {
            reader.close();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          }
          //action = Action.valueOf(reader.nextString());
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Complete action
    int status;
    RequestResponse requestResponse;
    try {
      boolean success = false;
      switch (action) {
        case CheckIn:
          success = dataStore.checkIn(id);
          break;
        case CheckOut:
          success = dataStore.checkOut(id);
          break;
      }
      if (success) {
        status = HttpServletResponse.SC_OK;
        requestResponse = RequestResponse.Ok;
      } else {
        status = 252;
        requestResponse = RequestResponse.FailedRequest;
      }
    } catch (UnknownUserException e) {
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    }

    // Set response
    response.setContentType(jsonContentType);
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    writer.name("action").value(gson.toJson(action));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Check out all persons.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleCheckOutAll(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    boolean value = false;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "checkOutAll":
          value = reader.nextBoolean();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    response.setContentType(jsonContentType);
    int status = HttpServletResponse.SC_OK;
    RequestResponse requestResponse = RequestResponse.Ok;
    response.setStatus(status);

    dataStore.checkOutAll();
    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Toggle the check-in status for a person.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleToggleCheckInStatus(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    long id = -1;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          id = reader.nextLong();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Complete action
    int status;
    RequestResponse requestResponse;
    Status checkInStatus = null;
    try {
      checkInStatus = dataStore.accept(id) ? Status.CheckedIn : Status.CheckedOut;
    } catch (UnknownUserException e) {
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    }
    status = HttpServletResponse.SC_OK;
    requestResponse = RequestResponse.Ok;

    // Set response
    response.setContentType(jsonContentType);
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    writer.name("status").value(gson.toJson(checkInStatus));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the check-in status for a person.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleGetCheckInStatus(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    long id = -1;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          id = reader.nextLong();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Complete action
    int status;
    RequestResponse requestResponse;
    AttendanceRecord record = dataStore.getAttendanceRecord(id);
    if (record == null) {
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    } else {
      status = HttpServletResponse.SC_OK;
      requestResponse = RequestResponse.Ok;
    }
    Status checkInStatus = record.getLastEvent().getStatus();

    // Set response
    response.setContentType(jsonContentType);
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    writer.name("status").value(gson.toJson(checkInStatus));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the attendance activity.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleGetActivity(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    boolean value = false;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "getActivity":
          value = reader.nextBoolean();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    response.setContentType(jsonContentType);
    int status = HttpServletResponse.SC_OK;
    RequestResponse requestResponse = RequestResponse.Ok;
    response.setStatus(status);

    CheckInActivity activity = dataStore.getActivity();
    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("activity").value(gson.toJson(activity));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Set the attendance activity.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleSetActivity(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    CheckInActivity activity = null;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "activity":
          try {
            activity = gson.fromJson(reader.nextString(), CheckInActivity.class);
          } catch (JsonSyntaxException e) {
            reader.close();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          }
          //action = Action.valueOf(reader.nextString());
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    dataStore.setActivity(activity);
    response.setContentType(jsonContentType);
    int status = HttpServletResponse.SC_OK;
    RequestResponse requestResponse = RequestResponse.Ok;
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("activity").value(gson.toJson(activity));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Find a person by name.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleAddPerson(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    String firstName = null;
    String lastName = null;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "firstName":
          firstName = reader.nextString();
          break;
        case "lastName":
          lastName = reader.nextString();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    Person person = dataStore.addUser(firstName, lastName);
    response.setContentType(jsonContentType);
    int status;
    RequestResponse requestResponse;
    if (person != null) {
      status = HttpServletResponse.SC_OK;
      requestResponse = RequestResponse.Ok;
    } else {
      status = 252;
      requestResponse = RequestResponse.FailedRequest;
    }
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("firstName").jsonValue(gson.toJson(firstName));
    writer.name("lastName").jsonValue(gson.toJson(lastName));
    writer.name("person").value(gson.toJson(person));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Find a person by name.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleFindPerson(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    String firstName = null;
    String lastName = null;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "firstName":
          firstName = reader.nextString();
          break;
        case "lastName":
          lastName = reader.nextString();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    Person person = dataStore.findPerson(firstName, lastName);
    response.setContentType(jsonContentType);
    int status;
    RequestResponse requestResponse;
    if (person != null) {
      // Success
      status = HttpServletResponse.SC_OK;
      requestResponse = RequestResponse.Ok;
    } else {
      // Failed due to unknown id
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    }
    // For known id, but invalid checkin/out action
    //status = 252;
    //requestResponse = RequestResponse.FailedRequest;
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("firstName").jsonValue(gson.toJson(firstName));
    writer.name("lastName").jsonValue(gson.toJson(lastName));
    writer.name("person").value(gson.toJson(person));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the last check-in events and return as a String.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleLastCheckInEventToString(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    boolean value = false;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "lastCheckInEventToString":
          value = reader.nextBoolean();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    response.setContentType(jsonContentType);
    int status = HttpServletResponse.SC_OK;
    RequestResponse requestResponse = RequestResponse.Ok;
    response.setStatus(status);

    String string = dataStore.printToString();
    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("lastCheckInEventToString").value(gson.toJson(string));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the last check-in events and return as a list.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleGetLastCheckInEventsSorted(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    boolean value = false;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "getLastCheckInEventsSorted":
          value = reader.nextBoolean();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Set response
    response.setContentType(jsonContentType);
    int status = HttpServletResponse.SC_OK;
    RequestResponse requestResponse = RequestResponse.Ok;
    response.setStatus(status);

    List<PersonCheckInEvent> list = dataStore.getLastCheckInEventsSorted();
    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("getLastCheckInEventsSorted").value(gson.toJson(list));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the last check-in event for an ID.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleGetLastCheckInEvent(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    long id = -1;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          id = reader.nextLong();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Complete action
    int status;
    RequestResponse requestResponse;
    CheckInEvent checkInEvent= null;
    AttendanceRecord record = dataStore.getAttendanceRecord(id);
    if (record == null) {
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    } else {
      status = HttpServletResponse.SC_OK;
      requestResponse = RequestResponse.Ok;
      checkInEvent = record.getLastEvent();
    }
    if (id != record.getPerson().getId()) {
      status = 252;
      requestResponse = RequestResponse.FailedRequest;
    }

    // Set response
    response.setContentType(jsonContentType);
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    writer.name("event").value(gson.toJson(checkInEvent));
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Get the attendance record for an ID.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleGetAttendanceRecord(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!checkJsonContentType(response, request.getContentType())) {
      return;
    }

    // Parse JSON content
    long id = -1;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(request.getReader());
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          id = reader.nextLong();
          break;
        default:
          reader.close();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
    }
    reader.endObject();
    reader.close();

    // Complete action
    int status;
    RequestResponse requestResponse;
    AttendanceRecord record = dataStore.getAttendanceRecord(id);
    if (record == null) {
      status = 251;
      requestResponse = RequestResponse.UnknownId;
    } else {
      status = HttpServletResponse.SC_OK;
      requestResponse = RequestResponse.Ok;
    }
    if (id != record.getPerson().getId()) {
      status = 252;
      requestResponse = RequestResponse.FailedRequest;
    }

    // Set response
    response.setContentType(jsonContentType);
    response.setStatus(status);

    PrintWriter out = response.getWriter();
    JsonWriter writer = new JsonWriter(out);
    gson = gsonBuilder.create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    if (record != null) {
      //writer.name("attendanceRecord").value(gson.toJson(record));
      writer.name("person").value(gson.toJson(record.getPerson()));
      writer.name("eventList").value(gson.toJson(record.getEventList()));
    }
    writer.name("result").value(gson.toJson(requestResponse));
    writer.endObject();
    writer.close();
  }

  /**
   * Send an echo of the request.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void sendRequestEcho(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(jsonContentType);
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();
    BufferedReader r = request.getReader();
    JsonWriter writer = new JsonWriter(out);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("contentType").value(gson.toJson(request.getContentType()));
    writer.name("contentLength").value(gson.toJson(request.getContentLengthLong()));
    String content = "";
    String s;
    while ((s=r.readLine()) != null) {
      content += s + "\n";
    }
    writer.name("content").jsonValue(content);
    writer.endObject();
    Enumeration<String> names = request.getParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String[] values = request.getParameterValues(name);
      writer.name(name).value(gson.toJson(values));
    }
    writer.close();
  }

  private boolean checkJsonContentType(HttpServletResponse response, String contentType) throws IOException {
    if (!contentType.equalsIgnoreCase(jsonContentType)) {
      // Did not receive JSON request; reply with error
      response.setContentType(htmlContentType);
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
      PrintWriter out = response.getWriter();
      out.println("<html><head><title>Invalid Content For Request.</title></head>");
      out.println("<body>");
      out.println("<h1>Invalid Content For Request</h1>");
      out.println("<p>Expected content type: " + jsonContentType + "</p>");
      out.println("<p>Received content type: " + contentType + "</p>");
      out.println("</body></html>");
      return false;
    }
    return true;
  }

}
