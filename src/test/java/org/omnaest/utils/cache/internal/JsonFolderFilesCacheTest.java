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
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.ThreadUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache.DataRoot;
import org.omnaest.utils.duration.TimeDuration;

public class JsonFolderFilesCacheTest
{
    @Test
    public void testDataRootSerialization()
    {
        DataRoot dataRoot = new JsonFolderFilesCache.DataRoot();
        dataRoot.getData()
                .put("key1", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key1", byte[].class);
        dataRoot.getData()
                .put("key2", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key2", byte[].class);
        dataRoot.getData()
                .put("key1", dataRoot.getNextIndex());
        dataRoot.getTypes()
                .put("key1", byte[].class);
        StringWriter stringWriter = new StringWriter();
        JSONHelper.prepareAsPrettyPrintWriterConsumer(dataRoot)
                  .accept(stringWriter);
        String json = stringWriter.toString();
        assertEquals(2, StringUtils.countMatches(json, "key1"));
        assertEquals(2, StringUtils.countMatches(json, "key2"));

        DataRoot clonedDataRoot = JSONHelper.readerDeserializer(DataRoot.class)
                                            .apply(new StringReader(json));
        assertEquals(2, clonedDataRoot.getData()
                                      .size());
        assertEquals(2, clonedDataRoot.getTypes()
                                      .size());
        assertEquals(Long.valueOf(3l), clonedDataRoot.getData()
                                                     .get("key1"));
        assertEquals(Long.valueOf(2l), clonedDataRoot.getData()
                                                     .get("key2"));
    }

    @Test
    public void testGetAge() throws Exception
    {
        Cache cache = CacheUtils.newJsonFolderCache(FileUtils.createRandomTempDirectory())
                                .asDurationLimitedCache(TimeDuration.of(1, TimeUnit.SECONDS));

        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1", String.class));
        assertTrue(cache.getAge("key1")
                        .as(TimeUnit.MILLISECONDS) <= 1000);

        ThreadUtils.sleepSilently(1100, TimeUnit.MILLISECONDS);

        assertEquals(null, cache.get("key1", String.class));
        assertTrue(cache.getAge("key1")
                        .as(TimeUnit.MILLISECONDS) >= 1000);
    }
}
