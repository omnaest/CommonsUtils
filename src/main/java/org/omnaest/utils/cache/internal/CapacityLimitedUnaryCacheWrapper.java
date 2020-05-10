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

}
