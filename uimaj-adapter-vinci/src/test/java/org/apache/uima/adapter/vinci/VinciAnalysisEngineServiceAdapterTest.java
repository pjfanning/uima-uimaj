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
package org.apache.uima.adapter.vinci;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.apache.uima.Constants;
import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.URISpecifier;
import org.junit.jupiter.api.Test;

public class VinciAnalysisEngineServiceAdapterTest {

  /**
   * Test initialize.
   *
   * @throws ResourceInitializationException
   *           the resource initialization exception
   */
  @Test
  public void testInitialize() throws ResourceInitializationException {
    // Don't want an actual network dependency so will test only with services that
    // don't exist. The tests just make sure that the AnalysisEngineServiceAdapter.initialize
    // method just returns false when passed an inappropriate specifier and throws an
    // exception when passed an appropriate specifier.

    final String NON_EXISTENT_URI = "this.service.does.not.exit.at.least.i.hope.not";
    URISpecifier uriSpec = UIMAFramework.getResourceSpecifierFactory().createURISpecifier();
    uriSpec.setUri(NON_EXISTENT_URI);

    // test incorrect protocol
    uriSpec.setProtocol("BAD_PROTOCOL");
    VinciAnalysisEngineServiceAdapter adapter = new VinciAnalysisEngineServiceAdapter();
    boolean result = adapter.initialize(uriSpec, null);
    assertThat(result).isFalse();

    // test correct protocol
    VinciAnalysisEngineServiceAdapter adapter2 = new VinciAnalysisEngineServiceAdapter();
    uriSpec.setProtocol(Constants.PROTOCOL_VINCI);
    uriSpec.setResourceType(URISpecifier.RESOURCE_TYPE_ANALYSIS_ENGINE);

    assertThatExceptionOfType(ResourceInitializationException.class)
            .isThrownBy(() -> adapter2.initialize(uriSpec, null));

    // test correct protocol and no component type
    uriSpec.setResourceType(null);
    VinciAnalysisEngineServiceAdapter adapter1 = new VinciAnalysisEngineServiceAdapter();
    assertThatExceptionOfType(ResourceInitializationException.class)
            .isThrownBy(() -> adapter1.initialize(uriSpec, null));
  }
}
