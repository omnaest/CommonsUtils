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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.annotation.JsonCreator;

@RunWith(Parameterized.class)
public class CacheUtilsTest
{
	private UnaryCache<Value> cache;

	public static class Value
	{
		private String value;

		@JsonCreator
		public Value(String value)
		{
			super();
			this.value = value;
		}

		public Value()
		{
			super();
		}

		public String getValue()
		{
			return this.value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

	}

	@Parameters
	public static Iterable<? extends Object> data() throws IOException
	{
		File tempFile = File.createTempFile("testCache", "json"); // new File("C:/Temp/cacheTest.json");
		Supplier<Cache> supplier1 = () -> new ConcurrentHashMapCache();
		Supplier<Cache> supplier2 = () -> new JsonSingleFileCache(tempFile);
		return Arrays	.<Supplier<Cache>>asList(supplier1, supplier2)
						.stream()
						.skip(0)
						.collect(Collectors.toList());
	}

	public CacheUtilsTest(Supplier<Cache> cacheSupplier)
	{
		super();
		this.cache = new CacheToUnaryCacheAdapter<>(cacheSupplier.get(), Value.class);
		this.cache.clear();
	}

	@Test
	public void testGet() throws Exception
	{
		this.cache.put("key1", new Value("test"));
		assertEquals("test", this.cache	.get("key1")
										.getValue());
		assertEquals(1, this.cache.size());
		assertFalse(this.cache.isEmpty());
	}

	@Test
	public void testRemove() throws Exception
	{
		this.cache.put("key1", new Value("test1"));
		this.cache.put("key2", new Value("test2"));

		this.cache.remove("key1");
		assertEquals(1, this.cache.size());
		assertFalse(this.cache.isEmpty());

		this.cache.clear();
		assertEquals(0, this.cache.size());
		assertTrue(this.cache.isEmpty());
	}

	@Test
	public void testComputeIfAbsent() throws Exception
	{
		Value value = this.cache.computeIfAbsent("key1", () -> new Value("value1"));
		assertEquals("value1", value.getValue());
	}

}
