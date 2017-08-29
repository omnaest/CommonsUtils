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

import java.util.function.Supplier;

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
		return this	.keySet()
					.isEmpty();
	}

	@Override
	public int size()
	{
		return this	.keySet()
					.size();
	}

	@Override
	public void clear()
	{
		this.keySet()
			.forEach(this::remove);
	}

}