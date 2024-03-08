/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
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
 * @version 0.5
 */
package loon;

import loon.utils.Bundle;

public class EmptyBundle implements Bundle<Object> {

	@Override
	public void put(String key, Object value) {
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return null;
	}

	@Override
	public Object remove(String key) {
		return null;
	}

	@Override
	public Object remove(String key, Object defaultValue) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean isNotEmpty() {
		return false;
	}
}
