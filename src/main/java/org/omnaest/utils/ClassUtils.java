/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

public class ClassUtils
{

    public static interface Resource
    {
        public String asString();

        public JSONResource asJson();

        public InputStream asInputStream();

        public byte[] asByteArray();

        public Stream<String> asLines();
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
                               catch (Exception e)
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

                           @Override
                           public Stream<String> asLines()
                           {
                               return StringUtils.splitToStreamByLineSeparator(this.asString());
                           }
                       });
    }

    public static <T> Optional<T> newInstance(Class<T> type)
    {
        return Optional.ofNullable(type)
                       .map(t ->
                       {
                           try
                           {
                               return t.newInstance();
                           }
                           catch (InstantiationException | IllegalAccessException e)
                           {
                               return null;
                           }
                       });
    }
}
