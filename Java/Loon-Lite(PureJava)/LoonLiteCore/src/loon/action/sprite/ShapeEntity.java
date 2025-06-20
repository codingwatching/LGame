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
package loon.action.sprite;

import loon.canvas.LColor;
import loon.geom.Shape;
import loon.opengl.GLEx;

/**
 * 由Shape生成Entity
 */
public class ShapeEntity extends Entity {

	private boolean _fill;

	public ShapeEntity(Shape shape, LColor c) {
		this(shape, c, true);
	}

	public ShapeEntity(Shape shape, LColor c, boolean fill) {
		this.setLocation(shape.getX(), shape.getY());
		this.setSize(shape.getWidth(), shape.getHeight());
		this.setColor(c == null ? LColor.white : c);
		this.setRepaint(true);
		this.setFill(fill);
		this.setCustomShape(shape);
	}

	@Override
	public void repaint(GLEx g, float offsetX, float offsetY) {
		final int color = g.color();
		g.setColor(_baseColor);
		if (_fill) {
			g.fill(_otherShape, drawX(offsetX), drawY(offsetY));
		} else {
			g.draw(_otherShape, drawX(offsetX), drawY(offsetY));
		}
		g.setColor(color);
	}

	public ShapeEntity setShape(Shape s) {
		this.setCustomShape(s);
		return this;
	}

	@Override
	public Shape getShape() {
		return this._otherShape;
	}

	public boolean isFill() {
		return _fill;
	}

	public ShapeEntity setFill(boolean f) {
		this._fill = f;
		return this;
	}

}
