package com.kmwllc.brigade.event;

import java.util.List;

import com.kmwllc.brigade.document.Document;
import com.kmwllc.brigade.document.ProcessingStatus;

/**
 * TODO: review this , this is what the workflow server should implement.
 * 
 * @author kwatters
 *
 */
public interface DocumentListener {

  void onDocument(Document doc);
}