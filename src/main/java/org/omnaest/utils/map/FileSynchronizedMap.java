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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;

public class FileSynchronizedMap<K, V> extends MapDecorator<K, V>
{
	private File				file;
	private Charset				encoding	= StandardCharsets.UTF_8;
	private Serializer<K, V>	serializer;
	private Deserializer<K, V>	deserializer;

	public static class FileAccessException extends IllegalStateException
	{
		private static final long serialVersionUID = -5661240860248366655L;

		public FileAccessException(Throwable cause)
		{
			super(cause);
		}
	}

	public static interface Serializer<K, V> extends Function<Map<K, V>, String>
	{
	}

	public static interface Deserializer<K, V> extends Function<String, Map<K, V>>
	{
	}

	public FileSynchronizedMap(Map<K, V> map, File file, Serializer<K, V> serializer, Deserializer<K, V> deserializer)
	{
		super(map);
		this.file = file;
		this.serializer = serializer;
		this.deserializer = deserializer;

		this.init(map);
	}

	private void init(Map<K, V> sourceMap)
	{
		//
		boolean emptySourceMap = sourceMap.isEmpty();
		this.loadFromFile();

		//
		if (!emptySourceMap)
		{
			this.synchronizeWithFile();
		}
	}

	private void loadFromFile()
	{
		try
		{
			if (this.file.exists() && this.file.isFile())
			{
				this.putAll(this.deserializer.apply(FileUtils.readFileToString(this.file, this.encoding)));
			}
		} catch (IOException e)
		{
			throw new FileAccessException(e);
		}
	}

	private void synchronizeWithFile()
	{
		try
		{
			FileUtils.writeStringToFile(this.file, this.serializer.apply(this), this.encoding);
		} catch (IOException e)
		{
			throw new FileAccessException(e);
		}
	}

	public FileSynchronizedMap<K, V> setEncoding(Charset encoding)
	{
		this.encoding = encoding;
		return this;
	}

	@Override
	public V put(K key, V value)
	{
		V retval = super.put(key, value);
		this.synchronizeWithFile();
		return retval;
	}

	@Override
	public V remove(Object key)
	{
		V remove = super.remove(key);
		this.synchronizeWithFile();
		return remove;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		super.putAll(m);
		this.synchronizeWithFile();
	}

	@Override
	public void clear()
	{
		super.clear();
		this.synchronizeWithFile();
	}

}
