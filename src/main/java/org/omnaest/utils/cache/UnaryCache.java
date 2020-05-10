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

import java.util.function.Supplier;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.Cache.EvictionStrategyProvider;

/**
 * @see Cache
 * @author Omnaest
 * @param <V>
 */
public interface UnaryCache<V> extends CacheBase
{
    public V get(String key);

    public Supplier<V> getSupplier(String key);

    public void put(String key, V value);

    public V computeIfAbsent(String key, Supplier<V> supplier);

    /**
     * Returns a new {@link Cache} instance with a capacity limit and a random element eviction strategy
     * 
     * @param capacity
     * @param evictionStrategy
     * @return
     */
    public default CapacityLimitedUnaryCache<V> withCapacityLimit(int capacity, EvictionStrategyProvider evictionStrategy)
    {
        return CacheUtils.toCapacityLimitedUnaryCache(this, evictionStrategy)
                         .withCapacityLimit(capacity);
    }
}
