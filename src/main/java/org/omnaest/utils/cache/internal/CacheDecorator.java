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

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.duration.TimeDuration;

/**
 * @see Cache
 * @author omnaest
 */
public class CacheDecorator implements Cache
{
    private Cache cache;

    public CacheDecorator(Cache cache)
    {
        super();
        this.cache = cache;
    }

    @Override
    public boolean contains(String key)
    {
        return this.cache.contains(key);
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        this.cache.removeAll(keys);
    }

    @Override
    public <V> V get(String key, Class<V> type)
    {
        return this.cache.get(key, type);
    }

    @Override
    public <V> Class<V> getType(String key)
    {
        return this.cache.getType(key);
    }

    @Override
    public <V> Supplier<V> getSupplier(String key, Class<V> type)
    {
        return this.cache.getSupplier(key, type);
    }

    @Override
    public void put(String key, Object value)
    {
        this.cache.put(key, value);
    }

    @Override
    public <V> void putAll(Map<String, V> map)
    {
        this.cache.putAll(map);
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return this.cache.computeIfAbsent(key, supplier, type);
    }

    @Override
    public <V> V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction, Class<V> type)
    {
        return this.cache.computeIfAbsentOrUpdate(key, supplier, updateFunction, type);
    }

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet();
    }

    @Override
    public boolean isEmpty()
    {
        return this.cache.isEmpty();
    }

    @Override
    public int size()
    {
        return this.cache.size();
    }

    @Override
    public void clear()
    {
        this.cache.clear();
    }

    @Override
    public TimeDuration getAge(String key)
    {
        return this.cache.getAge(key);
    }

    @Override
    public String toString()
    {
        return "CacheDecorator [cache=" + this.cache + "]";
    }

}
