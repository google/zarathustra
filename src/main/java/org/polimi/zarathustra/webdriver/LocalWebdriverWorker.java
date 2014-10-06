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

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

/**
 * A local webdriver worker to run a browser on the local machine.
 */
public final class LocalWebdriverWorker {
  private static final class MonitorThread extends Thread {
    private final long timeout;
    private boolean done = false;
    private final Runnable stopWorkerThread;

    private MonitorThread(int timeoutSeconds, Runnable stopWorkerThread) {
      timeout = timeoutSeconds * 1000;
      this.stopWorkerThread = stopWorkerThread;
    }

    public void done() {
      done = true;
    }

    @Override
    public void run() {
      long deadline = new Date().getTime() + timeout;
      while (true) {
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        if (done) {
          return;
        }
        if (new Date(deadline).after(new Date())) {
          stopWorkerThread.run();
          return;
        }
      }
    }
  }

  private static final int MAX_TIMEOUT_SECONDS = 120;
  private static final int DEFAULT_HARD_LIMIT_TIMEOUT_SECONDS = MAX_TIMEOUT_SECONDS + 5;
  private final int hardLimitTimeoutSeconds;
  private WebDriver driver;

  private final boolean useExplorer;

  public LocalWebdriverWorker() {
    this(false);
  }

  public LocalWebdriverWorker(boolean useExplorer) {
    this(useExplorer, DEFAULT_HARD_LIMIT_TIMEOUT_SECONDS);
  }

  @VisibleForTesting
  LocalWebdriverWorker(boolean useExplorer, int hardLimitTimeoutSeconds) {
    this.useExplorer = useExplorer;
    this.hardLimitTimeoutSeconds = hardLimitTimeoutSeconds;
    resetDriver();
  }

  /**
   * Returns a Document representation of the DOM at the provided url. Before navigating to the url,
   * all cookies are deleted.
   * 
   * @param url The full, absolute URL to fetch the DOM from.
   * @return A well formatted Document.
   */
  public Document getDocument(final String url) {
    MonitorThread monitor = new MonitorThread(hardLimitTimeoutSeconds, new Runnable() {
      @Override
      public void run() {
        driver.quit();
        throw new TimeoutException("Timedout while retrieving DOM for " + url);
      }
    });
    monitor.setDaemon(true);
    driver.manage().deleteAllCookies();
    driver.get(url);
    String page = driver.getPageSource();
    monitor.done();
    Preconditions.checkNotNull(url);
    return WebdriverHelper.getDom(page);
  }

  public Document getDocumentAndStoreSource(final String url, File outputDir, String fileName) {
    MonitorThread monitor = new MonitorThread(hardLimitTimeoutSeconds, new Runnable() {
      @Override
      public void run() {
        driver.quit();
        throw new TimeoutException("Timedout while retrieving DOM for " + url);
      }
    });
    monitor.setDaemon(true);
    driver.manage().deleteAllCookies();
    driver.get(url);
    // TODO(claudio): handle download errors, truncated pages, weird redirects (e.g. opendns)
    String page = driver.getPageSource();
    storeHtmlSource(outputDir, fileName, page);
    monitor.done();
    Preconditions.checkNotNull(url);
    return WebdriverHelper.getDom(page);
  }

  /**
   * Closes down the webdriver browser.
   */
  public void quit() {
    driver.quit();
  }

  public void storeHtmlSource(File outputDir, String fileName, String page) {
    try {
      // Write "page" to file in the default folder (html_sources) */
      File output = new File(outputDir + "/html_sources", fileName.replace(".dom", ".html"));
      String completeName = output.getAbsolutePath();

      BufferedWriter out = new BufferedWriter(new FileWriter(completeName));
      StringBuffer contents = new StringBuffer();
      contents.append(page);
      out.write(contents.toString());
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Resets and create a new browser instance.
   */
  public WebDriver resetDriver() {
    if (useExplorer) {
      DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
      caps.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
      caps.setCapability("unexpectedAlertBehaviour", "accept");
      caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
      driver = new InternetExplorerDriver(caps);
    } else {
      DesiredCapabilities caps = DesiredCapabilities.firefox();
      caps.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
      driver = new FirefoxDriver(caps);
    }
    driver.manage().timeouts().pageLoadTimeout(MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);

    // VERY IMPORTANT: wait some time after opening IE, or you'll run into issues.
    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    return driver;
  }
}
