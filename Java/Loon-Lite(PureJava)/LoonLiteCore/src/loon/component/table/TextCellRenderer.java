/**
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
 * @version 0.4.2
 */
package loon.component.table;

import loon.canvas.Canvas;
import loon.canvas.LColor;
import loon.component.table.LTable.BindIcon;
import loon.font.FontSet;
import loon.font.IFont;
import loon.geom.Alignment;
import loon.geom.Dimension;
import loon.opengl.GLEx;

public class TextCellRenderer implements ICellRenderer, FontSet<TextCellRenderer> {

	private IFont font;
	private LColor textColor = LColor.white;
	private Alignment alignment = Alignment.LEFT;

	public TextCellRenderer(IFont font) {
		this.font = font;
	}

	@Override
	public void paint(GLEx g, Object vl, int x, int y, int width, int height) {
		if (vl instanceof BindIcon) {
			int size = font.getHeight() - 4;
			BindIcon icon = (BindIcon) vl;
			String s = icon.name;
			s = font.confineLength(s, width - size - 4);
			int entryOffset = 4 + alignment.alignX(width - 4, font.stringWidth(s));
			if (icon.texture != null) {
				g.draw(icon.texture, x + 4, y + (font.getHeight() - size) / 2, size, size);
				font.drawString(g, s, x + size + entryOffset + 4, y - 4, textColor);
			} else {
				font.drawString(g, s, x + entryOffset, y - 4, textColor);
			}
		} else {
			String s = vl.toString();
			s = font.confineLength(s, width - 4);
			int entryOffset = 4 + alignment.alignX(width - 4, font.stringWidth(s));
			font.drawString(g, s, x + entryOffset, y - 4, textColor);
		}
	}

	public void paint(Canvas g, Object vl, int x, int y, int width, int height) {
		int old = g.getFillColor();
		if (vl instanceof BindIcon) {
			int size = font.getHeight() - 4;
			BindIcon icon = (BindIcon) vl;
			String s = icon.name;
			s = font.confineLength(s, width - size - 4);
			int entryOffset = 4 + alignment.alignX(width - 4, font.stringWidth(s));
			g.setColor(textColor);
			if (icon.texture != null) {
				g.draw(icon.texture.getImage(), x + 4, y + (font.getHeight() - size) / 2, size, size);
				g.drawText(s, x + size + entryOffset + 4, y - 4);
			} else {
				g.drawText(s, x + entryOffset, y - 4);
			}
		} else {
			String s = vl.toString();
			s = font.confineLength(s, width - 4);
			int entryOffset = 4 + alignment.alignX(width - 4, font.stringWidth(s));
			g.setColor(textColor);
			g.drawText(s, x + entryOffset, y - 4);
		}
		g.setFillColor(old);
	}

	@Override
	public TextCellRenderer setFont(IFont font) {
		this.font = font;
		return this;
	}

	@Override
	public IFont getFont() {
		return this.font;
	}

	public TextCellRenderer setTextColor(LColor textColor) {
		return setFontColor(textColor);
	}

	public Dimension getCellContentSize(Object vl) {
		if (vl == null) {
			return null;
		}
		String s = vl.toString();
		return new Dimension(font.stringWidth(s), font.getHeight());
	}

	@Override
	public TextCellRenderer setFontColor(LColor color) {
		this.textColor = color;
		return this;
	}

	@Override
	public LColor getFontColor() {
		return textColor.cpy();
	}

}
