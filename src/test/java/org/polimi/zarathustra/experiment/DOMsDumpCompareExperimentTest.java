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


package org.polimi.zarathustra.experiment;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.polimi.zarathustra.DOMHelper;
import org.polimi.zarathustra.DifferenceHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * Tests for the DOM comparing experiment of Zarathustra, {@link DOMsDumpComparerExperiment}.
 */
public class DOMsDumpCompareExperimentTest extends TestCase {

  private Document initDefaultDocument() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document document = factory.newDocumentBuilder().newDocument();
    Element root = document.createElement("rootElement");
    document.appendChild(root);
    root.appendChild(document.createTextNode("Data"));
    return document;
  }

  public void testDump() throws Exception {
    File outputDir = Files.createTempDir();

    File tempDir1 = Files.createTempDir();
    File dumpFile1 = new File(tempDir1, "foobar" + DOMHelper.DOM_DUMP_SUFFIX);
    File tempDir2 = Files.createTempDir();
    File dumpFile2 = new File(tempDir2, "foobar" + DOMHelper.DOM_DUMP_SUFFIX);

    Document firstDocument = initDefaultDocument();
    Document secondDocument = initDefaultDocument();
    secondDocument.getChildNodes().item(0).appendChild(secondDocument.createElement("script"));

    DOMHelper.serializeDocument(firstDocument, dumpFile1.getAbsolutePath());
    DOMHelper.serializeDocument(secondDocument, dumpFile2.getAbsolutePath());

    String[] args =
        {tempDir1.getAbsolutePath(), tempDir2.getAbsolutePath(), outputDir.getAbsolutePath()};
    DOMsDumpCompareExperiment.main(args);

    String expectedXpath =
        DifferenceHelper
            .getDifferences(firstDocument, ImmutableList.<Document>of(), secondDocument).get(0)
            .getTestNodeDetail().getXpathLocation();
    String readXpath = DifferenceHelper.readDifferenceDump(outputDir.listFiles()[0]).get(0);
    assertTrue(readXpath.contains(expectedXpath));
  }
}
