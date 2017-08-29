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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class StreamUtilsTest
{

	@Test
	public void testFromSupplier() throws Exception
	{
		List<String> source = Arrays.asList("1", "2", "3");
		Iterator<String> iterator = source.iterator();
		List<String> result = StreamUtils	.fromSupplier(() -> iterator.hasNext() ? iterator.next() : null, e -> e == null)
											.collect(Collectors.toList());

		assertEquals(source, result);
	}

	@Test
	public void testFromIterator() throws Exception
	{
		Iterator<String> iterator = Arrays	.asList("1", "2", "3")
											.iterator();

		List<String> collect = StreamUtils	.fromIterator(iterator)
											.limit(1)
											.collect(Collectors.toList());
		List<String> rest = StreamUtils	.fromIterator(iterator)
										.collect(Collectors.toList());

		assertEquals(1, collect.size());
		assertEquals(2, rest.size());
	}

	@Test
	public void testFromIteratorFlatMap() throws Exception
	{
		AtomicInteger counter = new AtomicInteger();
		Stream<String> stream = Arrays	.asList(new String[] { "1", "2" }, new String[] { "3", "4" })
										.stream()
										.map(array -> Arrays.asList(array)
															.iterator())
										.flatMap(iterator -> StreamUtils.fromIterator(iterator)
																		.peek(value -> counter.getAndIncrement()));

		List<String> collect = stream	.limit(1)
										.collect(Collectors.toList());

		assertEquals(1, collect.size());
		assertEquals(2, counter.get());
	}

	@Test
	public void testConcat() throws Exception
	{
		List<String> collect = StreamUtils	.concat(Arrays	.asList(Arrays	.asList("1", "2")
																		.stream(),
																Arrays	.asList("3", "4")
																		.stream())
															.stream())
											.collect(Collectors.toList());
		assertEquals(Arrays.asList("1", "2", "3", "4"), collect);
	}

	@Test
	public void testReverse() throws Exception
	{
		assertEquals(Arrays.asList("c", "b", "a"), StreamUtils	.reverse(Arrays	.asList("a", "b", "c")
																				.stream())
																.collect(Collectors.toList()));
	}

}
