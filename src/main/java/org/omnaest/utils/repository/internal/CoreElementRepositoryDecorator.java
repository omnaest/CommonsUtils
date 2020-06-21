package org.omnaest.utils.repository.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.CoreElementRepository;

public class CoreElementRepositoryDecorator<I, D> implements CoreElementRepository<I, D>
{
    protected CoreElementRepository<I, D> elementRepository;

    public CoreElementRepositoryDecorator(CoreElementRepository<I, D> elementRepository)
    {
        super();
        this.elementRepository = elementRepository;
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
    public D getValue(I id)
    {
        return this.elementRepository.getValue(id);
    }

    @Override
    public void remove(I id)
    {
        this.elementRepository.remove(id);
    }

    @Override
    public boolean containsId(I id)
    {
        return this.elementRepository.containsId(id);
    }

    @Override
    public CoreElementRepository<I, D> clear()
    {
        this.elementRepository.clear();
        return this;
    }

    @Override
    public NullOptional<D> get(I id)
    {
        return this.elementRepository.get(id);
    }

    @Override
    public void forEach(Consumer<? super BiElement<I, D>> action)
    {
        this.elementRepository.forEach(action);
    }

    @Override
    public Stream<I> ids(IdOrder idOrder)
    {
        return this.elementRepository.ids(idOrder);
    }

    @Override
    public Stream<BiElement<I, D>> entries()
    {
        return this.elementRepository.entries();
    }

    @Override
    public void close()
    {
        this.elementRepository.close();
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
    public Spliterator<BiElement<I, D>> spliterator()
    {
        return this.elementRepository.spliterator();
    }

    @Override
    public D getValueOrDefault(I id, D defaultElement)
    {
        return this.elementRepository.getValueOrDefault(id, defaultElement);
    }

    @Override
    public Map<I, D> get(Collection<I> ids)
    {
        return this.elementRepository.get(ids);
    }

    @Override
    public Iterator<BiElement<I, D>> iterator()
    {
        return this.elementRepository.iterator();
    }

    @Override
    public Stream<BiElement<I, D>> stream()
    {
        return this.elementRepository.stream();
    }

    @Override
    public Stream<D> values()
    {
        return this.elementRepository.values();
    }

}
