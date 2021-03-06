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
package org.omnaest.utils.cache.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.cache.UnaryCache;

public class UnaryCacheDecorator<V> implements UnaryCache<V>
{
    private UnaryCache<V> cache;

    public UnaryCacheDecorator(UnaryCache<V> cache)
    {
        super();
        this.cache = cache;
    }

    @Override
    public V get(String key)
    {
        return this.cache.get(key);
    }

    @Override
    public void putAll(Map<String, V> map)
    {
        this.cache.putAll(map);
    }

    @Override
    public Supplier<V> getSupplier(String key)
    {
        return this.cache.getSupplier(key);
    }

    @Override
    public void put(String key, V value)
    {
        this.cache.put(key, value);
    }

    @Override
    public V computeIfAbsent(String key, Supplier<V> supplier)
    {
        return this.cache.computeIfAbsent(key, supplier);
    }

    @Override
    public V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction)
    {
        return this.cache.computeIfAbsentOrUpdate(key, supplier, updateFunction);
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet();
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        this.cache.removeAll(keys);
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
    public Map<String, V> get(String... keys)
    {
        return this.cache.get(keys);
    }

    @Override
    public Map<String, V> get(Collection<String> keys)
    {
        return this.cache.get(keys);
    }

}
