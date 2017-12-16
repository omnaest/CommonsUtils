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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.omnaest.utils.FileMapUtils;

public class JSONDirectorySynchronizedValuesMapTest
{
	private static class Domain
	{
		private String field;

		public String getField()
		{
			return this.field;
		}

		public Domain setField(String field)
		{
			this.field = field;
			return this;
		}
	}

	@Test
	public void testJSONDirectorySynchronizedValuesMap() throws Exception
	{
		File directory = new File("C:/Temp/jsonDirectoryTest");
		Map<String, Domain> map = FileMapUtils.toJsonDirectorySynchronizedValuesMap(directory, Domain.class);

		map.put("key1", new Domain().setField("value1"));
		map.put("key2", new Domain().setField("value2"));

		Map<String, Domain> map2 = FileMapUtils.toJsonDirectorySynchronizedValuesMap(directory, Domain.class);
		assertEquals(2, map2.size());
		assertEquals("value1", map2	.get("key1")
									.getField());
		assertEquals("value2", map2	.get("key2")
									.getField());
	}

}
