/**
 * Copyright 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.4.2
 */
package loon.utils;

import java.util.Comparator;
import java.util.Iterator;

import loon.LRelease;
import loon.LSysException;
import loon.LSystem;
import loon.events.QueryEvent;

public class Array<T> implements Iterable<T>, IArray, LRelease {

	public static <T> Array<T> of(T[] list) {
		final Array<T> result = new Array<T>();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				T o = list[i];
				if (o != null) {
					result.add(o);
				}
			}
		}
		return result;
	}

	private static class ListItr<T> implements LIterator<T> {

		private Array<T> list;

		ListItr(Array<T> l) {
			this.list = l;
		}

		@Override
		public boolean hasNext() {
			return list.hashNext();
		}

		@Override
		public T next() {
			return list.next();
		}

		@Override
		public void remove() {
			list.remove();
		}

	}

	public static final <T> Array<T> at() {
		return new Array<T>();
	}

	public static final <T> Array<T> at(Array<T> data) {
		return new Array<T>(data);
	}

	public static class ArrayNode<T> {

		public ArrayNode<T> next;
		public ArrayNode<T> previous;
		public T data;

		public ArrayNode() {
			this.next = null;
			this.previous = null;
			this.data = null;
		}
	}

	private LIterator<T> _iterator;

	private ArrayNode<T> _items = null;

	private int _length;

	private boolean _close;

	private ArrayNode<T> _next_tmp = null, _previous_tmp = null;

	private int _next_count = 0, _previous_count = 0;

	public Array() {
		clear();
	}

	public Array(Array<T> data) {
		clear();
		addAll(data);
	}

	public Array<T> insertBetween(Array<T> previous, Array<T> next, Array<T> newNode) {
		return insertBetween(previous._items, next._items, newNode._items);
	}

	public Array<T> insertBetween(ArrayNode<T> previous, ArrayNode<T> next, ArrayNode<T> newNode) {
		if (_close) {
			return this;
		}
		if (previous == this._items && next != this._items) {
			this.addFront(newNode.data);
		} else if (previous != this._items && next == this._items) {
			this.addBack(newNode.data);
		} else {
			newNode.next = next;
			newNode.previous = previous;
			previous.next = newNode;
			next.previous = newNode;
		}
		return this;
	}

	public Array<T> reverse() {
		Array<T> tmp = new Array<T>();
		for (int i = size() - 1; i > -1; --i) {
			tmp.add(get(i));
		}
		return tmp;
	}

	public Array<T> addAll(Array<T> data) {
		for (; data.hashNext();) {
			add(data.next());
		}
		return data.stopNext();
	}

	public Array<T> concat(Array<T> data) {
		Array<T> list = new Array<T>();
		list.addAll(this);
		list.addAll(data);
		return list;
	}

	public Array<T> slice(int start) {
		return slice(start, this.size());
	}

	public Array<T> slice(int start, int end) {
		Array<T> list = new Array<T>();
		for (int i = start; i < end; i++) {
			list.add(get(i));
		}
		return list;
	}

	public Array<T> push(T data) {
		return add(data);
	}

	public Array<T> add(T data) {
		ArrayNode<T> newNode = new ArrayNode<T>();
		ArrayNode<T> o = this._items.next;
		newNode.data = data;
		if (o == this._items) {
			this.addFront(data);
		} else {
			for (; o != this._items;) {
				o = o.next;
			}
			if (o == this._items) {
				this.addBack(newNode.data);
			}
		}
		return this;
	}

	public Array<T> addFront(T data) {
		if (_close) {
			return this;
		}
		ArrayNode<T> newNode = new ArrayNode<T>();
		newNode.data = data;
		newNode.next = this._items.next;
		this._items.next.previous = newNode;
		this._items.next = newNode;
		newNode.previous = this._items;
		_length++;
		return this;
	}

	public Array<T> addBack(T data) {
		if (_close) {
			return this;
		}
		ArrayNode<T> newNode = new ArrayNode<T>();
		newNode.data = data;
		newNode.previous = this._items.previous;
		this._items.previous.next = newNode;
		this._items.previous = newNode;
		newNode.next = this._items;
		_length++;
		return this;
	}

	public T get(int idx) {
		if (_close) {
			return null;
		}
		int size = _length - 1;
		if (0 <= idx && idx <= size) {
			ArrayNode<T> o = this._items.next;
			int count = 0;
			for (; count < idx;) {
				o = o.next;
				count++;
			}
			return o.data;
		} else if (idx == size) {
			return _items.data;
		}
		return null;
	}

	public Array<T> set(int idx, T v) {
		if (_close) {
			return this;
		}
		int size = _length - 1;

		if (0 <= idx && idx <= size) {
			ArrayNode<T> o = this._items.next;
			int count = 0;
			for (; count < idx;) {
				o = o.next;
				count++;
			}
			o.data = v;
		} else if (idx == size) {
			_items.data = v;
		} else if (idx > size) {
			for (int i = size; i < idx; i++) {
				add(null);
			}
			set(idx, v);
		}
		return this;
	}

	public ArrayNode<T> node() {
		return _items;
	}

	public boolean contains(T data) {
		return contains(data, false);
	}

	public boolean contains(T data, boolean identity) {
		if (_close) {
			return false;
		}
		ArrayNode<T> o = this._items.next;
		for (; o != this._items;) {
			if ((identity || data == null) && o.data == data) {
				return true;
			} else if (data.equals(o.data)) {
				return true;
			}
			o = o.next;
		}
		return false;
	}

	public int indexOf(T data) {
		return indexOf(data, false);
	}

	public int indexOf(T data, boolean identity) {
		if (_close) {
			return -1;
		}
		int count = 0;
		ArrayNode<T> o = this._items.next;
		for (; o != this._items && count < _length;) {
			if ((identity || data == null) && o.data == data) {
				return count;
			} else if (data.equals(o.data)) {
				return count;
			}
			o = o.next;
			count++;
		}
		return -1;
	}

	public int lastIndexOf(T data) {
		return lastIndexOf(data, false);
	}

	public int lastIndexOf(T data, boolean identity) {
		if (_close) {
			return -1;
		}
		int count = _length - 1;
		ArrayNode<T> o = this._items.previous;
		for (; o != this._items && count > 0;) {
			if ((identity || data == null) && o.data == data) {
				return count;
			} else if (data.equals(o.data)) {
				return count;
			}
			o = o.previous;
			count--;
		}
		return -1;
	}

	public ArrayNode<T> find(T data) {
		if (_close) {
			return null;
		}
		ArrayNode<T> o = this._items.next;
		for (; o != this._items && !data.equals(o.data);) {
			o = o.next;
		}
		if (o == this._items) {
			return null;
		}
		return o;
	}

	public T removeFirst() {
		if (_close) {
			return null;
		}
		T result = first();
		remove(0);
		return result;
	}

	public T removeLast() {
		if (_close) {
			return null;
		}
		T result = last();
		remove(_length < 1 ? 0 : _length - 1);
		return result;
	}

	public boolean remove(int idx) {
		if (_close) {
			return false;
		}
		int size = _length - 1;
		if (0 <= idx && idx <= size) {
			ArrayNode<T> o = this._items.next;
			int count = 0;
			for (; count < idx;) {
				o = o.next;
				count++;
			}
			return remove(o.data);
		} else if (idx == size) {
			return remove(_items.data);
		}
		return false;
	}

	public boolean remove(T data) {
		if (_close) {
			return false;
		}
		ArrayNode<T> toDelete = this.find(data);
		if (toDelete != this._items && toDelete != null) {
			toDelete.previous.next = toDelete.next;
			toDelete.next.previous = toDelete.previous;
			_length--;
			return true;
		}
		return false;
	}

	public boolean remove() {
		int tsSize = _length;
		return remove(--tsSize);
	}

	public T pop() {
		T o = null;
		if (!isEmpty()) {
			o = this._items.previous.data;
			remove(o);
		}
		return o;
	}

	public T previousPop() {
		T o = null;
		o = this._items.previous.data;
		remove(o);
		return this._items.previous.data;
	}

	public boolean isFirst(Array<T> o) {
		if (o._items.previous == this._items) {
			return true;
		}
		return false;
	}

	public boolean isLast(Array<T> o) {
		if (o._items.next == this._items) {
			return true;
		}
		return false;
	}

	public T random() {
		if (_length == 0) {
			return null;
		}
		return get(MathUtils.random(0, _length - 1));
	}

	public Array<T> randomArrays() {
		if (_length == 0) {
			return new Array<T>();
		}
		T v = null;
		Array<T> newArrays = new Array<T>();
		for (; hashNext();) {
			newArrays.add(next());
		}
		stopNext();
		for (int i = 0; i < _length; i++) {
			v = random();
			for (int j = 0; j < i; j++) {
				if (newArrays.get(j) == v) {
					v = random();
					j = -1;
				}

			}
			newArrays.set(i, v);
		}
		return newArrays;
	}

	@Override
	public String toString() {
		return toString(LSystem.COMMA);
	}

	public T next() {
		if (isEmpty()) {
			return null;
		}
		if (_next_count == 0) {
			_next_tmp = this._items.next;
			_next_count++;
			return _next_tmp.data;
		}
		if (_next_tmp != this._items && _next_count < _length) {
			_next_tmp = _next_tmp.next;
			_next_count++;
			return _next_tmp.data;
		} else {
			stopNext();
			return null;
		}
	}

	public boolean hashNext() {
		return _next_count < _length;
	}

	public int idxNext() {
		return _next_count;
	}

	public Array<T> stopNext() {
		_next_tmp = null;
		_next_count = 0;
		return this;
	}

	public T previous() {
		if (isEmpty()) {
			return null;
		}
		if (_previous_count == 0) {
			_previous_tmp = this._items.previous;
			_previous_count++;
			return _previous_tmp.data;
		}
		if (_previous_tmp != this._items && _previous_count < _length) {
			_previous_tmp = _previous_tmp.previous;
			_previous_count++;
			return _previous_tmp.data;
		} else {
			stopPrevious();
			return null;
		}
	}

	public int idxPrevious() {
		return _previous_count;
	}

	public Array<T> stopPrevious() {
		_previous_tmp = null;
		_previous_count = 0;
		return this;
	}

	public String toString(char split) {
		if (isEmpty()) {
			return "[]";
		}
		ArrayNode<T> o = this._items.next;
		StrBuilder buffer = new StrBuilder(32);
		buffer.append('[');
		int count = 0;
		for (; o != this._items;) {
			buffer.append(o.data);
			if (count != _length - 1) {
				buffer.append(split);
			}
			o = o.next;
			count++;
		}
		buffer.append(']');
		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Array)) {
			return false;
		}
		Array<?> array = (Array<?>) o;
		if (_length != array._length) {
			return false;
		}
		ArrayNode<?> items1 = this._items;
		ArrayNode<?> items2 = array._items;
		if (items1 == items2) {
			return true;
		}
		if (items1 == null || items2 == null) {
			return false;
		}
		for (int i = 0; i < _length; i++) {
			Object o1 = items1.next.data;
			Object o2 = items2.next.data;
			items1 = items1.next;
			items2 = items2.next;
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
		}
		return true;
	}

	public T first() {
		if (this.isEmpty()) {
			return null;
		} else {
			return this._items.next.data;
		}
	}

	public T last() {
		if (this.isEmpty()) {
			return null;
		} else {
			return this._items.previous.data;
		}
	}

	public T peek() {
		return last();
	}

	public void sort(Comparator<T> compar) {
		if (_length <= 1) {
			return;
		}
		ArrayNode<T> headData = _items.next, dstData = null;
		if (headData == null) {
			return;
		} else {
			T temp;
			for (; headData != null && headData.data != null;) {
				dstData = headData.next;
				for (; dstData != null && dstData.data != null;) {
					if (compar.compare(headData.data, dstData.data) > 0) {
						temp = headData.data;
						headData.data = dstData.data;
						dstData.data = temp;
					}
					dstData = dstData.next;
				}
				headData = headData.next;
			}
		}
	}

	public int getNodeCount() {
		int count = 0;
		ArrayNode<T> headData = _items.next;
		for (; headData != null && headData.data != null;) {
			count++;
			headData = headData.next;
		}
		return count;
	}

	@Override
	public void clear() {
		this._close = false;
		this._length = 0;
		this.stopNext();
		this.stopPrevious();
		this._items = null;
		this._items = new ArrayNode<T>();
		this._items.next = this._items;
		this._items.previous = this._items;
	}

	@Override
	public int size() {
		return _length;
	}

	@Override
	public boolean isEmpty() {
		return _close || _length == 0 || this._items.next == this._items;
	}

	@Override
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	public boolean isClosed() {
		return _close;
	}

	public Array<T> cpy() {
		Array<T> newlist = new Array<T>();
		newlist.addAll(this);
		return newlist;
	}

	public Array<T> subList(final int fromIndex, final int toIndex) {
		if (fromIndex < 0 || fromIndex > this._length - 1 || toIndex < 0 || toIndex > this._length - 1) {
			throw new LSysException(
					"Index out of bounds on call to subList with from of " + fromIndex + " and to " + toIndex);
		}
		Array<T> list = new Array<T>();
		ArrayNode<T> cur = _items.next;
		int count = 0;
		for (; cur != null && cur.data != null;) {
			if (count >= fromIndex && count <= toIndex) {
				list.add(cur.data);
			}
			cur = cur.next;
			count++;
		}
		return list;
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	public LIterator<T> listIterator() {
		if (_iterator == null) {
			_iterator = new ListItr<T>(this);
		}
		this.stopNext();
		return _iterator;
	}

	public Array<T> where(QueryEvent<T> test) {
		Array<T> list = new Array<T>();
		for (; hashNext();) {
			T t = next();
			if (test.hit(t)) {
				list.add(t);
			}
		}
		stopNext();
		return list;
	}

	public T find(QueryEvent<T> test) {
		for (; hashNext();) {
			T t = next();
			if (test.hit(t)) {
				stopNext();
				return t;
			}
		}
		stopNext();
		return null;
	}

	public boolean remove(QueryEvent<T> test) {
		for (; hashNext();) {
			T t = next();
			if (test.hit(t)) {
				stopNext();
				return remove(t);
			}
		}
		stopNext();
		return false;
	}

	public void dispose() {
		_close = true;
		_length = 0;
		_items = null;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		for (; hashNext();) {
			Object obj = next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		stopNext();
		return hashCode;
	}

	@Override
	public void close() {
		dispose();
	}

}
