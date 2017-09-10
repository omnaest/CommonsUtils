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

/**
 * @see Cache
 * @author Omnaest
 */
public class CacheUtils
{
	/**
	 * Reads the content of the source {@link Cache} and populates the content into the target {@link Cache}
	 *
	 * @param cacheSource
	 * @param cacheTarget
	 */
	public static <V> void populateCacheContentToNewCache(Cache cacheSource, Cache cacheTarget)
	{
		if (cacheSource != null && cacheTarget != null)
		{
			for (String key : cacheSource.keySet())
			{
				cacheTarget.put(key, cacheSource.get(key, cacheSource.getType(key)));
			}
		}
	}

	public static Cache newConcurrentInMemoryCache()
	{
		return new ConcurrentHashMapCache();
	}

	public static <V> Cache newJsonFileCache(File cacheFile)
	{
		return new JsonSingleFileCache(cacheFile);
	}

	public static <V> Cache newJsonFolderCache(File cacheDirectory)
	{
		return new JsonFolderFilesCache(cacheDirectory);
	}
}
