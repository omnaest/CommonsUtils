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
package org.omnaest.utils.cache.internal;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.duration.TimeDuration;

/**
 * {@link Cache} implementation which is always empty and stores nothing.<br>
 * <br>
 * {@link #computeIfAbsent(String, Supplier, Class)} returns always a new object using the given {@link Supplier}.
 * 
 * @author omnaest
 */
public class NoOperationCache extends AbstractCache
{
    @Override
    public <V> V get(String key, Class<V> type)
    {
        return null;
    }

    @Override
    public TimeDuration getAge(String key)
    {
        return TimeDuration.zero();
    }

    @Override
    public <V> Class<V> getType(String key)
    {
        return null;
    }

    @Override
    public void put(String key, Object value)
    {
        // no operation
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return Optional.ofNullable(supplier)
                       .map(Supplier::get)
                       .orElse(null);
    }

    @Override
    public void remove(String key)
    {
        // no operation
    }

    @Override
    public Set<String> keySet()
    {
        return Collections.emptySet();
    }

}
