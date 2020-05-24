package org.omnaest.utils.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ElementRepositoryUtils
{

    private ElementRepositoryUtils()
    {
        super();
    }

    public static <I, D> ElementRepository<I, D> newConcurrentHashMapElementRepository(Supplier<I> idSupplier)
    {
        return ElementRepository.of(new ConcurrentHashMap<>(), idSupplier);
    }

    public static <D> IndexElementRepository<D> newConcurrentHashMapIndexElementRepository()
    {
        return ElementRepository.of(new ConcurrentHashMap<>());
    }

}
