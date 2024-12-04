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
package loon.action.map.tmx.renderers;

import loon.LSystem;
import loon.LTexture;
import loon.LTextureBatch;
import loon.action.map.tmx.TMXImageLayer;
import loon.action.map.tmx.TMXMap;
import loon.action.map.tmx.TMXTileLayer;
import loon.action.map.tmx.TMXTileSet;
import loon.action.map.tmx.tiles.TMXMapTile;
import loon.action.map.tmx.tiles.TMXTile;
import loon.canvas.LColor;
import loon.geom.Vector2f;
import loon.opengl.GLEx;
import loon.utils.MathUtils;

/**
 * 斜视视角（45度角）交错地图纹理渲染器
 *
 */
public class TMXStaggeredMapRenderer extends TMXMapRenderer {

	public TMXStaggeredMapRenderer(TMXMap map) {
		super(map);
	}

	private Vector2f orthoToIso(float x, float y) {
		_mapLocation.x = (x - y) * map.getTileWidth() / 2 + getRenderX();
		_mapLocation.y = (x + y) * map.getTileHeight() / 2 + getRenderY();
		return _mapLocation.addSelf(map.getWidth() * map.getTileWidth() / 2, 0);
	}

	@Override
	protected void renderImageLayer(GLEx g, TMXImageLayer imageLayer) {
		if (!imageLayer.isVisible()) {
			return;
		}
		LTexture current = textureMap.get(imageLayer.getImage().getSource());
		float tileWidth = map.getTileWidth();
		float tileHeight = map.getTileHeight();
		float posX = (imageLayer.getRenderOffsetY() * tileWidth / 2) + (imageLayer.getRenderOffsetX() * tileWidth / 2)
				+ getRenderX();
		float posY = (imageLayer.getRenderOffsetX() * tileHeight / 2) - (imageLayer.getRenderOffsetY() * tileHeight / 2)
				+ getRenderY();
		g.draw(current, posX, posY, imageLayer.getWidth() * map.getTileWidth(),
				imageLayer.getHeight() * map.getTileHeight(), imageLayer.getTileLayerColor(baseColor));
	}

	@Override
	protected void renderTileLayer(GLEx g, TMXTileLayer tileLayer) {
		synchronized (this) {
			if (!tileLayer.isVisible()) {
				return;
			}
			final int screenWidth = LSystem.viewSize.getWidth();
			final int screenHeight = LSystem.viewSize.getHeight();
			int tx = MathUtils.ifloor(getRenderX() / map.getTileWidth());
			int ty = MathUtils.ifloor(getRenderY() / map.getTileHeight());
			int windowWidth = screenWidth / map.getTileWidth();
			int windowHeight = screenHeight / map.getTileHeight();
			float doubleWidth = tileLayer.getWidth() * 2f;
			float doubleHeight = tileLayer.getHeight() * 2f;

			final int layerWidth = tileLayer.getWidth();
			final int layerHeight = tileLayer.getHeight();

			final float layerTileWidth = tileLayer.getTileWidth();
			final float layerTileHeight = tileLayer.getTileHeight();

			final float layerOffsetX = tileLayer.getRenderOffsetX() - (tileLayer.getParallaxX() - 1f);
			final float layerOffsetY = tileLayer.getRenderOffsetY() - (tileLayer.getParallaxY() - 1f);

			final boolean saveCache = textureMap.size == 1 && allowCache;

			LTexture current = textureMap.get(map.getTileset(0).getImage().getSource());
			LTextureBatch texBatch = current.getTextureBatch();

			boolean isCached = false;
			
			final LColor drawColor = tileLayer.getTileLayerColor(baseColor);

			try {

				if (saveCache) {
					int hashCode = 1;
					hashCode = LSystem.unite(hashCode, tx);
					hashCode = LSystem.unite(hashCode, ty);
					hashCode = LSystem.unite(hashCode, windowWidth);
					hashCode = LSystem.unite(hashCode, windowHeight);
					hashCode = LSystem.unite(hashCode, layerWidth);
					hashCode = LSystem.unite(hashCode, layerHeight);
					hashCode = LSystem.unite(hashCode, layerTileWidth);
					hashCode = LSystem.unite(hashCode, layerTileHeight);
					hashCode = LSystem.unite(hashCode, layerOffsetX);
					hashCode = LSystem.unite(hashCode, layerOffsetY);
					hashCode = LSystem.unite(hashCode, scaleX);
					hashCode = LSystem.unite(hashCode, scaleY);
					hashCode = LSystem.unite(hashCode, tileLayer.isDirty());
					hashCode = LSystem.unite(hashCode, _objectRotation);

					if (isCached = postCache(texBatch, hashCode)) {
						return;
					}

				} else {
					texBatch.begin();
				}
				
				texBatch.setColor(drawColor);
				
				for (int x = 0; x < tileLayer.getWidth(); x++) {
					for (int y = 0; y < tileLayer.getHeight(); y++) {

						if ((tx + x < 0) || (ty + y < 0) || (tx + x >= doubleWidth) || (ty + y >= doubleHeight)) {
							continue;
						}
						if ((tx + x >= windowWidth) || (ty + y >= windowHeight)) {
							continue;
						}

						TMXMapTile mapTile = tileLayer.getTile(x, y);

						if (mapTile.getTileSetID() == -1) {
							continue;
						}

						TMXTileSet tileSet = map.getTileset(mapTile.getTileSetID());
						TMXTile tile = tileSet.getTile(mapTile.getGID() - tileSet.getFirstGID());

						LTexture texture = textureMap.get(tileSet.getImage().getSource());

						if (texture.getID() != current.getID()) {
							texBatch.end();
							current = texture;
							texBatch = current.getTextureBatch();
							texBatch.begin();
							texBatch.checkTexture(current);
						}

						int tileID = mapTile.getGID() - tileSet.getFirstGID();
						if (tile != null && tile.isAnimated()) {
							tileID = tileAnimators.get(tile).getCurrentFrame().getTileID();
						}

						int numColsPerRow = tileSet.getImage().getWidth() / tileSet.getTileWidth();

						int tileSetCol = tileID % numColsPerRow;
						int tileSetRow = tileID / numColsPerRow;

						float tileWidth = tileSet.getTileWidth();
						float tileHeight = tileSet.getTileHeight();

						float srcX = (tileSet.getMargin()
								+ (tileSet.getTileWidth() + tileSet.getSpacing()) * tileSetCol);
						float srcY = (tileSet.getMargin()
								+ (tileSet.getTileHeight() + tileSet.getSpacing()) * tileSetRow);
						float srcWidth = srcX + tileWidth;
						float srcHeight = srcY + tileHeight;

						boolean flipX = mapTile.isFlippedHorizontally();
						boolean flipY = mapTile.isFlippedVertically();
						boolean flipZ = mapTile.isFlippedDiagonally();

						if (flipZ) {
							flipX = !flipX;
							flipY = !flipY;
						}
						Vector2f pos = orthoToIso(x, y);
						texBatch.draw(pos.x, pos.y, -1f, -1f, 0f, 0f, tileWidth, tileHeight, scaleX, scaleY,
								this._objectRotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);

					}
				}
			} finally {
				if (!isCached) {
					texBatch.end();
					if (saveCache) {
						saveCache(texBatch);
					}
				}
			}
		}
	}

}
