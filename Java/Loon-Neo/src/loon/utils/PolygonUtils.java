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
package loon.utils;

import loon.action.collision.CollisionHelper;
import loon.canvas.LColor;
import loon.geom.Polygon;
import loon.geom.Vector2f;
import loon.opengl.GLEx;

/**
 * 一个不规则形状批量随机生成器，可以充当陨石或者岛屿领地范围之类的自动生成
 */
public class PolygonUtils {

	public enum RegionShape {
		CIRCLE, ELLIPSE, SQUARE, NATURAL, ADAPTIVE
	}

	public static class RegionConfig {
		public String name;
		public float x;
		public float y;
		public float width;
		public float height;
		public RegionShape shape;
		public float edgeRoughness;

		public RegionConfig(String name, float x, float y, float width, float height, RegionShape shape,
				float edgeRoughness) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.shape = shape;
			this.edgeRoughness = MathUtils.clamp(edgeRoughness, 0, 1);
		}

		public RegionConfig(String name, float x, float y, float width, float height, RegionShape shape) {
			this(name, x, y, width, height, shape, 0.15f);
		}

		public RegionConfig(String name, float x, float y, float width, float height) {
			this(name, x, y, width, height, RegionShape.ADAPTIVE, 0.15f);
		}

		public float getCenterX() {
			return x + width * 0.5f;
		}

		public float getCenterY() {
			return y + height * 0.5f;
		}
	}

	public static class PolygonRegionData {

		public int id;
		public String name;
		public float[] verts;
		public float[] renderVerts;
		public LColor fillColor = new LColor();
		public boolean visible = true;
		public boolean isSelected = false;
		public float centroidX, centroidY;
		public float skewX, skewY;

		public PolygonRegionData(int id, String name, float[] verts) {
			this.id = id;
			this.name = name;
			this.verts = verts;
			this.renderVerts = verts.clone();
			this.fillColor.setColor(MathUtils.random() * 0.4f + 0.3f, MathUtils.random() * 0.5f + 0.4f,
					MathUtils.random() * 0.3f + 0.4f, 1f);
			calcCentroid();
			calcSkewPos(0.25f);
		}

		public void scale(float scale) {
			renderVerts = scalePolygon(verts, centroidX, centroidY, scale);
			isSelected = true;
		}

		public void resetScale() {
			renderVerts = verts.clone();
			isSelected = false;
		}

		private void calcCentroid() {
			Vector2f c = MathUtils.computeCentroid(verts);
			centroidX = c.x;
			centroidY = c.y;
		}

		private void calcSkewPos(float skew) {
			skewX = centroidX + centroidY * skew;
			skewY = centroidY;
		}
	}

	public static class PolygonRegion {

		final IntMap<PolygonRegionData> regions = new IntMap<PolygonRegionData>();

		private int _nextId = 1;

		private Polygon _polygon = new Polygon();

		public int addRegion(String name, float[] verts) {
			int id = _nextId++;
			PolygonRegionData r = new PolygonRegionData(id, name, verts);
			regions.put(id, r);
			return id;
		}

		public PolygonRegionData getRegion(int id) {
			return regions.get(id);
		}

		public PolygonRegionData getRegionAtPoint(float x, float y) {
			for (int key : regions.keys()) {
				PolygonRegionData r = regions.get(key);
				if (r.visible && CollisionHelper.checkPointvsPolygon(x, y, r.verts, 1f)) {
					return r;
				}
			}
			return null;
		}

		public void clear() {
			regions.clear();
			_nextId = 1;
		}

		public void draw(GLEx g) {
			for (int key : regions.keys()) {
				PolygonRegionData region = regions.get(key);
				if (!region.visible) {
					continue;
				}
				_polygon.setPolygon(region.renderVerts, region.renderVerts.length);
				g.draw(_polygon);
			}
		}

		public void snapAndRebuildRegions(float snapEps) {
			TArray<Vector2f> all = new TArray<Vector2f>();
			IntMap<TArray<Integer>> regionIndices = new IntMap<TArray<Integer>>();
			for (int key : regions.keys()) {
				PolygonRegionData r = regions.get(key);
				TArray<Integer> idxs = new TArray<Integer>();
				for (int i = 0; i < r.verts.length; i += 2) {
					all.add(Vector2f.at(r.verts[i], r.verts[i + 1]));
					idxs.add(all.size - 1);
				}
				regionIndices.put(key, idxs);
			}

			int m = all.size();
			if (m == 0) {
				return;
			}
			int[] parent = new int[m];
			for (int i = 0; i < m; i++) {
				parent[i] = i;
			}
			float eps2 = snapEps * snapEps;
			for (int i = 0; i < m; i++) {
				if (parent[i] != i) {
					continue;
				}
				Vector2f a = all.get(i);
				for (int j = i + 1; j < m; j++) {
					if (parent[j] != j) {
						continue;
					}
					Vector2f b = all.get(j);
					float dx = a.x - b.x, dy = a.y - b.y;
					if (dx * dx + dy * dy <= eps2) {
						parent[j] = i;
					}
				}
			}

			ObjectMap<Integer, Vector2f> repSum = new ObjectMap<Integer, Vector2f>();
			ObjectMap<Integer, Integer> repCount = new ObjectMap<Integer, Integer>();
			for (int i = 0; i < m; i++) {
				int p = parent[i];
				Vector2f v = all.get(i);
				if (!repSum.containsKey(p)) {
					repSum.put(p, Vector2f.at(0, 0));
				}
				Vector2f s = repSum.get(p);
				s.x += v.x;
				s.y += v.y;
				repSum.put(p, s);
				repCount.put(p, repCount.getOrDefault(p, 0) + 1);
			}

			ObjectMap<Integer, Vector2f> repCoord = new ObjectMap<>();
			for (ObjectMap.Entry<Integer, Vector2f> e : repSum.entries()) {
				int k = e.key;
				Vector2f s = e.value;
				int c = repCount.get(k);
				repCoord.put(k, Vector2f.at(s.x / c, s.y / c));
			}

			for (int key : regions.keys()) {
				PolygonRegionData r = regions.get(key);
				TArray<Integer> idxs = regionIndices.get(key);
				TArray<Vector2f> newPts = new TArray<>();
				for (int id : idxs) {
					int p = parent[id];
					Vector2f v = repCoord.get(p);
					newPts.add(Vector2f.at(v.x, v.y));
				}
				float[] arr = new float[newPts.size * 2];
				for (int i = 0; i < newPts.size; i++) {
					arr[i * 2] = newPts.get(i).x;
					arr[i * 2 + 1] = newPts.get(i).y;
				}
				float[] cleaned = CollisionHelper.removeCollinear(arr, 0.5f);
				r.verts = cleaned;
				r.renderVerts = cleaned.clone();
				r.calcCentroid();
			}
		}
	}

	public static void setGlobalEdgeRoughness(RegionConfig[] configs, float roughness) {
		if (configs == null) {
			return;
		}
		for (RegionConfig cfg : configs) {
			cfg.edgeRoughness = MathUtils.clamp(roughness, 0, 1);
		}
	}

	public static PolygonRegion createMultiPolygon(RegionConfig... configs) {
		PolygonRegion map = new PolygonRegion();
		if (configs == null || configs.length == 0 || configs.length > 7) {
			return map;
		}
		for (int i = 0; i < configs.length; i++) {
			RegionConfig cfg = configs[i];
			float[] poly = generateShape(cfg, configs, i);
			poly = CollisionHelper.removeCollinear(poly, 0.5f);
			map.addRegion(cfg.name, poly);
		}
		map.snapAndRebuildRegions(1e-2f);
		return map;
	}

	private static float[] generateShape(RegionConfig self, RegionConfig[] allConfigs, int index) {
		switch (self.shape) {
		case CIRCLE:
			return createCircle(self);
		case ELLIPSE:
			return createEllipse(self);
		case SQUARE:
			return createSquare(self);
		case NATURAL:
			return createNatural(self);
		case ADAPTIVE:
		default:
			return createAdaptiveShape(self, allConfigs, index);
		}
	}

	private static float[] createCircle(RegionConfig cfg) {
		int detail = 64;
		TArray<Vector2f> pts = new TArray<>(detail);
		float cx = cfg.getCenterX();
		float cy = cfg.getCenterY();
		float r = MathUtils.min(cfg.width, cfg.height) * 0.5f;

		for (int i = 0; i < detail; i++) {
			float a = MathUtils.TWO_PI * i / detail;
			float x = cx + MathUtils.cos(a) * r;
			float y = cy + MathUtils.sin(a) * r;
			pts.add(Vector2f.at(x, y));
		}
		return toFloatArray(pts);
	}

	private static float[] createEllipse(RegionConfig cfg) {
		int detail = 64;
		TArray<Vector2f> pts = new TArray<>(detail);
		float cx = cfg.getCenterX();
		float cy = cfg.getCenterY();
		float rx = cfg.width * 0.5f;
		float ry = cfg.height * 0.5f;

		for (int i = 0; i < detail; i++) {
			float a = MathUtils.TWO_PI * i / detail;
			float x = cx + MathUtils.cos(a) * rx;
			float y = cy + MathUtils.sin(a) * ry;
			pts.add(Vector2f.at(x, y));
		}
		return toFloatArray(pts);
	}

	private static float[] createSquare(RegionConfig cfg) {
		TArray<Vector2f> pts = new TArray<>(4);
		float x1 = cfg.x;
		float y1 = cfg.y;
		float x2 = cfg.x + cfg.width;
		float y2 = cfg.y + cfg.height;

		pts.add(Vector2f.at(x1, y1));
		pts.add(Vector2f.at(x2, y1));
		pts.add(Vector2f.at(x2, y2));
		pts.add(Vector2f.at(x1, y2));
		return toFloatArray(pts);
	}

	private static float[] createNatural(RegionConfig cfg) {
		int detail = 64;
		TArray<Vector2f> pts = new TArray<Vector2f>(detail);
		float cx = cfg.getCenterX();
		float cy = cfg.getCenterY();
		float rx = cfg.width * 0.5f;
		float ry = cfg.height * 0.5f;
		float noiseScale = cfg.edgeRoughness;

		for (int i = 0; i < detail; i++) {
			float a = MathUtils.TWO_PI * i / detail;
			float noise = 1f + (angleNoise(a, detail) - 0.5f) * noiseScale;
			float x = cx + MathUtils.cos(a) * rx * noise;
			float y = cy + MathUtils.sin(a) * ry * noise;
			pts.add(Vector2f.at(x, y));
		}
		return toFloatArray(pts);
	}

	private static float[] createAdaptiveShape(RegionConfig self, RegionConfig[] allConfigs, int index) {
		int detail = 64;
		TArray<Vector2f> pts = new TArray<Vector2f>(detail);
		int n = allConfigs.length;

		float cx = self.getCenterX();
		float cy = self.getCenterY();
		float halfW = self.width * 0.5f;
		float halfH = self.height * 0.5f;
		float noiseScale = self.edgeRoughness;

		for (int i = 0; i < detail; i++) {
			float angle = MathUtils.TWO_PI * i / detail;
			float cosA = MathUtils.cos(angle);
			float sinA = MathUtils.sin(angle);

			float baseDist = MathUtils.hypot(cosA / halfW, sinA / halfH);
			float baseRadius = 1.0f / baseDist;
			float finalRadius = baseRadius;

			for (int j = 0; j < n; j++) {
				if (j == index) {
					continue;
				}
				RegionConfig other = allConfigs[j];

				float ocx = other.getCenterX();
				float ocy = other.getCenterY();
				float dist = MathUtils.hypot(ocx - cx, ocy - cy);
				if (dist < 1f) {
					continue;
				}
				float oHalfW = other.width * 0.5f;
				float oHalfH = other.height * 0.5f;
				float dirAngle = MathUtils.atan2(ocy - cy, ocx - cx);
				float dirCos = MathUtils.cos(dirAngle);
				float dirSin = MathUtils.sin(dirAngle);

				float selfDirR = 1f / MathUtils.hypot(dirCos / halfW, dirSin / halfH);
				float otherDirR = 1f / MathUtils.hypot(dirCos / oHalfW, dirSin / oHalfH);
				float safeLimit = dist * (selfDirR / (selfDirR + otherDirR));

				if (safeLimit < finalRadius) {
					finalRadius = safeLimit;
				}
			}

			float noise = 1f + (angleNoise(angle, detail) - 0.5f) * noiseScale;
			finalRadius *= noise;
			finalRadius = MathUtils.max(finalRadius, MathUtils.min(halfW, halfH) * 0.25f);

			float px = cx + cosA * finalRadius;
			float py = cy + sinA * finalRadius;
			pts.add(Vector2f.at(px, py));
		}
		return toFloatArray(pts);
	}

	public static PolygonRegion createSinglePolygon(float x, float y, float w, float h, String name, RegionShape shape,
			float roughness) {
		return createMultiPolygon(new RegionConfig(name, x, y, w, h, shape, roughness));
	}

	public static PolygonRegion createSinglePolygon(float x, float y, float w, float h, String name,
			RegionShape shape) {
		return createMultiPolygon(new RegionConfig(name, x, y, w, h, shape));
	}

	public static PolygonRegion createSinglePolygon(float x, float y, float w, float h, String name) {
		return createMultiPolygon(new RegionConfig(name, x, y, w, h));
	}

	private static float angleNoise(float angle, int detail) {
		float a1 = MathUtils.cos(angle * 2.2f);
		float a2 = MathUtils.sin(angle * 3.7f);
		float seed = MathUtils.sin(angle * 13.9898f) * 43758.5453f;
		float r = seed - MathUtils.floor(seed);
		return 0.5f * (0.5f * (a1 + a2) + r);
	}

	private static float[] toFloatArray(TArray<Vector2f> points) {
		float[] res = new float[points.size * 2];
		for (int i = 0; i < points.size; i++) {
			res[i * 2] = points.get(i).x;
			res[i * 2 + 1] = points.get(i).y;
		}
		return res;
	}

	private static float[] scalePolygon(float[] verts, float cx, float cy, float scale) {
		float[] res = new float[verts.length];
		for (int i = 0; i < verts.length; i += 2) {
			res[i] = cx + (verts[i] - cx) * scale;
			res[i + 1] = cy + (verts[i + 1] - cy) * scale;
		}
		return res;
	}

}
