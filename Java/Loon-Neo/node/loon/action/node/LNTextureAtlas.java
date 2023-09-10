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

import loon.LTexture;
import loon.action.sprite.SpriteBatch;
import loon.canvas.LColor;
import loon.geom.RectBox;
import loon.geom.Vector2f;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.TArray;

public class LNTextureAtlas {

	private TArray<RectBox> _rectList;

	private LTexture _texture;

	private int _totalRect;

	public Vector2f anchor = new Vector2f();

	public LNTextureAtlas(LTexture texture, int capacity) {
		this._texture = texture;
		this._totalRect = capacity;
		this._rectList = new TArray<RectBox>();
	}

	public void addRect(RectBox rect) {
		this._rectList.add(rect);
	}

	public void draw(int idx, SpriteBatch batch, Vector2f absPos, float rotation, Vector2f scale, LColor color) {
		RectBox rect = this._rectList.get(idx);
		batch.setColor(color);
		batch.draw(_texture, absPos.x, absPos.y, anchor.x, anchor.y, rect.width, rect.height, scale.x, scale.y,
				MathUtils.toDegrees(rotation), rect.x, rect.y, rect.width, rect.height, false, false);
		batch.resetColor();
	}

	public void draw(int idx, SpriteBatch batch, float x, float y, float rotation, float sx, float sy, LColor color) {
		RectBox rect = this._rectList.get(idx);
		batch.setColor(color);
		batch.draw(_texture, x, y, anchor.x, anchor.y, rect.width, rect.height, sx, sy, MathUtils.toDegrees(rotation),
				rect.x, rect.y, rect.width, rect.height, false, false);
		batch.resetColor();
	}

	public void draw(int idx, GLEx g, Vector2f absPos, float rotation, Vector2f scale, LColor color) {
		RectBox rect = this._rectList.get(idx);
		g.draw(_texture, absPos.x, absPos.y, anchor, rect.width, rect.height, scale.x, scale.y,
				MathUtils.toDegrees(rotation), rect.x, rect.y, rect.width, rect.height, false, false, color);
	}

	public void draw(int idx, GLEx g, float x, float y, float rotation, float sx, float sy, LColor color) {
		RectBox rect = this._rectList.get(idx);
		g.draw(_texture, x, y, anchor, rect.width, rect.height, sx, sy, MathUtils.toDegrees(rotation), rect.x, rect.y,
				rect.width, rect.height, false, false, color);
	}

	public void resetRect() {
		this._rectList.clear();
	}

	public LTexture getTexture() {
		return this._texture;
	}

	public int getTotalRect() {
		return _totalRect;
	}
}
