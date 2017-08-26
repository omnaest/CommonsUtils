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
package org.omnaest.utils.cache;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @see Cache
 * @see UnaryCache
 * @author Omnaest
 * @param <V>
 */
public class CacheToUnaryCacheAdapter<V> implements UnaryCache<V>
{
	private Cache		cache;
	private Class<V>	type;

	public CacheToUnaryCacheAdapter(Cache cache, Class<V> type)
	{
		super();
		this.cache = cache;
		this.type = type;
	}

	@Override
	public V get(String key)
	{
		return this.cache.get(key, this.type);
	}

	@Override
	public Supplier<V> getSupplier(String key)
	{
		return this.cache.getSupplier(key, this.type);
	}

	@Override
	public void put(String key, V value)
	{
		this.cache.put(key, value);
	}

	@Override
	public V computeIfAbsent(String key, Supplier<V> supplier)
	{
		return this.cache.computeIfAbsent(key, supplier, this.type);
	}

	@Override
	public Set<String> keySet()
	{
		return this.cache.keySet();
	}

	@Override
	public void remove(String key)
	{
		this.cache.remove(key);
	}

	@Override
	public int size()
	{
		return this.cache.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.cache.isEmpty();
	}

	@Override
	public void clear()
	{
		this.cache.clear();
	}

}
