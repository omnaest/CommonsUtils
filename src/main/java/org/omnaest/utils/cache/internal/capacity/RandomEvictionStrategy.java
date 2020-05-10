package org.omnaest.utils.cache.internal.capacity;

import java.util.Set;
import java.util.stream.Collectors;

import org.omnaest.utils.ListUtils;

public class RandomEvictionStrategy implements EvictionStrategyHandler
{
    @Override
    public Set<String> determineEvictKeys(int volume, Set<String> keys)
    {
        return ListUtils.shuffled(keys)
                        .stream()
                        .limit(volume)
                        .collect(Collectors.toSet());
    }
}
