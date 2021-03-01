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
package org.omnaest.utils.map;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.omnaest.utils.FileMapUtils;
import org.omnaest.utils.JSONHelper;

public class JSONDirectorySynchronizedValuesMap<K, V> extends MapDecorator<K, V>
{
	public JSONDirectorySynchronizedValuesMap(File directory, Class<V> valueType)
	{
		super(newMapDelegate(directory, valueType));
	}

	private static <K, V> Map<K, V> newMapDelegate(File directory, Class<V> valueType)
	{
		File indexFile = new File(directory, "index.json");
		Map<K, Integer> keyToFileIndex = FileMapUtils.toJsonFileSynchronizedMap(new LinkedHashMap<>(), indexFile);
		AtomicInteger indexPosition = new AtomicInteger(keyToFileIndex	.values()
																		.stream()
																		.mapToInt(v -> v)
																		.max()
																		.orElse(0));
		Function<Integer, File> documentFileResolver = i -> new File(directory, "" + i + ".json");
		Function<File, String> documentContentResolver = file ->
		{
			String retval;
			try
			{
				retval = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			} catch (IOException e)
			{
				throw new FileSynchronizedMap.FileAccessException(e);
			}
			return retval;
		};
		Function<Integer, V> valueResolver = documentFileResolver	.andThen(documentContentResolver)
																	.andThen(JSONHelper.deserializer(valueType));

		Function<Integer, Consumer<V>> documentWriter = documentFileResolver.andThen(file ->
		{
			return value -> org.omnaest.utils.FileUtils	.toConsumer(file)
														.accept(JSONHelper	.serializer()
																			.apply(value));
		});

		return new CRUDMap<K, V>()
		{

			@Override
			public int size()
			{
				return keyToFileIndex.size();
			}

			@Override
			public boolean containsKey(K key)
			{
				return keyToFileIndex.containsKey(key);
			}

			@Override
			public V get(K key)
			{
				V retval = null;

				Integer fileIndex = keyToFileIndex.get(key);
				if (fileIndex != null)
				{
					retval = valueResolver.apply(fileIndex);
				}
				return retval;
			}

			@Override
			public V put(K key, V value)
			{
				V retval = this.get(key);

				Integer fileIndex = keyToFileIndex.get(key);
				if (fileIndex == null)
				{
					fileIndex = indexPosition.incrementAndGet();
				}
				documentWriter	.apply(fileIndex)
								.accept(value);
				keyToFileIndex.put(key, fileIndex);

				return retval;
			}

			@Override
			public V remove(K key)
			{
				V retval = this.get(key);

				Integer fileIndex = keyToFileIndex.remove(key);
				if (fileIndex != null)
				{
					documentWriter	.apply(fileIndex)
									.accept(null);
				}

				return retval;
			}

			@Override
			public void clear()
			{
				keyToFileIndex	.values()
								.forEach(fileIndex ->
								{
									if (fileIndex != null)
									{
										documentWriter	.apply(fileIndex)
														.accept(null);
									}
								});
				keyToFileIndex.clear();
			}

			@Override
			public Set<K> keySet()
			{
				return Collections.unmodifiableSet(keyToFileIndex.keySet());
			}
		}.toMap();
	}

}
