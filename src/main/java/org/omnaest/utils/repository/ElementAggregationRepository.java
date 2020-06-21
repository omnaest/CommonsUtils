package org.omnaest.utils.repository;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.repository.ElementAggregationRepository.Entry;
import org.omnaest.utils.repository.internal.ElementAggregationRepositoryImpl;

public interface ElementAggregationRepository<I, D, M> extends ElementRepository<I, Entry<D, M>>
{
    public Stream<LoggedEntry<D, M>> getLog(I id);

    public void remove(I id, M meta);

    public static <I, D, M> ElementAggregationRepository<I, D, M> of(Factory factory, Class<I> idType, Class<D> dataType, Class<M> metaType)
    {
        return new ElementAggregationRepositoryImpl<>(factory, idType, dataType, metaType);
    }

    public static interface LoggedEntry<D, M> extends Entry<D, M>
    {
        public State getState();

        public static enum State
        {
            CREATED, UPDATED, DELETED
        }
    }

    public static interface Entry<D, M> extends Supplier<D>
    {
        public M getMeta();

        public static <D> Entry<D, ?> of(D data)
        {
            Object meta = null;
            return of(data, meta);
        }

        public static <D, M> Entry<D, M> of(D data, M meta)
        {
            return new Entry<D, M>()
            {
                @Override
                public D get()
                {
                    return data;
                }

                @Override
                public M getMeta()
                {
                    return meta;
                }
            };
        }
    }
}
