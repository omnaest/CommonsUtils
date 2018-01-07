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
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@link ElementRepository} does define accessors for a data element with a reference identifier
 * 
 * @author omnaest
 * @param <I>
 *            reference identifier
 * @param <D>
 *            data element
 */
public interface ElementRepository<I, D>
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
     * Returns the next id the {@link ElementRepository} would return for a new element
     * 
     * @see #add(Object)
     * @return
     */
    public I peekNextId();

    /**
     * Clears the {@link ElementRepository}
     * 
     * @return this
     */
    public ElementRepository<I, D> clear();

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
     * Returns a new {@link ElementRepository} based on the folder structure of the given {@link File} directory
     * 
     * @param directory
     * @param type
     * @return
     */
    public static <D> ElementRepository<Long, D> of(File directory, Class<D> type)
    {
        return new DirectoryElementRepository<D>(directory, type);
    }

}
