package org.omnaest.utils.repository;

import java.util.Arrays;
import java.util.stream.Stream;

public interface AppendableElementRepository<I, D>
{
    /**
     * Adds a new data element to the {@link ElementRepository} and returns its reference identifier
     * 
     * @param element
     * @return
     */
    public I add(D element);

    /**
     * Adds multiple elements
     * 
     * @param elements
     * @return
     */
    public default Stream<I> add(Stream<D> elements)
    {
        return elements.map(this::add);
    }

    /**
     * @see #add(Stream)
     * @param elements
     * @return
     */
    @SuppressWarnings("unchecked")
    public default Stream<I> add(D... elements)
    {
        return this.add(Arrays.asList(elements)
                              .stream());
    }
}
