/**
 * Copyright 2008 - 2010
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
package loon.action.sprite.effect;

import loon.LSystem;
import loon.action.sprite.Entity;
import loon.canvas.LColor;
import loon.opengl.GLEx;

/**
 * 最基础的画面淡入淡出
 */
public class FadeEffect extends Entity implements BaseEffect {

	private float time;

	private float currentFrame;

	private int type;

	private boolean finished;

	private boolean autoRemoved;

	public static FadeEffect create(int type, LColor c) {
		return create(type, c, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public static FadeEffect create(int type, int timer, LColor c) {
		return new FadeEffect(c, timer, type, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public static FadeEffect create(int type, LColor c, int w, int h) {
		return new FadeEffect(c, 120, type, w, h);
	}

	public FadeEffect(int type, LColor c) {
		this(c, 120, type, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public FadeEffect(LColor c, int delay, int type, int w, int h) {
		this.type = type;
		this.setDelay(delay);
		this.setColor(c);
		this.setSize(w, h);
		this.setRepaint(true);
	}

	public float getDelay() {
		return time;
	}

	public FadeEffect setDelay(float delay) {
		this.time = delay;
		if (type == TYPE_FADE_IN) {
			this.currentFrame = this.time;
		} else {
			this.currentFrame = 0;
		}
		return this;
	}

	public float getCurrentFrame() {
		return currentFrame;
	}

	public FadeEffect setCurrentFrame(float currentFrame) {
		this.currentFrame = currentFrame;
		return this;
	}

	@Override
	public boolean isCompleted() {
		return finished;
	}

	@Override
	public FadeEffect setStop(boolean finished) {
		this.finished = finished;
		return this;
	}

	public int getEffectType() {
		return type;
	}

	public FadeEffect setEffectType(int type) {
		this.type = type;
		return this;
	}

	@Override
	public void repaint(GLEx g, float sx, float sy) {
		if (finished) {
			return;
		}
		g.fillRect(drawX(sx), drawY(sy), _width, _height, _baseColor.setAlpha(currentFrame / time));
		return;
	}

	@Override
	public void onUpdate(long timer) {
		if (type == TYPE_FADE_IN) {
			currentFrame--;
			if (currentFrame <= 0) {
				setAlpha(0);
				finished = true;
			}
		} else {
			currentFrame++;
			if (currentFrame >= time) {
				setAlpha(0);
				finished = true;
			}
		}
		if (this.finished) {
			if (autoRemoved && getSprites() != null) {
				getSprites().remove(this);
			}
		}
	}

	public int getFadeType() {
		return type;
	}

	public boolean isAutoRemoved() {
		return autoRemoved;
	}

	public FadeEffect setAutoRemoved(boolean autoRemoved) {
		this.autoRemoved = autoRemoved;
		return this;
	}

	@Override
	public void close() {
		super.close();
		finished = true;
	}

}
