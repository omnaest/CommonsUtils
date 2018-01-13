package org.omnaest.utils.repository.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.omnaest.utils.repository.ElementRepository;

/**
 * @see BiElementRepositoryMap
 * @author omnaest
 */
public class BiElementRepositoryMapTest
{
    private ElementRepositoryMap<String, String> repositoryMap = new BiElementRepositoryMap<>(ElementRepository.of(new LinkedHashMap<>()),
                                                                                              ElementRepository.of(new LinkedHashMap<>()));

    @Test
    public void test()
    {
        //
        for (int ii = 0; ii < 10000; ii++)
        {
            this.repositoryMap.put("key" + ii, "value" + ii);
            assertEquals("value" + ii, this.repositoryMap.get("key" + ii));
        }

        assertEquals(10000, this.repositoryMap.size());
        assertEquals(10000, this.repositoryMap.keySet()
                                              .size());
        assertEquals(10000, this.repositoryMap.values()
                                              .size());
        assertFalse(this.repositoryMap.isEmpty());

        //
        assertNotNull(this.repositoryMap.remove("key0"));
        assertEquals(10000 - 1, this.repositoryMap.size());

        //
        this.repositoryMap.clear();
        assertTrue(this.repositoryMap.isEmpty());

    }

    @Test
    public void testCopyInto()
    {
        for (int ii = 0; ii < 100; ii++)
        {
            this.repositoryMap.put("key" + ii, "value" + ii);
        }

        SortedMap<String, String> sortedMap = this.repositoryMap.copyInto(new TreeMap<>());
        assertEquals(100, sortedMap.size());
    }
}
