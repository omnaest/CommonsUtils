package org.omnaest.utils.cache;

import java.util.Set;

public interface CacheBase
{
    public void remove(String key);

    public void removeAll(Iterable<String> keys);

    public Set<String> keySet();

    public boolean isEmpty();

    public int size();

    public void clear();
}
