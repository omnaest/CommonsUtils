package org.omnaest.utils.cache;

import java.io.File;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;

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
        return this.withLocalCache(this.getClass()
                                       .getSimpleName());
    }

    /**
     * Same as {@link #withCache(Cache)} using a local {@link JsonFolderFilesCache} with the given name in the "cache" folder.
     * 
     * @param name
     * @return
     */
    public default T withLocalCache(String name)
    {
        return this.withCache(CacheUtils.newLocalJsonFolderCache(name));
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
