package org.omnaest.utils.cache.internal;

import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.cache.AbstractCache;
import org.omnaest.utils.cache.internal.RandomAccessLogarithmicBlockFileStorageCache.DataMap.Content;
import org.omnaest.utils.duration.TimeDuration;
import org.omnaest.utils.file.storage.RandomAccessLogarithmicBlockFileStorage;
import org.omnaest.utils.file.storage.StringBlockFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class RandomAccessLogarithmicBlockFileStorageCache extends AbstractCache
{
    private static final Logger    LOG = LoggerFactory.getLogger(RandomAccessLogarithmicBlockFileStorageCache.class);

    private StringBlockFileStorage fileStorage;
    private int                    hashCapacity;

    public RandomAccessLogarithmicBlockFileStorageCache(File cacheDirectory, int hashCapacity)
    {
        this(cacheDirectory, hashCapacity, -1);
    }

    public RandomAccessLogarithmicBlockFileStorageCache(File cacheDirectory, int hashCapacity, int initialBlockSize)
    {
        super();
        this.hashCapacity = hashCapacity;
        this.fileStorage = RandomAccessLogarithmicBlockFileStorage.of(cacheDirectory)
                                                                  .withInitialBlockSize(initialBlockSize)
                                                                  .asStringBlockFileStorage();
    }

    //    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class DataMap
    {
        private Map<String, Content> keyToContent;

        @JsonCreator
        public DataMap(Map<String, Content> keyToContent)
        {
            super();
            this.keyToContent = keyToContent;
        }

        @JsonValue
        public Map<String, Content> getKeyToContent()
        {
            return this.keyToContent;
        }

        //        @JsonIgnoreProperties(ignoreUnknown = true)
        protected static class Content
        {
            @JsonProperty
            private Object element;

            @JsonProperty
            private String type;

            @JsonProperty
            private Date   modifiedDate;

            protected Content()
            {
                super();
            }

            public Content(Object element, String type, Date modifiedDate)
            {
                super();
                this.element = element;
                this.type = type;
                this.modifiedDate = modifiedDate;
            }

            public Object getElement()
            {
                return this.element;
            }

            public String getType()
            {
                return this.type;
            }

            public Date getModifiedDate()
            {
                return this.modifiedDate;
            }

            @Override
            public String toString()
            {
                return "Content [element=" + this.element + ", type=" + this.type + ", modifiedDate=" + this.modifiedDate + "]";
            }

        }
    }

    @Override
    public <V> V get(String key, Class<V> type)
    {
        return this.findContent(key)
                   .map(content -> this.toValidatedValue(content, type, key))
                   .orElse(null);
    }

    /**
     * Resolves the stored type of the given {@link Content}, validates it against the requested {@code type} (if any),
     * deserializes the element and casts it to {@code V}.
     *
     * @param content
     * @param type    the requested type, or null to skip validation
     * @param key     used only for diagnostics in the failure message
     * @throws ClassCastException if {@code type} is not assignable from the actually stored type
     */
    @SuppressWarnings("unchecked")
    private <V> V toValidatedValue(Content content, Class<V> type, String key)
    {
        try
        {
            Class<?> storedType = ClassUtils.getClass(content.getType());
            if (type != null && !type.isAssignableFrom(storedType))
            {
                throw new ClassCastException("Requested cache value type [" + type.getName() + "] is not assignable from the actually stored type ["
                                             + storedType.getName() + "] for key [" + key + "]");
            }
            return type != null ? type.cast(JSONHelper.toObjectWithType(content.getElement(), storedType))
                    : (V) JSONHelper.toObjectWithType(content.getElement(), storedType);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Unable to deseralize data due to missing class type: " + content.getType(), e);
        }
    }

    private Optional<Content> findContent(String key)
    {
        return this.findKeyToContent(key)
                   .map(keyToContent -> keyToContent.get(key));
    }

    private Optional<Map<String, Content>> findKeyToContent(String key)
    {
        return this.findKeyToContent(this.determineRowIndex(key));
    }

    private Optional<Integer> determineRowIndex(String key)
    {
        return Optional.ofNullable(key)
                       .map(String::hashCode)
                       .map(hashCode -> Math.abs(hashCode) % this.hashCapacity);
    }

    private Optional<Map<String, Content>> findKeyToContent(Optional<Integer> rowIndex)
    {
        return rowIndex.map(this.fileStorage::read)
                       .filter(StringUtils::isNotBlank)
                       .map(json ->
                       {
                           return JSONHelper.deserializer(DataMap.class)
                                            .withExceptionHandler(e -> LOG.warn("Illegal json format: " + json, e))
                                            .apply(json);
                       })
                       .map(DataMap::getKeyToContent);
    }

    @Override
    public TimeDuration getAge(String key)
    {
        return this.findContent(key)
                   .map(content -> TimeDuration.between(content.getModifiedDate(), Date.from(Instant.now())))
                   .orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Class<V> getType(String key)
    {
        return this.findContent(key)
                   .map(content ->
                   {
                       try
                       {
                           Class<V> storedType = (Class<V>) ClassUtils.getClass(content.getType());
                           return storedType;
                       }
                       catch (ClassNotFoundException e)
                       {
                           throw new IllegalStateException("Unable to find class type: " + content.getType(), e);
                       }
                   })
                   .orElse(null);
    }

    @Override
    public void put(String key, Object value)
    {
        this.modifyDataMapAndGet(key, keyToContent -> keyToContent.put(key, new Content(value, value.getClass()
                                                                                                    .getCanonicalName(),
                                                                                        Date.from(Instant.now()))));
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return Optional.ofNullable(this.modifyDataMapAndGet(key, keyToContent ->
        {
            V value = supplier.get();
            return keyToContent.computeIfAbsent(key, k -> new Content(value, value.getClass()
                                                                                  .getCanonicalName(),
                                                                      Date.from(Instant.now())));
        }))
                       .map(content -> this.toValidatedValue(content, type, key))
                       .orElse(null);
    }

    @Override
    public void remove(String key)
    {
        this.modifyDataMap(key, keyToContent -> keyToContent.remove(key));
    }

    private void modifyDataMap(String key, Consumer<Map<String, Content>> keyToContentMapModifier)
    {
        this.modifyDataMapAndGet(key, keyToContent ->
        {
            keyToContentMapModifier.accept(keyToContent);
            return null;
        });
    }

    private <R> R modifyDataMapAndGet(String key, Function<Map<String, Content>, R> keyToContentMapModifier)
    {
        R result = null;
        if (key != null)
        {
            int rowIndex = this.determineRowIndex(key)
                               .orElse(0);

            Map<String, Content> keyToContent = this.findKeyToContent(key)
                                                    .orElse(new HashMap<>());
            result = keyToContentMapModifier.apply(keyToContent);
            String adjustedJson = JSONHelper.serialize(keyToContent, true);
            this.fileStorage.write(rowIndex, adjustedJson);
        }
        return result;
    }

    @Override
    public Set<String> keySet()
    {
        return IntStream.range(0, this.hashCapacity)
                        .mapToObj(rowIndex -> this.findKeyToContent(Optional.of(rowIndex)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(Map::keySet)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
    }

}
