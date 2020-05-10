package org.omnaest.utils.cache;

public interface CapacityLimitedCache extends Cache
{
    public CapacityLimitedCache withCapacityLimit(int capacity);
}