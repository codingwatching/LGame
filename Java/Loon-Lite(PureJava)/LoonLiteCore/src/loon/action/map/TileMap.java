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
package loon.action.map;

import loon.LObject;
import loon.LRelease;
import loon.LSysException;
import loon.LSystem;
import loon.LTexture;
import loon.PlayerUtils;
import loon.Screen;
import loon.action.ActionBind;
import loon.action.ActionTween;
import loon.action.map.colider.TileImpl;
import loon.action.map.items.Attribute;
import loon.action.sprite.Animation;
import loon.action.sprite.ISprite;
import loon.action.sprite.MoveControl;
import loon.action.sprite.Sprites;
import loon.canvas.Image;
import loon.canvas.LColor;
import loon.events.DrawListener;
import loon.events.ResizeListener;
import loon.geom.Affine2f;
import loon.geom.PointF;
import loon.geom.PointI;
import loon.geom.RectBox;
import loon.geom.Sized;
import loon.geom.Vector2f;
import loon.opengl.GLEx;
import loon.opengl.LTexturePack;
import loon.opengl.LTexturePackClip;
import loon.utils.MathUtils;
import loon.utils.TArray;

/**
 * 一个简单(易于操作)的二维数组地图构造以及显示类.复杂地图请使用tmx包
 */
public class TileMap extends LObject<ISprite> implements Sized, ISprite {

	private LTexture _background;

	// 地图自身存储子精灵的的Sprites
	private Sprites _mapSprites;

	// 显示Map的上级Sprites
	private Sprites _screenSprites;

	private ResizeListener<TileMap> _resizeListener;

	private int firstTileX;

	private int firstTileY;

	private int lastTileX;

	private int lastTileY;

	public DrawListener<TileMap> listener;

	private LTexturePack texturePack;

	private TArray<TileImpl> arrays = new TArray<>(10);

	private TArray<Animation> animations = new TArray<>();

	private final int maxWidth, maxHeight;

	private final Field2D field2d;

	private final PointF _scrollDrag = new PointF();

	private float _fixedWidthOffset = 0f;
	private float _fixedHeightOffset = 0f;

	private int lastOffsetX, lastOffsetY;

	private ActionBind follow;

	private Vector2f offset = new Vector2f(0f, 0f);

	private boolean active, dirty;

	private boolean visible, roll;

	private boolean playAnimation;

	private LColor baseColor = LColor.white;

	private float scaleX = 1f, scaleY = 1f;

	public TileMap(String fileName, int tileWidth, int tileHeight) {
		this(fileName, tileWidth, tileHeight, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(String fileName, int tileWidth, int tileHeight, int mWidth, int mHeight) {
		this(TileMapConfig.loadAthwartArray(fileName), tileWidth, tileHeight, mWidth, mHeight);
	}

	public TileMap(String fileName, Screen screen, int tileWidth, int tileHeight, int mWidth, int mHeight) {
		this(TileMapConfig.loadAthwartArray(fileName), screen, tileWidth, tileHeight, mWidth, mHeight);
	}

	public TileMap(int[][] maps, int tileWidth, int tileHeight, int mWidth, int mHeight) {
		this(new Field2D(maps, tileWidth, tileHeight), mWidth, mHeight);
	}

	public TileMap(int[][] maps, Screen screen, int tileWidth, int tileHeight, int mWidth, int mHeight) {
		this(new Field2D(maps, tileWidth, tileHeight), screen, mWidth, mHeight);
	}

	public TileMap(int[][] maps, int tileWidth, int tileHeight) {
		this(maps, tileWidth, tileHeight, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(int[][] maps, Screen screen, int tileWidth, int tileHeight) {
		this(maps, screen, tileWidth, tileHeight, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(Field2D field2d) {
		this(field2d, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(Field2D field2d, Screen screen) {
		this(field2d, screen, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(Field2D field2d, int mWidth, int mHeight) {
		this(field2d, null, LSystem.viewSize.getWidth(), LSystem.viewSize.getHeight());
	}

	public TileMap(Field2D field2d, Screen screen, int mWidth, int mHeight) {
		this.field2d = field2d;
		if (field2d != null && mWidth == -1 && mHeight == -1) {
			this.maxWidth = field2d.getViewWidth();
			this.maxHeight = field2d.getViewHeight();
		} else {
			this.maxWidth = mWidth;
			this.maxHeight = mHeight;
		}
		if (field2d == null) {
			this.offset = new Vector2f(0, 0);
		} else {
			this.offset = field2d.getOffset();
		}
		this.texturePack = new LTexturePack();
		this.lastOffsetX = -1;
		this.lastOffsetY = -1;
		this.active = true;
		this.dirty = true;
		this.visible = true;
		this._mapSprites = new Sprites("TileMapSprites", screen == null ? LSystem.getProcess().getScreen() : screen,
				maxWidth, maxHeight);
	}

	public static TileMap loadCharsMap(String resName, int tileWidth, int tileHeight) {
		return new TileMap(TileMapConfig.loadCharsField(resName, tileWidth, tileHeight));
	}

	public TileMap setImagePackAuto(String fileName, int tileWidth, int tileHeight) {
		if (texturePack != null) {
			texturePack.close();
			texturePack = null;
		}
		texturePack = new LTexturePack(fileName, LTexturePackClip.getTextureSplit(fileName, tileWidth, tileHeight));
		texturePack.packed();
		return this;
	}

	public TileMap setImagePack(String fileName, LTexturePackClip[] clips) {
		return setImagePack(fileName, new TArray<>(clips));
	}

	public TileMap setImagePack(String fileName, TArray<LTexturePackClip> clips) {
		if (texturePack != null) {
			texturePack.close();
			texturePack = null;
		}
		this.active = false;
		this.dirty = true;
		texturePack = new LTexturePack(fileName, clips);
		texturePack.packed();
		return this;
	}

	public <T extends LRelease> TileMap setImagePack(TileAllocation<T> allocation) {
		if (allocation == null) {
			return this;
		}
		return setImagePack(allocation.getPath(), allocation.getClips());
	}

	public TileMap setImagePack(String file) {
		if (texturePack != null) {
			texturePack.close();
			texturePack = null;
		}
		this.active = false;
		this.dirty = true;
		texturePack = new LTexturePack(file);
		texturePack.packed();
		return this;
	}

	public TileMap removeTile(int id) {
		for (TileImpl tile : arrays) {
			if (tile.getId() == id) {
				if (tile.isAnimation()) {
					animations.remove(tile.getAnimation());
				}
				arrays.remove(tile);
			}
		}
		if (animations.size == 0) {
			playAnimation = false;
		}
		this.dirty = true;
		return this;
	}

	public int putAnimationTile(int id, Animation animation, Attribute attribute) {
		if (active) {
			TileImpl tile = new TileImpl(id);
			tile.setImgId(-1);
			tile.setAttribute(attribute);
			if (animation != null && animation.getTotalFrames() > 0) {
				tile.setAnimation(animation);
				playAnimation = true;
			}
			animations.add(animation);
			arrays.add(tile);
			dirty = true;
			return tile.getImgId();
		} else {
			throw new LSysException("Map is no longer active, you can not add new tiles !");
		}
	}

	public int putAnimationTile(int id, String res, int w, int h, int timer) {
		return putAnimationTile(id, Animation.getDefaultAnimation(res, w, h, timer), null);
	}

	public int putAnimationTile(int id, Animation animation) {
		return putAnimationTile(id, animation, null);
	}

	public int putTile(int id, Image img, Attribute attribute) {
		if (active) {
			TileImpl tile = new TileImpl(id);
			tile.setImgId(texturePack.putImage(img));
			tile.setAttribute(attribute);
			arrays.add(tile);
			dirty = true;
			return tile.getImgId();
		} else {
			throw new LSysException("Map is no longer active, you can not add new tiles !");
		}
	}

	public int putTile(int id, Image img) {
		return putTile(id, img, null);
	}

	public int putTile(int id, LTexture img, Attribute attribute) {
		if (active) {
			TileImpl tile = new TileImpl(id);
			tile.setImgId(texturePack.putImage(img));
			tile.setAttribute(attribute);
			arrays.add(tile);
			dirty = true;
			return tile.getImgId();
		} else {
			throw new LSysException("Map is no longer active, you can not add new tiles !");
		}
	}

	public int putTile(int id, LTexture img) {
		return putTile(id, img, null);
	}

	public int putTile(int id, String res, Attribute attribute) {
		if (active) {
			TileImpl tile = new TileImpl(id);
			tile.setImgId(texturePack.putImage(res));
			tile.setAttribute(attribute);
			arrays.add(tile);
			dirty = true;
			return tile.getImgId();
		} else {
			throw new LSysException("Map is no longer active, you can not add new tiles !");
		}
	}

	public int putTile(int id, String res) {
		return putTile(id, res, null);
	}

	public TileMap putTile(int id, int imgId, Attribute attribute) {
		if (active) {
			TileImpl tile = new TileImpl(id);
			tile.setImgId(imgId);
			tile.setAttribute(attribute);
			arrays.add(tile);
			dirty = true;
		} else {
			new LSysException("Map is no longer active, you can not add new tiles !");
		}
		return this;
	}

	public TileMap putTile(int id, int imgId) {
		return putTile(id, imgId, null);
	}

	public TileImpl getTile(int id) {
		for (TileImpl tile : arrays) {
			if (tile.getId() == id) {
				return tile;
			}
		}
		return null;
	}

	public int[][] getMap() {
		return field2d.getMap();
	}

	public boolean isActive() {
		return active;
	}

	public boolean isValid(int x, int y) {
		return this.field2d.inside(x, y);
	}

	public TileMap pack() {
		completed();
		return this;
	}

	public TileMap completed() {
		if (texturePack != null) {
			if (!texturePack.isPacked()) {
				texturePack.packed();
			}
			int[] list = texturePack.getIdList();
			active = true;
			dirty = true;
			for (int id : list) {
				putTile(id, id);
			}
		}
		return this;
	}

	public TileMap replaceType(int oldid, int newid) {
		field2d.replaceType(oldid, newid);
		return this;
	}

	public int getTileID(int x, int y) {
		if (x >= 0 && x < field2d.getWidth() && y >= 0 && y < field2d.getHeight()) {
			return field2d.getTileType(x, y);
		} else {
			return -1;
		}
	}

	public TileMap setTileID(int x, int y, int id) {
		if (x >= 0 && x < field2d.getWidth() && y >= 0 && y < field2d.getHeight()) {
			field2d.setTileType(x, y, id);
		}
		return this;
	}

	public TileMap addMapSprite(ISprite sprite) {
		_mapSprites.add(sprite);
		return this;
	}

	public TileMap addMapSpriteAt(ISprite sprite, float x, float y) {
		_mapSprites.addAt(sprite, x, y);
		return this;
	}

	public TileMap removeMapSprite(int idx) {
		_mapSprites.remove(idx);
		return this;
	}

	public TileMap removeMapSprite(ISprite sprite) {
		_mapSprites.remove(sprite);
		return this;
	}

	public TileMap removeMapSprite(int start, int end) {
		_mapSprites.remove(start, end);
		return this;
	}

	public void draw(GLEx g) {
		if (this.roll) {
			this.offset = this.toRollPosition(this.offset);
		}
		draw(g, x() + offset.x(), y() + offset.y());
	}

	public void draw(GLEx g, int offsetX, int offsetY) {

		if (_background != null) {
			g.draw(_background, offsetX, offsetY);
		}

		if (!active || texturePack == null) {
			completed();
			return;
		}

		this.dirty = this.dirty || !texturePack.existCache();

		if (!dirty && lastOffsetX == offsetX && lastOffsetY == offsetY) {

			texturePack.postCache();

			if (playAnimation) {
				final int tileWidth = field2d.getTileWidth();
				final int tileHeight = field2d.getTileHeight();
				int[][] maps = field2d.getMap();
				for (int i = firstTileX; i < lastTileX; i++) {
					for (int j = firstTileY; j < lastTileY; j++) {
						if (i > -1 && j > -1 && i < field2d.getWidth() && j < field2d.getHeight()) {
							int id = maps[j][i];
							final float posX = field2d.tilesToWidthPixels(i) + offsetX;
							final float posY = field2d.tilesToHeightPixels(j) + offsetY;
							for (TileImpl tile : arrays) {
								if (tile.isAnimation() && tile.getId() == id) {
									g.draw(tile.getAnimation().getSpriteImage(), posX, posY, tileWidth, tileHeight,
											baseColor);
								}
							}
						}
					}
				}
			}
		} else {
			if (arrays.size == 0) {
				throw new LSysException("Not to add any tiles !");
			}

			texturePack.glBegin();

			firstTileX = field2d.pixelsToTilesWidth(-offsetX);
			firstTileY = field2d.pixelsToTilesHeight(-offsetY);

			lastTileX = firstTileX + field2d.pixelsToTilesWidth(maxWidth) + 1;
			lastTileX = MathUtils.min(lastTileX, field2d.getWidth());
			lastTileY = firstTileY + field2d.pixelsToTilesHeight(maxHeight) + 1;
			lastTileY = MathUtils.min(lastTileY, field2d.getHeight());

			final int width = field2d.getWidth();
			final int height = field2d.getHeight();
			final int tileWidth = field2d.getTileWidth();
			final int tileHeight = field2d.getTileHeight();
			int[][] maps = field2d.getMap();
			for (int i = firstTileX; i < lastTileX; i++) {
				for (int j = firstTileY; j < lastTileY; j++) {
					if (i > -1 && j > -1 && i < width && j < height) {
						int id = maps[j][i];
						final float posX = field2d.tilesToWidthPixels(i) + offsetX;
						final float posY = field2d.tilesToHeightPixels(j) + offsetY;
						for (TileImpl tile : arrays) {
							if (playAnimation) {
								if (tile.getId() == id) {
									if (tile.isAnimation()) {
										g.draw(tile.getAnimation().getSpriteImage(), posX, posY, tileWidth, tileHeight,
												baseColor);
									} else {
										texturePack.draw(tile.getImgId(), posX, posY, tileWidth, tileHeight, baseColor);
									}
								}
							} else if (tile.getId() == id) {
								texturePack.draw(tile.getImgId(), posX, posY, tileWidth, tileHeight);
							}
						}
					}
				}
			}

			texturePack.glEnd();
			texturePack.saveCache();

			lastOffsetX = offsetX;
			lastOffsetY = offsetY;
			dirty = false;
		}

		if (listener != null) {
			listener.draw(g, offsetX, offsetY);
		}
	}

	public float centerX() {
		return (getContainerWidth() - getWidth()) / 2f;
	}

	public float centerY() {
		return (getContainerHeight() - getHeight()) / 2f;
	}

	public TileMap scrollDown(float distance) {
		if (distance == 0) {
			return this;
		}
		this.offset.y = MathUtils.min((this.offset.y + distance),
				(MathUtils.max(0, this.getContainerHeight() - this.getHeight())));
		if (this.offset.y >= 0) {
			this.offset.y = 0;
		}
		return this;
	}

	public TileMap scrollLeft(float distance) {
		if (distance == 0) {
			return this;
		}
		this.offset.x = MathUtils.min(this.offset.x - distance, this.getX());
		float limitX = (getContainerWidth() - getWidth());
		if (this.offset.x <= limitX) {
			this.offset.x = limitX;
		}
		return this;
	}

	public TileMap scrollRight(float distance) {
		if (distance == 0) {
			return this;
		}
		this.offset.x = MathUtils.min((this.offset.x + distance),
				(MathUtils.max(0, this.getWidth() - getContainerWidth())));
		if (this.offset.x >= 0) {
			this.offset.x = 0;
		}
		return this;
	}

	public TileMap scrollUp(float distance) {
		if (distance == 0) {
			return this;
		}
		this.offset.y = MathUtils.min(this.offset.y - distance, 0);
		float limitY = (getContainerHeight() - getHeight());
		if (this.offset.y <= limitY) {
			this.offset.y = limitY;
		}
		return this;
	}

	public TileMap scrollLeftUp(float distance) {
		this.scrollUp(distance);
		this.scrollLeft(distance);
		return this;
	}

	public TileMap scrollRightDown(float distance) {
		this.scrollDown(distance);
		this.scrollRight(distance);
		return this;
	}

	public TileMap scrollClear() {
		if (!this.offset.equals(0f, 0f)) {
			this.offset.set(0, 0);
		}
		return this;
	}

	public TileMap scroll(float x, float y) {
		return scroll(x, y, 4f);
	}

	public TileMap scroll(float x, float y, float distance) {
		if (_scrollDrag.x == 0f && _scrollDrag.y == 0f) {
			_scrollDrag.set(x, y);
			return this;
		}
		return scroll(_scrollDrag.x, _scrollDrag.y, x, y, distance);
	}

	public TileMap scroll(float x1, float y1, float x2, float y2) {
		return scroll(x1, y1, x2, y2, 4f);
	}

	public TileMap scroll(float x1, float y1, float x2, float y2, float distance) {
		if (this.follow != null) {
			return this;
		}
		if (x1 < x2 && x1 > centerX()) {
			scrollRight(distance);
		} else if (x1 > x2) {
			scrollLeft(distance);
		}
		if (y1 < y2 && y1 > centerY()) {
			scrollDown(distance);
		} else if (y1 > y2) {
			scrollUp(distance);
		}
		_scrollDrag.set(x2, y2);
		return this;
	}

	public int[] getLimit() {
		return field2d.getLimit();
	}

	public TileMap setLimit(int[] limit) {
		field2d.setLimit(limit);
		return this;
	}

	public TileMap setAllowMove(int[] args) {
		field2d.setAllowMove(args);
		return this;
	}

	public boolean isHit(int px, int py) {
		return field2d.isHit(px, py);
	}

	public boolean isHit(Vector2f v) {
		return isHit(v.x(), v.y());
	}

	public boolean isPixelHit(int px, int py) {
		return isPixelHit(px, py, 0, 0);
	}

	public boolean isPixelHit(int px, int py, int movePx, int movePy) {
		return isHit(field2d.pixelsToTilesWidth(field2d.offsetXPixel(px)) + movePx,
				field2d.pixelsToTilesHeight(field2d.offsetYPixel(py)) + movePy);
	}

	public boolean isPixelTUp(int px, int py) {
		return isPixelHit(px, py, 0, -1);
	}

	public boolean isPixelTRight(int px, int py) {
		return isPixelHit(px, py, 1, 0);
	}

	public boolean isPixelTLeft(int px, int py) {
		return isPixelHit(px, py, -1, 0);
	}

	public boolean isPixelTDown(int px, int py) {
		return isPixelHit(px, py, 0, 1);
	}

	public Vector2f getTileCollision(LObject<?> o, float newX, float newY) {
		return field2d.getTileCollision(o.getX(), o.getY(), o.getWidth(), o.getHeight(), newX, newY);
	}

	public int getTileIDFromPixels(Vector2f v) {
		return getTileIDFromPixels(v.x, v.y);
	}

	public int getTileIDFromPixels(float sx, float sy) {
		float x = (sx + offset.getX());
		float y = (sy + offset.getY());
		Vector2f tileCoordinates = pixelsToTiles(x, y);
		return getTileID(MathUtils.round(tileCoordinates.getX()), MathUtils.round(tileCoordinates.getY()));
	}

	public Vector2f pixelsToTiles(float x, float y) {
		float xprime = x / field2d.getTileWidth() - 1;
		float yprime = y / field2d.getTileHeight() - 1;
		return new Vector2f(xprime, yprime);
	}

	public int tilesToPixelsX(float x) {
		return field2d.tilesToWidthPixels(x);
	}

	public int tilesToPixelsY(float y) {
		return field2d.tilesToHeightPixels(y);
	}

	public int pixelsToTilesWidth(float x) {
		return field2d.pixelsToTilesWidth(x);
	}

	public int pixelsToTilesHeight(float y) {
		return field2d.pixelsToTilesHeight(y);
	}

	public PointI pixelsToTileMap(float x, float y) {
		int tileX = pixelsToTilesWidth(x);
		int tileY = pixelsToTilesHeight(y);
		return new PointI(tileX, tileY);
	}

	public PointI tilePixels(float x, float y) {
		int newX = getPixelX(x);
		int newY = getPixelY(y);
		return new PointI(newX, newY);
	}

	/**
	 * 转化地图到屏幕像素(不考虑地图滚动)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public PointI tileMapToPixels(float x, float y) {
		int tileX = tilesToPixelsX(x);
		int tileY = tilesToPixelsY(y);
		return new PointI(tileX, tileY);
	}

	/**
	 * 转化地图到屏幕像素(考虑地图滚动)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public PointI tileMapToScrollTilePixels(float x, float y) {
		int newX = toTileScrollPixelX(x);
		int newY = toTileScrollPixelX(y);
		return new PointI(newX, newY);
	}

	/**
	 * 转化屏幕像素到地图(考虑地图滚动)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public PointI pixelsToScrollTileMap(float x, float y) {
		int tileX = toPixelScrollTileX(x);
		int tileY = toPixelScrollTileY(y);
		return new PointI(tileX, tileY);
	}

	/**
	 * 转换坐标为像素坐标
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector2f tilesToPixels(float x, float y) {
		float xprime = x * field2d.getTileWidth() - offset.getX();
		float yprime = y * field2d.getTileHeight() - offset.getY();
		return new Vector2f(xprime, yprime);
	}

	/**
	 * 地图居中偏移
	 *
	 * @return
	 */
	public TileMap centerOffset() {
		this.offset.set(centerX(), centerY());
		return this;
	}

	/**
	 * 设定偏移量
	 *
	 * @param x
	 * @param y
	 */
	public TileMap setOffset(float x, float y) {
		this.offset.set(x, y);
		return this;
	}

	/**
	 * 设定偏移量
	 *
	 * @param offset
	 */
	@Override
	public TileMap setOffset(Vector2f offset) {
		this.offset.set(offset);
		return this;
	}

	/**
	 * 获得瓦片位置
	 *
	 * @return
	 */
	public Vector2f getOffset() {
		return offset;
	}

	public int getTileWidth() {
		return field2d.getTileWidth();
	}

	public int getTileHeight() {
		return field2d.getTileHeight();
	}

	@Override
	public float getHeight() {
		return (field2d.getHeight() * field2d.getTileWidth() * scaleY) - _fixedHeightOffset;
	}

	@Override
	public float getWidth() {
		return (field2d.getWidth() * field2d.getTileHeight() * scaleX) - _fixedWidthOffset;
	}

	public int getRow() {
		return field2d.getWidth();
	}

	public int getCol() {
		return field2d.getHeight();
	}

	public TileMap setMapValues(int v) {
		field2d.setValues(v);
		return this;
	}

	public Field2D getNewField2D() {
		return new Field2D(field2d);
	}

	public DrawListener<TileMap> getListener() {
		return listener;
	}

	public TileMap setListener(DrawListener<TileMap> l) {
		this.listener = l;
		return this;
	}

	public boolean isDirty() {
		return dirty;
	}

	public TileMap setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

	@Override
	public void setVisible(boolean v) {
		this.visible = v;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void createUI(GLEx g) {
		createUI(g, 0f, 0f);
	}

	@Override
	public void createUI(GLEx g, float offsetX, float offsetY) {
		if (!visible) {
			return;
		}
		boolean update = (_objectRotation != 0) || !(scaleX == 1f && scaleY == 1f);
		int blend = g.getBlendMode();
		int tmp = g.color();
		try {
			g.setAlpha(_objectAlpha);
			if (this.roll) {
				this.offset = toRollPosition(this.offset);
			}
			float newX = this._objectLocation.x + offsetX + offset.getX();
			float newY = this._objectLocation.y + offsetY + offset.getY();
			if (update) {
				g.saveTx();
				Affine2f tx = g.tx();
				if (_objectRotation != 0) {
					final float rotationCenterX = newX + getWidth() / 2f;
					final float rotationCenterY = newY + getHeight() / 2f;
					tx.translate(rotationCenterX, rotationCenterY);
					tx.preRotate(_objectRotation);
					tx.translate(-rotationCenterX, -rotationCenterY);
				}
				if ((scaleX != 1) || (scaleY != 1)) {
					final float scaleCenterX = newX + getWidth() / 2f;
					final float scaleCenterY = newY + getHeight() / 2f;
					tx.translate(scaleCenterX, scaleCenterY);
					tx.preScale(scaleX, scaleY);
					tx.translate(-scaleCenterX, -scaleCenterY);
				}
			}
			followActionObject();
			int moveX = (int) newX;
			int moveY = (int) newY;
			draw(g, moveX, moveY);
			if (_mapSprites != null) {
				_mapSprites.paintPos(g, moveX, moveY);
			}
		} catch (Throwable ex) {
			LSystem.error("Array2D TileMap error !", ex);
		} finally {
			if (update) {
				g.restoreTx();
			}
			g.setBlendMode(blend);
			g.setColor(tmp);
		}
	}

	@Override
	public RectBox getCollisionBox() {
		return getRect(x() + offset.x, y() + offset.y, field2d.getTileWidth() * field2d.getWidth(),
				field2d.getTileHeight() * field2d.getHeight());
	}

	@Override
	public LTexture getBitmap() {
		return texturePack.getTexture();
	}

	@Override
	public void update(long elapsedTime) {
		if (playAnimation && animations.size > 0) {
			for (Animation a : animations) {
				a.update(elapsedTime);
			}
		}
		if (_mapSprites != null) {
			_mapSprites.update(elapsedTime);
		}
		if (listener != null) {
			listener.update(elapsedTime);
		}
	}

	public TileMap startAnimation() {
		playAnimation = true;
		return this;
	}

	public TileMap stopAnimation() {
		playAnimation = false;
		return this;
	}

	protected float limitOffsetX(float newOffsetX) {
		float offsetX = getContainerWidth() / 2 - newOffsetX;
		offsetX = MathUtils.min(offsetX, 0);
		offsetX = MathUtils.max(offsetX, getContainerWidth() - getWidth());
		return offsetX;
	}

	protected float limitOffsetY(float newOffsetY) {
		float offsetY = getContainerHeight() / 2 - newOffsetY;
		offsetY = MathUtils.min(offsetY, 0);
		offsetY = MathUtils.max(offsetY, getContainerHeight() - getHeight());
		return offsetY;
	}

	public TileMap followActionObject() {
		if (follow != null) {
			float offsetX = limitOffsetX(follow.getX());
			float offsetY = limitOffsetY(follow.getY());
			if (offsetX != 0 || offsetY != 0) {
				setOffset(offsetX, offsetY);
				field2d.setOffset(offset);
			}
		}
		return this;
	}

	@Override
	public LColor getColor() {
		return new LColor(baseColor);
	}

	@Override
	public void setColor(LColor c) {
		if (c != null && !c.equals(baseColor)) {
			this.baseColor = c;
			this.dirty = true;
		}
	}

	public int getPixelsAtFieldType(Vector2f pos) {
		return field2d.getPixelsAtFieldType(pos.x, pos.y);
	}

	public int getPixelsAtFieldType(float x, float y) {
		int itsX = pixelsToTilesWidth(x);
		int itsY = pixelsToTilesHeight(y);
		return field2d.getPixelsAtFieldType(itsX, itsY);
	}

	@Override
	public Field2D getField2D() {
		return field2d;
	}

	@Override
	public float getScaleX() {
		return scaleX;
	}

	@Override
	public float getScaleY() {
		return scaleY;
	}

	@Override
	public void setScale(float sx, float sy) {
		this.scaleX = sx;
		this.scaleY = sy;
	}

	@Override
	public TileMap setSize(float w, float h) {
		setScale(w / getWidth(), h / getHeight());
		return this;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public boolean inContains(float x, float y, float w, float h) {
		return field2d.getRect().contains(x, y, w, h);
	}

	@Override
	public RectBox getRectBox() {
		return getCollisionBox();
	}

	public ActionBind getFollow() {
		return follow;
	}

	public TileMap setFollow(ActionBind follow) {
		this.follow = follow;
		return this;
	}

	public TileMap followDonot() {
		return setFollow(null);
	}

	public TileMap followAction(ActionBind follow) {
		return setFollow(follow);
	}

	public Vector2f toTilesScrollPixels(float x, float y) {
		return new Vector2f(toTileScrollPixelX(x), toTileScrollPixelY(y));
	}

	public int toTileScrollPixelX(float x) {
		return offsetXPixel(tilesToPixelsX(x));
	}

	public int toTileScrollPixelY(float y) {
		return offsetYPixel(tilesToPixelsY(y));
	}

	public Vector2f toPixelsScrollTiles(float x, float y) {
		return new Vector2f(toPixelScrollTileX(x), toPixelScrollTileY(y));
	}

	public int toPixelScrollTileX(float x) {
		return pixelsToTilesWidth(offsetXPixel(x));
	}

	public int toPixelScrollTileY(float y) {
		return pixelsToTilesHeight(offsetYPixel(y));
	}

	public Vector2f offsetPixels(float x, float y) {
		return new Vector2f(offsetXPixel(x), offsetYPixel(y));
	}

	public int getPixelX(float x) {
		return MathUtils.iceil((x - _objectLocation.x) / scaleX);
	}

	public int getPixelY(float y) {
		return MathUtils.iceil((y - _objectLocation.y) / scaleY);
	}

	public int offsetXPixel(float x) {
		return MathUtils.iceil((x - offset.x - _objectLocation.x) / scaleX);
	}

	public int offsetYPixel(float y) {
		return MathUtils.iceil((y - offset.y - _objectLocation.y) / scaleY);
	}

	public boolean inMap(int x, int y) {
		return ((((x >= 0) && (x < maxWidth)) && (y >= 0)) && (y < maxHeight));
	}

	public MoveControl followControl(ActionBind bind) {
		followAction(bind);
		return new MoveControl(bind, this.field2d);
	}

	public Vector2f toRollPosition(Vector2f pos) {
		pos.x = pos.x % ((field2d.getViewWidth()));
		pos.y = pos.y % ((field2d.getViewHeight()));
		if (pos.x < 0f) {
			pos.x += field2d.getViewWidth();
		}
		if (pos.x < 0f) {
			pos.y += field2d.getViewHeight();
		}
		return pos;
	}

	public boolean isRoll() {
		return roll;
	}

	public TileMap setRoll(boolean roll) {
		this.roll = roll;
		return this;
	}

	public LTexture getBackground() {
		return this._background;
	}

	public TileMap setBackground(LTexture bg) {
		this._background = bg;
		return this;
	}

	public boolean move(ActionBind o, float newX, float newY) {
		return move(o, newX, newY, true);
	}

	public boolean move(ActionBind o, float newX, float newY, boolean toMoved) {
		if (o == null) {
			return false;
		}
		float x = offsetXPixel(o.getX()) + newX;
		float y = offsetYPixel(o.getY()) + newY;
		if (!field2d.checkTileCollision(o, x, y)) {
			if (toMoved) {
				o.setLocation(x, y);
			}
			return true;
		}
		return false;
	}

	public boolean moveX(ActionBind o, float newX) {
		return moveX(o, newX, true);
	}

	public boolean moveX(ActionBind o, float newX, boolean toMoved) {
		if (o == null) {
			return false;
		}
		float x = offsetXPixel(o.getX()) + newX;
		float y = offsetYPixel(o.getY());
		if (!field2d.checkTileCollision(o, x, y)) {
			if (toMoved) {
				o.setLocation(x, y);
			}
			return true;
		}
		return false;
	}

	public boolean moveY(ActionBind o, float newY) {
		return moveY(o, newY, true);
	}

	public boolean moveY(ActionBind o, float newY, boolean toMoved) {
		if (o == null) {
			return false;
		}
		float x = offsetXPixel(o.getX());
		float y = offsetYPixel(o.getY()) + newY;
		if (!field2d.checkTileCollision(o, x, y)) {
			if (toMoved) {
				o.setLocation(x, y);
			}
			return true;
		}
		return false;
	}

	@Override
	public ActionTween selfAction() {
		return PlayerUtils.set(this);
	}

	@Override
	public boolean isActionCompleted() {
		return PlayerUtils.isActionCompleted(this);
	}

	public Sprites getMapSprites() {
		return _mapSprites;
	}

	public TileMap setMapSprites(Sprites s) {
		_mapSprites = s;
		return this;
	}

	@Override
	public ISprite setSprites(Sprites ss) {
		if (this._screenSprites == ss) {
			return this;
		}
		this._screenSprites = ss;
		return this;
	}

	@Override
	public Sprites getSprites() {
		return this._screenSprites;
	}

	@Override
	public Screen getScreen() {
		if (this._screenSprites == null) {
			return LSystem.getProcess().getScreen();
		}
		return this._screenSprites.getScreen() == null ? LSystem.getProcess().getScreen()
				: this._screenSprites.getScreen();
	}

	public float getScreenX() {
		float x = 0;
		ISprite parent = _objectSuper;
		if (parent != null) {
			x += parent.getX();
			for (; (parent = parent.getParent()) != null;) {
				x += parent.getX();
			}
		}
		return x + getX();
	}

	public float getScreenY() {
		float y = 0;
		ISprite parent = _objectSuper;
		if (parent != null) {
			y += parent.getY();
			for (; (parent = parent.getParent()) != null;) {
				y += parent.getY();
			}
		}
		return y + getY();
	}

	@Override
	public float getContainerX() {
		if (_objectSuper != null) {
			return getScreenX() - getX();
		}
		return this._screenSprites == null ? super.getContainerX() : this._screenSprites.getX();
	}

	@Override
	public float getContainerY() {
		if (_objectSuper != null) {
			return getScreenY() - getY();
		}
		return this._screenSprites == null ? super.getContainerY() : this._screenSprites.getY();
	}

	@Override
	public float getContainerWidth() {
		return this._screenSprites == null ? super.getContainerWidth() : this._screenSprites.getWidth();
	}

	@Override
	public float getContainerHeight() {
		return this._screenSprites == null ? super.getContainerHeight() : this._screenSprites.getHeight();
	}

	@Override
	public float getFixedWidthOffset() {
		return _fixedWidthOffset;
	}

	@Override
	public ISprite setFixedWidthOffset(float fixedWidthOffset) {
		this._fixedWidthOffset = fixedWidthOffset;
		return this;
	}

	@Override
	public float getFixedHeightOffset() {
		return _fixedHeightOffset;
	}

	@Override
	public ISprite setFixedHeightOffset(float fixedHeightOffset) {
		this._fixedHeightOffset = fixedHeightOffset;
		return this;
	}

	@Override
	public boolean collides(ISprite e) {
		if (e == null || !e.isVisible()) {
			return false;
		}
		return getRectBox().intersects(e.getCollisionBox());
	}

	@Override
	public boolean collidesX(ISprite other) {
		if (other == null || !other.isVisible()) {
			return false;
		}
		RectBox rectSelf = getRectBox();
		RectBox a = new RectBox(rectSelf.getX(), 0, rectSelf.getWidth(), rectSelf.getHeight());
		RectBox rectDst = getRectBox();
		RectBox b = new RectBox(rectDst.getX(), 0, rectDst.getWidth(), rectDst.getHeight());
		return a.intersects(b);
	}

	@Override
	public boolean collidesY(ISprite other) {
		if (other == null || !other.isVisible()) {
			return false;
		}
		RectBox rectSelf = getRectBox();
		RectBox a = new RectBox(0, rectSelf.getY(), rectSelf.getWidth(), rectSelf.getHeight());
		RectBox rectDst = getRectBox();
		RectBox b = new RectBox(0, rectDst.getY(), rectDst.getWidth(), rectDst.getHeight());
		return a.intersects(b);
	}

	@Override
	public void onResize() {
		if (_resizeListener != null) {
			_resizeListener.onResize(this);
		}
		if (_mapSprites != null) {
			_mapSprites.resize(getWidth(), getHeight(), false);
		}
	}

	public ResizeListener<TileMap> getResizeListener() {
		return _resizeListener;
	}

	public TileMap setResizeListener(ResizeListener<TileMap> listener) {
		this._resizeListener = listener;
		return this;
	}

	public TileMap setOffsetX(float sx) {
		this.offset.setX(sx);
		return this;
	}

	public TileMap setOffsetY(float sy) {
		this.offset.setY(sy);
		return this;
	}

	@Override
	public float getOffsetX() {
		return offset.x;
	}

	@Override
	public float getOffsetY() {
		return offset.y;
	}

	@Override
	public float left() {
		return getX();
	}

	@Override
	public float top() {
		return getY();
	}

	@Override
	public float right() {
		return getWidth();
	}

	@Override
	public float bottom() {
		return getHeight();
	}

	@Override
	public boolean showShadow() {
		return false;
	}

	@Override
	public boolean autoXYSort() {
		return false;
	}

	public boolean isClosed() {
		return isDisposed();
	}

	@Override
	public String toString() {
		return field2d.toString();
	}

	@Override
	public void close() {
		visible = false;
		playAnimation = false;
		roll = false;
		animations.clear();
		if (texturePack != null) {
			texturePack.close();
			texturePack = null;
		}
		if (_mapSprites != null) {
			_mapSprites.close();
			_mapSprites = null;
		}
		if (_background != null) {
			_background.close();
			_background = null;
		}
		_resizeListener = null;
		removeActionEvents(this);
		setState(State.DISPOSED);
	}

}
