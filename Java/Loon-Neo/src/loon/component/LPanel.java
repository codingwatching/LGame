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
package loon.component;

import loon.opengl.GLEx;
import loon.utils.MathUtils;

/**
 * 空的容器面板,什么也不显示,可以充当布局器或者单纯容器使用
 */
public class LPanel extends LContainer {

	public LPanel(float x, float y, float w, float h) {
		this(MathUtils.ifloor(x), MathUtils.ifloor(y), MathUtils.iceil(w), MathUtils.iceil(h));
	}

	public LPanel(int x, int y, int w, int h) {
		super(x, y, w, h);
		this.customRendering = true;
	}

	@Override
	public String getUIName() {
		return "Panel";
	}

	@Override
	public void createUI(GLEx g, int x, int y) {
	}

	@Override
	public void destory() {

	}

}
