package org.omnaest.utils.cache.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;

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

    /**
     * AC2 - the reported bug: a value stored under one type must not be silently returned when a different,
     * non-assignable type is requested. Instead {@link ClassCastException} must be thrown naming both types.
     */
    @Test
    public void testGetWithMismatchedTypeThrowsClassCastException() throws Exception
    {
        this.cache.put("2", new Point(1, 2));

        try
        {
            this.cache.get("2", String.class);
            fail("Expected a ClassCastException due to the requested type not matching the stored type");
        }
        catch (ClassCastException e)
        {
            assertTrue("Exception message should name the requested type: " + e.getMessage(), e.getMessage()
                                                                                               .contains(String.class.getName()));
            assertTrue("Exception message should name the stored type: " + e.getMessage(), e.getMessage()
                                                                                            .contains(Point.class.getName()));
        }
    }

    /**
     * AC3 - requesting a legitimate supertype/interface of the stored concrete type must not throw.
     */
    @Test
    public void testGetWithAssignableSupertypeReturnsValue() throws Exception
    {
        this.cache.put("3", "I love you!");

        assertEquals("I love you!", this.cache.get("3", Object.class));
    }

    /**
     * AC4 - computeIfAbsent must also fail fast when the key already holds a value of a different, non-assignable
     * stored type (closing the same hole that get() had).
     */
    @Test
    public void testComputeIfAbsentWithMismatchedStoredTypeThrowsClassCastException() throws Exception
    {
        this.cache.put("4", new Point(1, 2));

        try
        {
            this.cache.computeIfAbsent("4", () -> "str", String.class);
            fail("Expected a ClassCastException due to the existing stored type not matching the requested type");
        }
        catch (ClassCastException e)
        {
            assertTrue("Exception message should name the requested type: " + e.getMessage(), e.getMessage()
                                                                                               .contains(String.class.getName()));
            assertTrue("Exception message should name the stored type: " + e.getMessage(), e.getMessage()
                                                                                            .contains(Point.class.getName()));
        }
    }

    /**
     * AC5 - computeIfAbsent on a key that has no value yet must still return the supplied value.
     */
    @Test
    public void testComputeIfAbsentOnAbsentKeyReturnsSuppliedValue() throws Exception
    {
        String value = this.cache.computeIfAbsent("5", () -> "supplied value", String.class);

        assertEquals("supplied value", value);
    }

    /**
     * AC6 - contains() relies internally on getType(key) followed by get(key, storedType); this must keep working
     * once type validation is introduced (requested type equals stored type is always assignable).
     */
    @Test
    public void testContainsAfterPutReturnsTrue() throws Exception
    {
        assertFalse(this.cache.contains("6"));

        this.cache.put("6", "I love you!");

        assertTrue(this.cache.contains("6"));
    }

}
