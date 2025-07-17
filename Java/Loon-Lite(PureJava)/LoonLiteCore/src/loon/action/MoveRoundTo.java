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
package loon.action;

import loon.LSystem;
import loon.geom.Vector2f;
import loon.utils.Easing.EasingMode;
import loon.utils.MathUtils;
import loon.utils.StringKeyValue;
import loon.utils.timer.EaseTimer;

/**
 * 让指定对象做环绕运动(与CircleTo差异在于CircleTo不会自行停止,且CircleTo仅以对象本身为中心做360度旋转),需要偏移或矫正显示位置时请设置setOffset参数
 */
public class MoveRoundTo extends ActionEvent {

	private final float angle;
	private final float startAngle;
	private final float radius;
	private final float startRadius;
	private Vector2f startPoint, oldStartPoint;
	private Vector2f centerPoint, oldCenterPoint;

	public MoveRoundTo(float angle, float radius, Vector2f centerPoint, float duration, EasingMode easing) {
		this(0f, angle, 0f, radius, centerPoint, null, duration, LSystem.DEFAULT_EASE_DELAY, easing);
	}

	public MoveRoundTo(float angle, float radius, Vector2f centerPoint, EasingMode easing) {
		this(0f, angle, 0f, radius, centerPoint, null, 1f, LSystem.DEFAULT_EASE_DELAY, easing);
	}

	public MoveRoundTo(float angle, float radius, Vector2f centerPoint, float duration, float delay,
			EasingMode easing) {
		this(0f, angle, 0f, radius, centerPoint, null, duration, delay, easing);
	}

	public MoveRoundTo(float startAngle, float angle, float startRadius, float radius, Vector2f centerPoint) {
		this(startAngle, angle, startRadius, radius, centerPoint, null, 1f, LSystem.DEFAULT_EASE_DELAY,
				EasingMode.Linear);
	}

	public MoveRoundTo(float startAngle, float angle, float startRadius, float radius, float duration,
			Vector2f centerPoint) {
		this(startAngle, angle, startRadius, radius, centerPoint, null, duration, LSystem.DEFAULT_EASE_DELAY,
				EasingMode.Linear);
	}

	public MoveRoundTo(float startAngle, float angle, float startRadius, float radius, Vector2f centerPoint,
			Vector2f startPoint, float duration, float delay, EasingMode easing) {
		if (angle > 360) {
			angle = 360;
		}
		this.angle = angle;
		this.radius = radius;
		this.startAngle = startAngle;
		this.startRadius = startRadius;
		this.centerPoint = centerPoint;
		this.startPoint = startPoint;
		if (startPoint == null) {
			startPoint = new Vector2f();
		}
		this._easeTimer = new EaseTimer(duration, delay, easing);
		this.oldStartPoint = startPoint;
		this.oldCenterPoint = centerPoint;
	}

	@Override
	public void update(long elapsedTime) {
		_easeTimer.update(elapsedTime);
		if (_easeTimer.isCompleted()) {
			_isCompleted = true;
			float radian = MathUtils.toRadians(this.startAngle + this.angle);
			float x = this.centerPoint.x + MathUtils.cos(radian) * (this.startRadius + this.radius);
			float y = this.centerPoint.y + MathUtils.sin(radian) * (this.startRadius + this.radius);
			movePos(x + original.getWidth() / 2 + offsetX, y + original.getHeight() / 2 + offsetY);
			return;
		}
		float currentRadius = this.startRadius + this.radius * _easeTimer.getProgress();
		float currentAngle = this.startAngle + this.angle * _easeTimer.getProgress();
		float radian = MathUtils.toRadians(currentAngle);
		this.startPoint.x = (this.centerPoint.x + MathUtils.cos(radian) * currentRadius);
		this.startPoint.y = (this.centerPoint.y + MathUtils.sin(radian) * currentRadius);
		movePos(this.startPoint.x + offsetX, this.startPoint.y + offsetY);
	}

	public float getAngle() {
		return angle;
	}

	public float getStartAngle() {
		return startAngle;
	}

	public float getRadius() {
		return radius;
	}

	public float getStartRadius() {
		return startRadius;
	}

	@Override
	public void onLoad() {
		if (startPoint == null || startPoint.getX() == -1 || startPoint.getY() == -1) {
			this.startPoint = new Vector2f(original.getX(), original.getY());
		}
		this.oldStartPoint.set(startPoint);
		this.oldCenterPoint.set(centerPoint);
	}

	@Override
	public ActionEvent cpy() {
		MoveRoundTo mover = new MoveRoundTo(startAngle, angle, startRadius, radius, oldCenterPoint, oldStartPoint,
				_easeTimer.getDuration(), _easeTimer.getDelay(), _easeTimer.getEasingMode());
		mover.set(this);
		return mover;
	}

	@Override
	public ActionEvent reverse() {
		return cpy();
	}

	@Override
	public String getName() {
		return "moveround";
	}

	@Override
	public String toString() {
		final StringKeyValue builder = new StringKeyValue(getName());
		builder.kv("startAngle", startAngle).comma().kv("angle", angle).comma().kv("startRadius", startRadius).comma()
				.kv("radius", radius).comma().kv("startPoint", startPoint).comma().kv("centerPoint", centerPoint)
				.comma().kv("EaseTimer", _easeTimer);
		return builder.toString();
	}

}
