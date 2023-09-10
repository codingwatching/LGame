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
package loon.action.node;

import java.util.Comparator;

import loon.LObject;
import loon.LSystem;
import loon.LTexture;
import loon.PlayerUtils;
import loon.Screen;
import loon.action.ActionBind;
import loon.action.ActionTween;
import loon.action.map.Field2D;
import loon.action.sprite.ISprite;
import loon.action.sprite.SpriteBatch;
import loon.action.sprite.Sprites;
import loon.canvas.LColor;
import loon.events.GameTouch;
import loon.events.ResizeListener;
import loon.events.SysInput;
import loon.events.SysKey;
import loon.events.SysTouch;
import loon.geom.BoxSize;
import loon.geom.RectBox;
import loon.geom.Vector2f;
import loon.opengl.GL20;
import loon.opengl.GLEx;
import loon.utils.GLUtils;
import loon.utils.InsertionSorter;
import loon.utils.MathUtils;
import loon.utils.TArray;
import loon.utils.timer.Duration;

public class LNNode extends LObject<LNNode> implements ISprite, BoxSize {

	public void down(GameTouch e) {

	}

	public void up(GameTouch e) {

	}

	public void drag(GameTouch e) {

	}

	private static final Comparator<LNNode> DEFAULT_COMPARATOR = new Comparator<LNNode>() {

		private int match(int x, int y) {
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}

		public int compare(LNNode p1, LNNode p2) {
			if (p1 == null || p2 == null) {
				if (p1 != null) {
					return p1._objectLayer;
				}
				if (p2 != null) {
					return p2._objectLayer;
				}
				return 0;
			}
			if (SysTouch.isDrag()) {
				return p2._objectLayer - p1._objectLayer;
			}
			return match(p2._objectLayer, p1._objectLayer);
		}
	};

	private Comparator<LNNode> comparator = LNNode.DEFAULT_COMPARATOR;

	private ResizeListener<LNNode> _resizeListener;

	private final static InsertionSorter<LNNode> _node_sorter = new InsertionSorter<LNNode>();

	public static interface CallListener {

		public void act(float dt);

	}

	public LNClickListener Click;

	public void SetClick(LNClickListener c) {
		Click = c;
	}

	public LNClickListener GetClick() {
		return Click;
	}

	public CallListener Call;

	public void SetCall(CallListener u) {
		Call = u;
	}

	public CallListener GetCall() {
		return Call;
	}

	protected boolean _locked;

	public LNNode[] childs = new LNNode[0];

	protected int _childCount = 0;

	private LNNode latestInserted = null;

	protected TArray<LNAction> _actionList;

	protected Vector2f _anchor = new Vector2f();

	protected LColor _color;

	protected float _size_width, _size_height;

	protected float _orig_width, _orig_height;

	protected Sprites _sprites;

	@Override
	public void setWidth(float w) {
		if (this._size_width != w) {
			this.onResize();
		}
		this._size_width = w;
		if (_orig_width == 0) {
			this._orig_width = w;
		}
	}

	@Override
	public void setHeight(float h) {
		if (this._size_height != h) {
			this.onResize();
		}
		this._size_height = h;
		if (_orig_height == 0) {
			this._orig_height = h;
		}
	}

	void setNodeSize(float w, float h) {
		if (this._size_height != h || this._size_height != h) {
			this.onResize();
		}
		this._size_width = w;
		this._size_height = h;
		if (_orig_width == 0) {
			this._orig_width = w;
		}
		if (_orig_height == 0) {
			this._orig_height = h;
		}
	}

	protected float _fixedWidthOffset = 0f;

	protected float _fixedHeightOffset = 0f;

	protected int _top;

	protected int _left;

	protected float _rotationAlongX;

	protected float _rotationAlongY;

	protected final Vector2f _scale = new Vector2f(1f, 1f);

	protected boolean _visible = true;

	protected boolean _autoDestroy;

	protected boolean _isClose;

	private int cam_x, cam_y;

	protected int _screenX, _screenY;

	protected boolean _enabled = true;

	protected boolean _focusable = true;

	protected boolean _selected = false;

	protected boolean _limitMove;

	protected NodeScreen _screen;

	protected RectBox _screenRect;

	protected SysInput _input;

	protected final Vector2f _offset = new Vector2f();

	LNNode() {
		this(LSystem.viewSize.getRect());
	}

	public LNNode(int x, int y, int width, int height) {
		this(null, x, y, width, height);
	}

	public LNNode(RectBox rect) {
		this(null, rect.x(), rect.y(), rect.width, rect.height);
	}

	public LNNode(NodeScreen screen, RectBox rect) {
		this(screen, rect.x(), rect.y(), rect.width, rect.height);
	}

	public LNNode(NodeScreen screen, int x, int y, int width, int height) {
		this.setLocation(x, y);
		this._objectRotation = 0f;
		this.scale[0] = 1f;
		this.scale[1] = 1f;
		this._scale.x = 1f;
		this._scale.y = 1f;
		this._objectAlpha = 1f;
		this._left = 0;
		this._top = 0;
		this._screen = screen;
		this._color = new LColor(0xff, 0xff, 0xff, 0xff);
		this._actionList = new TArray<LNAction>();
		this._limitMove = true;
		this._locked = true;
		this._size_width = width;
		this._size_height = height;
		this._screenRect = LSystem.viewSize.getRect();
		if (this._size_width == 0) {
			this._size_width = 10;
		}
		if (this._size_height == 0) {
			this._size_height = 10;
		}
	}

	public boolean isLocked() {
		return _locked;
	}

	public void setLocked(boolean locked) {
		this._locked = locked;
	}

	public void addNode(LNNode node) {
		this.addNode(node, 0);
	}

	public synchronized void addNode(LNNode node, int z) {
		if (this.contains(node)) {
			return;
		}
		if (node.getContainer() != null) {
			node.setContainer(null);
		}
		node.setContainer(this);
		node.setSprites(this._sprites);
		node.setState(State.ADDED);
		int index = 0;
		boolean flag = false;
		for (int i = 0; i < this._childCount; i++) {
			LNNode node2 = this.childs[i];
			int zd = 0;
			if (node2 != null) {
				zd = node2.getZOrder();
			}
			if (zd > z) {
				flag = true;
				this.childs = NodeScreen.expand(this.childs, 1, false);
				childs[index] = node;
				_childCount++;
				node.setScreen(_screen);
				this.latestInserted = node;
				break;
			}
			index++;
		}
		if (!flag) {
			this.childs = NodeScreen.expand(this.childs, 1, false);
			this.childs[0] = node;
			this._childCount++;
			node.setScreen(_screen);
			this.latestInserted = node;
		}
		node.setZOrder(z);
		node.setParent(this);
		_node_sorter.sort(childs, comparator);
	}

	public synchronized void add(LNNode node, int index) {
		if (node.getContainer() != null) {
			throw new IllegalStateException(node + " already reside in another node!!!");
		}
		node.setContainer(this);
		node.setSprites(this._sprites);
		node.setState(State.ADDED);
		LNNode[] newChilds = new LNNode[this.childs.length + 1];
		this._childCount++;
		int ctr = 0;
		for (int i = 0; i < this._childCount; i++) {
			if (i != index) {
				newChilds[i] = this.childs[ctr];
				ctr++;
			}
		}
		this.childs = newChilds;
		this.childs[index] = node;
		node.setScreen(_screen);
		this.sortComponents();
		this.latestInserted = node;
	}

	public synchronized boolean contains(LNNode node) {
		if (node == null) {
			return false;
		}
		if (childs == null) {
			return false;
		}
		for (int i = 0; i < this._childCount; i++) {
			if (childs[i] != null && node.equals(childs[i])) {
				return true;
			}
		}
		return false;
	}

	public synchronized int removeNode(LNNode node) {
		for (int i = 0; i < this._childCount; i++) {
			if (this.childs[i] == node) {
				this.removeNode(i);
				return i;
			}
		}
		return -1;
	}

	public synchronized LNNode removeNode(int index) {
		LNNode node = this.childs[index];
		if (node != null && node instanceof ActionBind) {
			removeActionEvents((ActionBind) node);
		}
		this._screen.setNodeStat(node, false);
		node.setContainer(null);
		node.setState(State.REMOVED);
		this.childs = NodeScreen.cut(this.childs, index);
		this._childCount--;
		return node;
	}

	public void clear() {
		this._screen.clearNodesStat(this.childs);
		for (int i = 0; i < this._childCount; i++) {
			final LNNode removed = this.childs[i];
			if (removed != null) {
				removed.setContainer(null);
				removed.setState(State.REMOVED);
			}
			// 删除精灵同时，删除缓动动画
			if (removed != null && removed instanceof ActionBind) {
				removeActionEvents((ActionBind) removed);
			}
		}
		this.childs = new LNNode[0];
		this._childCount = 0;
	}

	public synchronized void replace(LNNode oldComp, LNNode newComp) {
		int index = this.removeNode(oldComp);
		this.add(newComp, index);
	}

	public void dispose() {
		if (_screen != null) {
			if (_screen.removeNode(this) != -1) {
				this.close();
			}
		}
	}

	public void update(float dt) {

	}

	@Override
	public final void update(long elapsedTime) {
		if (_isClose) {
			return;
		}
		float dt = Duration.toS(elapsedTime);
		synchronized (childs) {
			if (_objectSuper != null) {
				validatePosition();
			}
			if (Call != null) {
				Call.act(dt);
			}
			for (int i = 0; i < _actionList.size; i++) {
				if (this._actionList.get(i).isEnd()) {
					this._actionList.remove(this._actionList.get(i));
					i--;
				} else {
					this._actionList.get(i).step(dt);
					if (this._actionList.isEmpty()) {
						break;
					}
					if (this._actionList.get(i).isEnd()) {
						this._actionList.remove(this._actionList.get(i));
						i--;
					}
				}
			}
			LNNode component;
			for (int i = 0; i < this._childCount; i++) {
				component = childs[i];
				if (component != null) {
					component.update(elapsedTime);
				}
			}
		}
		update(dt);
	}

	protected void validateSize() {
		for (int i = 0; i < this._childCount; i++) {
			if (this.childs[i] != null) {
				this.childs[i].validateSize();
			}
		}
	}

	public void sendToFront(LNNode node) {
		if (this.childs == null) {
			return;
		}
		if (this._childCount <= 1 || this.childs[0] == node) {
			return;
		}
		if (childs[0] == node) {
			return;
		}
		for (int i = 0; i < this._childCount; i++) {
			if (this.childs[i] == node) {
				this.childs = NodeScreen.cut(this.childs, i);
				this.childs = NodeScreen.expand(this.childs, 1, false);
				this.childs[0] = node;
				this.sortComponents();
				break;
			}
		}
	}

	public void sendToBack(LNNode node) {
		if (this._childCount <= 1 || this.childs[this._childCount - 1] == node) {
			return;
		}
		if (childs[this._childCount - 1] == node) {
			return;
		}
		for (int i = 0; i < this._childCount; i++) {
			if (this.childs[i] == node) {
				this.childs = NodeScreen.cut(this.childs, i);
				this.childs = NodeScreen.expand(this.childs, 1, true);
				this.childs[this._childCount - 1] = node;
				this.sortComponents();
				break;
			}
		}
	}

	public void sortComponents() {
		_node_sorter.sort(this.childs, this.comparator);
	}

	protected void transferFocus(LNNode component) {
		for (int i = 0; i < this._childCount; i++) {
			if (component == this.childs[i]) {
				int j = i;
				do {
					if (--i < 0) {
						i = this._childCount - 1;
					}
					if (i == j) {
						return;
					}
				} while (!this.childs[i].requestFocus());

				break;
			}
		}
	}

	protected void transferFocusBackward(LNNode component) {
		for (int i = 0; i < this._childCount; i++) {
			if (component == this.childs[i]) {
				int j = i;
				do {
					if (++i >= this._childCount) {
						i = 0;
					}
					if (i == j) {
						return;
					}
				} while (!this.childs[i].requestFocus());

				break;
			}
		}
	}

	public Comparator<LNNode> getComparator() {
		return this.comparator;
	}

	public void setComparator(Comparator<LNNode> c) {
		if (c == null) {
			throw new NullPointerException("Comparator can not null !");
		}

		this.comparator = c;
		this.sortComponents();
	}

	public LNNode findNode(int x1, int y1) {
		if (!this.intersects(x1, y1)) {
			return null;
		}
		for (int i = 0; i < this._childCount; i++) {
			if (childs[i] != null) {
				if (this.childs[i].intersects(x1, y1)) {
					LNNode node = isContainer() ? this.childs[i] : (this.childs[i]).findNode(x1, y1);
					return node;
				}
			}
		}
		return this;
	}

	public int getNodeCount() {
		return this._childCount;
	}

	public LNNode[] getNodes() {
		return this.childs;
	}

	public LNNode get() {
		return this.latestInserted;
	}

	public void draw(SpriteBatch batch) {

	}

	public void draw(GLEx gl) {

	}

	public final void drawNode(SpriteBatch batch) {
		if (_isClose) {
			return;
		}
		if (!this._visible) {
			return;
		}
		if (_objectAlpha < 0.01) {
			return;
		}
		int blend = GLUtils.getBlendMode();
		GL20 gl = LSystem.base().graphics().gl;
		GLUtils.setBlendMode(gl, _GL_BLEND);
		float tmp = batch.alpha();
		batch.setAlpha(_objectAlpha);
		for (int i = this._childCount - 1; i >= 0; i--) {
			if (childs[i] != null && childs[i].getZOrder() < 0) {
				childs[i].drawNode(batch);
			}
		}
		this.draw(batch);
		int zOrder = 0;
		for (int i = this._childCount - 1; i >= 0; i--) {
			LNNode o = this.childs[i];
			if (o != null) {
				if (o.getZOrder() >= 0) {
					if (zOrder == 0) {
						zOrder = o.getZOrder();
					} else {
						zOrder = o.getZOrder();
					}

					o.drawNode(batch);
				}
			}
		}
		batch.setAlpha(tmp);
		GLUtils.setBlendMode(gl, blend);
	}

	public final void drawNode(GLEx gl) {
		if (_isClose) {
			return;
		}
		if (!this._visible) {
			return;
		}
		if (_objectAlpha < 0.01) {
			return;
		}
		int blend = gl.getBlendMode();
		float tmp = gl.alpha();
		gl.setBlendMode(_GL_BLEND);
		gl.setAlpha(_objectAlpha);
		for (int i = this._childCount - 1; i >= 0; i--) {
			if (childs[i] != null && childs[i].getZOrder() < 0) {
				childs[i].drawNode(gl);
			}
		}
		this.draw(gl);
		int zOrder = 0;
		for (int i = this._childCount - 1; i >= 0; i--) {
			LNNode o = this.childs[i];
			if (o != null) {
				if (o.getZOrder() >= 0) {
					if (zOrder == 0) {
						zOrder = o.getZOrder();
					} else {
						zOrder = o.getZOrder();
					}

					o.drawNode(gl);
				}
			}
		}
		gl.setAlpha(tmp);
		gl.setBlendMode(blend);
	}

	public void setOffset(float x, float y) {
		this._offset.set(x, y);
	}

	@Override
	public LNNode setOffset(Vector2f v) {
		this._offset.set(v);
		return this;
	}

	@Override
	public float getOffsetX() {
		return this._offset.x;
	}

	@Override
	public float getOffsetY() {
		return this._offset.y;
	}

	public Vector2f getOffset() {
		return this._offset;
	}

	private float[] pos = new float[2];

	public float[] convertToWorldPos() {
		pos[0] = _offset.x + _objectLocation.x;
		pos[1] = _offset.y + _objectLocation.y;
		if (this._objectSuper != null) {
			float[] result = this._objectSuper.convertToWorldPos();
			pos[0] += result[0];
			pos[1] += result[1];
		}
		return pos;
	}

	private float[] scale = new float[2];

	public float[] convertToWorldScale() {
		scale[0] = _scale.x;
		scale[1] = _scale.y;
		if (this._objectSuper != null) {
			float[] result = this._objectSuper.convertToWorldScale();
			scale[0] *= result[0];
			scale[1] *= result[1];
		}
		return scale;
	}

	public float convertToWorldRot() {
		float num = 0f;
		if (this._objectSuper != null) {
			num += this._objectSuper.convertToWorldRot();
		}
		return (num + this._objectRotation);
	}

	public void onSceneActive() {
	}

	public final void pauseAllAction() {
		for (LNAction action : this._actionList) {
			action.pause();
		}
	}

	public final void reorderNode(LNNode node, int NewOrder) {
		this.removeNode(node);
		this.addNode(node, NewOrder);
	}

	public final void resumeAllAction() {
		for (LNAction action : this._actionList) {
			action.resume();
		}
	}

	public final void removeAction(LNAction action) {
		_actionList.remove(action);
		action._target = null;
	}

	public final void stopAllAction() {
		for (LNAction action : _actionList) {
			action._isEnd = true;
		}
		this._actionList.clear();
	}

	public final void runAction(LNAction action) {
		this._actionList.add(action);
		action.setTarget(this);
	}

	public void setColor(LColor c) {
		this._color.setColor(c);
	}

	public void setColor(int r, int g, int b) {
		this._color.setColor(r, g, b);
	}

	public void setColor(int r, int g, int b, int a) {
		this._color.setColor(r, g, b, a);
	}

	public void setColor(float r, float g, float b) {
		this._color.setColor(r, g, b);
	}

	public void setColor(float r, float g, float b, float a) {
		this._color.setColor(r, g, b, a);
	}

	public void setLimitMove(boolean v) {
		this._limitMove = v;
	}

	public boolean isLimitMove() {
		return _limitMove;
	}

	public void setPosition(float x, float y) {
		this._objectLocation.set(x, y);
	}

	public void setPositionOrig(Vector2f v) {
		setPositionOrig(v.x, v.y);
	}

	public void setPositionOrig(float x, float y) {
		this._objectLocation.set((x + this._anchor.x) - (_screenRect.width / 2),
				(_screenRect.height / 2) - (y + this._anchor.y));
	}

	public void setPosition(Vector2f newPosition) {
		if (!newPosition.equals(this._objectLocation)) {
			this.position(newPosition);
		}
	}

	public void setPositionBL(float x, float y) {
		this.setPosition((x + this._anchor.x) - (_screenRect.width / 2),
				((_screenRect.height / 2) - (((_screenRect.height - y) - this._size_height) + this._anchor.y)));
	}

	public void setPositionBR(float x, float y) {
		this.setPosition((((_screenRect.width - x) - this._size_width) + this._anchor.x) - (_screenRect.width / 2),
				(_screenRect.height / 2) - (((_screenRect.height - y) - this._size_height) + this._anchor.y));
	}

	public void setPositionTL(float x, float y) {
		this.setPosition((x + this._anchor.x) - (_screenRect.width / 2),
				((_screenRect.height / 2) - (_screenRect.height - (y + this._anchor.y))));
	}

	public void setPositionTR(float x, float y) {
		this.setPosition(
				((((_screenRect.width - x) - this._size_width) + this._anchor.x) - (_screenRect.width / 2)) + 240,
				((_screenRect.height / 2) - (y + this._anchor.y)) + 160);

	}

	public Vector2f getAnchor() {
		return this._anchor;
	}

	public void setAnchor(Vector2f v) {
		this._anchor = v;
	}

	public LColor getColor() {
		return _color;
	}

	public float getAlpha() {
		return _objectAlpha;
	}

	public float getOpacity() {
		return _objectAlpha * 255;
	}

	public void setOpacity(float o) {
		this._objectAlpha = o / 255f;
	}

	public void setAlpha(float v) {
		this._objectAlpha = v;
		this._color.a = this._objectAlpha;
	}

	public LNNode getParent() {
		return this._objectSuper;
	}

	public void setParent(LNNode v) {
		this._objectSuper = v;
	}

	public Vector2f getPosition() {
		return this._objectLocation;
	}

	public void position(Vector2f v) {
		this._objectLocation.set(v);
	}

	public float getRotation() {
		return MathUtils.toDegrees(this._objectRotation);
	}

	public final void setRotation(float v) {
		this._objectRotation = MathUtils.toRadians(v);
	}

	public final Vector2f getScale() {
		return this._scale;
	}

	public final void setScale(Vector2f value) {
		this._scale.set(value);
	}

	public final void setScale(float x, float y) {
		this._scale.set(x, y);
	}

	public final float getScaleX() {
		return this._scale.x;
	}

	public final void setScaleX(float value) {
		this._scale.x = value;
	}

	public final float getScaleY() {
		return this._scale.y;
	}

	public final void setScaleY(float value) {
		this._scale.y = value;
	}

	public final int getZOrder() {
		return this._objectLayer;
	}

	public final void setZOrder(int value) {
		this._objectLayer = value;
	}

	public int getScreenWidth() {
		return _screenRect.width;
	}

	public int getScreenHeight() {
		return _screenRect.height;
	}

	@Override
	public float getWidth() {
		return (_size_width * scale[0]) - _fixedWidthOffset;
	}

	@Override
	public float getHeight() {
		return (_size_height * scale[1]) - _fixedHeightOffset;
	}

	public void moveCamera(float x, float y) {
		if (!this._limitMove) {
			setLocation(x, y);
			return;
		}
		int tempX = (int) x;
		int tempY = (int) y;
		int tempWidth = (int) (getWidth() - _screenRect.width);
		int tempHeight = (int) (getHeight() - _screenRect.height);

		int limitX = tempX + tempWidth;
		int limitY = tempY + tempHeight;

		if (_size_width >= _screenRect.width) {
			if (limitX > tempWidth) {
				tempX = (int) (_screenRect.width - _size_width);
			} else if (limitX < 1) {
				tempX = _objectLocation.x();
			}
		} else {
			return;
		}
		if (_size_height >= _screenRect.height) {
			if (limitY > tempHeight) {
				tempY = (int) (_screenRect.height - _size_height);
			} else if (limitY < 1) {
				tempY = _objectLocation.y();
			}
		} else {
			return;
		}
		this.cam_x = tempX;
		this.cam_y = tempY;
		this.setLocation(cam_x, cam_y);
	}

	protected boolean isNotMoveInScreen(float x, float y) {
		if (!this._limitMove) {
			return false;
		}
		int width = (int) (getWidth() - _screenRect.width);
		int height = (int) (getHeight() - _screenRect.height);
		int limitX = (int) x + width;
		int limitY = (int) y + height;
		if (getWidth() >= _screenRect.width) {
			if (limitX >= width - 1) {
				return true;
			} else if (limitX <= 1) {
				return true;
			}
		} else {
			if (!_screenRect.contains(x, y, getWidth(), getHeight())) {
				return true;
			}
		}
		if (getHeight() >= _screenRect.height) {
			if (limitY >= height - 1) {
				return true;
			} else if (limitY <= 1) {
				return true;
			}
		} else {
			if (!_screenRect.contains(x, y, getWidth(), getHeight())) {
				return true;
			}
		}
		return false;
	}

	public boolean isContainer() {
		return true;
	}

	public boolean contains(float x, float y) {
		return contains(x, y, 0, 0);
	}

	public boolean contains(float x, float y, float width, float height) {
		return (this._visible) && (x >= pos[0] && y >= pos[1] && ((x + width) <= (pos[0] + getWidth()))
				&& ((y + height) <= (pos[1] + getHeight())));
	}

	public boolean intersects(float x1, float y1) {
		return (this._visible)
				&& (x1 >= pos[0] && x1 <= pos[0] + getWidth() && y1 >= pos[1] && y1 <= pos[1] + getHeight());
	}

	public boolean intersects(LNNode node) {
		float[] nodePos = node.convertToWorldPos();
		return (this._visible) && (node._visible)
				&& (pos[0] + getWidth() >= nodePos[0] && pos[0] <= nodePos[0] + node.getWidth()
						&& pos[1] + getWidth() >= nodePos[1] && pos[1] <= nodePos[1] + node.getHeight());
	}

	public boolean isVisible() {
		return this._visible;
	}

	public void setVisible(boolean visible) {
		if (this._visible == visible) {
			return;
		}
		this._visible = visible;
		if (_screen != null) {
			this._screen.setNodeStat(this, this._visible);
		}
	}

	public boolean isEnabled() {
		return (this._objectSuper == null) ? this._enabled : (this._enabled && this._objectSuper.isEnabled());
	}

	public void setEnabled(boolean b) {
		if (this._enabled == b) {
			return;
		}
		this._enabled = b;
		this._screen.setNodeStat(this, this._enabled);
	}

	public boolean isSelected() {
		if (!_selected) {
			for (int i = 0; i < this._childCount; i++) {
				if (this.childs[i].isSelected()) {
					return true;
				}
			}
			return false;

		} else {
			return true;
		}
	}

	public final void setSelected(boolean b) {
		this._selected = b;
	}

	public boolean requestFocus() {
		return this._screen.selectNode(this);
	}

	public void transferFocus() {
		if (this.isSelected() && this._objectSuper != null) {
			this._objectSuper.transferFocus(this);
		}
	}

	public void transferFocusBackward() {
		if (this.isSelected() && this._objectSuper != null) {
			this._objectSuper.transferFocusBackward(this);
		}
	}

	public boolean isFocusable() {
		return this._focusable;
	}

	public void setFocusable(boolean b) {
		this._focusable = b;
	}

	public LNNode getContainer() {
		return this._objectSuper;
	}

	final void setContainer(LNNode node) {
		this._objectSuper = node;
		this.validatePosition();
	}

	public final void setScreen(NodeScreen s) {
		if (s == _screen) {
			return;
		}
		this._screen = s;
		this._input = s.getInput();
	}

	public void setBounds(float dx, float dy, int width, int height) {
		setLocation(dx, dy);
		if (this._size_width != width || this._size_height != height) {
			this._size_width = width;
			this._size_height = height;
			if (width == 0) {
				width = 1;
			}
			if (height == 0) {
				height = 1;
			}
			this.validateSize();
		}
	}

	@Override
	public float getX() {
		return _objectLocation.x;
	}

	@Override
	public float getY() {
		return _objectLocation.y;
	}

	@Override
	public void setX(Integer x) {
		if (this._objectLocation.x != x || x == 0) {
			this._objectLocation.x = x;
			this.validatePosition();
		}
	}

	@Override
	public void setX(float x) {
		if (this._objectLocation.x != x || x == 0) {
			this._objectLocation.x = x;
			this.validatePosition();
		}
	}

	@Override
	public void setY(Integer y) {
		if (this._objectLocation.y != y || y == 0) {
			this._objectLocation.y = y;
			this.validatePosition();
		}
	}

	@Override
	public void setY(float y) {
		if (this._objectLocation.y != y || y == 0) {
			this._objectLocation.y = y;
			this.validatePosition();
		}
	}

	@Override
	public void setLocation(Vector2f _objectLocation) {
		setLocation(_objectLocation.x, _objectLocation.y);
	}

	@Override
	public void setLocation(float dx, float dy) {
		if (this._objectLocation.x != dx || this._objectLocation.y != dy || dx == 0 || dy == 0) {
			this._objectLocation.set(dx, dy);
			this.validatePosition();
		}
	}

	@Override
	public void move(float dx, float dy) {
		if (dx != 0 || dy != 0) {
			if (dx > -100 && dx < 100 && dy > -100 && dy < 100) {
				if (_objectSuper != null && _limitMove) {
					if (_objectSuper.contains((int) (pos[0] + dx), (int) (pos[1] + dy), _size_width, _size_height)) {
						this._objectLocation.move(dx, dy);
						this.validatePosition();
					}
				} else {
					this._objectLocation.move(dx, dy);
					this.validatePosition();
				}
			}
		}
	}

	public void setSize(int w, int h) {
		if (this._size_width != w || this._size_height != h) {
			this._size_width = w;
			this._size_height = h;
			if (this._size_width == 0) {
				this._size_width = 1;
			}
			if (this._size_height == 0) {
				this._size_height = 1;
			}
			this.validateSize();
		}
	}

	public void validatePosition() {
		if (_isClose) {
			return;
		}
		if (_objectSuper != null) {
			this._screenX = (int) pos[0];
			this._screenY = (int) pos[1];
		} else {
			this._screenX = _objectLocation.x();
			this._screenY = _objectLocation.y();
		}
		for (int i = 0; i < this._childCount; i++) {
			if (this.childs[i] != null) {
				this.childs[i].validatePosition();
			}
		}
	}

	@Override
	public float getCenterX() {
		return getX() + getWidth() / 2f;
	}

	@Override
	public float getCenterY() {
		return getY() + getHeight() / 2f;
	}

	private RectBox temp_rect;

	public RectBox getRectBox() {
		if (_objectRotation != 0) {
			int[] result = MathUtils.getLimit(_objectLocation.getX(), _objectLocation.getY(), getWidth(), getHeight(),
					MathUtils.toDegrees(_objectRotation));
			if (temp_rect == null) {
				temp_rect = new RectBox(result[0], result[1], result[2], result[3]);
			} else {
				temp_rect.setBounds(result[0], result[1], result[2], result[3]);
			}
		} else {
			if (temp_rect == null) {
				temp_rect = new RectBox(_objectLocation.getX(), _objectLocation.getY(), getWidth(), getHeight());
			} else {
				temp_rect.setBounds(_objectLocation.getX(), _objectLocation.getY(), getWidth(), getHeight());
			}
		}
		return temp_rect;
	}

	public int getScreenX() {
		return this._screenX;
	}

	public int getScreenY() {
		return this._screenY;
	}

	@Override
	public boolean showShadow() {
		return false;
	}

	public void processTouchPressed() {
		if (!_visible || !_enabled) {
			return;
		}
		if (Click != null) {
			Click.DownClick(this, SysTouch.getX(), SysTouch.getY());
		}
	}

	public void processTouchReleased() {
		if (!_visible || !_enabled) {
			return;
		}
		if (Click != null) {
			Click.UpClick(this, SysTouch.getX(), SysTouch.getY());
		}
	}

	public void processTouchDragged() {
		if (!_visible || !_enabled) {
			return;
		}
		if (!_locked && _input != null) {
			if (getContainer() != null) {
				getContainer().sendToFront(this);
			}
			this.move(this._input.getTouchDX(), this._input.getTouchDY());
		}
		if (Click != null) {
			Click.DragClick(this, SysTouch.getX(), SysTouch.getY());
		}
	}

	public void processKeyPressed() {

	}

	public void processKeyReleased() {
	}

	public final void keyPressed() {
		this.checkFocusKey();
		this.processKeyPressed();
	}

	void checkFocusKey() {
		if (_input != null && this._input.getKeyPressed() == SysKey.ENTER) {
			this.transferFocus();
		} else {
			this.transferFocusBackward();
		}
	}

	public int getCamX() {
		return cam_x == 0 ? _objectLocation.x() : cam_x;
	}

	public int getCamY() {
		return cam_y == 0 ? _objectLocation.y() : cam_y;
	}

	public boolean isClosed() {
		return _isClose;
	}

	public void setAutoDestroy(boolean flag) {
		this._autoDestroy = flag;
	}

	public boolean isAutoDestory() {
		return _autoDestroy;
	}

	@Override
	public Field2D getField2D() {
		return null;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public boolean inContains(float x, float y, float w, float h) {
		return getCollisionBox().contains(x, y, w, h);
	}

	public RectBox getCollisionBox() {
		return getRect(getLocation().x(), getLocation().y(), getWidth(), getHeight());
	}

	@Override
	public void createUI(GLEx g) {
		drawNode(g);
	}

	@Override
	public void createUI(GLEx g, float offsetX, float offsetY) {
		float ox = this.getX();
		float oy = this.getY();
		this.setLocation(offsetX + ox, offsetY + oy);
		drawNode(g);
		this.setLocation(ox, oy);
	}

	@Override
	public LTexture getBitmap() {
		return null;
	}

	@Override
	public void setParent(ISprite s) {
		if (s instanceof LNNode) {
			setContainer((LNNode) s);
		} else if (s instanceof ISprite) {
			setContainer(new SpriteToNode(s));
		}
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
	public ISprite setSprites(Sprites ss) {
		if (this._sprites == ss) {
			return this;
		}
		this._sprites = ss;
		return this;
	}

	@Override
	public Sprites getSprites() {
		return this._sprites;
	}

	@Override
	public Screen getScreen() {
		if (this._sprites == null) {
			return LSystem.getProcess().getScreen();
		}
		return this._sprites.getScreen() == null ? LSystem.getProcess().getScreen() : this._sprites.getScreen();
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

	public ResizeListener<LNNode> getResizeListener() {
		return _resizeListener;
	}

	public LNNode setResizeListener(ResizeListener<LNNode> listener) {
		this._resizeListener = listener;
		return this;
	}

	@Override
	public void onResize() {
		if (_resizeListener != null) {
			_resizeListener.onResize(this);
		}
		if (childs != null) {
			for (int i = this.childs.length - 1; i >= 0; i--) {
				final LNNode child = this.childs[i];
				if (child != null && child != this) {
					child.onResize();
				}
			}
		}
	}

	@Override
	public boolean autoXYSort() {
		return false;
	}

	@Override
	public void close() {
		this._isClose = true;
		if (this._objectSuper != null) {
			this._objectSuper.removeNode(this);
		}
		this._selected = false;
		this._visible = false;
		if (_screen != null) {
			this._screen.setNodeStat(this, false);
		}
		if (_autoDestroy) {
			if (childs != null) {
				for (LNNode c : childs) {
					if (c != null) {
						c.close();
					}
				}
			}
		}
		setState(State.DISPOSED);
		removeActionEvents(this);
		_resizeListener = null;
	}

}
