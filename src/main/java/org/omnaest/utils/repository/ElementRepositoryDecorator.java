package org.omnaest.utils.repository;

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
    public I peekNextId()
    {
        return this.elementRepository.peekNextId();
    }

    @Override
    public ElementRepository<I, D> clear()
    {
        return this.elementRepository.clear();
    }

}