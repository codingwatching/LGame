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

public class FlipYTo extends ActionEvent {

	private boolean flipY;

	public FlipYTo(boolean x) {
		this.flipY = x;
	}

	@Override
	public void update(long elapsedTime) {
		if (original != null && original instanceof Flip<?>) {
			Flip<?> flip = (Flip<?>) original;
			flip.setFlipY(flipY);
			this._isCompleted = true;
		}
	}

	public boolean isFlipY() {
		return flipY;
	}

	@Override
	public void onLoad() {

	}

	@Override
	public ActionEvent cpy() {
		FlipYTo flip = new FlipYTo(flipY);
		flip.set(this);
		return flip;
	}

	@Override
	public ActionEvent reverse() {
		FlipYTo flip = new FlipYTo(!flipY);
		flip.set(this);
		return flip;
	}

	@Override
	public String getName() {
		return "flipy";
	}

	@Override
	public String toString() {
		StringKeyValue builder = new StringKeyValue(getName());
		builder.kv("flipY", flipY);
		return builder.toString();
	}
}
