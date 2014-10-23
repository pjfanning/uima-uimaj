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

package org.apache.uima.internal.util;

import java.util.Arrays;
import java.util.Random;

import org.apache.uima.internal.util.IntBitSet;
import org.apache.uima.internal.util.IntHashSet;
import org.apache.uima.internal.util.rb_trees.IntArrayRBT;

import junit.framework.TestCase;

public class PositiveIntSetTest extends TestCase {
   
  Random r = new Random();
  


  public void testBasic() {
    PositiveIntSet s = new PositiveIntSet();
    s.add(128);
    assertTrue(s.isBitSet);
    s.add(128128);
    assertFalse(s.isBitSet);
    
    IntListIterator it = s.getOrderedIterator();
    assertTrue(it.hasNext());
    assertEquals(128, it.next());
    assertTrue(it.hasNext());
    assertEquals(128128, it.next());
    assertFalse(it.hasNext());

    // test offset
    int bb = 300000;
    s = new PositiveIntSet();
    assertTrue(s.useOffset);
    s.add(bb);
    s.add(bb);
    s.add(bb+1);
    s.add(bb+2);
    
    assertEquals(3, s.size());
    assertTrue(s.isBitSet);
    assertTrue(s.useOffset);
    
    // test offset converting to hashset
    s.add(bb - 66);
    assertEquals(4, s.size());
    assertFalse(s.isBitSet);
    it = s.getOrderedIterator();
    assertEquals(bb-66, it.next());
    assertEquals(bb, it.next());
    assertEquals(bb+1, it.next());
    assertEquals(bb+2, it.next());

    
    bb = 67;
    s = new PositiveIntSet();
    assertTrue(s.useOffset);
    s.add(bb);
    s.add(bb);
    s.add(bb+1);
    s.add(bb+2);
    
    assertEquals(3, s.size());
    assertTrue(s.isBitSet);
    assertTrue(s.useOffset);
    // test offset converting to bitset with no offset    
    s.add(bb - 66);
    assertEquals(4, s.size());
    assertTrue(s.isBitSet);
    assertFalse(s.useOffset);
    it = s.getOrderedIterator();
    assertEquals(bb-66, it.next());
    assertEquals(bb, it.next());
    assertEquals(bb+1, it.next());
    assertEquals(bb+2, it.next());
    
    // test switch from hash set to bit set
    s.clear();  // keeps useOffset false
    s.add(767 - 64);  // makes the space used by bit set = 25 words
    
    for (int i = 1; i < 13; i++) {
      s.add(i);
//      System.out.println("i is " + i + ", isBitSet = " + s.isBitSet);
      assertTrue("i is " + i, (i < 12) ? (!s.isBitSet) : s.isBitSet);
    }
    
    it = s.getOrderedIterator();
    assertEquals(1,it.next());
    assertEquals(2,it.next());
    assertEquals(3,it.next());
    assertEquals(4,it.next());
    assertEquals(5,it.next());
    assertEquals(6,it.next());
    assertEquals(7,it.next());
    assertEquals(8,it.next());
    assertEquals(9,it.next());
    assertEquals(10,it.next());
    assertEquals(11,it.next());
    assertEquals(12,it.next());
    assertEquals(767-64,it.next());
    
    boolean reached = false;
    for (int i = 10; i < 5122; i = i <<1) {
      s.add(i);  // switches to hash set when i = 2560. == 1010 0000 0000  (>>5 = 101 0000 = 80 (decimal)
                 // hash set size for 19 entries = 19 * 3 = 57
                 // bit set size for 2560 = 80 words
                 // bit set size for prev i (1280 dec) = 101 0000 0000 (>>5 = 10 1000) = 40 words
//      if (!s.isBitSet) {
//        System.out.println("is Bit set? " + s.isBitSet + ", # of entries is " + s.size());
//      }
      reached = i >= 5120;
      assertTrue((!reached) ? s.isBitSet : !s.isBitSet);
    }
    assertTrue(reached);
  }
  
  public void testiterators() {
    PositiveIntSet s = new PositiveIntSet();
    int [] e = new int [] {123, 987, 789};
    int [] eOrdered = Arrays.copyOf(e, e.length);
    Arrays.sort(eOrdered);
    s.add(e);
    int[] r = s.toUnorderedIntArray();
    assertTrue(Arrays.equals(r, eOrdered));    
   
    s.clear(); 
    e[0] = 125;
    e[2] = 1500000;
    s.add(e);
    r = s.toUnorderedIntArray();
    assertFalse(Arrays.equals(r, e));
    assertFalse(s.isBitSet);
    r = s.toOrderedIntArray();
    assertTrue(Arrays.equals(r, e));
    
  }
  
  
}
