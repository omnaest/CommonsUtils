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
package org.omnaest.utils.map;

import java.io.File;
import java.util.Map;

import org.omnaest.utils.JSONHelper;

/**
 * {@link FileSynchronizedMap} using {@link JSONHelper} as {@link Serializer} and {@link Deserializer}
 * 
 * @author omnaest
 * @param <K>
 * @param <V>
 */
public class JSONFileSynchronizedMap<K, V> extends FileSynchronizedMap<K, V>
{
	public JSONFileSynchronizedMap(Map<K, V> map, File file)
	{
		super(map, file, new FileSynchronizedMap.Serializer<K, V>()
		{
			@Override
			public String apply(Map<K, V> map)
			{
				return JSONHelper.prettyPrint(map);
			}
		}, new FileSynchronizedMap.Deserializer<K, V>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Map<K, V> apply(String data)
			{
				return JSONHelper.readFromString(data, Map.class);
			}
		});
	}

}
