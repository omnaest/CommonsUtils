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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.Cache.EvictionStrategy;
import org.omnaest.utils.cache.CapacityLimitedUnaryCache;
import org.omnaest.utils.cache.SingleElementCache;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.cache.internal.ConcurrentHashMapCache;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;
import org.omnaest.utils.cache.internal.JsonSingleFileCache;
import org.omnaest.utils.duration.TimeDuration;

import com.fasterxml.jackson.annotation.JsonCreator;

@RunWith(Parameterized.class)
public class CacheUtilsTest
{
    private UnaryCache<Value> cache;
    private Supplier<Cache>   cacheSupplier;

    public static class Value
    {
        private String value;

        @JsonCreator
        public Value(String value)
        {
            super();
            this.value = value;
        }

        public Value()
        {
            super();
        }

        public String getValue()
        {
            return this.value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

    }

    @Parameters
    public static Iterable<? extends Object> data() throws IOException
    {
        File tempFile = FileUtils.createRandomTempFile();
        File tempDirectory1 = FileUtils.createRandomTempDirectory();
        File tempDirectory2 = FileUtils.createRandomTempDirectory();
        File tempDirectory3 = FileUtils.createRandomTempDirectory();
        File tempDirectory4 = FileUtils.createRandomTempDirectory();
        Supplier<Cache> supplier1 = () -> new ConcurrentHashMapCache();
        Supplier<Cache> supplier2 = () -> new JsonSingleFileCache(tempFile);
        Supplier<Cache> supplier3 = () -> new JsonFolderFilesCache(tempDirectory1);
        Supplier<Cache> supplier4 = () -> new JsonFolderFilesCache(tempDirectory2).withNativeStringStorage(true)
                                                                                  .withNativeByteArrayStorage(true);
        Supplier<Cache> supplier5 = () -> new ConcurrentHashMapCache().asDurationLimitedCache(TimeDuration.of(1, TimeUnit.HOURS));
        Supplier<Cache> supplier6 = () -> new JsonSingleFileCache(tempFile).asDurationLimitedCache(TimeDuration.of(1, TimeUnit.HOURS));
        Supplier<Cache> supplier7 = () -> new JsonFolderFilesCache(tempDirectory3).asDurationLimitedCache(TimeDuration.of(1, TimeUnit.HOURS));
        Supplier<Cache> supplier8 = () -> CacheUtils.newRandomAccessLogarithmicBlockFileStorageCache(tempDirectory4, 10);
        return Arrays.<Supplier<Cache>>asList(supplier1, supplier2, supplier3, supplier4, supplier5, supplier6, supplier7, supplier8)
                     .stream()
                     .collect(Collectors.toList());
    }

    public CacheUtilsTest(Supplier<Cache> cacheSupplier)
    {
        super();
        this.cacheSupplier = cacheSupplier;
        this.cache = cacheSupplier.get()
                                  .asUnaryCache(Value.class);
        this.cache.clear();
    }

    @Test
    public void testGet() throws Exception
    {
        this.cache.put("key1", new Value("test"));
        assertEquals("test", this.cache.get("key1")
                                       .getValue());
        assertEquals(1, this.cache.size());
        assertFalse(this.cache.isEmpty());
    }

    @Test
    public void testGetByteArray() throws Exception
    {
        UnaryCache<byte[]> byteArrayCache = this.cacheSupplier.get()
                                                              .asUnaryCache(byte[].class);
        byteArrayCache.put("key1", new byte[] { 0, 1, 127 });
        assertEquals(Arrays.asList(ArrayUtils.toObject(new byte[] { 0, 1, 127 })), Arrays.asList(ArrayUtils.toObject(byteArrayCache.get("key1"))));
        assertEquals(1, byteArrayCache.size());
        assertFalse(byteArrayCache.isEmpty());
    }

    @Test
    public void testGetString() throws Exception
    {
        UnaryCache<String> stringCache = this.cacheSupplier.get()
                                                           .asUnaryCache(String.class);
        stringCache.put("key1", "hello everyone");
        assertEquals("hello everyone", stringCache.get("key1"));
        assertEquals(1, stringCache.size());
        assertFalse(stringCache.isEmpty());
    }

    @Test
    public void testRemove() throws Exception
    {
        this.cache.put("key1", new Value("test1"));
        this.cache.put("key2", new Value("test2"));

        this.cache.remove("key1");
        assertEquals(1, this.cache.size());
        assertFalse(this.cache.isEmpty());

        this.cache.clear();
        assertEquals(0, this.cache.size());
        assertTrue(this.cache.isEmpty());
    }

    @Test
    public void testComputeIfAbsent() throws Exception
    {
        Value value = this.cache.computeIfAbsent("key1", () -> new Value("value1"));
        assertEquals("value1", value.getValue());
    }

    @Test
    public void testToCapacityLimitedUnaryCache() throws Exception
    {
        //
        CapacityLimitedUnaryCache<Value> cache = this.cache.withCapacityLimit(10, EvictionStrategy.RANDOM);

        //
        cache.put("key1", new Value("test1"));
        assertEquals("key1", SetUtils.first(cache.keySet()));
        assertEquals("test1", cache.get("key1")
                                   .getValue());

        //
        IntStream.range(0, 100)
                 .forEach(ii -> cache.put("key" + ii, new Value("test" + ii)));
        assertTrue(cache.size() < 20);
        assertTrue(cache.size() > 5);
    }

    @Test
    public void testSingleElementCache() throws Exception
    {
        SingleElementCache<Value> singleElementCache = this.cache.asSingleElementCache();
        singleElementCache.accept(new Value("value1"));
        assertEquals("value1", singleElementCache.get()
                                                 .getValue());
    }

}
