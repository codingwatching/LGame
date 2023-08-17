/**
 * Copyright 2008 - 2010
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
 * @version 0.1
 */
package loon.component;

import loon.LSystem;
import loon.LTexture;
import loon.action.map.Field2D;
import loon.action.sprite.ISprite;
import loon.action.sprite.Sprites;
import loon.canvas.Canvas;
import loon.canvas.Image;
import loon.geom.RectBox;
import loon.geom.Vector2f;
import loon.opengl.GLEx;
import loon.utils.ArrayMap;
import loon.utils.LIterator;
import loon.utils.MathUtils;
import loon.utils.timer.LTimer;

/**
 * 一个Layer类,用于同Actor类合作渲染游戏
 */
public class LLayer extends ActorLayer {

	private Sprites _layerSprites;

	private float width;

	private float height;

	private float colorAlpha;

	private float actorX;

	private float actorY;

	private float actorWidth;

	private float actorHeight;

	protected boolean actorDrag, pressed;

	private Actor dragActor;

	private LTimer timer = new LTimer(0);

	private boolean isTouchClick;

	private Actor thing = null;

	private boolean isListener = false;

	private boolean isVSync;

	private int paintSeq = 0;

	public LLayer(int w, int h) {
		this(0, 0, w, h);
	}

	public LLayer(int w, int h, int size) {
		this(0, 0, w, h, true, size);
	}

	public LLayer(int w, int h, boolean bounded) {
		this(0, 0, w, h, bounded);
	}

	public LLayer(int x, int y, int w, int h) {
		this(x, y, w, h, true);
	}

	public LLayer(int x, int y, int w, int h, boolean bounded) {
		this(x, y, w, h, bounded, 1);
	}

	public LLayer(int x, int y, int w, int h, boolean bounded, int size) {
		super(x, y, w, h, size, bounded);
		this.setLocation(x, y);
		this._isLimitMove = true;
		this.actorDrag = true;
		this.customRendering = true;
		this.isTouchClick = true;
		this.isVSync = true;
		this.setElastic(true);
		this.setLocked(true);
		this.setLayer(-10000);
	}

	private void allocateSprites() {
		if (_layerSprites == null) {
			this._layerSprites = new Sprites(getScreen() == null ? LSystem.getProcess().getScreen() : getScreen(),
					getWidth(), getHeight());
		}
	}

	public LLayer addSprite(ISprite s) {
		allocateSprites();
		_layerSprites.add(s);
		return this;
	}

	public LLayer addSprite(ISprite... s) {
		for (int i = 0; i < s.length; i++) {
			addSprite(s[i]);
		}
		return this;
	}

	public LLayer addSpriteAt(ISprite s, float x, float y) {
		allocateSprites();
		_layerSprites.addAt(s, x, y);
		return this;
	}

	public LLayer removeSprite(ISprite s) {
		allocateSprites();
		_layerSprites.remove(s);
		return this;
	}

	public LLayer removeSprite(int idx) {
		allocateSprites();
		_layerSprites.remove(idx);
		return this;
	}

	public LLayer removeSpriteName(String name) {
		allocateSprites();
		_layerSprites.removeName(name);
		return this;
	}

	public LLayer removeSpriteAll() {
		allocateSprites();
		_layerSprites.removeAll();
		return this;
	}

	public LLayer setVSync(boolean vsync) {
		this.isVSync = vsync;
		return this;
	}

	public boolean isVSync() {
		return isVSync;
	}

	public void downClick(int x, int y) {
		if (_click != null) {
			_click.DownClick(this, x, y);
		}
		super.downClick();
	}

	public void upClick(int x, int y) {
		if (_click != null) {
			_click.UpClick(this, x, y);
		}
	}

	public void drag(int x, int y) {
		if (_click != null) {
			_click.DragClick(this, x, y);
		}
	}

	public void downKey() {
	}

	public void upKey() {
	}

	/**
	 * 设定动作触发延迟时间
	 * 
	 * @param delay
	 */
	public LLayer setDelay(long delay) {
		timer.setDelay(delay);
		return this;
	}

	/**
	 * 返回动作触发延迟时间
	 * 
	 * @return
	 */
	public long getDelay() {
		return timer.getDelay();
	}

	/**
	 * 动作处理
	 * 
	 * @param elapsedTime
	 */
	@Override
	public void action(long elapsedTime) {

	}

	@Override
	public void update(long elapsedTime) {
		if (isVisible()) {
			super.update(elapsedTime);
			if (timer.action(this.elapsedTime = elapsedTime)) {
				action(elapsedTime);
				if (!isVSync) {
					LIterator<Actor> it = objects.iterator();
					for (; it.hasNext();) {
						thing = (Actor) it.next();
						if (!thing.visible) {
							continue;
						}
						thing.update(elapsedTime);
					}
				}
				if (_layerSprites != null) {
					_layerSprites.update(elapsedTime);
				}
			}
		}
	}

	@Override
	public void createCustomUI(GLEx g, int x, int y, int w, int h) {
		if (!isVisible()) {
			return;
		}
		int tint = g.color();
		paintObjects(g, x, y, x + w, y + h);
		if (x == 0 && y == 0) {
			paint(g);
			if (_layerSprites != null) {
				_layerSprites.paint(g, x, y, w, h);
			}
		} else {
			try {
				g.translate(x, y);
				paint(g);
				if (_layerSprites != null) {
					_layerSprites.paint(g, x, y, w, h);
				}
			} finally {
				g.translate(-x, -y);
			}
		}
		g.setTint(tint);
	}

	public void paint(GLEx g) {

	}

	public void paintObjects(GLEx g, int minX, int minY, int maxX, int maxY) {
		synchronized (objects) {
			LIterator<Actor> it = objects.iterator();
			for (; it.hasNext();) {
				thing = it.next();
				if (!thing.visible) {
					continue;
				}

				isListener = (thing.actorListener != null);

				if (isVSync) {
					if (isListener) {
						thing.actorListener.update(elapsedTime);
					}
					thing.update(elapsedTime);
				}

				RectBox rect = thing.getRectBox();
				actorX = minX + thing.getX();
				actorY = minY + thing.getY();
				actorWidth = rect.width;
				actorHeight = rect.height;
				if (actorX + actorWidth < minX || actorX > maxX || actorY + actorHeight < minY || actorY > maxY) {
					continue;
				}
				int tint = g.color();
				float alpha = g.alpha();
				LTexture actorImage = thing.getImage();
				if (actorImage != null) {
					width = actorImage.getWidth();
					height = actorImage.getHeight();
					thing.setLastPaintSeqNum(paintSeq++);
					float oldAlpha = g.alpha();
					colorAlpha = thing.getAlpha();
					if (colorAlpha != oldAlpha) {
						g.setAlpha(colorAlpha);
					}
					g.draw(actorImage, actorX, actorY, width, height,
							_component_baseColor == null ? thing.filterColor
									: _component_baseColor.mul(thing.filterColor),
							thing.getRotation(), thing.scaleX, thing.scaleY, thing.flipX, thing.flipY);
					if (colorAlpha != oldAlpha) {
						g.setAlpha(oldAlpha);
					}
				}
				if (thing.isConsumerDrawing) {
					if (actorX == 0 && actorY == 0) {
						thing.draw(g);
						if (isListener) {
							thing.actorListener.draw(g);
						}
					} else {
						try {
							g.saveTx();
							g.translate(actorX, actorY);
							thing.draw(g);
							if (isListener) {
								thing.actorListener.draw(g);
							}
						} finally {
							g.translate(-actorX, -actorY);
							g.restoreTx();
						}
					}
				}
				g.setAlpha(alpha);
				g.setTint(tint);
			}

		}
	}

	public LLayer moveCamera(Actor actor) {
		moveCamera(actor.x(), actor.y());
		return this;
	}

	public LLayer centerOn(final Actor object) {
		object.setLocation(getWidth() / 2 - object.getWidth() / 2, getHeight() / 2 - object.getHeight() / 2);
		return this;
	}

	public LLayer topOn(final Actor object) {
		object.setLocation(getWidth() / 2 - object.getWidth() / 2, 0);
		return this;
	}

	public LLayer leftOn(final Actor object) {
		object.setLocation(0, getHeight() / 2 - object.getHeight() / 2);
		return this;
	}

	public LLayer rightOn(final Actor object) {
		object.setLocation(getWidth() - object.getWidth(), getHeight() / 2 - object.getHeight() / 2);
		return this;
	}

	public LLayer bottomOn(final Actor object) {
		object.setLocation(getWidth() / 2 - object.getWidth() / 2, getHeight() - object.getHeight());
		return this;
	}

	public LLayer setField2DBackground(Field2D field, ArrayMap pathMap) {
		setField2DBackground(field, pathMap, null);
		return this;
	}

	public LLayer setField2DBackground(Field2D field, ArrayMap pathMap, String fileName) {
		setField2D(field);
		Image background = null;
		if (fileName != null) {
			Image tmp = Image.createImage(fileName);
			background = setTileBackground(tmp, true);
			if (tmp != null) {
				tmp.close();
				tmp = null;
			}
		} else {
			background = Image.createImage((int) getWidth(), (int) getHeight());
		}
		Canvas g = background.getCanvas();
		for (int i = 0; i < field.getWidth(); i++) {
			for (int j = 0; j < field.getHeight(); j++) {
				int index = field.getTileType(i, j);
				Object o = pathMap.get(index);
				if (o != null) {
					if (o instanceof Image) {
						g.draw(((Image) o), field.tilesToWidthPixels(i), field.tilesToHeightPixels(j));
					} else if (o instanceof Actor) {
						addObject(((Actor) o), field.tilesToWidthPixels(i), field.tilesToHeightPixels(j));
					}
				}
			}
		}
		g.close();
		setBackground(background.texture());
		if (background != null) {
			background.close();
			background = null;
		}
		return this;
	}

	public LLayer setTileBackground(String fileName) {
		setTileBackground(Image.createImage(fileName));
		return this;
	}

	public LLayer setTileBackground(Image image) {
		setTileBackground(image, false);
		return this;
	}

	public Image setTileBackground(Image image, boolean isReturn) {
		if (image == null) {
			return null;
		}
		int layerWidth = (int) getWidth();
		int layerHeight = (int) getHeight();
		int tileWidth = image.getWidth();
		int tileHeight = image.getHeight();

		Image tempImage = Image.createImage(layerWidth, layerHeight);
		Canvas g = tempImage.getCanvas();
		for (int x = 0; x < layerWidth; x += tileWidth) {
			for (int y = 0; y < layerHeight; y += tileHeight) {
				g.draw(image, x, y);
			}
		}
		g.close();
		if (isReturn) {
			return tempImage;
		}
		setBackground(tempImage.texture());
		if (tempImage != null) {
			tempImage.close();
			tempImage = null;
		}
		return null;
	}

	public int getScroll(RectBox visibleRect, int orientation, int direction) {
		int cellSize = this.getCellSize();
		float scrollPos = 0f;
		if (orientation == 0) {
			if (direction < 0) {
				scrollPos = visibleRect.getMinX();
			} else if (direction > 0) {
				scrollPos = visibleRect.getMaxX();
			}
		} else if (direction < 0) {
			scrollPos = visibleRect.getMinY();
		} else if (direction > 0) {
			scrollPos = visibleRect.getMaxY();
		}
		int increment = MathUtils.abs((int) MathUtils.IEEEremainder(scrollPos, cellSize));
		if (increment == 0) {
			increment = cellSize;
		}
		return increment;
	}

	public Actor getClickActor() {
		return dragActor;
	}

	@Override
	protected void processTouchEntered() {
		this.pressed = true;
	}

	@Override
	protected void processTouchExited() {
		this.pressed = false;
	}

	@Override
	protected void processKeyPressed() {
		if (this.isSelected()) {
			this.downKey();
		}
	}

	@Override
	protected void processKeyReleased() {
		if (this.isSelected()) {
			this.upKey();
		}
	}

	@Override
	protected void processTouchPressed() {
		if (!isTouchClick) {
			return;
		}
		super.processTouchPressed();
		if (!input.isMoving()) {
			final Vector2f pos = getUITouchXY();
			int dx = MathUtils.floor(pos.x);
			int dy = MathUtils.floor(pos.y);
			dragActor = getSynchronizedObject(dx, dy);
			if (dragActor != null) {
				if (dragActor.isClick()) {
					dragActor.downClick(dx, dy);
					if (dragActor.actorListener != null) {
						dragActor.actorListener.downClick(dx, dy);
					}
				}
			}
			try {
				this.downClick(dx, dy);
			} catch (Throwable e) {
				LSystem.error("Layer downClick() exception", e);
			}
		}
	}

	@Override
	protected void processTouchReleased() {
		if (!isTouchClick) {
			return;
		}
		super.processTouchReleased();
		if (!input.isMoving()) {
			final Vector2f pos = getUITouchXY();
			int dx = MathUtils.floor(pos.x);
			int dy = MathUtils.floor(pos.y);
			dragActor = getSynchronizedObject(dx, dy);
			if (dragActor != null) {
				if (dragActor.isClick()) {
					dragActor.upClick(dx, dy);
					if (dragActor.actorListener != null) {
						dragActor.actorListener.upClick(dx, dy);
					}
				}
			}
			try {
				this.upClick(dx, dy);
			} catch (Throwable e) {
				LSystem.error("Layer upClick() exception", e);
			}
			this.dragActor = null;
		}
	}

	@Override
	protected void processTouchDragged() {
		int dropX = 0;
		int dropY = 0;
		if (!locked) {
			boolean moveActor = false;
			if (actorDrag) {
				synchronized (objects) {
					final Vector2f pos = getUITouchXY();
					dropX = MathUtils.floor(pos.x);
					dropY = MathUtils.floor(pos.y);
					if (dragActor == null) {
						dragActor = getSynchronizedObject(dropX, dropY);
					}
					if (dragActor != null && dragActor.isDrag()) {
						synchronized (dragActor) {
							objects.sendToFront(dragActor);
							RectBox rect = dragActor.getBoundingRect();
							int dx = dropX - (rect.width / 2);
							int dy = dropY - (rect.height / 2);
							if (dragActor.getLLayer() != null) {
								dragActor.setLocation(dx, dy);
								dragActor.drag(dropX, dropY);
								if (dragActor.actorListener != null) {
									dragActor.actorListener.drag(dropX, dropY);
								}
							}
							moveActor = true;
						}
					}
				}
			}
			if (!moveActor) {
				synchronized (input) {
					dropX = this.input.getTouchDX();
					dropY = this.input.getTouchDY();
					if (isNotMoveInScreen(dropX + this.x(), dropY + this.y())) {
						return;
					}
					if (getContainer() != null) {
						getContainer().sendToFront(this);
					}
					try {
						this.move(dropX, dropY);
						this.drag(dropX, dropY);
					} catch (Throwable e) {
						LSystem.error("Layer drag() exception", e);
					}
				}
			}
		} else {
			if (!actorDrag) {
				return;
			}
			synchronized (objects) {
				final Vector2f pos = getUITouchXY();
				dropX = MathUtils.floor(pos.x);
				dropY = MathUtils.floor(pos.y);
				if (dragActor == null) {
					dragActor = getSynchronizedObject(dropX, dropY);
				}
				if (dragActor != null && dragActor.isDrag()) {
					synchronized (dragActor) {
						objects.sendToFront(dragActor);
						RectBox rect = dragActor.getBoundingRect();
						int dx = dropX - (rect.width / 2);
						int dy = dropY - (rect.height / 2);
						if (dragActor.getLLayer() != null) {
							dragActor.setLocation(dx, dy);
							dragActor.drag(dropX, dropY);
							if (dragActor.actorListener != null) {
								dragActor.actorListener.drag(dropX, dropY);
							}
						}
					}
				}
			}
		}
		try {
			super.dragClick();
		} catch (Throwable e) {
			LSystem.error("Layer dragClick() exception", e);
		}
	}

	public boolean isTouchPressed() {
		return this.pressed;
	}

	public boolean isActorDrag() {
		return actorDrag;
	}

	public LLayer setActorDrag(boolean d) {
		this.actorDrag = d;
		return this;
	}

	public boolean isLimitMove() {
		return _isLimitMove;
	}

	public LLayer setLimitMove(boolean isLimitMove) {
		this._isLimitMove = isLimitMove;
		return this;
	}

	public boolean isTouchClick() {
		return isTouchClick;
	}

	public LLayer setTouchClick(boolean t) {
		this.isTouchClick = t;
		return this;
	}

	public float getLayerTouchX() {
		return getUITouchX();
	}

	public float getLayerTouchY() {
		return getUITouchY();
	}

	@Override
	public void createUI(GLEx g, int x, int y) {

	}

	@Override
	public String getUIName() {
		return "Layer";
	}

	@Override
	public void destory() {
		if (collisionChecker != null) {
			collisionChecker.dispose();
			collisionChecker = null;
		}
		if (objects != null) {
			Object[] o = objects.toActors();
			for (int i = 0; i < o.length; i++) {
				Actor actor = (Actor) o[i];
				if (actor != null) {
					actor.close();
					actor = null;
				}
			}
		}
		if (_layerSprites != null) {
			_layerSprites.close();
		}
		_layerSprites = null;
	}
}
