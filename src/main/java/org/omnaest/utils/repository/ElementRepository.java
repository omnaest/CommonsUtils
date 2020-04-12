/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils.repository;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * {@link ElementRepository} does define accessors for a data element with a reference identifier
 * 
 * @see #of(Map, Supplier)
 * @see #of(File, Class)
 * @see #asWeakCached()
 * @see #asSynchronized()
 * @see IndexElementRepository
 * @author omnaest
 * @param <I>
 *            reference identifier
 * @param <D>
 *            data element
 */
public interface ElementRepository<I, D> extends AutoCloseable
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
     * Gets a data element based on the given reference identifier
     * 
     * @param id
     * @return
     */
    public D get(I id);

    /**
     * Returns the element for the given id, but if the element is not available it calls the given {@link Supplier}, returns that retrieved element and adds it
     * to the {@link ElementRepository}
     * 
     * @param id
     * @param supplier
     * @return
     */
    public default D computeIfAbsent(I id, Supplier<D> supplier)
    {
        D element = this.get(id);
        if (element == null)
        {
            element = supplier.get();
            this.put(id, element);
        }
        return element;
    }

    /**
     * Gets an element by the given id and allows to update it by a {@link UnaryOperator}. After the {@link UnaryOperator} finishes the element will be written
     * back to the {@link ElementRepository}.
     * 
     * @param id
     * @param updateFunction
     * @return
     */
    public default ElementRepository<I, D> update(I id, UnaryOperator<D> updateFunction)
    {
        D element = this.get(id);
        element = updateFunction.apply(element);
        this.put(id, element);
        return this;
    }

    /**
     * Gets or creates an element by the given id and supplier and allows to update it by a {@link UnaryOperator}. After the {@link UnaryOperator} finished the
     * element will be written to the store.
     * 
     * @param id
     * @param supplier
     * @param updateFunction
     * @return
     */
    public default ElementRepository<I, D> computeIfAbsentAndUpdate(I id, Supplier<D> supplier, UnaryOperator<D> updateFunction)
    {
        D element = this.get(id);
        if (element == null)
        {
            element = supplier.get();
        }
        element = updateFunction.apply(element);
        this.put(id, element);
        return this;
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
     * Closes the underlying repository. This is an optional method.
     */
    @Override
    public default void close()
    {
        //do nothing
    }

    /**
     * Clears the {@link ElementRepository}
     * 
     * @return this
     */
    public ElementRepository<I, D> clear();

    /**
     * Returns a new {@link ElementRepository} wrapping the current one into a {@link WeakReference} cached structure
     * 
     * @return
     */
    public default ElementRepository<I, D> asWeakCached()
    {
        return new WeakHashMapDecoratingElementRepository<>(this);
    }

    /**
     * Returns a synchronized {@link ElementRepository} wrapping the current one
     * 
     * @return
     */
    public default ElementRepository<I, D> asSynchronized()
    {
        return new SynchronizedElementRepository<>(this);
    }

    /**
     * Returns a new {@link ElementRepository} based on the given {@link Map} and {@link Supplier} of reference ids
     * 
     * @param map
     * @param idSupplier
     * @return
     */
    public static <I, D> ElementRepository<I, D> of(Map<I, D> map, Supplier<I> idSupplier)
    {
        return new MapElementRepository<>(map, idSupplier);
    }

    /**
     * Returns an {@link IndexElementRepository} based on the given {@link Map} using a {@link Long} id {@link Supplier}
     * 
     * @param map
     * @return
     */
    public static <D> IndexElementRepository<D> of(Map<Long, D> map)
    {
        return IndexElementRepository.of(map);
    }

    /**
     * Returns a new {@link ElementRepository} based on the folder structure of the given {@link File} directory
     * 
     * @param directory
     * @param type
     * @return
     */
    public static <D> IndexElementRepository<D> of(File directory, Class<D> type)
    {
        return IndexElementRepository.of(directory, type);

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

}
