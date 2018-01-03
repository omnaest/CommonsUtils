package org.omnaest.utils.repository.map;

import java.util.Map;

import org.omnaest.utils.repository.ElementRepository;

/**
 * A {@link RepositoryMap} is a {@link Map} where the key and values are stored in a {@link ElementRepository} to save memory. Only the information necessary to
 * build up the internal hash or tree structure remains in memory.
 * 
 * @see ConcurrentRepositoryHashMap
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public interface RepositoryMap<K, V> extends Map<K, V>
{

}
