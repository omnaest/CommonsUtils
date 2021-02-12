package org.omnaest.utils.cache;

import java.io.File;
import java.util.function.UnaryOperator;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;
import org.omnaest.utils.duration.TimeDuration;

/**
 * Interface for any {@link Class} which supports a {@link Cache} layer
 * 
 * @author omnaest
 * @param <T>
 */
public interface Cacheable<T>
{
    /**
     * Adding a {@link Cache} layer which is used by the current type
     * 
     * @param cache
     * @return
     */
    public T withCache(Cache cache);

    /**
     * Same as {@link #withLocalCache(String)} with {@link Class#getSimpleName()} as name
     * 
     * @return
     */
    public default T withLocalCache()
    {
        return this.withLocalCache(v -> v);
    }

    /**
     * Similar to {@link #withLocalCache()} but allows to modify the {@link Cache} instance
     * 
     * @param cacheModifier
     * @return
     */
    public default T withLocalCache(UnaryOperator<Cache> cacheModifier)
    {
        return this.withLocalCache(this.getClass()
                                       .getSimpleName(),
                                   cacheModifier);
    }

    /**
     * Similar to {@link #withLocalCache()} but with a key eviction after the given {@link TimeDuration}
     * 
     * @param duration
     * @return
     */
    public default T withLocalCache(TimeDuration duration)
    {
        return this.withLocalCache(cache -> cache.asDurationLimitedCache(duration));
    }

    /**
     * Same as {@link #withCache(Cache)} using a local {@link JsonFolderFilesCache} with the given name in the "cache" folder.
     * 
     * @param name
     * @return
     */
    public default T withLocalCache(String name)
    {
        return this.withLocalCache(name, cache -> cache);
    }

    /**
     * Same as {@link #withLocalCache(String)} but allows to modify the {@link Cache} instance
     * 
     * @param name
     * @param cacheModifier
     * @return
     */
    public default T withLocalCache(String name, UnaryOperator<Cache> cacheModifier)
    {
        return this.withCache(cacheModifier.apply(CacheUtils.newLocalJsonFolderCache(name)));
    }

    /**
     * Similar to {@link #withCache(Cache)} using a {@link JsonFolderFilesCache} with the given {@link File} folder.
     * 
     * @param folder
     * @return
     */
    public default T withDirectoryCache(File folder)
    {
        return this.withCache(CacheUtils.newJsonFolderCache(folder));
    }
}
