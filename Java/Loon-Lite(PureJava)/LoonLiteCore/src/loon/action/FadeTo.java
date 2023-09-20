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
package loon.action;

import loon.action.sprite.ISprite;
import loon.utils.StringKeyValue;

public class FadeTo extends ActionEvent {

	private float time;

	private float currentFrame;

	private int currentType;

	public FadeTo(int type, float speed) {
		this.currentType = type;
		this.setSpeed(speed);
	}

	public int getEffectType() {
		return currentType;
	}

	public FadeTo setEffectType(int type) {
		this.currentType = type;
		return this;
	}

	public float getSpeed() {
		return time;
	}

	public FadeTo setSpeed(float delay) {
		this.time = delay;
		if (currentType == ISprite.TYPE_FADE_OUT) {
			this.currentFrame = this.time;
		} else {
			this.currentFrame = 0f;
		}
		return this;
	}

	@Override
	public FadeTo reset() {
		super.reset();
		setSpeed(time);
		return this;
	}

	@Override
	public void onLoad() {
	}

	@Override
	public void update(long elapsedTime) {
		if (currentType == ISprite.TYPE_FADE_OUT) {
			currentFrame--;
			if (currentFrame <= 0) {
				original.setAlpha(0f);
				_isCompleted = true;
				return;
			}
		} else {
			currentFrame++;
			if (currentFrame >= time) {
				original.setAlpha(1f);
				_isCompleted = true;
				return;
			}
		}
		original.setAlpha(currentFrame / time);
		if (_isCompleted) {
			if (currentType == ISprite.TYPE_FADE_OUT) {
				original.setAlpha(0f);
			} else {
				original.setAlpha(1f);
			}
		}
	}

	public float getTime() {
		return time;
	}

	public float getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public ActionEvent cpy() {
		FadeTo fade = new FadeTo(currentType, time);
		fade.set(this);
		return fade;
	}

	@Override
	public ActionEvent reverse() {
		FadeTo fade = null;
		if (currentType == ISprite.TYPE_FADE_IN) {
			fade = new FadeTo(ISprite.TYPE_FADE_OUT, time);
		} else {
			fade = new FadeTo(ISprite.TYPE_FADE_IN, time);
		}
		fade.set(this);
		return fade;
	}

	@Override
	public String getName() {
		return "fade";
	}

	@Override
	public String toString() {
		StringKeyValue builder = new StringKeyValue(getName());
		if (original != null) {
			builder.kv("alpha", original.getAlpha()).comma();
		}
		builder.kv("speed", time).comma().kv("currentFrame", currentFrame);
		return builder.toString();
	}

}
