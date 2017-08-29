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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link Cache} which uses {@link JSONHelper} to store the cache content within a single json {@link File}
 *
 * @author Omnaest
 * @param <V>
 */
public class JsonSingleFileCache extends AbstractCache
{
	private static final String	UTF_8	= "utf-8";
	private static final Logger	LOG		= LoggerFactory.getLogger(JsonSingleFileCache.class);

	private File cacheFile;

	private AtomicReference<DataRoot> root = new AtomicReference<>();

	protected static class DataRoot
	{
		private LinkedHashMap<String, JsonNode>	data	= new LinkedHashMap<>();
		private LinkedHashMap<String, Class<?>>	types	= new LinkedHashMap<>();

		public DataRoot()
		{
			super();

		}

		public LinkedHashMap<String, JsonNode> getData()
		{
			return this.data;
		}

		public LinkedHashMap<String, Class<?>> getTypes()
		{
			return this.types;
		}

	}

	public JsonSingleFileCache(File cacheFile)
	{
		super();
		this.cacheFile = cacheFile;
	}

	@Override
	public <V> V get(String key, Class<V> type)
	{
		return this.readFromJsonNode(	this.getOrCreateRoot()
											.getData()
											.get(key),
										type);
	}

	private <V> V readFromJsonNode(JsonNode jsonNode, Class<V> type)
	{
		return JSONHelper.readFromString(JSONHelper.prettyPrint(jsonNode), type);
	}

	private JsonNode convertToJsonNode(Object value)
	{
		return JSONHelper.readFromString(JSONHelper.prettyPrint(value), JsonNode.class);
	}

	@Override
	public void put(String key, Object value)
	{
		this.operateOnRootAndGet(t ->
		{
			t	.getData()
				.put(key, this.convertToJsonNode(value));
			t	.getTypes()
				.put(key, value.getClass());
			return t;
		});

	}

	@Override
	public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
	{
		return this.readFromJsonNode(	Optional.ofNullable(this.getOrCreateRoot()
																.getData()
																.get(key))
												.orElseGet(() -> this	.operateOnRootAndGet(t ->
												{
													t	.getData()
														.computeIfAbsent(key, (id) ->
														{
															V value = supplier.get();
															t	.getTypes()
																.put(key, value.getClass());
															return this.convertToJsonNode(value);
														});
													return t;
												})
																		.getData()
																		.get(key)),
										type);

	}

	@Override
	public Set<String> keySet()
	{
		return new HashSet<>(this	.getOrCreateRoot()
									.getData()
									.keySet());
	}

	protected DataRoot getOrCreateRoot()
	{
		DataRoot retmap = this.root.get();

		if (retmap == null)
		{
			this.operateOnRootAndGet(r -> r == null ? this.loadRoot() : null, () -> this.root.get());
			retmap = this.root.get();
		}

		return retmap;
	}

	public DataRoot operateOnRootAndGet(UnaryOperator<DataRoot> updateFunction)
	{
		return this.operateOnRootAndGet(updateFunction, () -> this.getOrCreateRoot());
	}

	public DataRoot operateOnRootAndGet(UnaryOperator<DataRoot> updateFunction, Supplier<DataRoot> initialDataRoot)
	{
		synchronized (this.root)
		{
			this.root.set(updateFunction.apply(initialDataRoot.get()));
			this.writeCacheFile();
		}
		return this.root.get();
	}

	private DataRoot loadRoot()
	{
		return Optional	.ofNullable(this.readFromCacheFile())
						.orElseGet(() -> new DataRoot());

	}

	private DataRoot readFromCacheFile()
	{
		DataRoot retval = null;
		if (this.cacheFile.exists() && this.cacheFile.isFile())
		{
			try
			{
				String json = FileUtils.readFileToString(this.cacheFile, UTF_8);
				retval = StringUtils.isBlank(json) ? null : JSONHelper.readFromString(json, DataRoot.class);
			}
			catch (Exception e)
			{
				LOG.error("Exception reading file cache: " + this.cacheFile, e);
				retval = null;
			}
		}
		return retval;
	}

	private void writeCacheFile()
	{
		try
		{
			String json = JSONHelper.prettyPrint(this.root.get());
			FileUtils.writeStringToFile(this.cacheFile, json, UTF_8);
		}
		catch (Exception e)
		{
			LOG.error("Exception writing json to cache file: " + this.cacheFile, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> Class<V> getType(String key)
	{
		return (Class<V>) this	.getOrCreateRoot()
								.getTypes()
								.get(key);
	}

	@Override
	public void remove(String key)
	{
		if (this.getOrCreateRoot()
				.getData()
				.containsKey(key))
		{
			this.operateOnRootAndGet(r ->
			{
				r	.getData()
					.remove(key);
				r	.getTypes()
					.remove(key);
				return r;
			});
		}

	}

}