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
package org.omnaest.utils.repository;

import java.util.Map;
import java.util.function.Supplier;

public class MapElementRepository<I, D> implements ElementRepository<I, D>
{
	private Map<I, D>	map;
	private Supplier<I>	idSupplier;

	public MapElementRepository(Map<I, D> map, Supplier<I> idSupplier)
	{
		super();
		this.map = map;
		this.idSupplier = idSupplier;
	}

	@Override
	public D get(I id)
	{
		return this.map.get(id);
	}

	@Override
	public I put(D entry)
	{
		I id = this.idSupplier.get();
		this.map.put(id, entry);
		return id;
	}

	@Override
	public String toString()
	{
		return "MapElementRepository [map=" + this.map + ", idSupplier=" + this.idSupplier + "]";
	}

}