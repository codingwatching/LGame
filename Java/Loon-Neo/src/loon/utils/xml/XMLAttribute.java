/**
 * Copyright 2008 - 2011
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
 * @version 0.1
 */
package loon.utils.xml;

import loon.LSysException;
import loon.LSystem;
import loon.utils.MathUtils;
import loon.utils.StringUtils;

public class XMLAttribute {

	private String name;

	private String value;

	protected XMLElement element;

	XMLAttribute(String n, String v) {
		this.name = n;
		this.value = v;
	}

	public XMLElement getElement() {
		return element;
	}

	public String getValue() {
		return this.value;
	}

	public int getIntValue() {
		if (!MathUtils.isNan(this.value)) {
			return 0;
		}
		try {
			return Integer.valueOf(this.value);
		} catch (Throwable ex) {
			throw new LSysException(
					"Attribute '" + this.name + "' has value '" + this.value + "' which is not an integer !");
		}
	}

	public float getFloatValue() {
		if (!MathUtils.isNan(this.value)) {
			return 0;
		}
		try {
			return Float.valueOf(this.value);
		} catch (Throwable ex) {
			throw new LSysException(
					"Attribute '" + this.name + "' has value '" + this.value + "' which is not an float !");
		}
	}

	public double getDoubleValue() {
		if (!MathUtils.isNan(this.value)) {
			return 0;
		}
		try {
			if (this.value.indexOf('b') != -1) {
				this.value = value.replace("b", LSystem.EMPTY);
			}
			return Double.parseDouble(this.value);
		} catch (Throwable ex) {
			throw new LSysException(
					"Attribute '" + this.name + "' has value '" + this.value + "' which is not an double !");
		}
	}

	public boolean getBoolValue() {
		if (!StringUtils.isBoolean(this.value)) {
			return false;
		}
		if (value == null) {
			return false;
		}
		return StringUtils.toBoolean(value);
	}

	public String getName() {
		return this.name;
	}

}
