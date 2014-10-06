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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * A class fetching pages and DOMs form URLs, abstracting the interaction with the underlying
 * Webdriver instances. It is meant to be used to fetch URLs using a pair of infected and a "clean",
 * uninfected machines. It uses an Internet Explorer driver.
 */
public class WebdriverWorker {
  private final WebDriver infectedDriver;
  private final WebDriver cleanDriver;
  private final WebDriver cleanTestDriver;

  @VisibleForTesting
  WebdriverWorker(RemoteWebDriver infectedDriver, RemoteWebDriver clearDriver,
      RemoteWebDriver cleanTestDriver) {
    this.infectedDriver = Preconditions.checkNotNull(infectedDriver);
    this.cleanDriver = Preconditions.checkNotNull(clearDriver);
    this.cleanTestDriver = Preconditions.checkNotNull(cleanTestDriver);
    infectedDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    cleanDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    cleanTestDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  /**
   * Creates a worker given the two fully qualified references to the webdriver instances. It builds
   * the webdriver instances once for all the tests.
   * 
   * @throws MalformedURLException if one of the provided URLs is malformed.
   */
  @VisibleForTesting
  public WebdriverWorker(String infectedWebdriverUrl, String clearWebdriverUrl,
      String clearTestWebdriverUrl) throws MalformedURLException {
    this(
        new RemoteWebDriver(new URL(infectedWebdriverUrl), DesiredCapabilities.internetExplorer()),
        new RemoteWebDriver(new URL(clearWebdriverUrl), DesiredCapabilities.internetExplorer()),
        new RemoteWebDriver(new URL(clearTestWebdriverUrl), DesiredCapabilities.internetExplorer()));
  }

  public void cleanGoTo(String url) {
    cleanDriver.get(url);
  }

  /**
   * Close the current window of each driver, quitting the browser if it's the last window currently
   * open.
   */
  public void closeDrivers() {
    cleanDriver.close();
    infectedDriver.close();
  }

  public Object executeJavascriptOnClean(String javascript) {
    JavascriptExecutor js = (JavascriptExecutor) cleanDriver;
    return js.executeScript(javascript);
  }

  /**
   * Executes a javascript snippet on the infected machine. Note that to return a value you need to
   * return it from the javascript.
   */
  public Object executeJavascriptOnInfected(String javascript) {
    JavascriptExecutor js = (JavascriptExecutor) infectedDriver;
    return js.executeScript(javascript);
  }

  /**
   * Returns a Document representation of the page from the clean webdriver.
   */
  public Document getCleanDocument(String url) {
    String page = getCleanPage(url);
    return WebdriverHelper.getDom(page);
  }

  /**
   * Returns the HTML code of the page as it should appear without trojans.
   */
  private String getCleanPage(String url) {
    Preconditions.checkNotNull(url);
    return getPage(url, cleanDriver);
  }

  /**
   * Returns a Document representation of the page from the clean webdriver used for testing.
   */
  Document getCleanTestDocument(String url) {
    String page = getCleanTestPage(url);
    return WebdriverHelper.getDom(page);
  }

  /**
   * Returns the HTML code of the clear page used for testing.
   */
  private String getCleanTestPage(String url) {
    Preconditions.checkNotNull(url);
    return getPage(url, cleanTestDriver);
  }

  /**
   * Returns a Document representation of the page from the infected webdriver.
   */
  public Document getInfectedDocument(String url) {
    String page = getInfectedPage(url);
    return WebdriverHelper.getDom(page);
  }

  /**
   * Returns the HTML code of the page at the provided {@code url}.
   */
  private String getInfectedPage(String url) {
    Preconditions.checkNotNull(url);
    return getPage(url, infectedDriver);
  }

  private String getPage(String url, WebDriver driver) {
    driver.get(url);
    String pageSource = driver.getPageSource();
    return pageSource;
  }

  /**
   * Navigates the infected webdriver to the given url
   */
  public void infectedGoTo(String url) {
    infectedDriver.get(url);
  }

  /**
   * Quit the drivers, closing every associated window.
   */
  public void quitDrivers() {
    cleanDriver.quit();
    infectedDriver.quit();
  }

  Document serialize(String url) {
    String page = getCleanTestPage(url);
    return WebdriverHelper.getDom(page);
  }
}
