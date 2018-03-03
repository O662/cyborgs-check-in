package org.cyborgs3335.checkin.server.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cyborgs3335.checkin.server.http.ICheckInDataStore;

public class LoadPojoToJdbc {

  private static final Logger LOG = Logger.getLogger(LoadPojoToJdbc.class.getName());

  private static final String POJO_PATH_OPTION = "pojopath";

  private static final String JDBC_PATH_OPTION = "jdbcpath";

  private static final String HELP_OPTION = "help";

  /**
   * Main application to load database records from a POJO dump file into a
   * JDBC database file.
   * @param args commandline arguments
   * @throws IOException on I/O errors
   */
  public static void main(String[] args) throws IOException {
    Map<String, String> map = parseOptions(args);

    // Get "original" record from POJO
    String pathOrig = map.get(POJO_PATH_OPTION) + File.separator + ICheckInDataStore.DB_ATTENDANCE_RECORDS;
    PojoInput pojoOrig = PojoInput.loadAttendanceRecords(pathOrig);

    // Save to JDBC (sqlite database)
    String pathJdbc = map.get(JDBC_PATH_OPTION);
    File dir = new File(pathJdbc);
    if (!dir.exists()) {
      LOG.info("Creating directory " + pathJdbc + " for saving database...");
      boolean success = dir.mkdirs();
      if (!success) {
        throw new IOException("Could not create directory " + pathJdbc
            + "for saving database!");
      }
    }
    LOG.info("Saving attendance records to " + pathJdbc);
    String pathJdbcDb = "jdbc:sqlite:" + map.get(JDBC_PATH_OPTION) + File.separator + ICheckInDataStore.DB_ATTENDANCE_RECORDS;
    JdbcOutput.dumpAttendanceRecords(pathJdbcDb, pojoOrig.getCheckInActivity(), pojoOrig.getMap());
  }

  /**
   * Parse options from commandline arguments.
   * @param args commandline arguments
   * @return map containing options
   */
  private static Map<String, String> parseOptions(String[] args) {
    Map<String, String> map = new java.util.HashMap<String, String>();

    // Create the command line parser
    CommandLineParser parser = new DefaultParser();
    Options options = createOptions();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption(HELP_OPTION)) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(LoadPojoToJdbc.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption(POJO_PATH_OPTION)) {
        map.put(POJO_PATH_OPTION, line.getOptionValue(POJO_PATH_OPTION));
      } else {
        System.out.println("Missing required option -p | --" + POJO_PATH_OPTION);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(LoadPojoToJdbc.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption(JDBC_PATH_OPTION)) {
        map.put(JDBC_PATH_OPTION, line.getOptionValue(JDBC_PATH_OPTION));
      } else {
        System.out.println("Missing required option -j | --" + JDBC_PATH_OPTION);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(LoadPojoToJdbc.class.getName(), options);
        System.exit(1);
      }
    } catch (ParseException exp) {
      System.out.println("Unexpected exception:" + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(LoadPojoToJdbc.class.getName(), options);
      System.exit(1);
    }
    return map;
  }

  /**
   * Create command line options
   * @return command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption(Option.builder("p").longOpt(POJO_PATH_OPTION).hasArg()
        .argName("pojo-path")
        .desc("input pojo directory, such as $HOME/CyborgsCheckIn/check-in-server.dump")
        .build());
    options.addOption(Option.builder("j").longOpt(JDBC_PATH_OPTION).hasArg()
        .argName("jdbc-path")
        .desc("output jdbc path, such as $HOME/CyborgsCheckIn/check-in-server.jdbc")
        .build());
    options.addOption(Option.builder("h").longOpt(HELP_OPTION)
        .desc("print this message").build());
    return options;
  }
}
