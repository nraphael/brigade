package com.kmwllc.brigade.stage;

import java.util.List;

import com.kmwllc.brigade.config.StageConfig;
import com.kmwllc.brigade.document.Document;

/**
 * This stage will remove all but the N values from a field on a document.
 * default to just the first 1 value.
 * 
 * @author kwatters
 *
 */
public class TruncateFieldValues extends AbstractStage {

  private int numValues = 1;
  private String inputField = null;
  private String outputField = null;

  @Override
  public void startStage(StageConfig config) {

    if (config != null) {
      inputField = config.getProperty("inputField");
      outputField = config.getProperty("outputField");
      numValues = Integer.valueOf(config.getProperty("numValues", "1"));
    }

    // no op
    if (outputField == null) {
      outputField = inputField;
    }
  }

  @Override
  public List<Document> processDocument(Document doc) {
    if (doc.hasField(inputField)) {
      List<Object> values = doc.getField(inputField);
      if (values.size() > numValues) {
        values = values.subList(0, numValues);
        if (inputField.equals(outputField)) {
          doc.removeField(inputField);
        }
        for (Object o : values) {
          doc.addToField(outputField, o);
        }
      }
    }
    return null;
  }

  @Override
  public void stopStage() {
    // no-op for this stage
  }

  @Override
  public void flush() {
    // no-op for this stage
  }

  public int getNumValues() {
    return numValues;
  }

  public void setNumValues(int numValues) {
    this.numValues = numValues;
  }

  public String getInputField() {
    return inputField;
  }

  public void setInputField(String inputField) {
    this.inputField = inputField;
  }

  public String getOutputField() {
    return outputField;
  }

  public void setOutputField(String outputField) {
    this.outputField = outputField;
  }

}
