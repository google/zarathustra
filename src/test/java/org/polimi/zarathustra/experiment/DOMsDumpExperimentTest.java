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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

import org.openqa.selenium.net.UrlChecker.TimeoutException;
import org.polimi.zarathustra.DOMHelper;
import org.polimi.zarathustra.DifferenceHelper;
import org.polimi.zarathustra.webdriver.LocalWebdriverWorker;
import org.w3c.dom.Document;

import com.google.common.io.Files;

/**
 * Tests for the DOM dumping experiment of Zarathustra.
 */
public class DOMsDumpExperimentTest extends TestCase {
  private static final String TEST_URL = "http://www.google.com";

  public void testBaseDOMDump() throws NoSuchAlgorithmException, IOException, TimeoutException {
    File tempDir = Files.createTempDir();
    // We use Firefox for the test.
    LocalWebdriverWorker worker = new LocalWebdriverWorker(false);
    DOMsDumpExperiment.storeDOM(TEST_URL, tempDir, worker);
    Document document = worker.getDocument(TEST_URL);
    DifferenceHelper.domIsEqual(document, DOMHelper.deserializeDocument(tempDir.listFiles()[0]));
    worker.quit();
  }
}
