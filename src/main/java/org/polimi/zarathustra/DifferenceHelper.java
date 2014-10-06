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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.polimi.zarathustra.malleus.RulesJsonSerializer;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * An helper class to compare and manipulate Differences in Zarathustra.
 */
public final class DifferenceHelper {

  /**
   * Only returns the differences in {@code differences} that are not present in {@code whitelist}.
   * Uses the XPath of the Difference to determine equality.
   */
  private static List<Difference> differencesDiff(List<Difference> whitelist,
      List<Difference> differences) {
    List<Difference> uniqueDifferences = Lists.newArrayList();
    for (Difference difference : differences) {
      if ((difference.getId() == 2) || (difference.getId() == 3) || (difference.getId() == 14)
          || (difference.getId() == 22)) {
        if (!isFalseDifference(difference, whitelist)) {
          uniqueDifferences.add(difference);
        }
      }
    }

    return uniqueDifferences;
  }

  /**
   * Returns true if the two documents are functionally identical, i.e. they don't differ in
   * anything but comments, ordering of attributes, comments and so on.
   */
  public static boolean domIsEqual(Document first, Document second) {
    setupXmlUnit();
    Diff diff = XMLUnit.compareXML(first, second);
    return diff.identical();
  }

  /**
   * Compares two Documents stored into files.
   * 
   * @param firstDocument First file containing a Document.
   * @param secondDocument Second file containing a Document.
   * @return true if the two serialized representations are equal, false otherwise.
   * @throws IOException If an error occurs opening the files or loading the class.
   */
  public static boolean domIsEqual(File firstDocument, File secondDocument) throws IOException {
    Document first = DOMHelper.deserializeDocument(firstDocument);
    Document second = DOMHelper.deserializeDocument(secondDocument);
    return domIsEqual(first, second);
  }

  /**
   * Saves a text representation of the differences in a file.
   */
  public static void dumpDifferences(List<Difference> differences, String filePath)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    RulesJsonSerializer serializer = new RulesJsonSerializer(filePath + ".json");

    for (Difference difference : differences) {
      sb.append(
          "--- Difference " + difference.getId() + " at "
              + difference.getTestNodeDetail().getXpathLocation() + " ---\n"
              + difference.toString()).append("\n\n");
    }
    File dumpFile = new File(filePath);
    Files.write(sb.toString(), dumpFile, Charsets.UTF_8);
    serializer.printDifferencesToFile(differences);
  }

  @SuppressWarnings("unchecked")
  private static List<Difference> getDifferences(Document base, List<Document> targets) {
    base.normalizeDocument();
    List<Difference> differences = Lists.newArrayList();
    for (Document target : targets) {
      target.normalizeDocument();
      DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(base, target));
      differences.addAll(myDiff.getAllDifferences());
    }
    return differences;
  }

  /**
   * Performs comparison between two or more dumps of the same page.
   * 
   * @param base the base document to compare
   * @param verifications a list of documents to use as a baseline. All differences between these
   *        documents and the base document will be ignored with the target.
   * @param target a target document to compare with the base
   */
  public static List<Difference> getDifferences(Document base, List<Document> verifications,
      Document target) throws IOException {
    setupXmlUnit();
    // Gets the differences between the various "base" reference docs.
    List<Difference> whitelist = getDifferences(base, verifications);
    // Generates the differences between the base and the target.
    List<Difference> targetDifferences = getDifferences(base, ImmutableList.of(target));
    // Filters out all the differences that were observed in the base reference.
    return differencesDiff(whitelist, targetDifferences);
  }

  public static List<Difference> getDifferences(File firstDocument, File secondDocument)
      throws IOException {
    return getDifferences(firstDocument, ImmutableList.<File>of(), secondDocument);
  }

  /**
   * Performs comparison between three or more dumps of the same page. Two are the base dump and the
   * actual target, while the others are more baseline dumps. Only differences that are not already
   * present in the base dumps will be reported.
   */
  public static List<Difference> getDifferences(File baseDocument,
      List<File> verificationDocuments, File targetDocument) throws IOException {
    Document base = DOMHelper.deserializeDocument(baseDocument);
    List<Document> verifications = Lists.newArrayList();

    for (File verificationDocument : verificationDocuments) {
      verifications.add(DOMHelper.deserializeDocument(verificationDocument));
    }

    Document target = DOMHelper.deserializeDocument(targetDocument);
    return getDifferences(base, verifications, target);
  }

  /**
   * Compares a Difference with a list of Differences returning true if the Difference is in the
   * list. The comparison is based on both test and control nodes' xpaths.
   */
  private static boolean isFalseDifference(Difference difference, List<Difference> whitelist) {
    String diffXpathOnControlNode =
        Strings.nullToEmpty(difference.getControlNodeDetail().getXpathLocation());
    String diffXpathOnTestNode =
        Strings.nullToEmpty(difference.getTestNodeDetail().getXpathLocation());
    String falsePositiveXPathOnControlNode;
    String falsePositiveXPathOnTestNode;

    if (matchesFalsePositiveHeuristics(difference)) {
      return true;
    }

    for (Difference whitelistedDifference : whitelist) {
      falsePositiveXPathOnControlNode =
          Strings.nullToEmpty(whitelistedDifference.getControlNodeDetail().getXpathLocation());
      falsePositiveXPathOnTestNode =
          Strings.nullToEmpty(whitelistedDifference.getTestNodeDetail().getXpathLocation());

      if (diffXpathOnTestNode.equalsIgnoreCase(falsePositiveXPathOnTestNode)
          && diffXpathOnControlNode.equalsIgnoreCase(falsePositiveXPathOnControlNode)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns a boolean value resulting from the checks of all heuristics. If at least one heuristic
   * holds, the value true is returned and the difference is handles as a false one.
   */
  private static boolean matchesFalsePositiveHeuristics(Difference difference) {
    return Heuristics.onBlacklistedElement(difference)
        || Heuristics.editedElementOnDifferentXpath(difference)
        || Heuristics.differentTextNotInScript(difference)
        || Heuristics.differentValueAttributeOnInput(difference);
  }

  /** Reads the dump of differences in a file, returning the JSON encoded differences. */
  public static List<String> readDifferenceDump(File savedDifferences) throws IOException {
    // TODO(claudio): decode the differences.
    return Files.readLines(savedDifferences, Charsets.UTF_8);
  }

  private static final void setupXmlUnit() {
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreAttributeOrder(true);
    XMLUnit.setCompareUnmatched(true);
  }

}
