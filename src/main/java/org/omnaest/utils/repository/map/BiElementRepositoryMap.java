package org.omnaest.utils.repository.map;

import java.util.Objects;
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
    private ElementRepository<?, K> keyRepository;
    private ElementRepository<?, V> valueRepository;

    private static class ElementSupplier<E> implements Supplier<E>
    {
        private Supplier<E> supplier;

        public ElementSupplier(Supplier<E> supplier)
        {
            super();
            this.supplier = supplier;
        }

        @Override
        public int hashCode()
        {
            E element = this.supplier.get();
            return element != null ? element.hashCode() : 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof ElementSupplier ? ((ElementSupplier<E>) obj).get()
                                                                              .equals(this.get())
                    : false;
        }

        @Override
        public E get()
        {
            return this.supplier.get();
        }

        @Override
        public String toString()
        {
            return Objects.toString(this.get());
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
            return this.repository.getValue(this.id);
        }

        @Override
        public String toString()
        {
            return "IdAndRepository [id=" + this.id + ", repository=" + this.repository + "]";
        }

    }

    public <I1, I2> BiElementRepositoryMap(ElementRepository<I1, K> keyRepository, ElementRepository<I2, V> valueRepository)
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

        //
        this.keyRepository = keyRepository;
        this.valueRepository = valueRepository;

    }

    @Override
    public void close()
    {
        this.keyRepository.close();
        this.valueRepository.close();
    }

}
