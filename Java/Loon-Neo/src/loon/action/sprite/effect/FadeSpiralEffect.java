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
package loon.action.sprite.effect;

import loon.LSystem;
import loon.action.sprite.Entity;
import loon.action.sprite.ISprite;
import loon.canvas.LColor;
import loon.opengl.GLEx;
import loon.utils.timer.LTimer;

/**
 * 黑幕过渡效果,瓦片从向中心处螺旋集中或向外螺旋扩散最终消失
 */
public class FadeSpiralEffect extends Entity implements BaseEffect {

	private boolean finished;
	private boolean autoRemoved;
	private int tilewidth;
	private int tileheight;
	private int speed;
	private int tilescovered = 0;

	private boolean[][] conversions;
	private int cx = 0;
	private int cy = 0;

	private int state = 1;

	private int type;

	private LTimer timer;

	public FadeSpiralEffect(int type) {
		this(type, 1, LColor.black);
	}

	public FadeSpiralEffect(int type, LColor c) {
		this(type, 1, c);
	}

	public FadeSpiralEffect(int type, int speed, LColor c) {
		this(type, speed, c, 64, 32);
	}

	public FadeSpiralEffect(int type, int speed, LColor c, int w, int h) {
		this.type = type;
		this.speed = speed;
		this.timer = new LTimer(30);
		this.tilewidth = (int) (LSystem.viewSize.getWidth() / w + 1);
		this.tileheight = (int) (LSystem.viewSize.getHeight() / h + 1);
		this.conversions = new boolean[tilewidth][tileheight];
		this.reset();
		this.setRepaint(true);
		this.setColor(c);
		this.setSize(w, h);
	}

	@Override
	public FadeSpiralEffect reset() {
		super.reset();
		int tmp = _baseColor.getARGB();
		if (type == ISprite.TYPE_FADE_IN) {
			for (int x = 0; x < tilewidth; x++) {
				for (int y = 0; y < tileheight; y++) {
					conversions[x][y] = true;
				}
			}
		} else {
			for (int x = 0; x < tilewidth; x++) {
				for (int y = 0; y < tileheight; y++) {
					conversions[x][y] = false;
				}
			}
		}
		this.state = 1;
		this.cx = 0;
		this.cy = 0;
		this.tilescovered = 0;
		_baseColor.setColor(tmp);
		return this;
	}

	public float getDelay() {
		return timer.getDelay();
	}

	public FadeSpiralEffect setDelay(int delay) {
		timer.setDelay(delay);
		return this;
	}

	public boolean finished() {
		return tilescovered >= (tilewidth * tileheight);
	}

	@Override
	public void repaint(GLEx g, float offsetX, float offsetY) {
		if (finished) {
			return;
		}
		for (int x = 0; x < tilewidth; x++) {
			for (int y = 0; y < tileheight; y++) {
				if (conversions[x][y]) {
					g.fillRect((x * _width) + offsetX + _offset.x, (y * _height) + offsetY + _offset.y, _width, _height,
							_baseColor);
				}
			}
		}

	}

	@Override
	public boolean isCompleted() {
		return finished;
	}

	@Override
	public FadeSpiralEffect setStop(boolean finished) {
		this.finished = finished;
		return this;
	}
	
	@Override
	public void onUpdate(long elapsedTime) {
		if (finished) {
			return;
		}
		if (timer.action(elapsedTime)) {
			if (type == ISprite.TYPE_FADE_IN) {
				for (int i = 0; i < speed; i++) {
					if (conversions[cx][cy]) {
						conversions[cx][cy] = false;
						tilescovered++;
					}
					switch (state) {
					case 0:
						cy--;
						if (cy <= -1 || (!conversions[cx][cy])) {
							cy++;
							state = 2;
						}
						break;
					case 1:
						cy++;
						if (cy >= tileheight || (!conversions[cx][cy])) {
							cy--;
							state = 3;
						}
						break;
					case 2:
						cx--;
						if (cx <= -1 || (!conversions[cx][cy])) {
							cx++;
							state = 1;
						}
						break;
					case 3:
						cx++;
						if (cx >= tilewidth || (!conversions[cx][cy])) {
							cx--;
							state = 0;
						}
						break;
					}
				}
			} else {
				for (int i = 0; i < speed; i++) {
					if (!conversions[cx][cy]) {
						conversions[cx][cy] = true;
						tilescovered++;
					}
					switch (state) {
					case 0:
						cy--;
						if (cy <= -1 || (conversions[cx][cy])) {
							cy++;
							state = 2;
						}
						break;
					case 1:
						cy++;
						if (cy >= tileheight || (conversions[cx][cy])) {
							cy--;
							state = 3;
						}
						break;
					case 2:
						cx--;
						if (cx <= -1 || (conversions[cx][cy])) {
							cx++;
							state = 1;
						}
						break;
					case 3:
						cx++;
						if (cx >= tilewidth || (conversions[cx][cy])) {
							cx--;
							state = 0;
						}
						break;
					}
				}
			}
			if (finished()) {
				finished = true;
			}
		}
		if (this.finished) {
			if (autoRemoved && getSprites() != null) {
				getSprites().remove(this);
			}
		}
	}

	public boolean isAutoRemoved() {
		return autoRemoved;
	}

	public FadeSpiralEffect setAutoRemoved(boolean autoRemoved) {
		this.autoRemoved = autoRemoved;
		return this;
	}

	@Override
	public void close() {
		super.close();
		finished = true;
		conversions = null;
	}

}
