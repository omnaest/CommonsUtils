package org.omnaest.utils.repository.internal;

import java.util.function.Supplier;

import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.repository.CoreElementRepository;
import org.omnaest.utils.repository.ElementRepository;

public class AppendableSupportedCoreElementRepositoryDecorator<I, D> extends CoreElementRepositoryDecorator<I, D> implements ElementRepository<I, D>
{
    private Supplier<I> idSupplier;

    public AppendableSupportedCoreElementRepositoryDecorator(CoreElementRepository<I, D> elementRepository, Supplier<I> idSupplier)
    {
        super(elementRepository);
        this.idSupplier = idSupplier;
    }

    @Override
    public I add(D element)
    {
        I id = StreamUtils.fromSupplier(this.idSupplier)
                          .filter(i -> !this.containsId(i))
                          .findFirst()
                          .get();
        this.put(id, element);
        return id;
    }

    @Override
    public ElementRepository<I, D> clear()
    {
        super.clear();
        return this;
    }

    @Override
    public String toString()
    {
        return "AppendableSupportedCoreElementRepositoryDecorator [idSupplier=" + this.idSupplier + "]";
    }

}
