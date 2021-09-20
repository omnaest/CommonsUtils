package org.omnaest.utils.cache;

import java.util.function.Supplier;

import org.omnaest.utils.supplier.SupplierConsumer;

public interface SingleElementCache<V> extends SupplierConsumer<V>
{
    public V computeIfAbsent(Supplier<V> supplier);
}
