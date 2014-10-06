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
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.TimeoutException;
import org.polimi.zarathustra.DOMHelper;
import org.polimi.zarathustra.webdriver.LocalWebdriverWorker;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * This experiment dumps the DOMs from a set of URLs to files in a directory. It
 * also creates a manifest.txt file in the directory.
 */
public class DOMsDumpExperiment {

  private static String getFileName(String url) throws NoSuchAlgorithmException {
    String domain = URI.create(url).getHost();
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    md5.update(url.getBytes(), 0, url.length());
    String urlMd5 = new BigInteger(1, md5.digest()).toString(16);
    return String.format("%s.%s%s", domain, urlMd5, DOMHelper.DOM_DUMP_SUFFIX);
  }

  /**
   * Dumps the DOMs from a set of URLs to a directory. Sample invocation: java
   * -jar domdump.jar urllist.txt /tmp/output. urllist.txt is expected to have
   * one URL per line, without an empty final line.
   * <p>
   * NOTE: for testing purposes, this can be run with an extra argument,
   * FIREFOX: if so, it runs a firefox webdriver instead of explorer.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Invoke this experiment with 2 parameters:" + "urlFileList and outputDir");
      System.exit(-1);
    }
    paramsAreOkOrDie(args);
    File inputFile = new File(args[0]);
    File outputDir = new File(args[1]);

    LocalWebdriverWorker worker;
    if ((args.length > 2) && (args[2].equals("FIREFOX"))) {
      System.out.println("Running in FIREFOX mode, for testing only");
      worker = new LocalWebdriverWorker(false);
    } else {
      worker = new LocalWebdriverWorker(true);
    }
    

    List<String> urls = Files.readLines(inputFile, Charsets.UTF_8);
    urls = removeComments(urls);
    File manifest = new File(outputDir, "manifest.txt");
    Files.append("Start capture. Version 1.0\n", manifest, Charsets.UTF_8);
    for (String url : urls) {
      try {
        storeDOM(url, outputDir, worker);
        storeDOMManifest(url, outputDir, new Date().getTime());
        updateManifest(url, new Date().getTime(), manifest);
      } catch (TimeoutException e2) {
        System.out.println("WebDriver Timeout: " + e2.toString());
      } catch (org.openqa.selenium.UnhandledAlertException e1) {
        System.out.println(url + " --- UnhandledAlertException caught" + e1.toString());
      } catch (Exception e) {
          System.out.println("Generic exception caught: " + e.toString());
      }

    }
    worker.quit();
  }

  private static void paramsAreOkOrDie(String[] args) {
    File inputFile = new File(args[0]);
    File outputDir = new File(args[1]);
    if (!inputFile.canRead()) {
      System.err.println("Cannot read input file");
      System.exit(1);
    }
    if (!outputDir.canWrite()) {
      System.err.println("Cannot write to output dir");
      System.exit(1);
    }
    if (!outputDir.isDirectory()) {
      System.err.println("Cannot find output dir");
      System.exit(1);
    }
  }

  static List<String> removeComments(List<String> urls) {
    List<String> selectedLines = new ArrayList<String>();
    for (String url : urls) {
      if (url.charAt(0) != '#') {
        selectedLines.add(url);
      }
    }
    return selectedLines;
  }

  /**
   * Saves the DOM at the provided URL to the given outptuDir.
   */
  @VisibleForTesting
  static void storeDOM(String url, File outputDir, LocalWebdriverWorker worker) throws IOException,
    NoSuchAlgorithmException, TimeoutException {
    String fileName = getFileName(url);
    Document dom = worker.getDocumentAndStoreSource(url, outputDir, fileName);
    File output = new File(outputDir, fileName);
    DOMHelper.serializeDocument(dom, output.getAbsolutePath());
  }

  private static void storeDOMManifest(String url, File outputDir, long time)
      throws NoSuchAlgorithmException, IOException {
    String fileName = getFileName(url) + ".manifest";
    File output = new File(outputDir, fileName);
    Files.write(url + ", " + time, output, Charsets.UTF_8);
  }

  /**
   * Updates the manifest file adding a track of the current request
   */
  private static void updateManifest(String url, Long timestamp, File manifest) throws IOException {
    String trace = String.format("%s, %s\n", url, timestamp.toString());
    Files.append(trace, manifest, Charsets.UTF_8);
  }
}