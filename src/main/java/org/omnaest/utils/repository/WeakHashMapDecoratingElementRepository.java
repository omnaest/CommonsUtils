package org.omnaest.utils.repository;

import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapDecoratingElementRepository<I, D> extends ElementRepositoryDecorator<I, D>
{
    private Map<I, D> cache = new WeakHashMap<>();

    public WeakHashMapDecoratingElementRepository(ElementRepository<I, D> elementRepository)
    {
        super(elementRepository);
    }

    @Override
    public I add(D element)
    {
        return super.add(element);
    }

    @Override
    public void update(I id, D element)
    {
        this.clearCacheFor(id);
        super.update(id, element);
    }

    private void clearCacheFor(I id)
    {
        this.cache.remove(id);
    }

    @Override
    public void delete(I id)
    {
        this.clearCacheFor(id);
        super.delete(id);
    }

    @Override
    public D get(I id)
    {
        D retval = this.cache.get(id);

        if (retval == null)
        {
            retval = super.get(id);
            this.cache.put(id, retval);
        }

        return retval;
    }

    @Override
    public ElementRepository<I, D> clear()
    {
        this.cache.clear();
        return super.clear();
    }

}
