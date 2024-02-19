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
package loon.opengl;

import loon.LTexture;
import loon.canvas.LColor;
import loon.geom.Affine2f;
import loon.utils.GLUtils;

public abstract class BaseBatch extends LTextureBind {

	protected ShaderSource _shader_source;

	private boolean _shader_ditry = true;

	abstract ShaderProgram createShaderProgram();

	abstract ShaderProgram getShaderProgram();

	public abstract BaseBatch setBlendMode(int b);

	public abstract int getBlendMode();

	public BaseBatch setShaderSource(ShaderSource source) {
		if (source == null) {
			return this;
		}
		if (source.equals(_shader_source)) {
			return this;
		}
		this._shader_source = source;
		this._shader_ditry = true;
		return this;
	}

	public BaseBatch loadShader(String vertexPath, String fragmentPath) {
		return setShaderSource(GLUtils.loadShaderSource(vertexPath, fragmentPath));
	}

	public ShaderSource getShaderSource() {
		return this._shader_source;
	}

	protected void setShaderDirty(boolean dirty) {
		this._shader_ditry = dirty;
	}

	protected boolean isShaderDirty() {
		return this._shader_ditry;
	}

	public void addQuad(LTexture tex, int tint, Affine2f xf, float x, float y, float w, float h) {
		if (tex == null || tex.isClosed()) {
			return;
		}
		if (w < 1f || h < 1f) {
			return;
		}
		if (LColor.getAlpha(tint) <= 0) {
			return;
		}

		setTexture(tex);

		final boolean childTexture = (tex.isScale() || tex.isCopy());

		if (childTexture) {
			float u2 = tex.getFormat().repeatX ? w / tex.width() : tex.widthRatio();
			float uv = tex.getFormat().repeatY ? h / tex.height() : tex.heightRatio();
			addQuad(tint, xf, x, y, x + w, y + h, tex.xOff(), tex.yOff(), u2, uv);
		} else {
			addQuad(tint, xf, x, y, x + w, y + h, 0f, 0f, 1f, 1f);
		}
	}

	public void addQuad(LTexture tex, int tint, Affine2f xf, float dx, float dy, float dw, float dh, float sx, float sy,
			float sw, float sh) {
		if (tex == null || tex.isClosed()) {
			return;
		}
		if (dw < 1f || dh < 1f || sw < 1f || sh < 1f) {
			return;
		}
		if (LColor.getAlpha(tint) <= 0) {
			return;
		}

		setTexture(tex);

		final boolean childTexture = (tex.isScale() || tex.isCopy());

		if (!childTexture) {
			float displayWidth = tex.width();
			float displayHeight = tex.height();
			float xOff = ((sx / displayWidth) * tex.widthRatio()) + tex.xOff();
			float yOff = ((sy / displayHeight) * tex.heightRatio()) + tex.yOff();
			float widthRatio = ((sw / displayWidth) * tex.widthRatio()) + xOff;
			float heightRatio = ((sh / displayHeight) * tex.heightRatio()) + yOff;
			addQuad(tint, xf, dx, dy, dx + dw, dy + dh, xOff, yOff, widthRatio, heightRatio);
		} else {
			LTexture forefather = LTexture.firstFather(tex);
			float displayWidth = forefather.width();
			float displayHeight = forefather.height();
			float xOff = ((sx / displayWidth) * forefather.widthRatio()) + forefather.xOff() + tex.xOff();
			float yOff = ((sy / displayHeight) * forefather.heightRatio()) + forefather.yOff() + tex.yOff();
			float widthRatio = ((sw / displayWidth) * forefather.widthRatio()) + xOff;
			float heightRatio = ((sh / displayHeight) * forefather.heightRatio()) + yOff;
			addQuad(tint, xf, dx, dy, dx + dw, dy + dh, xOff, yOff, widthRatio, heightRatio);
		}
	}

	public void quad(LTexture tex, int tint, Affine2f xf, float x1, float y1, float x2, float y2, float x3, float y3,
			float x4, float y4) {
		if (tex == null || tex.isClosed()) {
			return;
		}
		if (LColor.getAlpha(tint) <= 0) {
			return;
		}
		setTexture(tex);
		quad(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, x1, y1, x2, y2, x3, y3, x4, y4, tex.xOff(), tex.yOff(),
				tex.widthRatio(), tex.heightRatio());
	}

	public void quad(LTexture tex, Affine2f xf, float x1, float y1, float c1, float x2, float y2, float c2, float x3,
			float y3, float c3, float x4, float y4, float c4) {
		if (tex == null || tex.isClosed()) {
			return;
		}
		setTexture(tex);
		quad(xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, x1, y1, c1, x2, y2, c2, x3, y3, c3, x4, y4, c4, tex.xOff(),
				tex.yOff(), tex.widthRatio(), tex.heightRatio());
	}

	public abstract void quad(int tint, float m00, float m01, float m10, float m11, float tx, float ty, float x1,
			float y1, float x2, float y2, float x3, float y3, float x4, float y4, float u, float v, float u2, float v2);

	public abstract void quad(float m00, float m01, float m10, float m11, float tx, float ty, float x1, float y1,
			float c1, float x2, float y2, float c2, float x3, float y3, float c3, float x4, float y4, float c4, float u,
			float v, float u2, float v2);

	public void addQuad(int tint, Affine2f xf, float left, float top, float right, float bottom, float sl, float st,
			float sr, float sb) {
		addQuad(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, left, top, right, bottom, sl, st, sr, sb);
	}

	public void addQuad(int tint, float m00, float m01, float m10, float m11, float tx, float ty, float left, float top,
			float right, float bottom, float sl, float st, float sr, float sb) {
		addQuad(tint, m00, m01, m10, m11, tx, ty, left, top, sl, st, right, top, sr, st, left, bottom, sl, sb, right,
				bottom, sr, sb);
	}

	public abstract void addQuad(int tint, float m00, float m01, float m10, float m11, float tx, float ty, float x1,
			float y1, float sx1, float sy1, float x2, float y2, float sx2, float sy2, float x3, float y3, float sx3,
			float sy3, float x4, float y4, float sx4, float sy4);

	protected BaseBatch(GL20 gl) {
		super(gl);
	}
}
