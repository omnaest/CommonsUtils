package org.omnaest.utils.repository.map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.MapUtils;
import org.omnaest.utils.ObjectUtils;
import org.omnaest.utils.functional.BidirectionalFunction;
import org.omnaest.utils.map.MapDecorator;
import org.omnaest.utils.repository.ElementRepository;

/**
 * a {@link ElementRepositoryMap} based on two given {@link ElementRepository}s one for the keys, the other for the values
 * 
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public class BiElementRepositoryMap<K, V> extends MapDecorator<K, V> implements ElementRepositoryMap<K, V>
{
    private static class ElementSupplier<K> implements Supplier<K>
    {
        private Supplier<K> supplier;

        public ElementSupplier(Supplier<K> supplier)
        {
            super();
            this.supplier = supplier;
        }

        @Override
        public int hashCode()
        {
            return this.supplier.get()
                                .hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof ElementSupplier ? ((ElementSupplier<K>) obj).get()
                                                                              .equals(this.get())
                    : false;
        }

        @Override
        public K get()
        {
            return this.supplier.get();
        }

    }

    private static class IdAndRepository<I, D> implements Supplier<D>
    {
        private I                       id;
        private ElementRepository<I, D> repository;

        public IdAndRepository(ElementRepository<I, D> repository, D element)
        {
            super();
            this.id = repository.add(element);
            this.repository = repository;
        }

        @Override
        public D get()
        {
            return this.repository.get(this.id);
        }

    }

    public <I> BiElementRepositoryMap(ElementRepository<I, K> keyRepository, ElementRepository<I, V> valueRepository)
    {
        super(MapUtils.toMediatedMap(new ConcurrentHashMap<>(), new BidirectionalFunction<K, ElementSupplier<K>>()
        {
            @Override
            public Function<K, ElementSupplier<K>> forward()
            {
                return key -> new ElementSupplier<>(new IdAndRepository<>(keyRepository, key));
            }

            @Override
            public Function<ElementSupplier<K>, K> backward()
            {
                return elementSupplier -> ObjectUtils.getIfNotNull(elementSupplier, es -> es.get());
            }

        }, new BidirectionalFunction<K, ElementSupplier<K>>()
        {
            @Override
            public Function<K, ElementSupplier<K>> forward()
            {
                return key -> new ElementSupplier<>(() -> key);
            }

            @Override
            public Function<ElementSupplier<K>, K> backward()
            {
                return elementSupplier -> ObjectUtils.getIfNotNull(elementSupplier, es -> es.get());
            }

        }, new BidirectionalFunction<V, ElementSupplier<V>>()
        {
            @Override
            public Function<V, ElementSupplier<V>> forward()
            {
                return value -> new ElementSupplier<>(new IdAndRepository<>(valueRepository, value));
            }

            @Override
            public Function<ElementSupplier<V>, V> backward()
            {
                return elementSupplier -> ObjectUtils.getIfNotNull(elementSupplier, es -> es.get());
            }
        }, new BidirectionalFunction<V, ElementSupplier<V>>()
        {
            @Override
            public Function<V, ElementSupplier<V>> forward()
            {
                return value -> new ElementSupplier<>(() -> value);
            }

            @Override
            public Function<ElementSupplier<V>, V> backward()
            {
                return elementSupplier -> ObjectUtils.getIfNotNull(elementSupplier, es -> es.get());
            }
        }));

    }

}
