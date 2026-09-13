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
package org.omnaest.utils.cache;

import java.io.File;

import org.omnaest.utils.cache.Cache.EvictionStrategyProvider;
import org.omnaest.utils.cache.internal.CacheToUnaryCacheAdapter;
import org.omnaest.utils.cache.internal.CapacityLimitedCacheWrapper;
import org.omnaest.utils.cache.internal.CapacityLimitedUnaryCacheWrapper;
import org.omnaest.utils.cache.internal.ConcurrentHashMapCache;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;
import org.omnaest.utils.cache.internal.JsonSingleFileCache;
import org.omnaest.utils.cache.internal.NoOperationCache;
import org.omnaest.utils.cache.internal.RandomAccessLogarithmicBlockFileStorageCache;

/**
 * Constructs the {@link Cache} implementations that live in this package's {@code internal/} sub-package.
 * <p>
 * This is the door {@code cache/internal/} opens onto: it sits directly above that package, so it is the only
 * place in the codebase that needs to know those implementation types exist. Callers outside this package - in
 * particular {@link org.omnaest.utils.CacheUtils}, which delegates here and keeps its own signatures unchanged -
 * see only the interfaces declared in this package.
 * 
 * @see Cache
 * @author Omnaest
 */
public class CacheFactory
{
    public static Cache newConcurrentInMemoryCache()
    {
        return new ConcurrentHashMapCache();
    }

    public static Cache newJsonFileCache(File cacheFile)
    {
        return new JsonSingleFileCache(cacheFile);
    }

    public static Cache newRandomAccessLogarithmicBlockFileStorageCache(File cacheDirectory, int hashCapacity)
    {
        return new RandomAccessLogarithmicBlockFileStorageCache(cacheDirectory, hashCapacity);
    }

    public static Cache newRandomAccessLogarithmicBlockFileStorageCache(File cacheDirectory, int hashCapacity, int initialBlockSize)
    {
        return new RandomAccessLogarithmicBlockFileStorageCache(cacheDirectory, hashCapacity, initialBlockSize);
    }

    public static CacheWithNativeTypeSupport newJsonFolderCache(File cacheDirectory)
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

    public static <V> UnaryCache<V> toUnaryCache(Cache cache, Class<V> type)
    {
        return new CacheToUnaryCacheAdapter<>(cache, type);
    }

    public static Cache newNoOperationCache()
    {
        return new NoOperationCache();
    }
}
