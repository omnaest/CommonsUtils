package org.omnaest.utils.processor.repeating;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.utils.FunctionUtils;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.functional.TriFunction;
import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * @see RepeatingFilteredProcessor
 * @author omnaest
 * @param <R>
 */
public class DefaultRepeatingFilteredProcessor<R> implements RepeatingFilteredProcessor<R>
{
    private Function<Integer, UnaryCache<R>> cacheProvider;
    private int                              distributionFactor = 10;
    private Function<String, Integer>        idToCacheIdMapper  = id -> Optional.ofNullable(id)
                                                                                .map(String::hashCode)
                                                                                .orElse(Integer.MAX_VALUE)
            % this.distributionFactor;

    public DefaultRepeatingFilteredProcessor(Function<Integer, UnaryCache<R>> cacheProvider)
    {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public RepeatingFilteredProcessor<R> withDistributionFactor(int distributionFactor)
    {
        this.distributionFactor = distributionFactor;
        return this;
    }

    @Override
    public RepeatingFilteredProcessor<R> withDistributionFactor(double distributionFactor, int maximumCapacity)
    {
        return this.withDistributionFactor((int) Math.round(distributionFactor * maximumCapacity));
    }

    @Override
    public <E> RepeatingFilteredProcessorWithStreamProvider<E, R> process(Supplier<Stream<E>> elementStreamSupplier)
    {
        return new RepeatingFilteredProcessorWithStreamProvider<E, R>()
        {

            @Override
            public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction)
            {
                return this.withOperation(identityFunction, mappingFunction,
                                          FunctionUtils.toExceptionThrowingSupplier(() -> new IllegalStateException("Encountered the same identity but no merge function is given")));
            }

            @Override
            public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                UnaryBiFunction<R> mergeFunction)
            {
                return this.withOperation(identityFunction, mappingFunction, mergeFunction, this.determineAllAvailableCacheIds());
            }

            private IntStream determineAllAvailableCacheIds()
            {
                return IntStream.range(0, DefaultRepeatingFilteredProcessor.this.distributionFactor);
            }

            public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                UnaryBiFunction<R> mergeFunction, IntStream cacheIds)
            {
                return this.processInBatchesPerDistributionKey(elementStreamSupplier, identityFunction,
                                                               (filteredStream, cache, cacheId) -> filteredStream.map(element ->
                                                               {
                                                                   String id = identityFunction.apply(element);
                                                                   R mappingResult = mappingFunction.apply(element);
                                                                   R mergeResult = cache.computeIfAbsentOrUpdate(id, () -> mappingResult,
                                                                                                                 existing -> mergeFunction.apply(existing,
                                                                                                                                                 mappingResult));
                                                                   return new ProcessedElementImpl<>(id, mergeResult, element);
                                                               }), cacheIds);
            }

            private <PE extends AggregatedElement<R>> Stream<PE> processInBatchesPerDistributionKey(Supplier<Stream<E>> elementStreamSupplier,
                                                                                                    Function<E, String> identityFunction,
                                                                                                    TriFunction<Stream<E>, UnaryCache<R>, Integer, Stream<PE>> filteredStreamProcessor)
            {
                return this.processInBatchesPerDistributionKey(elementStreamSupplier, identityFunction, filteredStreamProcessor,
                                                               this.determineAllAvailableCacheIds());
            }

            private <PE extends AggregatedElement<R>> Stream<PE> processInBatchesPerDistributionKey(Supplier<Stream<E>> elementStreamSupplier,
                                                                                                    Function<E, String> identityFunction,
                                                                                                    TriFunction<Stream<E>, UnaryCache<R>, Integer, Stream<PE>> filteredStreamProcessor,
                                                                                                    IntStream cacheIds)
            {
                return cacheIds.boxed()
                               .flatMap(cacheId ->
                               {
                                   Predicate<E> cacheIdMatcher = element ->
                                   {
                                       String id = identityFunction.apply(element);
                                       return DefaultRepeatingFilteredProcessor.this.idToCacheIdMapper.apply(id) == cacheId;
                                   };
                                   return filteredStreamProcessor.apply(elementStreamSupplier.get()
                                                                                             .filter(cacheIdMatcher),
                                                                        DefaultRepeatingFilteredProcessor.this.cacheProvider.apply(cacheId), cacheId);
                               });
            }

            @Override
            public Stream<AggregatedElement<R>> withAggregatingOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                         UnaryBiFunction<R> mergeFunction)
            {
                return this.processInBatchesPerDistributionKey(elementStreamSupplier, identityFunction, (filteredStream, cache, cacheId) ->
                {
                    AtomicReference<BiElement<Integer, UnaryCache<R>>> currentCache = new AtomicReference<>();
                    return this.withOperation(identityFunction, mappingFunction, mergeFunction, IntStream.of(cacheId))
                               .map(ProcessedElement::getId)
                               .collect(Collectors.toSet())
                               .stream()
                               .map(id ->
                               {
                                   int requestedCacheId = DefaultRepeatingFilteredProcessor.this.idToCacheIdMapper.apply(id);
                                   if (!Optional.ofNullable(currentCache.get())
                                                .map(BiElement::getFirst)
                                                .map(currentCacheId -> currentCacheId.equals(requestedCacheId))
                                                .orElse(false))
                                   {
                                       currentCache.set(BiElement.of(requestedCacheId,
                                                                     DefaultRepeatingFilteredProcessor.this.cacheProvider.apply(requestedCacheId)));
                                   }

                                   UnaryCache<R> second = currentCache.get()
                                                                      .getSecond();
                                   R result = second.get(id);
                                   E element = null;
                                   return new ProcessedElementImpl<>(id, result, element);
                               });
                });
            }

        };
    }

    private static class ProcessedElementImpl<E, R> implements ProcessedElement<E, R>
    {
        private final String id;
        private final R      result;
        private final E      element;

        private ProcessedElementImpl(String id, R result, E element)
        {
            this.id = id;
            this.result = result;
            this.element = element;
        }

        @Override
        public E getElement()
        {
            return this.element;
        }

        @Override
        public String getId()
        {
            return this.id;
        }

        @Override
        public R getResult()
        {
            return this.result;
        }

        @Override
        public String toString()
        {
            return "[id=" + this.id + ", result=" + this.result + ", element=" + this.element + "]";
        }

    }

}
