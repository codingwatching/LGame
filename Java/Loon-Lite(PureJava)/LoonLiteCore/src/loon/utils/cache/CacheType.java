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
package loon.utils.cache;

/**
 * 具体的缓存类型,标识用,用户可以自行扩展
 */
public class CacheType {

	public final static CacheType TYPE_POOL = new CacheType("POOL", 0);

	private final String _name;

	private final int _code;

	public CacheType(String name, int code) {
		this._name = name;
		this._code = code;
	}

	public String getName() {
		return _name;
	}

	public int getTypeCode() {
		return _code;
	}

}
