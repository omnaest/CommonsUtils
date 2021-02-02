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

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.UnaryCache;

/**
 * @see Cache
 * @see UnaryCache
 * @author Omnaest
 * @param <V>
 */
public class CacheToUnaryCacheAdapter<V> implements UnaryCache<V>
{
    private Cache    cache;
    private Class<V> type;

    public CacheToUnaryCacheAdapter(Cache cache, Class<V> type)
    {
        super();
        this.cache = cache;
        this.type = type;
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        this.cache.removeAll(keys);
    }

    @Override
    public V get(String key)
    {
        return this.cache.get(key, this.type);
    }

    @Override
    public Supplier<V> getSupplier(String key)
    {
        return this.cache.getSupplier(key, this.type);
    }

    @Override
    public void put(String key, V value)
    {
        this.cache.put(key, value);
    }

    @Override
    public void putAll(Map<String, V> map)
    {
        this.cache.putAll(map);
    }

    @Override
    public V computeIfAbsent(String key, Supplier<V> supplier)
    {
        return this.cache.computeIfAbsent(key, supplier, this.type);
    }

    @Override
    public V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction)
    {
        return this.cache.computeIfAbsentOrUpdate(key, supplier, updateFunction, this.type);
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet();
    }

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public int size()
    {
        return this.cache.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.cache.isEmpty();
    }

    @Override
    public void clear()
    {
        this.cache.clear();
    }

    @Override
    public String toString()
    {
        return "CacheToUnaryCacheAdapter [cache=" + this.cache + ", type=" + this.type + "]";
    }

}
