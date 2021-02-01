package org.omnaest.utils.cache.internal;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.cache.UnaryCache;

public class UnaryCacheDecorator<V> implements UnaryCache<V>
{
    private UnaryCache<V> cache;

    public UnaryCacheDecorator(UnaryCache<V> cache)
    {
        super();
        this.cache = cache;
    }

    @Override
    public V get(String key)
    {
        return this.cache.get(key);
    }

    @Override
    public Supplier<V> getSupplier(String key)
    {
        return this.cache.getSupplier(key);
    }

    @Override
    public void put(String key, V value)
    {
        this.cache.put(key, value);
    }

    @Override
    public V computeIfAbsent(String key, Supplier<V> supplier)
    {
        return this.cache.computeIfAbsent(key, supplier);
    }

    @Override
    public V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction)
    {
        return this.cache.computeIfAbsentOrUpdate(key, supplier, updateFunction);
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet();
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        this.cache.removeAll(keys);
    }

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public int size()
    {
        return this.cache.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.cache.isEmpty();
    }

    @Override
    public void clear()
    {
        this.cache.clear();
    }

}
