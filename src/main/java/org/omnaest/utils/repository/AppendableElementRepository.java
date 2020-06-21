package org.omnaest.utils.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public default Stream<I> addAll(Stream<D> elements)
    {
        return Optional.ofNullable(elements)
                       .orElse(Stream.empty())
                       .map(this::add);
    }

    /**
     * Adds multiple elements
     * 
     * @param elements
     * @return
     */
    public default List<I> addAll(Collection<D> elements)
    {
        return this.addAll(Optional.ofNullable(elements)
                                   .orElse(Collections.emptyList())
                                   .stream())
                   .collect(Collectors.toList());
    }

    /**
     * @see #addAll(Stream)
     * @param elements
     * @return
     */
    @SuppressWarnings("unchecked")
    public default Stream<I> addAll(D... elements)
    {
        return this.addAll(Arrays.asList(elements)
                                 .stream());
    }
}
