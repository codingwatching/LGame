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
package loon.events;

import loon.utils.TArray;

/**
 * 游戏触屏点缓存用池,可以保存多个GameTouch并进行一系列集合操作
 */
public class GameTouchPool {

	private TArray<GameTouch> _activeList;
	private TArray<GameTouch> _touchList;

	public GameTouchPool() {
		this._activeList = new TArray<GameTouch>();
		this._touchList = new TArray<GameTouch>();
	}

	public GameTouchPool addTouch(GameTouch touch) {
		_touchList.add(touch);
		return this;
	}

	public boolean removeTouch(GameTouch touch) {
		return _touchList.remove(touch);
	}

	public GameTouchPool clear() {
		for (int i = 0; i < this._touchList.size; i++) {
			GameTouch touch = _touchList.get(i);
			touch.setState(SysTouch.TOUCH_UNKNOWN);
			touch.x = 0f;
			touch.y = 0f;
			touch.setActive(false);
		}
		this._activeList.clear();
		return this;
	}

	public GameTouchPool reset() {
		for (int i = 0; i < this._touchList.size; i++) {
			GameTouch newTouch = new GameTouch();
			newTouch.id = i;
			newTouch.setState(SysTouch.TOUCH_UNKNOWN);
			newTouch.setActive(false);
			this._touchList.add(newTouch);
		}
		return this.clear();
	}

	public GameTouch getTouch(int idx) {
		return _touchList.get(idx);
	}

	public TArray<GameTouch> getNoActives() {
		TArray<GameTouch> list = new TArray<GameTouch>();
		for (int i = 0; i < this._touchList.size; i++) {
			GameTouch touch = _touchList.get(i);
			if (!touch.isActive()) {
				list.add(touch);
			}
		}
		return list;
	}

	public GameTouch getNoActive() {
		for (int i = 0; i < this._touchList.size; i++) {
			GameTouch touch = _touchList.get(i);
			if (!touch.isActive()) {
				return touch;
			}
		}
		return null;
	}

	public GameTouchPool touches(int button, int amount, GameTouch touch) {
		for (int i = 0; i < amount; i++) {
			if (this._activeList.size == 0) {
				this._activeList.add(this.getNoActive());
				int len = this._activeList.size - 1;
				GameTouch newTouch = this._activeList.get(len);
				newTouch.setState(button);
				newTouch.convertOrientation(touch.x, touch.y);
				newTouch.setActive(true);
			}
		}
		return this;
	}

	public GameTouchPool update() {
		for (int i = this._activeList.size; i > -1; i--) {
			GameTouch touch = _activeList.get(i);
			if (touch.getState() == SysTouch.TOUCH_UP) {
				touch.setActive(false);
				touch.x = 0f;
				touch.y = 0f;
				touch.setState(SysTouch.TOUCH_UNKNOWN);
				this._activeList.removeIndex(i);
			}
		}
		return this;
	}
}
