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
package org.apache.uima.cas.text;

import java.util.function.IntPredicate;

import org.apache.uima.cas.AnnotationBaseFS;

/**
 * Interface for Annotation Feature Structures.
 */
public interface AnnotationFS extends AnnotationBaseFS {

  /**
   * Get the start position of the annotation as character offset into the text. The smallest
   * possible start position is <code>0</code>, the offset of the first character in the text.
   * 
   * @return The start position.
   */
  int getBegin();

  /**
   * Get the end position of the annotation as character offset into the text. The end position
   * points at the first character after the annotation, such that
   * <code>(getEnd()-getBegin()) == getCoveredText().length()</code>.
   * 
   * @return The end position.
   */
  int getEnd();

  /**
   * Set the start position of the annotation as character offset into the text. The smallest
   * possible start position is <code>0</code>, the offset of the first character in the text.
   * 
   * @param begin The start position.
   */
  void setBegin(int begin);

  /**
   * Set the end position of the annotation as character offset into the text. The end position
   * points at the first character after the annotation, such that
   * <code>(getEnd()-getBegin()) == getCoveredText().length()</code>.
   * 
   * @param end The end position position.
   */
  void setEnd(int end);

  /**
   * Get the text covered by an annotation as a string. If <code>docText</code> is your document
   * text and <code>annot</code> an annotation, then <code>
   * annot.getCoveredText().equals(docText.substring(annot.getBegin(), 
   * annot.getEnd()))</code>.
   * 
   * @return String
   */
  String getCoveredText();

  /**
   * Strips leading and trailing whitespace by increasing/decreasing the begin/end offsets. This 
   * method is aware of Unicode codepoints. It expects that the begin/end offsets point to valid
   * codepoints.
   */
  default void trim() {
      trim(Character::isWhitespace);
  }
  
  /**
   * Strips leading and trailing characters matching the given predicate by increasing/decreasing 
   * the begin/end offsets.
   * 
   * @see #trim()
   * @param aPredicate the predicate used to identify  whitespace
   */
  void trim(IntPredicate aPredicate);
  
  /**
   * @see AnnotationPredicates#covers(AnnotationFS, AnnotationFS)
   */
  default boolean covers(int aBegin, int aEnd)
  {
    return AnnotationPredicates.covers(this, aBegin, aEnd);
  }
  
  /**
   * @see AnnotationPredicates#covers(AnnotationFS, AnnotationFS)
   */
  default boolean covers(AnnotationFS aOther)
  {
    return AnnotationPredicates.covers(this, aOther);
  }
  
  /**
   * @see AnnotationPredicates#coveredBy(AnnotationFS, AnnotationFS)
   */
  default boolean coveredBy(int aBegin, int aEnd)
  {
    return AnnotationPredicates.coveredBy(this, aBegin, aEnd);
  }
  
  /**
   * @see AnnotationPredicates#coveredBy(AnnotationFS, AnnotationFS)
   */
  default boolean coveredBy(AnnotationFS aOther)
  {
    return AnnotationPredicates.coveredBy(this, aOther);
  }
  
  /**
   * @see AnnotationPredicates#overlaps(AnnotationFS, AnnotationFS)
   */
  default boolean overlaps(int aBegin, int aEnd)
  {
    return AnnotationPredicates.overlaps(this, aBegin, aEnd);
  }

  /**
   * @see AnnotationPredicates#overlaps(AnnotationFS, AnnotationFS)
   */
  default boolean overlaps(AnnotationFS aOther)
  {
    return AnnotationPredicates.overlaps(this, aOther);
  }

  /**
   * @see AnnotationPredicates#overlapsLeft(AnnotationFS, AnnotationFS)
   */
  default boolean overlapsLeft(int aBegin, int aEnd)
  {
    return AnnotationPredicates.overlapsLeft(this, aBegin, aEnd);
  }

  /**
   * @see AnnotationPredicates#overlapsLeft(AnnotationFS, AnnotationFS)
   */
  default boolean overlapsLeft(AnnotationFS aOther)
  {
    return AnnotationPredicates.overlapsLeft(this, aOther);
  }

  /**
   * @see AnnotationPredicates#overlapsRight(AnnotationFS, AnnotationFS)
   */
  default boolean overlapsRight(int aBegin, int aEnd)
  {
    return AnnotationPredicates.overlapsRight(this, aBegin, aEnd);
  }

  /**
   * @see AnnotationPredicates#overlapsRight(AnnotationFS, AnnotationFS)
   */
  default boolean overlapsRight(AnnotationFS aOther)
  {
    return AnnotationPredicates.overlapsRight(this, aOther);
  }

  /**
   * @see AnnotationPredicates#rightOf(AnnotationFS, AnnotationFS)
   */
  default boolean rightOf(int aBegin, int aEnd)
  {
    return AnnotationPredicates.rightOf(this, aBegin, aEnd);
  }

  /**
   * @see AnnotationPredicates#rightOf(AnnotationFS, AnnotationFS)
   */
  default boolean rightOf(AnnotationFS aOther)
  {
    return AnnotationPredicates.rightOf(this, aOther);
  }

  /**
   * @see AnnotationPredicates#leftOf(AnnotationFS, AnnotationFS)
   */
  default boolean leftOf(int aBegin, int aEnd)
  {
    return AnnotationPredicates.leftOf(this, aBegin, aEnd);
  }

  /**
   * @see AnnotationPredicates#leftOf(AnnotationFS, AnnotationFS)
   */
  default boolean leftOf(AnnotationFS aOther)
  {
    return AnnotationPredicates.leftOf(this, aOther);
  }
}
