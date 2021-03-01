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
package org.omnaest.utils.cache.internal;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.ThreadUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.duration.TimeDuration;

/**
 * @see DurationLimitedCache
 * @author omnaest
 */
public class DurationLimitedCacheTest
{
    private Cache cache = CacheUtils.newConcurrentInMemoryCache()
                                    .asDurationLimitedCache(TimeDuration.of(300, TimeUnit.MILLISECONDS));

    @Test
    public void testGet() throws Exception
    {
        this.cache.put("key1", "value1");
        assertEquals("value1", this.cache.get("key1", String.class));

        ThreadUtils.sleepSilently(500, TimeUnit.MILLISECONDS);
        assertEquals(null, this.cache.get("key1", String.class));
    }

    @Test
    public void testComputeIfAbsent() throws Exception
    {
        assertEquals("value1", this.cache.computeIfAbsent("key1", () -> "value1", String.class));

        ThreadUtils.sleepSilently(500, TimeUnit.MILLISECONDS);
        assertEquals("value2", this.cache.computeIfAbsent("key1", () -> "value2", String.class));
    }

    @Test
    public void testComputeIfAbsentOrUpdate() throws Exception
    {
        assertEquals("value1", this.cache.computeIfAbsentOrUpdate("key1", () -> "value1", v -> v, String.class));

        ThreadUtils.sleepSilently(500, TimeUnit.MILLISECONDS);
        assertEquals("value2", this.cache.computeIfAbsentOrUpdate("key1", () -> "value2", v -> v, String.class));
    }

}
