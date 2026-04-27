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
package loon.component;

import loon.LSystem;
import loon.LTexture;
import loon.canvas.LColor;
import loon.component.skin.SelectSkin;
import loon.events.ActionKey;
import loon.events.CallFunction;
import loon.events.EventActionN;
import loon.events.SysKey;
import loon.events.SysTouch;
import loon.font.FontSet;
import loon.font.FontUtils;
import loon.font.IFont;
import loon.font.LFont;
import loon.geom.PointF;
import loon.geom.RectF;
import loon.opengl.GLEx;
import loon.opengl.LSTRDictionary;
import loon.utils.MathUtils;
import loon.utils.ObjectMap;
import loon.utils.StringUtils;
import loon.utils.TArray;
import loon.utils.timer.LTimer;

/**
 * 游戏中常见的分行选择型菜单栏,注入几行文字(字符串数组),就会自行产生几行可选菜单UI
 * 
 * <pre>
 * LMenuSelect ms = new LMenuSelect("第一选项,第二个,第三个,第四个,我是第五个", 66, 66); 
 * // 选中行的选择外框渲染颜色,不设置不显示 
 * // ms.setSelectRectColor(LColor.red); 
 * // 选中行所用的图像标记(箭头图之类),不设置使用默认样式 
 * // ms.setImageFlag(LSystem.FRAMEWORK_IMG_NAME+"creese.png"); 
 * // 选择框菜单所用的背景图,不设置使用默认样式,也可以noneBackground不显示
 * ms.setBackground(DefUI.getGameWinFrame(ms.width(),
 * ms.height(),LColor.black,LColor.blue, false)); 
 * // 设置监听 ms.setMenuListener(new LMenuSelect.ClickEvent() {
 * 
 * // 监听当前点击的索引与内容
 * 
 * public void onSelected(int index, String context) { 
 *           // 添加气泡提示
 *           add(LToast.makeText(context, Style.SUCCESS));
 * 
 *           }}); 
 *           // 添加到screen 
 *           add(ms);
 * </pre>
 */
public class LMenuSelect extends LComponent implements FontSet<LMenuSelect> {

	public static interface ClickEvent {
		public void onSelected(int index, String context);
	}

	private final ActionKey _touchEvent = new ActionKey(ActionKey.NORMAL);
	private final ActionKey _keyEvent = new ActionKey(ActionKey.NORMAL);

	private ObjectMap<String, EventActionN> _doClickEvents;
	private ClickEvent _menuSelectedEvent;
	private CallFunction _function;

	private boolean _over, _pressed, _focused;
	private int _pressedTime = 0;
	// 默认选中索引
	private int _selected = 0;

	private boolean _justReset;

	private IFont _font;
	private String[] _labels;
	private RectF[] _selectRects;

	private int _selectCountMax = -1;
	private String _flag = LSystem.FLAG_SELECT_TAG;
	private LTexture _flag_image = null;
	private final PointF _offsetFont = new PointF();
	private float _flag_text_space;
	private boolean _showRect;
	private boolean _showBackground;
	private float _flagWidth = 0;
	private float _flagHeight = 0;
	private String _result = null;

	private LColor _selectRectColor;
	private LColor _fontColor;
	private LColor _selectedFillColor;
	private LColor _selectBackgroundColor;
	private LColor _selectFlagColor;
	private LTimer _colorUpdate;

	public static LMenuSelect make(String labels) {
		return new LMenuSelect(labels, 0, 0);
	}

	public static LMenuSelect make(String labels, float x, float y) {
		return new LMenuSelect(labels, x, y);
	}

	public static LMenuSelect make(String[] labels, float x, float y) {
		return new LMenuSelect(labels, x, y);
	}

	public static LMenuSelect make(IFont font, String[] labels, float x, float y) {
		return new LMenuSelect(font, labels, x, y);
	}

	public static LMenuSelect make(IFont font, String[] labels, String path, float x, float y) {
		return new LMenuSelect(font, labels, path, x, y);
	}

	public static LMenuSelect make(IFont font, String[] labels, LTexture bg, float x, float y) {
		return new LMenuSelect(font, labels, bg, x, y);
	}

	public LMenuSelect(SelectSkin skin, String[] labels) {
		this(skin, labels, 0f, 0f);
	}

	public LMenuSelect(SelectSkin skin, String[] labels, float x, float y) {
		this(skin.getFont(), skin.getFontColor(), labels, skin.getBackgroundTexture(), x, y);
	}

	public LMenuSelect(String labels, float x, float y) {
		this(StringUtils.split(labels, LSystem.COMMA), x, y);
	}

	public LMenuSelect(String[] labels, float x, float y) {
		this(LSystem.getSystemGameFont(), labels, x, y);
	}

	public LMenuSelect(IFont font, String[] labels, float x, float y) {
		this(font, labels, (LTexture) null, x, y);
	}

	public LMenuSelect(IFont font, String[] labels, String path, float x, float y) {
		this(font, labels, LSystem.loadTexture(path), x, y);
	}

	public LMenuSelect(IFont font, String[] labels, LTexture bg, float x, float y) {
		this(font, LColor.white, labels, bg, x, y);
	}

	public LMenuSelect(IFont font, LColor fontColor, String[] labels, LTexture bg, float x, float y) {
		this(MathUtils.ifloor(x), MathUtils.ifloor(y), 1, 1);
		// 初始化颜色
		this._selectRectColor = LColor.white;
		this._selectedFillColor = LColor.blue;
		this._selectBackgroundColor = LColor.blue.darker();
		this._selectFlagColor = LColor.orange;
		this._fontColor = fontColor;
		// 初始化计时器
		this._colorUpdate = new LTimer(LSystem.SECOND * 2);
		this._flag_text_space = 10;
		this._showRect = false;
		this._showBackground = true;
		// 初始化状态
		this._justReset = false;
		onlyBackground(bg);
		setFont(font);
		setLabels(labels);
	}

	public LMenuSelect(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	public LMenuSelect reset() {
		super.reset();
		resetSelect();
		return this;
	}

	/**
	 * 选择状态重置
	 */
	public void resetSelect() {
		_justReset = true;
		_selected = 0;
		_over = false;
		_pressed = false;
		_pressedTime = 0;
		_touchEvent.release();
		_keyEvent.release();
		_colorUpdate.refresh();
		setEnabled(true);
	}

	@Override
	public void setVisible(boolean v) {
		super.setVisible(v);
		// 显示时强制还原初始状态,方便重用
		if (v) {
			resetSelect();
		}
	}

	public LMenuSelect setTextFlag(String flag) {
		if (!_flag.equals(flag)) {
			this._flag = flag;
			this.setLabels(_labels);
		}
		return this;
	}

	public String getTextFlag() {
		return this._flag;
	}

	public LTexture getImageFlag() {
		return this._flag_image;
	}

	public LMenuSelect setImageFlag(LTexture tex) {
		this._flag_image = tex;
		if (_flag_image != null) {
			freeRes().add(_flag_image);
		}
		return this;
	}

	public LMenuSelect setImageFlag(String path) {
		return setImageFlag(LSystem.loadTexture(path));
	}

	public LMenuSelect update(String labels) {
		return setLabels(labels);
	}

	public LMenuSelect setLabels(String labels) {
		return setLabels(StringUtils.split(labels, LSystem.COMMA));
	}

	public LMenuSelect update(String[] labels) {
		return setLabels(labels);
	}

	public LMenuSelect setLabels(String[] labels) {
		resetSelect();
		this._labels = labels;
		if (_labels != null && _labels.length > 0) {
			_selectCountMax = labels.length;
			_selectRects = new RectF[_selectCountMax];
			TArray<CharSequence> chars = new TArray<CharSequence>(_selectCountMax);
			float maxWidth = 0;
			float maxHeight = 0;

			// 计算标记宽高
			if (_flag_image == null) {
				_flagWidth = _font.stringWidth(_flag);
				_flagHeight = _font.stringHeight(_flag);
			} else {
				_flagWidth = _flag_image.getWidth();
				_flagHeight = _flag_image.getHeight();
			}

			for (int i = 0; i < _labels.length; i++) {
				chars.clear();
				chars = FontUtils.splitLines(_labels[i], chars);
				float height = 0;

				for (CharSequence ch : chars) {
					float textWidth = FontUtils.measureText(_font, ch);
					maxWidth = MathUtils.max(maxWidth, textWidth + _font.getHeight() + _flagWidth + _flag_text_space);
					height += MathUtils.max(_font.stringHeight(ch.toString()), _flagHeight);
				}

				height += _font.getHeight() / 2;
				_selectRects[i] = new RectF(0, maxHeight, maxWidth, height);
				maxHeight += height;
			}

			setSize(maxWidth + _flag_text_space * 2, maxHeight + _flag_text_space * 2);

			// 绑定字体渲染
			if (_font instanceof LFont) {
				LSTRDictionary.get().bind((LFont) _font, _labels);
			}
		} else {
			// 空数据处理
			_selectCountMax = 0;
			_selectRects = null;
			setSize(1, 1);
		}
		return this;
	}

	public LMenuSelect setMenuListener(ClickEvent event) {
		_menuSelectedEvent = event;
		return this;
	}

	public ClickEvent getMenuListener() {
		return this._menuSelectedEvent;
	}

	@Override
	public LMenuSelect setFont(IFont font) {
		_font = font;
		return this;
	}

	@Override
	public IFont getFont() {
		return _font;
	}

	@Override
	public void createUI(GLEx g, int x, int y) {
		if (!isVisible()) {
			return;
		}
		if (_font == null || _labels == null || _selectRects == null) {
			return;
		}
		final IFont tmp = g.getFont();
		g.setFont(_font);
		if (_showBackground) {
			if (_background != null) {
				RectF rect = _selectRects[0];
				g.draw(_background, x + rect.x - _flag_text_space, y + rect.y - _flag_text_space, getWidth(),
						getHeight());
			} else {
				RectF rect = _selectRects[0];
				g.fillRect(x + rect.x - _flag_text_space, y + rect.y - _flag_text_space, getWidth(), getHeight(),
						_selectBackgroundColor.setAlpha(0.5f));
			}
		}
		for (int i = 0; i < _labels.length; i++) {
			RectF rect = _selectRects[i];
			if (_selected == i) {
				drawSelectedFill(g, _offsetFont.x + x + rect.x, _offsetFont.y + y + rect.y, rect.width, rect.height);
			}
			float textX = _offsetFont.x + x + rect.x + (rect.width - _font.stringWidth(_labels[i])) / 2;
			float textY = _offsetFont.y + y + rect.y + (rect.height - _font.stringHeight(_labels[i])) / 4;

			if (_flagWidth > 0 || _flagHeight > 0) {
				textX += _flagWidth;
				if (_selected == i) {
					float flagX = _offsetFont.x + x + rect.x + _flagWidth / 2;
					float flagY = _offsetFont.y + y + rect.y + _flagHeight / 8;
					if (_flag_image == null) {
						g.drawString(_flag, flagX, flagY, _selectFlagColor);
					} else {
						g.draw(_flag_image, flagX, flagY);
					}
				}
			}
			g.drawString(_labels[i], textX, textY, _fontColor);
			if (_showRect) {
				g.drawRect(x + rect.x, y + rect.y, rect.width, rect.height, _selectRectColor);
			}
		}

		g.setFont(tmp);
	}

	protected void drawSelectedFill(GLEx g, float x, float y, float width, float height) {
		final int color = g.color();
		g.setColor(_selectedFillColor.getRed(), _selectedFillColor.getGreen(), _selectedFillColor.getBlue(),
				MathUtils.iceil(155 * MathUtils.max(0.5f, _colorUpdate.getPercentage())));
		g.fillRect(x, y - 2, width, height);
		g.drawRect(x, y - 2, width - 2, height - 2);
		g.setColor(color);
	}

	@Override
	public void update(long elapsedTime) {
		if (!isVisible()) {
			return;
		}
		super.update(elapsedTime);

		// 计时器更新
		if (_colorUpdate.action(elapsedTime)) {
			_colorUpdate.refresh();
		}

		// 按压状态更新
		if (_focused) {
			_pressed = true;
			return;
		}
		if (_pressedTime > 0 && --_pressedTime <= 0) {
			_pressed = false;
		}

		if (_justReset) {
			_justReset = false;
			return;
		}

		if ((SysTouch.isDown() || SysTouch.isDrag() || SysTouch.isMove() || _input.isMoving()) && _selectRects != null
				&& _labels != null) {
			float touchX = getUITouchX();
			float touchY = getUITouchY();
			for (int i = 0; i < _selectRects.length; i++) {
				RectF rect = _selectRects[i];
				if (rect != null && rect.inside(touchX, touchY)) {
					_selected = i;
					break;
				}
			}
		}
	}

	public String getResult() {
		if (_labels != null && _selected >= 0 && _selected < _labels.length) {
			_result = _labels[_selected];
		}
		return _result;
	}

	public boolean isTouchOver() {
		return _over;
	}

	public boolean isTouchPressed() {
		return _pressed;
	}

	@Override
	protected void processTouchDragged() {
		_over = _pressed = intersects(getUITouchX(), getUITouchY());
		super.processTouchDragged();
	}

	@Override
	protected void processTouchPressed() {
		if (!isVisible()) {
			return;
		}
		if (!_touchEvent.isPressed()) {
			_pressed = true;
			super.processTouchPressed();
			_touchEvent.press();
		}
	}

	@Override
	protected void processTouchReleased() {
		if (!isVisible()) {
			return;
		}
		if (_touchEvent.isPressed()) {
			_pressed = false;
			if (_menuSelectedEvent != null && _labels != null && _selected >= 0 && _selected < _labels.length) {
				try {
					_menuSelectedEvent.onSelected(_selected, _labels[_selected]);
				} catch (Throwable thr) {
					LSystem.error("LMenuSelect onSelected() exception", thr);
				}
			}
			// 触发自定义命令
			if (_doClickEvents != null && _labels != null && _selected >= 0 && _selected < _labels.length) {
				EventActionN event = _doClickEvents.get(_labels[_selected]);
				if (event != null) {
					event.update();
				}
			}
			super.processTouchReleased();
			if (_function != null) {
				_function.call(this);
			}
			_touchEvent.release();
		}
	}

	@Override
	protected void processTouchEntered() {
		_over = true;
	}

	@Override
	protected void processTouchExited() {
		_over = _pressed = false;
	}

	@Override
	protected void processKeyPressed() {
		if (!isVisible() || _labels == null || _labels.length == 0) {
			return;
		}
		if (isSelected() && !_keyEvent.isPressed()) {
			_pressedTime = 5;
			_pressed = true;
			int code = _input.getKeyPressed();
			switch (code) {
			case SysKey.UP:
				_selected = MathUtils.max(0, _selected - 1);
				break;
			case SysKey.DOWN:
				_selected = MathUtils.min(_labels.length - 1, _selected + 1);
				break;
			}
			if (_menuSelectedEvent != null) {
				try {
					_menuSelectedEvent.onSelected(_selected, _labels[_selected]);
				} catch (Throwable t) {
					LSystem.error("LMenuSelect onSelected() exception", t);
				}
			}
			doClick();
			_keyEvent.press();
		}
	}

	@Override
	protected void processKeyReleased() {
		if (!isVisible() || !isSelected()) {
			return;
		}
		if (_keyEvent.isPressed()) {
			_pressed = false;
			_keyEvent.release();
		}
	}

	public LMenuSelect setCommand(int idx, EventActionN e) {
		if (_labels != null && idx > -1 && idx < _labels.length) {
			setCommand(_labels[idx], e);
		}
		return this;
	}

	public LMenuSelect setCommand(String key, EventActionN e) {
		if (_doClickEvents == null) {
			_doClickEvents = new ObjectMap<String, EventActionN>();
		}
		if (e != null) {
			_doClickEvents.put(key, e);
		}
		return this;
	}

	public LMenuSelect clearCommand() {
		if (_doClickEvents != null) {
			_doClickEvents.clear();
		}
		return this;
	}

	public PointF getOffsetFont() {
		return _offsetFont;
	}

	public LMenuSelect setOffsetFont(PointF offset) {
		this._offsetFont.set(offset);
		return this;
	}

	public boolean isShowRect() {
		return _showRect;
	}

	public LMenuSelect setShowRect(boolean s) {
		this._showRect = s;
		return this;
	}

	public LColor getSelectRectColor() {
		return _selectRectColor.cpy();
	}

	public LMenuSelect setSelectRectColor(LColor c) {
		this.setShowRect(true);
		this._selectRectColor = c;
		return this;
	}

	public int getSelected() {
		return _selected;
	}

	public LMenuSelect setSelected(int s) {
		if (_labels != null && s >= 0 && s < _labels.length) {
			this._selected = s;
		}
		return this;
	}

	public LColor getSelectedFillColor() {
		return _selectedFillColor.cpy();
	}

	public LMenuSelect setSelectedFillColor(LColor s) {
		this._selectedFillColor = s;
		return this;
	}

	public LTimer getColorUpdateTimer() {
		return _colorUpdate;
	}

	public CallFunction getFunction() {
		return _function;
	}

	public LMenuSelect setFunction(CallFunction f) {
		this._function = f;
		return this;
	}

	public String[] getLabels() {
		return _labels;
	}

	@Override
	public LColor getFontColor() {
		return _fontColor.cpy();
	}

	@Override
	public LMenuSelect setFontColor(LColor fc) {
		this._fontColor = fc;
		return this;
	}

	public LMenuSelect noneBackground() {
		this._drawBackground = false;
		this._showBackground = false;
		if (_background != null) {
			_background.close();
			_background = null;
		}
		return this;
	}

	@Override
	public LComponent clearBackground() {
		this.noneBackground();
		return this;
	}

	public float getFlagTextSpace() {
		return _flag_text_space;
	}

	public LMenuSelect setFlagTextSpace(float f) {
		if (_flag_text_space == f) {
			return this;
		}
		this._flag_text_space = f;
		this.setLabels(_labels);
		return this;
	}

	public LColor getSelectBackgroundColor() {
		return _selectBackgroundColor.cpy();
	}

	public void setSelectBackgroundColor(LColor s) {
		this._selectBackgroundColor = new LColor(s);
	}

	public LColor getSelectFlagColor() {
		return _selectFlagColor.cpy();
	}

	public LMenuSelect setSelectFlagColor(LColor s) {
		this._selectFlagColor = new LColor(s);
		return this;
	}

	@Override
	public String getUIName() {
		return "MenuSelect";
	}

	@Override
	public void destroy() {
		clearCommand();
		_doClickEvents = null;
		_menuSelectedEvent = null;
		_function = null;
		_labels = null;
		_selectRects = null;
	}

}
