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
package org.omnaest.utils.cache.internal.capacity;

import java.util.concurrent.atomic.AtomicLong;

import org.omnaest.utils.AssertionUtils;
import org.omnaest.utils.cache.CacheBase;

public class CacheCapacityLimiter
{
    private int    capacity;
    private double evictionRatio = 0.3;

    private CacheBase cache;

    private AtomicLong              counter          = new AtomicLong();
    private EvictionStrategyHandler evictionStrategy = new RandomEvictionStrategy();

    public CacheCapacityLimiter(CacheBase cache)
    {
        super();
        this.cache = cache;
    }

    public CacheCapacityLimiter setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }

    public CacheCapacityLimiter setEvictionRatio(double evictionRatio)
    {
        this.evictionRatio = evictionRatio;
        return this;
    }

    public CacheCapacityLimiter setEvictionStrategy(EvictionStrategyHandler evictionStrategy)
    {
        AssertionUtils.assertIsNotNull("A eviction strategy must be provided", evictionStrategy);
        this.evictionStrategy = evictionStrategy;
        return this;
    }

    public void incrementCounterAndValidateCapacity()
    {
        this.incrementCounterAndValidateCapacity(1);
    }

    public void incrementCounterAndValidateCapacity(int increment)
    {
        long counter = this.counter.addAndGet(increment);
        long evictionVolume = this.determineEvictionVolume();
        if (counter % evictionVolume == 0)
        {
            this.validateCapacity();
        }
    }

    private int determineEvictionVolume()
    {
        return (int) (this.capacity * this.evictionRatio);
    }

    private void validateCapacity()
    {
        if (this.cache.size() > this.capacity)
        {
            this.cache.removeAll(this.evictionStrategy.determineEvictKeys(this.determineEvictionVolume(), this.cache.keySet()));
        }
    }

    @Override
    public String toString()
    {
        return "CacheCapacityLimiter [capacity=" + this.capacity + ", evictionRatio=" + this.evictionRatio + ", counter=" + this.counter + ", evictionStrategy="
                + this.evictionStrategy + "]";
    }

}
