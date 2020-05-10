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

import java.io.File;
import java.util.function.Supplier;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.Cache.EvictionStrategyProvider;
import org.omnaest.utils.cache.CapacityLimitedCache;
import org.omnaest.utils.cache.CapacityLimitedUnaryCache;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.cache.internal.CacheToUnaryCacheAdapter;
import org.omnaest.utils.cache.internal.CapacityLimitedCacheWrapper;
import org.omnaest.utils.cache.internal.CapacityLimitedUnaryCacheWrapper;
import org.omnaest.utils.cache.internal.ConcurrentHashMapCache;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;
import org.omnaest.utils.cache.internal.JsonSingleFileCache;
import org.omnaest.utils.element.cached.CachedElement;

/**
 * @see Cache
 * @author Omnaest
 */
public class CacheUtils
{
    public static final String DEFAULT_CACHE_FOLDER = "cache";

    /**
     * Reads the content of the source {@link Cache} and populates the content into the target {@link Cache}
     *
     * @param cacheSource
     * @param cacheTarget
     */
    public static <V> void populateCacheContentToNewCache(Cache cacheSource, Cache cacheTarget)
    {
        if (cacheSource != null && cacheTarget != null)
        {
            for (String key : cacheSource.keySet())
            {
                cacheTarget.put(key, cacheSource.get(key, cacheSource.getType(key)));
            }
        }
    }

    public static Cache newConcurrentInMemoryCache()
    {
        return new ConcurrentHashMapCache();
    }

    public static <V> Cache newJsonFileCache(File cacheFile)
    {
        return new JsonSingleFileCache(cacheFile);
    }

    public static <V> Cache newJsonFolderCache(File cacheDirectory)
    {
        return new JsonFolderFilesCache(cacheDirectory);
    }

    public static CapacityLimitedCache toCapacityLimitedCache(Cache cache, EvictionStrategyProvider evictionStrategy)
    {
        return new CapacityLimitedCacheWrapper(cache, evictionStrategy);
    }

    public static <V> CapacityLimitedUnaryCache<V> toCapacityLimitedUnaryCache(UnaryCache<V> cache, EvictionStrategyProvider evictionStrategy)
    {
        return new CapacityLimitedUnaryCacheWrapper<>(cache, evictionStrategy);
    }

    /**
     * Returns a new {@link JsonFolderFilesCache} for the local {@value #DEFAULT_CACHE_FOLDER} folder.
     * 
     * @param name
     * @return
     */
    public static Cache newLocalJsonFolderCache(String name)
    {
        return newJsonFolderCache(new File(DEFAULT_CACHE_FOLDER, name));
    }

    /**
     * Returns a {@link CachedElement} around the given {@link Supplier} using a json based {@link File} cache
     * 
     * @param type
     * @param cacheFile
     * @param supplier
     * @return
     */
    public static <V> CachedElement<V> toFileCachedElement(Class<V> type, File cacheFile, Supplier<V> supplier)
    {
        boolean pretty = true;
        return toFileCachedElement(type, cacheFile, pretty, supplier);
    }

    /**
     * Similar to {@link #toFileCachedElement(Class, File, Supplier)} but allows to specify if pretty print is enabled or not
     * 
     * @param type
     * @param cacheFile
     * @param pretty
     * @param supplier
     * @return
     */
    public static <V> CachedElement<V> toFileCachedElement(Class<? super V> type, File cacheFile, boolean pretty, Supplier<V> supplier)
    {
        return CachedElement.of(supplier)
                            .withFileCache(cacheFile, JSONHelper.writerSerializer(type), JSONHelper.readerDeserializer(type));
    }

    /**
     * Returns a {@link UnaryCache} for the given {@link Cache} instance
     * 
     * @param cache
     * @param type
     * @return
     */
    public static <V> UnaryCache<V> toUnaryCache(Cache cache, Class<V> type)
    {
        return new CacheToUnaryCacheAdapter<>(cache, type);
    }
}
