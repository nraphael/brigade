package com.kmwllc.brigade;

import com.kmwllc.brigade.config.*;
import com.kmwllc.brigade.connector.AbstractConnector;
import com.kmwllc.brigade.connector.ConnectorServer;
import com.kmwllc.brigade.connector.ConnectorState;
import com.kmwllc.brigade.logging.LoggerFactory;
import com.kmwllc.brigade.utils.BrigadeRunner;
import com.kmwllc.brigade.utils.FileUtils;
import com.kmwllc.brigade.workflow.Workflow;
import com.kmwllc.brigade.workflow.WorkflowServer;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Brigade:  Is is a connector and pipeline framework for processing documents.
 * It consists of a Connector and a Workflow.  The workflow is a set of stages that
 * operate on a document.  From the command line you can start the connector
 * and the workflow to start processing data.
 * <p>
 * Pass on the command line
 * -c conf/connector.xml      (location of connector config file)
 * -w conf/workflow.xml       (location of the workflow definition)
 * -p conf/brigade.properties (location of the properties file)
 * <p>
 * Maintained by KMW Technology
 * http://www.kmwllc.com/
 *
 * @author kwatters
 */
public class Brigade {

  public final static Logger log = LoggerFactory.getLogger(Brigade.class.getCanonicalName());
  // brigade is a singleton server instance
  private static Brigade brigadeServer = null;
  private BrigadeConfig config = null;
  private boolean running = false;

  private Brigade() {
    // Don't allow it to be instantiated directly.
  }

  private void loadConfig() throws Exception {
    // we have a serialized version of a brigade config.
    // The config can be a workflow and a connector config

    // Add and initialize all workflows
    WorkflowServer ws = WorkflowServer.getInstance();
    for (WorkflowConfig<?> wC : config.getWorkflowConfigs()) {
      ws.addWorkflow(wC, config.getProps());
    }

    // Add all connector configs
    // Add and initialize all workflows
    ConnectorServer cS = ConnectorServer.getInstance();
    for (ConnectorConfig cc : config.getConnectorConfigs()) {
      cS.addConnector(cc);
    }

    for (ConnectorConfig cc : config.getConnectorConfigs()) {
      AbstractConnector c = ConnectorServer.getInstance().getConnector(cc.getConnectorName());
      // Attach connector to WF as listener
      String workflowName = cc.getStringParam("workflowName");
      Workflow wf = WorkflowServer.getInstance().getWorkflow(workflowName);
      wf.addCallbackListener(c);
      wf.addDocumentListener(c);
    }
  }

  public static Brigade getInstance() {
    if (brigadeServer == null) {
      brigadeServer = new Brigade();
    }
    return brigadeServer;
  }

  public void start() throws Exception {
    // start the server .. etc.. etc..
    // Add a default connector

    log.info("Brigade Starting up.");
    if (config == null) {
      // TODO create a default config?
      log.warn("Error config was null!");
      System.exit(-1);
    }

    log.info("Loading config");
    try {
      loadConfig();
      running = true;
    } catch (ClassNotFoundException e) {
      log.warn("Load config error : {}", e);
      running = false;
      throw e;
    }
  }

  public void shutdown(boolean systemExit) {
    log.info("Shutting down.");
    running = false;
    log.info("Shut down.");
    // We are done, exit
    if (systemExit) {
      System.exit(0);
    }
  }

  public BrigadeConfig getConfig() {
    return config;
  }

  public void setConfig(BrigadeConfig config) {
    this.config = config;
  }

  public boolean isRunning() {
    return running;
  }

  public void startConnector(String connectorName) throws InterruptedException {
    // This needs to fire off a job.
    // Thread me .. join later
    ConnectorServer cS = ConnectorServer.getInstance();
    if (cS.hasConnector(connectorName)) {
      log.info("Called start connector : {}", connectorName);
      // TODO: move the start call to the connector server class
      // TODO: also this is currently synchronous .. we want this to be a message
      // so we don't block the ui here. (maybe?)
      cS.startConnector(connectorName);
      log.info("Connector started.");
    } else {
      log.info("Unknown connector : {}", connectorName);
    }
  }

  public void waitForConnector(String connectorName) throws Exception {
    log.info("Waiting on connector {} to complete", connectorName);
    ConnectorServer cS = ConnectorServer.getInstance();
    ConnectorState s = cS.getConnectorState(connectorName);
    log.info("Connector state is {}", s);

    // TODO: do this more async , rather than polling.
    while (s == ConnectorState.OFF || s == ConnectorState.RUNNING) {
      s = cS.getConnectorState(connectorName);
    }
    if (s == ConnectorState.ERROR) {
      throw new Exception("Connector returned in error state");
    }
    log.info("connector {} is not running.", connectorName);
  }

  /**
   * Invoke Brigade from the command line.  The flags specify paths to configuration objects; these
   * are resolved by calling BrigadeRunner.init().  For each configuration path, if there is a physical
   * file at that path, a configuration object will be built from the file.  If there is not a physical
   * file, BrigadeRunner will attempt to build the configuration object by streaming a resource from the
   * classpath.  If this fails, an exception is thrown.  Once a BrigadeRunner is assembled, it is executed
   * and the program exits.
   * @param args User supplied args
   * @throws InterruptedException
   * @throws IOException
   * @throws ParseException
   */
  public static void main(String[] args) throws InterruptedException, IOException, ParseException, ConfigException, ClassNotFoundException {

    // create Options object
    Options options = new Options();

    options.addOption("c", true, "specify the connector config file.");
    options.addOption("w", true, "specify the workflow config file.");
    options.addOption("p", true, "specify the properties file.");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    // validate command line args
    if (cmd.hasOption("h") || !(cmd.hasOption("c") && cmd.hasOption("w") && cmd.hasOption("p"))) {
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar brigade.jar -c connector.xml -w workflow.xml -p brigade.properties", options);
      System.exit(1);
    }

    long startTime = System.currentTimeMillis();

    // set the params
    String propertiesFile = cmd.getOptionValue("p");
    String connectorFile = cmd.getOptionValue("c");
    String workflowFile = cmd.getOptionValue("w");

    BrigadeRunner br = BrigadeRunner.init(propertiesFile, connectorFile, workflowFile);

    try {
      br.exec();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0);
  }
}
