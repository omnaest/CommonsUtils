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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils
{

	@SafeVarargs
	public static <E> Stream<E> concat(Stream<E>... streams)
	{
		return concat(Arrays.asList(streams)
							.stream());
	}

	public static <E> Stream<E> concat(Stream<Stream<E>> streams)
	{
		return streams	.reduce(Stream::concat)
						.orElseGet(() -> Stream.empty());
	}

	public static <E> Stream<E> fromIterator(Iterator<E> iterator)
	{
		return StreamSupport.stream(((Iterable<E>) () -> iterator).spliterator(), false);
	}

	public static <E> Stream<E> fromSupplier(Supplier<E> supplier, Predicate<E> terminateMatcher)
	{
		return fromIterator(new Iterator<E>()
		{
			private AtomicReference<E> takenElement = new AtomicReference<>();

			@Override
			public boolean hasNext()
			{
				this.takeOneElement();
				return terminateMatcher	.negate()
										.test(this.takenElement.get());
			}

			@Override
			public E next()
			{
				this.takeOneElement();
				return this.takenElement.getAndSet(null);
			}

			private void takeOneElement()
			{
				this.takenElement.getAndUpdate(e -> e != null ? e : supplier.get());
			}
		}).filter(terminateMatcher.negate());

	}

	/**
	 * Reverses the order of the given {@link Stream}. Be aware that this will terminate the given {@link Stream} and returns a new {@link Stream}, which makes
	 * this a TERMINAL operation!!
	 *
	 * @param stream
	 * @return
	 */
	public static <E> Stream<E> reverse(Stream<E> stream)
	{
		List<E> list = Optional	.ofNullable(stream)
								.orElse(Stream.empty())
								.collect(Collectors.toList());
		Collections.reverse(list);
		return list.stream();
	}
}
