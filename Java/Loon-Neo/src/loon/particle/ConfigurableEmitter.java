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
package loon.particle;

import loon.LTexture;
import loon.LSystem;
import loon.canvas.LColor;
import loon.geom.RangeF;
import loon.geom.Vector2f;
import loon.utils.MathUtils;
import loon.utils.StringUtils;
import loon.utils.TArray;

public class ConfigurableEmitter implements ParticleEmitter {

	private String relativePath = "";

	public void setRelativePath(String path) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		relativePath = path;
	}

	public RangeF spawnInterval = new RangeF(100, 100);

	public RangeF spawnCount = new RangeF(5, 5);

	public RangeF initialLife = new RangeF(1000, 1000);

	public RangeF initialSize = new RangeF(10, 10);

	public RangeF xOffset = new RangeF(0, 0);

	public RangeF yOffset = new RangeF(0, 0);

	public RandomValue spread = new RandomValue(360);

	public SimpleValue angularOffset = new SimpleValue(0);

	public RangeF initialDistance = new RangeF(0, 0);

	public RangeF speed = new RangeF(50, 50);

	public SimpleValue growthFactor = new SimpleValue(0);

	public SimpleValue gravityFactor = new SimpleValue(0);

	public SimpleValue windFactor = new SimpleValue(0);

	public RangeF length = new RangeF(1000, 1000);

	public TArray<ColorRecord> colors = new TArray<ColorRecord>();

	public SimpleValue startAlpha = new SimpleValue(255);

	public SimpleValue endAlpha = new SimpleValue(0);

	public LinearInterpolator alpha;

	public LinearInterpolator size;

	public LinearInterpolator velocity;

	public LinearInterpolator scaleY;

	public RangeF emitCount = new RangeF(1000, 1000);

	public int usePoints = ParticleParticle.INHERIT_POINTS;

	public boolean useOriented = false;

	public boolean useAdditive = false;

	public String name;

	public String imageName = "";

	private LTexture image;

	private boolean updateImage;

	private boolean enabled = true;

	private float x;

	private float y;

	private int nextSpawn = 0;

	private int timeout;

	private int particleCount;

	private ParticleSystem engine;

	private int leftToEmit;

	protected boolean wrapUp = false;

	protected boolean completed = false;

	protected boolean adjust;

	protected float adjustx;

	protected float adjusty;

	public ConfigurableEmitter(String name) {
		this.name = name;
		leftToEmit = (int) emitCount.random();
		timeout = (int) (length.random());

		colors.add(new ColorRecord(0, LColor.white));
		colors.add(new ColorRecord(1, LColor.red));

		TArray<Vector2f> curve = new TArray<Vector2f>();
		curve.add(new Vector2f(0.0f, 0.0f));
		curve.add(new Vector2f(1.0f, 255.0f));
		alpha = new LinearInterpolator(curve, 0, 255);

		curve = new TArray<Vector2f>();
		curve.add(new Vector2f(0.0f, 0.0f));
		curve.add(new Vector2f(1.0f, 255.0f));
		size = new LinearInterpolator(curve, 0, 255);

		curve = new TArray<Vector2f>();
		curve.add(new Vector2f(0.0f, 0.0f));
		curve.add(new Vector2f(1.0f, 1.0f));
		velocity = new LinearInterpolator(curve, 0, 1);

		curve = new TArray<Vector2f>();
		curve.add(new Vector2f(0.0f, 0.0f));
		curve.add(new Vector2f(1.0f, 1.0f));
		scaleY = new LinearInterpolator(curve, 0, 1);
	}

	public ConfigurableEmitter setImageName(String imageName) {
		if (StringUtils.isEmpty(imageName)) {
			imageName = null;
		}

		this.imageName = imageName;
		if (imageName == null) {
			image = null;
		} else {
			updateImage = true;
		}
		return this;
	}

	public String getImageName() {
		return imageName;
	}

	public void setPosition(float x, float y) {
		setPosition(x, y, true);
	}

	public void setPosition(float x, float y, boolean moveParticles) {
		if (moveParticles) {
			adjust = true;
			adjustx -= this.x - x;
			adjusty -= this.y - y;
		}
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void update(ParticleSystem system, long delta) {
		this.engine = system;

		if (!adjust) {
			adjustx = 0;
			adjusty = 0;
		} else {
			adjust = false;
		}

		if (updateImage) {
			updateImage = false;

			image = LSystem.loadTexture(relativePath + imageName);

		}

		if ((wrapUp) || ((length.isEnabled()) && (timeout < 0)) || ((emitCount.isEnabled() && (leftToEmit <= 0)))) {
			if (particleCount == 0) {
				completed = true;
			}
		}
		particleCount = 0;

		if (wrapUp) {
			return;
		}

		if (length.isEnabled()) {
			if (timeout < 0) {
				return;
			}
			timeout -= delta;
		}
		if (emitCount.isEnabled()) {
			if (leftToEmit <= 0) {
				return;
			}
		}

		nextSpawn -= delta;
		if (nextSpawn < 0) {
			nextSpawn = (int) spawnInterval.random();
			int count = (int) spawnCount.random();

			for (int i = 0; i < count; i++) {
				ParticleParticle p = system.getNewParticle(this, initialLife.random());
				p.setSize(initialSize.random());
				p.setPosition(x + xOffset.random(), y + yOffset.random());
				p.setVelocity(0, 0, 0);

				float dist = initialDistance.random();
				float power = speed.random();
				if ((dist != 0) || (power != 0)) {
					float s = spread.getValue(0);
					float ang = (s + angularOffset.getValue(0) - (spread.getValue() / 2)) - 90;

					float xa = (MathUtils.cos(MathUtils.toDegrees(ang)) * dist);
					float ya = (MathUtils.sin(MathUtils.toDegrees(ang)) * dist);
					p.adjustPosition(xa, ya);

					float xv = MathUtils.cos(MathUtils.toDegrees(ang));
					float yv = MathUtils.sin(MathUtils.toDegrees(ang));
					p.setVelocity(xv, yv, power * 0.001f);
				}

				if (image != null) {
					p.setImage(image);
				}

				ColorRecord start = (ColorRecord) colors.get(0);
				p.setColor(start.col.r, start.col.g, start.col.b, startAlpha.getValue(0) / 255.0f);
				p.setUsePoint(usePoints);
				p.setOriented(useOriented);

				if (emitCount.isEnabled()) {
					leftToEmit--;
					if (leftToEmit <= 0) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void updateParticle(ParticleParticle particle, long delta) {
		particleCount++;

		particle.x += adjustx;
		particle.y += adjusty;

		particle.adjustVelocity(windFactor.getValue(0) * 0.00005f * delta,
				gravityFactor.getValue(0) * 0.00005f * delta);

		float offset = particle.getLife() / particle.getOriginalLife();
		float inv = 1 - offset;
		float colOffset = 0;
		float colInv = 1;

		LColor startColor = null;
		LColor endColor = null;
		for (int i = 0; i < colors.size - 1; i++) {
			ColorRecord rec1 = (ColorRecord) colors.get(i);
			ColorRecord rec2 = (ColorRecord) colors.get(i + 1);

			if ((inv >= rec1.pos) && (inv <= rec2.pos)) {
				startColor = rec1.col;
				endColor = rec2.col;

				float step = rec2.pos - rec1.pos;
				colOffset = inv - rec1.pos;
				colOffset /= step;
				colOffset = 1 - colOffset;
				colInv = 1 - colOffset;
			}
		}

		if (startColor != null) {
			float r = (startColor.r * colOffset) + (endColor.r * colInv);
			float g = (startColor.g * colOffset) + (endColor.g * colInv);
			float b = (startColor.b * colOffset) + (endColor.b * colInv);

			float a;
			if (alpha.isActive()) {
				a = alpha.getValue(inv) / 255.0f;
			} else {
				a = ((startAlpha.getValue(0) / 255.0f) * offset) + ((endAlpha.getValue(0) / 255.0f) * inv);
			}
			particle.setColor(r, g, b, a);
		}

		if (size.isActive()) {
			float s = size.getValue(inv);
			particle.setSize(s);
		} else {
			particle.adjustSize(delta * growthFactor.getValue(0) * 0.001f);
		}

		if (velocity.isActive()) {
			particle.setSpeed(velocity.getValue(inv));
		}

		if (scaleY.isActive()) {
			particle.setScaleY(scaleY.getValue(inv));
		}
	}

	@Override
	public boolean completed() {
		if (engine == null) {
			return false;
		}

		if (length.isEnabled()) {
			if (timeout > 0) {
				return false;
			}
			return completed;
		}
		if (emitCount.isEnabled()) {
			if (leftToEmit > 0) {
				return false;
			}
			return completed;
		}

		if (wrapUp) {
			return completed;
		}

		return false;
	}

	public ConfigurableEmitter replay() {
		reset();
		nextSpawn = 0;
		leftToEmit = (int) emitCount.random();
		timeout = (int) (length.random());
		return this;
	}

	public ConfigurableEmitter reset() {
		completed = false;
		if (engine != null) {
			engine.releaseAll(this);
		}
		return this;
	}

	public ConfigurableEmitter replayCheck() {
		if (completed()) {
			if (engine != null) {
				if (engine.getParticleCount() == 0) {
					replay();
				}
			}
		}
		return this;
	}

	public interface Value {
		public float getValue(float time);
	}

	public class SimpleValue implements Value {

		private float value;

		private SimpleValue(float value) {
			this.value = value;
		}

		@Override
		public float getValue(float time) {
			return value;
		}

		public SimpleValue setValue(float value) {
			this.value = value;
			return this;
		}
	}

	public class RandomValue implements Value {

		private float value;

		private RandomValue(float value) {
			this.value = value;
		}

		public float getValue(float time) {
			return (MathUtils.random() * value);
		}

		public RandomValue setValue(float value) {
			this.value = value;
			return this;
		}

		public float getValue() {
			return value;
		}
	}

	public class LinearInterpolator implements Value {

		private TArray<Vector2f> curve;

		private boolean active;

		private int min;

		private int max;

		public LinearInterpolator(TArray<Vector2f> curve, int min, int max) {
			this.curve = curve;
			this.min = min;
			this.max = max;
			this.active = false;
		}

		public LinearInterpolator setCurve(TArray<Vector2f> curve) {
			this.curve = curve;
			return this;
		}

		public TArray<Vector2f> getCurve() {
			return curve;
		}

		@Override
		public float getValue(float t) {
			Vector2f p0 = (Vector2f) curve.get(0);
			for (int i = 1; i < curve.size; i++) {
				Vector2f p1 = (Vector2f) curve.get(i);
				if (t >= p0.getX() && t <= p1.getX()) {
					float st = (t - p0.getX()) / (p1.getX() - p0.getX());
					float r = p0.getY() + st * (p1.getY() - p0.getY());
					return r;
				}

				p0 = p1;
			}
			return 0;
		}

		public boolean isActive() {
			return active;
		}

		public LinearInterpolator setActive(boolean active) {
			this.active = active;
			return this;
		}

		public int getMax() {
			return max;
		}

		public LinearInterpolator setMax(int max) {
			this.max = max;
			return this;
		}

		public int getMin() {
			return min;
		}

		public LinearInterpolator setMin(int min) {
			this.min = min;
			return this;
		}
	}

	public class ColorRecord {

		public float pos;

		public LColor col;

		public ColorRecord(float pos, LColor col) {
			this.pos = pos;
			this.col = col;
		}
	}

	public ConfigurableEmitter addColorPoint(float pos, LColor col) {
		colors.add(new ColorRecord(pos, col));
		return this;
	}

	@Override
	public boolean useAdditive() {
		return useAdditive;
	}

	@Override
	public boolean isOriented() {
		return this.useOriented;
	}

	@Override
	public boolean usePoints(ParticleSystem system) {
		return (this.usePoints == ParticleParticle.INHERIT_POINTS) && (system.usePoints())
				|| (this.usePoints == ParticleParticle.USE_POINTS);
	}

	@Override
	public LTexture getImage() {
		return image;
	}

	@Override
	public void up() {
		wrapUp = true;
	}

	public void resetState() {
		wrapUp = false;
		replay();
	}

	@Override
	public String toString() {
		return "SimpleConfigurableEmitter [" + name + "]";
	}

}
