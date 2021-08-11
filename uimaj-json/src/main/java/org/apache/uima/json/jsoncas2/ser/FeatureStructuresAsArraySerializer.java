/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.uima.json.jsoncas2.ser;

import java.io.IOException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.json.jsoncas2.model.FeatureStructures;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class FeatureStructuresAsArraySerializer extends StdSerializer<FeatureStructures> {
  private static final long serialVersionUID = 4848917731920209133L;

  public FeatureStructuresAsArraySerializer() {
    super(FeatureStructures.class);
  }

  @Override
  public void serialize(FeatureStructures aFeatureStructures, JsonGenerator jg,
          SerializerProvider aProvider) throws IOException {
    if (aFeatureStructures.isEmpty()) {
      jg.writeStartArray();
      jg.writeEndArray();
      return;
    }

    Map<FeatureStructure, Set<String>> fsToViewIndex = buildFsToViewIndex(aFeatureStructures);

    jg.writeStartArray();

    for (FeatureStructure fs : aFeatureStructures) {
      aProvider.defaultSerializeValue(fs, jg);
    }
    jg.writeEndArray();
  }

  private Map<FeatureStructure, Set<String>> buildFsToViewIndex(
          FeatureStructures aFeatureStructures) {
    Map<FeatureStructure, Set<String>> fsToViewsCache = new IdentityHashMap<>();
    aFeatureStructures.iterator().next().getCAS().getViewIterator().forEachRemaining(view -> {
      for (FeatureStructure fs : view.select()) {
        fsToViewsCache.computeIfAbsent(fs, _fs -> new HashSet<>()).add(view.getViewName());
      }
    });
    return fsToViewsCache;
  }
}