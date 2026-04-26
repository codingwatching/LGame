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
import loon.canvas.Canvas;
import loon.canvas.Image;
import loon.canvas.LColor;
import loon.component.skin.SelectSkin;
import loon.component.skin.SkinManager;
import loon.events.EventActionN;
import loon.events.SysKey;
import loon.font.FontSet;
import loon.font.IFont;
import loon.font.LFont;
import loon.opengl.GLEx;
import loon.opengl.LSTRDictionary;
import loon.utils.MathUtils;
import loon.utils.StringUtils;
import loon.utils.TArray;
import loon.utils.timer.LTimer;

/**
 * 一个选项器UI,与LMenuSelect的最大区别在于这个的UI大小是固定的,而LMenuSelect会随着注入的内容不同而自行改变UI大小
 */
public class LSelect extends LContainer implements FontSet<LSelect> {

	public static interface OnSelectListener {
		/**
		 * 选中项改变
		 */
		void onSelectionChanged(LSelect select, int index, String item);

		/**
		 * 确认选择
		 */
		void onItemConfirmed(LSelect select, int index, String item);
	}

	private LTexture _tempTexture = null;
	private IFont _messageFont;
	private LColor _fontColor = LColor.white;
	private LColor _cursorColor = LColor.white;
	private int _left, _top, _nTop;
	private int _sizeFont, _doubleSizeFont, _tmpOffset;
	private int _messageLeft, _nLeft, _messageTop, _selectSize, _selectFlag;
	private int _space;
	private float _autoAlpha;
	private LTimer _delay;
	private LTimer _confirmDelay;

	private boolean _pendingConfirm;
	private String[] _selects;
	private String _message, _result;
	private LTexture _cursor, _buoyage;
	private boolean _isAutoAlpha, _isSelect;
	// 新增：标记是否刚重置数据（禁止触摸自动修改索引）
	private boolean _justReset;

	private int _selectedIndex = -1;
	// 翻页分页
	private int _visibleStartIndex = 0;
	private int _visibleEndIndex = 0;
	private int _maxVisibleCount = 0;
	// 事件回调
	private OnSelectListener _onSelectListener;

	private EventActionN _callEvent;

	public LSelect(int x, int y, int width, int height) {
		this(SkinManager.get().getMessageSkin().getFont(), x, y, width, height);
	}

	public LSelect(IFont font, int x, int y, int width, int height) {
		this(font, (LTexture) null, x, y, width, height);
	}

	public LSelect(String fileName) {
		this(SkinManager.get().getMessageSkin().getFont(), fileName);
	}

	public LSelect(IFont font, String fileName) {
		this(font, fileName, 0, 0);
	}

	public LSelect(IFont font, String fileName, int x, int y) {
		this(font, LSystem.loadTexture(fileName), x, y);
	}

	public LSelect(LTexture formImage) {
		this(SkinManager.get().getMessageSkin().getFont(), formImage);
	}

	public LSelect(IFont font, LTexture formImage) {
		this(font, formImage, 0, 0);
	}

	public LSelect(IFont font, LTexture formImage, int x, int y) {
		this(font, formImage, x, y, formImage.getWidth(), formImage.getHeight());
	}

	public LSelect(LTexture formImage, int x, int y) {
		this(SkinManager.get().getMessageSkin().getFont(), formImage, x, y, formImage.getWidth(),
				formImage.getHeight());
	}

	public LSelect(IFont font, LTexture formImage, int x, int y, int width, int height) {
		this(font, formImage, x, y, width, height, SkinManager.get().getMessageSkin().getFontColor());
	}

	public LSelect(SelectSkin skin, int x, int y, int width, int height) {
		this(skin.getFont(), skin.getBackgroundTexture(), x, y, width, height, skin.getFontColor());
	}

	public LSelect(IFont font, LTexture formImage, int x, int y, int width, int height, LColor fontColor) {
		super(x, y, width, height);
		if (formImage == null) {
			this.setBackground(createTempTexture(width, height, 0.3f, 15f));
		} else {
			this.setBackground(formImage);
		}
		this._fontColor = fontColor;
		this._messageFont = (font == null ? LSystem.getSystemGameFont() : font);
		this.customRendering = true;
		this._selectFlag = -1;
		this._space = 30;
		this._tmpOffset = -(width / 10);
		this._delay = new LTimer(150);
		this._confirmDelay = new LTimer(200);
		this._autoAlpha = 0.25F;
		this._isAutoAlpha = true;
		this.setCursor(LSystem.getSystemImagePath() + "creese.png");
		this.setElastic(true);
		this.setLocked(true);
		this._sizeFont = this._messageFont.getSize();
		this._doubleSizeFont = (this._sizeFont > 0 ? this._sizeFont * 2 : LSystem.getFontSize());
		// 初始化标记
		this._justReset = false;
	}

	public LSelect setMessage(String message, TArray<String> list) {
		return setMessage(message, StringUtils.toStrings(list));
	}

	public LSelect setMessage(String[] selects) {
		return setMessage(null, selects);
	}

	public LSelect setMessage(TArray<String> list) {
		return setMessage(null, list);
	}

	public LSelect setMessage(String message, String[] selects) {
		_pendingConfirm = false;
		_confirmDelay.reset();
		_visibleStartIndex = 0;
		_visibleEndIndex = 0;
		_justReset = true;
		_selectedIndex = -1;
		_message = message;
		if (selects == null) {
			this._selects = new String[0];
			this._selectSize = 0;
		} else {
			this._selects = selects;
			this._selectSize = selects.length;
		}
		if (_doubleSizeFont == 0) {
			_doubleSizeFont = LSystem.getFontSize();
		}
		if (_messageFont instanceof LFont) {
			LSTRDictionary.get().bind((LFont) _messageFont, this._selects);
		}
		// 强制设置默认第一个索引
		if (this._selectSize > 0) {
			setSelectedIndex(0);
		} else {
			clearSelection();
		}
		calculateVisibleRange();
		return this;
	}

	protected LTexture createTempTexture(int w, int h, float alpha, float r) {
		Image img = Image.createImage(w, h);
		Canvas canvas = img.getCanvas();
		canvas.setAlpha(alpha);
		canvas.fillRoundRect(0, 0, w, h, r);
		return (_tempTexture = img.texture());
	}

	public LSelect setLeftOffset(int left) {
		this._left = left;
		return this;
	}

	public LSelect setTopOffset(int top) {
		this._top = top;
		return this;
	}

	public int getLeftOffset() {
		return _left;
	}

	public int getTopOffset() {
		return _top;
	}

	public int getResultIndex() {
		return _selectedIndex;
	}

	public LSelect setDelay(long timer) {
		_delay.setDelay(timer);
		return this;
	}

	public long getDelay() {
		return _delay.getDelay();
	}

	public LSelect setConfirmDelay(long delay) {
		this._confirmDelay.setDelay(delay);
		return this;
	}

	public long getConfirmDelay() {
		return _confirmDelay.getDelay();
	}

	public String getResult() {
		return _result;
	}

	@Override
	public void setVisible(boolean v) {
		super.setVisible(v);
		if (v) {
			_pendingConfirm = false;
			_confirmDelay.reset();
			_visibleStartIndex = 0;
			_visibleEndIndex = 0;
			_justReset = true;
			setEnabled(true);
			if (_selectSize > 0) {
				setSelectedIndex(0);
			} else {
				clearSelection();
			}
			calculateVisibleRange();
		}
	}

	@Override
	public void update(long elapsedTime) {
		if (!isVisible()) {
			return;
		}
		super.update(elapsedTime);

		// 延迟确认，避免看不到选中项
		if (_pendingConfirm && _confirmDelay.action(elapsedTime)) {
			dispatchConfirm();
			_pendingConfirm = false;
			setEnabled(true);
		}
		if (_pendingConfirm) {
			return;
		}

		if (_isAutoAlpha && _buoyage != null) {
			if (_delay.action(elapsedTime)) {
				if (_autoAlpha < 0.95F) {
					_autoAlpha += 0.05F;
				} else {
					_autoAlpha = 0.25F;
				}
			}
		}

		// 刚重置时，禁止触摸自动修改索引
		if (!isClickUp() && !_justReset) {
			if (_selects != null && _selectSize > 0) {
				float touchY = _input.getTouchY();
				int listTop = MathUtils.ifloor(_messageTop + _space - _sizeFont / 2);
				int effectiveTouchY = MathUtils.ifloor(touchY == 0 ? getTouchY() : touchY);
				int itemHeight = _space;
				int idx = (effectiveTouchY - listTop + itemHeight / 2) / itemHeight;
				idx = MathUtils.max(0, MathUtils.min(idx, _selectSize - 1));
				setSelectedIndex(idx);
			}
		}

		// 第一次update后取消重置标记，恢复正常触摸选择
		if (_justReset) {
			_justReset = false;
		}

		if (isSelected()) {
			if (isKeyDown(SysKey.UP)) {
				moveSelectionUp();
			} else if (isKeyDown(SysKey.DOWN)) {
				moveSelectionDown();
			} else if (isKeyDown(SysKey.ENTER)) {
				if (_selectedIndex >= 0 && _selectedIndex < _selectSize) {
					_result = _selects[_selectedIndex];
					startPendingConfirm();
				}
			}
		}
	}

	@Override
	protected void createCustomUI(GLEx g, int x, int y, int w, int h) {
		if (!isVisible()) {
			return;
		}
		final int oldColor = g.color();
		_sizeFont = _messageFont.getSize();
		_doubleSizeFont = (_sizeFont > 0 ? _sizeFont * 2 : LSystem.getFontSize());
		_messageLeft = (x + _doubleSizeFont + _sizeFont / 2) + _tmpOffset + _left + _doubleSizeFont;
		float ascent = _messageFont.getAscent();
		if (_message != null) {
			_messageTop = y + _doubleSizeFont + _top - 10;
			_messageFont.drawString(g, _message, _messageLeft, _messageTop - ascent, _fontColor);
		} else {
			_messageTop = y + _top;
		}
		_nTop = _messageTop;
		if (_selects != null && _selectSize > 0) {
			_nLeft = _messageLeft - _sizeFont / 4;
			for (int i = _visibleStartIndex; i <= _visibleEndIndex; i++) {
				_nTop += _space;
				_isSelect = (i == _selectedIndex);
				if ((_buoyage != null) && _isSelect) {
					g.setAlpha(_autoAlpha);
					int buoyY = _nTop - MathUtils.iceil(_buoyage.getHeight() / 1.5f);
					g.draw(_buoyage, _nLeft, buoyY, _component_baseColor);
					g.setAlpha(1F);
				}
				_messageFont.drawString(g, _selects[i], _messageLeft, _nTop - ascent, _fontColor);
				if ((_cursor != null) && _isSelect) {
					int cursorY = _nTop - _cursor.getHeight() / 2;
					g.draw(_cursor, _nLeft, cursorY, _cursorColor);
				}
			}
		}
		g.setColor(oldColor);
	}

	public LSelect setCursorColor(LColor c) {
		this._cursorColor = c;
		return this;
	}

	public LColor getCursorColor() {
		return _cursorColor;
	}

	public void callConfirm() {
		if ((this._selects != null) && (this._selectedIndex >= 0) && (this._selectedIndex < this._selectSize)) {
			this._result = this._selects[this._selectedIndex];
			this._selectFlag = this._selectedIndex + 1;
			startPendingConfirm();
		}
	}

	@Override
	public void upClick() {
		super.upClick();
		_justReset = false;
		callConfirm();
	}

	@Override
	protected void processKeyPressed() {
		super.processKeyPressed();
		if (this.isSelected() && this.isKeyDown(SysKey.ENTER)) {
			if (_selectedIndex >= 0 && _selectedIndex < _selectSize) {
				this._result = _selects[_selectedIndex];
				startPendingConfirm();
			}
		}
	}

	@Override
	public LColor getFontColor() {
		return _fontColor.cpy();
	}

	public LSelect setFontColor(LColor f) {
		this._fontColor = f;
		return this;
	}

	public IFont getMessageFont() {
		return _messageFont;
	}

	public LSelect setMessageFont(IFont m) {
		this._messageFont = m;
		this._sizeFont = (m == null ? LSystem.getFontSize() : m.getSize());
		this._doubleSizeFont = (this._sizeFont > 0 ? this._sizeFont * 2 : LSystem.getFontSize());
		return this;
	}

	@Override
	public LSelect setFont(IFont newFont) {
		return this.setMessageFont(newFont);
	}

	@Override
	public IFont getFont() {
		return getMessageFont();
	}

	public int getSelectFlag() {
		return _selectFlag;
	}

	public LTexture getCursor() {
		return _cursor;
	}

	public LSelect setNotCursor() {
		this._cursor = null;
		return this;
	}

	public LSelect setCursor(LTexture cursor) {
		this._cursor = cursor;
		return this;
	}

	public LSelect setCursor(String fileName) {
		setCursor(LSystem.loadTexture(fileName));
		return this;
	}

	public LTexture getBuoyage() {
		return _buoyage;
	}

	public LSelect setNotBuoyage() {
		this._buoyage = null;
		return this;
	}

	public LSelect setBuoyage(LTexture buoyage) {
		this._buoyage = buoyage;
		return this;
	}

	public LSelect setBuoyage(String fileName) {
		setBuoyage(LSystem.loadTexture(fileName));
		return this;
	}

	public boolean isFlashBuoyage() {
		return _isAutoAlpha;
	}

	public LSelect setFlashBuoyage(boolean flashBuoyage) {
		this._isAutoAlpha = flashBuoyage;
		return this;
	}

	public int getSpace() {
		return _space;
	}

	public LSelect setSpace(int space) {
		this._space = space;
		calculateVisibleRange();
		return this;
	}

	public LSelect setSelectedIndex(int index) {
		if (_pendingConfirm) {
			return this;
		}
		int oldIndex = _selectedIndex;
		if (index < 0 || _selects == null || index >= _selectSize) {
			this._selectedIndex = -1;
			this._selectFlag = -1;
		} else {
			this._selectedIndex = index;
			this._selectFlag = index + 1;
		}
		if (oldIndex != _selectedIndex) {
			calculateVisibleRange();
			dispatchSelectionChange();
		}
		return this;
	}

	/**
	 * 清除选择
	 */
	public LSelect clearSelection() {
		_pendingConfirm = false;
		_confirmDelay.reset();
		_visibleStartIndex = 0;
		_visibleEndIndex = 0;
		_justReset = true;
		this._selectedIndex = -1;
		this._selectFlag = -1;
		this._result = null;
		return this;
	}

	/**
	 * 向上移动选择
	 */
	public LSelect moveSelectionUp() {
		if (_selectSize <= 0 || _pendingConfirm) {
			return this;
		}
		_justReset = false;
		setSelectedIndex(MathUtils.max(0, _selectedIndex - 1));
		return this;
	}

	/**
	 * 向下移动选择
	 */
	public LSelect moveSelectionDown() {
		if (_selectSize <= 0 || _pendingConfirm) {
			return this;
		}
		_justReset = false;
		setSelectedIndex(MathUtils.min(_selectSize - 1, _selectedIndex + 1));
		return this;
	}

	public LSelect setSelects(String[] selects) {
		return setMessage(this._message, selects);
	}

	/**
	 * 获取当前选中项
	 */
	public int getSelectedIndex() {
		return _selectedIndex;
	}

	/**
	 * 获取选项总数
	 */
	public int getSelectSize() {
		return _selectSize;
	}

	public LSelect setOnSelectListener(OnSelectListener listener) {
		_onSelectListener = listener;
		return this;
	}

	public OnSelectListener getOnSelectListener() {
		return _onSelectListener;
	}

	public LSelect setCallEvent(EventActionN e) {
		_callEvent = e;
		return this;
	}

	public EventActionN getCallEvent() {
		return _callEvent;
	}

	private void calculateVisibleRange() {
		if (_selectSize == 0 || _space == 0) {
			_visibleStartIndex = 0;
			_visibleEndIndex = 0;
			return;
		}
		final int listTop = MathUtils.ifloor(getHeight() - _space - _doubleSizeFont);
		_maxVisibleCount = MathUtils.max(1, MathUtils.ifloor(listTop / _sizeFont));
		if (_selectedIndex == 0) {
			_visibleStartIndex = 0;
		} else if (_selectedIndex < _visibleStartIndex) {
			_visibleStartIndex = _selectedIndex;
		} else if (_selectedIndex > _visibleStartIndex + _maxVisibleCount - 1) {
			_visibleStartIndex = _selectedIndex - _maxVisibleCount + 1;
		}

		_visibleEndIndex = MathUtils.min(_visibleStartIndex + _maxVisibleCount - 1, _selectSize - 1);
		_visibleStartIndex = MathUtils.max(0, _visibleStartIndex);
	}

	private void dispatchSelectionChange() {
		if (_onSelectListener != null && _selectedIndex >= 0 && _selectedIndex < _selectSize) {
			_onSelectListener.onSelectionChanged(this, _selectedIndex, _selects[_selectedIndex]);
		}
	}

	private void dispatchConfirm() {
		if (_selectedIndex >= 0 && _selectedIndex < _selectSize) {
			if (_onSelectListener != null) {
				_onSelectListener.onItemConfirmed(this, _selectedIndex, _selects[_selectedIndex]);
			}
			if (_callEvent != null) {
				_callEvent.update();
			}
		}
	}

	/**
	 * 延迟调用回调事件
	 */
	private void startPendingConfirm() {
		if (_pendingConfirm) {
			return;
		}
		_pendingConfirm = true;
		_confirmDelay.reset();
		setEnabled(false);
	}

	@Override
	public void createUI(GLEx g, int x, int y) {
	}

	@Override
	public String getUIName() {
		return "Select";
	}

	@Override
	public void destroy() {
		if (_tempTexture != null) {
			_tempTexture.close(true);
			_tempTexture = null;
		}
		_onSelectListener = null;
	}

}
