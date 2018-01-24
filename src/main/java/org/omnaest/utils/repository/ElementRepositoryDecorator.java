package org.omnaest.utils.repository;

import java.util.stream.Stream;

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
    public void update(I id, D element)
    {
        this.elementRepository.update(id, element);
    }

    @Override
    public void delete(I id)
    {
        this.elementRepository.delete(id);
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
