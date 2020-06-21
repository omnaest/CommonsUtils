package org.omnaest.utils.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.optional.NullOptional;

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
     * Gets a data element based on the given reference identifier. Returns null if null is stored for the given id or if there is no entry for the given id.
     * Alternatively use {@link #get(Object)},
     * 
     * @see #get(Object)
     * @see #getValueOrDefault(Object, Object)
     * @param id
     * @return
     */
    public default D getValue(I id)
    {
        return this.get(id)
                   .orElse(null);
    }

    //public Optional<D> get(I id);

    /**
     * Tests if the given id is present
     * 
     * @param id
     * @return
     */
    public default boolean containsId(I id)
    {
        return this.get(id)
                   .isPresent();
    }

    /**
     * Returns the value for the given id. Returns an {@link NullOptional#empty()} if no entry is available for the given reference identifier but will return a
     * present {@link NullOptional} with a null value if there is an entry.
     * 
     * @see #getValue(Object)
     * @see #getValueOrDefault(Object, Object)
     * @param id
     * @return
     */
    public NullOptional<D> get(I id);

    /**
     * Returns a {@link Stream} of available ids
     * 
     * @return
     */
    public default Stream<I> ids()
    {
        return this.ids(IdOrder.ARBITRARY);
    }

    /**
     * Returns a {@link Stream} of available ids in the given {@link IdOrder}
     * 
     * @param idOrder
     * @return
     */
    public Stream<I> ids(IdOrder idOrder);

    public static enum IdOrder
    {
        ARBITRARY, FROM_NEWEST_TO_OLDEST, FROM_OLDEST_TO_NEWEST
    }

    /**
     * Returns the entries of the repository
     * 
     * @return
     */
    public default Stream<BiElement<I, D>> entries()
    {
        return this.ids()
                   .map(id -> BiElement.of(id, this.getValue(id)));
    }

    /**
     * Returns the size of the {@link ElementRepository}
     * 
     * @see #addAll(Object)
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
     * Gets an element by its id or returns the default element if there is no stored entry or if the stored element would be null.
     * 
     * @see #getValue(Object)
     * @see #getOrDefault(Object, Object)
     * @param id
     * @param defaultElement
     * @return
     */
    public default D getValueOrDefault(I id, D defaultElement)
    {
        D retval = this.getValue(id);

        if (retval == null)
        {
            retval = defaultElement;
        }

        return retval;
    }

    /**
     * Gets an element by its id or returns the default element if no entry is present. This can also return null, if the stored value for an id is null.
     * 
     * @see #get(Collection)
     * @see #getValueOrDefault(Object, Object)
     * @param id
     * @param defaultElement
     * @return
     */
    public default D getOrDefault(I id, D defaultElement)
    {
        return this.get(id)
                   .orElse(defaultElement);
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
                                .collect(Collectors.toMap(id -> id, id -> this.getValue(id)))
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
                   .map(id -> BiElement.of(id, this.getValue(id)));
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
