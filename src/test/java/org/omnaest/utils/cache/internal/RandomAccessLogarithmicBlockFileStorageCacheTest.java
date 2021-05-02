package org.omnaest.utils.cache.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.cache.Cache;

public class RandomAccessLogarithmicBlockFileStorageCacheTest
{
    private Cache cache = new RandomAccessLogarithmicBlockFileStorageCache(FileUtils.createRandomTempDirectoryQuietly()
                                                                                    .orElseThrow(() -> new IllegalStateException("Unable to create temp directory")),
                                                                           10);

    @Test
    public void testPutAndGet() throws Exception
    {
        assertNull(this.cache.get("1", String.class));
        this.cache.put("1", "I love you!");
        assertEquals("I love you!", this.cache.get("1", String.class));
    }

}
