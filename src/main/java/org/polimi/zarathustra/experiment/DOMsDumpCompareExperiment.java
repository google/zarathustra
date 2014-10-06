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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.polimi.zarathustra.DOMHelper;
import org.polimi.zarathustra.DifferenceHelper;

import com.google.common.collect.Lists;

/**
 * This experiment compares the contents of 3 or more directories. All are expected to
 * contain dumps of the same domains: 2 are the "reference" directory and the "target" directory,
 * the others (one or more) are additional "reference" directories. 
 * Only differences that occur between "reference" and "target", but NOT between "reference"
 * directories, are reported.
 */
public class DOMsDumpCompareExperiment {

  /**
   * Compares two doms contained in files with the same name in three dirs.
   * Stores the output in another dir.
   *
   * @param filename name of the file to compare.
   * @param sourceDir1 base source directory.
   * @param targetDir directory holding targets
   * @param outputDir output directory.
   * @param sourceDirs2 verification source directories.
   * @throws IOException if an error occurs while saving the differences.
   */
  private static void compareAndStoreDom(String filename, File sourceDir1, File targetDir,
		  File outputDir, List<File> verificationDirs) throws IOException {
    try {
      List<Difference> differences = compareDomDump(filename, sourceDir1, targetDir, verificationDirs);
      if (!differences.isEmpty()) {
        DifferenceHelper.dumpDifferences(differences,
            new File(outputDir, filename).getAbsolutePath());
      }
    } catch (FileNotFoundException e) {
      System.err.println(String.format("%s : matching file not found", filename));
    }
  }

  private static List<Difference> compareDomDump(String filename, File baseDir1, File targetDir,
		  List<File> baseDirs) throws FileNotFoundException {
    File baseFile = verifyFileExists(new File(baseDir1, filename));
    List<File> verificationFiles = Lists.newArrayList();
    File targetFile = verifyFileExists(new File(targetDir, filename));
    
    for (File dir : baseDirs) {
    	try {
    		verificationFiles.add(verifyFileExists(new File(dir, filename)));
    	} catch (FileNotFoundException e) {
    	}
    }

    try {
      if (DifferenceHelper.domIsEqual(baseFile, targetFile)) {
        return Lists.newArrayList();
      } else {
        return DifferenceHelper.getDifferences(baseFile, verificationFiles, targetFile);
      }
    } catch (IOException e) {
      System.err.println(String.format("Error while comparing %s and a verification file: %s", baseFile, e));
      return Lists.newArrayList();
    }
  }

  /**
   * Compares the DOMs found in dumps in two directories.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.err.println("Invoke this experiment with at least 3 parameters" +
      		"and optionally one or more verification dir(s):"
          + "sourceDir targetDir outputDir verificationDir(s)");
    }

    File sourceDir1 = verifyReadableDir(new File(args[0]));
    File targetDir = verifyReadableDir(new File(args[1]));
    File outputDir = verifyWritableDir(new File(args[2]));
    List<File> verificationDirs = Lists.newArrayList();
    
    for (int i = 3; i < args.length; i++) {
    	verificationDirs.add(verifyReadableDir(new File(args[i])));
    }

    for (String filename : sourceDir1.list()) {
      if (filename.endsWith(DOMHelper.DOM_DUMP_SUFFIX)) {
        compareAndStoreDom(filename, sourceDir1, targetDir, outputDir, verificationDirs);
      }
    }
  }

  private static File verifyFileExists(File file) throws FileNotFoundException {
    if (!file.exists()) {
      String errMsg = "ERROR: " + file.getAbsolutePath() + " does not exist!";
      System.err.println(errMsg);
      throw new FileNotFoundException(errMsg);
    } else {
      return file;
    }
  }

  private static File verifyReadableDir(File file) {
    if (!file.isDirectory() || !file.canRead()) {
      String msg = "Cannot read " + file.getAbsolutePath();
      System.err.println(msg);
      throw new RuntimeException(msg);
    }
    return file;
  }

  private static File verifyWritableDir(File file) {
    if (!file.isDirectory() || !file.canWrite()) {
      String msg = "Cannot write into " + file.getAbsolutePath();
      System.err.println(msg);
      throw new RuntimeException(msg);
    }
    return file;
  }
}
