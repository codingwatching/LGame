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
package loon.geom;

import loon.utils.StringKeyValue;

public class Padding {

	private int _left;
	private int _top;
	private int _right;
	private int _bottom;

	public Padding() {
		this(0, 0, 0, 0);
	}

	public Padding(int left, int top, int right, int bottom) {
		this._left = left;
		this._top = top;
		this._right = right;
		this._bottom = bottom;
	}

	public int getLeft() {
		return _left;
	}

	public void setLeft(int left) {
		this._left = left;
	}

	public int getTop() {
		return _top;
	}

	public void setTop(int top) {
		this._top = top;
	}

	public int getRight() {
		return _right;
	}

	public void setRight(int right) {
		this._right = right;
	}

	public int getBottom() {
		return _bottom;
	}

	public void setBottom(int bottom) {
		this._bottom = bottom;
	}

	@Override
	public String toString() {
		StringKeyValue builder = new StringKeyValue("Padding");
		builder.kv("left", _left).comma().kv("top", _top).comma().kv("right", _right).comma().kv("bottom", _bottom);
		return builder.toString();
	}
}
