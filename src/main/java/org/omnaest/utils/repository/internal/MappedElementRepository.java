package org.omnaest.utils.repository.internal;

import java.util.stream.Stream;

import org.omnaest.utils.functional.BidirectionalFunction;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementRepository;

public class MappedElementRepository<I, D, OD> implements ElementRepository<I, D>
{
    protected ElementRepository<I, OD>     elementRepository;
    protected BidirectionalFunction<OD, D> mapper;

    public MappedElementRepository(ElementRepository<I, OD> elementRepository, BidirectionalFunction<OD, D> mapper)
    {
        super();
        this.elementRepository = elementRepository;
        this.mapper = mapper;
    }

    @Override
    public NullOptional<D> get(I id)
    {
        return this.elementRepository.get(id)
                                     .map(this.mapper.forward());
    }

    @Override
    public Stream<I> ids(IdOrder idOrder)
    {
        return this.elementRepository.ids(idOrder);
    }

    @Override
    public long size()
    {
        return this.elementRepository.size();
    }

    @Override
    public void put(I id, D element)
    {
        this.elementRepository.put(id, this.mapper.backward()
                                                  .apply(element));
    }

    @Override
    public void remove(I id)
    {
        this.elementRepository.remove(id);
    }

    @Override
    public I add(D element)
    {
        return this.elementRepository.add(this.mapper.backward()
                                                     .apply(element));
    }

    @Override
    public ElementRepository<I, D> clear()
    {
        this.elementRepository.clear();
        return this;
    }

}
