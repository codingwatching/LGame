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
 */
package loon.component;

import loon.LSystem;
import loon.canvas.LColor;
import loon.font.FontSet;
import loon.font.IFont;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.TArray;
import loon.utils.timer.Duration;

/**
 * 单纯文字显示用组件(无边框或背景图,但是加入了自动定是替换，并且可以注入多个信息)
 */
public class LLabels extends LComponent implements FontSet<LLabels> {

	public class Info {

		public LColor color;

		public String message;

		public float x;
		public float y;

		public float stateTime;
		public float length;

		public float speed;
	}

	private IFont font;

	private LColor fontColor;

	public final TArray<Info> labels = new TArray<>();

	private float speed = 0f;

	public LLabels(int x, int y, int width, int height) {
		this(LSystem.getSystemGameFont(), x, y, width, height);
	}

	public LLabels(IFont font, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.font = font;
		this.fontColor = LColor.white.cpy();
	}

	@Override
	public void update(long elapsedTime) {
		if (!isVisible()) {
			return;
		}
		super.update(elapsedTime);
		this.speed = MathUtils.max(Duration.toS(elapsedTime), LSystem.MIN_SECONE_SPEED_FIXED);

	}

	public void draw(GLEx g, int x, int y) {
		if (!isVisible()) {
			return;
		}
		for (int i = 0; i < labels.size; i++) {
			Info label = labels.get(i);
			if (label.length == -1) {
				font.drawString(g, label.message, x + label.x, y + label.y - font.getHeight() / 2 + 5,
						_colorTemp.setColor(fontColor == null ? LColor.getColorARGBInt(label.color)
								: LColor.combine(fontColor, label.color)));
			} else {
				label.stateTime += speed;
				if (label.stateTime > label.length) {
					labels.remove(label);
				} else {
					font.drawString(g, label.message, x + label.x, y + label.y - font.getHeight() / 2 + 5,
							_colorTemp.setColor(fontColor == null ? LColor.getColorARGBInt(label.color)
									: LColor.combine(fontColor, label.color)));
				}
			}
		}
	}

	public LLabels addLabel(String message, LColor color) {
		return addLabel(0, 0, message, -1, color, -1);
	}

	public LLabels addLabel(float x, float y, String message, LColor color) {
		return addLabel(x, y, message, -1, color, -1);
	}

	public LLabels addLabel(float x, float y, String message, float length, LColor color, float speed) {
		Info label = new Info();
		label.x = x;
		label.y = y;
		label.message = message;
		label.color = color;
		label.length = length;
		label.speed = speed;
		return addLabel(label);
	}

	public LLabels addLabel(Info info) {
		labels.add(info);
		return this;
	}

	public LLabels clear() {
		synchronized (labels) {
			labels.clear();
		}
		return this;
	}

	@Override
	public void createUI(GLEx g, int x, int y) {
		draw(g, x, y);
	}

	@Override
	public LLabels setFont(IFont font) {
		this.font = font;
		return this;
	}

	@Override
	public IFont getFont() {
		return this.font;
	}

	@Override
	public LColor getFontColor() {
		return fontColor.cpy();
	}

	@Override
	public LLabels setFontColor(LColor color) {
		this.fontColor = color;
		return this;
	}

	@Override
	public String getUIName() {
		return "Labels";
	}

	@Override
	public void destory() {
		clear();
	}

}
