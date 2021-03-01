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
package org.omnaest.utils.cache.internal;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.RetryUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.duration.TimeDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link Cache} which uses {@link JSONHelper} to store the cache content within a single json {@link File}
 *
 * @see JsonFolderFilesCache
 * @author Omnaest
 * @param <V>
 */
public class JsonSingleFileCache extends AbstractCache
{
    private static final Logger LOG = LoggerFactory.getLogger(JsonSingleFileCache.class);

    private File cacheFile;

    private AtomicReference<DataRoot> root = new AtomicReference<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class DataRoot
    {
        @JsonProperty
        private LinkedHashMap<String, JsonNode> data = new LinkedHashMap<>();

        @JsonProperty
        private LinkedHashMap<String, Class<?>> types = new LinkedHashMap<>();

        @JsonProperty
        private LinkedHashMap<String, Long> creationDates = new LinkedHashMap<>();

        public DataRoot()
        {
            super();

        }

        public LinkedHashMap<String, JsonNode> getData()
        {
            return this.data;
        }

        public LinkedHashMap<String, Class<?>> getTypes()
        {
            return this.types;
        }

        public LinkedHashMap<String, Long> getCreationDates()
        {
            return this.creationDates;
        }

    }

    public JsonSingleFileCache(File cacheFile)
    {
        super();
        this.cacheFile = cacheFile;
    }

    @Override
    public <V> V get(String key, Class<V> type)
    {
        return this.readFromJsonNode(this.getOrCreateRoot()
                                         .getData()
                                         .get(key),
                                     type);
    }

    @Override
    public TimeDuration getAge(String key)
    {
        long creationTime = Optional.ofNullable(this.getOrCreateRoot()
                                                    .getCreationDates()
                                                    .get(key))
                                    .orElseGet(() -> System.currentTimeMillis());
        long duration = System.currentTimeMillis() - creationTime;
        return TimeDuration.of(duration, TimeUnit.MILLISECONDS);
    }

    private <V> V readFromJsonNode(JsonNode jsonNode, Class<V> type)
    {
        return JSONHelper.toObjectWithType(jsonNode, type);
    }

    private JsonNode convertToJsonNode(Object value)
    {
        return JSONHelper.toObjectWithType(value, JsonNode.class);
    }

    @Override
    public void put(String key, Object value)
    {
        this.operateOnRootAndGet(t ->
        {
            t.getData()
             .put(key, this.convertToJsonNode(value));
            t.getTypes()
             .put(key, value.getClass());
            t.getCreationDates()
             .put(key, System.currentTimeMillis());
            return t;
        });

    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return this.readFromJsonNode(Optional.ofNullable(this.getOrCreateRoot()
                                                             .getData()
                                                             .get(key))
                                             .orElseGet(() -> this.operateOnRootAndGet(t ->
                                             {
                                                 t.getData()
                                                  .computeIfAbsent(key, (id) ->
                                                  {
                                                      V value = supplier.get();
                                                      if (value != null)
                                                      {
                                                          t.getTypes()
                                                           .put(key, value.getClass());
                                                          t.getCreationDates()
                                                           .put(key, System.currentTimeMillis());
                                                      }
                                                      return this.convertToJsonNode(value);
                                                  });
                                                 return t;
                                             })
                                                                  .getData()
                                                                  .get(key)),
                                     type);

    }

    @Override
    public Set<String> keySet()
    {
        return new HashSet<>(this.getOrCreateRoot()
                                 .getData()
                                 .keySet());
    }

    protected DataRoot getOrCreateRoot()
    {
        DataRoot retmap = this.root.get();

        if (retmap == null)
        {
            this.operateOnRootAndGet(r -> r == null ? this.loadRoot() : null, () -> this.root.get());
            retmap = this.root.get();
        }

        return retmap;
    }

    public DataRoot operateOnRootAndGet(UnaryOperator<DataRoot> updateFunction)
    {
        return this.operateOnRootAndGet(updateFunction, () -> this.getOrCreateRoot());
    }

    public DataRoot operateOnRootAndGet(UnaryOperator<DataRoot> updateFunction, Supplier<DataRoot> initialDataRoot)
    {
        synchronized (this.root)
        {
            this.root.set(updateFunction.apply(initialDataRoot.get()));
            this.writeCacheFile();
        }
        return this.root.get();
    }

    private DataRoot loadRoot()
    {
        return Optional.ofNullable(this.readFromCacheFile())
                       .orElseGet(() -> new DataRoot());

    }

    private DataRoot readFromCacheFile()
    {
        DataRoot retval = null;
        if (this.cacheFile.exists() && this.cacheFile.isFile())
        {
            synchronized (this.cacheFile)
            {
                try
                {
                    retval = RetryUtils.retry(5 * 10, 100, TimeUnit.MILLISECONDS, () ->
                    {
                        return FileUtils.readFrom(this.cacheFile, JSONHelper.prepareAsReaderToObjectFunction(DataRoot.class));
                    });
                }
                catch (Exception e)
                {
                    LOG.error("Exception reading file cache: " + this.cacheFile, e);
                    retval = null;
                }
            }
        }
        return retval;
    }

    private void writeCacheFile()
    {
        try
        {
            synchronized (this.cacheFile)
            {
                FileUtils.writeTo(this.cacheFile, JSONHelper.prepareAsPrettyPrintWriterConsumer(this.root.get()));
            }
        }
        catch (Exception e)
        {
            LOG.error("Exception writing json to cache file: " + this.cacheFile, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Class<V> getType(String key)
    {
        return (Class<V>) this.getOrCreateRoot()
                              .getTypes()
                              .get(key);
    }

    @Override
    public void remove(String key)
    {
        if (this.getOrCreateRoot()
                .getData()
                .containsKey(key))
        {
            this.operateOnRootAndGet(r ->
            {
                r.getData()
                 .remove(key);
                r.getTypes()
                 .remove(key);
                r.getCreationDates()
                 .remove(key);
                return r;
            });
        }

    }

}
