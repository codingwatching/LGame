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

import loon.LTexture;
import loon.canvas.LColor;
import loon.component.DefUI;

public class SliderSkin extends SkinAbstract<SliderSkin> {

	public static SliderSkin def(LColor sliderColor, LColor barColor, boolean vertical) {
		return new SliderSkin(sliderColor, barColor, vertical);
	}

	private LTexture sliderText;

	private LTexture barText;

	public SliderSkin(LColor sliderColor, LColor barColor, boolean vertical) {
		this(DefUI.getGameWinDiamond(100, 100, sliderColor), vertical ? DefUI.getGameWinHollow(30, 100, 12, barColor)
				: DefUI.getGameWinHollow(100, 30, 12, barColor));
	}

	public SliderSkin(LTexture slider, LTexture bar) {
		super();
		this.sliderText = slider;
		this.barText = bar;
	}

	public LTexture getSliderText() {
		return sliderText;
	}

	public SliderSkin setSliderText(LTexture sliderText) {
		this.sliderText = sliderText;
		return this;
	}

	public LTexture getBarText() {
		return barText;
	}

	public SliderSkin setBarText(LTexture barText) {
		this.barText = barText;
		return this;
	}

	@Override
	public String getSkinName() {
		return "slider";
	}
}
