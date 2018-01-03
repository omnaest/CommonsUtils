package org.omnaest.utils.repository.map;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.utils.repository.DirectoryElementRepository;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.MapElementRepository;

public class ConcurrentRepositoryHashMapTest
{
    private static final int CYCLES = 10000;

    @Test
    public void testConcurrentRepositoryMap() throws Exception
    {
        AtomicLong counter = new AtomicLong();
        Supplier<Long> idSupplier = () -> counter.getAndIncrement();
        ElementRepository<Long, String> keyElementRepository = new MapElementRepository<>(new HashMap<>(), idSupplier);
        ElementRepository<Long, String> valueElementRepository = new MapElementRepository<>(new HashMap<>(), idSupplier);
        Map<String, String> repositoryMap = new ConcurrentRepositoryHashMap<>(keyElementRepository, valueElementRepository);

        for (int ii = 0; ii < 100; ii++)
        {
            repositoryMap.put(this.generateKey(ii), this.generateValue(ii));
            assertEquals(this.generateValue(ii), repositoryMap.get(this.generateKey(ii)));
        }

    }

    @Test
    @Ignore
    public void testConcurrentRepositoryMapPerformance() throws Exception
    {
        System.gc();
        Thread.sleep(10000);

        AtomicLong counter = new AtomicLong();
        Supplier<Long> idSupplier = () -> counter.getAndIncrement();
        ElementRepository<Long, String> keyElementRepository = new MapElementRepository<>(new HashMap<>(), idSupplier);
        ElementRepository<Long, String> valueElementRepository = new MapElementRepository<>(new HashMap<>(), idSupplier);
        Map<String, String> repositoryMap = new ConcurrentRepositoryHashMap<>(keyElementRepository, valueElementRepository);

        for (int ii = 0; ii < CYCLES; ii++)
        {
            repositoryMap.put(this.generateKey(ii), this.generateValue(ii));
            assertEquals(this.generateValue(ii), repositoryMap.get(this.generateKey(ii)));
        }

        System.gc();
        Thread.sleep(10000);
    }

    private String generateKey(int ii)
    {
        return "key" + ii;
    }

    private String generateValue(int ii)
    {
        return StringUtils.repeat("value" + ii, 1000);
    }

    @Test
    @Ignore
    public void testConcurrentRepositoryMapWithDirectoryPerformance() throws Exception
    {
        File directory = new File("C:/Temp/elementRepositoryTest");
        File keyDirectory = new File(directory, "key");
        File valueDirectory = new File(directory, "value");
        ElementRepository<Long, String> keyElementRepository = new DirectoryElementRepository<>(keyDirectory, String.class);
        ElementRepository<Long, String> valueElementRepository = new DirectoryElementRepository<>(valueDirectory, String.class);
        Map<String, String> repositoryMap = new ConcurrentRepositoryHashMap<>(keyElementRepository, valueElementRepository);

        for (int ii = 0; ii < CYCLES; ii++)
        {
            repositoryMap.put(this.generateKey(ii), this.generateValue(ii));
            assertEquals(this.generateValue(ii), repositoryMap.get(this.generateKey(ii)));
        }

        System.gc();
        Thread.sleep(10000);
    }

}
