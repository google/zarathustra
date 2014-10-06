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


package org.polimi.zarathustra.malleus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;

import com.google.gson.stream.JsonWriter;

/**
 * A class to serialize and store Difference(s) in JSON to a file.
 */
public class RulesJsonSerializer {

  JsonWriter writer;

  public RulesJsonSerializer(String fileName) {
    try {
      writer = new JsonWriter(new FileWriter(fileName));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void printDifferencesToFile(List<Difference> differences) throws IOException {
    writer.beginObject();

    writer.name("differences");
    writer.beginArray();
    for (Difference difference : differences) {
      writer.beginObject();
      writer.name("id").value(difference.getId());

      writer.name("control_node");
      writer.beginObject();
      writer.name("control_node_parent").value(getParentName(difference.getControlNodeDetail()));
      writer.name("control_node_value").value(getNodeValue(difference.getControlNodeDetail()));
      writer.name("control_node_xpath").value(getNodeXpath(difference.getControlNodeDetail()));
      writer.endObject();

      writer.name("test_node");
      writer.beginObject();
      writer.name("test_node_parent").value(getParentName(difference.getTestNodeDetail()));
      writer.name("test_node_value").value(getNodeValue(difference.getTestNodeDetail()));
      writer.name("test_node_xpath").value(getNodeXpath(difference.getTestNodeDetail()));
      writer.endObject();

      writer.endObject();
    }
    writer.endArray();

    writer.endObject();
    writer.close();
  }

  private String getParentName(NodeDetail nodeDetail) {
    try {
      return valueOrNull(nodeDetail.getNode().getParentNode().getNodeName());
    } catch (NullPointerException e) {
      return "null";
    }
  }

  private String getNodeXpath(NodeDetail nodeDetail) {
    try {
      return valueOrNull(nodeDetail.getXpathLocation());
    } catch (NullPointerException e) {
      return "null";
    }
  }

  private String getNodeValue(NodeDetail nodeDetail) {
    try {
      return valueOrNull(nodeDetail.getValue());
    } catch (NullPointerException e) {
      return "null";
    }
  }

  private String valueOrNull(String value) {
    return value == null ? "null" : value;
  }
}
