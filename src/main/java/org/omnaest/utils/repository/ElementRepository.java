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
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
     * Updates a data element where the id is already known
     * 
     * @param id
     * @param element
     * @return
     */
    public void update(I id, D element);

    /**
     * Deletes a data element by its id
     * 
     * @param id
     * @return
     */
    public void delete(I id);

    /**
     * Gets a data element based on the given reference identifier
     * 
     * @param id
     * @return
     */
    public D get(I id);

    /**
     * Returns a {@link Stream} of available ids
     * 
     * @return
     */
    public Stream<I> ids();

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

}
