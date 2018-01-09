package org.omnaest.utils.repository;

import java.util.Arrays;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * {@link ElementRepository} which uses a {@link Long} value as key
 * 
 * @author omnaest
 * @param <D>
 */
public interface IndexElementRepository<D> extends ElementRepository<Long, D>
{
    /**
     * Returns the next id the {@link IndexElementRepository} would return for a new element
     * 
     * @see #add(Object)
     * @return
     */
    public long size();

    /**
     * Returns a {@link LongStream} of available ids
     * 
     * @return
     */
    public LongStream ids();

    /**
     * Adds multiple elements
     * 
     * @param elements
     * @return
     */
    public default LongStream add(Stream<D> elements)
    {
        return elements.mapToLong(this::add);
    }

    @SuppressWarnings("unchecked")
    public default LongStream add(D... elements)
    {
        return this.add(Arrays.asList(elements)
                              .stream());
    }

    @Override
    public IndexElementRepository<D> clear();

}
