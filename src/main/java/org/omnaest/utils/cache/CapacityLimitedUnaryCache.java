package org.omnaest.utils.cache;

public interface CapacityLimitedUnaryCache<V> extends UnaryCache<V>
{
    public CapacityLimitedUnaryCache<V> withCapacityLimit(int capacity);
}