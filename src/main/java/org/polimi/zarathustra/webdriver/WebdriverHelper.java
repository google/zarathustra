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

package org.polimi.zarathustra.webdriver;

import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Collection of helper class for webdriver.
 */
public final class WebdriverHelper {
  /**
   * Returns the DOM representation of a page given its full HTML
   * representation.
   * 
   * @param page full dump of the HTML of the page.
   * @return the DOM corresponding to the provided page.
   */
  public static Document getDom(String page) {
    try {
      DOMResult result = new DOMResult();
      XMLReader reader = new Parser();
      reader.setFeature(Parser.namespacesFeature, true);
      reader.setFeature(Parser.namespacePrefixesFeature, true);
      // See: http://ccil.org/~cowan/XML/tagsoup/#properties
      reader.setFeature("http://www.ccil.org/~cowan/tagsoup/features/root-bogons", true);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new SAXSource(reader, new InputSource(new StringReader(page))), result);
      return (Document) result.getNode();
    } catch (SAXNotRecognizedException e) {
      throw new AssertionError(e);
    } catch (SAXNotSupportedException e) {
      throw new AssertionError(e);
    } catch (TransformerException e) {
      throw new AssertionError(e);
    }
  }

  private WebdriverHelper() {
  }

}
