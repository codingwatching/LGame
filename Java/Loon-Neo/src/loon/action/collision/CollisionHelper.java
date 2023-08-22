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
package loon.action.collision;

import loon.canvas.Image;
import loon.canvas.LColor;
import loon.geom.BoxSize;
import loon.geom.Line;
import loon.geom.Point;
import loon.geom.RangeF;
import loon.geom.RectBox;
import loon.geom.Shape;
import loon.geom.ShapeUtils;
import loon.geom.Vector2f;
import loon.geom.Vector3f;
import loon.geom.XY;
import loon.geom.XYZ;
import loon.utils.MathUtils;
import loon.utils.TArray;

/**
 * 碰撞事件检测与处理工具类,内部是一系列碰撞检测与处理方法的集合
 */
public final class CollisionHelper extends ShapeUtils {

	private CollisionHelper() {
	}

	private static final RectBox rectTemp1 = new RectBox();

	private static final RectBox rectTemp2 = new RectBox();

	/**
	 * 检查两个坐标值是否在指定的碰撞半径内
	 * 
	 * @param x1
	 * @param y1
	 * @param r1
	 * @param x2
	 * @param y2
	 * @param r2
	 * @return
	 */
	public static boolean isCollision(float x1, float y1, float r1, float x2, float y2, float r2) {
		float a = r1 + r2;
		float dx = x1 - x2;
		float dy = y1 - y2;
		return a * a > dx * dx + dy * dy;
	}

	/**
	 * 获得两个三维体间初始XYZ位置的距离
	 * 
	 * @param target
	 * @param beforePlace
	 * @param distance
	 * @return
	 */
	public static Vector3f getDistantPoint(XYZ target, XYZ source, float distance) {

		float deltaX = target.getX() - source.getX();
		float deltaY = target.getY() - source.getY();
		float deltaZ = target.getZ() - source.getZ();

		float dist = MathUtils.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		deltaX /= dist;
		deltaY /= dist;
		deltaZ /= dist;

		return new Vector3f(target.getX() - distance * deltaX, target.getY() - distance * deltaY,
				target.getZ() - distance * deltaZ);
	}

	/**
	 * 获得两个三维体间初始XYZ位置的距离
	 * 
	 * @param target
	 * @param source
	 * @param distance
	 * @return
	 */
	public static Vector2f distantPoint(XY target, XY source, float distance) {

		float deltaX = target.getX() - source.getX();
		float deltaY = target.getY() - source.getY();

		float dist = MathUtils.sqrt(deltaX * deltaX + deltaY * deltaY);

		deltaX /= dist;
		deltaY /= dist;

		return new Vector2f(target.getX() - distance * deltaX, target.getY() - distance * deltaY);
	}

	/**
	 * 获得两个矩形间初始XY位置的距离
	 * 
	 * @param target
	 * @param beforePlace
	 * @return
	 */
	public static float getDistance(final BoxSize target, final BoxSize beforePlace) {
		if (target == null || beforePlace == null) {
			return 0f;
		}
		final float xdiff = target.getX() - beforePlace.getX();
		final float ydiff = target.getY() - beforePlace.getY();
		return MathUtils.sqrt(xdiff * xdiff + ydiff * ydiff);
	}

	/**
	 * 获得多个点间距离
	 * 
	 * @param target
	 * @param beforePlace
	 * @param afterPlace
	 * @param distance
	 * @return
	 */
	public static final float getDistance(XY target, XY beforePlace, XY afterPlace, float distance) {
		return getDistance(target, beforePlace, afterPlace, distance, false);
	}

	/**
	 * 获得多个点间距离
	 * 
	 * @param target
	 * @param beforePlace
	 * @param afterPlace
	 * @param distance
	 * @param limit
	 * @return
	 */
	public static final float getDistance(XY target, XY beforePlace, XY afterPlace, float distance, boolean limit) {
		float before = MathUtils.abs(target.getX() - beforePlace.getX())
				+ MathUtils.abs(target.getY() - beforePlace.getY());
		float after = MathUtils.abs(target.getX() - afterPlace.getX())
				+ MathUtils.abs(target.getY() - afterPlace.getY());
		if (limit && before > distance) {
			return 0;
		}
		return 1f * (before - after) / after;
	}

	/**
	 * 检查两个矩形是否发生了碰撞
	 * 
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static boolean isRectToRect(BoxSize rect1, BoxSize rect2) {
		if (rect1 == null || rect2 == null) {
			return false;
		}
		return intersects(rect1.getX(), rect1.getY(), rect1.getWidth(), rect1.getHeight(), rect2.getX(), rect2.getY(),
				rect2.getWidth(), rect2.getHeight());
	}

	/**
	 * 判断两个圆形是否发生了碰撞
	 * 
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static boolean isCircToCirc(BoxSize rect1, BoxSize rect2) {
		Point middle1 = getMiddlePoint(rect1);
		Point middle2 = getMiddlePoint(rect2);
		float distance = middle1.distanceTo(middle2);
		float radius1 = rect1.getWidth() / 2;
		float radius2 = rect2.getWidth() / 2;
		return (distance - radius2) < radius1;
	}

	/**
	 * 检查矩形与圆形是否发生了碰撞
	 * 
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static boolean isRectToCirc(BoxSize rect1, BoxSize rect2) {
		float radius = rect2.getWidth() / 2;
		float minX = rect1.getX();
		float minY = rect1.getY();
		float maxX = rect1.getX() + rect1.getWidth();
		float maxY = rect1.getY() + rect1.getHeight();
		Point middle = getMiddlePoint(rect2);
		Point upperLeft = new Point(minX, minY);
		Point upperRight = new Point(maxX, minY);
		Point downLeft = new Point(minX, maxY);
		Point downRight = new Point(maxX, maxY);
		boolean collided = true;
		if (!isPointToLine(upperLeft, upperRight, middle, radius)) {
			if (!isPointToLine(upperRight, downRight, middle, radius)) {
				if (!isPointToLine(upperLeft, downLeft, middle, radius)) {
					if (!isPointToLine(downLeft, downRight, middle, radius)) {
						collided = false;
					}
				}
			}
		}
		return collided;
	}

	/**
	 * 换算点线距离
	 * 
	 * @param point1
	 * @param point2
	 * @param middle
	 * @param radius
	 * @return
	 */
	private static boolean isPointToLine(XY point1, XY point2, XY middle, float radius) {
		Line line = new Line(point1, point2);
		float distance = line.ptLineDist(middle);
		return distance < radius;
	}

	/**
	 * 返回中间距离的Point2D形式
	 * 
	 * @param rectangle
	 * @return
	 */
	private static Point getMiddlePoint(BoxSize rectangle) {
		return new Point(rectangle.getCenterX(), rectangle.getCenterY());
	}

	/**
	 * 判定指定的两张图片之间是否产生了碰撞
	 * 
	 * @param src
	 * @param x1
	 * @param y1
	 * @param dest
	 * @param x2
	 * @param y2
	 * @return
	 */
	public boolean isPixelCollide(Image src, float x1, float y1, Image dest, float x2, float y2) {

		float width1 = x1 + src.width() - 1, height1 = y1 + src.height() - 1, width2 = x2 + dest.width() - 1,
				height2 = y2 + dest.height() - 1;

		int xstart = (int) MathUtils.max(x1, x2), ystart = (int) MathUtils.max(y1, y2),
				xend = (int) MathUtils.min(width1, width2), yend = (int) MathUtils.min(height1, height2);

		int toty = MathUtils.abs(yend - ystart);
		int totx = MathUtils.abs(xend - xstart);

		for (int y = 1; y < toty - 1; y++) {
			int ny = MathUtils.abs(ystart - (int) y1) + y;
			int ny1 = MathUtils.abs(ystart - (int) y2) + y;

			for (int x = 1; x < totx - 1; x++) {
				int nx = MathUtils.abs(xstart - (int) x1) + x;
				int nx1 = MathUtils.abs(xstart - (int) x2) + x;

				try {
					if (((src.getPixel(nx, ny) & LColor.TRANSPARENT) != 0x00)
							&& ((dest.getPixel(nx1, ny1) & LColor.TRANSPARENT) != 0x00)) {
						return true;
					} else if (getPixelData(src, nx, ny)[0] != 0 && getPixelData(dest, nx1, ny1)[0] != 0) {
						return true;
					}
				} catch (Throwable e) {

				}
			}
		}
		return false;
	}

	private static final int[] getPixelData(Image image, int x, int y) {
		return LColor.getRGBs(image.getPixel(x, y));
	}

	public static boolean isPointInRect(float rectX, float rectY, float rectW, float rectH, float x, float y) {
		if (x >= rectX && x <= rectX + rectW) {
			if (y >= rectY && y <= rectY + rectH) {
				return true;
			}
		}
		return false;
	}

	public static final boolean intersects(RectBox rect, float x, float y) {
		if (rect != null) {
			if (rect.Left() <= x && x < rect.Right() && rect.Top() <= y && y < rect.Bottom())
				return true;
		}
		return false;
	}

	public static final boolean intersects(float sx, float sy, float width, float height, float x, float y) {
		return (x >= sx) && ((x - sx) < width) && (y >= sy) && ((y - sy) < height);
	}

	/**
	 * 判断指定大小的两组像素是否相交
	 * 
	 * @param rectA
	 * @param dataA
	 * @param rectB
	 * @param dataB
	 * @return
	 */
	public static boolean intersects(RectBox rectA, int[] dataA, RectBox rectB, int[] dataB) {
		int top = (int) MathUtils.max(rectA.getY(), rectB.getY());
		int bottom = (int) MathUtils.min(rectA.getBottom(), rectB.getBottom());
		int left = (int) MathUtils.max(rectA.getX(), rectB.getX());
		int right = (int) MathUtils.min(rectA.getRight(), rectB.getRight());

		for (int y = top; y < bottom; y++) {
			for (int x = left; x < right; x++) {

				int colorA = dataA[(int) ((x - rectA.x) + (y - rectA.y) * rectA.width)];
				int colorB = dataB[(int) ((x - rectB.x) + (y - rectB.y) * rectB.width)];
				if (colorA >>> 24 != 0 && colorB >>> 24 != 0) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断两个Shape是否相交
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static final boolean intersects(Shape s1, Shape s2) {
		if (s1 == null || s2 == null) {
			return false;
		}
		return s1.intersects(s2);
	}

	public static final int[] intersects(RectBox rect1, RectBox rect2) {
		if (rect1.Left() < rect2.Right() && rect2.Left() < rect1.Right() && rect1.Top() < rect2.Bottom()
				&& rect2.Top() < rect1.Bottom()) {
			return new int[] { rect1.Left() < rect2.Left() ? rect2.Left() - rect1.Left() : 0,
					rect1.Top() < rect2.Top() ? rect2.Top() - rect1.Top() : 0,
					rect1.Right() > rect2.Right() ? rect1.Right() - rect2.Right() : 0,
					rect1.Bottom() > rect2.Bottom() ? rect1.Bottom() - rect2.Bottom() : 0 };
		}
		return null;
	}

	public static final boolean intersects(float x, float y, float width, float height, float dx, float dy, float dw,
			float dh) {
		return intersects(x, y, width, height, dx, dy, dw, dh, false);
	}

	public static final boolean intersects(float x, float y, float width, float height, float dx, float dy, float dw,
			float dh, boolean touchingIsIn) {
		rectTemp1.setBounds(x, y, width, height).normalize();
		rectTemp2.setBounds(dx, dy, dw, dh).normalize();
		if (touchingIsIn) {
			if (rectTemp1.x + rectTemp1.width == rectTemp2.x) {
				return true;
			}
			if (rectTemp1.x == rectTemp2.x + rectTemp2.width) {
				return true;
			}
			if (rectTemp1.y + rectTemp1.height == rectTemp2.y) {
				return true;
			}
			if (rectTemp1.y == rectTemp2.y + rectTemp2.height) {
				return true;
			}
		}
		return rectTemp1.intersects(rectTemp2);
	}

	/**
	 * 计算并返回两个正方形之间的碰撞间距值
	 * 
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static float squareRects(BoxSize rect1, BoxSize rect2) {
		if (rect1 == null || rect2 == null) {
			return 0f;
		}
		return squareRects(rect1.getX(), rect1.getY(), rect1.getWidth(), rect1.getHeight(), rect2.getX(), rect2.getY(),
				rect2.getWidth(), rect2.getHeight());
	}

	/**
	 * 计算并返回两个正方形之间的碰撞间距值
	 * 
	 * @param x1
	 * @param y1
	 * @param w1
	 * @param h1
	 * @param x2
	 * @param y2
	 * @param w2
	 * @param h2
	 * @return
	 */
	public static float squareRects(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
		if (x1 < x2 + w2 && x2 < x1 + w1) {
			if (y1 < y2 + h2 && y2 < y1 + h1) {
				return 0f;
			}
			if (y1 > y2) {
				return (y1 - (y2 + h2)) * (y1 - (y2 + h2));
			}
			return (y2 - (y1 + h1)) * (y2 - (y1 + h1));
		}
		if (y1 < y2 + h2 && y2 < y1 + h1) {
			if (x1 > x2) {
				return (x1 - (x2 + w2)) * (x1 - (x2 + w2));
			}
			return (x2 - (x1 + w1)) * (x2 - (x1 + w1));
		}
		if (x1 > x2) {
			if (y1 > y2) {
				return MathUtils.distSquared((x2 + w2), (y2 + h2), x1, y1);
			}
			return MathUtils.distSquared(x2 + w2, y2, x1, y1 + h1);
		}
		if (y1 > y2) {
			return MathUtils.distSquared(x2, y2 + h2, x1 + w1, y1);
		}
		return MathUtils.distSquared(x2, y2, x1 + w1, y1 + h1);
	}

	/**
	 * 计算并返回指定位置与指定正方形之间的碰撞间距值
	 * 
	 * @param xy
	 * @param box
	 * @return
	 */
	public static float squarePointRect(XY xy, BoxSize box) {
		if (xy == null || box == null) {
			return 0f;
		}
		return squarePointRect(xy.getX(), xy.getY(), box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}

	/**
	 * 计算并返回指定位置与指定正方形之间的碰撞间距值
	 * 
	 * @param px
	 * @param py
	 * @param rx
	 * @param ry
	 * @param rw
	 * @param rh
	 * @return
	 */
	public static float squarePointRect(float px, float py, float rx, float ry, float rw, float rh) {
		if (px >= rx && px <= rx + rw) {
			if (py >= ry && py <= ry + rh) {
				return 0f;
			}
			if (py > ry) {
				return (py - (ry + rh)) * (py - (ry + rh));
			}
			return (ry - py) * (ry - py);
		}
		if (py >= ry && py <= ry + rh) {
			if (px > rx) {
				return (px - (rx + rw)) * (px - (rx + rw));
			}
			return (rx - px) * (rx - px);
		}
		if (px > rx) {
			if (py > ry) {
				return MathUtils.distSquared(rx + rw, ry + rh, px, py);
			}
			return MathUtils.distSquared(rx + rw, ry, px, py);
		}
		if (py > ry) {
			return MathUtils.distSquared(rx, ry + rh, px, py);
		}
		return MathUtils.distSquared(rx, ry, px, py);
	}

	/**
	 * 判断两个Shape是否存在包含关系
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static final boolean contains(Shape s1, Shape s2) {
		if (s1 == null || s2 == null) {
			return false;
		}
		return s1.contains(s2);
	}

	public static final boolean contains(float sx, float sy, float sw, float sh, float dx, float dy, float dw,
			float dh) {
		return (dx >= sx && dy >= sy && ((dx + dw) <= (sx + sw)) && ((dy + dh) <= (sy + sh)));
	}

	public static final boolean contains(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
		return (dx >= sx && dy >= sy && ((dx + dw) <= (sx + sw)) && ((dy + dh) <= (sy + sh)));
	}

	public static final boolean containsIsometric(int x, int y, int w, int h, int px, int py) {
		float mx = w / 2;
		float my = h / 2;
		float ix = px - x;
		float iy = py - y;
		if (iy > my) {
			iy = my - (iy - my);
		}
		if ((ix > mx + 1 + (2 * iy)) || (ix < mx - 1 - (2 * iy))) {
			return false;
		}
		return true;
	}

	public static final boolean containsHexagon(int x, int y, int w, int h, int px, int py) {
		float mx = w / 4;
		float my = h / 2;
		float hx = px - x;
		float hy = py - y;
		if (hx > mx * 3) {
			hx = mx - (hx - mx * 3);
		} else if (hx > mx) {
			return py >= y && py <= y + h;
		}
		if ((hy > my + 1 + (2 * hx)) || (hy < my - 1 - (2 * hx))) {
			return false;
		}
		return true;
	}

	public static final void confine(RectBox rect, RectBox field) {
		int x = rect.Right() > field.Right() ? field.Right() - (int) rect.getWidth() : rect.Left();
		if (x < field.Left()) {
			x = field.Left();
		}
		int y = (int) (rect.Bottom() > field.Bottom() ? field.Bottom() - rect.getHeight() : rect.Top());
		if (y < field.Top()) {
			y = field.Top();
		}
		rect.offset(x, y);
	}

	public static final RectBox constructRect(Vector2f topLeft, Vector2f bottomRight) {
		return new RectBox(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
	}

	public static final RectBox constructRect(Vector2f pos, Vector2f size, Vector2f alignement) {
		Vector2f offset = size.mul(alignement);
		Vector2f topLeft = pos.sub(offset);
		return new RectBox(topLeft.x, topLeft.y, size.x, size.y);
	}

	public static final Object[] collideField(RectBox rect, Vector2f pos, float radius) {
		boolean collided = false;
		Vector2f hitPoint = pos;
		Vector2f result = new Vector2f();
		Vector2f newPos = pos;
		if (pos.x + radius > rect.x + rect.width) {
			hitPoint = new Vector2f(rect.x + rect.width, pos.y);
			newPos.x = hitPoint.x - radius;
			result = new Vector2f(-1f, 0f);
			collided = true;
		} else if (pos.x - radius < rect.x) {
			hitPoint = new Vector2f(rect.x, pos.y);
			newPos.x = hitPoint.x + radius;
			result = new Vector2f(1f, 0f);
			collided = true;
		}

		if (pos.y + radius > rect.y + rect.height) {
			hitPoint = new Vector2f(pos.x, rect.y + rect.height);
			newPos.y = hitPoint.y - radius;
			result = new Vector2f(0f, -1f);
			collided = true;
		} else if (pos.y - radius < rect.y) {
			hitPoint = new Vector2f(pos.x, rect.y);
			newPos.y = hitPoint.y + radius;
			result = new Vector2f(0f, 1f);
			collided = true;
		}

		return new Object[] { collided, hitPoint, result, newPos };
	}

	public static final Object[] collideAroundField(RectBox rect, Vector2f pos, float radius) {
		boolean outOfBounds = false;
		Vector2f newPos = pos;
		if (pos.x + radius > rect.x + rect.width) {
			newPos = new Vector2f(rect.x, pos.y);
			outOfBounds = true;
		} else if (pos.x - radius < rect.x) {
			newPos = new Vector2f(rect.x + rect.width, pos.y);
			outOfBounds = true;
		}
		if (pos.y + radius > rect.y + rect.height) {
			newPos = new Vector2f(pos.x, rect.y);
			outOfBounds = true;
		} else if (pos.y - radius < rect.y) {
			newPos = new Vector2f(pos.x, rect.y + rect.height);
			outOfBounds = true;
		}
		return new Object[] { outOfBounds, newPos };
	}

	public static final Line getLine(Shape shape, int s, int e) {
		float[] start = shape.getPoint(s);
		float[] end = shape.getPoint(e);
		Line line = new Line(start[0], start[1], end[0], end[1]);
		return line;
	}

	public static final Line getLine(Shape shape, float sx, float sy, int e) {
		float[] end = shape.getPoint(e);
		Line line = new Line(sx, sy, end[0], end[1]);
		return line;
	}

	public static final boolean checkOverlappingRange(float minA, float maxA, float minB, float maxB) {
		if (maxA < minA) {
			float temp = minA;
			minA = maxA;
			maxA = temp;
		}
		if (maxB < minB) {
			float temp = minB;
			minB = maxB;
			maxB = temp;
		}
		return minB <= maxA && minA <= maxB;
	}

	public static final boolean checkOverlappingRange(RangeF a, RangeF b) {
		return checkOverlappingRange(a.getMin(), a.getMax(), b.getMin(), b.getMax());
	}

	public static final boolean checkAABBvsAABB(XY p1, float w1, float h1, XY p2, float w2, float h2) {
		return checkAABBvsAABB(p1.getX(), p1.getY(), w1, h1, p2.getX(), p2.getY(), w2, h2);
	}

	public static final boolean checkAABBvsAABB(float x1, float y1, float w1, float h1, float x2, float y2, float w2,
			float h2) {
		return x1 < x2 + w2 && x2 < x1 + w1 && y1 < y2 + h2 && y2 < y1 + h1;
	}

	public static final boolean checkAABBvsAABB(XY p1Min, XY p1Max, XY p2Min, XY p2Max) {
		return checkAABBvsAABB(p1Min.getX(), p1Min.getY(), p1Max.getX() - p1Min.getX(), p1Max.getY() - p1Min.getY(),
				p2Min.getX(), p2Min.getY(), p2Max.getX() - p2Min.getX(), p2Max.getY() - p2Min.getY());
	}

	public static final boolean checkAABBvsAABB(XYZ p1, float w1, float h1, float t1, XYZ p2, float w2, float h2,
			float t2) {
		return checkAABBvsAABB(p1.getX(), p1.getY(), p1.getZ(), w1, h1, t1, p2.getX(), p2.getY(), p2.getZ(), w2, h2,
				t2);
	}

	public static final boolean checkAABBvsAABB(float x1, float y1, float z1, float w1, float h1, float t1, float x2,
			float y2, float z2, float w2, float h2, float t2) {
		return !(x1 > x2 + w2 || x1 + w1 < x2) && !(y1 > y2 + h2 || y1 + h1 < y2) && !(z1 > z2 + t2 || z1 + t1 < z2);
	}

	public static final boolean checkAABBvsAABB(XYZ p1Min, XYZ p1Max, XYZ p2Min, XYZ p2Max) {
		return checkAABBvsAABB(p1Min.getX(), p1Min.getY(), p1Min.getZ(), p1Max.getX() - p1Min.getX(),
				p1Max.getY() - p1Min.getY(), p1Max.getZ() - p1Min.getZ(), p2Min.getX(), p2Min.getY(), p1Min.getZ(),
				p2Max.getX() - p2Min.getX(), p2Max.getY() - p2Min.getY(), p2Max.getZ() - p2Min.getZ());
	}

	public static final boolean checkCircleCircle(XY p1, float r1, XY p2, float r2) {
		return checkCircleCircle(p1.getX(), p1.getY(), r1, p2.getX(), p2.getY(), r2);
	}

	public static final boolean checkCircleCircle(float x1, float y1, float r1, float x2, float y2, float r2) {
		float distance = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		float radiusSumSq = (r1 + r2) * (r1 + r2);

		return distance <= radiusSumSq;
	}

	public static final boolean checkSphereSphere(XYZ p1, float r1, XYZ p2, float r2) {
		return checkSphereSphere(p1.getX(), p1.getY(), p1.getZ(), r1, p2.getX(), p2.getY(), p2.getZ(), r2);
	}

	public static final boolean checkSphereSphere(float x1, float y1, float z1, float r1, float x2, float y2, float z2,
			float r2) {
		float distance = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1);
		float radiusSumSq = (r1 + r2) * (r1 + r2);
		return distance <= radiusSumSq;
	}

	public static final boolean checkSegmentOnOneSide(Vector2f axisPos, Vector2f axisDir, Vector2f segmentPos,
			Vector2f segmentEnd) {
		Vector2f d1 = segmentPos.sub(axisPos);
		Vector2f d2 = segmentEnd.sub(axisPos);
		Vector2f n = Vector2f.rotationLeft(axisDir);
		return n.dot(d1) * n.dot(d2) > 0f;
	}

	public static final boolean checkSeperateAxisRect(Vector2f axisStart, Vector2f axisEnd, Vector2f rectPos,
			Vector2f rectSize, Vector2f rectAlignement) {
		Vector2f result = axisStart.sub(axisEnd);
		Vector2f edgeAStart = getRectCorner(rectPos, rectSize, rectAlignement, 0);
		Vector2f edgeAEnd = getRectCorner(rectPos, rectSize, rectAlignement, 1);
		Vector2f edgeBStart = getRectCorner(rectPos, rectSize, rectAlignement, 2);
		Vector2f edgeBEnd = getRectCorner(rectPos, rectSize, rectAlignement, 3);

		RangeF edgeARange = getProjectSegment(edgeAStart, edgeAEnd, result);
		RangeF edgeBRange = getProjectSegment(edgeBStart, edgeBEnd, result);
		RangeF projection = getRangeHull(edgeARange, edgeBRange);

		RangeF axisRange = getProjectSegment(axisStart, axisEnd, result);
		return !checkOverlappingRange(axisRange, projection);
	}

	public static final RangeF getProjectSegment(Vector2f pos, Vector2f end, Vector2f onto) {
		Vector2f unitOnto = onto.nor();
		return new RangeF(unitOnto.dot(pos), unitOnto.dot(end));
	}

	public static final float getJumpVelocity(float gravity, float distance) {
		return MathUtils.sqrt(2 * distance * gravity);
	}

	public final static boolean checkAngle(float angle, float actual) {
		return actual > angle - 22.5f && actual < angle + 22.5f;
	}

	/**
	 * 判断两点坐标是否存在移动
	 * 
	 * @param distance
	 * @param startPoints
	 * @param endPoint
	 * @return
	 */
	public static final boolean isMoved(float distance, XY startPoints, XY endPoint) {
		return isMoved(distance, startPoints.getX(), startPoints.getY(), endPoint.getX(), endPoint.getY());
	}

	/**
	 * 判断两点坐标是否存在移动
	 * 
	 * @param distance
	 * @param sx
	 * @param sy
	 * @param dx
	 * @param dy
	 * @return
	 */
	public static final boolean isMoved(float distance, float sx, float sy, float dx, float dy) {
		float xDistance = dx - sx;
		float yDistance = dy - sy;
		if (MathUtils.abs(xDistance) < distance && MathUtils.abs(yDistance) < distance) {
			return false;
		}
		return true;
	}

	public static final Vector2f nearestToLine(Vector2f p1, Vector2f p2, Vector2f p3, Vector2f n) {
		int ax = (int) (p2.x - p1.x), ay = (int) (p2.y - p1.y);
		float u = (p3.x - p1.x) * ax + (p3.y - p1.y) * ay;
		u /= (ax * ax + ay * ay);
		n.x = p1.x + MathUtils.round(ax * u);
		n.y = p1.y + MathUtils.round(ay * u);
		return n;
	}

	public static final boolean lineIntersection(XY p1, XY p2, boolean seg1, XY p3, XY p4, boolean seg2,
			Vector2f result) {
		float y43 = p4.getY() - p3.getY();
		float x21 = p2.getX() - p1.getX();
		float x43 = p4.getX() - p3.getX();
		float y21 = p2.getY() - p1.getY();
		float denom = y43 * x21 - x43 * y21;
		if (denom == 0) {
			return false;
		}

		float y13 = p1.getY() - p3.getY();
		float x13 = p1.getX() - p3.getX();
		float ua = (x43 * y13 - y43 * x13) / denom;
		if (seg1 && ((ua < 0) || (ua > 1))) {
			return false;
		}

		if (seg2) {
			float ub = (x21 * y13 - y21 * x13) / denom;
			if ((ub < 0) || (ub > 1)) {
				return false;
			}
		}

		float x = p1.getX() + ua * x21;
		float y = p1.getY() + ua * y21;
		result.setLocation(x, y);
		return true;
	}

	public static final boolean lineIntersection(XY p1, XY p2, XY p3, XY p4, Vector2f ptIntersection) {
		float num1 = ((p4.getY() - p3.getY()) * (p2.getX() - p1.getX()))
				- ((p4.getX() - p3.getX()) * (p2.getY() - p1.getY()));
		float num2 = ((p4.getX() - p3.getX()) * (p1.getY() - p3.getY()))
				- ((p4.getY() - p3.getY()) * (p1.getX() - p3.getX()));
		float num3 = ((p2.getX() - p1.getX()) * (p1.getY() - p3.getY()))
				- ((p2.getY() - p1.getY()) * (p1.getX() - p3.getX()));
		if (num1 != 0f) {
			float num4 = num2 / num1;
			float num5 = num3 / num1;
			if (((num4 >= 0f) && (num4 <= 1f)) && ((num5 >= 0f) && (num5 <= 1f))) {
				ptIntersection.x = (int) (p1.getX() + (num4 * (p2.getX() - p1.getX())));
				ptIntersection.y = (int) (p1.getY() + (num4 * (p2.getY() - p1.getY())));
				return true;
			}
		}
		return false;
	}

	public static final int whichSide(XY p1, float theta, XY p2) {
		theta += MathUtils.PI / 2;
		float x = (int) (p1.getX() + MathUtils.round(1000 * MathUtils.cos(theta)));
		float y = (int) (p1.getY() + MathUtils.round(1000 * MathUtils.sin(theta)));
		return MathUtils.iceil(dotf(p1.getX(), p1.getY(), p2.getX(), p2.getY(), x, y));
	}

	public static final void shiftToContain(RectBox tainer, RectBox tained) {
		if (tained.x < tainer.x) {
			tainer.x = tained.x;
		}
		if (tained.y < tainer.y) {
			tainer.y = tained.y;
		}
		if (tained.x + tained.width > tainer.x + tainer.width) {
			tainer.x = tained.x - (tainer.width - tained.width);
		}
		if (tained.y + tained.height > tainer.y + tainer.height) {
			tainer.y = tained.y - (tainer.height - tained.height);
		}
	}

	/**
	 * 将目标矩形添加到原始矩形的边界。
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public static final RectBox add(RectBox source, RectBox target) {
		if (target == null) {
			return new RectBox(source);
		} else if (source == null) {
			source = new RectBox(target);
		} else {
			source.add(target);
		}
		return source;
	}

	/**
	 * 填充指定瓦片的边界。瓦片从左到右，从上到下。
	 * 
	 * @param width
	 * @param height
	 * @param tileWidth
	 * @param tileHeight
	 * @param tileIndex
	 * @return
	 */
	public static final RectBox getTile(int width, int height, int tileWidth, int tileHeight, int tileIndex) {
		return getTile(width, height, tileWidth, tileHeight, tileIndex, null);
	}

	/**
	 * 填充指定瓦片的边界。瓦片从左到右，从上到下。
	 * 
	 * @param width
	 * @param height
	 * @param tileWidth
	 * @param tileHeight
	 * @param tileIndex
	 * @param result
	 */
	public static final RectBox getTile(int width, int height, int tileWidth, int tileHeight, int tileIndex,
			RectBox result) {
		if (result == null) {
			result = new RectBox();
		}
		int tilesPerRow = width / tileWidth;
		if (tilesPerRow == 0) {
			result.setBounds(0, 0, width, height);
		} else {
			int row = tileIndex / tilesPerRow;
			int col = tileIndex % tilesPerRow;
			result.setBounds(tileWidth * col, tileHeight * row, tileWidth, tileHeight);
		}
		return result;
	}

	/**
	 * 返回指定矩形间的对应碰撞点集合
	 * 
	 * @param src
	 * @param dst
	 * @return
	 */
	public static TArray<RectBox> getNineTiles(final RectBox src, final RectBox dst) {
		TArray<RectBox> tiles = new TArray<RectBox>(9);

		// topLeft
		Vector2f tl0 = new Vector2f(dst.x, dst.y);
		Vector2f br0 = new Vector2f(src.x, src.y);

		// topCenter
		Vector2f tl1 = new Vector2f(src.x, dst.y);
		Vector2f br1 = new Vector2f(src.x + src.width, src.y);

		// topRight
		Vector2f tl2 = new Vector2f(src.x + src.width, dst.y);
		Vector2f br2 = new Vector2f(dst.x + dst.width, src.y);

		// rightCenter
		Vector2f tl3 = br1;
		Vector2f br3 = new Vector2f(dst.x + dst.width, src.y + src.height);

		// bottomRight
		Vector2f tl4 = new Vector2f(src.x + src.width, src.y + src.height);
		Vector2f br4 = new Vector2f(dst.x + dst.width, dst.y + dst.height);

		// bottomCenter
		Vector2f tl5 = new Vector2f(src.x, src.y + src.height);
		Vector2f br5 = new Vector2f(src.x + src.width, dst.y + dst.height);

		// bottomLeft
		Vector2f tl6 = new Vector2f(dst.x, src.y + src.height);
		Vector2f br6 = new Vector2f(src.x, dst.y + dst.height);

		// leftCenter
		Vector2f tl7 = new Vector2f(dst.x, src.y);
		Vector2f br7 = tl5;

		tiles.add(constructRect(tl0, br0));
		tiles.add(constructRect(tl1, br1));
		tiles.add(constructRect(tl2, br2));
		tiles.add(constructRect(tl7, br7));
		tiles.add(src);
		tiles.add(constructRect(tl3, br3));
		tiles.add(constructRect(tl6, br6));
		tiles.add(constructRect(tl5, br5));
		tiles.add(constructRect(tl4, br4));

		return tiles;
	}

	/**
	 * 获得指定线经过的点
	 * 
	 * @param line
	 * @param stepRate
	 * @return
	 */
	public static final TArray<Vector2f> getBresenhamPoints(Line line, float stepRate) {
		if (stepRate < 1f) {
			stepRate = 1f;
		}
		TArray<Vector2f> results = new TArray<Vector2f>();

		float x1 = MathUtils.round(line.getX1());
		float y1 = MathUtils.round(line.getY1());
		float x2 = MathUtils.round(line.getX2());
		float y2 = MathUtils.round(line.getY2());

		float dx = MathUtils.abs(x2 - x1);
		float dy = MathUtils.abs(y2 - y1);
		float sx = (x1 < x2) ? 1 : -1;
		float sy = (y1 < y2) ? 1 : -1;
		int err = MathUtils.ceil(dx) - MathUtils.ceil(dy);

		results.add(new Vector2f(x1, y1));

		int i = 1;

		while (!((x1 == x2) && (y1 == y2))) {
			int e2 = err << 1;

			if (e2 > -dy) {
				err -= dy;
				x1 += sx;
			}

			if (e2 < dx) {
				err += dx;
				y1 += sy;
			}

			if (i % stepRate == 0) {
				results.add(new Vector2f(x1, y1));
			}

			i++;
		}

		return results;
	}

	private static final RangeF getRangeHull(RangeF a, RangeF b) {
		return new RangeF(a.getMin() < b.getMin() ? a.getMin() : b.getMin(),
				a.getMax() > b.getMax() ? a.getMax() : b.getMax());
	}

	public static final Vector2f getRectCorner(Vector2f rectPos, Vector2f rectSize, Vector2f rectAlignement,
			int corner) {
		return getRectCorner(constructRect(rectPos, rectSize, rectAlignement), corner);
	}

	public static final Vector2f getRectCorner(RectBox rect, int corner) {
		return getRectCornersList(rect)[corner % 4];
	}

	public static Vector2f[] getRectCornersList(RectBox rect) {
		Vector2f tl = new Vector2f(rect.x, rect.y);
		Vector2f tr = new Vector2f(rect.x + rect.width, rect.y);
		Vector2f bl = new Vector2f(rect.x, rect.y + rect.height);
		Vector2f br = new Vector2f(rect.x + rect.width, rect.y + rect.height);
		return new Vector2f[] { tl, tr, br, bl };
	}
}
