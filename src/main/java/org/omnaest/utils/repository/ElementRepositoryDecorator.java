package org.omnaest.utils.repository;

import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * @see ElementRepository
 * @author omnaest
 * @param <I>
 * @param <D>
 */
public abstract class ElementRepositoryDecorator<I, D> implements ElementRepository<I, D>
{
    protected ElementRepository<I, D> elementRepository;

    public ElementRepositoryDecorator(ElementRepository<I, D> elementRepository)
    {
        super();
        this.elementRepository = elementRepository;
    }

    @Override
    public Stream<I> ids()
    {
        return this.elementRepository.ids();
    }

    @Override
    public I add(D element)
    {
        return this.elementRepository.add(element);
    }

    @Override
    public void put(I id, D element)
    {
        this.elementRepository.put(id, element);
    }

    @Override
    public void putAll(Map<I, D> map)
    {
        this.elementRepository.putAll(map);
    }

    @Override
    public Stream<I> add(Stream<D> elements)
    {
        return this.elementRepository.add(elements);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<I> add(D... elements)
    {
        return this.elementRepository.add(elements);
    }

    @Override
    public D computeIfAbsent(I id, Supplier<D> supplier)
    {
        return this.elementRepository.computeIfAbsent(id, supplier);
    }

    @Override
    public ElementRepository<I, D> update(I id, UnaryOperator<D> updateFunction)
    {
        return this.elementRepository.update(id, updateFunction);
    }

    @Override
    public ElementRepository<I, D> computeIfAbsentAndUpdate(I id, Supplier<D> supplier, UnaryOperator<D> updateFunction)
    {
        return this.elementRepository.computeIfAbsentAndUpdate(id, supplier, updateFunction);
    }

    @Override
    public Stream<BiElement<I, D>> entries()
    {
        return this.elementRepository.entries();
    }

    @Override
    public void remove(I id)
    {
        this.elementRepository.remove(id);
    }

    @Override
    public D get(I id)
    {
        return this.elementRepository.get(id);
    }

    @Override
    public ElementRepository<I, D> clear()
    {
        return this.elementRepository.clear();
    }

    @Override
    public long size()
    {
        return this.elementRepository.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.elementRepository.isEmpty();
    }

    @Override
    public void close()
    {
        this.elementRepository.close();
    }

}
