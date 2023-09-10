/**
 * Copyright 2008 - 2012
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
 * @version 0.3.3
 */
package loon.particle;

import loon.LTexture;
import loon.canvas.LColor;
import loon.opengl.GLEx;
import loon.utils.MathUtils;

public class ParticleParticle {

	public static final int INHERIT_POINTS = 1;

	public static final int USE_POINTS = 2;

	public static final int USE_QUADS = 3;

	protected float x;

	protected float y;

	protected float velx;

	protected float vely;

	protected float size = 10;

	protected LColor color = LColor.white;

	protected float life;

	protected float originalLife;

	private ParticleSystem engine;

	private ParticleEmitter emitter;

	protected LTexture image;

	protected int type;

	protected int usePoints = INHERIT_POINTS;

	protected boolean oriented = false;

	protected float scaleY = 1.0f;

	public ParticleParticle(ParticleSystem engine) {
		this.engine = engine;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public ParticleParticle move(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public float getSize() {
		return size;
	}

	public LColor getColor() {
		return color;
	}

	public ParticleParticle setImage(LTexture image) {
		this.image = image;
		return this;
	}

	public float getOriginalLife() {
		return originalLife;
	}

	public float getLife() {
		return life;
	}

	public boolean inUse() {
		return life > 0;
	}

	public ParticleParticle paint(GLEx g) {
		if ((engine.usePoints() && (usePoints == INHERIT_POINTS)) || (usePoints == USE_POINTS)) {
			g.drawPoint(velx, scaleY, color.getARGB());
		} else {
			float angle = 0;
			if (oriented) {
				angle = MathUtils.atan2(y, x) * 180 / MathUtils.PI;
			}
			image.draw((x - (size / 2)), (y - (size / 2)), size, size, angle, color);
		}
		return this;
	}

	public ParticleParticle update(long delta) {
		emitter.updateParticle(this, delta);
		life -= delta;
		if (life > 0) {
			x += delta * velx;
			y += delta * vely;
		} else {
			engine.release(this);
		}
		return this;
	}

	public ParticleParticle init(ParticleEmitter emitter, float l) {
		x = 0;
		this.emitter = emitter;
		y = 0;
		velx = 0;
		vely = 0;
		size = 10;
		type = 0;
		this.originalLife = this.life = l;
		oriented = false;
		scaleY = 1.0f;
		return this;
	}

	public ParticleParticle setEffectType(int type) {
		this.type = type;
		return this;
	}

	public ParticleParticle setUsePoint(int usePoints) {
		this.usePoints = usePoints;
		return this;
	}

	public int getEffectType() {
		return type;
	}

	public ParticleParticle setSize(float size) {
		this.size = size;
		return this;
	}

	public ParticleParticle adjustSize(float delta) {
		size += delta;
		size = MathUtils.max(0, size);
		return this;
	}

	public ParticleParticle setLife(float life) {
		this.life = life;
		return this;
	}

	public ParticleParticle adjustLife(float delta) {
		life += delta;
		return this;
	}

	public ParticleParticle kill() {
		life = 1;
		return this;
	}

	public ParticleParticle setColor(float r, float g, float b, float a) {
		if (color.equals(LColor.white)) {
			color = new LColor(r, g, b, a);
		} else {
			color.r = r;
			color.g = g;
			color.b = b;
			color.a = a;
		}
		return this;
	}

	public ParticleParticle setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public ParticleParticle setVelocity(float dirx, float diry, float speed) {
		this.velx = dirx * speed;
		this.vely = diry * speed;
		return this;
	}

	public ParticleParticle setSpeed(float speed) {
		float currentSpeed = MathUtils.sqrt((velx * velx) + (vely * vely));
		velx *= speed;
		vely *= speed;
		velx /= currentSpeed;
		vely /= currentSpeed;
		return this;
	}

	public ParticleParticle setVelocity(float velx, float vely) {
		setVelocity(velx, vely, 1);
		return this;
	}

	public ParticleParticle adjustPosition(float dx, float dy) {
		x += dx;
		y += dy;
		return this;
	}

	public ParticleParticle adjustColor(float r, float g, float b, float a) {
		if (color == null) {
			color = new LColor(1, 1, 1, 1f);
		}
		color.r += r;
		color.g += g;
		color.b += b;
		color.a += a;
		return this;
	}

	public ParticleParticle adjustColor(int r, int g, int b, int a) {
		if (color == null) {
			color = new LColor(1, 1, 1, 1f);
		}

		color.r += (r / 255.0f);
		color.g += (g / 255.0f);
		color.b += (b / 255.0f);
		color.a += (a / 255.0f);
		return this;
	}

	public ParticleParticle adjustVelocity(float dx, float dy) {
		velx += dx;
		vely += dy;
		return this;
	}

	public ParticleEmitter getEmitter() {
		return emitter;
	}

	public boolean isOriented() {
		return oriented;
	}

	public ParticleParticle setOriented(boolean oriented) {
		this.oriented = oriented;
		return this;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}
}
