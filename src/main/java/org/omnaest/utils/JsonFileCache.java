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
package org.omnaest.utils;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;

/**
 * Simple {@link JsonFileCache} which uses json to store element from a {@link Supplier} within a given {@link File}
 *
 * @author Omnaest
 * @param <T>
 */
public class JsonFileCache<T> implements Supplier<T>
{
	private static final String UTF_8 = "utf-8";

	private Supplier<T>			supplier;
	private Class<? super T>	type;
	private File				cacheFile;

	public JsonFileCache(File cacheFile, Supplier<T> supplier, Class<? super T> type)
	{
		super();
		this.cacheFile = cacheFile;
		this.supplier = supplier;
		this.type = type;
	}

	@Override
	public T get()
	{
		T element = null;

		if (this.cacheFile.exists() && this.cacheFile.isFile())
		{
			element = this.loadElementFromCache();
		}

		if (element == null)
		{
			element = this.supplier.get();
			this.writeElementToCache(element);
		}
		return element;
	}

	@SuppressWarnings("unchecked")
	private T loadElementFromCache()
	{
		T element = null;
		try
		{
			element = (T) JSONHelper.readFromString(FileUtils.readFileToString(this.cacheFile, UTF_8), this.type);
		}
		catch (IOException e)
		{
		}
		return element;
	}

	private void writeElementToCache(T element)
	{
		try
		{
			String data = JSONHelper.prettyPrint(element);
			FileUtils.writeStringToFile(this.cacheFile, data, UTF_8);
		}
		catch (IOException e)
		{
		}
	}

}
