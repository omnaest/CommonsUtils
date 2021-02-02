package org.omnaest.utils.processor.repeating;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.utils.FunctionUtils;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.functional.BiIntFunction;
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
    private OnCacheCloseHandler<R>           onCacheCloseHandler;

    private int                       distributionFactor = 10;
    private Function<String, Integer> idToCacheIdMapper  = id -> Optional.ofNullable(id)
                                                                         .map(String::hashCode)
                                                                         .orElse(Integer.MAX_VALUE)
            % this.distributionFactor;

    public DefaultRepeatingFilteredProcessor(Function<Integer, UnaryCache<R>> cacheProvider, OnCacheCloseHandler<R> onCacheCloseHandler)
    {
        this.cacheProvider = cacheProvider;
        this.onCacheCloseHandler = onCacheCloseHandler;
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
    public RepeatingFilteredProcessor<R> withDistributionFactor(int maximumBucketSize, int maximumCapacity)
    {
        return this.withDistributionFactor(Math.max(1, 1 + maximumCapacity / Math.max(1, maximumBucketSize)));
    }

    @Override
    public <E> RepeatingFilteredProcessorWithStreamProvider<E, R> process(Supplier<Stream<E>> elementStreamSupplier)
    {
        return this.process(cacheId -> elementStreamSupplier.get());
    }

    @Override
    public <E> RepeatingFilteredProcessorWithStreamProvider<E, R> process(BiIntFunction<Stream<E>> elementStreamSupplier)
    {
        return this.process(cacheId -> elementStreamSupplier.apply(cacheId, this.distributionFactor));
    }

    @Override
    public <E> RepeatingFilteredProcessorWithStreamProvider<E, R> process(IntFunction<Stream<E>> elementStreamSupplier)
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
                return this.withOperation(identityFunction, mappingFunction, mergeFunction, this.determineAllAvailableCacheIds()
                                                                                                .mapToObj(cacheId -> BiElement.of(cacheId, null)));
            }

            private IntStream determineAllAvailableCacheIds()
            {
                return IntStream.range(0, DefaultRepeatingFilteredProcessor.this.distributionFactor);
            }

            public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                UnaryBiFunction<R> mergeFunction, Stream<BiElement<Integer, UnaryCache<R>>> cacheIdsAndCache)
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
                                                               }), cacheIdsAndCache);
            }

            private <PE extends AggregatedElement<R>> Stream<PE> processInBatchesPerDistributionKey(IntFunction<Stream<E>> elementStreamSupplier,
                                                                                                    Function<E, String> identityFunction,
                                                                                                    TriFunction<Stream<E>, UnaryCache<R>, Integer, Stream<PE>> filteredStreamProcessor)
            {
                return this.processInBatchesPerDistributionKey(elementStreamSupplier, identityFunction, filteredStreamProcessor,
                                                               this.determineAllAvailableCacheIds()
                                                                   .mapToObj(cacheId -> BiElement.of(cacheId, null)));
            }

            private <PE extends AggregatedElement<R>> Stream<PE> processInBatchesPerDistributionKey(IntFunction<Stream<E>> elementStreamSupplier,
                                                                                                    Function<E, String> identityFunction,
                                                                                                    TriFunction<Stream<E>, UnaryCache<R>, Integer, Stream<PE>> filteredStreamProcessor,
                                                                                                    Stream<BiElement<Integer, UnaryCache<R>>> cacheIdsAndCache)
            {
                return cacheIdsAndCache.flatMap(cacheIdAndCache ->
                {
                    int cacheId = cacheIdAndCache.getFirst();
                    Predicate<E> cacheIdMatcher = element ->
                    {
                        String id = identityFunction.apply(element);
                        return DefaultRepeatingFilteredProcessor.this.idToCacheIdMapper.apply(id) == cacheId;
                    };
                    UnaryCache<R> cache = Optional.ofNullable(cacheIdAndCache.getSecond())
                                                  .orElseGet(() -> DefaultRepeatingFilteredProcessor.this.cacheProvider.apply(cacheId));
                    return filteredStreamProcessor.apply(elementStreamSupplier.apply(cacheId)
                                                                              .filter(cacheIdMatcher)
                                                                              .onClose(() -> Optional.ofNullable(DefaultRepeatingFilteredProcessor.this.onCacheCloseHandler)
                                                                                                     .filter(consumer -> cacheIdAndCache.isSecondValueNull())
                                                                                                     .ifPresent(consumer -> consumer.accept(cacheId, cache))),
                                                         cache, cacheId);
                });
            }

            @Override
            public Stream<AggregatedElement<R>> withAggregatingOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                         UnaryBiFunction<R> mergeFunction)
            {
                return this.processInBatchesPerDistributionKey(elementStreamSupplier, identityFunction, (filteredStream, cache, cacheId) ->
                {
                    Set<String> identifiers = this.withOperation(identityFunction, mappingFunction, mergeFunction, Stream.of(BiElement.of(cacheId, cache)))
                                                  .map(ProcessedElement::getId)
                                                  .collect(Collectors.toSet());

                    return identifiers.stream()
                                      .map(id ->
                                      {
                                          int requestedCacheId = DefaultRepeatingFilteredProcessor.this.idToCacheIdMapper.apply(id);
                                          this.assertCurrentCacheIdMatchesNeededCacheId(id, requestedCacheId, cacheId);

                                          R result = cache.get(id);
                                          E element = null;
                                          return (AggregatedElement<R>) new ProcessedElementImpl<>(id, result, element);
                                      })
                                      .onClose(() -> Optional.ofNullable(DefaultRepeatingFilteredProcessor.this.onCacheCloseHandler)
                                                             .ifPresent(consumer -> consumer.accept(cacheId, cache)));
                });
            }

            private void assertCurrentCacheIdMatchesNeededCacheId(String id, int requestedCacheId, Integer cacheId)
            {
                if (requestedCacheId != cacheId)
                {
                    throw new IllegalStateException("Invalid identifier " + id + " for the current cache " + cacheId);
                }
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
