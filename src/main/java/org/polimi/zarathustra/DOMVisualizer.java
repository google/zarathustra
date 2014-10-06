/*
 * Copyright 2014 Google Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.polimi.zarathustra;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

/**
 * A simple class to output the content of a DOM serialized to file. This is a support class meant
 * to be used while debugging or testing Zarathustra.
 */
public class DOMVisualizer {

  /**
   * Loads the given file and prints it out as a DOM tree.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Specify path for DOM file.");
    }
    String path = args[0];
    File domDump = new File(path);
    printDOM(domDump, System.out);
  }

  @VisibleForTesting
  static void printDOM(Document document, OutputStream out) throws IOException,
      TransformerFactoryConfigurationError, TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty("method", "html");
    Source source = new DOMSource(document);
    Result output = new StreamResult(out);
    transformer.transform(source, output);
  }

  @VisibleForTesting
  static void printDOM(File domDump, OutputStream out) throws IOException,
      TransformerFactoryConfigurationError, TransformerException {
    Document document = DOMHelper.deserializeDocument(domDump);
    printDOM(document, out);
  }
}
