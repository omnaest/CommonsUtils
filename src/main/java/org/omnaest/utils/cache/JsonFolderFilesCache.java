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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link Cache} which uses {@link JSONHelper} to store the cache content within multiple json {@link File}s
 *
 * @see JsonSingleFileCache
 * @author Omnaest
 * @param <V>
 */
public class JsonFolderFilesCache extends AbstractCache
{
	private static final String	UTF_8	= "utf-8";
	private static final Logger	LOG		= LoggerFactory.getLogger(JsonFolderFilesCache.class);

	private File cacheDirectory;

	private AtomicReference<DataRoot> root = new AtomicReference<>();

	protected static class DataRoot
	{
		private AtomicLong						index	= new AtomicLong();
		private LinkedHashMap<String, Long>		data	= new LinkedHashMap<>();
		private LinkedHashMap<String, Class<?>>	types	= new LinkedHashMap<>();

		public DataRoot()
		{
			super();

		}

		public LinkedHashMap<String, Long> getData()
		{
			return this.data;
		}

		public LinkedHashMap<String, Class<?>> getTypes()
		{
			return this.types;
		}

		public AtomicLong getIndex()
		{
			return this.index;
		}

		public void setIndex(long index)
		{
			this.index.set(index);
		}

		@JsonIgnore
		public Long getNextIndex()
		{
			return this.index.incrementAndGet();
		}

	}

	public JsonFolderFilesCache(File cacheDirectory)
	{
		super();
		this.cacheDirectory = cacheDirectory;
	}

	@Override
	public <V> V get(String key, Class<V> type)
	{
		Long fileIndex = this	.getOrCreateRoot()
								.getData()
								.get(key);
		return this.readFromSingleCacheFile(fileIndex, type);
	}

	private <V> V readFromSingleCacheFile(Long index, Class<V> type)
	{
		String json = null;
		try
		{
			if (index != null)
			{
				json = FileUtils.readFileToString(this.determineCacheFile(index), StandardCharsets.UTF_8);
			}
		}
		catch (IOException e)
		{
			LOG.error("Exception reading single cache file", e);
		}
		return this.readFromJson(json, type);
	}

	private File determineCacheFile(Long fileIndex)
	{
		return new File(this.cacheDirectory, "" + fileIndex + ".json");
	}

	private <V> V readFromJson(String json, Class<V> type)
	{
		return JSONHelper.readFromString(json, type);
	}

	private Long writeToFileAndGetIndex(Object value)
	{
		Long index = this	.getOrCreateRoot()
							.getNextIndex();
		this.writeToSingleCacheFile(value, index);
		return index;
	}

	private void deleteOrphanCacheFile(Long index)
	{
		if (index != null)
		{
			try
			{
				FileUtils.forceDelete(this.determineCacheFile(index));
			}
			catch (IOException e)
			{
				LOG.error("Failed deleting orphan cache file: " + index);
			}
		}
	}

	private void writeToSingleCacheFile(Object value, Long index)
	{
		try
		{
			FileUtils.write(this.determineCacheFile(index), JSONHelper.prettyPrint(value), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			LOG.error("Exception writing single cache file", e);
		}
	}

	@Override
	public void put(String key, Object value)
	{
		this.operateOnRootAndGet(t ->
		{
			this.deleteOrphanCacheFile(t.getData()
										.get(key));
			t	.getData()
				.put(key, this.writeToFileAndGetIndex(value));
			t	.getTypes()
				.put(key, value.getClass());
			return t;
		});

	}

	@Override
	public <V> V computeIfAbsent(String key, Supplier<V> supplier, Class<V> type)
	{
		Long index = this	.getOrCreateRoot()
							.getData()
							.get(key);

		if (index == null)
		{
			index = this.operateOnRootAndGet(t ->
			{
				t	.getData()
					.computeIfAbsent(key, (id) ->
					{
						V value = supplier.get();
						if (value != null)
						{
							t	.getTypes()
								.put(key, value.getClass());
						}

						Long tindex = this.writeToFileAndGetIndex(value);
						t	.getData()
							.put(key, tindex);

						return tindex;
					});
				return t;
			})
						.getData()
						.get(key);
		}

		if (index != null)
		{
			return this.readFromSingleCacheFile(index, type);
		}
		else
		{
			return null;
		}
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
			this.writeRootCacheFile();
		}
		return this.root.get();
	}

	private DataRoot loadRoot()
	{
		return Optional	.ofNullable(this.readFromRootCacheFile())
						.orElseGet(() -> new DataRoot());

	}

	private DataRoot readFromRootCacheFile()
	{
		DataRoot retval = null;
		File rootCacheFile = this.determineRootCacheFile();
		if (rootCacheFile.exists() && rootCacheFile.isFile())
		{
			try
			{
				String json = FileUtils.readFileToString(rootCacheFile, UTF_8);
				retval = StringUtils.isBlank(json) ? null : JSONHelper.readFromString(json, DataRoot.class);
			}
			catch (Exception e)
			{
				LOG.error("Exception reading file cache: " + this.cacheDirectory, e);
				retval = null;
			}
		}
		return retval;
	}

	private void writeRootCacheFile()
	{
		try
		{
			String json = JSONHelper.prettyPrint(this.root.get());
			FileUtils.writeStringToFile(this.determineRootCacheFile(), json, UTF_8);
		}
		catch (Exception e)
		{
			LOG.error("Exception writing json to cache file: " + this.cacheDirectory, e);
		}
	}

	private File determineRootCacheFile()
	{
		return new File(this.cacheDirectory, "root.json");
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
