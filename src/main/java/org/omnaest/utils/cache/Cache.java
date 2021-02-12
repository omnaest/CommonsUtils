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
import java.util.function.UnaryOperator;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.internal.DurationLimitedCache;
import org.omnaest.utils.cache.internal.capacity.EvictionStrategyHandler;
import org.omnaest.utils.cache.internal.capacity.RandomEvictionStrategy;
import org.omnaest.utils.duration.TimeDuration;

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

    public TimeDuration getAge(String key);

    public <V> Class<V> getType(String key);

    public <V> Supplier<V> getSupplier(String key, Class<V> type);

    public void put(String key, Object value);

    public <V> void putAll(Map<String, V> map);

    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type);

    /**
     * If the given key is not present the {@link Supplier} is called, if there is a key existing then the update function is called to modify the value in the
     * store.
     * 
     * @param key
     * @param supplier
     * @param updateFunction
     * @param type
     * @return
     */
    public <V> V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction, Class<V> type);

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
    @SuppressWarnings("unchecked")
    public default <V> UnaryCache<V> asUnaryCache(Class<? super V> type)
    {
        return (UnaryCache<V>) CacheUtils.toUnaryCache(this, type);
    }

    /**
     * Returns a {@link Cache} where the entries are evicted on read after the given {@link TimeDuration}
     * 
     * @param timeDuration
     * @return
     */
    public default Cache asDurationLimitedCache(TimeDuration timeDuration)
    {
        return new DurationLimitedCache(this, timeDuration);
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
