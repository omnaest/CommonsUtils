/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils.processor.cyclic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.processor.cyclic.CyclicHashMap;

public class CyclicHashMapTest
{
    private Map<String, String> cyclicHashMap = this.newCyclicHashMap();;

    @Test
    @Ignore
    public void testCyclicHashMap() throws Exception
    {
        Function<Integer, String> keyGenerator = ii -> "key" + ii;

        //put and get
        int numberOfKeys = 1000;
        for (int ii = 0; ii < numberOfKeys; ii++)
        {
            this.cyclicHashMap.put(keyGenerator.apply(ii), "value" + ii);
        }

        assertEquals(numberOfKeys, this.cyclicHashMap.size());
        assertFalse(this.cyclicHashMap.isEmpty());
        for (int ii = 0; ii < numberOfKeys; ii++)
        {
            assertEquals("value" + ii, this.cyclicHashMap.get(keyGenerator.apply(ii)));
        }
        assertEquals(IntStream.range(0, numberOfKeys)
                              .mapToObj(Integer::valueOf)
                              .map(keyGenerator)
                              .collect(Collectors.toSet()),
                     this.cyclicHashMap.keySet());

        //remove
        for (int ii = 0; ii < numberOfKeys; ii++)
        {
            this.cyclicHashMap.remove(keyGenerator.apply(ii));

        }
        assertTrue(this.cyclicHashMap.isEmpty());
        for (int ii = 0; ii < numberOfKeys; ii++)
        {
            assertNull(this.cyclicHashMap.get(keyGenerator.apply(ii)));
        }
    }

    private CyclicHashMap<String, String> newCyclicHashMap()
    {
        Cache cache = CacheUtils.newConcurrentInMemoryCache();
        return new CyclicHashMap<>(5, cache);
    }

}
