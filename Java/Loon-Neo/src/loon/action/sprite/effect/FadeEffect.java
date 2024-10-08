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
import loon.canvas.LColor;
import loon.opengl.GLEx;
import loon.utils.timer.Duration;

/**
 * 最基础的画面淡入淡出
 */
public class FadeEffect extends BaseAbstractEffect {

	private long time;

	private float currentFrame;

	private int type;

	private int _step;

	public static FadeEffect create(int type, LColor c) {
		return new FadeEffect(type, c);
	}

	public static FadeEffect create(int type, long delay, LColor c) {
		return new FadeEffect(type, delay, c);
	}

	public static FadeEffect create(int type, LColor c, int w, int h, int s) {
		return new FadeEffect(type, c, w, h, s);
	}

	public FadeEffect(int type, LColor c) {
		this(type, c, 1);
	}

	public FadeEffect(int type, LColor c, int step) {
		this(type, c, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight(), step);
	}

	public FadeEffect(int type, long delay, LColor c) {
		this(type, delay, c, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight(), 1);
	}

	public FadeEffect(int type, LColor c, int w, int h, int step) {
		this(type, 120, c, w, h, step);
	}

	public FadeEffect(int type, long delay, LColor c, int w, int h, int step) {
		this.type = type;
		this.setDelay(delay);
		this.setColor(c);
		this.setSize(w, h);
		this.setRepaint(true);
		this.setStep(step);
	}

	@Override
	public long getDelay() {
		return time;
	}

	@Override
	public float getDelayS() {
		return Duration.ofS(time);
	}

	@Override
	public FadeEffect setDelay(long delay) {
		this.time = delay;
		if (type == TYPE_FADE_IN) {
			this.currentFrame = this.time;
		} else {
			this.currentFrame = 0;
		}
		return this;
	}

	@Override
	public FadeEffect setDelayS(float s) {
		return setDelay(Duration.ofS(s));
	}

	public float getCurrentFrame() {
		return currentFrame;
	}

	public FadeEffect setCurrentFrame(float currentFrame) {
		this.currentFrame = currentFrame;
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
		if (completedAfterBlackScreen(g, sx, sy)) {
			return;
		}
		if (type == TYPE_FADE_OUT && _completed) {
			g.fillRect(drawX(sx), drawY(sy), _width, _height, _baseColor);
			return;
		}
		if (type == TYPE_FADE_IN && _completed) {
			return;
		}
		if (currentFrame >= time) {
			g.fillRect(drawX(sx), drawY(sy), _width, _height, _baseColor);
		} else {
			g.fillRect(drawX(sx), drawY(sy), _width, _height, _baseColor.setAlpha(currentFrame / time));
		}
	}

	@Override
	public void onUpdate(long timer) {
		if (checkAutoRemove()) {
			return;
		}
		if (type == TYPE_FADE_IN) {
			currentFrame -= _step;
			if (currentFrame <= _step) {
				setAlpha(0f);
				_completed = true;
			}
		} else {
			currentFrame += _step;
			if (currentFrame >= time - _step) {
				setAlpha(1f);
				_completed = true;
			}
		}
	}

	public FadeEffect setStep(int s) {
		_step = LSystem.toIScaleFPS(s, 1);
		return this;
	}

	public int getStep() {
		return _step;
	}

	public int getFadeType() {
		return type;
	}

	@Override
	public FadeEffect setAutoRemoved(boolean autoRemoved) {
		super.setAutoRemoved(autoRemoved);
		return this;
	}

}
