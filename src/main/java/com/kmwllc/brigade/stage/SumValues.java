package com.kmwllc.brigade.stage;

import org.slf4j.Logger;

import com.kmwllc.brigade.config.StageConfig;
import com.kmwllc.brigade.document.Document;
import com.kmwllc.brigade.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// TODO: use MathValues instead, it's much more flexible and better about handling type casting issues.
@Deprecated
public class SumValues extends AbstractStage {

  public final static Logger log = LoggerFactory.getLogger(SumValues.class.getCanonicalName());

  private List<String> inputFields = null;
  private String outputField = null;

  @Override
  public void startStage(StageConfig config) {

    if (config != null) {
      inputFields = config.getListParam("inputFields");
      outputField = config.getProperty("outputField");
    }
  }

  @Override
  public List<Document> processDocument(Document doc) {
    // divide the double values in 2 fields, store the result in the quotent
    // field.
    for (String inField : inputFields) {
      if (!doc.hasField(inField)) {
        // doc missing one of the input fields?
        // TODO: maybe we want to control this behavior (ignore unset fields?)
        return null;
      }
    }

    ArrayList<Double> results = new ArrayList<Double>();
    int size = doc.getField(inputFields.get(0)).size();
    for (int i = 0; i < size; i++) {
      try {
        // log.info("Compute {} divided by {}",
        // doc.getField(dividendField).get(i),
        // doc.getField(divisorField).get(i));
        Double sum = 0.0;
        for (String inField : inputFields) {
          sum += convertToDouble(doc.getField(inField).get(i));
        }
        results.add(sum);
      } catch (ClassCastException e) {
        log.warn("Division Error DocID: ", doc.getId());
        e.printStackTrace();
      }
    }

    for (Double v : results) {
      doc.addToField(outputField, v);
    }

    return null;
  }

  private Double convertToDouble(Object obj) throws ClassCastException {
    Double doubleVal = null;
    if (obj instanceof Integer) {
      doubleVal = new Double(((Integer) obj).intValue());
    } else if (obj instanceof Double) {
      doubleVal = (Double) obj;
    } else {
      throw new ClassCastException("Cannot convert " + obj.getClass().getName() + " to Double.");
    }

    return doubleVal;
  }

  @Override
  public void stopStage() {
    // no-op for this stage
  }

  @Override
  public void flush() {
    // no-op for this stage
  }

}
