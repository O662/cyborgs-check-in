package org.cyborgs3335.checkin.server.http;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;

public class SimpleJettyServer {

  public enum DataStoreType {
    Memory,
    Jdbc
  }

  private static final Logger LOG = Logger.getLogger(SimpleJettyServer.class.getName());

  private static final String DATABASE_PATH_OPTION = "dbpath";

  private static final String PORT_OPTION = "port";

  private static final String HELP_OPTION = "help";

  private final ICheckInDataStore dataStore;

  public SimpleJettyServer(String databasePath) throws IOException {
    this(databasePath, DataStoreType.Memory);
  }

  public SimpleJettyServer(String databasePath, DataStoreType type) throws IOException {
    //dataStore = CheckInDataStore.getInstance();
    File dir = new File(databasePath);
    if (dir.exists()) {
      LOG.info("Loading attendance records from " + databasePath);
    } else {
      LOG.info("No attendance records found at path " + databasePath
          + ". Creating directory for saving database.");
      boolean success = dir.mkdirs();
      if (!success) {
        throw new IOException("Could not create directory " + databasePath
            + "for saving database!");
      }
    }
    //dataStore.load(databasePath);
    ICheckInDataStore store = null;
    switch (type) {
      case Memory:
        store  = CheckInDataStore.getInstance().load(databasePath);
        break;
      case Jdbc:
      default:
        store  = CheckInDataStoreJdbc.getInstance().load(databasePath);
        break;
    }
    dataStore  = store;
  }

  public ICheckInDataStore getDataStore() {
    return dataStore;
  }


  /**
   * Create command line options
   * @return command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption(Option.builder("d").longOpt(DATABASE_PATH_OPTION).hasArg()
        .argName("database-path")
        .desc("output database directory, such as "
            + "$HOME/CyborgsCheckIn/check-in-server-2017-kickoff.dump")
        .build());
    options.addOption(Option.builder("p").longOpt(PORT_OPTION).hasArg()
        .argName("http-port")
        .desc("port to use for http server")
        .build());
    options.addOption(Option.builder("h").longOpt(HELP_OPTION)
        .desc("print this message").build());
    return options;
  }

  /**
   * Main application to start up the http server and the backend &quot;database&quot;.
   * @param args commandline arguments
   * @throws Exception on server failure
   */
  public static void main(String[] args) throws Exception {
    String databasePath = "/tmp/check-in-store.db";
    int port = 8080;

    // Create the command line parser
    CommandLineParser parser = new DefaultParser();
    Options options = createOptions();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption(HELP_OPTION)) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SimpleJettyServer.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption(DATABASE_PATH_OPTION)) {
        databasePath = line.getOptionValue(DATABASE_PATH_OPTION);
      } else {
        System.out.println("Missing required option -d | --" + DATABASE_PATH_OPTION);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SimpleJettyServer.class.getName(), options);
        System.exit(1);
      }
      if (line.hasOption(PORT_OPTION)) {
        port = Integer.parseInt(line.getOptionValue(PORT_OPTION));
      } else {
        System.out.println("Missing required option -p | --" + PORT_OPTION);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SimpleJettyServer.class.getName(), options);
        System.exit(1);
      }
    } catch (ParseException exp) {
      System.out.println("Unexpected exception:" + exp.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(SimpleJettyServer.class.getName(), options);
      System.exit(1);
    }

    SimpleJettyServer sjs = new SimpleJettyServer(databasePath, DataStoreType.Jdbc);
    Server server = new Server(port);
    server.setHandler(new CheckInHandler(sjs.getDataStore()));
    server.start();
    server.dumpStdErr();
    server.join();
  }
}
