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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.polimi.zarathustra.experiment.DOMsDumpCompareExperimentTest;
import org.polimi.zarathustra.experiment.DOMsDumpExperimentTest;

/**
 * Tests for Zarathustra (Unit Tests only)
 */
public class ZarathustraUnitTests extends TestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(DifferenceHelperTest.class);
    suite.addTestSuite(DOMsDumpCompareExperimentTest.class);
    suite.addTestSuite(DOMsDumpExperimentTest.class);
    suite.addTestSuite(DOMHelperTest.class);
    suite.addTestSuite(DOMVisualizerTest.class);
    return suite;
  }
}
