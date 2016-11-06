package org.cyborgs3335.checkin;

import java.nio.charset.Charset;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialComm {

  private final SerialPort serialPort;

  /**
   * Open port specified by port name and add an event listener.
   * @param portName serial port name; e.g., COM1 or /dev/...
   * @param addEventListener add event listener if true
   * @throws SerialPortException 
   */
  public SerialComm(String portName, boolean addEventListener) throws SerialPortException {
    serialPort = new SerialPort(portName); 
    // Event mask & SerialPortEventListener interface
    // Note: The mask is an additive quantity, thus to set a mask on the
    //       expectation of the arrival of Event Data (MASK_RXCHAR) and change
    //       the status lines CTS (MASK_CTS), DSR (MASK_DSR) we just need to
    //       combine all three masks.
    //try {
    serialPort.openPort();
    serialPort.setParams(9600, 8, 1, 0);
    if (addEventListener) {
      int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
      serialPort.setEventsMask(mask);
      serialPort.addEventListener(new SerialPortReader());
    }
    //} catch (SerialPortException ex) {
    //  System.out.println(ex);
    //}
  }

  public static void printSerialPortNames() {
    System.out.println("Fetching serial port names...");
    String[] portNames = SerialPortList.getPortNames();
    if (portNames.length ==0) {
      System.out.println("Found no serial ports.");
      return;
    }
    for (String portName : portNames) {
      System.out.println("Port: " + portName);
    }
  }

  /**
   * Read bytes until the next newline character, and convert to String using the default charset.
   * @return the next line
   */
  public String readLine() {
    byte newLine = 10;
    byte[] line = new byte[1024];
    int i = 0;
    byte currentByte;
    try {
      while ((currentByte = serialPort.readBytes(1)[0]) != newLine) {
        if (currentByte == 13) {
          // skip carriage returns
          continue;
        }
        line[i++] = currentByte;
      }
    } catch (SerialPortException e) {
      e.printStackTrace();
      return null;
    }
    return new String(line);
  }

  /**
   * Write String
   * @param message
   */
  public void writeString(String message) {
    try {
      serialPort.writeString(message);
    } catch (SerialPortException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    final String portName = (args.length == 1) ? args[0] : "/dev/ttyACM0";
    printSerialPortNames();
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        SerialComm comm;
        try {
          comm = new SerialComm(portName, false/*true*/);
        } catch (SerialPortException e) {
          throw new RuntimeException("Caught serial port exception.  Exiting...", e);
        }
        int code = 0;
        while (true) {
          System.out.println("Read: \"" + comm.readLine() + "\"");
          comm.writeString("" + (code++%3 + 1));
        }
      }}, "SerialPortReader");
    //t.setDaemon(true);
    t.start();
  }

  /*
   * In this class must implement the method serialEvent, through it we learn about 
   * events that happened to our port. But we will not report on all events but only 
   * those that we put in the mask. In this case the arrival of the data and change the 
   * status lines CTS and DSR
   */
  public class SerialPortReader implements SerialPortEventListener {

    public void serialEvent(SerialPortEvent event) {
      if (event.isRXCHAR()) { // If data is available
        int nbytes = event.getEventValue(); // Check bytes count in the input buffer
        try {
          byte[] buffer = serialPort.readBytes(nbytes);
          //System.out.println("Read: " + Arrays.toString(buffer));
          String s = new String(buffer, Charset.forName("UTF-8"));
          while (serialPort.getInputBufferBytesCount() > 0) {
            buffer = serialPort.readBytes();
            s += new String(buffer, Charset.forName("UTF-8"));
          }
          System.out.println("Read: \"" + s + "\"");
        } catch (SerialPortException ex) {
          System.out.println(ex);
        }
      } else if (event.isCTS()) { // If CTS line has changed state
        if (event.getEventValue() == 1) { // If line is ON
          System.out.println("CTS - ON");
        } else {
          System.out.println("CTS - OFF");
        }
      } else if (event.isDSR()) { // If DSR line has changed state
        if (event.getEventValue() == 1) { // If line is ON
          System.out.println("DSR - ON");
        } else {
          System.out.println("DSR - OFF");
        }
      }
    }
  }
}
