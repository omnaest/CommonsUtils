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
