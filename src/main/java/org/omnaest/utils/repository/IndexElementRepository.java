package org.omnaest.utils.repository;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.omnaest.utils.repository.internal.DirectoryElementRepository;
import org.omnaest.utils.repository.internal.DispatchingIndexElementRepository;
import org.omnaest.utils.repository.internal.ElementRepositoryToIndexElementRepositoryAdapter;
import org.omnaest.utils.repository.internal.IndexElementRepositoryList;
import org.omnaest.utils.repository.internal.MapElementRepository;

/**
 * {@link ElementRepository} which uses a {@link Long} value as key
 * 
 * @author omnaest
 * @param <D>
 */
public interface IndexElementRepository<D> extends ElementRepository<Long, D>
{

    @Override
    public IndexElementRepository<D> clear();

    /**
     * Returns a {@link IndexElementRepository} for the given {@link ElementRepository} which has {@link Long} as identifier {@link Class} type
     * 
     * @param elementRepository
     * @return
     */
    public static <D> IndexElementRepository<D> of(ElementRepository<Long, D> elementRepository)
    {
        return new ElementRepositoryToIndexElementRepositoryAdapter<>(elementRepository);
    }

    /**
     * Returns an {@link IndexElementRepository} based on the given {@link Map} using a {@link Long} id {@link Supplier}
     * 
     * @param map
     * @return
     */
    public static <D> IndexElementRepository<D> of(Map<Long, D> map)
    {
        return IndexElementRepository.of(new MapElementRepository<>(map, new Supplier<Long>()
        {
            private AtomicLong counter = new AtomicLong();

            @Override
            public Long get()
            {
                return counter.getAndIncrement();
            }
        }));
    }

    /**
     * Returns a new {@link IndexElementRepository} based on the folder structure of the given {@link File} directory
     * 
     * @param directory
     * @param type
     * @return
     */
    public static <D> IndexElementRepository<D> of(File directory, Class<D> type)
    {
        return new DirectoryElementRepository<D>(directory, type);
    }

    /**
     * Returns a new {@link IndexElementRepository} which uses the given {@link Supplier} factory to create new repositories if the given repositories exceed
     * the given upperSize limit.
     * 
     * @param repositoryFactory
     * @param upperSize
     * @return
     */
    public static <D> IndexElementRepository<D> of(Supplier<IndexElementRepository<D>> repositoryFactory, long upperSize)
    {
        return new DispatchingIndexElementRepository<>(repositoryFactory, upperSize);
    }

    @Override
    public default IndexElementRepository<D> asWeakCached()
    {
        return of(ElementRepository.super.asWeakCached());
    }

    @Override
    public default IndexElementRepository<D> asSynchronized()
    {
        return of(ElementRepository.super.asSynchronized());
    }

    /**
     * Returns a {@link List} wrapper which allows to access this {@link IndexElementRepository} via the {@link List} inteface.
     * 
     * @return
     */
    public default List<D> asList()
    {
        return new IndexElementRepositoryList<>(this);
    }
}
