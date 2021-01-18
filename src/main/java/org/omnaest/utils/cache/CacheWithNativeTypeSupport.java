package org.omnaest.utils.cache;

/**
 * Extension for a {@link Cache} which supports some native {@link Class} type formats like {@link String} etc.
 * 
 * @author omnaest
 */
public interface CacheWithNativeTypeSupport extends Cache
{
    public CacheWithNativeTypeSupport withNativeStringStorage(boolean active);

    public CacheWithNativeTypeSupport withNativeByteArrayStorage(boolean active);
}
