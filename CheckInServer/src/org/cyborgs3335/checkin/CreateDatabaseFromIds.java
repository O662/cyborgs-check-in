package org.cyborgs3335.checkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Dumps all the records from the server database.
 *
 * @author brian
 *
 */
public class CreateDatabaseFromIds {

  /**
   * Print the last check-in event for each attendance record.
   * @param server check in server to print
   */
  public static void print(CheckInServer server) {
    CheckInActivity activity = server.getActivity();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    if (activity != null) {
      activity.print(dateFormat);
    }
    Set<Long> set = server.getIdSet();
    synchronized (set) {
      for (Long id : set) {
        AttendanceRecord record = server.getAttendanceRecord(id);
        System.out.print("id " + id + " \tname " + record.getPerson() + " \tevents: ");
        //System.out.println(id + " " + record.getPerson());
        ArrayList<CheckInEvent> list = record.getEventList();
        for (CheckInEvent event : list) {
          if (event.getActivity() != null) {
            System.out.print(" " + event.getActivity().getName());
          } else {
            System.out.print(" " + event.getActivity());
          }
          System.out.print(" " + event.getStatus() + " " + dateFormat.format(new Date(event.getTimeStamp())));
        }
        System.out.println();
      }
    }
  }

  /**
   * Load a list of IDs and user names from the input path into the server.
   * @param path path to existing list of IDs and user names, one pair per line
   * @param server check in server to load into
   */
  public static void load(String path, CheckInServer server) throws IOException {
    long timeBeg = System.currentTimeMillis();
    timeBeg = 3600L * 1000L * (timeBeg/(3600L * 1000L)); // Truncate to current hour
    // Activity starts at current hour + 2 hours
    CheckInActivity activity = new CheckInActivity("Default", timeBeg, timeBeg + 2L * 3600L * 1000L);
    server.setActivity(activity);

    BufferedReader reader = new BufferedReader(new FileReader(path));
    String line = null;
    int lineno = 0;
    while ((line = reader.readLine()) != null) {
      System.out.println("line " + ++lineno + ": " + line);
      String[] vals = line.split(" ");
      long id = Long.parseLong(vals[0]);
      String firstName = vals[1];
      String lastName = vals[2];
      Person p = server.addUserWithId(id, firstName, lastName);
      if (p == null) {
        System.out.println("Could not add " + firstName + " " + lastName + " because ID "
            + id + " already exists in the server!");
      }
    }
    reader.close();
  }

  /**
   * Create command line options
   * @return command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption(Option.builder("i").longOpt("infile").hasArg()
        .argName("input-file")
        .desc("input IDs text file containing ID and user names, one per line; e.g.,\n"
            + "1 John Doe\n"
            + "2 Jane Dough\n"
            + "3 Jerry Deaux\n")
        .build());
    options.addOption(Option.builder("o").longOpt("outdir").hasArg()
        .argName("output-directory")
        .desc("output database directory, such as "
            + "$HOME/CyborgsCheckIn/check-in-server-2017-kickoff.dump")
        .build());
    options.addOption(Option.builder("h").longOpt("help")
        .desc("print this message").build());
    return options;
  }

  /**
   * Main application starts up server, then dumps all records from specified database to stdout.
   * @param args path to database
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String pathIn = null;
    String pathOut = null;

    // Create the command line parser
    CommandLineParser parser = new DefaultParser();
    Options options = createOptions();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(CreateDatabaseFromIds.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption("infile")) {
        pathIn = line.getOptionValue("infile");
      } else {
        System.out.println("Missing required option -i | --infile");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(CreateDatabaseFromIds.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption("outdir")) {
        pathOut = line.getOptionValue("outdir");
      } else {
        System.out.println("Missing required option -o | --outdir");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(CreateDatabaseFromIds.class.getName(), options);
        System.exit(1);
      }
    } catch (ParseException exp) {
      System.out.println("Unexpected exception:" + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CreateDatabaseFromIds.class.getName(), options);
      System.exit(1);
    }

//    if (args.length == 0) {
//      pathIn = "/home/brian/CyborgsCheckIn/test_ids.txt";
//      pathOut = "/home/brian/CyborgsCheckIn/test_createdatabase_check-in-server.dump";
//    } else if (args.length == 2) {
//      pathIn = args[0];
//      pathOut = args[1];
//    } else {

    CheckInServer server = CheckInServer.getInstance();
    File fileIn = new File(pathIn);
    if (!fileIn.exists()) {
      throw new RuntimeException("Could not find input IDs file \"" + pathIn + "\"");
    }
    File dirOut = new File(pathOut);
    if (dirOut.exists()) {
      throw new RuntimeException("Output database \"" + pathOut + "\" already exists!");
    }
    boolean success = dirOut.mkdirs();
    if (!success) {
      throw new RuntimeException("Error creating directory for output database!");
    }
    //if (dir.exists()) {
    //  server.load(path);
    //} else {
    //  throw new RuntimeException("Could not open database directory " + path + "!");
    //}
    System.out.println("Initial state of server:");
    print(server);
    System.out.println("Loading server...");
    load(pathIn, server);
    System.out.println("State of server after load:");
    print(server);
    System.out.println("Saving database...");
    server.dump(pathOut);
    System.out.println("Complete.");
  }

}
