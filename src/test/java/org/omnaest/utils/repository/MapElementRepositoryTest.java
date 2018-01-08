package org.omnaest.utils.repository;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.Test;

public class MapElementRepositoryTest
{

    @Test
    public void testGet() throws Exception
    {
        AtomicLong counter = new AtomicLong();
        Supplier<Long> idSupplier = () -> counter.getAndIncrement();
        ElementRepository<Long, String> repository = ElementRepository.of(new HashMap<Long, String>(), idSupplier)
                                                                      .asWeakCached()
                                                                      .asSynchronized();

        assertEquals("a", repository.get(repository.add("a")));
        assertEquals("a", repository.get(0l));
        assertEquals("b", repository.get(repository.add("b")));

        repository.update(0l, "c");
        assertEquals("c", repository.get(0l));
    }

}