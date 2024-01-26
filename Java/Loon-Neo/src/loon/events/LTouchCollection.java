/**
 * Copyright 2008 - 2012
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
 * @version 0.3.3
 */
package loon.events;

import loon.LSysException;
import loon.geom.Vector2f;
import loon.utils.LIterator;
import loon.utils.SortedList;
import loon.utils.reply.ObjRef;

public class LTouchCollection extends SortedList<LTouchLocation> {

	private boolean _connected;

	public boolean AnyTouch() {
		for (LIterator<LTouchLocation> it = listIterator(); it.hasNext();) {
			LTouchLocation location = it.next();
			if ((location.getState() == LTouchLocationState.Pressed)
					|| (location.getState() == LTouchLocationState.Dragged)) {
				return true;
			}
		}
		return false;
	}

	public LTouchCollection setConnected(boolean c) {
		this._connected = c;
		return this;
	}

	public boolean isConnected() {
		return this._connected;
	}

	public boolean isReadOnly() {
		return true;
	}

	public LTouchCollection() {
	}

	public LTouchCollection(SortedList<LTouchLocation> locations) {
		super(locations);
	}

	public final void update() {
		for (int i = this.size() - 1; i >= 0; --i) {
			LTouchLocation t = this.get(i);
			switch (t.getState()) {
			case Pressed:
				t.setState(LTouchLocationState.Dragged);
				t.setPrevPosition(t.getPosition());
				this.set(i, t.cpy());
				break;
			case Dragged:
				t.setPrevState(LTouchLocationState.Dragged);
				this.set(i, t.cpy());
				break;
			case Released:
			case Invalid:
				remove(i);
				break;
			}
		}
	}

	public final int findIndexById(int id, ObjRef<LTouchLocation> touchLocation) {
		for (int i = 0; i < this.size(); i++) {
			LTouchLocation location = this.get(i);
			if (location.getId() == id) {
				touchLocation.set(this.get(i));
				return i;
			}
		}
		touchLocation.set(new LTouchLocation());
		return -1;
	}

	public final void add(int id, Vector2f position) {
		for (int i = 0; i < size(); i++) {
			if (this.get(i).id == id) {
				clear();
			}
		}
		add(new LTouchLocation(id, LTouchLocationState.Pressed, position));
	}

	public final void add(int id, float x, float y) {
		for (int i = 0; i < size(); i++) {
			if (this.get(i).id == id) {
				clear();
			}
		}
		add(new LTouchLocation(id, LTouchLocationState.Pressed, x, y));
	}

	public final void update(int id, LTouchLocationState state, float posX, float posY) {
		if (state == LTouchLocationState.Pressed) {
			throw new LSysException("Argument 'state' cannot be TouchLocationState.Pressed.");
		}

		for (int i = 0; i < size(); i++) {
			if (this.get(i).id == id) {
				LTouchLocation touchLocation = this.get(i);
				touchLocation.setPosition(posX, posY);
				touchLocation.setState(state);
				this.set(i, touchLocation);
				return;
			}
		}
		clear();
	}

	public final void update(int id, LTouchLocationState state, Vector2f position) {
		if (state == LTouchLocationState.Pressed) {
			throw new LSysException("Argument 'state' cannot be TouchLocationState.Pressed.");
		}

		for (int i = 0; i < size(); i++) {
			if (this.get(i).id == id) {
				LTouchLocation touchLocation = this.get(i);
				touchLocation.setPosition(position);
				touchLocation.setState(state);
				this.set(i, touchLocation);
				return;
			}
		}
		clear();
	}

}
