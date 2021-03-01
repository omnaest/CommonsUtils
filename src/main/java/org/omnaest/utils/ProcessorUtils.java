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
package org.omnaest.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor;
import org.omnaest.utils.repository.MapElementRepository;

/**
 * Helper around processor implementations
 * 
 * @author omnaest
 */
public class ProcessorUtils
{
    /**
     * @see RepeatingFilteredProcessor
     * @param cacheProvider
     * @return
     */
    public static <R> RepeatingFilteredProcessor<R> newRepeatingFilteredProcessorWithCache(Function<Integer, UnaryCache<R>> cacheProvider)
    {
        return RepeatingFilteredProcessor.of(cacheProvider);
    }

    public static class CacheContent extends HashMap<String, Object>
    {
        private static final long serialVersionUID = -4143677993107803034L;

        public static CacheContent of(UnaryCache<?> cache)
        {
            CacheContent content = new CacheContent();
            content.putAll(cache.toMap());
            return content;
        }
    }

    public static <R> RepeatingFilteredProcessor<R> newRepeatingFilteredProcessorWithInMemoryCacheAndContentRepository(MapElementRepository<Integer, CacheContent> repository,
                                                                                                                       Class<R> type)
    {
        @SuppressWarnings("unchecked")
        Function<Integer, UnaryCache<R>> cacheProvider = cacheId ->
        {
            UnaryCache<R> cache = CacheUtils.newConcurrentInMemoryCache()
                                            .asUnaryCache(type);
            repository.get(cacheId)
                      .ifPresent(cacheContent -> cache.putAll((Map<String, R>) cacheContent));
            return cache;
        };
        return RepeatingFilteredProcessor.of(cacheProvider, (cacheId, cache) ->
        {
            repository.put(cacheId, CacheContent.of(cache));
            cache.clear();
        });
    }

    public static <R> RepeatingFilteredProcessor<R> newRepeatingFilteredProcessorWithInMemoryCacheAndRepository(MapElementRepository<String, R> repository,
                                                                                                                Class<R> type)
    {
        return RepeatingFilteredProcessor.of(cacheId -> CacheUtils.newConcurrentInMemoryCache()
                                                                  .asUnaryCache(type),
                                             (cacheId, cache) ->
                                             {
                                                 repository.putAll(cache.toMap());
                                                 cache.clear();
                                             });
    }
}
