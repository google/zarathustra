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

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests for the {@link DOMVisualizer} class
 */
public class DOMVisualizerTest extends TestCase {
  private Document initDefaultDocument() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document document = factory.newDocumentBuilder().newDocument();
    Element root = document.createElement("rootElement");
    document.appendChild(root);
    root.appendChild(document.createTextNode("Data"));
    return document;
  }

  public void testDomOutput() throws Exception {
    Document defaultDocument = initDefaultDocument();
    File dumpFile = File.createTempFile("tempFile", "");
    DOMHelper.serializeDocument(defaultDocument, dumpFile.getAbsolutePath());
    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    DOMVisualizer.printDOM(dumpFile, outputBuffer);
    String expectedOutput = "<rootElement>Data</rootElement>\n";
    assertEquals(expectedOutput, outputBuffer.toString());
  }

  public void testNoSelfClosingTags() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document document = factory.newDocumentBuilder().newDocument();
    Element root = document.createElement("rootElement");
    document.appendChild(root);
    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    DOMVisualizer.printDOM(document, outputBuffer);
    String expectedOutput = "<rootElement></rootElement>\n";
    assertEquals(expectedOutput, outputBuffer.toString());
  }
}
