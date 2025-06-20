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
 * @version 0.1.1
 */
package loon.utils.timer;

import loon.LSystem;
import loon.utils.MathUtils;
import loon.utils.StringKeyValue;

public class LTimerContext {

	public long timeSinceLastUpdate;

	public long unscaledTimeSinceLastUpdate;

	public long tick;

	public float alpha;

	public LTimerContext() {
		this(0L);
	}

	public LTimerContext(long v) {
		this.reset(v);
	}

	public void reset() {
		this.reset(0L);
	}

	public void reset(long v) {
		this.timeSinceLastUpdate = unscaledTimeSinceLastUpdate = tick = v;
		this.alpha = 0f;
	}

	public float getMilliseconds() {
		return MathUtils.max(Duration.toS(timeSinceLastUpdate), LSystem.MIN_SECONE_SPEED_FIXED);
	}

	public float getDelta() {
		return getDelta(getMilliseconds());
	}

	public float getDelta(float delta) {
		if (delta > 0.1f) {
			delta = 0.1f;
		} else if (delta <= 0f) {
			delta = LSystem.DEFAULT_EASE_DELAY;
		}
		return delta;
	}

	public float calcPpf(float pps) {
		return MathUtils.calcPpf(pps, getDelta());
	}

	public float dt() {
		return getDelta();
	}

	public float getUnscaledMilliseconds() {
		return MathUtils.max(Duration.toS(unscaledTimeSinceLastUpdate), LSystem.MIN_SECONE_SPEED_FIXED);
	}

	public float getUnscaledDelta() {
		return getDelta(getUnscaledMilliseconds());
	}

	public float calcUnscaledPpf(float pps) {
		return MathUtils.calcPpf(pps, getUnscaledDelta());
	}

	public long getTimeSinceLastUpdate() {
		return timeSinceLastUpdate;
	}

	public long getUnscaledTimeSinceLastUpdate() {
		return unscaledTimeSinceLastUpdate;
	}

	public float getAlpha() {
		return alpha;
	}

	public float getScale() {
		return LSystem.getScaleFPS();
	}

	@Override
	public String toString() {
		StringKeyValue builder = new StringKeyValue("LTimerContext");
		builder.kv("timeSinceLastUpdate", timeSinceLastUpdate).comma()
				.kv("unscaledTimeSinceLastUpdate", unscaledTimeSinceLastUpdate).comma().kv("tick", tick).comma()
				.kv("alpha", alpha);
		return builder.toString();
	}
}
