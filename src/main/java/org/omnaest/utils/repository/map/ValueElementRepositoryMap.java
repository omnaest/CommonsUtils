package org.omnaest.utils.repository.map;

import java.util.concurrent.ConcurrentHashMap;

import org.omnaest.utils.repository.ElementRepository;

public class ValueElementRepositoryMap<K, V> extends BiElementRepositoryMap<K, V>
{

    public <I> ValueElementRepositoryMap(ElementRepository<I, V> valueRepository)
    {
        super(ElementRepository.of(new ConcurrentHashMap<>()), valueRepository);
    }

}
