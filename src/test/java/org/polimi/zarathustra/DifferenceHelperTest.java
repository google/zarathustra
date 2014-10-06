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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.Difference;
import org.polimi.zarathustra.webdriver.WebdriverHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * Tests for the {@link DOMHelper} class
 */
public class DifferenceHelperTest extends TestCase {
  private static final List<Document> NO_DOCS = ImmutableList.of();
  private Document firstDocument;
  private Document secondDocument;
  private Document thirdDocument;
  private File firstFile;
  private File secondFile;
  private File thirdFile;

  private Document initDefaultDocument() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    Document document = factory.newDocumentBuilder().newDocument();
    Element root = document.createElement("rootElement");
    document.appendChild(root);
    Attr scriptAttribute = document.createAttribute("src");
    scriptAttribute.setValue("http://localhost");
    Element script = document.createElement("script");
    script.setAttributeNode(scriptAttribute);
    root.appendChild(script);
    root.appendChild(document.createTextNode("Data"));
    return document;
  }

  private void serializeDocs() throws IOException {
    firstFile = File.createTempFile("tempDoc", "");
    secondFile = File.createTempFile("tempDoc", "");
    thirdFile = File.createTempFile("tempDoc", "");
    DOMHelper.serializeDocument(firstDocument, firstFile.getAbsolutePath());
    DOMHelper.serializeDocument(secondDocument, secondFile.getAbsolutePath());
    DOMHelper.serializeDocument(thirdDocument, thirdFile.getAbsolutePath());
  }

  @Override
  public void setUp() throws Exception {
    firstDocument = initDefaultDocument();
    secondDocument = initDefaultDocument();
    thirdDocument = initDefaultDocument();

    serializeDocs();
  }

  public void test_DifferentOrderOfNodesIsNotADiff() throws Exception {
    String page1 = "<html><head></head><body><tag1></tag1><tag2></tag2></body></html>";
    String page2 = "<html><head></head><body><tag2></tag2><tag1></tag1></body></html>";
    Document dom1 = WebdriverHelper.getDom(page1);
    Document dom2 = WebdriverHelper.getDom(page2);
    List<Difference> differences = DifferenceHelper.getDifferences(dom1, NO_DOCS, dom2);
    assertTrue(differences.isEmpty());
  }

  public void test_DocumentEquals() {
    assertTrue(DifferenceHelper.domIsEqual(firstDocument, secondDocument));
  }

  public void test_DocumentFromFileEquals() throws IOException {
    assertTrue(DifferenceHelper.domIsEqual(firstFile, secondFile));
  }

  public void test_doubleDifference_on_attribute() throws IOException {
    Element nodeToEdit = (Element) thirdDocument.getChildNodes().item(0).getChildNodes().item(0);
    Attr attributeToEdit = thirdDocument.createAttribute("src");
    attributeToEdit.setValue("http://127.0.0.1/");
    nodeToEdit.setAttributeNode(attributeToEdit);
    serializeDocs();
    List<Difference> diff =
        DifferenceHelper.getDifferences(firstFile, ImmutableList.of(secondFile), thirdFile);

    assertEquals(1, diff.size());
    assertEquals("/rootElement[1]/script[1]/@src", diff.get(0).getTestNodeDetail()
        .getXpathLocation());
  }

  public void test_doubleDifference_on_attribute_numbers() throws IOException {
    Element nodeToEdit = (Element) thirdDocument.getChildNodes().item(0).getChildNodes().item(0);
    Attr attributeToAdd = thirdDocument.createAttribute("type");
    attributeToAdd.setValue("text/javascript");
    nodeToEdit.setAttributeNode(attributeToAdd);
    serializeDocs();
    List<Difference> diff =
        DifferenceHelper.getDifferences(firstFile, ImmutableList.of(secondFile), thirdFile);

    assertEquals(1, diff.size());
    assertEquals("/rootElement[1]/script[1]", diff.get(0).getTestNodeDetail().getXpathLocation());
  }

  public void test_doubleDifference_on_node() throws IOException {
    thirdDocument.getChildNodes().item(0).appendChild(thirdDocument.createElement("script"));
    serializeDocs();
    List<Difference> diff =
        DifferenceHelper.getDifferences(firstFile, ImmutableList.of(secondFile), thirdFile);

    assertEquals(1, diff.size());
    assertEquals("/rootElement[1]/script[2]", diff.get(0).getTestNodeDetail().getXpathLocation());
  }

  public void test_doubleDifference_withBaseDifferences() throws IOException {
    firstDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(firstDocument.createTextNode("VAL1"));
    secondDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(secondDocument.createTextNode("VAL2"));
    secondDocument.getChildNodes().item(0).appendChild(secondDocument.createElement("script"));
    thirdDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(thirdDocument.createTextNode("VAL3"));
    thirdDocument.getChildNodes().item(0).appendChild(thirdDocument.createElement("script"));
    Element nodeToEdit = (Element) thirdDocument.getChildNodes().item(0).getChildNodes().item(0);
    Attr attributeToEdit = thirdDocument.createAttribute("src");
    attributeToEdit.setValue("http://127.0.0.1/");
    nodeToEdit.setAttributeNode(attributeToEdit);
    serializeDocs();
    List<Difference> diff =
        DifferenceHelper.getDifferences(firstFile, ImmutableList.of(secondFile), thirdFile);

    assertEquals(1, diff.size());
    assertEquals("/rootElement[1]/script[1]/@src", diff.get(0).getTestNodeDetail()
        .getXpathLocation());
  }

  public void test_doubleDifference_withDifferentDifferences() throws IOException {
    firstDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(firstDocument.createTextNode("VAL1"));
    secondDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(secondDocument.createElement("foobar"));
    secondDocument.getChildNodes().item(0).appendChild(secondDocument.createTextNode("Data2"));
    thirdDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(thirdDocument.createTextNode("VAL3"));
    thirdDocument.getChildNodes().item(0).getChildNodes().item(0)
        .appendChild(thirdDocument.createElement("barfoo"));
    thirdDocument.getChildNodes().item(0).appendChild(thirdDocument.createTextNode("Data3"));

    serializeDocs();
    List<Difference> diff =
        DifferenceHelper.getDifferences(firstFile, ImmutableList.of(secondFile), thirdFile);

    assertEquals(2, diff.size());
    assertEquals("/rootElement[1]/script[1]/text()[1]", diff.get(0).getTestNodeDetail()
        .getXpathLocation());
    assertEquals("/rootElement[1]/script[1]/barfoo[1]", diff.get(1).getTestNodeDetail()
        .getXpathLocation());
  }

  public void test_DumpDifferences() throws IOException {
    thirdDocument.getChildNodes().item(0).appendChild(thirdDocument.createElement("script"));
    serializeDocs();

    File tempFile = File.createTempFile("tempDiff", "");
    List<Difference> differences =
        DifferenceHelper.getDifferences(firstDocument, NO_DOCS, thirdDocument);
    DifferenceHelper.dumpDifferences(differences, tempFile.getAbsolutePath());
    List<String> readDifferences = DifferenceHelper.readDifferenceDump(tempFile);
    assertTrue(differences.size() > 0);
    for (int i = 0; i < differences.size(); i++) {
      assertTrue(readDifferences.get(i).endsWith(
          differences.get(i).getTestNodeDetail().getXpathLocation() + " ---"));
    }
  }

  public void testAttributeOrderIsSame() throws ParserConfigurationException, IOException,
      TransformerFactoryConfigurationError, TransformerException, SAXException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    String document =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rootElement><tag a=\"valuea\" b=\"valueb\"/></rootElement>";
    Document first = builder.parse(new ByteArrayInputStream(document.getBytes()));

    String document2 =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rootElement><tag b=\"valueb\" a=\"valuea\"/></rootElement>";
    Document second = builder.parse(new ByteArrayInputStream(document2.getBytes()));

    assertTrue(DifferenceHelper.getDifferences(first, NO_DOCS, second).isEmpty());
  }

}
