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
package loon;

import loon.canvas.Canvas;
import loon.canvas.Image;
import loon.canvas.LColor;
import loon.geom.Affine2f;
import loon.opengl.BlendMethod;
import loon.opengl.GLEx;
import loon.opengl.Mesh;
import loon.opengl.MeshData;
import loon.opengl.Painter;
import loon.utils.IntMap;
import loon.utils.TArray;
import loon.utils.TimeUtils;

/**
 * 这是一个针对单独纹理的批量渲染类,默认绑定在特定Texture上运行（_meshdata.texture.geTexturetBatch即可获得）,<br>
 * 方便针对特定纹理的缓存以及渲染.
 */
public class LTextureBatch implements LRelease {

	private TArray<Cache> _tempCaches = new TArray<Cache>();

	private IntMap<Cache> _caches;

	private boolean _updateBlend = false;

	private boolean isClosed;

	public boolean isCacheLocked;

	private Cache lastCache;

	/**
	 * 纯Java环境版本使用的是Image存储缓存图像,创建太多Cache可能会耗尽内存
	 */
	public static class Cache implements LRelease {

		public float x = 0;

		public float y = 0;

		protected final Image _image;

		public Cache(LTextureBatch batch) {
			_image = batch._buffer.newSnapshot();
		}

		public Cache(Image image) {
			Image img = image;
			Canvas canvas = LSystem.base().graphics().createCanvas(img.getWidth(), img.getHeight());
			canvas.draw(image, 0, 0);
			_image = canvas.snapshot();
		}

		@Override
		public int hashCode() {
			return _image.hashCode();
		}

		public Image get() {
			return _image;
		}

		public boolean isClosed() {
			return _image == null || _image.isClosed();
		}

		@Override
		public void close() {
			if (_image != null) {
				_image.close();
			}
		}
	}

	private Affine2f _display = new Affine2f();

	private Mesh _mesh;

	private boolean isInitMesh;

	private Canvas _buffer;

	private LColor _color = new LColor();

	protected int count = 0;

	protected boolean drawing = false;

	public int maxSpritesInBatch = 0;

	protected boolean isLoaded;

	private int vertexIdx;

	private int texWidth, texHeight;

	private float tx, ty;

	private int _blendMode;

	private int _drawCallCount;

	private MeshData _meshdata;

	private String _source;

	public LTextureBatch(LTexture tex, String src) {
		this._source = src;
		this._meshdata = new MeshData();
		this.setTexture(tex);
	}

	public LTextureBatch begin() {
		if (!isLoaded) {
			isLoaded = true;
		}
		if (!_meshdata.texture.isLoaded()) {
			_meshdata.texture.loadTexture();
		}
		if (drawing) {
			throw new LSysException("TextureBatch.end must be called before begin.");
		}
		if (!isInitMesh) {
			if (_buffer == null) {
				_buffer = LSystem.base().graphics().createCanvas(LSystem.viewSize.getWidth(),
						LSystem.viewSize.getHeight());
			}
			if (_mesh == null) {
				_mesh = LSystem.base().makeMesh(_buffer);
			}
			this._mesh.setMesh(_meshdata);
			this.isInitMesh = true;
		}
		if (!isCacheLocked) {
			vertexIdx = 0;
		}
		this.drawing = true;
		this._drawCallCount = 0;
		return this;
	}

	public LTextureBatch end() {
		return end(BlendMethod.MODE_SCREEN);
	}

	public LTextureBatch end(int blend) {
		if (!isLoaded) {
			return this;
		}
		if (!drawing) {
			throw new LSysException("TextureBatch.begin must be called before end.");
		}
		if (vertexIdx > 0) {
			submit(blend, tx, ty);
		}
		this.drawing = false;
		this._drawCallCount = 0;
		return this;
	}

	public String src() {
		return _source;
	}

	public int getBlendMode() {
		return this._blendMode;
	}

	public LTextureBatch setBlendMode(int blend) {
		this._blendMode = blend;
		return this;
	}

	public LTextureBatch setLocation(float tx, float ty) {
		this.tx = tx;
		this.ty = ty;
		return this;
	}

	public LTextureBatch setTexture(LTexture tex2d) {
		this._meshdata.texture = tex2d;
		this.texWidth = _meshdata.texture.getWidth();
		this.texHeight = _meshdata.texture.getHeight();
		return this;
	}

	public int getTextureWidth() {
		return texWidth;
	}

	public int getTextureHeight() {
		return texHeight;
	}

	public LTexture toTexture() {
		return _meshdata.texture;
	}

	public LTextureBatch setColor(LColor tint) {
		_color.setColor(tint);
		return this;
	}

	public LTextureBatch setColor(float r, float g, float b, float a) {
		_color.setColor(r, g, b, a);
		return this;
	}

	public LColor getColor() {
		return _color.cpy();
	}

	private LTextureBatch checkDrawing() {
		if (!drawing) {
			throw new LSysException("Not implemented begin !");
		}
		return this;
	}

	public boolean checkTexture(final LTexture texture) {
		if (!isLoaded || isCacheLocked || isClosed || (texture == null)) {
			return false;
		}
		checkDrawing();
		if (!texture.isLoaded()) {
			texture.loadTexture();
		}
		return true;
	}

	public LTextureBatch clear() {
		_buffer.clear();
		return this;
	}

	public LTextureBatch submit(int blend) {
		return submit(blend, 0f, 0f);
	}

	public LTextureBatch submit(int blend, float x, float y) {
		if (isClosed) {
			return this;
		}
		GLEx gl = LSystem.base().display().GL();
		if (gl != null) {
			final int curBlend = gl.getBlendMode();
			_updateBlend = (BlendMethod.MODE_NORMAL != blend);
			if (_updateBlend) {
				gl.setBlendMode(blend);
			}
			Canvas canvas = gl.getCanvas();
			canvas.setTransform(gl.tx());
			canvas.draw(_buffer.getImage(), x, y);
			if (_updateBlend) {
				gl.setBlendMode(curBlend);
			}
			_drawCallCount++;
			GraphicsDrawCall.add(_drawCallCount);
		}
		return this;
	}

	public boolean isUpdateBlend() {
		return this._updateBlend;
	}

	public LTextureBatch commit(final float x, final float y) {
		return commit(x, y, 0f);
	}

	public LTextureBatch commit(float x, float y, float rotation) {
		return commit(x, y, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch commit(float x, float y, float rotation, boolean flipX, boolean flipY, boolean flipZ) {
		return commit(x, y, 1f, 1f, 0f, 0f, rotation, flipX, flipY, flipZ);
	}

	public LTextureBatch commit(float x, float y, float sx, float sy, float ax, float ay, float rotation, boolean flipX,
			boolean flipY, boolean flipZ) {
		if (isClosed) {
			return this;
		}
		GLEx gl = LSystem.base().display().GL();

		if (gl != null) {

			final Image image = _buffer.snapshot();
			final int width = image.getWidth();
			final int height = image.getHeight();

			final boolean rotDirty = (rotation != 0);
			final boolean scaleDirty = !(sx == 1 && sy == 1);

			Affine2f display = gl.tx();

			if (rotDirty || scaleDirty) {
				updateTransform(display, x, y, width, height, rotation, sx, sy, ax, ay, -1f, -1f, rotDirty, scaleDirty,
						flipX, flipY);
				Affine2f.multiply(gl.tx(), display, display);
			}

			Canvas canvas = gl.getCanvas();
			canvas.setTransform(display);
			canvas.draw(_buffer.snapshot(), x, y);
			GraphicsDrawCall.add(1);
		}
		return this;
	}

	public void clearDrawCallCount() {
		this._drawCallCount = 0;
	}

	public int getDrawCallCount() {
		return _drawCallCount;
	}

	public boolean isDrawing() {
		return drawing;
	}

	public LTextureBatch lock() {
		this.isCacheLocked = true;
		return this;
	}

	public LTextureBatch unLock() {
		this.isCacheLocked = false;
		return this;
	}

	public boolean postLastCache() {
		if (lastCache != null) {
			postCache(lastCache, _color, 0f);
			return true;
		}
		return false;
	}

	public Cache getLastCache() {
		return lastCache;
	}

	public boolean existCache() {
		return lastCache != null && !lastCache.isClosed();
	}

	public Cache newCache() {
		return newCache(true);
	}

	public Cache newCache(boolean cached) {
		if (isLoaded) {
			lastCache = new Cache(this);
			if (cached) {
				_tempCaches.add(lastCache);
			}
			return lastCache;
		} else {
			return null;
		}
	}

	public boolean disposeLastCache() {
		if (lastCache != null) {
			lastCache.close();
			lastCache = null;
			return true;
		}
		return false;
	}

	public LTextureBatch draw(float x, float y) {
		return draw(x, y, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height) {
		return draw(x, y, width, height, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight) {
		return draw(x, y, width, height, srcX, srcY, srcWidth, srcHeight, _color);
	}

	public LTextureBatch draw(float x, float y, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _meshdata.texture.width() / 2, _meshdata.texture.height() / 2,
				_meshdata.texture.width(), _meshdata.texture.height(), 1f, 1f, 0f, 0, 0, _meshdata.texture.width(),
				_meshdata.texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, 0, 0, _meshdata.texture.width(),
				_meshdata.texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float x1, float y1, float x2, float y2,
			float rotation, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, rotation, x1, y1, x2, y2, false,
				false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float x1, float y1, float x2, float y2,
			LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, x1, y1, x2, y2, false, false,
				update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float rotation, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _meshdata.texture.width() / 2, _meshdata.texture.height() / 2,
				_meshdata.texture.width(), _meshdata.texture.height(), 1f, 1f, rotation, 0, 0,
				_meshdata.texture.width(), _meshdata.texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float rotation, LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, rotation, 0, 0,
				_meshdata.texture.width(), _meshdata.texture.height(), false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float srcX, float srcY, float srcWidth, float srcHeight, float rotation,
			LColor color) {
		final boolean update = checkUpdateColor(color);
		return draw(x, y, -1f, -1f, _meshdata.texture.width() / 2, _meshdata.texture.height() / 2,
				_meshdata.texture.width(), _meshdata.texture.height(), 1f, 1f, rotation, srcX, srcY, srcWidth,
				srcHeight, false, false, update ? color : null);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, float rotation) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, rotation, srcX, srcY, srcWidth,
				srcHeight, false, false, _color);
	}

	public LTextureBatch draw(float x, float y, float originX, float originY, float width, float height, float scaleX,
			float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight, boolean flipX,
			boolean flipY) {
		return draw(x, y, -1f, -1f, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth,
				srcHeight, flipX, flipY, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY, _color);
	}

	public LTextureBatch draw(float x, float y, float width, float height, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY, LColor color) {
		return draw(x, y, -1f, -1f, width / 2, height / 2, width, height, 1f, 1f, 0f, srcX, srcY, srcWidth, srcHeight,
				flipX, flipY, color);
	}

	public LTextureBatch draw(float x, float y, float pivotX, float pivotY, float originX, float originY, float width,
			float height, float scaleX, float scaleY, float rotation, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY) {
		return draw(x, y, pivotX, pivotY, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY,
				srcWidth, srcHeight, flipX, flipY, _color);
	}

	public LTextureBatch draw(float x, float y, float pivotX, float pivotY, float originX, float originY, float width,
			float height, float scaleX, float scaleY, float rotation, float srcX, float srcY, float srcWidth,
			float srcHeight, boolean flipX, boolean flipY, LColor color) {

		if (!checkTexture(_meshdata.texture)) {
			return this;
		}

		boolean rotDirty = (rotation != 0 || (pivotX != -1 && pivotY != -1));

		boolean scaleDirty = !(scaleX == 1 && scaleY == 1);

		_display.idt();

		if (flipX || flipY || rotDirty || scaleDirty) {
			updateTransform(_display, x, y, width, height, rotation, scaleX, scaleY, originX, originY, pivotX, pivotY,
					rotDirty, scaleDirty, flipX, flipY);
		}

		int argb = this._color.getABGR();
		if (color != null && this._color != color) {
			argb = LColor.combine(argb, color.getARGB());
		}

		final LTexture texture = _meshdata.texture;

		final boolean childTexture = (texture.isScale() || texture.isCopy());
		if (childTexture && srcX == 0 && srcY == 0 && srcWidth == width && srcHeight == height) {
			if (!childTexture) {
				_mesh.paint(argb, _display, x, y, x + width, y + height, 0f, 0f, 1f, 1f);
			} else {
				_mesh.paint(argb, _display, x, y, x + width, y + height, texture.xOff(), texture.yOff(),
						texture.widthRatio(), texture.heightRatio());
			}
		} else {
			float displayWidth = texture.getDisplayWidth();
			float displayHeight = texture.getDisplayHeight();

			float xOff = 0f;
			float yOff = 0f;
			float widthRatio = 1f;
			float heightRatio = 1f;

			if (!childTexture) {
				xOff = ((srcX / displayWidth) * texture.widthRatio()) + texture.xOff();
				yOff = ((srcY / displayHeight) * texture.heightRatio()) + texture.yOff();
				widthRatio = ((srcWidth / displayWidth) * texture.widthRatio());
				heightRatio = ((srcHeight / displayHeight) * texture.heightRatio());
			} else {
				LTexture forefather = Painter.firstFather(texture);
				displayWidth = forefather.getDisplayWidth();
				displayHeight = forefather.getDisplayHeight();
				xOff = ((srcX / displayWidth) * forefather.widthRatio()) + forefather.xOff() + texture.xOff();
				yOff = ((srcY / displayHeight) * forefather.heightRatio()) + forefather.yOff() + texture.yOff();
				widthRatio = ((srcWidth / displayWidth) * forefather.widthRatio());
				heightRatio = ((srcHeight / displayHeight) * forefather.heightRatio());
			}
			_mesh.paint(argb, _display, x, y, x + width, y + height, xOff, yOff, widthRatio, heightRatio);
		}

		vertexIdx += 9;
		return this;
	}

	public LTextureBatch setImageColor(LColor c) {
		if (c == null) {
			return this;
		}
		_color.setColor(c);
		return this;
	}

	private boolean checkUpdateColor(LColor c) {
		return c != null && !LColor.white.equals(c);
	}

	public LTextureBatch postCache(LColor color, float rotation) {
		if (lastCache != null) {
			postCache(lastCache, color, rotation);
		}
		return this;
	}

	public LTextureBatch postCache(float x, float y) {
		if (lastCache != null) {
			return postCache(lastCache, _color, x, y, 1f, 1f, 0f, 0f, 0f, false, false, false);
		}
		return this;
	}

	public LTextureBatch postCache(LColor color, float x, float y, float rotation) {
		if (lastCache != null) {
			return postCache(lastCache, color, x, y, 1f, 1f, 0f, 0f, rotation, false, false, false);
		}
		return this;
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y) {
		return postCache(cache, color, x, y, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float rotation) {
		return postCache(cache, color, tx, ty, 1f, 1f, 0f, 0f, 0f, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y, float sx, float sy, float ax, float ay,
			float rotation) {
		return postCache(cache, color, x, y, sx, sy, ax, ay, rotation, false, false, false);
	}

	public LTextureBatch postCache(Cache cache, LColor color, float x, float y, float sx, float sy, float ax, float ay,
			float rotation, boolean flipX, boolean flipY, boolean flipZ) {

		GLEx gl = LSystem.base().display().GL();

		if (gl != null) {

			final int width = cache._image.getWidth();
			final int height = cache._image.getHeight();

			final boolean rotDirty = (rotation != 0);
			final boolean scaleDirty = !(sx == 1 && sy == 1);

			Affine2f display = gl.tx();

			if (rotDirty || scaleDirty) {
				updateTransform(display, x, y, width, height, rotation, sx, sy, ax, ay, -1f, -1f, rotDirty, scaleDirty,
						flipX, flipY);
				Affine2f.multiply(gl.tx(), display, display);
			}

			Canvas canvas = gl.getCanvas();
			canvas.setTransform(display);
			canvas.draw(cache._image, x, y);
			GraphicsDrawCall.add(1);
		}
		return this;
	}

	public int saveCache() {
		return saveCache((int) (TimeUtils.millis() + (_caches == null ? 1 : _caches.size)));
	}

	public int saveCache(int hashCodeValue) {
		if (_caches == null) {
			_caches = new IntMap<>();
		}
		Cache cache = newCache(false);
		if (cache != null) {
			_caches.put(hashCodeValue, cache);
		}
		return hashCodeValue;
	}

	public Cache restoreCachePost(int hashCodeValue) {
		return restoreCachePost(hashCodeValue, _color == null ? LColor.white : _color, 0f, 0f);
	}

	public Cache restoreCachePost(int hashCodeValue, LColor color, float x, float y) {
		Cache cache = restoreCache(hashCodeValue);
		if (cache != null) {
			postCache(cache, color, x, y);
		}
		return cache;
	}

	public Cache restoreCache(int hashCodeValue) {
		if (_caches != null) {
			return _caches.get(hashCodeValue);
		}
		return null;
	}

	protected void updateTransform(Affine2f display, float x, float y, float width, float height, float rotation,
			float scaleX, float scaleY, float originX, float originY, float pivotX, float pivotY, boolean rotDirty,
			boolean scaleDirty, boolean flipX, boolean flipY) {

		if (originX == 0 && originY == 0) {
			originX = originX == 0 ? width / 2 : originX;
			originY = originY == 0 ? height / 2 : originY;
		}

		float centerX = x + originX;
		float centerY = y + originY;

		if (rotDirty) {
			if (pivotX != -1 && pivotY != -1) {
				centerX = x + pivotX;
				centerX = y + pivotY;
			}
			display.translate(centerX, centerY);
			display.preRotate(rotation);
			display.translate(-centerX, -centerY);
		}
		if (scaleDirty) {
			if (pivotX != -1 && pivotY != -1) {
				centerX = x + pivotX;
				centerX = y + pivotY;
			}
			display.translate(centerX, centerY);
			display.preScale(scaleX, scaleY);
			display.translate(-centerX, -centerY);
		}

		if (flipX || flipY) {
			if (flipX && flipY) {
				Affine2f.transformOrigin(display, x, y, LTrans.TRANS_ROT180, originX, originY);
			} else if (flipX) {
				Affine2f.transformOrigin(display, x, y, LTrans.TRANS_MIRROR, originX, originY);
			} else if (flipY) {
				Affine2f.transformOrigin(display, x, y, LTrans.TRANS_MIRROR_ROT180, originX, originY);
			}
		}
	}

	public int getTextureID() {
		if (_meshdata.texture != null) {
			return _meshdata.texture.getID();
		}
		return -1;
	}

	public int getTextureHashCode() {
		if (_meshdata.texture != null) {
			return _meshdata.texture.hashCode();
		}
		return -1;
	}

	public boolean closed() {
		return isClosed;
	}

	public boolean isClosed() {
		return closed();
	}

	@Override
	public void close() {
		isClosed = true;
		isLoaded = false;
		isCacheLocked = false;
		isInitMesh = false;
		if (lastCache != null) {
			lastCache.close();
		}
		if (_caches != null) {
			for (Cache cache : _caches) {
				if (cache != null) {
					cache.close();
				}
			}
			_caches.clear();
		}
		if (_tempCaches != null) {
			for (Cache cache : _tempCaches) {
				if (cache != null) {
					cache.close();
				}
			}
			_tempCaches.clear();
		}
		LSystem.disposeBatchCache(this, false);
	}

	public LTextureBatch destroy() {
		if (_meshdata.texture != null) {
			_meshdata.texture.close(true);
		}
		if (_buffer != null) {
			_buffer.close();
		}
		return this;
	}

}
