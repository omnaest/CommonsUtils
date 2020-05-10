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
package org.omnaest.utils.cache;

import java.util.Map;
import java.util.function.Supplier;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.internal.capacity.EvictionStrategyHandler;
import org.omnaest.utils.cache.internal.capacity.RandomEvictionStrategy;

/**
 * Defines a {@link Cache} API.
 * <br>
 * <br>
 * Example:<br>
 * 
 * <pre>
 * CacheUtils.newConcurrentInMemoryCache()
 * </pre>
 * 
 * @see UnaryCache
 * @see CacheUtils
 * @see CacheUtils#newConcurrentInMemoryCache()
 * @see CacheUtils#newJsonFileCache(java.io.File)
 * @author Omnaest
 * @param <V>
 */
public interface Cache extends CacheBase
{
    public <V> V get(String key, Class<V> type);

    public <V> Class<V> getType(String key);

    public <V> Supplier<V> getSupplier(String key, Class<V> type);

    public void put(String key, Object value);

    public <V> void putAll(Map<String, V> map);

    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type);

    /**
     * Returns a new {@link Cache} instance with a capacity limit and a random element eviction strategy
     * 
     * @param capacity
     * @param evictionStrategy
     * @return
     */
    public default CapacityLimitedCache withCapacityLimit(int capacity, EvictionStrategyProvider evictionStrategy)
    {
        return CacheUtils.toCapacityLimitedCache(this, evictionStrategy)
                         .withCapacityLimit(capacity);
    }

    /**
     * Returns an {@link UnaryCache} instance based on the current {@link Cache}
     * 
     * @param type
     * @return
     */
    public default <V> UnaryCache<V> asUnaryCache(Class<V> type)
    {
        return CacheUtils.toUnaryCache(this, type);
    }

    public static interface EvictionStrategyProvider extends Supplier<EvictionStrategyHandler>
    {
    }

    public static enum EvictionStrategy implements EvictionStrategyProvider
    {
        RANDOM(new RandomEvictionStrategy());

        private EvictionStrategyHandler handler;

        private EvictionStrategy(EvictionStrategyHandler handler)
        {
            this.handler = handler;
        }

        @Override
        public EvictionStrategyHandler get()
        {
            return this.handler;
        }

    }

}
