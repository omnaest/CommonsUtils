package org.omnaest.utils.repository.map;

import java.util.concurrent.ConcurrentHashMap;

import org.omnaest.utils.repository.ElementRepository;

public class KeyElementRepositoryMap<K, V> extends BiElementRepositoryMap<K, V>
{

    public <I> KeyElementRepositoryMap(ElementRepository<I, K> keyRepository)
    {
        super(keyRepository, ElementRepository.of(new ConcurrentHashMap<>()));
    }

}
