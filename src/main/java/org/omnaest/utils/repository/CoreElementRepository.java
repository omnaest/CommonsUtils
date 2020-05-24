package org.omnaest.utils.repository;

import java.util.function.Supplier;

/**
 * @see ElementRepository
 * @author omnaest
 * @param <I>
 * @param <D>
 */
public interface CoreElementRepository<I, D> extends ImmutableElementRepository<I, D>, MutableElementRepository<I, D>, AutoCloseable
{
    /**
     * Closes the underlying repository. This is an optional method.
     */
    @Override
    public default void close()
    {
        //do nothing
    }

    /**
     * Returns an {@link ElementRepository} with the given id {@link Supplier}
     * 
     * @param idSupplier
     * @return
     */
    public default ElementRepository<I, D> toElementRepository(Supplier<I> idSupplier)
    {
        return ElementRepository.from(this, idSupplier);
    }

    @Override
    public CoreElementRepository<I, D> clear();
}
