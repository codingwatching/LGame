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

import loon.LRelease;
import loon.action.ActionBind;
import loon.action.collision.CollisionFilter;
import loon.action.collision.CollisionResult;
import loon.action.collision.CollisionWorld;
import loon.action.map.Config;
import loon.action.map.Field2D;
import loon.action.map.HexagonMap;
import loon.action.map.TileMap;
import loon.geom.ActionBindRect;
import loon.utils.processes.GameProcessType;
import loon.utils.processes.RealtimeProcess;
import loon.utils.processes.RealtimeProcessManager;
import loon.utils.timer.LTimerContext;

/**
 * 一个四方向(八方向)运动的控制器,主要用来键盘或虚拟摇杆控制角色移动
 */
public class MoveControl implements LRelease {

	private float _moveSpeed = 1f;

	private float _offsetX = 0f;

	private float _offsetY = 0f;

	private float _lastX;

	private float _lastY;

	private float _vagueWidthScale, _vagueHeightScale;

	private int _direction = -1, _lastDirection = -1;

	private boolean _isMoving = false, _running = false, _freeDir = true, _closed = false;

	private CollisionWorld _collisionWorld;

	private CollisionFilter _worldCollisionFilter;

	private ActionBind _bindObject;

	private ActionBindRect _actionRect;

	private Field2D _currentArrayMap;

	private long _delay = 0;

	public MoveControl(ActionBind bind, TileMap map) {
		this(bind, map.getField2D());
	}

	public MoveControl(ActionBind bind, HexagonMap map) {
		this(bind, map.getField2D());
	}

	public MoveControl(ActionBind bind, int[][] map) {
		this(bind, new Field2D(map));
	}

	public MoveControl(ActionBind bind) {
		this(bind, (Field2D) null);
	}

	public MoveControl(ActionBind bind, Field2D field2d) {
		this(bind, field2d, 8, 30, 1f, 1f);
	}

	public MoveControl(ActionBind bind, Field2D field2d, int moveSpeed, int delay, float ws, float hs) {
		this._bindObject = bind;
		this._currentArrayMap = field2d;
		this._actionRect = new ActionBindRect(bind);
		this._delay = delay;
		this._moveSpeed = moveSpeed;
		this._vagueWidthScale = ws;
		this._vagueHeightScale = hs;
	}

	public MoveControl upIso() {
		return this.setDirection(Config.UP);
	}

	public MoveControl tup() {
		return this.setDirection(Config.TUP);
	}

	public MoveControl downIso() {
		return this.setDirection(Config.DOWN);
	}

	public MoveControl tdown() {
		return this.setDirection(Config.TDOWN);
	}

	public MoveControl leftIso() {
		return this.setDirection(Config.LEFT);
	}

	public MoveControl tleft() {
		return this.setDirection(Config.TLEFT);
	}

	public MoveControl rightIso() {
		return this.setDirection(Config.RIGHT);
	}

	public MoveControl tright() {
		return this.setDirection(Config.TRIGHT);
	}

	public MoveControl setDirection(int d) {
		this._direction = d;
		return this;
	}

	public MoveControl resetDirection() {
		return setDirection(-1);
	}

	public int getDirection() {
		return this._direction;
	}

	public final MoveControl call() {
		move(_bindObject, _currentArrayMap, _direction);
		return this;
	}

	public MoveControl start() {
		return submit();
	}

	public MoveControl submit() {
		if (!_running) {
			final RealtimeProcess process = new RealtimeProcess() {

				@Override
				public void run(LTimerContext time) {
					if (_running) {
						call();
						if (_freeDir) {
							resetDirection();
						}
					} else {
						kill();
					}
				}
			};
			process.setProcessType(GameProcessType.Progress);
			process.setDelay(_delay);
			_running = true;
			RealtimeProcessManager.get().addProcess(process);
		}
		return this;
	}

	public MoveControl stop() {
		this._running = false;
		return this;
	}

	protected final boolean checkTileCollision(Field2D field2d, ActionBind bind, float newX, float newY) {
		if (field2d == null) {
			return false;
		}
		return field2d.checkTileCollision(bind.getX() - _offsetX, bind.getY() - _offsetY,
				bind.getWidth() * _vagueWidthScale, bind.getHeight() * _vagueHeightScale, newX, newY);
	}

	public final boolean move(ActionBind bind, Field2D field2d, int direction) {
		float startX = bind.getX() - _offsetX;
		float startY = bind.getY() - _offsetY;
		float newX = 0;
		float newY = 0;
		_isMoving = true;
		switch (direction) {
		case Field2D.TUP:
			newY = startY - _moveSpeed;
			if (!checkTileCollision(field2d, bind, startX, newY)) {
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.TDOWN:
			newY = startY + _moveSpeed;
			if (!checkTileCollision(field2d, bind, startX, newY)) {
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.TLEFT:
			newX = startX - _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, startY)) {
				startX = newX;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.TRIGHT:
			newX = startX + _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, startY)) {
				startX = newX;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.UP:
			newX = startX + _moveSpeed;
			newY = startY - _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, newY)) {
				startX = newX;
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.DOWN:
			newX = startX - _moveSpeed;
			newY = startY + _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, newY)) {
				startX = newX;
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.LEFT:
			newX = startX - _moveSpeed;
			newY = startY - _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, newY)) {
				startX = newX;
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		case Field2D.RIGHT:
			newX = startX + _moveSpeed;
			newY = startY + _moveSpeed;
			if (!checkTileCollision(field2d, bind, newX, newY)) {
				startX = newX;
				startY = newY;
			} else {
				_isMoving = false;
			}
			break;
		}
		if (_isMoving) {
			movePos(bind, startX, startY, direction);
		}
		return true;
	}

	public MoveControl movePos(ActionBind bind, float x, float y, int dir) {
		return movePos(bind, x, y, -1f, -1f, dir);
	}

	public MoveControl movePos(ActionBind bind, float x, float y, float lastX, float lastY, int dir) {
		if (bind == null) {
			return this;
		}
		if (_collisionWorld != null) {
			if (_worldCollisionFilter == null) {
				_worldCollisionFilter = CollisionFilter.getDefault();
			}
			_actionRect.setRect(bind.getX() - _offsetX, bind.getY() - _offsetY, bind.getWidth() * _vagueWidthScale,
					bind.getHeight() * _vagueHeightScale);
			CollisionResult.Result result = _collisionWorld.move(_actionRect, x, y, _worldCollisionFilter);
			if (lastX != -1 && lastY != -1) {
				if (result.goalX != x || result.goalY != y) {
					bind.setLocation(lastX + _offsetX, lastY + _offsetY);
				} else {
					bind.setLocation(result.goalX + _offsetY, result.goalY + _offsetY);
				}
			} else {
				bind.setLocation(result.goalX + _offsetX, result.goalY + _offsetY);
			}
		} else {
			bind.setLocation(x + _offsetX, y + _offsetY);
		}
		_lastX = bind.getX() - _offsetX;
		_lastY = bind.getY() - _offsetY;
		_lastDirection = dir;
		return this;
	}

	public boolean isMoving() {
		return _isMoving;
	}

	public float getSpeed() {
		return _moveSpeed;
	}

	public MoveControl setSpeed(float s) {
		this._moveSpeed = s;
		return this;
	}

	public boolean isRunning() {
		return _running;
	}

	public boolean isFreeDir() {
		return _freeDir;
	}

	public MoveControl setFreeDir(boolean d) {
		this._freeDir = d;
		return this;
	}

	public boolean isWN() {
		return _lastDirection == Config.WN;
	}

	public boolean isW() {
		return _lastDirection == Config.W;
	}

	public boolean isSW() {
		return _lastDirection == Config.SW;
	}

	public boolean isS() {
		return _lastDirection == Config.S;
	}

	public boolean isNE() {
		return _lastDirection == Config.NE;
	}

	public boolean isN() {
		return _lastDirection == Config.N;
	}

	public boolean isES() {
		return _lastDirection == Config.ES;
	}

	public boolean isE() {
		return _lastDirection == Config.E;
	}

	public boolean isLeft() {
		return _lastDirection == Config.LEFT;
	}

	public boolean isRight() {
		return _lastDirection == Config.RIGHT;
	}

	public boolean isDown() {
		return _lastDirection == Config.DOWN;
	}

	public boolean isUp() {
		return _lastDirection == Config.UP;
	}

	public boolean isTLeft() {
		return _lastDirection == Config.TLEFT;
	}

	public boolean isTRight() {
		return _lastDirection == Config.TRIGHT;
	}

	public boolean isTDown() {
		return _lastDirection == Config.TDOWN;
	}

	public boolean isTUp() {
		return _lastDirection == Config.TUP;
	}

	public long getDelay() {
		return _delay;
	}

	public MoveControl setDelay(long delay) {
		this._delay = delay;
		return this;
	}

	public float getLastX() {
		return _lastX;
	}

	public float getLastY() {
		return _lastY;
	}

	public float getOffsetX() {
		return _offsetX;
	}

	public MoveControl setOffsetX(float offsetX) {
		this._offsetX = offsetX;
		return this;
	}

	public float getOffsetY() {
		return _offsetY;
	}

	public MoveControl setOffsetY(float offsetY) {
		this._offsetY = offsetY;
		return this;
	}

	public CollisionFilter getCollisionFilter() {
		return _worldCollisionFilter;
	}

	public MoveControl setCollisionFilter(CollisionFilter filter) {
		this._worldCollisionFilter = filter;
		return this;
	}

	public CollisionWorld getCollisionWorld() {
		return _collisionWorld;
	}

	public MoveControl setCollisionWorld(CollisionWorld world) {
		this._collisionWorld = world;
		return this;
	}

	public MoveControl setVagueScale(float scale) {
		this.setVagueScale(scale, scale);
		return this;
	}

	public MoveControl setVagueScale(float ws, float hs) {
		this.setVagueWidthScale(ws);
		this.setVagueHeightScale(hs);
		return this;
	}

	public float getVagueWidthScale() {
		return _vagueWidthScale;
	}

	public MoveControl setVagueWidthScale(float ws) {
		this._vagueWidthScale = ws;
		return this;
	}

	public float getVagueHeightScale() {
		return _vagueHeightScale;
	}

	public MoveControl setVagueHeightScale(float hs) {
		this._vagueHeightScale = hs;
		return this;
	}

	public boolean isClosed() {
		return this._closed;
	}

	@Override
	public void close() {
		_running = false;
		_closed = true;
	}

}
