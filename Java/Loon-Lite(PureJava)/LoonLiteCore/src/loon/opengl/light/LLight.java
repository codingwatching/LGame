/**
 * Copyright 2008 - 2011
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
package loon.opengl.light;

import loon.LSysException;
import loon.canvas.LColor;
import loon.utils.TArray;

/**
 * 像素化光源(伪光源)用类
 */
public abstract class LLight {

	protected LColor[] colors;

	// 图层光源是否被开启
	protected boolean lightingOn;

	protected boolean colouredLights;

	protected boolean isLightDirty;

	// 图层光源
	protected float[][][] lightValue;

	// 光源集合
	protected TArray<Light> lights = new TArray<Light>();

	// 默认的主控光源
	protected Light mainLight;

	private int width;

	private int height;

	public static Light create(float x, float y, float str) {
		return new Light(x, y, str);
	}

	protected void maxLightSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public LLight setColor(int corner, float r, float g, float b, float a) {
		if (colors == null) {
			colors = new LColor[] { new LColor(1, 1, 1, 1f), new LColor(1, 1, 1, 1f), new LColor(1, 1, 1, 1f),
					new LColor(1, 1, 1, 1f) };
		}

		colors[corner].r = r;
		colors[corner].g = g;
		colors[corner].b = b;
		colors[corner].a = a;
		return this;
	}

	/**
	 * 设定地图光源颜色
	 * 
	 * @param image
	 * @param x
	 * @param y
	 */
	public LLight setLightColor(int x, int y) {
		if (x < width && y < height) {
			setColor(0, lightValue[x][y][0], lightValue[x][y][1], lightValue[x][y][2], 1);
			setColor(1, lightValue[x + 1][y][0], lightValue[x + 1][y][1], lightValue[x + 1][y][2], 1);
			setColor(2, lightValue[x + 1][y + 1][0], lightValue[x + 1][y + 1][1], lightValue[x + 1][y + 1][2], 1);
			setColor(3, lightValue[x][y + 1][0], lightValue[x][y + 1][1], lightValue[x][y + 1][2], 1);
			isLightDirty = true;
		}
		return this;
	}

	private void createLight() {
		if (lightValue == null) {
			lightValue = new float[width + 1][height + 1][3];
		}
		lights.clear();
		if (mainLight != null) {
			lights.add(mainLight);
		}
		updateLight();
	}

	public LLight setMainLight(Light l) {
		this.mainLight = l;
		this.isLightDirty = true;
		return this;
	}

	public Light getMainLight() {
		return mainLight;
	}

	public LLight addLight(Light l) {
		if (l != null) {
			this.lights.add(l);
			this.isLightDirty = true;
		}
		return this;
	}

	public LLight removeLight(Light l) {
		if (l != null) {
			this.lights.remove(l);
			this.isLightDirty = true;
		}
		return this;
	}

	public LLight setLight(boolean l) {
		if (lightValue == null && l) {
			createLight();
		}
		this.lightingOn = l;
		if (!l) {
			colors = null;
		}
		return this;
	}

	public boolean isLight() {
		return lightingOn;
	}

	public LLight clearLight() {
		if (lights != null) {
			this.lights.clear();
			this.isLightDirty = true;
		}
		return this;
	}

	public LLight updateLight() {
		if (mainLight == null) {
			throw new LSysException("the main light is null !");
		}
		for (int y = 0; y < height + 1; y++) {
			for (int x = 0; x < width + 1; x++) {
				for (int component = 0; component < 3; component++) {
					lightValue[x][y][component] = 0;
				}
				for (int i = 0; i < lights.size; i++) {
					float[] effect = (lights.get(i)).getEffectAt(x, y, colouredLights);
					for (int component = 0; component < 3; component++) {
						lightValue[x][y][component] += effect[component];
					}
				}
				for (int component = 0; component < 3; component++) {
					if (lightValue[x][y][component] > 1) {
						lightValue[x][y][component] = 1;
					}
				}
			}
		}
		this.isLightDirty = true;
		return this;
	}

	// 地图光源用类
	public static class Light extends BaseLight {

		private float xpos;

		private float ypos;

		private float strength;

		public Light(float x, float y, float str, LColor col) {
			this.xpos = x;
			this.ypos = y;
			this.strength = str;
			if (col != null) {
				this.color.setColor(col);
			}
		}

		public Light(float x, float y, float str) {
			this(x, y, str, null);
		}

		public Light setLocation(float x, float y) {
			xpos = x;
			ypos = y;
			return this;
		}

		public float[] getEffectAt(float x, float y, boolean colouredLights) {
			float dx = (x - xpos);
			float dy = (y - ypos);
			float distance2 = (dx * dx) + (dy * dy);
			float effect = 1 - (distance2 / (strength * strength));
			if (effect < 0) {
				effect = 0;
			}
			if (colouredLights) {
				return new float[] { color.r * effect, color.g * effect, color.b * effect };
			} else {
				return new float[] { effect, effect, effect };
			}
		}

	}

}
