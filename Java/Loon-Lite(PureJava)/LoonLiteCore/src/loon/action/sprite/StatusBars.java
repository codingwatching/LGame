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
package loon.action.sprite;

import loon.opengl.GLEx;
import loon.utils.CollectionUtils;
import loon.utils.TArray;

public final class StatusBars extends Entity {

	private TArray<StatusBar> barCaches;

	public StatusBars() {
		this.barCaches = new TArray<>(CollectionUtils.INITIAL_CAPACITY);
		this.setRepaint(true);
	}

	public StatusBar addBar(int value, int maxValue, int x, int y, int w, int h) {
		synchronized (barCaches) {
			StatusBar bar = new StatusBar(value, maxValue, x, y, w, h);
			barCaches.add(bar);
			return bar;
		}
	}

	public StatusBar addBar(int x, int y, int width, int height) {
		return addBar(100, 100, x, y, width, height);
	}

	public StatusBar addBar(int width, int height) {
		return addBar(100, 100, 0, 0, width, height);
	}

	public void addBar(StatusBar bar) {
		synchronized (barCaches) {
			barCaches.add(bar);
		}
	}

	public boolean removeBar(StatusBar bar) {
		if (bar == null) {
			return false;
		}
		synchronized (barCaches) {
			return barCaches.remove(bar);
		}
	}

	@Override
	public void clear() {
		synchronized (barCaches) {
			barCaches.clear();
		}
	}

	@Override
	public int size() {
		return barCaches.size;
	}

	public void hide(StatusBar bar) {
		if (bar != null) {
			bar.setVisible(false);
		}
	}

	public void show(StatusBar bar) {
		if (bar != null) {
			bar.setVisible(true);
		}
	}

	@Override
	public void repaint(GLEx g, float offsetX, float offsetY) {
		int size = barCaches.size;
		if (size > 0) {
			synchronized (barCaches) {
				for (int i = 0; i < size; i++) {
					StatusBar bar = barCaches.get(i);
					if (bar != null && bar.isVisible()) {
						bar.createUI(g, offsetX, offsetY);
					}
				}
			}
		}
	}

	@Override
	public void onUpdate(long elapsedTime) {
		int size = barCaches.size;
		if (size > 0) {
			synchronized (barCaches) {
				for (int i = 0; i < size; i++) {
					StatusBar bar = barCaches.get(i);
					if (bar != null) {
						bar.update(elapsedTime);
					}
				}
			}
		}
	}

	@Override
	public void _onDestroy() {
		super._onDestroy();
		int size = barCaches.size;
		for (int i = 0; i < size; i++) {
			StatusBar bar = barCaches.get(i);
			if (bar != null) {
				bar.close();
				bar = null;
			}
		}
		barCaches.clear();
		barCaches = null;
	}
}
