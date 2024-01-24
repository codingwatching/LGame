/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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
package loon.utils.reply;

import loon.LRelease;
import loon.geom.IV;
import loon.geom.SetIV;

public class TValue<T> extends Nullable<T> implements SetIV<T>, IV<T>, LRelease {

	public static final <T> TValue<T> of(T obj) {
		return new TValue<T>(obj);
	}

	public static final <T> Nullable<T> ofNull(T obj) {
		return new TValue<T>(obj);
	}

	public TValue(T v) {
		super(v);
	}

	@Override
	public void set(T v) {
		this._value = v;
	}

	public T result() {
		return _value;
	}

	public TValue<T> cpy() {
		return new TValue<T>(_value);
	}

	public Nullable<T> toNullable() {
		return new Nullable<T>(_value);
	}

	public ObservableValue<T> observable(TChange<T> v) {
		return ObservableValue.at(v, this, _value);
	}

}
