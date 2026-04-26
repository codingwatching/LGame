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

	public final static <T> Array<T> create() {
		return new Array<T>();
	}

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
		private final Array<T> list;

		ListItr(Array<T> l) {
			this.list = l;
		}

		@Override
		public boolean hasNext() {
			return list.hasNext();
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
	private ArrayNode<T> _items;
	private int _length;
	private boolean _close;
	private ArrayNode<T> _next_tmp, _previous_tmp;
	private int _next_count, _previous_count;

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
		if (previous == _items) {
			addFront(newNode.data);
		} else if (next == _items) {
			addBack(newNode.data);
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
		for (int i = size() - 1; i > -1; i--) {
			tmp.add(get(i));
		}
		return tmp;
	}

	public Array<T> addAll(Array<T> data) {
		while (data.hasNext()) {
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
		return slice(start, size());
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
		addBack(data);
		return this;
	}

	public Array<T> addFront(T data) {
		if (_close) {
			return this;
		}
		ArrayNode<T> newNode = new ArrayNode<T>();
		newNode.data = data;
		newNode.next = _items.next;
		_items.next.previous = newNode;
		_items.next = newNode;
		newNode.previous = _items;
		_length++;
		return this;
	}

	public Array<T> addBack(T data) {
		if (_close) {
			return this;
		}
		ArrayNode<T> newNode = new ArrayNode<T>();
		newNode.data = data;
		newNode.previous = _items.previous;
		_items.previous.next = newNode;
		_items.previous = newNode;
		newNode.next = _items;
		_length++;
		return this;
	}

	public T get(int idx) {
		if (_close || idx < 0 || idx >= _length) {
			return null;
		}
		ArrayNode<T> node;
		if (idx < _length / 2) {
			node = _items.next;
			for (int i = 0; i < idx; i++) {
				node = node.next;
			}
		} else {
			node = _items.previous;
			for (int i = _length - 1; i > idx; i--) {
				node = node.previous;
			}
		}
		return node.data;
	}

	public Array<T> set(int idx, T v) {
		if (_close || idx < 0) {
			return this;
		}
		while (idx >= _length) {
			add(null);
		}
		ArrayNode<T> node = idx < _length / 2 ? _items.next : _items.previous;
		if (idx < _length / 2) {
			for (int i = 0; i < idx; i++) {
				node = node.next;
			}
		} else {
			for (int i = _length - 1; i > idx; i--) {
				node = node.previous;
			}
		}
		node.data = v;
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
		ArrayNode<T> o = _items.next;
		while (o != _items) {
			if (identity || data == null) {
				if (o.data == data) {
					return true;
				}
			} else {
				if (data.equals(o.data)) {
					return true;
				}
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
		ArrayNode<T> o = _items.next;
		while (o != _items && count < _length) {
			if (identity || data == null) {
				if (o.data == data) {
					return count;
				}
			} else {
				if (data.equals(o.data)) {
					return count;
				}
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
		ArrayNode<T> o = _items.previous;
		while (o != _items && count > 0) {
			if (identity || data == null) {
				if (o.data == data) {
					return count;
				}
			} else {
				if (data.equals(o.data)) {
					return count;
				}
			}
			o = o.previous;
			count--;
		}
		return -1;
	}

	public ArrayNode<T> find(T data) {
		if (_close || data == null) {
			return null;
		}
		ArrayNode<T> o = _items.next;
		while (o != _items && !data.equals(o.data)) {
			o = o.next;
		}
		return o == _items ? null : o;
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
		if (_close || idx < 0 || idx >= _length) {
			return false;
		}
		ArrayNode<T> node = idx < _length / 2 ? _items.next : _items.previous;
		if (idx < _length / 2) {
			for (int i = 0; i < idx; i++) {
				node = node.next;
			}
		} else {
			for (int i = _length - 1; i > idx; i--) {
				node = node.previous;
			}
		}
		node.previous.next = node.next;
		node.next.previous = node.previous;
		_length--;
		return true;
	}

	public boolean remove(T data) {
		if (_close || data == null) {
			return false;
		}
		ArrayNode<T> toDelete = find(data);
		if (toDelete != null && toDelete != _items) {
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
		if (isEmpty()) {
			return null;
		}
		T o = _items.previous.data;
		remove(o);
		return o;
	}

	public T previousPop() {
		if (isEmpty()) {
			return null;
		}
		T o = _items.previous.data;
		remove(o);
		return isEmpty() ? null : _items.previous.data;
	}

	public boolean isFirst(Array<T> o) {
		return o._items.previous == this._items;
	}

	public boolean isLast(Array<T> o) {
		return o._items.next == this._items;
	}

	public T random() {
		return _length == 0 ? null : get(MathUtils.random(0, _length - 1));
	}

	public Array<T> randomArrays() {
		if (_length == 0) {
			return new Array<T>();
		}
		T v = null;
		Array<T> newArrays = new Array<T>();
		for (; hasNext();) {
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
			_next_tmp = _items.next;
			_next_count++;
			return _next_tmp.data;
		}
		if (_next_tmp != _items && _next_count < _length) {
			_next_tmp = _next_tmp.next;
			_next_count++;
			return _next_tmp.data;
		} else {
			stopNext();
			return null;
		}
	}

	public boolean hasNext() {
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
			_previous_tmp = _items.previous;
			_previous_count++;
			return _previous_tmp.data;
		}
		if (_previous_tmp != _items && _previous_count < _length) {
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
		ArrayNode<T> o = _items.next;
		StrBuilder buffer = new StrBuilder(32);
		buffer.append('[');
		int count = 0;
		while (o != _items) {
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
		if (this == o) {
			return true;
		}
		if (!(o instanceof Array)) {
			return false;
		}
		Array<?> array = (Array<?>) o;
		if (_length != array._length) {
			return false;
		}
		ArrayNode<?> n1 = _items.next;
		ArrayNode<?> n2 = array._items.next;
		while (n1 != _items && n2 != array._items) {
			Object o1 = n1.data;
			Object o2 = n2.data;
			if (!(o1 == null ? o2 == null : o1.equals(o2))) {
				return false;
			}
			n1 = n1.next;
			n2 = n2.next;
		}
		return true;
	}

	public T first() {
		return isEmpty() ? null : _items.next.data;
	}

	public T last() {
		return isEmpty() ? null : _items.previous.data;
	}

	public T peek() {
		return last();
	}

	public void sort(Comparator<T> compar) {
		if (_length <= 1 || compar == null) {
			return;
		}
		ArrayNode<T> headData = _items.next;
		while (headData != _items) {
			ArrayNode<T> dstData = headData.next;
			while (dstData != _items) {
				if (compar.compare(headData.data, dstData.data) > 0) {
					T temp = headData.data;
					headData.data = dstData.data;
					dstData.data = temp;
				}
				dstData = dstData.next;
			}
			headData = headData.next;
		}
	}

	public int getNodeCount() {
		return _length;
	}

	@Override
	public void clear() {
		_close = false;
		_length = 0;
		stopNext();
		stopPrevious();
		_items = new ArrayNode<T>();
		_items.next = _items;
		_items.previous = _items;
	}

	@Override
	public int size() {
		return _length;
	}

	@Override
	public boolean isEmpty() {
		return _close || _length == 0;
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
		if (fromIndex < 0 || fromIndex >= _length || toIndex < 0 || toIndex >= _length || fromIndex > toIndex) {
			throw new LSysException(
					"Index out of bounds on call to subList with from of " + fromIndex + " and to " + toIndex);
		}
		Array<T> list = new Array<T>();
		ArrayNode<T> cur = _items.next;
		int count = 0;
		while (cur != _items) {
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
		stopNext();
		return _iterator;
	}

	public Array<T> where(QueryEvent<T> test) {
		Array<T> list = new Array<T>();
		while (hasNext()) {
			T t = next();
			if (test.hit(t)) {
				list.add(t);
			}
		}
		stopNext();
		return list;
	}

	public T find(QueryEvent<T> test) {
		while (hasNext()) {
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
		while (hasNext()) {
			T t = next();
			if (test.hit(t)) {
				stopNext();
				return remove(t);
			}
		}
		stopNext();
		return false;
	}

	public void queryEach(QueryEvent<T> event) {
		if (event == null || isEmpty()) {
			return;
		}
		ArrayNode<T> node = _items.next;
		while (node != _items) {
			event.hit(node.data);
			node = node.next;
		}
	}

	public int replaceAll(T oldVal, T newVal) {
		if (isEmpty()) {
			return 0;
		}
		int count = 0;
		ArrayNode<T> node = _items.next;
		while (node != _items) {
			boolean match = (oldVal == null ? node.data == null : oldVal.equals(node.data));
			if (match) {
				node.data = newVal;
				count++;
			}
			node = node.next;
		}
		return count;
	}

	public boolean removeAll(Array<T> data) {
		if (data == null || data.isEmpty() || isEmpty()) {
			return false;
		}
		boolean modified = false;
		ArrayNode<T> node = _items.next;
		while (node != _items) {
			ArrayNode<T> next = node.next;
			if (data.contains(node.data)) {
				remove(node.data);
				modified = true;
			}
			node = next;
		}
		return modified;
	}

	public Object[] toArray() {
		if (isEmpty()) {
			return new Object[0];
		}
		Object[] arr = new Object[_length];
		ArrayNode<T> node = _items.next;
		for (int i = 0; i < _length; i++) {
			arr[i] = node.data;
			node = node.next;
		}
		return arr;
	}

	public void dispose() {
		_close = true;
		_length = 0;
		_items = null;
		_next_tmp = null;
		_previous_tmp = null;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		while (hasNext()) {
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