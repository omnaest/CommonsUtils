package org.omnaest.utils.repository.map;

import java.util.Map;
import java.util.function.Supplier;

import org.omnaest.utils.ObjectUtils;
import org.omnaest.utils.map.MappingMapDecorator;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.map.AbstractRepositoryMap.Resolver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @see RepositoryMap
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public class AbstractRepositoryMap<K, V> extends MappingMapDecorator<K, Resolver<K>, V, Resolver<V>> implements RepositoryMap<K, V>
{
    protected ElementRepository<Long, K> keyElementRepository;
    protected ElementRepository<Long, V> valueElementRepository;

    public static interface Resolver<E> extends Supplier<E>
    {
        public Resolver<E> withRepository(ElementRepository<Long, E> repository);
    }

    public static class ReadableResolver<E> implements Resolver<E>
    {
        private E element;

        public ReadableResolver(E element)
        {
            super();
            this.element = element;
        }

        @Override
        public E get()
        {
            return this.element;
        }

        @Override
        public String toString()
        {
            return "ReadableResolver [element=" + this.element + "]";
        }

        @Override
        public Resolver<E> withRepository(ElementRepository<Long, E> repository)
        {
            //do nothing
            return this;
        }

    }

    public static class WritableResolver<E> implements Resolver<E>
    {
        private long id;

        private ElementRepository<Long, E> repository;

        @JsonCreator
        public WritableResolver(long id)
        {
            super();
            this.id = id;
        }

        @Override
        public Resolver<E> withRepository(ElementRepository<Long, E> repository)
        {
            this.repository = repository;
            return this;
        }

        @SuppressWarnings("unused")
        public long getId()
        {
            return this.id;
        }

        @JsonIgnore
        @Override
        public E get()
        {
            return this.repository.get(this.id);
        }

        @Override
        public String toString()
        {
            return "Resolver [id=" + this.id + ", repository=" + this.repository + ", get()=" + this.get() + "]";
        }

    }

    public AbstractRepositoryMap(Map<Resolver<K>, Resolver<V>> sourceMap, ElementRepository<Long, K> keyElementRepository,
                                 ElementRepository<Long, V> valueElementRepository)
    {
        super(sourceMap, null, null, null, null, null, null);
        this.keyElementRepository = keyElementRepository;
        this.valueElementRepository = valueElementRepository;
        this.keyFromSourceMapper = ks -> ObjectUtils.getIfNotNull(ks, () -> ks.withRepository(this.keyElementRepository)
                                                                              .get());
        this.valueFromSourceMapper = vs -> ObjectUtils.getIfNotNull(vs, () -> vs.withRepository(this.valueElementRepository)
                                                                                .get());
        this.keyToReadableSourceMapper = k -> new ReadableResolver<K>(k);
        this.keyToWritableSourceMapper = k -> new WritableResolver<K>(this.keyElementRepository.put(k)).withRepository(keyElementRepository);
        this.valueToReadableSourceMapper = v -> new ReadableResolver<V>(v);
        this.valueToWritableSourceMapper = v -> new WritableResolver<V>(this.valueElementRepository.put(v)).withRepository(valueElementRepository);
    }

}