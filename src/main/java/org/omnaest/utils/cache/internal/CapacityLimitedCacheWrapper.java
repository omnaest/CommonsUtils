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
import java.util.function.Supplier;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.CapacityLimitedCache;
import org.omnaest.utils.cache.internal.capacity.CacheCapacityLimiter;

public class CapacityLimitedCacheWrapper extends CacheDecorator implements CapacityLimitedCache
{
    private CacheCapacityLimiter capacityLimiter = new CacheCapacityLimiter(this);

    public CapacityLimitedCacheWrapper(Cache cache, EvictionStrategyProvider evictionStrategyProvider)
    {
        super(cache);
        this.capacityLimiter.setEvictionStrategy(evictionStrategyProvider.get());
    }

    @Override
    public CapacityLimitedCacheWrapper withCapacityLimit(int capacity)
    {
        this.capacityLimiter.setCapacity(capacity);
        return this;
    }

    @Override
    public void put(String key, Object value)
    {
        this.capacityLimiter.incrementCounterAndValidateCapacity();
        super.put(key, value);
    }

    @Override
    public <V> void putAll(Map<String, V> map)
    {
        this.capacityLimiter.incrementCounterAndValidateCapacity(map != null ? map.size() : 0);
        super.putAll(map);
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        this.capacityLimiter.incrementCounterAndValidateCapacity();
        return super.computeIfAbsent(key, supplier, type);
    }

    @Override
    public String toString()
    {
        return "CapacityLimitedCacheWithRandomEviction [capacityLimiter=" + this.capacityLimiter + ", toString()=" + super.toString() + "]";
    }

    @Override
    public CapacityLimitedCache withEvictionRatio(double ratio)
    {
        this.capacityLimiter.setEvictionRatio(ratio);
        return this;
    }

}
