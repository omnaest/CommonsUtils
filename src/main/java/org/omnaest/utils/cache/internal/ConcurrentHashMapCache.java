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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils.cache.internal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.omnaest.utils.duration.TimeDuration;

@SuppressWarnings("unchecked")
public class ConcurrentHashMapCache extends AbstractCache
{
    private Map<String, ValueHolder> cache = new ConcurrentHashMap<>();

    @Override
    public <V> V get(String key, Class<V> type)
    {
        return Optional.ofNullable(key)
                       .map(this.cache::get)
                       .map(ValueHolder::getValue)
                       .map(result -> (V) result)
                       .orElse(null);
    }

    @Override
    public boolean contains(String key)
    {
        return Optional.ofNullable(key)
                       .map(this.cache::containsKey)
                       .orElse(false);
    }

    @Override
    public TimeDuration getAge(String key)
    {
        long creationTime = Optional.ofNullable(this.cache.get(key))
                                    .map(holder -> holder.getCreationTime())
                                    .orElseGet(() -> System.currentTimeMillis());
        long age = System.currentTimeMillis() - creationTime;
        return TimeDuration.of(age, TimeUnit.MILLISECONDS);
    }

    @Override
    public void put(String key, Object value)
    {
        this.cache.put(key, new ValueHolder(value));
    }

    @Override
    public <V> void putAll(Map<String, V> map)
    {
        if (map != null)
        {
            map.forEach((key, value) ->
            {
                this.cache.put(key, new ValueHolder(value));
            });
        }
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return (V) this.cache.computeIfAbsent(key, (id) -> new ValueHolder(supplier.get()))
                             .getValue();
    }

    @Override
    public Set<String> keySet()
    {
        return this.cache.keySet();
    }

    @Override
    public <V> Class<V> getType(String key)
    {
        return this.cache.containsKey(key) ? (Class<V>) this.cache.get(key)
                                                                  .getClass()
                : null;
    }

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public String toString()
    {
        return "ConcurrentHashMapCache [cache=" + this.cache + "]";
    }

    private static class ValueHolder
    {
        private Object value;
        private long   creationTime;

        public ValueHolder(Object value)
        {
            super();
            this.value = value;
            this.creationTime = System.currentTimeMillis();
        }

        public Object getValue()
        {
            return this.value;
        }

        public long getCreationTime()
        {
            return this.creationTime;
        }

        @Override
        public String toString()
        {
            return "ValueHolder [value=" + this.value + ", creationTime=" + this.creationTime + "]";
        }

    }

}
