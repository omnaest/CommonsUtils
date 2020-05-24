package org.omnaest.utils.repository.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.junit.Test;
import org.omnaest.utils.repository.IndexElementRepository;
import org.omnaest.utils.repository.internal.DispatchingIndexElementRepository;

/**
 * @see DispatchingIndexElementRepository
 * @author omnaest
 */
public class DispatchingIndexElementRepositoryTest
{

    @Test
    public void testCRUD() throws Exception
    {
        IndexElementRepository<String> repository = IndexElementRepository.of(() -> IndexElementRepository.of(new LinkedHashMap<>()), 10l);

        assertTrue(repository.isEmpty());

        assertEquals("a", repository.get(repository.add("a")));
        assertEquals("a", repository.get(0l));
        assertEquals("b", repository.get(repository.add("b")));

        assertFalse(repository.isEmpty());

        repository.put(0l, "c");
        assertEquals("c", repository.get(0l));

        //
        repository.remove(0l);
        repository.remove(1l);
        assertTrue(repository.isEmpty());
    }

    @Test
    public void testDispatching() throws Exception
    {
        IndexElementRepository<String> repository = IndexElementRepository.of(() -> IndexElementRepository.of(new LinkedHashMap<>()), 10l);

        for (int ii = 0; ii < 100; ii++)
        {
            assertEquals("a" + ii, repository.get(repository.add("a" + ii)));
        }

    }

}
