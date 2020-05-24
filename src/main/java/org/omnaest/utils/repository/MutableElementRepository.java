package org.omnaest.utils.repository;

import java.util.Map;

public interface MutableElementRepository<I, D>
{
    /**
     * Updates a data element where the id is already known
     * 
     * @param id
     * @param element
     * @return
     */
    public void put(I id, D element);

    /**
     * Puts all elements from the given {@link Map} into the {@link ElementRepository}
     * 
     * @param map
     */
    public default void putAll(Map<I, D> map)
    {
        if (map != null)
        {
            map.forEach(this::put);
        }
    }

    /**
     * Deletes a data element by its id
     * 
     * @param id
     * @return
     */
    public void remove(I id);

    /**
     * Clears the {@link ElementRepository}
     * 
     * @return this
     */
    public MutableElementRepository<I, D> clear();
}
