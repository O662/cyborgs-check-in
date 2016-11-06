package org.cyborgs3335.checkin.rfidreader;

import java.nio.charset.Charset;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JsscExamples {

  private final SerialPort serialPort;

  /**
   * Open port specified by port name and add an event listener.
   * @param portName serial port name; e.g., COM1 or /dev/...
   * @param addEventListener add event listener if true
   */
  public JsscExamples(String portName, boolean addEventListener) {
    serialPort = new SerialPort(portName); 
    // Event mask & SerialPortEventListener interface
    // Note: The mask is an additive quantity, thus to set a mask on the
    //       expectation of the arrival of Event Data (MASK_RXCHAR) and change
    //       the status lines CTS (MASK_CTS), DSR (MASK_DSR) we just need to
    //       combine all three masks.
    try {
      serialPort.openPort();
      serialPort.setParams(9600, 8, 1, 0);
      if (addEventListener) {
        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
        serialPort.setEventsMask(mask);
        serialPort.addEventListener(new SerialPortReader());
      }
    } catch (SerialPortException ex) {
      System.out.println(ex);
    }
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

  public static void writeToSerialPort(String portName) {
    SerialPort serialPort = new SerialPort(portName);
    try {
      serialPort.openPort();
      // Can also set params by: serialPort.setParams(9600, 8, 1, 0);
      serialPort.setParams(SerialPort.BAUDRATE_9600, 
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);
      serialPort.writeBytes("This is a test string".getBytes());
      serialPort.closePort();
    } catch (SerialPortException ex) {
      System.out.println(ex);
    }
  }

  public static void readFromSerialPort(String portName) {
    SerialPort serialPort = new SerialPort(portName);
    try {
      serialPort.openPort();
      serialPort.setParams(9600, 8, 1, 0);
      // TODO RFID uid could be up to 10 bytes!!!
      // TODO RFID uid could be up to 10 bytes!!!
      // TODO RFID uid could be up to 10 bytes!!!
      // TODO RFID uid could be up to 10 bytes!!!
      byte[] buffer = serialPort.readBytes(10); // Read 10 bytes from serial port
      String s = new String(buffer, Charset.forName("UTF-8"));
      System.out.println("Read: " + s);
      serialPort.closePort();
    } catch (SerialPortException ex) {
      System.out.println(ex);
    }
  }

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

  public void writeLine(String message) {
    try {
      serialPort.writeString(message);
    } catch (SerialPortException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    final String portName = (args.length == 1) ? args[0] : "/dev/ttyACM0";
    printSerialPortNames();
    //readFromSerialPort("");
    //readFromSerialPort(portName);
    //writeToSerialPort("");
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        JsscExamples jssc = new JsscExamples(portName, false/*true*/);
        int code = 0;
        while (true) {
          System.out.println("Read: \"" + jssc.readLine() + "\"");
          jssc.writeLine("" + (code++%3 + 1));
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
//        if (event.getEventValue() == 10) { // Check bytes count in the input buffer
//          //Read data, if 10 bytes available 
//          try {
//            byte[] buffer = serialPort.readBytes(10);
//            System.out.println("Read: " + Arrays.toString(buffer));
//          } catch (SerialPortException ex) {
//            System.out.println(ex);
//          }
//        }
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
//
// From https://code.google.com/archive/p/java-simple-serial-connector/wikis/jSSC_examples.wiki
//
//jSSC-0.7 examples
//Getting serial ports names
//
//```
//
//import jssc.SerialPortList;
//
//public class Main {
//
//public static void main(String[] args) {
//    String[] portNames = SerialPortList.getPortNames();
//    for(int i = 0; i < portNames.length; i++){
//        System.out.println(portNames[i]);
//    }
//}
//
//}
//
//```
//Writing data to serial port
//
//```
//
//import jssc.SerialPort; import jssc.SerialPortException;
//
//public class Main {
//
//public static void main(String[] args) {
//    SerialPort serialPort = new SerialPort("COM1");
//    try {
//        serialPort.openPort();//Open serial port
//        serialPort.setParams(SerialPort.BAUDRATE_9600, 
//                             SerialPort.DATABITS_8,
//                             SerialPort.STOPBITS_1,
//                             SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
//        serialPort.writeBytes("This is a test string".getBytes());//Write data to port
//        serialPort.closePort();//Close serial port
//    }
//    catch (SerialPortException ex) {
//        System.out.println(ex);
//    }
//}
//
//}
//
//```
//Reading data from serial port
//
//```
//
//import jssc.SerialPort; import jssc.SerialPortException;
//
//public class Main {
//
//public static void main(String[] args) {
//    SerialPort serialPort = new SerialPort("COM1");
//    try {
//        serialPort.openPort();//Open serial port
//        serialPort.setParams(9600, 8, 1, 0);//Set params.
//        byte[] buffer = serialPort.readBytes(10);//Read 10 bytes from serial port
//        serialPort.closePort();//Close serial port
//    }
//    catch (SerialPortException ex) {
//        System.out.println(ex);
//    }
//}
//
//}
//
//```
//Event mask & SerialPortEventListener interface
//Note: The mask is an additive quantity, thus to set a mask on the expectation of the arrival of Event Data (MASK_RXCHAR) and change the status lines CTS (MASK_CTS), DSR (MASK_DSR) we just need to combine all three masks.
//
//```
//
//import jssc.SerialPort; import jssc.SerialPortEvent; import jssc.SerialPortEventListener; import jssc.SerialPortException;
//
//public class Main {
//
//static SerialPort serialPort;
//
//public static void main(String[] args) {
//    serialPort = new SerialPort("COM1"); 
//    try {
//        serialPort.openPort();//Open port
//        serialPort.setParams(9600, 8, 1, 0);//Set params
//        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
//        serialPort.setEventsMask(mask);//Set mask
//        serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
//    }
//    catch (SerialPortException ex) {
//        System.out.println(ex);
//    }
//}
//
///*
// * In this class must implement the method serialEvent, through it we learn about 
// * events that happened to our port. But we will not report on all events but only 
// * those that we put in the mask. In this case the arrival of the data and change the 
// * status lines CTS and DSR
// */
//static class SerialPortReader implements SerialPortEventListener {
//
//    public void serialEvent(SerialPortEvent event) {
//        if(event.isRXCHAR()){//If data is available
//            if(event.getEventValue() == 10){//Check bytes count in the input buffer
//                //Read data, if 10 bytes available 
//                try {
//                    byte buffer[] = serialPort.readBytes(10);
//                }
//                catch (SerialPortException ex) {
//                    System.out.println(ex);
//                }
//            }
//        }
//        else if(event.isCTS()){//If CTS line has changed state
//            if(event.getEventValue() == 1){//If line is ON
//                System.out.println("CTS - ON");
//            }
//            else {
//                System.out.println("CTS - OFF");
//            }
//        }
//        else if(event.isDSR()){///If DSR line has changed state
//            if(event.getEventValue() == 1){//If line is ON
//                System.out.println("DSR - ON");
//            }
//            else {
//                System.out.println("DSR - OFF");
//            }
//        }
//    }
//}
//
//}