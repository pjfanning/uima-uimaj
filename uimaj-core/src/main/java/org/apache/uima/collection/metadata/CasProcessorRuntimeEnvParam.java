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

package org.apache.uima.collection.metadata;

import org.apache.uima.resource.metadata.MetaDataObject;

/**
 * An object that holds configuration that is part of the CPE descriptor. It provides the means of
 * configuring environment variables used when launching local CasProcessors.
 * 
 * 
 */
public interface CasProcessorRuntimeEnvParam extends MetaDataObject {
  /**
   * Sets a name for new parameter
   * 
   * @param aEnvParamName -
   *          param name
   * @throws CpeDescriptorException tbd
   */
  void setEnvParamName(String aEnvParamName) throws CpeDescriptorException;

  /**
   * Returns a name of parameter
   * 
   * @return - parm name
   * @throws CpeDescriptorException tbd
   */
  String getEnvParamName() throws CpeDescriptorException;

  /**
   * Sets a value for new parameter
   * 
   * @param aEnvParamValue -
   *          param value
   * @throws CpeDescriptorException tbd
   */
  void setEnvParamValue(String aEnvParamValue) throws CpeDescriptorException;

  /**
   * Returns parameter value
   * 
   * @return - param value
   * @throws CpeDescriptorException tbd
   */
  String getEnvParamValue() throws CpeDescriptorException;
}
