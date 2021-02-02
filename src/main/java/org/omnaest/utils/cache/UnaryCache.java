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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.Cache.EvictionStrategyProvider;
import org.omnaest.utils.cache.UnaryCache.Entry;

/**
 * @see Cache
 * @author Omnaest
 * @param <V>
 */
public interface UnaryCache<V> extends CacheBase, Iterable<Entry<V>>, Function<String, V>
{
    public V get(String key);

    public Supplier<V> getSupplier(String key);

    public void put(String key, V value);

    public void putAll(Map<String, V> map);

    public V computeIfAbsent(String key, Supplier<V> supplier);

    /**
     * If the given key is not present the {@link Supplier} is called, if there is a key existing then the update function is called to modify the value in the
     * store.
     * 
     * @param key
     * @param supplier
     * @param updateFunction
     * @return
     */
    public V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction);

    public default V computeIfAbsent(String key, Function<String, V> supplierFunction)
    {
        return this.computeIfAbsent(key, () -> supplierFunction.apply(key));
    }

    public default Stream<Entry<V>> stream()
    {
        return this.keySet()
                   .stream()
                   .map(key -> new Entry<V>()
                   {
                       @Override
                       public String getKey()
                       {
                           return key;
                       }

                       @Override
                       public V getValue()
                       {
                           return get(key);
                       }
                   });
    }

    @Override
    public default Iterator<Entry<V>> iterator()
    {
        return this.stream()
                   .iterator();
    }

    @Override
    public default V apply(String key)
    {
        return this.get(key);
    }

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

    /**
     * Returns a {@link Function} on top of this {@link UnaryCache} which will use the given supplier {@link Function} to generate an {@link UnaryCache.Entry}
     * if there is no entry for the given key.
     * 
     * @param supplierFunction
     * @return
     */
    public default Function<String, V> asSuppliedFunction(Function<String, V> supplierFunction)
    {
        return key -> this.computeIfAbsent(key, supplierFunction);
    }

    public static interface Entry<V>
    {
        public String getKey();

        public V getValue();
    }

    /**
     * Returns a new immutable {@link Map} instance with the content of the {@link UnaryCache} populated
     * 
     * @return
     */
    public default Map<String, V> toMap()
    {
        return Collections.unmodifiableMap(this.keySet()
                                               .stream()
                                               .collect(Collectors.toMap(key -> key, this::get)));
    }
}
