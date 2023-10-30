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
package loon.component.skin;

import loon.LSystem;
import loon.LTexture;
import loon.canvas.LColor;
import loon.component.DefUI;
import loon.font.IFont;

public class ProgressSkin {

	private LTexture progressTexture;
	private LTexture backgroundTexture;
	private LColor color;

	public final static ProgressSkin def() {
		return new ProgressSkin();
	}

	public ProgressSkin() {
		this(LSystem.getSystemGameFont(), LColor.white.cpy(), DefUI.self().getDefaultTextures(4),
				DefUI.self().getDefaultTextures(1));
	}

	public ProgressSkin(IFont font, LColor c, LTexture progress, LTexture background) {
		this.color = c;
		this.progressTexture = progress;
		this.backgroundTexture = background;
	}

	public LTexture getProgressTexture() {
		return progressTexture;
	}

	public void setProgressTexture(LTexture progressTexture) {
		this.progressTexture = progressTexture;
	}

	public LTexture getBackgroundTexture() {
		return backgroundTexture;
	}

	public void setBackgroundTexture(LTexture backgroundTexture) {
		this.backgroundTexture = backgroundTexture;
	}

	public LColor getColor() {
		return color;
	}

	public void setColor(LColor c) {
		this.color = c;
	}

}
