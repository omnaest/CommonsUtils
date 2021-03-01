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

import java.util.function.Supplier;

import org.omnaest.utils.cache.Cache.EvictionStrategyProvider;
import org.omnaest.utils.cache.CapacityLimitedUnaryCache;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.cache.internal.capacity.CacheCapacityLimiter;
import org.omnaest.utils.cache.internal.capacity.EvictionStrategyHandler;

public class CapacityLimitedUnaryCacheWrapper<V> extends UnaryCacheDecorator<V> implements CapacityLimitedUnaryCache<V>
{
    private CacheCapacityLimiter capacityLimiter = new CacheCapacityLimiter(this);

    public CapacityLimitedUnaryCacheWrapper(UnaryCache<V> cache, EvictionStrategyProvider evictionStrategyProvider)
    {
        super(cache);
        this.capacityLimiter.setEvictionStrategy(evictionStrategyProvider.get());
    }

    @Override
    public CapacityLimitedUnaryCacheWrapper<V> withCapacityLimit(int capacity)
    {
        this.capacityLimiter.setCapacity(capacity);
        return this;
    }

    public CapacityLimitedUnaryCacheWrapper<V> withEvictionStrategy(EvictionStrategyHandler evictionStrategy)
    {
        this.capacityLimiter.setEvictionStrategy(evictionStrategy);
        return this;
    }

    @Override
    public void put(String key, V value)
    {
        this.capacityLimiter.incrementCounterAndValidateCapacity();
        super.put(key, value);
    }

    @Override
    public V computeIfAbsent(String key, Supplier<V> supplier)
    {
        this.capacityLimiter.incrementCounterAndValidateCapacity();
        return super.computeIfAbsent(key, supplier);
    }

    @Override
    public CapacityLimitedUnaryCache<V> withEvictionRatio(double ratio)
    {
        this.capacityLimiter.setEvictionRatio(ratio);
        return this;
    }

}
