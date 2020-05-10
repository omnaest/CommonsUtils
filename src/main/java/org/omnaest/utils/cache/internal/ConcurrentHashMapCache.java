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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ConcurrentHashMapCache extends AbstractCache
{
    private Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public <V> V get(String key, Class<V> type)
    {
        return key == null ? null : (V) this.cache.get(key);
    }

    @Override
    public void put(String key, Object value)
    {
        this.cache.put(key, value);
    }

    @Override
    public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
    {
        return (V) this.cache.computeIfAbsent(key, (id) -> supplier.get());
    }

    @Override
    public Set<String> keySet()
    {
        return new HashSet<>(this.cache.keySet());
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

}
