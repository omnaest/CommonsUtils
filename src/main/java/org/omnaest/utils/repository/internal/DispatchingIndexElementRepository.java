package org.omnaest.utils.repository.internal;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.ListUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.IndexElementRepository;

/**
 * An {@link IndexElementRepository} which does dispatch the calls to multiple {@link IndexElementRepository}s.<br>
 * The {@link DispatchingIndexElementRepository} defines upper size limits for each single {@link IndexElementRepository}, if those size limits are exceeded a
 * new {@link IndexElementRepository} is generated from the given {@link Supplier}
 * 
 * @author omnaest
 * @param <D>
 */
public class DispatchingIndexElementRepository<D> implements IndexElementRepository<D>
{
    private Supplier<IndexElementRepository<D>> repositoryFactory;
    private List<IndexElementRepository<D>>     repositories;
    private long                                upperSize;

    public DispatchingIndexElementRepository(Supplier<IndexElementRepository<D>> repositoryFactory, long upperSize)
    {
        super();
        this.repositoryFactory = repositoryFactory;
        this.upperSize = upperSize;

        this.repositories = StreamUtils.fromSupplier(repositoryFactory)
                                       .withTerminationMatcherInclusive(repository -> repository.isEmpty())
                                       .collect(Collectors.toList());
    }

    @Override
    public Long add(D element)
    {
        //
        ListUtils.ensureSize(this.repositories, 1, this.repositoryFactory);
        long id = (this.repositories.size() - 1) * this.upperSize + ListUtils.last(this.repositories)
                                                                             .size();

        //
        BiElement<Integer, Long> repositoryIndexAndId = this.determineRepositoryIndexAndId(id);
        int repositoryIndex = repositoryIndexAndId.getFirst();
        this.repositories = ListUtils.ensureSize(this.repositories, repositoryIndex + 1, this.repositoryFactory);
        IndexElementRepository<D> repository = ListUtils.get(this.repositories, repositoryIndex);
        Long retval = repository.add(element) + repositoryIndex * this.upperSize;
        return retval;

    }

    @Override
    public void put(Long id, D element)
    {
        this.executeOnSingleRepository((repository, reducedId) -> repository.put(reducedId, element), id);
    }

    @Override
    public void remove(Long id)
    {
        this.executeOnSingleRepository(IndexElementRepository<D>::remove, id);
    }

    @Override
    public NullOptional<D> get(Long id)
    {
        return this.executeOnSingleRepositoryAndGet(IndexElementRepository<D>::get, id);
    }

    private static interface RepositoryAction<D, R>
    {
        public R run(IndexElementRepository<D> repository, long id);
    }

    private static interface RepositoryVoidAction<D>
    {
        public void run(IndexElementRepository<D> repository, long id);
    }

    private void executeOnSingleRepository(RepositoryVoidAction<D> action, Long id)
    {
        this.executeOnSingleRepositoryAndGet((repository, reducedId) ->
        {
            action.run(repository, reducedId);
            return null;
        }, id);
    }

    private <R> R executeOnSingleRepositoryAndGet(RepositoryAction<D, R> action, Long id)
    {
        //
        BiElement<Integer, Long> repositoryIndexAndId = this.determineRepositoryIndexAndId(id);
        int repositoryIndex = repositoryIndexAndId.getFirst();
        long reducedId = repositoryIndexAndId.getSecond();
        this.repositories = ListUtils.ensureSize(this.repositories, repositoryIndex + 1, this.repositoryFactory);
        IndexElementRepository<D> repository = ListUtils.get(this.repositories, repositoryIndex);
        return action.run(repository, reducedId);
    }

    private BiElement<Integer, Long> determineRepositoryIndexAndId(Long id)
    {
        int repositoryIndex = (int) (id / this.upperSize);
        long reducedId = id % this.upperSize;
        return BiElement.of(repositoryIndex, reducedId);
    }

    @Override
    public long size()
    {
        return this.repositories.stream()
                                .mapToLong(IndexElementRepository::size)
                                .sum();
    }

    @Override
    public Stream<Long> ids(IdOrder idOrder)
    {
        return this.repositories.stream()
                                .flatMap(repository -> repository.ids(idOrder));
    }

    @Override
    public IndexElementRepository<D> clear()
    {
        this.repositories.forEach(IndexElementRepository<D>::clear);
        return this;
    }

    @Override
    public void close()
    {
        this.repositories.stream()
                         .parallel()
                         .forEach(IndexElementRepository<D>::close);
    }

}
