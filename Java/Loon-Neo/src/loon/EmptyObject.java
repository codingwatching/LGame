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
package loon;

import loon.action.ActionBind;
import loon.action.ActionTween;
import loon.action.map.Field2D;
import loon.canvas.LColor;
import loon.geom.RectBox;

public class EmptyObject extends LObject<Object> implements ActionBind {

	private boolean _visible;

	private float _scaleX = 1f, _scaleY = 1f;

	private float _width = 0f, _height = 0f;

	private LColor _color;

	@Override
	public void update(long elapsedTime) {

	}

	@Override
	public float getWidth() {
		return _width;
	}

	@Override
	public float getHeight() {
		return _height;
	}

	@Override
	public Field2D getField2D() {
		return null;
	}

	@Override
	public void setVisible(boolean v) {
		this._visible = v;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}

	@Override
	public float getScaleX() {
		return _scaleX;
	}

	@Override
	public float getScaleY() {
		return _scaleY;
	}

	public EmptyObject setScale(float scale) {
		this.setScale(scale, scale);
		return this;
	}

	@Override
	public void setScale(float sx, float sy) {
		this._scaleX = sx;
		this._scaleY = sy;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean inContains(float x, float y, float w, float h) {
		return false;
	}

	@Override
	public RectBox getRectBox() {
		return getCollisionArea();
	}

	@Override
	public void setColor(LColor color) {
		this._color = color;
	}

	@Override
	public LColor getColor() {
		return _color;
	}

	@Override
	public ActionBind setSize(float w, float h) {
		this._width = w;
		this._height = h;
		return this;
	}

	@Override
	public ActionTween selfAction() {
		return PlayerUtils.set(this);
	}

	@Override
	public boolean isActionCompleted() {
		return PlayerUtils.isActionCompleted(this);
	}

	@Override
	protected void _onDestroy() {

	}

}
