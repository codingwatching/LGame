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
package loon.utils;

import loon.geom.Vector2f;

/**
 * 工具类,用以缓动计算
 */
public final class Easing {

	public static enum EasingMode {
		InQuad, OutQuad, InOutQuad, InCubic, OutCubic, InOutCubic, InQuart, OutQuart, InOutQuart, InQuint, OutQuint,
		InOutQuint, InSine, OutSine, InOutSine, InExp, OutExp, InOutExp, InCirc, OutCirc, InOutCirc, InBack, OutBack,
		InOutBack, OutBounce, InBounce, InOutBounce, InElastic, OutElastic, InOutElastic, Linear;
	}

	public static EasingMode toEasingMode(String name) {
		String key = name == null ? "Linear" : name.trim();
		if ("InQuad".equals(key)) {
			return EasingMode.InQuad;
		} else if ("OutQuad".equals(key)) {
			return EasingMode.OutQuad;
		} else if ("InOutQuad".equals(key)) {
			return EasingMode.InOutQuad;
		} else if ("InCubic".equals(key)) {
			return EasingMode.InCubic;
		} else if ("OutCubic".equals(key)) {
			return EasingMode.OutCubic;
		} else if ("InOutCubic".equals(key)) {
			return EasingMode.InOutCubic;
		} else if ("InQuart".equals(key)) {
			return EasingMode.InQuart;
		} else if ("OutQuart".equals(key)) {
			return EasingMode.OutQuart;
		} else if ("InOutQuart".equals(key)) {
			return EasingMode.InOutQuart;
		} else if ("InQuint".equals(key)) {
			return EasingMode.InQuint;
		} else if ("OutQuint".equals(key)) {
			return EasingMode.OutQuint;
		} else if ("InOutQuint".equals(key)) {
			return EasingMode.InOutQuint;
		} else if ("InSine".equals(key)) {
			return EasingMode.InSine;
		} else if ("OutSine".equals(key)) {
			return EasingMode.OutSine;
		} else if ("InOutSine".equals(key)) {
			return EasingMode.InOutSine;
		} else if ("InExp".equals(key)) {
			return EasingMode.InExp;
		} else if ("OutExp".equals(key)) {
			return EasingMode.OutExp;
		} else if ("InOutExp".equals(key)) {
			return EasingMode.InOutExp;
		} else if ("InCirc".equals(key)) {
			return EasingMode.InCirc;
		} else if ("OutCirc".equals(key)) {
			return EasingMode.OutCirc;
		} else if ("InOutCirc".equals(key)) {
			return EasingMode.InOutCirc;
		} else if ("InBack".equals(key)) {
			return EasingMode.InBack;
		} else if ("OutBack".equals(key)) {
			return EasingMode.OutBack;
		} else if ("InOutBack".equals(key)) {
			return EasingMode.InOutBack;
		} else if ("OutBounce".equals(key)) {
			return EasingMode.OutBounce;
		} else if ("InBounce".equals(key)) {
			return EasingMode.InBounce;
		} else if ("InOutBounce".equals(key)) {
			return EasingMode.InOutBounce;
		} else if ("InElastic".equals(key)) {
			return EasingMode.InElastic;
		} else if ("OutElastic".equals(key)) {
			return EasingMode.OutElastic;
		} else if ("InOutElastic".equals(key)) {
			return EasingMode.InOutElastic;
		} else {
			return EasingMode.Linear;
		}
	}

	public static float getTween(EasingMode ease, float t) {
		switch (ease) {
		case Linear:
			return linear(t);
		case InBack:
			return backEaseIn(t);
		case OutBack:
			return backEaseOut(t);
		case InOutBack:
			return backEaseInOut(t);
		case InQuad:
			return quadEaseIn(t);
		case OutQuad:
			return quadEaseOut(t);
		case InOutQuad:
			return quadEaseInOut(t);
		case InCubic:
			return cubicEaseIn(t);
		case OutCubic:
			return cubicEaseOut(t);
		case InOutCubic:
			return cubicEaseInOut(t);
		case InQuart:
			return quartEaseIn(t);
		case OutQuart:
			return quartEaseOut(t);
		case InOutQuart:
			return quartEaseInOut(t);
		case InQuint:
			return quintEaseIn(t);
		case OutQuint:
			return quintEaseOut(t);
		case InOutQuint:
			return quintEaseInOut(t);
		case InSine:
			return sineEaseIn(t);
		case OutSine:
			return sineEaseOut(t);
		case InOutSine:
			return sineEaseInOut(t);
		case InCirc:
			return circEaseIn(t);
		case OutCirc:
			return circEaseOut(t);
		case InOutCirc:
			return circEaseInOut(t);
		case InExp:
			return expEaseIn(t);
		case OutExp:
			return expEaseOut(t);
		case InOutExp:
			return expEaseInOut(t);
		case InElastic:
			return elasticEaseIn(t);
		case OutElastic:
			return elasticEaseOut(t);
		case InOutElastic:
			return elasticEaseInOut(t);
		case InBounce:
			return bounceEaseIn(t);
		case OutBounce:
			return bounceEaseOut(t);
		case InOutBounce:
			return bounceEaseInOut(t);
		}
		return linear(t);
	}

	private static final int TYPE_IN = 0;

	private static final int TYPE_OUT = 1;

	private static final int TYPE_IN_OUT = 2;

	private static final int TYPE_TIME = 3;

	private static final int FUNCTION_LINEAR = 0;

	private static final int FUNCTION_QUADRADIC = 1;

	private static final int FUNCTION_IN = 2;

	private static final int FUNCTION_QUARTIC = 3;

	private static final int FUNCTION_QUINTIC = 4;

	private static final int FUNCTION_BACK = 5;

	private static final int FUNCTION_ELASTIC = 6;

	private static final int FUNCTION_NONE = 7;

	private static final int FUNCTION_OUT = 8;

	private static final int FUNCTION_INOUT = 9;

	private static final int FUNCTION_IN_BACK = 10;

	private static final int FUNCTION_OUT_BACK = 11;

	private static final int FUNCTION_BOUNCE_OUT = 12;

	private static final int FUNCTION_OUT_ELASTIC = 13;

	public static final Easing NONE = new Easing("NONE", TYPE_IN, FUNCTION_LINEAR);

	public static final Easing ELASTIC_INOUT = new Easing("ELASTIC_INOUT", TYPE_IN_OUT, FUNCTION_ELASTIC);

	public static final Easing QUAD_INOUT = new Easing("QUAD_INOUT", TYPE_IN_OUT, FUNCTION_LINEAR);

	public static final Easing REGULAR_IN = new Easing("REGULAR_IN", TYPE_IN, FUNCTION_QUADRADIC);

	public static final Easing REGULAR_OUT = new Easing("REGULAR_OUT", TYPE_OUT, FUNCTION_QUADRADIC);

	public static final Easing REGULAR_IN_OUT = new Easing("REGULAR_IN_OUT", TYPE_IN_OUT, FUNCTION_QUADRADIC);

	public static final Easing STRONG_IN = new Easing("STRONG_IN", TYPE_IN, FUNCTION_QUINTIC);

	public static final Easing STRONG_OUT = new Easing("STRONG_OUT", TYPE_OUT, FUNCTION_QUINTIC);

	public static final Easing STRONG_IN_OUT = new Easing("STRONG_IN_OUT", TYPE_IN_OUT, FUNCTION_QUINTIC);

	public static final Easing BACK_IN = new Easing("BACK_IN", TYPE_IN, FUNCTION_BACK);

	public static final Easing BACK_OUT = new Easing("BACK_OUT", TYPE_OUT, FUNCTION_BACK);

	public static final Easing CUBIC_IN = new Easing("CUBIC_IN", TYPE_IN, FUNCTION_IN);

	public static final Easing CUBIC_OUT = new Easing("CUBIC_OUT", TYPE_OUT, FUNCTION_OUT);

	public static final Easing CUBIC_INOUT = new Easing("CUBIC_INOUT", TYPE_OUT, FUNCTION_INOUT);

	public static final Easing BOUNCE_IN = new Easing("BOUNCE_IN", TYPE_OUT, FUNCTION_BOUNCE_OUT);

	public static final Easing BOUNCE_INOUT = new Easing("BOUNCE_INOUT", TYPE_IN_OUT, FUNCTION_BOUNCE_OUT);

	public static final Easing BACK_IN_OUT = new Easing("BACK_IN_OUT", TYPE_IN_OUT, FUNCTION_BACK);

	public static final Easing ELASTIC_IN = new Easing("ELASTIC_IN", TYPE_IN, FUNCTION_ELASTIC);

	public static final Easing ELASTIC_OUT = new Easing("ELASTIC_OUT", TYPE_OUT, FUNCTION_ELASTIC);

	public static final Easing ELASTIC_IN_OUT = new Easing("ELASTIC_IN_OUT", TYPE_IN_OUT, FUNCTION_ELASTIC);

	public static final Easing TIME_NONE = new Easing("TIME_NONE", TYPE_TIME, FUNCTION_NONE);

	public static final Easing TIME_LINEAR = new Easing("TIME_LINEAR", TYPE_TIME, FUNCTION_LINEAR);

	public static final Easing TIME_EASE_IN = new Easing("TIME_EASE_IN", TYPE_TIME, FUNCTION_IN);

	public static final Easing TIME_EASE_OUT = new Easing("TIME_EASE_OUT", TYPE_TIME, FUNCTION_OUT);

	public static final Easing TIME_EASE_INOUT = new Easing("TIME_EASE_INOUT", TYPE_TIME, FUNCTION_INOUT);

	public static final Easing TIME_EASE_IN_BACK = new Easing("TIME_EASE_IN_BACK", TYPE_TIME, FUNCTION_IN_BACK);

	public static final Easing TIME_EASE_OUT_BACK = new Easing("TIME_EASE_OUT_BACK", TYPE_TIME, FUNCTION_OUT_BACK);

	public static final Easing TIME_BOUNCE_OUT = new Easing("TIME_BOUNCE_OUT", TYPE_TIME, FUNCTION_BOUNCE_OUT);

	public static final Easing TIME_EASE_OUT_ELASTIC = new Easing("TIME_EASE_OUT_ELASTIC", TYPE_TIME,
			FUNCTION_OUT_ELASTIC);

	private final int type;

	private final int function;

	private final float strength;

	private final String name;

	protected Easing() {
		this("NONE");
	}

	protected Easing(String name) {
		this(name, TYPE_IN, FUNCTION_LINEAR);
	}

	protected Easing(String name, int type) {
		this(name, type, FUNCTION_LINEAR);
	}

	private Easing(String name, int type, int function) {
		this(name, type, function, 1);
	}

	private Easing(String name, int type, int function, float stength) {
		this.name = name;
		this.type = type;
		this.function = function;
		this.strength = stength;
	}

	public Easing(Easing easing, float strength) {
		this(easing.name, easing.type, easing.function, strength);
	}

	public final float apply(float time, float duration) {
		return apply(time, duration, true);
	}

	public final float apply(float time, float duration, boolean mul) {
		if (TYPE_TIME == type) {
			return call(function, duration / time);
		}
		if (time <= 0 || duration <= 0) {
			return 0;
		} else if (time >= duration) {
			return duration;
		}

		final float t = time / duration;

		float easedT;

		switch (type) {
		default:
			easedT = t;
			break;

		case TYPE_IN:
			easedT = call(function, t);
			break;

		case TYPE_OUT:
			easedT = 1 - call(function, 1 - t);
			break;

		case TYPE_IN_OUT:
			if (t < 0.5) {
				easedT = call(function, 2 * t) / 2;
			} else {
				easedT = 1 - call(function, 2 - 2 * t) / 2;
			}
			break;
		}
		if (strength != 1) {
			easedT = strength * easedT + (1 - strength) * t;
		}
		if (mul) {
			return (easedT * duration);
		}
		return easedT;
	}

	public float applyClamp(float time, float duration) {
		return apply((time < 0) ? 0 : (time > 1 ? 1 : time), duration);
	}

	public float apply(float start, float range, float time, float duration) {
		float pos = (duration == 0) ? 1 : apply(time, duration);
		return start + range * pos;
	}

	public float applyClamp(float start, float range, float time, float duration) {
		return apply(start, range, duration, MathUtils.clamp(time, 0, duration));
	}

	protected static float call(int fun, float t) {

		float t2;
		float t3;

		switch (fun) {

		default:
		case FUNCTION_LINEAR:
			return t;

		case FUNCTION_QUADRADIC:
			return t * t;

		case FUNCTION_IN:
			return t * t * t;

		case FUNCTION_QUARTIC:
			t2 = t * t;
			return t2 * t2;

		case FUNCTION_QUINTIC:
			t2 = t * t;
			return t2 * t2 * t;

		case FUNCTION_BACK:
			t2 = t * t;
			t3 = t2 * t;
			return t3 + t2 - t;

		case FUNCTION_ELASTIC:
			t2 = t * t;
			t3 = t2 * t;

			float scale = t2 * (2 * t3 + t2 - 4 * t + 2);
			float wave = -MathUtils.sin(t * 3.5f * MathUtils.PI);

			return scale * wave;
		case FUNCTION_NONE:
			return 0;
		case FUNCTION_OUT:
			t2 = t - 1;
			return (1 + t2 * t2 * t2);
		case FUNCTION_INOUT:
			t2 = 2 * t;
			if (t2 < 1) {
				return (t2 * t2 * t2) / 2;
			}
			t3 = t2 - 2;
			return (2 + t3 * t3 * t3) / 2;
		case FUNCTION_IN_BACK:
			t2 = 1.70158f;
			return t * t * ((t2 + 1) * t - t2);
		case FUNCTION_OUT_BACK:
			t2 = 1.70158f;
			t3 = t - 1;
			return (t3 * t3 * ((t2 + 1) * t3 + t2) + 1);
		case FUNCTION_BOUNCE_OUT:
			if (t < (1 / 2.75f)) {
				return 7.5625f * t * t;
			} else if (t < (2 / 2.75f)) {
				t2 = t - (1.5f / 2.75f);
				return 7.5625f * t2 * t2 + 0.75f;
			} else if (t < (2.5 / 2.75)) {
				t2 = t - (2.25f / 2.75f);
				return 7.5625f * t2 * t2 + 0.9375f;
			} else {
				t2 = t - (2.625f / 2.75f);
				return 7.5625f * t2 * t2 + 0.984375f;
			}
		case FUNCTION_OUT_ELASTIC:
			t2 = 0.3f / 4;
			t3 = (float) (2 * MathUtils.PI / 0.3);
			return MathUtils.pow(2, -10 * t) * MathUtils.sin((t - t2) * t3) + 1;
		}
	}

	public static float inQuad(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return max * t * t + min;
	}

	public static float outQuad(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return -max * t * (t - 2.0f) + min;
	}

	public static float inOutQuad(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return max / 2.0f * t * t + min;
		}
		t -= 1.0f;
		return -max * (t * (t - 2.0f) - 1.0f) + min;
	}

	public static float inCubic(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return max * t * t * t + min;
	}

	public static float outCubic(float t, float totaltime, float max, float min) {
		max -= min;
		t = t / totaltime - 1.0f;
		return max * (t * t * t + 1.0f) + min;
	}

	public static float inOutCubic(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return max / 2.0f * t * t * t + min;
		}
		t -= 2.0f;
		return max / 2.0f * (t * t * t + 2.0f) + min;
	}

	public static float inQuart(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return max * t * t * t * t + min;
	}

	public static float outQuart(float t, float totaltime, float max, float min) {
		max -= min;
		t = t / totaltime - 1.0f;
		return -max * (t * t * t * t - 1.0f) + min;
	}

	public static float inOutQuart(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return max / 2.0f * t * t * t * t + min;
		}
		t -= 2.0f;
		return -max / 2.0f * (t * t * t * t - 2.0f) + min;
	}

	public static float inQuint(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return max * t * t * t * t * t + min;
	}

	public static float outQuint(float t, float totaltime, float max, float min) {
		max -= min;
		t = t / totaltime - 1.0f;
		return max * (t * t * t * t * t + 1.0f) + min;
	}

	public static float inOutQuint(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return max / 2.0f * t * t * t * t * t + min;
		}
		t -= 2.0f;
		return max / 2.0f * (t * t * t * t * t + 2.0f) + min;
	}

	public static float inSine(float t, float totaltime, float max, float min) {
		max -= min;
		return -max * MathUtils.cos(t * 1.570796326794897f / totaltime) + max + min;
	}

	public static float outSine(float t, float totaltime, float max, float min) {
		max -= min;
		return max * MathUtils.sin(t * 1.570796326794897f / totaltime) + min;
	}

	public static float inOutSine(float t, float totaltime, float max, float min) {
		max -= min;
		return -max / 2.0f * (MathUtils.cos(t * 3.141592653589793f / totaltime) - 1.0f) + min;
	}

	public static float inExp(float t, float totaltime, float max, float min) {
		max -= min;
		return t == 0.0f ? min : max * MathUtils.pow(2.0f, 10.0f * (t / totaltime - 1.0f)) + min;
	}

	public static float outExp(float t, float totaltime, float max, float min) {
		max -= min;
		return t == totaltime ? max + min : max * (-MathUtils.pow(2.0f, -10.0f * t / totaltime) + 1.0f) + min;
	}

	public static float inOutExp(float t, float totaltime, float max, float min) {
		if (t == 0f) {
			return min;
		}
		if (t == totaltime) {
			return max;
		}
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return max / 2.0f * MathUtils.pow(2.0f, 10.0f * (t - 1.0f)) + min;
		}
		t -= 1.0f;
		return max / 2.0f * (-MathUtils.pow(2.0f, -10.0f * t) + 2.0f) + min;
	}

	public static float inCirc(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return -max * (MathUtils.sqrt(1.0f - t * t) - 1.0f) + min;
	}

	public static float outCirc(float t, float totaltime, float max, float min) {
		max -= min;
		t = t / totaltime - 1.0f;
		return max * MathUtils.sqrt(1.0f - t * t) + min;
	}

	public static float inOutCirc(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return -max / 2.0f * (MathUtils.sqrt(1.0f - t * t) - 1.0f) + min;
		}
		t -= 2.0f;
		return max / 2.0f * (MathUtils.sqrt(1.0f - t * t) + 1.0f) + min;
	}

	public static float inElastic(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return -max * MathUtils.sin(20.420353f * t) * MathUtils.pow(2f, 10f * (t - 1f)) + min;
	}

	public static float outElastic(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		return max * MathUtils.sin(-20.420353f * (t + 1f)) * MathUtils.pow(2f, -10f * t) + 1f + min;
	}

	public static float inOutElastic(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t / 2.0f < 1.0f) {
			return -max / 2.0f * MathUtils.sin(20.420353f * (2f * t)) * MathUtils.pow(2f, 10f * (2f * t - 1f)) + min;
		}
		t -= 2.0f;
		return max / 2.0f
				* (MathUtils.sin(-20.420353f * (2f * t - 1f + 1f)) * MathUtils.pow(2f, -10f * (2f * t - 1f)) + 2) + min;

	}

	public static float inBack(float t, float totaltime, float max, float min, float s) {
		max -= min;
		t /= totaltime;
		return max * t * t * ((s + 1.0f) * t - s) + min;
	}

	public static float outBack(float t, float totaltime, float max, float min, float s) {
		max -= min;
		t = t / totaltime - 1.0f;
		return max * (t * t * ((s + 1.0f) * t * s) + 1.0f) + min;
	}

	public static float inOutBack(float t, float totaltime, float max, float min, float s) {
		max -= min;
		s *= 1.525f;
		if (t / 2.0f < 1.0f) {
			return max * (t * t * ((s + 1.0f) * t - s)) + min;
		}
		t -= 2.0f;
		return max / 2.0f * (t * t * ((s + 1.0f) * t + s) + 2.0f) + min;
	}

	public static float outBounce(float t, float totaltime, float max, float min) {
		max -= min;
		t /= totaltime;
		if (t < 0.3636363636363637f) {
			return max * (7.5625f * t * t) + min;
		}
		if (t < 0.7272727272727273f) {
			t -= 0.5454545454545454f;
			return max * (7.5625f * t * t + 0.75f) + min;
		}
		if (t < 0.9090909090909091f) {
			t -= 0.8181818181818182f;
			return max * (7.5625f * t * t + 0.9375f) + min;
		}
		t -= 0.9545454545454546f;
		return max * (7.5625f * t * t + 0.984375f) + min;
	}

	public static float inBounce(float t, float totaltime, float max, float min) {
		return max - outBounce(totaltime - t, totaltime, max - min, 0.0f) + min;
	}

	public static float inOutBounce(float t, float totaltime, float max, float min) {
		if (t < totaltime / 2.0f) {
			return inBounce(t * 2.0f, totaltime, max - min, max) * 0.5f + min;
		}
		return outBounce(t * 2.0f - totaltime, totaltime, max - min, 0.0f) * 0.5f + min + (max - min) * 0.5f;
	}

	public static float linear(float t) {
		return t;
	}

	public static float linear(float t, float max, float min) {
		return (max - min) * t + min;
	}

	public static float linear(float t, float totaltime, float max, float min) {
		return (max - min) * t / totaltime + min;
	}

	public static Vector2f linearXY(float t, Vector2f vector1, Vector2f vector2) {
		return vector1.cpy().lerp(vector2, t);
	}

	public static float between(float max, float min) {
		return MathUtils.floor(MathUtils.random() * (max - min + 1.0f) + min);
	}

	public static int stepped(float v, int steps) {
		if (v <= 0f) {
			return 0;
		} else if (v >= 1f) {
			return 1;
		} else {
			return (((MathUtils.ifloor(steps * v)) | 0) + 1) * MathUtils.ifloor(1f / steps);
		}
	}

	public static float quadEaseIn(float t) {
		return t * t;
	}

	public static float quadEaseOut(float t) {
		return -(t * (t - 2f));
	}

	public static float quadEaseInOut(float t) {
		if (t < 0.5f) {
			return 2f * t * t;
		} else {
			return -2f * t * t + 4f * t - 1f;
		}
	}

	public static float cubicEaseIn(float t) {
		return t * t * t;
	}

	public static float cubicEaseOut(float t) {
		float f = t - 1f;
		return f * f * f + 1f;
	}

	public static float cubicEaseInOut(float t) {
		if (t < 0.5f) {
			return 4f * t * t * t;
		} else {
			float f = 2f * t - 2f;
			return 0.5f * f * f * f + 1f;
		}
	}

	public static float quartEaseIn(float t) {
		return t * t * t * t;
	}

	public static float quartEaseOut(float t) {
		float f = t - 1f;
		return f * f * f * (1 - t) + 1f;
	}

	public static float quartEaseInOut(float t) {
		if (t < 0.5f) {
			return 8f * t * t * t * t;
		} else {
			float f = t - 1f;
			return -8 * f * f * f * f + 1f;
		}
	}

	public static float quintEaseIn(float t) {
		return t * t * t * t * t;
	}

	public static float quintEaseOut(float t) {
		float f = t - 1f;
		return f * f * f * f * f + 1f;
	}

	public static float quintEaseInOut(float t) {
		if (t < 0.5f) {
			return 16f * t * t * t * t * t;
		} else {
			float f = 2f * t - 2f;
			return 0.5f * f * f * f * f * f + 1f;
		}
	}

	public static float sineEaseIn(float t) {
		return MathUtils.sin((t - 1f) * MathUtils.HALF_PI) + 1f;
	}

	public static float sineEaseOut(float t) {
		return MathUtils.sin(t * MathUtils.HALF_PI);
	}

	public static float sineEaseInOut(float t) {
		return 0.5f * (1f - MathUtils.cos(t * MathUtils.PI));
	}

	public static float circEaseIn(float t) {
		return 1 - MathUtils.sqrt(1f - t * t);
	}

	public static float circEaseOut(float t) {
		return MathUtils.sqrt((2f - t) * t);
	}

	public static float circEaseInOut(float t) {
		if (t < 0.5f) {
			return 0.5f * (1f - MathUtils.sqrt(1 - 4f * (t * t)));
		} else {
			return 0.5f * (MathUtils.sqrt(-(2f * t - 3f) * (2f * t - 1f)) + 1f);
		}
	}

	public static float expEaseIn(float t) {
		return t == 0.0f ? t : MathUtils.pow(2f, 10f * (t - 1f));
	}

	public static float expEaseOut(float t) {
		return t == 1.0f ? t : 1f - MathUtils.pow(2f, -10 * t);
	}

	public static float expEaseInOut(float t) {
		if (t == 0.0f || t == 1.0f) {
			return t;
		}
		if (t < 0.5f) {
			return 0.5f * MathUtils.pow(2f, 20f * t - 10f);
		} else {
			return -0.5f * MathUtils.pow(2f, -20f * t + 10f) + 1f;
		}
	}

	public static float elasticEaseIn(float t) {
		return MathUtils.sin(20.420353f * t) * MathUtils.pow(2f, 10f * (t - 1f));
	}

	public static float elasticEaseOut(float t) {
		return MathUtils.sin(-20.420353f * (t + 1f)) * MathUtils.pow(2f, -10f * t) + 1f;
	}

	public static float elasticEaseInOut(float t) {
		if (t < 0.5f) {
			return 0.5f * MathUtils.sin(20.420353f * (2f * t)) * MathUtils.pow(2f, 10f * (2f * t - 1f));
		} else {
			return 0.5f
					* (MathUtils.sin(-20.420353f * (2f * t - 1f + 1f)) * MathUtils.pow(2f, -10f * (2f * t - 1f)) + 2);
		}
	}

	public static float backEaseIn(float t) {
		return t * t * t - t * MathUtils.sin(t * MathUtils.PI);
	}

	public static float backEaseOut(float t) {
		float f = 1 - t;
		return 1 - (f * f * f - f * MathUtils.sin(f * MathUtils.PI));
	}

	public static float backEaseInOut(float t) {
		if (t < 0.5f) {
			float f = 2f * t;
			return 0.5f * (f * f * f - f * MathUtils.sin(f * MathUtils.PI));
		} else {
			float f = 1f - (2f * t - 1f);
			return 0.5f * (1f - (f * f * f - f * MathUtils.sin(f * MathUtils.PI))) + 0.5f;
		}
	}

	public static float bounceEaseIn(float t) {
		return 1f - bounceEaseOut(1f - t);
	}

	public static float bounceEaseOut(float t) {
		if (t < 0.3636363636363637f) {
			return 1f * (7.5625f * t * t);
		}
		if (t < 0.7272727272727273f) {
			t -= 0.5454545454545454f;
			return 1f * (7.5625f * t * t + 0.75f);
		}
		if (t < 0.9090909090909091f) {
			t -= 0.8181818181818182f;
			return 1f * (7.5625f * t * t + 0.9375f);
		}
		t -= 0.9545454545454546f;
		return 1f * (7.5625f * t * t + 0.984375f);
	}

	public static float bounceEaseInOut(float t) {
		if (t < 0.5f) {
			return 0.5f * bounceEaseIn(t * 2f);
		} else {
			return 0.5f * bounceEaseOut(t * 2f - 1f) + 0.5f;
		}
	}
}
