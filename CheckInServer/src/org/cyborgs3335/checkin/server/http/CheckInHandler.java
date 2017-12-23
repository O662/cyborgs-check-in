package org.cyborgs3335.checkin.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  private final String greeting;

  private final String body;

  public CheckInHandler() {
    this("Hello World");
  }

  public CheckInHandler(String greeting) {
    this(greeting, null);
  }

  public CheckInHandler(String greeting, String body) {
    this.greeting = greeting;
    this.body = body;
  }

  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType(htmlContentType);
    response.setStatus(HttpServletResponse.SC_OK);

    switch (target) {
      case "/attendance/request":
        handleAttendanceRequest(target, request, response);
        break;
      case "/findPerson":
        handleFindPerson(target, request, response);
        break;
      case "/time":
        handleTime(target, response);
        break;
      case "/b":
        handleB(target, response);
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
   * Process an attendance request.
   * @param target
   * @param request 
   * @param response
   * @throws IOException
   */
  private void handleAttendanceRequest(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String inContentType = request.getContentType();
    if (!inContentType.equalsIgnoreCase(jsonContentType)) {
      // Did not receive JSON request; reply with error
      response.setContentType(htmlContentType);
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
      PrintWriter out = response.getWriter();
      out.println("<html><head><title>Invalid Content For Request.</title></head>");
      out.println("<body>");
      out.println("<h1>Invalid Content For Request</h1>");
      out.println("<p>Expected content type: " + jsonContentType + "</p>");
      out.println("<p>Received content type: " + inContentType + "</p>");
      out.println("</body></html>");
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

    // Set response
    response.setContentType(jsonContentType);
    int status;
    RequestResponse requestResponse;
    if (id > 0) {
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
    writer.name("id").value(gson.toJson(id));
    writer.name("action").value(gson.toJson(action));
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
    response.setContentType(jsonContentType);
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();

    String firstName = null;
    String lastName = null;
    Enumeration<String> names = request.getParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String[] values = request.getParameterValues(name);
      switch (name) {
        case "firstName":
          if (values.length > 0) {
            firstName = values[0];
            for (int i = 1; i < values.length; i++) {
              firstName += " " + values[i];
            }
          }
          break;
        case "lastName":
          if (values.length > 0) {
            lastName = values[0];
            for (int i = 1; i < values.length; i++) {
              lastName += " " + values[i];
            }
          }
          break;
        default:
          response.setContentType(htmlContentType);
          response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
          out.println("<html><head><title>Find person.</title></head>");
          out.println("<body><p>Unknown parameter name: " + name + "</p></body></html>");
          return;
      }
    }

    //out.println("<html><head><title>Find person.</title></head>");
    //while (names.hasMoreElements()) {
    //  String name = names.nextElement();
    //  String[] values = request.getParameterValues(name);
    //  out.println("<p>" + name + " " + Arrays.toString(values) + "</p>");
    //}
    //out.println("<body><p>The person is " + firstName + " " + lastName + ".</p>");
    //out.println("<p>You accessed path: " + target + "</p>");
    //out.println("</body></html>");

    JsonWriter writer = new JsonWriter(out);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("firstName").value(gson.toJson(firstName));
    writer.name("lastName").value(gson.toJson(lastName));
    writer.endObject();
    writer.close();
  }

  /**
   * Request current time.
   * @param target
   * @param response
   * @throws IOException 
   */
  private void handleTime(String target, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Time request.</title></head>");
    out.println("<body><p>The current time is " + Calendar.getInstance().getTime() + ".</p>");
    out.println("<p>You accessed path: " + target + "</p>");
    out.println("</body></html>");
  }

  /**
   * Response to a GET request for 'b'.
   * @param target
   * @param response
   * @throws IOException 
   */
  private void handleB(String target, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Request for b.</title></head>");
    out.println("<body><p>This is a test for b.</p>");
    out.println("<p>You accessed path: " + target + "</p>");
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

}
