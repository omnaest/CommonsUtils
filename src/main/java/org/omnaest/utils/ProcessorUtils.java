package org.omnaest.utils;

import java.util.function.Function;

import org.omnaest.utils.cache.UnaryCache;
import org.omnaest.utils.processor.repeating.RepeatingFilteredProcessor;

/**
 * Helper around processor implementations
 * 
 * @author omnaest
 */
public class ProcessorUtils
{
    /**
     * @see RepeatingFilteredProcessor
     * @param cacheProvider
     * @return
     */
    public static <R> RepeatingFilteredProcessor<R> newRepeatingFilteredProcessorWithCache(Function<Integer, UnaryCache<R>> cacheProvider)
    {
        return RepeatingFilteredProcessor.of(cacheProvider);
    }
}
