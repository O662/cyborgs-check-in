package org.cyborgs3335.checkin.messenger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import org.cyborgs3335.checkin.AttendanceRecord;
import org.cyborgs3335.checkin.CheckInActivity;
import org.cyborgs3335.checkin.CheckInEvent;
import org.cyborgs3335.checkin.CheckInEvent.Status;
import org.cyborgs3335.checkin.Person;
import org.cyborgs3335.checkin.PersonCheckInEvent;
import org.cyborgs3335.checkin.UnknownUserException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpMessenger implements IMessenger {

  private static final Logger LOG = Logger.getLogger(HttpMessenger.class.getName());

  public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private final OkHttpClient client;

  private final String serverUrl;

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public HttpMessenger(String serverUrl) {
    client = new OkHttpClient();
    this.serverUrl = serverUrl;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkIn(long)
   */
  @Override
  public RequestResponse checkIn(long id) throws IOException, UnknownUserException {
    return sendRequest(id, Action.CheckIn);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOut(long)
   */
  @Override
  public RequestResponse checkOut(long id) throws IOException, UnknownUserException {
    return sendRequest(id, Action.CheckOut);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#checkOutAll()
   */
  @Override
  public RequestResponse checkOutAll() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#toggleCheckInStatus(long)
   */
  @Override
  public Status toggleCheckInStatus(long id) throws IOException, UnknownUserException {
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getCheckInStatus(long)
   */
  @Override
  public Status getCheckInStatus(long id) throws IOException, UnknownUserException {
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#findPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person findPerson(String firstName, String lastName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPerson(java.lang.String, java.lang.String)
   */
  @Override
  public Person addPerson(String firstName, String lastName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#setActivity(org.cyborgs3335.checkin.CheckInActivity)
   */
  @Override
  public void setActivity(CheckInActivity activity) {
    CheckInActivity oldActivity = getActivity();
    // TODO Auto-generated method stub
    // TODO set activity
    throw new UnsupportedOperationException("not implemented");
    //pcs.firePropertyChange(ACTIVITY_PROPERTY, oldActivity, activity);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getActivity()
   */
  @Override
  public CheckInActivity getActivity() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
    //return null;
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#lastCheckInEventToString()
   */
  @Override
  public String lastCheckInEventToString() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEventsSorted()
   */
  @Override
  public List<PersonCheckInEvent> getLastCheckInEventsSorted() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getLastCheckInEvent(long)
   */
  @Override
  public CheckInEvent getLastCheckInEvent(long id)
      throws IOException, UnknownUserException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#getAttendanceRecord(long)
   */
  @Override
  public AttendanceRecord getAttendanceRecord(long id)
      throws IOException, UnknownUserException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  /* (non-Javadoc)
   * @see org.cyborgs3335.checkin.messenger.IMessenger#close()
   */
  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("not implemented");
  }

  private RequestResponse sendRequest(long id, Action action) throws IOException, UnknownUserException {
    // Possible responses from server
    //  1. Request completed successfully
    //  2. Unknown ID
    //  3. Known ID, but cannot complete request (e.g., requested checkin, but id already checked in
    //  4. Failed to receive response from server (throw exception)
    // TODO throw exception for case 4

    // 1. serialize to JSON
    //    String content = "{\n";
    //    content += "    \"id\": \"" + id + "\",\n";
    //    content += "    \"action\": \"" + action + "\"\n";
    //    content += "}";
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    writer.setIndent("  ");
    writer.beginObject();
    writer.name("id").value(gson.toJson(id));
    writer.name("action").value(gson.toJson(action));
    writer.endObject();
    writer.close();
    String content = stringWriter.toString();

    // 2. send to server
    LOG.info("request type: " + JSON.toString());
    RequestBody body = RequestBody.create(JSON, content);
    Request request = new Request.Builder().url(serverUrl).post(body).build();
    Response response = client.newCall(request).execute();
    LOG.info("Received response code " + response.code() + " with message " + response.message());
    LOG.info("Request body:\n" + response.peekBody(100L*1024L).string());

    // 3. process response from server
    if (response.isSuccessful()) {
      RequestResponse requestResponse = parseResponse(id, action, response);
      response.close();
      return requestResponse;
    }

    // Throw exception? How to distinguish unsuccessful requests?
    response.close();
    return RequestResponse.FailedRequest;
  }

  private RequestResponse parseResponse(long id, Action action, Response response) throws IOException {
    MediaType type = response.body().contentType();
    if (!type.type().equalsIgnoreCase(JSON.type()) || !type.subtype().equalsIgnoreCase(JSON.subtype()) ||
        type.charset().compareTo(JSON.charset()) != 0) {
      throw new IOException("Unknown content type for response: " + type.toString());
    }
    Reader responseReader = response.body().charStream();

    // Parse JSON content
    long idResponse = -1;
    Action actionResponse = null;
    RequestResponse requestResponse = null;
    GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    JsonReader reader = new JsonReader(responseReader);
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      switch (name) {
        case "id":
          idResponse = reader.nextLong();
          if (idResponse != id) {
            throw new IOException("Expected id " + id + " but received id " + idResponse + " in response from server");
          }
          break;
        case "action":
          try {
            actionResponse = gson.fromJson(reader.nextString(), Action.class);
            if (actionResponse.compareTo(action) != 0) {
              throw new IOException("Expected action " + action + " but received action " + actionResponse + " in response from server");
            }
          } catch (JsonSyntaxException e) {
            reader.close();
            throw new IOException("Invalid action in response from server", e);
          }
          break;
        case "result":
          try {
            requestResponse = gson.fromJson(reader.nextString(), RequestResponse.class);
          } catch (JsonSyntaxException e) {
            reader.close();
            throw new IOException("Invalid request response in response from server", e);
          }
          break;
        default:
          reader.close();
          throw new IOException("Unknown property name in response from server: " + name);
      }
    }
    reader.endObject();
    reader.close();
    return requestResponse;
  }

  private static void logResponse(RequestResponse response, String prefix) {
    switch (response) {
      case Ok:
        LOG.info(prefix + ": received ok");
        break;
      case UnknownId:
        LOG.info(prefix + ": received Unknown ID; exiting...");
        System.exit(1);
        break;
      case FailedRequest:
      default:
        LOG.info(prefix + ": failed request; aborting...");
        System.exit(2);
    }
  }

  public static void main(String[] args) throws UnknownUserException {
    IMessenger m = new HttpMessenger("http://localhost:8080/attendance/request");
    try {
      RequestResponse response = m.checkIn(1);
      logResponse(response, "checkin");
      response = m.checkOut(1);
      logResponse(response, "checkout");
      response = m.checkIn(-1);
      logResponse(response, "checkout");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
