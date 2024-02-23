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
import loon.action.map.tmx.TMXImageLayer;
import loon.action.map.tmx.TMXMap;
import loon.action.map.tmx.TMXTileLayer;
import loon.action.map.tmx.TMXTileSet;
import loon.action.map.tmx.tiles.TMXMapTile;
import loon.action.map.tmx.tiles.TMXTile;
import loon.geom.Vector2f;
import loon.opengl.BlendState;
import loon.opengl.GLEx;

/**
 * 标准斜视视角（标准45度角）地图纹理渲染器
 *
 */
public class TMXIsometricMapRenderer extends TMXMapRenderer {

	public TMXIsometricMapRenderer(TMXMap map) {
		super(map);
	}

	private Vector2f orthoToIso(float x, float y) {
		_mapLocation.x = (x - y) * map.getTileWidth() / 2f + _objectLocation.x;
		_mapLocation.y = (x + y) * map.getTileHeight() / 2f + _objectLocation.y;
		return _mapLocation.addSelf(map.getWidth() * map.getTileWidth() / 2f, 0);
	}

	@Override
	protected void renderImageLayer(GLEx g, TMXImageLayer imageLayer) {
		if (!imageLayer.isVisible()) {
			return;
		}
		float opacity = imageLayer.getOpacity();
		if (opacity <= 0f) {
			return;
		}
		if (opacity > 1f) {
			opacity = 1f;
		}
		float tmpAlpha = baseColor.a;
		baseColor.a *= opacity;
		LTexture texCurrent = textureMap.get(imageLayer.getImage().getSource());
		float tileWidth = map.getTileWidth();
		float tileHeight = map.getTileHeight();
		float posX = (imageLayer.getRenderOffsetY() * tileWidth / 2) + (imageLayer.getRenderOffsetX() * tileWidth / 2)
				+ getRenderX();
		float posY = (imageLayer.getRenderOffsetX() * tileHeight / 2) - (imageLayer.getRenderOffsetY() * tileHeight / 2)
				+ getRenderY();
		g.draw(texCurrent, posX, posY, imageLayer.getWidth() * map.getTileWidth(),
				imageLayer.getHeight() * map.getTileHeight(), baseColor);
		baseColor.a = tmpAlpha;
	}

	@Override
	protected void renderTileLayer(GLEx g, TMXTileLayer tileLayer) {
		synchronized (this) {
			if (!tileLayer.isVisible()) {
				return;
			}
			float opacity = tileLayer.getOpacity();
			if (opacity <= 0f) {
				return;
			}
			if (opacity > 1f) {
				opacity = 1f;
			}

			final int screenWidth = LSystem.viewSize.getWidth();
			final int screenHeight = LSystem.viewSize.getHeight();

			final int tx = (int) (getRenderX() / map.getTileWidth());
			final int ty = (int) (getRenderY() / map.getTileHeight());
			final float windowWidth = screenWidth / map.getTileWidth() * 2f;
			final float windowHeight = screenHeight / map.getTileHeight() * 2f;
			final float doubleWidth = tileLayer.getWidth() * 2f;
			final float doubleHeight = tileLayer.getHeight() * 2f;

			final int layerWidth = tileLayer.getWidth();
			final int layerHeight = tileLayer.getHeight();

			final float layerTileWidth = tileLayer.getTileWidth();
			final float layerTileHeight = tileLayer.getTileHeight();

			final float layerOffsetX = tileLayer.getRenderOffsetX() - (tileLayer.getParallaxX() - 1f);
			final float layerOffsetY = tileLayer.getRenderOffsetY() - (tileLayer.getParallaxY() - 1f);

			final boolean saveCache = textureMap.size == 1 && allowCache;

			_texCurrent = textureMap.get(map.getTileset(0).getImage().getSource());
			_texBatch = _texCurrent.getTextureBatch();

			final float tmpAlpha = baseColor.a;
			boolean isCached = false;
			baseColor.a *= opacity;

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

					if (isCached = postCache(_texBatch, hashCode)) {
						return;
					}

				} else {
					_texBatch.begin();
				}

				_texBatch.setBlendState(BlendState.AlphaBlend);
				_texBatch.setColor(baseColor);

				for (int x = 0; x < tileLayer.getWidth(); x++) {
					for (int y = 0; y < tileLayer.getHeight(); y++) {
						if ((tx + x < 0) || (ty + y < 0)) {
							continue;
						}
						if ((tx + x >= doubleWidth) || (ty + y >= doubleHeight)) {
							continue;
						}
						if ((tx + x >= windowWidth) || (ty + y >= windowHeight)) {
							continue;
						}
						drawTile(tileLayer, x, y);
					}
				}

			} finally {
				if (!isCached) {
					_texBatch.end();
					if (saveCache) {
						saveCache(_texBatch);
					}
				}
				baseColor.a = tmpAlpha;
			}
		}
	}

	private void drawTile(final TMXTileLayer tileLayer, final int x, final int y) {

		TMXMapTile mapTile = tileLayer.getTile(x, y);

		if (mapTile.getTileSetID() == -1) {
			return;
		}

		TMXTileSet tileSet = map.getTileset(mapTile.getTileSetID());
		TMXTile tile = tileSet.getTile(mapTile.getGID() - tileSet.getFirstGID());

		LTexture texture = textureMap.get(tileSet.getImage().getSource());

		if (texture.getID() != _texCurrent.getID()) {
			_texBatch.end();
			_texCurrent = texture;
			_texBatch = _texCurrent.getTextureBatch();
			_texBatch.begin();
			_texBatch.setBlendState(BlendState.AlphaBlend);
			_texBatch.checkTexture(_texCurrent);
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

		float srcX = (tileSet.getMargin() + (tileSet.getTileWidth() + tileSet.getSpacing()) * tileSetCol);
		float srcY = (tileSet.getMargin() + (tileSet.getTileHeight() + tileSet.getSpacing()) * tileSetRow);
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

		_texBatch.draw(pos.x, pos.y, tileWidth, tileHeight, scaleX, scaleY, this._objectRotation, srcX, srcY, srcWidth,
				srcHeight, flipX, flipY);
	}

}
