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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.CacheWithNativeTypeSupport;
import org.omnaest.utils.optional.NullOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link Cache} which uses {@link JSONHelper} to store the cache content within multiple json {@link File}s
 *
 * @see JsonSingleFileCache
 * @author Omnaest
 * @param <V>
 */
public class JsonFolderFilesCache extends AbstractCache implements CacheWithNativeTypeSupport
{
    private static final String UTF_8 = "utf-8";
    private static final Logger LOG   = LoggerFactory.getLogger(JsonFolderFilesCache.class);

    private File cacheDirectory;

    private AtomicReference<DataRoot> root = new AtomicReference<>();

    private boolean nativeByteArrayStorage = false;
    private boolean nativeStringStorage    = false;

    @Override
    public JsonFolderFilesCache withNativeByteArrayStorage(boolean active)
    {
        this.nativeByteArrayStorage = active;
        return this;
    }

    @Override
    public JsonFolderFilesCache withNativeStringStorage(boolean active)
    {
        this.nativeStringStorage = active;
        return this;
    }

    protected static class DataRoot
    {
        @JsonProperty
        private AtomicLong index = new AtomicLong();

        @JsonProperty
        private Map<String, Long> data = new LinkedHashMap<>();

        @JsonProperty
        private Map<String, Class<?>> types = new LinkedHashMap<>();

        public DataRoot()
        {
            super();
        }

        public Map<String, Long> getData()
        {
            return this.data;
        }

        public Map<String, Class<?>> getTypes()
        {
            return this.types;
        }

        public AtomicLong getIndex()
        {
            return this.index;
        }

        public void setIndex(long index)
        {
            this.index.set(index);
        }

        @JsonIgnore
        public Long getNextIndex()
        {
            return this.index.incrementAndGet();
        }

    }

    public JsonFolderFilesCache(File cacheDirectory)
    {
        super();
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    public <V> V get(String key, Class<V> type)
    {
        Long fileIndex = this.getOrCreateRoot()
                             .getData()
                             .get(key);
        return this.readFromSingleCacheFile(fileIndex, type)
                   .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <V> NullOptional<V> readFromSingleCacheFile(Long index, Class<V> type)
    {
        return NullOptional.ofNullable(index)
                           .map(cacheFileIndex -> this.determineCacheFile(cacheFileIndex))
                           .filter(cacheFile -> cacheFile.exists())
                           .flatMap(cacheFile ->
                           {
                               try
                               {
                                   if (this.shouldBeHandledAsNativeByteArray(type))
                                   {
                                       return NullOptional.ofPresentNullable((V) FileUtils.readFileToByteArray(cacheFile));
                                   }
                                   else if (this.shouldBeHandledAsNativeString(type))
                                   {
                                       return NullOptional.ofPresentNullable((V) FileUtils.readFileToString(cacheFile, StandardCharsets.UTF_8));
                                   }
                                   else
                                   {
                                       return NullOptional.ofPresentNullable(JSONHelper.readerDeserializer(type)
                                                                                       .apply(org.omnaest.utils.FileUtils.toReader(cacheFile,
                                                                                                                                   StandardCharsets.UTF_8)));
                                   }
                               }
                               catch (IOException e)
                               {
                                   LOG.error("Exception reading single cache file", e);
                                   return NullOptional.empty();
                               }
                           });
    }

    private <V> boolean shouldBeHandledAsNativeString(Class<V> type)
    {
        return this.nativeStringStorage && String.class.equals(type);
    }

    private <V> boolean shouldBeHandledAsNativeByteArray(Class<V> type)
    {
        return this.nativeByteArrayStorage && byte[].class.equals(type);
    }

    private File determineCacheFile(Long fileIndex)
    {
        return new File(this.cacheDirectory, "" + fileIndex + ".json");
    }

    private Long writeToFileAndGetIndex(Object value)
    {
        Long index = this.getOrCreateRoot()
                         .getNextIndex();
        this.writeToSingleCacheFile(value, index);
        return index;
    }

    private void deleteOrphanCacheFile(Long index)
    {
        if (index != null)
        {
            try
            {
                FileUtils.forceDelete(this.determineCacheFile(index));
            }
            catch (IOException e)
            {
                LOG.error("Failed deleting orphan cache file: " + index);
            }
        }
    }

    private void writeToSingleCacheFile(Object value, Long index)
    {
        try
        {
            File cacheFile = this.determineCacheFile(index);
            if (this.shouldBeHandledAsNativeByteArray(Optional.ofNullable(value)
                                                              .map(Object::getClass)
                                                              .orElse(null)))
            {
                FileUtils.writeByteArrayToFile(cacheFile, (byte[]) value);
            }
            else if (this.shouldBeHandledAsNativeString(Optional.ofNullable(value)
                                                                .map(Object::getClass)
                                                                .orElse(null)))
            {
                FileUtils.writeStringToFile(cacheFile, (String) value, StandardCharsets.UTF_8);
            }
            else
            {
                try (FileWriterWithEncoding writer = new FileWriterWithEncoding(cacheFile, StandardCharsets.UTF_8))
                {
                    JSONHelper.prepareAsPrettyPrintWriterConsumer(value)
                              .accept(writer);
                }
            }
        }
        catch (IOException e)
        {
            LOG.error("Exception writing single cache file", e);
        }
    }

    @Override
    public void put(String key, Object value)
    {
        this.operateOnRootAndGet(root ->
        {
            this.deleteOrphanCacheFile(root.getData()
                                           .get(key));
            root.getData()
                .put(key, this.writeToFileAndGetIndex(value));
            root.getTypes()
                .put(key, value != null ? value.getClass() : null);
            return root;
        });

    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        Long index = this.getOrCreateRoot()
                         .getData()
                         .get(key);

        if (index == null)
        {
            index = this.operateOnRootAndGet(root ->
            {
                root.getData()
                    .computeIfAbsent(key, (id) ->
                    {
                        V value = supplier.get();
                        if (value != null)
                        {
                            root.getTypes()
                                .put(key, value.getClass());
                        }

                        Long fileIndex = this.writeToFileAndGetIndex(value);
                        root.getData()
                            .put(key, fileIndex);

                        return fileIndex;
                    });
                return root;
            })
                        .getData()
                        .get(key);
        }

        if (index != null)
        {
            long fileIndex = index;
            return this.readFromSingleCacheFile(fileIndex, type)
                       .orElseGet(() ->
                       {
                           V value = supplier.get();
                           this.writeToSingleCacheFile(value, fileIndex);
                           return value;
                       });
        }
        else
        {
            return null;
        }
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
            this.writeRootCacheFile();
        }
        return this.root.get();
    }

    private DataRoot loadRoot()
    {
        return Optional.ofNullable(this.readFromRootCacheFile())
                       .orElseGet(() -> new DataRoot());

    }

    private DataRoot readFromRootCacheFile()
    {
        DataRoot retval = null;
        File rootCacheFile = this.determineRootCacheFile();
        if (rootCacheFile.exists() && rootCacheFile.isFile())
        {
            try
            {
                String json = FileUtils.readFileToString(rootCacheFile, UTF_8);
                retval = StringUtils.isBlank(json) ? null : JSONHelper.readFromString(json, DataRoot.class);
            }
            catch (Exception e)
            {
                LOG.error("Exception reading file cache: " + this.cacheDirectory, e);
                retval = null;
            }
        }
        return retval;
    }

    private void writeRootCacheFile()
    {
        try
        {
            byte commitIndex = (byte) ((this.determineCommitIndex() + 1) % 2);
            org.omnaest.utils.FileUtils.toWriterSupplierUTF8(this.determineRootCacheFile(commitIndex))
                                       .toConsumerWith(JSONHelper.writerSerializer(DataRoot.class))
                                       .accept(this.root.get());
            FileUtils.writeByteArrayToFile(this.determineRootCommitFile(), new byte[] { commitIndex });
        }
        catch (Exception e)
        {
            LOG.error("Exception writing json to cache file: " + this.cacheDirectory, e);
        }
    }

    private byte determineCommitIndex()
    {
        byte retval = -1;
        try
        {
            byte[] content = FileUtils.readFileToByteArray(this.determineRootCommitFile());
            retval = content[0];
        }
        catch (Exception e)
        {
            //do nothing
        }

        return retval;
    }

    private File determineRootCommitFile()
    {
        return new File(this.cacheDirectory, "root.commit");
    }

    private File determineRootCacheFile()
    {
        return this.determineRootCacheFile(this.determineCommitIndex());
    }

    private File determineRootCacheFile(byte commitIndex)
    {
        String commitIndexStr = commitIndex >= 0 ? String.valueOf(commitIndex) : "";
        return new File(this.cacheDirectory, "root" + commitIndexStr + ".json");
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
                return r;
            });
        }

    }

}
