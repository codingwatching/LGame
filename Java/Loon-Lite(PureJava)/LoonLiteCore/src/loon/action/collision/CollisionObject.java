/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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
package loon.action.collision;

import loon.action.ActionBind;
import loon.geom.RectBox;
import loon.geom.Shape;

public interface CollisionObject extends ActionBind {

	RectBox getBoundingRect();

	void setCollisionData(ActionBind data);

	ActionBind getCollisionData();

	boolean containsPoint(float x, float y);

	boolean intersects(CollisionObject o);

	boolean intersects(Shape shape);

	boolean contains(CollisionObject o);

	boolean contains(Shape shape);

	boolean collided(Shape shape);

	int getFlagType();
	
	String getObjectFlag();

	@Override
	float getX();

	@Override
	float getY();

	@Override
	float getWidth();

	@Override
	float getHeight();

	@Override
	RectBox getRectBox();

	@Override
	int getLayer();

}
