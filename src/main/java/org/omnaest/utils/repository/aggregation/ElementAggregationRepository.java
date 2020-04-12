package org.omnaest.utils.repository.aggregation;

import java.util.List;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

public interface ElementAggregationRepository<I, D> extends AutoCloseable
{
    public Stream<D> get(I id);

    public void add(I id, D element);

    public void addAll(List<BiElement<I, D>> list);

    public Stream<I> ids();

    public void remove(I id);

    public ElementAggregationRepository<I, D> clear();

    public long size();

    /**
     * Returns an {@link ElementAggregationRepository} based on a {@link List}
     * 
     * @param list
     * @return
     */
    public static <I, D> ElementAggregationRepository<I, D> of(List<BiElement<I, D>> list)
    {
        return new ListElementAggregationRepository<>(list);
    }

    @Override
    public default void close()
    {
        //do nothing
    }
}