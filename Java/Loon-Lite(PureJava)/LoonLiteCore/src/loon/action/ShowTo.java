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
package loon.action;

import loon.utils.StringKeyValue;

public class ShowTo extends ActionEvent {

	private boolean visible;

	public ShowTo(boolean v) {
		this.visible = v;
	}

	@Override
	public void update(long elapsedTime) {
		if (original.isVisible() != visible) {
			original.setVisible(visible);
			this._isCompleted = true;
		}
	}

	public boolean isVisible() {
		return visible;
	}

	@Override
	public void onLoad() {

	}

	@Override
	public ActionEvent cpy() {
		ShowTo show = new ShowTo(visible);
		show.set(this);
		return show;
	}

	@Override
	public ActionEvent reverse() {
		ShowTo show = new ShowTo(!visible);
		show.set(this);
		return show;
	}

	@Override
	public String getName() {
		return "show";
	}

	@Override
	public String toString() {
		final StringKeyValue builder = new StringKeyValue(getName());
		builder.kv("visible", visible);
		return builder.toString();
	}

}
