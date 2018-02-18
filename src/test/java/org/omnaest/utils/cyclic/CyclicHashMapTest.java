package org.omnaest.utils.cyclic;

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
