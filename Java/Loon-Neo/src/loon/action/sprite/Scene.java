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
package loon.action.sprite;

import loon.action.map.items.Role;
import loon.action.map.items.Item;
import loon.opengl.GLEx;
import loon.utils.TArray;

/**
 * 由Entity产生的窗体Scene,方便递归调用
 */
public class Scene extends Entity {

	private long _secondsElapsedTotal;

	protected Scene _parentScene;
	protected Scene _childScene;
	private boolean _childSceneModalDraw;
	private boolean _childSceneModalUpdate;

	private boolean _backgroundEnabled = true;

	private TArray<Item<Object>> _items = new TArray<Item<Object>>();

	private TArray<Role> _characters = new TArray<Role>();

	public Scene() {
		this(0);
	}

	public Scene(final int count) {
		for (int i = 0; i < count; i++) {
			this.addChild(new Entity());
		}
	}

	public void addItem(Item<Object> item) {
		this._items.add(item);
	}

	public Item<Object> getItem(int index) {
		return this._items.get(index);
	}

	public Item<Object> getItem(String name) {
		int index = findItem(name);
		if (index == -1) {
			return null;
		}
		return getItem(index);
	}

	public int findItem(String name) {
		for (int i = 0; i < this._items.size; i++) {
			if (getItem(i).getName().equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}

	public Item<Object> removeItem(int index) {
		return this._items.removeIndex(index);
	}

	public int countItems() {
		return this._items.size;
	}

	public void addCharacter(Role character) {
		this._characters.add(character);
	}

	public Role getCharacter(int index) {
		return this._characters.get(index);
	}

	public Role getCharacter(String name) {
		int index = findCharacter(name);
		if (index == -1) {
			return null;
		}
		return getCharacter(index);
	}

	public int findCharacter(String name) {
		for (int i = 0; i < this._characters.size; i++) {
			Role ch = getCharacter(i);
			if (ch != null && ch.getRoleName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public Role removeCharacter(int index) {
		return this._characters.removeIndex(index);
	}

	public int countCharacters() {
		return this._characters.size;
	}

	public float getSecondsElapsedTotal() {
		return this._secondsElapsedTotal;
	}

	private void setParentScene(final Scene pParentScene) {
		this._parentScene = pParentScene;
	}

	@Override
	public Scene reset() {
		super.reset();
		this.clearChildScene();
		return this;
	}

	@Override
	protected void onManagedPaint(final GLEx gl, float offsetX, float offsetY) {
		final Scene childScene = this._childScene;
		if (childScene == null || !this._childSceneModalDraw) {
			if (this._backgroundEnabled) {
				super.onManagedPaint(gl, offsetX, offsetY);
			}
		}
		if (childScene != null) {
			childScene.createUI(gl, offsetX, offsetY);
		}
	}

	public Scene back() {
		this.clearChildScene();
		if (this._parentScene != null) {
			this._parentScene.clearChildScene();
			this._parentScene = null;
		}
		return this;
	}

	public boolean hasChildScene() {
		return this._childScene != null;
	}

	public Scene getChildScene() {
		return this._childScene;
	}

	public Scene setChildSceneModal(final Scene child) {
		return this.setChildScene(child, true, true);
	}

	public Scene setChildScene(final Scene child) {
		return this.setChildScene(child, false, false);
	}

	public Scene setChildScene(final Scene child, final boolean modalDraw, final boolean modalUpdate) {
		child.setParentScene(this);
		this._childScene = child;
		this._childSceneModalDraw = modalDraw;
		this._childSceneModalUpdate = modalUpdate;
		return this;
	}

	public Scene clearChildScene() {
		this._childScene = null;
		return this;
	}

	@Override
	protected void onManagedUpdate(final long elapsedTime) {
		this._secondsElapsedTotal += elapsedTime;
		final Scene childScene = this._childScene;
		if (childScene == null || !this._childSceneModalUpdate) {
			super.onManagedUpdate(elapsedTime);
		}
		if (childScene != null) {
			childScene.update(elapsedTime);
		}
	}

}
