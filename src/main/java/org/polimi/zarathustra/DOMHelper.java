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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.w3c.dom.Document;

/**
 * A helper class to compare and manipulate DOM documents in Zarathustra.
 */
public class DOMHelper {

  public static final String DOM_DUMP_SUFFIX = ".dom";

  /**
   * Deserializes a Document from a file.
   */
  public static Document deserializeDocument(File savedDocument) throws IOException {
    return (Document) deserializeObject(savedDocument);
  }

  private static Object deserializeObject(File savedObject) throws IOException {
    InputStream file = new FileInputStream(savedObject);
    InputStream buffer = new BufferedInputStream(file);
    ObjectInput input = new ObjectInputStream(buffer);
    try {
      return input.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } finally {
      input.close();
    }
  }

  /**
   * Serializes a Document to a file.
   * 
   * @param doc to serialize.
   * @param filePath writable path to store the document in.
   */
  public static void serializeDocument(Document doc, String filePath) throws IOException {
    serializeObject(doc, filePath);
  }

  /**
   * Serializes an Object to a file.
   * 
   * @param doc to serialize.
   * @param filePath writable path to store the document in.
   */
  private static void serializeObject(Object object, String filePath) throws IOException {
    FileOutputStream fileOut = new FileOutputStream(filePath);
    ObjectOutputStream out = new ObjectOutputStream(fileOut);
    out.writeObject(object);
    out.close();
    fileOut.close();
  }

}
