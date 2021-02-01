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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.cache.Cache;

/**
 * @see Cache
 * @author Omnaest
 * @param <V>
 */
public abstract class AbstractCache implements Cache
{
    @Override
    public <V> Supplier<V> getSupplier(String key, Class<V> type)
    {
        return () -> this.get(key, type);
    }

    @Override
    public boolean isEmpty()
    {
        return this.keySet()
                   .isEmpty();
    }

    @Override
    public int size()
    {
        return this.keySet()
                   .size();
    }

    @Override
    public void clear()
    {
        this.keySet()
            .forEach(this::remove);
    }

    @Override
    public <V> void putAll(Map<String, V> map)
    {
        if (map != null)
        {
            map.forEach(this::put);
        }
    }

    @Override
    public void removeAll(Iterable<String> keys)
    {
        if (keys != null)
        {
            keys.forEach(this::remove);
        }
    }

    @Override
    public <V> V computeIfAbsentOrUpdate(String key, Supplier<V> supplier, UnaryOperator<V> updateFunction, Class<V> type)
    {
        AtomicBoolean supplied = new AtomicBoolean(false);
        V result = this.computeIfAbsent(key, () ->
        {
            supplied.set(true);
            return supplier.get();
        }, type);
        if (!supplied.get())
        {
            result = updateFunction.apply(result);
            this.put(key, result);
        }
        return result;
    }

}
