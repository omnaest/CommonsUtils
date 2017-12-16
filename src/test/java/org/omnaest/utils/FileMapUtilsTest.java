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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @see FileMapUtils
 * @author omnaest
 */
public class FileMapUtilsTest
{
	@Test
	public void testToJsonFileSynchronizedMap() throws Exception
	{
		File file = new File("C:/Temp/syncMapTest.json");
		Map<String, String> map1 = FileMapUtils.toJsonFileSynchronizedMap(	MapUtils.builder()
																					.put("key1", "value1")
																					.build(),
																			file);

		assertTrue(map1.containsKey("key1"));
		map1.put("key2", "value2");

		map1.put("key3", "value3");
		map1.remove("key3");

		assertEquals(2, map1.size());
		assertEquals("value1", map1.get("key1"));
		assertEquals("value2", map1.get("key2"));

		Map<String, String> map2 = FileMapUtils.toJsonFileSynchronizedMap(new HashMap<>(), file);

		assertEquals(2, map2.size());
		assertEquals("value1", map2.get("key1"));
		assertEquals("value2", map2.get("key2"));
	}

}
