package org.omnaest.utils.repository;

/**
 * Synchronized {@link ElementRepository} decorator
 * 
 * @author omnaest
 * @param <I>
 * @param <D>
 */
public class SynchronizedElementRepository<I, D> extends ElementRepositoryDecorator<I, D>
{

    public SynchronizedElementRepository(ElementRepository<I, D> elementRepository)
    {
        super(elementRepository);
    }

    @Override
    public synchronized I add(D element)
    {
        return super.add(element);
    }

    @Override
    public synchronized void update(I id, D element)
    {
        super.update(id, element);
    }

    @Override
    public synchronized void delete(I id)
    {
        super.delete(id);
    }

    @Override
    public synchronized D get(I id)
    {
        return super.get(id);
    }

    @Override
    public synchronized ElementRepository<I, D> clear()
    {
        return super.clear();
    }

    @Override
    public String toString()
    {
        return "SynchronizedElementRepository []";
    }

}
