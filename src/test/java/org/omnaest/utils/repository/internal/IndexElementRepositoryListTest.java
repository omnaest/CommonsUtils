package org.omnaest.utils.repository.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.omnaest.utils.repository.IndexElementRepository;

public class IndexElementRepositoryListTest
{
    private IndexElementRepository<String> elementRepository = IndexElementRepository.of(new HashMap<>());

    @Test
    public void test() throws Exception
    {
        List<String> list = this.elementRepository.asList();

        assertTrue(list.isEmpty());

        list.add("a");
        assertEquals(1, list.size());
        assertEquals("a", list.get(0));

        list.add("b");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));

        list.add("b");
        assertEquals(3, list.size());
        assertEquals("b", list.get(2));

        list.remove(0);
        assertEquals("b", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals(Arrays.asList("b", "b"), list);
    }

}
