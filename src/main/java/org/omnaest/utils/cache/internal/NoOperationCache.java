package org.omnaest.utils.cache.internal;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.omnaest.utils.cache.Cache;

/**
 * {@link Cache} implementation which is always empty and stores nothing.<br>
 * <br>
 * {@link #computeIfAbsent(String, Supplier, Class)} returns always a new object using the given {@link Supplier}.
 * 
 * @author omnaest
 */
public class NoOperationCache extends AbstractCache
{
    @Override
    public <V> V get(String key, Class<V> type)
    {
        return null;
    }

    @Override
    public <V> Class<V> getType(String key)
    {
        return null;
    }

    @Override
    public void put(String key, Object value)
    {
        // no operation
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return Optional.ofNullable(supplier)
                       .map(Supplier::get)
                       .orElse(null);
    }

    @Override
    public void remove(String key)
    {
        // no operation
    }

    @Override
    public Set<String> keySet()
    {
        return Collections.emptySet();
    }

}
