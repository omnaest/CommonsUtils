package org.omnaest.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

public class ClassUtils
{

    public static interface Resource
    {
        public String asString();

        public JSONResource asJson();

        public InputStream asInputStream();

        public byte[] asByteArray();
    }

    public static interface JSONResource
    {
        public <R, T extends R> R as(Class<T> type);
    }

    /**
     * Similar to {@link #loadResource(Class, String)} but for a given instance of a {@link Class} type.
     * 
     * @param instance
     * @param resource
     * @return
     */
    public static Optional<Resource> loadResource(Object instance, String resource)
    {
        return Optional.ofNullable(instance)
                       .map(i -> loadResource(instance.getClass(), resource).orElse(null));
    }

    /**
     * Allows to load absolute resources
     * 
     * @param resource
     * @return
     */
    public static Optional<Resource> loadResource(String resource)
    {
        return loadResource(Class.class, StringUtils.ensurePrefix(resource, "/"));
    }

    /**
     * Loads a classpath resource for the given {@link Class} type
     * 
     * @param type
     * @param resource
     * @return
     */
    public static Optional<Resource> loadResource(Class<?> type, String resource)
    {

        return Optional.ofNullable(type)
                       .filter(t -> org.apache.commons.lang3.StringUtils.isNotBlank(resource))
                       .map(t -> t.getResourceAsStream(resource))
                       .map(is ->
                       {
                           try
                           {
                               return IOUtils.toByteArray(is);
                           }
                           catch (IOException e1)
                           {
                               return null;
                           }
                       })
                       .map(data -> new Resource()
                       {
                           @Override
                           public String asString()
                           {
                               try
                               {
                                   return IOUtils.toString(this.asByteArray(), StandardCharsets.UTF_8.name());
                               }
                               catch (IOException e)
                               {
                                   throw new IllegalStateException(e);
                               }
                           }

                           @Override
                           public byte[] asByteArray()
                           {
                               return data;
                           }

                           @Override
                           public JSONResource asJson()
                           {
                               return new JSONResource()
                               {
                                   @Override
                                   public <R, T extends R> R as(Class<T> type)
                                   {
                                       return JSONHelper.readFromString(asString(), type);
                                   }
                               };
                           }

                           @Override
                           public InputStream asInputStream()
                           {
                               return new ByteArrayInputStream(data);
                           }
                       });
    }
}
