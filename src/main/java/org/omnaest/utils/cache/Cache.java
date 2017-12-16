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

import org.omnaest.utils.CacheUtils;

/**
 * @see UnaryCache
 * @see CacheUtils
 * @see CacheUtils#newConcurrentInMemoryCache()
 * @see CacheUtils#newJsonFileCache(java.io.File)
 * @author Omnaest
 * @param <V>
 */
public interface Cache
{
	public <V> V get(String key, Class<V> type);

	public <V> Class<V> getType(String key);

	public <V> Supplier<V> getSupplier(String key, Class<V> type);

	public void put(String key, Object value);

	public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type);

	public void remove(String key);

	public Set<String> keySet();

	public boolean isEmpty();

	public int size();

	public void clear();
}
