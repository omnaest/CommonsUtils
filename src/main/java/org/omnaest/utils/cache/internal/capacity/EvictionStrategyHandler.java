package org.omnaest.utils.cache.internal.capacity;

import java.util.Set;

public interface EvictionStrategyHandler
{
    public Set<String> determineEvictKeys(int volume, Set<String> keys);
}
