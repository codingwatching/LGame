/**
 * 
 * Copyright 2014
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
 * @version 0.4.1
 * 
 *          此类用以用户名生成，由用户选择指定字符构成用户名。
 * 
 *          Examples1:
 * 
 *          TArray<String> list = new TArray<String>(); list.add("赵钱孙李周吴郑王");
 *          list.add("冯陈褚卫蒋沈韩杨"); list.add("朱秦尤许何吕施张"); list.add("孔曹严华金魏陶姜");
 *          list.add("<>"); LDecideName decideName = new LDecideName(list,0, 0);
 *          add(decideName);
 * 
 */
package loon.component;

import loon.LSystem;
import loon.LTexture;
import loon.canvas.LColor;
import loon.component.skin.MessageSkin;
import loon.component.skin.SkinManager;
import loon.font.FontSet;
import loon.font.IFont;
import loon.opengl.GLEx;
import loon.utils.TArray;
import loon.utils.timer.LTimer;
import loon.utils.MathUtils;

/**
 * 在一些不方便输入字符串的设备上,输入角色名称时可用此UI,字数不够上下分页,多来几个就成了……
 * 
 * Examples:
 * 
 * <pre>
 * TArray<String> list = new TArray<String>();
 * list.add("赵钱孙李周吴郑王");
 * list.add("冯陈褚卫蒋沈韩杨");
 * list.add("朱秦尤许何吕施张");
 * list.add("孔曹严华金魏陶姜");
 * list.add("<>");
 * LDecideName decideName = new LDecideName(list, 0, 0);
 * add(decideName);
 * </pre>
 */
public class LDecideName extends LComponent implements FontSet<LDecideName> {

	public static interface NameDecidedListener {
		void onNameDecided(String name);
	}

	private LTimer _countTimer = new LTimer(500);

	private LColor _fontColor = LColor.white;
	private LColor _selectedColor;
	private LColor _labelNameColor = LColor.orange;
	private IFont _font;

	private int _text_width_space = 5;
	private String _enterFlag;
	private String _name;
	private String _labelName;

	private int _cursorX = 0;
	private int _cursorY = 0;
	private TArray<String> _keyArrays;

	private boolean _showGrid = false;
	private float _dx = 0.1f;
	private float _dy = 0.1f;
	private float _labelOffsetX, _labelOffsetY;
	private int _maxNameString = 12;
	private int _leftOffset, _topOffset;
	private char _enterFlagString = '>';
	private char _clearFlagString = '<';

	private boolean _cursorVisible = true;

	private boolean _repeatActive = false;

	private int _repeatDx = 0, _repeatDy = 0;

	private NameDecidedListener _listener;

	public LDecideName(TArray<String> mes, int x, int y) {
		this(mes, x, y, 400, 250);
	}

	public LDecideName(String label, TArray<String> mes, int x, int y, int width, int height) {
		this(label, LSystem.EMPTY, mes, SkinManager.get().getMessageSkin().getFont(), x, y, width, height,
				SkinManager.get().getMessageSkin().getBackgroundTexture());
	}

	public LDecideName(String label, TArray<String> mes, int x, int y, int width, int height, LTexture bg) {
		this(label, LSystem.EMPTY, mes, SkinManager.get().getMessageSkin().getFont(), x, y, width, height, bg);
	}

	public LDecideName(TArray<String> mes, int x, int y, int width, int height) {
		this("Name:", LSystem.EMPTY, mes, SkinManager.get().getMessageSkin().getFont(), x, y, width, height,
				SkinManager.get().getMessageSkin().getBackgroundTexture());
	}

	public LDecideName(String label, String name, TArray<String> mes, IFont f, int x, int y, int width, int height,
			LTexture bg) {
		this(label, name, mes, f, x, y, width, height, bg, SkinManager.get().getMessageSkin().getFontColor());
	}

	public LDecideName(MessageSkin skin, String label, String name, TArray<String> mes, int x, int y, int width,
			int height) {
		this(label, name, mes, skin.getFont(), x, y, width, height, skin.getBackgroundTexture(), skin.getFontColor());
	}

	public LDecideName(String label, String name, TArray<String> mes, IFont f, int x, int y, int width, int height,
			LTexture bg, LColor color) {
		super(x, y, width, height - (f == null ? 0 : f.getHeight()) - 20);
		this._fontColor = color == null ? LColor.white : color;
		this._selectedColor = new LColor(0, 150, 0, 150);
		this.setFont(f);
		this.onlyBackground(bg);
		this._labelName = label;
		this._name = name == null ? LSystem.EMPTY : name;
		this._keyArrays = mes;
		this._leftOffset = (_font == null ? 12 : _font.getHeight()) + 15;
		this._topOffset = (_font == null ? 12 : _font.getHeight()) + 20;
	}

	@Override
	public void process(long elapsedTime) {
		if (_countTimer.action(elapsedTime)) {
			_cursorVisible = !_cursorVisible;
			if (_repeatActive) {
				moving(_repeatDx, _repeatDy);
			}
		}
	}

	public void draw(GLEx g, int x, int y) {
		final IFont oldFont = g.getFont();
		final int oldColor = g.color();
		if (_background != null) {
			g.draw(_background, x, y, getWidth(), getHeight());
		}
		if (_font != null) {
			g.setFont(_font);
		}
		float posX = x + _leftOffset;
		if (_labelName != null) {
			g.drawString(_labelName + this._name, posX + _labelOffsetX + _text_width_space,
					y + _labelOffsetY - _text_width_space / 2, _labelNameColor);
		}
		float posY = y + _topOffset;
		if (_keyArrays != null && _font != null) {
			for (int row = 0; row < this._keyArrays.size; row++) {
				String line = this._keyArrays.get(row);
				if (line == null) {
					continue;
				}
				for (int col = 0; col < line.length(); col++) {
					char ch = line.charAt(col);
					if (ch != '　' && ch != LSystem.SPACE) {
						float drawX = posX + MathUtils.round((col * _dx + 0.01f) * getWidth()) + _text_width_space;
						float drawY = posY + MathUtils.round(((row + 1) * _dy - 0.01f) * getHeight())
								- _font.getAscent() - _text_width_space / 2;
						g.drawString(String.valueOf(ch), drawX, drawY, _fontColor);
						if (_showGrid) {
							g.drawRect(posX + MathUtils.round((col * _dx) * getWidth()),
									posY + MathUtils.round((row * _dy) * getHeight()),
									MathUtils.round(_dx * getWidth()), MathUtils.round(_dy * getHeight()),
									_selectedColor);
						}
					}
				}
			}
		}
		if (_cursorVisible && _keyArrays != null && _keyArrays.size > 0) {
			if (_cursorY >= 0 && _cursorY < _keyArrays.size) {
				String row0 = _keyArrays.get(0);
				int cols = row0 == null ? 0 : row0.length();
				if (cols > 0 && _cursorX >= 0) {
					String curRow = _keyArrays.get(_cursorY);
					if (curRow != null && _cursorX < curRow.length()) {
						g.fillRect(posX + MathUtils.round((this._cursorX * _dx) * getWidth()) - 1,
								posY + MathUtils.round((this._cursorY * _dy) * getHeight()) - 1,
								MathUtils.round(_dx * getWidth()) + 2, MathUtils.round(_dy * getHeight()) + 2,
								_selectedColor);
					}
				}
			}
		}
		g.setFont(oldFont);
		g.setColor(oldColor);
	}

	public LColor getLabelNameColor() {
		return _labelNameColor;
	}

	public LDecideName setLabelNameColor(LColor c) {
		_labelNameColor = c;
		return this;
	}

	@Override
	public void upClick() {
		super.upClick();
		this.pushEnter();
	}

	private char getCharAt(int row, int col) {
		if (_keyArrays == null) {
			return LSystem.SPACE;
		}
		if (row < 0 || row >= _keyArrays.size) {
			return LSystem.SPACE;
		}
		String line = _keyArrays.get(row);
		if (line == null || col < 0 || col >= line.length()) {
			return LSystem.SPACE;
		}
		return line.charAt(col);
	}

	private char normalizeChar(char c) {
		if (c == '　') {
			return LSystem.SPACE;
		}
		if (c >= 65281 && c <= 65374) {
			return (char) (c - 65248);
		}
		return c;
	}

	public int pushEnter() {
		char current = getCharAt(this._cursorY, this._cursorX);
		current = normalizeChar(current);
		if (current == _enterFlagString) {
			if (this._name == null || this._name.equals(LSystem.EMPTY)) {
				return -2;
			}
			_enterFlag = "Enter";
			if (_listener != null) {
				_listener.onNameDecided(this._name);
			}
			return -1;
		}
		if (current == _clearFlagString) {
			if (this._name != null && !this._name.equals(LSystem.EMPTY)) {
				this._name = this._name.substring(0, this._name.length() - 1);
			}
			_enterFlag = "Clear";
		} else if (current != LSystem.SPACE && current != '　') {
			if (this._name == null) {
				this._name = LSystem.EMPTY;
			}
			if (this._name.length() < _maxNameString) {
				this._name += current;
				_enterFlag = "Add";
			}
		}
		return -2;
	}

	public int pushEscape() {
		return -1;
	}

	/**
	 * 强制坐标向左移动
	 * 
	 * @return
	 */
	public int pushLeft() {
		moving(-1, 0);
		return -2;
	}

	/**
	 * 强制坐标向右移动
	 * 
	 * @return
	 */
	public int pushRight() {
		moving(1, 0);
		return -2;
	}

	/**
	 * 强制坐标向下移动
	 * 
	 * @return
	 */
	public int pushDown() {
		moving(0, 1);
		return -2;
	}

	/**
	 * 强制坐标向上移动
	 * 
	 * @return
	 */
	public int pushUp() {
		moving(0, -1);
		return -2;
	}

	private void moving(int dx, int dy) {
		if (_keyArrays == null || _keyArrays.size == 0) {
			return;
		}
		int rows = _keyArrays.size;
		int newX = this._cursorX + dx;
		int newY = this._cursorY + dy;
		if (newY >= rows) {
			newY = 0;
		}
		if (newY < 0) {
			newY = rows - 1;
		}
		String targetRow = _keyArrays.get(newY);
		int cols = targetRow == null ? 0 : targetRow.length();
		if (cols == 0) {
			int attempts = rows;
			int ty = newY;
			while (attempts-- > 0) {
				ty += dy;
				if (ty >= rows) {
					ty = 0;
				}
				if (ty < 0) {
					ty = rows - 1;
				}
				String r = _keyArrays.get(ty);
				if (r != null && r.length() > 0) {
					newY = ty;
					targetRow = r;
					cols = r.length();
					break;
				}
			}
			if (cols == 0) {
				return;
			}
		}
		if (newX >= cols) {
			newX = 0;
		}
		if (newX < 0) {
			newX = cols - 1;
		}
		int maxCols = 0;
		for (int i = 0; i < _keyArrays.size; i++) {
			String s = _keyArrays.get(i);
			if (s != null && s.length() > maxCols) {
				maxCols = s.length();
			}
		}
		int attempts = rows * (maxCols == 0 ? 1 : maxCols);
		int tx = newX;
		int ty = newY;
		while (attempts-- > 0) {
			char c = getCharAt(ty, tx);
			if (c != LSystem.SPACE && c != '　') {
				this._cursorX = tx;
				this._cursorY = ty;
				return;
			}
			tx += dx;
			ty += dy;
			if (ty >= rows) {
				ty = 0;
			}
			if (ty < 0) {
				ty = rows - 1;
			}
			String rowStr = _keyArrays.get(ty);
			int rowCols = rowStr == null ? 0 : rowStr.length();
			if (rowCols == 0) {
				int innerAttempts = rows;
				int ttt = ty;
				while (innerAttempts-- > 0) {
					ttt += dy;
					if (ttt >= rows) {
						ttt = 0;
					}
					if (ttt < 0) {
						ttt = rows - 1;
					}
					String rr = _keyArrays.get(ttt);
					if (rr != null && rr.length() > 0) {
						ty = ttt;
						rowCols = rr.length();
						break;
					}
				}
				if (rowCols == 0) {
					break;
				}
			}
			if (tx >= rowCols) {
				tx = 0;
			}
			if (tx < 0) {
				tx = rowCols - 1;
			}
		}
	}

	public void moveCursor(float x, float y) {
		if (_keyArrays == null || _keyArrays.size == 0) {
			return;
		}
		int cellW = MathUtils.round(_dx * getWidth());
		int cellH = MathUtils.round(_dy * getHeight());
		if (cellW <= 0 || cellH <= 0) {
			return;
		}
		int relX = MathUtils.round(x) - _leftOffset;
		int relY = MathUtils.round(y) - _topOffset;
		if (relX < 0 || relY < 0) {
			return;
		}
		final int indexX = relX / cellW;
		final int indexY = relY / cellH;
		if (indexY < 0 || indexY >= this._keyArrays.size) {
			return;
		}
		String row = this._keyArrays.get(indexY);
		if (row == null) {
			return;
		}
		if (indexX < 0 || indexX >= row.length()) {
			return;
		}
		char ch = getCharAt(indexY, indexX);
		if (ch != '　' && ch != LSystem.SPACE) {
			this._cursorX = indexX;
			this._cursorY = indexY;
		}
	}

	public void startRepeatMove(int dx, int dy) {
		this._repeatDx = dx;
		this._repeatDy = dy;
		this._repeatActive = true;
		_countTimer.reset();
	}

	public void stopRepeatMove() {
		this._repeatActive = false;
		_countTimer.stop();
	}

	public void setBlinkInterval(long millis) {
		_countTimer.setDelay(millis);
	}

	public void setSelectColor(LColor selectColor) {
		this._selectedColor = selectColor;
	}

	@Override
	public LColor getFontColor() {
		return this._fontColor;
	}

	@Override
	public LDecideName setFontColor(LColor fontColor) {
		this._fontColor = fontColor;
		return this;
	}

	public String getLabelName() {
		return _labelName;
	}

	public void setLabelName(String labelName) {
		this._labelName = labelName;
	}

	@Override
	public void createUI(GLEx g, int x, int y) {
		moveCursor(getUITouchX(), getUITouchY());
		draw(g, x, y);
	}

	public int getCursorX() {
		return _cursorX;
	}

	public void setCursorX(int cursorX) {
		this._cursorX = cursorX;
	}

	public int getCursorY() {
		return _cursorY;
	}

	public void setCursorY(int cursorY) {
		this._cursorY = cursorY;
	}

	public float getDx() {
		return _dx;
	}

	public void setDx(float dx) {
		this._dx = dx;
	}

	public float getDy() {
		return _dy;
	}

	public void setDy(float dy) {
		this._dy = dy;
	}

	public String getEnterFlag() {
		return _enterFlag;
	}

	public String getDecideName() {
		return _name;
	}

	public void setEnterFlag(String enterFlag) {
		this._enterFlag = enterFlag;
	}

	public LTexture getBgTexture() {
		return _background;
	}

	public void setBgTexture(LTexture bgTexture) {
		this.setBackground(bgTexture);
	}

	public int getMaxNameString() {
		return _maxNameString;
	}

	public void setMaxNameString(int maxNameString) {
		this._maxNameString = maxNameString;
	}

	public char getEnterFlagString() {
		return _enterFlagString;
	}

	public void setEnterFlagString(char enterFlagString) {
		this._enterFlagString = enterFlagString;
	}

	public char getClearFlagString() {
		return _clearFlagString;
	}

	public void setClearFlagString(char clearFlagString) {
		this._clearFlagString = clearFlagString;
	}

	public int getLeftOffset() {
		return _leftOffset;
	}

	public void setLeftOffset(int leftOffset) {
		this._leftOffset = leftOffset;
	}

	public int getTopOffset() {
		return _topOffset;
	}

	public void setTopOffset(int topOffset) {
		this._topOffset = topOffset;
	}

	@Override
	public LDecideName setFont(IFont font) {
		if (font == null) {
			return this;
		}
		this._font = font;
		return this;
	}

	@Override
	public IFont getFont() {
		return _font;
	}

	public float getLabelOffsetX() {
		return _labelOffsetX;
	}

	public void setLabelOffsetX(float x) {
		this._labelOffsetX = x;
	}

	public float getLabelOffsetY() {
		return _labelOffsetY;
	}

	public void setLabelOffsetY(float y) {
		this._labelOffsetY = y;
	}

	public boolean isShowGrid() {
		return _showGrid;
	}

	public LDecideName setShowGrid(boolean showGrid) {
		this._showGrid = showGrid;
		return this;
	}

	public int getTextWidthSpace() {
		return _text_width_space;
	}

	public LDecideName setTextWidthSpace(int tws) {
		this._text_width_space = tws;
		return this;
	}

	public void setKeyArrays(TArray<String> keyArrays) {
		this._keyArrays = keyArrays;
		if (_keyArrays != null && _keyArrays.size > 0) {
			if (_cursorY >= _keyArrays.size) {
				_cursorY = 0;
			}
			String row = _keyArrays.get(_cursorY);
			if (row == null || _cursorX >= row.length()) {
				for (int r = 0; r < _keyArrays.size; r++) {
					String s = _keyArrays.get(r);
					if (s != null && s.length() > 0) {
						_cursorY = r;
						_cursorX = 0;
						break;
					}
				}
			}
		}
	}

	public TArray<String> getKeyArrays() {
		return _keyArrays;
	}

	public void setNameDecidedListener(NameDecidedListener l) {
		this._listener = l;
	}

	@Override
	public String getUIName() {
		return "DecideName";
	}

	@Override
	public void destroy() {

	}

}
