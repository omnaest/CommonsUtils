package org.omnaest.utils.processor.repeating;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.functional.UnaryBiFunction;

/**
 * Defines a processor that repeatedly cycles through {@link Stream}s given by a {@link Supplier} and stores the intermediate mapping results in a
 * {@link UnaryCache} implementation.
 * <br>
 * <br>
 * 
 * <pre>
 * RepeatingFilteredProcessor.newInstance(String.class)
 *                           .withDistributionFactor(0.2, 1000)
 *                           .process(() -> IntStream.range(0, 50000)
 *                                                   .boxed())
 *                           .withOperation(element -> "key" + element, element -> "value" + element)
 *                           .forEach(processedElement ->
 *                           {
 *                               // do something with the result
 *                           });
 * </pre>
 * 
 * @author omnaest
 * @param <R>
 */
public interface RepeatingFilteredProcessor<R>
{
    /**
     * Defines a {@link Supplier} which should be able return the same elements as a {@link Stream} multiple times. <br>
     * The number of times the {@link Supplier#get()} is called is similar to the distribution factor.
     * 
     * @see #withDistributionFactor(int)
     * @param elementStreamSupplier
     * @return
     */
    public <E> RepeatingFilteredProcessorWithStreamProvider<E, R> process(Supplier<Stream<E>> elementStreamSupplier);

    /**
     * Defines the distribution factor based on a factor of the maximum capacity. Be aware that the resulting distribution factor is only roughly the real
     * maximum number of elements per bucket as the distribution is based on a hash function.
     * 
     * @param bucketSizeFactor
     * @param maximumCapacity
     * @return
     */
    public RepeatingFilteredProcessor<R> withDistributionFactor(double bucketSizeFactor, int maximumCapacity);

    /**
     * Defines the distribution factor. This sets the number of distinct buckets into which the provided elements are inserted. As more buckets are available
     * the memory overhead gets reduced but for each bucket a repetition cycle is needed through the provided element {@link Stream}.
     * 
     * @see #withDistributionFactor(double, int)
     * @param distributionFactor
     * @return
     */
    public RepeatingFilteredProcessor<R> withDistributionFactor(int distributionFactor);

    public static interface RepeatingFilteredProcessorWithStreamProvider<E, R>
    {
        public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction);

        public Stream<ProcessedElement<E, R>> withOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                            UnaryBiFunction<R> mergeFunction);

        /**
         * Returns a {@link Stream} of mapping results which is not based on the direct processing like by
         * {@link #withOperation(Function, Function, UnaryBiFunction)}, instead it returns the content of the {@link UnaryCache} and it will return a single
         * distinct element based on the ids generated.<br>
         * <br>
         * This method needs to collect and hold the keys of a distribution group in memory. So please be aware that this needs a larger memory overhead.
         * 
         * @param identityFunction
         * @param mappingFunction
         * @param mergeFunction
         * @return
         */
        public Stream<AggregatedElement<R>> withAggregatingOperation(Function<E, String> identityFunction, Function<E, R> mappingFunction,
                                                                     UnaryBiFunction<R> mergeFunction);
    }

    public static interface AggregatedElement<R>
    {
        public String getId();

        public R getResult();
    }

    public static interface ProcessedElement<E, R> extends AggregatedElement<R>
    {
        public E getElement();
    }

    /**
     * Returns an instance based on a given {@link UnaryCache}
     * 
     * @see CacheUtils
     * @see Cache#asUnaryCache(Class)
     * @param cacheProvider
     * @return
     */
    public static <R> RepeatingFilteredProcessor<R> of(Function<Integer, UnaryCache<R>> cacheProvider)
    {
        return new DefaultRepeatingFilteredProcessor<>(cacheProvider);
    }

    /**
     * Returns an in memory instance utilizing multiple {@link ConcurrentHashMap} instances
     * 
     * @see #of(Function)
     * @param type
     * @return
     */
    public static <R> RepeatingFilteredProcessor<R> newInstance(Class<R> type)
    {
        Map<Integer, UnaryCache<R>> cacheIdToCache = new ConcurrentHashMap<>();
        return of(cacheId -> cacheIdToCache.computeIfAbsent(cacheId, id -> CacheUtils.newConcurrentInMemoryCache()
                                                                                     .asUnaryCache(type)));
    }

}
