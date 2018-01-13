package org.omnaest.utils.repository.map;

import java.util.Map;

import org.omnaest.utils.repository.ElementRepository;

/**
 * A {@link ElementRepositoryMap} is a {@link Map} where the key and values are stored in a {@link ElementRepository} to save memory. Only the information
 * necessary to
 * build up the internal hash or tree structure remains in memory.
 * 
 * @see ConcurrentRepositoryHashMap
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public interface ElementRepositoryMap<K, V> extends Map<K, V>
{

    /**
     * Returns an {@link ElementRepositoryMap} based on a {@link ElementRepository} for the keys and an {@link ElementRepository} for the values
     * 
     * @param keyRepository
     * @param valueRepository
     * @return
     */
    public static <K, V, I1, I2> ElementRepositoryMap<K, V> of(ElementRepository<I1, K> keyRepository, ElementRepository<I2, V> valueRepository)
    {
        return new BiElementRepositoryMap<>(keyRepository, valueRepository);
    }

    /**
     * Calls {@link ElementRepository#close()} on both underlying {@link ElementRepository}s
     */
    public void close();
}
