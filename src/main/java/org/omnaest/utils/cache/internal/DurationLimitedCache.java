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
import java.util.stream.Collectors;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.duration.TimeDuration;

/**
 * {@link Cache} wrapper which enforces an eviction of single keys after a given {@link TimeDuration}
 * 
 * @author omnaest
 */
public class DurationLimitedCache implements Cache
{
    private Cache        cache;
    private TimeDuration duration;

    public DurationLimitedCache(Cache cache, TimeDuration duration)
    {
        super();
        this.cache = cache;
        this.duration = duration;
    }

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public boolean contains(String key)
    {
        if (this.cache.getAge(key)
                      .isSmallerThan(this.duration))
        {
            return this.cache.contains(key);
        }
        else
        {
            return false;
        }
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        this.cache.removeAll(keys);
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet()
                         .stream()
                         .filter(key -> this.getAge(key)
                                            .isSmallerThan(this.duration))
                         .collect(Collectors.toSet());
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
    public <V> V get(String key, Class<V> type)
    {
        if (this.cache.getAge(key)
                      .isSmallerThan(this.duration))
        {
            return this.cache.get(key, type);
        }
        else
        {
            return null;
        }
    }

    @Override
    public TimeDuration getAge(String key)
    {
        return this.cache.getAge(key);
    }

    @Override
    public <V> Class<V> getType(String key)
    {
        return this.cache.getType(key);
    }

    @Override
    public <V> Supplier<V> getSupplier(String key, Class<V> type)
    {
        return () -> this.get(key, type);
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
        if (this.cache.getAge(key)
                      .isSmallerThan(this.duration))
        {
            return this.cache.computeIfAbsent(key, supplier, type);
        }
        else
        {
            return this.cache.computeIfAbsentOrUpdate(key, supplier, previous -> supplier.get(), type);
        }
    }

    @Override
    public <V> V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction, Class<V> type)
    {
        return this.cache.computeIfAbsentOrUpdate(key, supplier, previous ->
        {
            if (this.cache.getAge(key)
                          .isSmallerThan(this.duration))
            {
                return updateFunction.apply(previous);
            }
            else
            {
                return supplier.get();
            }
        }, type);
    }

}
