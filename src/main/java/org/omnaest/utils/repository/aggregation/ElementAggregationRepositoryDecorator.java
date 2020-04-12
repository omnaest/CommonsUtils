package org.omnaest.utils.repository.aggregation;

import java.util.List;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

public class ElementAggregationRepositoryDecorator<I, D> implements ElementAggregationRepository<I, D>
{
    protected ElementAggregationRepository<I, D> repository;

    public ElementAggregationRepositoryDecorator(ElementAggregationRepository<I, D> repository)
    {
        super();
        this.repository = repository;
    }

    @Override
    public Stream<D> get(I id)
    {
        return this.repository.get(id);
    }

    @Override
    public void add(I id, D element)
    {
        this.repository.add(id, element);
    }

    @Override
    public void addAll(List<BiElement<I, D>> list)
    {
        this.repository.addAll(list);
    }

    @Override
    public Stream<I> ids()
    {
        return this.repository.ids();
    }

    @Override
    public void remove(I id)
    {
        this.repository.remove(id);
    }

    @Override
    public ElementAggregationRepository<I, D> clear()
    {
        return this.repository.clear();
    }

    @Override
    public long size()
    {
        return this.repository.size();
    }

}
