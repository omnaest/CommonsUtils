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
        I id = super.add(element);
        this.cache.put(id, element);
        return id;
    }

    @Override
    public void update(I id, D element)
    {
        this.cache.put(id, element);
        super.update(id, element);
    }

    @Override
    public void delete(I id)
    {
        this.cache.remove(id);
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

    @Override
    public String toString()
    {
        return "WeakHashMapDecoratingElementRepository [cache=" + this.cache + "]";
    }

}
