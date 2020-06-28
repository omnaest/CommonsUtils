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
package org.omnaest.utils.element;

import org.apache.commons.lang.ObjectUtils;
import org.omnaest.utils.events.DistributionEventHandler;
import org.omnaest.utils.events.EventHandler;

public class ListenableElement<E>
{
	private E element;

	public static class Change<E>
	{
		private E	previous;
		private E	next;

		public Change(E previous, E next)
		{
			super();
			this.previous = previous;
			this.next = next;
		}

		/**
		 * Element that is currently still the element but is going to be overwritten by the {@link #getNext()} value
		 * 
		 * @see #getNext()
		 * @return
		 */
		public E getPrevious()
		{
			return this.previous;
		}

		/**
		 * Element that is up to be set
		 * 
		 * @see #getPrevious()
		 * @return
		 */
		public E getNext()
		{
			return this.next;
		}

	}

	private DistributionEventHandler<Change<E>> distributionEventHandler = new DistributionEventHandler<>();

	public E get()
	{
		return this.element;
	}

	public ListenableElement<E> set(E element)
	{
		if (!ObjectUtils.equals(this.element, element))
		{
			this.distributionEventHandler.accept(new Change<E>(this.element, element));
		}
		this.element = element;
		return this;
	}

	public ListenableElement<E> registerOnChange(EventHandler<Change<E>> eventHandler)
	{
		this.distributionEventHandler.register(eventHandler);
		return this;
	}

}
