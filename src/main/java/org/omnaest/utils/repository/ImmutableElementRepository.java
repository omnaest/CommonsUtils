package org.omnaest.utils.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * Immutable {@link ElementRepository}
 * 
 * @author omnaest
 * @param <I>
 * @param <D>
 */
public interface ImmutableElementRepository<I, D> extends Iterable<BiElement<I, D>>
{
    /**
     * Gets a data element based on the given reference identifier
     * 
     * @param id
     * @return
     */
    public D get(I id);

    /**
     * Tests if the given id is present
     * 
     * @param id
     * @return
     */
    public default boolean containsId(I id)
    {
        return this.get(id) != null;
    }

    /**
     * Same as {@link #get(Object)} but returns an {@link Optional}
     * 
     * @param id
     * @return
     */
    public default Optional<D> getValue(I id)
    {
        return Optional.ofNullable(this.get(id));
    }

    /**
     * Returns a {@link Stream} of available ids
     * 
     * @return
     */
    public Stream<I> ids();

    /**
     * Returns the entries of the repository
     * 
     * @return
     */
    public default Stream<BiElement<I, D>> entries()
    {
        return this.ids()
                   .map(id -> BiElement.of(id, this.get(id)));
    }

    /**
     * Returns the size of the {@link ElementRepository}
     * 
     * @see #add(Object)
     * @return
     */
    public long size();

    /**
     * Returns true if this {@link IndexElementRepository} is empty
     * 
     * @return
     */
    public default boolean isEmpty()
    {
        return this.size() == 0;
    }

    /**
     * Gets an element by its id or returns the default element if the retrieved element would be null.
     * 
     * @param id
     * @param defaultElement
     * @return
     */
    public default D getOrDefault(I id, D defaultElement)
    {
        D retval = this.get(id);

        if (retval == null)
        {
            retval = defaultElement;
        }

        return retval;
    }

    /**
     * Returns a {@link Map} of the given ids and their values
     * 
     * @param ids
     * @return
     */
    public default Map<I, D> get(Collection<I> ids)
    {
        return ids != null ? ids.stream()
                                .collect(Collectors.toMap(id -> id, id -> this.get(id)))
                : Collections.emptyMap();
    }

    @Override
    public default Iterator<BiElement<I, D>> iterator()
    {
        return this.stream()
                   .iterator();
    }

    /**
     * Returns a {@link Stream} of {@link BiElement}s which contains the id and the value.
     * 
     * @return
     */
    public default Stream<BiElement<I, D>> stream()
    {
        return this.ids()
                   .map(id -> BiElement.of(id, this.get(id)));
    }

    /**
     * Returns a {@link Stream} of all values
     * 
     * @return
     */
    public default Stream<D> values()
    {
        return this.stream()
                   .map(BiElement::getSecond);
    }
}
