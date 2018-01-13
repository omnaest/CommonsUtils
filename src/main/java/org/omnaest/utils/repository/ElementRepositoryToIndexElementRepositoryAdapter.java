package org.omnaest.utils.repository;

import java.util.stream.LongStream;

public class ElementRepositoryToIndexElementRepositoryAdapter<D> implements IndexElementRepository<D>
{
    private ElementRepository<Long, D> elementRepository;

    public ElementRepositoryToIndexElementRepositoryAdapter(ElementRepository<Long, D> elementRepository)
    {
        super();
        this.elementRepository = elementRepository;
    }

    @Override
    public Long add(D element)
    {
        return this.elementRepository.add(element);
    }

    @Override
    public void update(Long id, D element)
    {
        this.elementRepository.update(id, element);
    }

    @Override
    public void delete(Long id)
    {
        this.elementRepository.delete(id);
    }

    @Override
    public D get(Long id)
    {
        return this.elementRepository.get(id);
    }

    @Override
    public IndexElementRepository<D> clear()
    {
        this.elementRepository.clear();
        return this;
    }

    @Override
    public IndexElementRepository<D> asWeakCached()
    {
        return IndexElementRepository.of(this.elementRepository.asWeakCached());
    }

    @Override
    public IndexElementRepository<D> asSynchronized()
    {
        return IndexElementRepository.of(this.elementRepository.asSynchronized());
    }

    @Override
    public long size()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public LongStream ids()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndexElementRepository<D> close()
    {
        throw new UnsupportedOperationException();
    }

}
