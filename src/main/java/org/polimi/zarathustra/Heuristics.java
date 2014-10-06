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

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;

import com.google.common.base.Strings;

class Heuristics {

  private static boolean checkBlacklistedElement(NodeDetail nodeDetail, String[] blacklistedKeywords) {
    String value = nodeDetail.getValue();
    String xPath = nodeDetail.getXpathLocation();

    for (String keyword : blacklistedKeywords) {
      if (keyword.equalsIgnoreCase(value)) {
        return true;
      }

      if ((xPath != null) && xPath.endsWith(keyword)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Given a node and the name of an element, returns "true" its direct parent IS NOT an element
   * with that name
   */
  private static boolean checkNodeNotInParent(NodeDetail nodeDetail, String parentName) {
    String value;

    try {
      value = nodeDetail.getNode().getParentNode().getNodeName();
      if (value == null) {
        return true;
      } else {
        return !parentName.equalsIgnoreCase(value);
      }
    } catch (NullPointerException e) {
      return true;
    }
  }

  /**
   * Checks this heuristic: if a modified text is not in a script, consider it a false difference.
   * This difference was introduced after finding an injection on the content of an existing script.
   * Most of "modified text" differences are harmless and occur on span elements
   */
  static boolean differentTextNotInScript(Difference difference) {
    if (difference.getId() == 14) {
      NodeDetail controlNode = difference.getControlNodeDetail();
      NodeDetail testNode = difference.getTestNodeDetail();
      String parentName = "script";

      return checkNodeNotInParent(controlNode, parentName)
          || checkNodeNotInParent(testNode, parentName);
    }

    return false;
  }

  /**
   * Checks this heuristic: if the attribute that has been modified is the "value" attribute of an
   * "input" element, it is much likely to be a false difference
   */
  static boolean differentValueAttributeOnInput(Difference difference) {
    String controlNodeXpath = difference.getTestNodeDetail().getXpathLocation();

    if ((controlNodeXpath != null) && controlNodeXpath.endsWith("@value")
        && (difference.getId() == 3)) {
      String nodeName = "input";
      String[] elements = controlNodeXpath.split("/");
      String secondLastElement = elements[elements.length - 2];

      if ((secondLastElement != null) && secondLastElement.startsWith(nodeName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks this heuristic: if an added/deleted/edited attribute or an edited text have different
   * xpaths on the control and test nodes, consider them as a false difference. This is needed to
   * deal with the recurring behavior of the comparer according to which elements are erroneously
   * seen as moved and hence compared with completely different nodes on different xpaths.
   */
  static boolean editedElementOnDifferentXpath(Difference difference) {
    String firstXPath = Strings.nullToEmpty(difference.getControlNodeDetail().getXpathLocation());
    String secondXPath = Strings.nullToEmpty(difference.getTestNodeDetail().getXpathLocation());
    if (((difference.getId() == 2) || (difference.getId() == 3) || (difference.getId() == 14))
        && !firstXPath.equalsIgnoreCase(secondXPath)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks this heuristic: if a keyword that usually appears alongside a false difference is found
   * as the name (value) of an added element or as the last element in the xpath, the difference is
   * handled as false. This happens in particular with formatting elements.
   */
  static boolean onBlacklistedElement(Difference difference) {
    NodeDetail controlNode = difference.getControlNodeDetail();
    NodeDetail testNode = difference.getTestNodeDetail();

    String[] blacklistedKeywords =
        {"style", "class", "width", "height", "sizset", "sizcache", "alt"};

    return checkBlacklistedElement(controlNode, blacklistedKeywords)
        || checkBlacklistedElement(testNode, blacklistedKeywords);
  }

  private Heuristics() {}
}
