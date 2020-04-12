package org.omnaest.utils.repository.aggregation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.supplier.SupplierConsumer;

public class ListElementAggregationRepository<I, D> implements ElementAggregationRepository<I, D>
{
    private List<BiElement<I, D>> list;
    private SupplierConsumer<I>   idSupplier;

    public ListElementAggregationRepository(List<BiElement<I, D>> list)
    {
        super();
        this.list = list;
        this.idSupplier = this.idSupplier;
    }

    @Override
    public Stream<D> get(I id)
    {
        return this.list.stream()
                        .filter(bi -> Objects.equals(id, bi.getFirst()))
                        .map(bi -> bi.getSecond());
    }

    @Override
    public void add(I id, D element)
    {
        this.list.add(BiElement.of(id, element));
    }

    @Override
    public void addAll(List<BiElement<I, D>> list)
    {
        this.list.addAll(list);
    }

    @Override
    public Stream<I> ids()
    {
        return this.list.stream()
                        .map(bi -> bi.getFirst());
    }

    @Override
    public void remove(I id)
    {
        this.list.stream()
                 .filter(bi -> Objects.equals(id, bi.getFirst()))
                 .collect(Collectors.toList())
                 .forEach(bi -> this.list.remove(bi));
    }

    @Override
    public ElementAggregationRepository<I, D> clear()
    {
        this.list.clear();
        return this;
    }

    @Override
    public long size()
    {
        return this.list.size();
    }

}
