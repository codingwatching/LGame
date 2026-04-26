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
package loon.action.sprite;

import loon.LSystem;
import loon.LTexture;
import loon.canvas.LColor;
import loon.canvas.Pixmap;
import loon.geom.Vector2f;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.TArray;
import loon.utils.timer.Duration;

/**
 * 移动路径精灵渲染用类(纯像素绘制版，特点是省事通用性强，不要美术，对画质要求不高的路径指向拿过去就用。而另一个Arrow是纹理图像版的，必须设置完整路线图才能用)
 */
public class ArrowPath extends Entity {

	// 移动路线渲染模式
	public static enum Mode {
		// 四方向箭头
		FOUR,
		// 八方向箭头
		EIGHT,
		// 自动适配箭头方向
		FREE,
		// 完整渲染移动路径(战棋染移动格子模式)
		PATH
	}

	private TArray<Vector2f> _pathTiles;
	private Mode _mode;

	// 路径样式
	private LColor _colorStart;
	private LColor _colorEnd;
	private float _thickness;
	private boolean _dashed;
	private float _dashLength;
	private float _gapLength;
	private float _dashPhase;
	private float _dashSpeed;

	// 箭头配置
	private float _arrowWidth;
	private float _arrowHeight;
	private boolean _arrowVisible;
	private LColor _arrowColor;
	private LTexture _arrowTexture;
	private Pixmap _arrowPixmap;

	// 瓦片网格
	private float _gridWidth;
	private float _gridHeight;
	private boolean _centerAlign;

	// 发光效果
	private boolean _glowEnabled;
	private LColor _glowColor;
	private float _glowIntensity;

	// 渲染扩展
	private TArray<LColor> _segmentColors;
	private LTexture _gradientTex;
	private int _gradientHeight;
	private LTexture _glowTex;
	private int _glowHeight;
	private LColor _outlineColor;
	private float _outlineThickness;
	private float _globalAlpha;

	// 缓存与脏绘标志
	private final TArray<Vector2f> _drawPath = new TArray<Vector2f>();
	private boolean _pathDirty = true;
	private boolean _textureDirty = true;
	// 箭头起始像素位置
	private Vector2f _startPixel;
	// 目标指向像素位置
	private Vector2f _targetPixel;

	public ArrowPath(float tileSize) {
		this(tileSize, tileSize);
	}

	public ArrowPath(float tileW, float tileH, Mode mode) {
		this(tileW, tileH);
		this._mode = mode;
	}

	public ArrowPath(float tileSize, Mode mode) {
		this(tileSize, tileSize, mode);
	}

	public ArrowPath(float tileW, float tileH, LColor solidPathColor) {
		this(tileW, tileH);
		setSolidColor(solidPathColor);
	}

	public ArrowPath(float tileW, float tileH, LColor solidPathColor, LColor arrowColor) {
		this(tileW, tileH, solidPathColor);
		setArrowColor(arrowColor);
	}

	public ArrowPath(float tileW, float tileH, Mode mode, LColor solidPathColor, LColor arrowColor) {
		this(tileW, tileH, solidPathColor, arrowColor);
		this._mode = mode;
	}

	public ArrowPath(float tileW, float tileH, Mode mode, LColor pathColor, LColor arrowColor, boolean centerAlign,
			boolean glowEnabled) {
		this(tileW, tileH, mode, pathColor, arrowColor);
		this._centerAlign = centerAlign;
		this._glowEnabled = glowEnabled;
	}

	public ArrowPath() {
		this(LSystem.LAYER_TILE_SIZE);
	}

	public ArrowPath(float tileW, float tileH) {
		_pathTiles = new TArray<Vector2f>();
		_mode = Mode.FREE;
		_arrowVisible = true;
		_centerAlign = true;
		_colorStart = LColor.darkRed;
		_colorEnd = LColor.red;
		_gridWidth = tileW;
		_gridHeight = tileH;
		_dashPhase = 0f;
		_dashSpeed = 60f;
		_dashed = false;
		_glowEnabled = false;
		_glowColor = LColor.lightGray;
		_glowIntensity = 0.6f;
		_segmentColors = null;
		_gradientHeight = 64;
		_glowHeight = 24;
		_outlineColor = new LColor(0, 0, 0, 0.45f);
		_outlineThickness = 1f;
		_globalAlpha = 1f;
		_startPixel = new Vector2f();
		_targetPixel = new Vector2f();
		autoAdaptToGrid();
		setRepaint(true);
	}

	/**
	 * 自适应网格
	 */
	private void autoAdaptToGrid() {
		float baseSize = MathUtils.min(_gridWidth, _gridHeight);
		_thickness = MathUtils.clamp(baseSize * 0.15f, 2f, 12f);
		_arrowWidth = MathUtils.clamp(baseSize * 0.5f, 8f, baseSize * 1.5f);
		_arrowHeight = MathUtils.clamp(baseSize * 0.5f, 8f, baseSize * 1.5f);
		_dashLength = MathUtils.clamp(baseSize * 0.2f, 4f, 16f);
		_gapLength = MathUtils.clamp(baseSize * 0.1f, 2f, 8f);
		_textureDirty = true;
	}

	private void rebuildArrowTexture() {
		if (!_textureDirty || _arrowTexture != null) {
			return;
		}
		freeArrowResources();
		int size = MathUtils.ceil(MathUtils.max(_arrowWidth, _arrowHeight));
		_arrowPixmap = new Pixmap(size, size);
		float centerX = size / 2f;
		float tipY = 0;
		float baseY = _arrowHeight;
		float halfW = _arrowWidth / 2f;
		boolean useSolidColor = _arrowColor != null;
		boolean usePathGradient = !useSolidColor && !_colorStart.equals(_colorEnd);
		for (float y = tipY; y <= baseY; y++) {
			int argb;
			if (useSolidColor) {
				argb = ((int) (_arrowColor.a * 255 * _globalAlpha) << 24) | ((int) (_arrowColor.r * 255) << 16)
						| ((int) (_arrowColor.g * 255) << 8) | (int) (_arrowColor.b * 255);
			} else if (usePathGradient) {
				float t = y / baseY;
				float r = _colorStart.r * (1 - t) + _colorEnd.r * t;
				float g = _colorStart.g * (1 - t) + _colorEnd.g * t;
				float b = _colorStart.b * (1 - t) + _colorEnd.b * t;
				float a = (_colorStart.a * (1 - t) + _colorEnd.a * t) * _globalAlpha;
				argb = ((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
			} else {
				argb = ((int) (_colorStart.a * 255 * _globalAlpha) << 24) | ((int) (_colorStart.r * 255) << 16)
						| ((int) (_colorStart.g * 255) << 8) | (int) (_colorStart.b * 255);
			}
			float lineWidth = halfW * (1 - y / baseY);
			int x1 = MathUtils.floor(centerX - lineWidth);
			int x2 = MathUtils.ceil(centerX + lineWidth);
			int py = MathUtils.floor(y);
			for (int x = x1; x <= x2; x++) {
				_arrowPixmap.putPixel(x, py, argb);
			}
		}
		_arrowTexture = _arrowPixmap.toTexture();
		_arrowPixmap.close();
		_arrowPixmap = null;
	}

	@Override
	protected void repaint(GLEx g, float offsetX, float offsetY) {
		if (_pathTiles == null || _pathTiles.size < 2) {
			return;
		}
		if (_pathDirty) {
			_drawPath.clear();
			_drawPath.addAll(_pathTiles);
			if (_drawPath.size >= 2) {
				Vector2f last = _drawPath.get(_drawPath.size - 1);
				Vector2f prev = _drawPath.get(_drawPath.size - 2);
				float dx = last.x - prev.x;
				float dy = last.y - prev.y;
				float len = MathUtils.sqrt(dx * dx + dy * dy);
				if (len > _arrowWidth) {
					float nx = dx / len;
					float ny = dy / len;
					last.x = prev.x + nx * (len - _arrowWidth);
					last.y = prev.y + ny * (len - _arrowWidth);
				}
			}
			_pathDirty = false;
		}
		if (_textureDirty) {
			rebuildArrowTexture();
			rebuildGradient();
			rebuildGlow();
			_textureDirty = false;
		}
		if (_arrowTexture == null) {
			return;
		}
		final float drawX = offsetX;
		final float drawY = offsetY;
		if (_glowEnabled && _glowTex != null) {
			drawGlowLines(g, drawX, drawY);
		}
		drawPathOutline(g, drawX, drawY);
		drawGradientLines(g, drawX, drawY);
		if (_arrowVisible) {
			drawArrow(g, drawX, drawY);
		}
	}

	@Override
	protected void onProcess(long elapsedTime) {
		update(Duration.toS(elapsedTime));
	}

	public void update(float delta) {
		if (!_dashed) {
			return;
		}
		_dashPhase += delta * _dashSpeed;
		float pattern = _dashLength + _gapLength;
		if (pattern > 0 && _dashPhase > pattern) {
			_dashPhase -= pattern;
		}
	}

	private void drawGlowLines(GLEx g, float offsetX, float offsetY) {
		float glowScale = 1f + _glowIntensity * 2f;
		float drawThickness = MathUtils.max(1f, _thickness * glowScale);
		for (int i = 0; i < _drawPath.size - 1; i++) {
			Vector2f a = _drawPath.get(i);
			Vector2f b = _drawPath.get(i + 1);
			float dx = b.x - a.x;
			float dy = b.y - a.y;
			float len = MathUtils.sqrt(dx * dx + dy * dy);
			if (len <= 0.001f) {
				continue;
			}
			float angle = MathUtils.toDegrees(MathUtils.atan2(dy, dx));
			if (_dashed) {
				float offset = -_dashPhase;
				while (offset < len) {
					float s = MathUtils.max(0, offset);
					float e = MathUtils.min(len, offset + _dashLength);
					if (e > s) {
						float x = a.x + dx * (s + e) * 0.5f / len + offsetX;
						float y = a.y + dy * (s + e) * 0.5f / len + offsetY;
						drawTextured(g, _glowTex, x, y, e - s, drawThickness, angle, _glowColor);
					}
					offset += _dashLength + _gapLength;
				}
			} else {
				float x = (a.x + b.x) * 0.5f + offsetX;
				float y = (a.y + b.y) * 0.5f + offsetY;
				drawTextured(g, _glowTex, x, y, len, drawThickness, angle, _glowColor);
			}
		}
	}

	private void drawGradientLines(GLEx g, float offsetX, float offsetY) {
		if (_drawPath.size < 2) {
			return;
		}
		float safeThickness = MathUtils.max(1f, _thickness);
		for (int i = 0; i < _drawPath.size - 1; i++) {
			Vector2f a = _drawPath.get(i);
			Vector2f b = _drawPath.get(i + 1);
			float dx = b.x - a.x;
			float dy = b.y - a.y;
			float len = MathUtils.sqrt(dx * dx + dy * dy);
			if (len <= 0.001f) {
				continue;
			}
			float angle = MathUtils.toDegrees(MathUtils.atan2(dy, dx));
			LColor tint = new LColor(_colorStart);
			if (_segmentColors != null && !_segmentColors.isEmpty()) {
				LColor c = _segmentColors.get(MathUtils.min(i, _segmentColors.size - 1));
				tint = new LColor(c.r, c.g, c.b, c.a * _globalAlpha);
			} else {
				tint.a *= _globalAlpha;
			}
			if (_dashed) {
				float offset = -_dashPhase;
				while (offset < len) {
					float s = MathUtils.max(0, offset);
					float e = MathUtils.min(len, offset + _dashLength);
					if (e > s) {
						float x = a.x + dx * (s + e) * 0.5f / len + offsetX;
						float y = a.y + dy * (s + e) * 0.5f / len + offsetY;
						drawTextured(g, _gradientTex, x, y, e - s, safeThickness, angle, tint);
					}
					offset += _dashLength + _gapLength;
				}
			} else {
				float x = (a.x + b.x) * 0.5f + offsetX;
				float y = (a.y + b.y) * 0.5f + offsetY;
				drawTextured(g, _gradientTex, x, y, len, safeThickness, angle, tint);
			}
		}
	}

	private void drawTextured(GLEx g, LTexture tex, float x, float y, float w, float h, float angle, LColor tint) {
		if (tex == null || tint == null) {
			return;
		}
		h = MathUtils.max(1f, h);
		float drawX = x - w * 0.5f;
		float drawY = y - h * 0.5f;
		g.draw(tex, drawX, drawY, w, h, tint, angle);
	}

	private void drawArrow(GLEx g, float offsetX, float offsetY) {
		Vector2f end = _pathTiles.get(_pathTiles.size() - 1);
		Vector2f prev = _pathTiles.get(_pathTiles.size() - 2);
		float dx = end.x - prev.x;
		float dy = end.y - prev.y;
		float len = MathUtils.sqrt(dx * dx + dy * dy);
		if (len <= 0.001f) {
			return;
		}
		float angle = MathUtils.toDegrees(MathUtils.atan2(dy, dx)) - 90f;
		float renderX = end.x + offsetX - _arrowWidth * 0.5f;
		float renderY = end.y + offsetY - _arrowHeight * 0.5f;
		LColor renderColor = _arrowColor != null ? _arrowColor : LColor.white;
		renderColor.a *= _globalAlpha;
		g.draw(_arrowTexture, renderX, renderY, _arrowWidth, _arrowHeight, renderColor, angle);
	}

	private void drawPathOutline(GLEx g, float offsetX, float offsetY) {
		if (_drawPath.size < 2 || _outlineThickness <= 0) {
			return;
		}
		for (int i = 0; i < _drawPath.size - 1; i++) {
			Vector2f a = _drawPath.get(i);
			Vector2f b = _drawPath.get(i + 1);
			g.drawLine(a.x + offsetX, a.y + offsetY, b.x + offsetX, b.y + offsetY, _outlineColor);
		}
	}

	public void rebuildGradient() {
		if (!_textureDirty) {
			return;
		}
		if (_gradientTex != null) {
			_gradientTex.close();
			_gradientTex = null;
		}
		if (_gradientHeight < 4) {
			_gradientHeight = 4;
		}
		Pixmap pm = new Pixmap(1, _gradientHeight);
		for (int y = 0; y < _gradientHeight; y++) {
			float t = (float) y / (_gradientHeight - 1);
			float r = _colorStart.r * (1 - t) + _colorEnd.r * t;
			float g = _colorStart.g * (1 - t) + _colorEnd.g * t;
			float b = _colorStart.b * (1 - t) + _colorEnd.b * t;
			float a = (_colorStart.a * (1 - t) + _colorEnd.a * t) * _globalAlpha;
			pm.putPixel(0, y,
					((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255));
		}
		_gradientTex = pm.toTexture();
		pm.close();
		pm = null;
	}

	public void rebuildGlow() {
		if (!_textureDirty || !_glowEnabled) {
			return;
		}
		if (_glowTex != null) {
			_glowTex.close();
			_glowTex = null;
		}
		if (_glowHeight < 4) {
			_glowHeight = 4;
		}
		Pixmap pm = new Pixmap(1, _glowHeight);
		for (int y = 0; y < _glowHeight; y++) {
			float t = (float) y / (_glowHeight - 1);
			float a = _glowColor.a * (1 - t) * _glowIntensity * _globalAlpha;
			pm.putPixel(0, y, ((int) (a * 255) << 24) | ((int) (_glowColor.r * 255) << 16)
					| ((int) (_glowColor.g * 255) << 8) | (int) (_glowColor.b * 255));
		}
		_glowTex = pm.toTexture();
		pm.close();
		pm = null;
	}

	private Vector2f tileToPixel(float x, float y) {
		float px = x * _gridWidth;
		float py = y * _gridHeight;
		if (_centerAlign) {
			px += _gridWidth / 2f;
			py += _gridHeight / 2f;
		}
		return new Vector2f(px, py);
	}

	public Vector2f pixelToTile(float x, float y) {
		float tx = x / _gridWidth;
		float ty = y / _gridHeight;
		return _centerAlign ? new Vector2f(MathUtils.round(tx), MathUtils.round(ty))
				: new Vector2f(MathUtils.floor(tx), MathUtils.floor(ty));
	}

	public void setPathFromTiles(TArray<Vector2f> tiles) {
		if (tiles == null || tiles.size < 2) {
			_pathTiles = new TArray<Vector2f>();
			_pathDirty = true;
			return;
		}
		TArray<Vector2f> pixelPath = new TArray<Vector2f>();
		for (Vector2f t : tiles) {
			pixelPath.add(tileToPixel(t.x, t.y));
		}
		_pathTiles = pixelPath;
		_pathDirty = true;
	}

	public void setPathFromTiles(TArray<Vector2f> tiles, Mode mode) {
		setPathFromTiles(tiles);
		setPathQuantized(_pathTiles, mode, _centerAlign);
	}

	public void setPathQuantized(TArray<Vector2f> input, Mode mode, boolean align) {
		_mode = mode;
		if (mode == Mode.PATH) {
			_pathTiles = new TArray<Vector2f>(input);
			_pathDirty = true;
			return;
		}
		TArray<Vector2f> snapped = new TArray<Vector2f>();
		for (Vector2f p : input) {
			float gx = p.x / _gridWidth;
			float gy = p.y / _gridHeight;
			float sx = align ? MathUtils.round(gx) : MathUtils.floor(gx);
			float sy = align ? MathUtils.round(gy) : MathUtils.floor(gy);
			snapped.add(new Vector2f(sx * _gridWidth, sy * _gridHeight));
		}
		TArray<Vector2f> out = new TArray<Vector2f>();
		out.add(snapped.get(0));
		for (int i = 1; i < snapped.size(); i++) {
			Vector2f p = snapped.get(i);
			Vector2f pre = snapped.get(i - 1);
			float dx = p.x - pre.x;
			float dy = p.y - pre.y;
			float len = MathUtils.sqrt(dx * dx + dy * dy);
			if (len < 0.0001f) {
				continue;
			}
			int sectors = mode == Mode.FOUR ? 4 : 8;
			float ang = MathUtils.toDegrees(MathUtils.atan2(dy, dx));
			float snapAng = MathUtils.round(ang / (360 / sectors)) * (360 / sectors);
			float rad = MathUtils.toRadians(snapAng);
			out.add(new Vector2f(pre.x + MathUtils.cos(rad) * len, pre.y + MathUtils.sin(rad) * len));
		}
		_pathTiles = out;
		_pathDirty = true;
	}

	public void setFreeLine(Vector2f start, Vector2f dir, float len) {
		_pathTiles.clear();
		Vector2f d = new Vector2f(dir).norSelf();
		_pathTiles.addAll(start, new Vector2f(start.x + d.x * len, start.y + d.y * len));
		_mode = Mode.FREE;
		_pathDirty = true;
	}

	public void setGridSize(float size) {
		setGridSize(size, true);
	}

	public void setGridSize(float size, boolean autoFix) {
		setGridSize(size, size, autoFix);
	}

	public void setGridSize(float w, float h, boolean autoFix) {
		_gridWidth = MathUtils.max(1f, w);
		_gridHeight = MathUtils.max(1f, h);
		if (autoFix) {
			autoAdaptToGrid();
		}
		_pathDirty = true;
	}

	public void clearPath() {
		_pathTiles.clear();
		_drawPath.clear();
		_pathDirty = true;
	}

	public void addPathPoint(Vector2f tilePoint) {
		if (tilePoint == null) {
			return;
		}
		TArray<Vector2f> temp = new TArray<Vector2f>();
		temp.addAll(_pathTiles);
		temp.add(tilePoint);
		setPathFromTiles(temp);
	}

	public void showArrow() {
		_arrowVisible = true;
	}

	public void hideArrow() {
		_arrowVisible = false;
	}

	public void setSolidColor(LColor color) {
		_colorStart = new LColor(color);
		_colorEnd = new LColor(color);
		_textureDirty = true;
	}

	public void setGlobalAlpha(float alpha) {
		_globalAlpha = MathUtils.clamp(alpha, 0f, 1f);
		_textureDirty = true;
	}

	public void setOutlineColor(LColor color) {
		_outlineColor = new LColor(color);
	}

	public void setOutlineThickness(float thickness) {
		_outlineThickness = MathUtils.max(0f, thickness);
	}

	public void setMode(Mode m) {
		_mode = m;
		_pathDirty = true;
		if (m == Mode.PATH) {
			_dashed = false;
		}
	}

	public void setArrowColor(LColor color) {
		_arrowColor = color != null ? new LColor(color) : null;
		_textureDirty = true;
	}

	public void clearArrowColor() {
		setArrowColor(null);
	}

	public void setArrowWidth(float w) {
		_arrowWidth = MathUtils.max(2f, w);
		_textureDirty = true;
	}

	public void setArrowHeight(float h) {
		_arrowHeight = MathUtils.max(1f, h);
		_textureDirty = true;
	}

	public void setCenterAlign(boolean b) {
		if (_centerAlign != b) {
			_centerAlign = b;
			_pathDirty = true;
		}
	}

	public void setDashSpeed(float s) {
		_dashSpeed = s;
	}

	public void setGlow(boolean enable, LColor color, float intensity) {
		_glowEnabled = enable;
		_glowColor = new LColor(color);
		_glowIntensity = MathUtils.clamp(intensity, 0, 1);
		_textureDirty = true;
	}

	public void setSegmentColors(TArray<LColor> colors) {
		_segmentColors = colors;
	}

	public void setDashed(boolean dashed, float dashLen, float gapLen) {
		if (_mode == Mode.PATH) {
			_dashed = false;
			return;
		}
		_dashed = dashed;
		_dashLength = MathUtils.max(1f, dashLen);
		_gapLength = MathUtils.max(0f, gapLen);
	}

	public void setGradient(LColor start, LColor end) {
		_colorStart = new LColor(start);
		_colorEnd = new LColor(end);
		_textureDirty = true;
	}

	public void setThickness(float t) {
		_thickness = MathUtils.max(1f, t);
		_textureDirty = true;
	}

	public void setArrowSize(float size) {
		setArrowSize(size, size);
	}

	public void setArrowSize(float w, float h) {
		setArrowWidth(w);
		setArrowHeight(h);
	}

	public void setStartAndTargetPixel(Vector2f startPixel, Vector2f targetPixel) {
		if (startPixel == null || targetPixel == null) {
			return;
		}
		this._startPixel.set(startPixel);
		this._targetPixel.set(targetPixel);
		_pathTiles.clear();
		_pathTiles.add(new Vector2f(startPixel));
		_pathTiles.add(new Vector2f(targetPixel));
		// 因为触屏位置不特定，所以强制转化为自由方向模式
		_mode = Mode.FREE;
		_pathDirty = true;
	}

	public void setStartPixel(Vector2f startPixel) {
		if (startPixel == null) {
			return;
		}
		this._startPixel.set(startPixel);
		if (_targetPixel.x != 0 || _targetPixel.y != 0) {
			setStartAndTargetPixel(_startPixel, _targetPixel);
		}
	}

	public void setTargetPixel(Vector2f targetPixel) {
		if (targetPixel == null) {
			return;
		}
		this._targetPixel.set(targetPixel);
		if (_startPixel.x != 0 || _startPixel.y != 0) {
			setStartAndTargetPixel(_startPixel, _targetPixel);
		}
	}

	public void pathToPixelPos(Vector2f targetPixel) {
		if (targetPixel == null || _pathTiles == null || _pathTiles.isEmpty()) {
			return;
		}
		if (_pathTiles.size() >= 1) {
			_pathTiles.removeIndex(_pathTiles.size() - 1);
		}
		_pathTiles.add(new Vector2f(targetPixel));
		_mode = Mode.FREE;
		_pathDirty = true;
	}

	private void freeArrowResources() {
		if (_arrowTexture != null) {
			_arrowTexture.close();
			_arrowTexture = null;
		}
		if (_arrowPixmap != null) {
			_arrowPixmap.close();
			_arrowPixmap = null;
		}
	}

	@Override
	protected void _onDestroy() {
		super._onDestroy();
		freeArrowResources();
		if (_gradientTex != null) {
			_gradientTex.close();
			_gradientTex = null;
		}
		if (_glowTex != null) {
			_glowTex.close();
			_glowTex = null;
		}
	}
}
