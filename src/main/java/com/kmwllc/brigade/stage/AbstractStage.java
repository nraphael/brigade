package com.kmwllc.brigade.stage;

import com.kmwllc.brigade.config.StageConfig;
import com.kmwllc.brigade.document.Document;
import com.kmwllc.brigade.logging.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for all stages in a pipeline.  The stages have a lifecycle 
 * startStage takes the configuration and initalizes. 
 * process document is where the main processing of a document happens,
 * flush tells the stage to flush any pending data
 * stopStage is (should be) called when the stage is shutting down.
 * 
 * @author kwatters
 *
 */
public abstract class AbstractStage {

  private Map<String, String> props = new HashMap<>();
  private String enabled;
  private String skipIfField;
  
  // TODO: make sure the subclasses get the right logger..
  public final static Logger log = LoggerFactory.getLogger(AbstractStage.class.getCanonicalName());
  // Process only when output field doesn't exist in the document
  // Stages that support this should check and handle it in their
  // processDocument()
  protected boolean processOnlyNull = false;

  public void init(StageConfig config) {
    enabled = config.getStringParam("enabled");
    skipIfField = config.getStringParam("skipIfField");
    startStage(config);
  }

  public abstract void startStage(StageConfig config);

  public abstract List<Document> processDocument(Document doc) throws Exception;

  public abstract void stopStage();

  public abstract void flush();

  public Map<String, String> getProps() {
    return props;
  }

  public void setProps(Map<String, String> props) {
    this.props = props;
  }

  public String getEnabled() {
    return enabled;
  }

  public String getSkipIfField() {
    return skipIfField;
  }
}
