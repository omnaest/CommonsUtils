package org.omnaest.utils.repository.map;

import java.util.concurrent.ConcurrentHashMap;

import org.omnaest.utils.MapUtils;
import org.omnaest.utils.repository.ElementRepository;

/**
 * {@link ElementRepositoryMap} which is based upon a {@link ConcurrentHashMap}
 * 
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public class ConcurrentRepositoryHashMap<K, V> extends AbstractRepositoryMap<K, V>
{
    public ConcurrentRepositoryHashMap(ElementRepository<Long, K> keyElementRepository, ElementRepository<Long, V> valueElementRepository)
    {
        super(MapUtils.newConcurrentHashSupplierMap(s -> (Resolver<K>) new SupplierWrappingResolver<K>(s),
                                                    s -> (Resolver<V>) new SupplierWrappingResolver<V>(s))
                      .useSoftReferenceCache(),
                keyElementRepository, valueElementRepository);
    }
}
