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
package loon.action.map.battle;

import loon.Json;
import loon.LSystem;
import loon.action.sprite.Animation;
import loon.canvas.LColor;
import loon.utils.ISOUtils.IsoConfig;
import loon.utils.StringUtils;
import loon.utils.timer.Duration;

public class BattleTileJsonParser {

	public static BattleTile parseTile(Json.Object rootJson, IsoConfig isoConfig,
			BattleTile.EffectService effectService, BattleTile.SkillService skillService) {
		int gridX = rootJson.getInt("gridX", 0);
		int gridY = rootJson.getInt("gridY", 0);
		int cellWidth = rootJson.getInt("cellWidth", LSystem.LAYER_TILE_SIZE);
		int cellHeight = rootJson.getInt("cellHeight", LSystem.LAYER_TILE_SIZE);
		int layerHeight = rootJson.getInt("layerHeight", 0);
		int layerIndex = rootJson.getInt("layerIndex", 0);
		BattleTile tile = new BattleTile(gridX, gridY, cellWidth, cellHeight, isoConfig, effectService, skillService);
		parseTileType(rootJson, tile);
		parseAnimations(rootJson, tile);
		parseOffsets(rootJson, tile);
		parseTransform(rootJson, tile);
		parseRender(rootJson, tile);
		parseInteract(rootJson, tile);
		parseCommon(rootJson, tile);
		tile.setLayerHeight(layerHeight);
		tile.setLayerIndex(layerIndex);
		return tile;
	}

	private static void parseTileType(Json.Object root, BattleTile tile) {
		int typeId = root.getInt("tileType", BattleTileType.PLAIN.getId());
		int originalId = root.getInt("originalType", BattleTileType.PLAIN.getId());
		try {
			tile.setTileType(BattleTileType.getById(typeId));
			tile.setOriginalType(BattleTileType.getById(originalId));
		} catch (Exception e) {
			tile.setTileType(BattleTileType.PLAIN);
			tile.setOriginalType(BattleTileType.PLAIN);
		}
		String terrainEffectStr = root.getString("terrainEffect", BattleTerrainEffect.NONE.getName());
		try {
			tile.setTerrainEffect(BattleTerrainEffect.valueOf(terrainEffectStr));
		} catch (Exception e) {
			tile.setTerrainEffect(BattleTerrainEffect.NONE);
		}

		tile.setSpecialEffectDuration(root.getNumber("specialEffectDuration", 0));
	}

	private static void parseAnimations(Json.Object root, BattleTile tile) {
		Json.Object animObj = root.getObject("animation");
		if (animObj == null) {
			return;
		}
		Json.Object bgAnim = animObj.getObject("bgAnim");
		tile.setBgAnim(createAnimation(bgAnim));
		Json.Object groundAnim = animObj.getObject("groundAnim");
		tile.setGroundAnim(createAnimation(groundAnim));
		Json.Object effectAnim = animObj.getObject("effectAnim");
		tile.setEffectAnim(createAnimation(effectAnim));
	}

	private static Animation createAnimation(Json.Object animJson) {
		if (animJson == null) {
			return null;
		}
		String assetPath = animJson.getString("path");
		if (StringUtils.isEmpty(assetPath)) {
			return null;
		}
		int frameWidth = animJson.getInt("width", 1);
		int frameHeight = animJson.getInt("height", 1);
		float frameDuration = animJson.getNumber("speed", 0.5f);
		int loop = animJson.getInt("loop", -1);
		Animation animation = Animation.getDefaultAnimation(assetPath, frameWidth, frameHeight,
				Duration.ofS(frameDuration));
		animation.setLoopCount(loop);
		return animation;
	}

	private static void parseOffsets(Json.Object root, BattleTile tile) {
		Json.Object offsetObj = root.getObject("offset");
		if (offsetObj == null) {
			return;
		}
		tile.setBaseLayerOffset(offsetObj.getNumber("baseOffsetX", 0f), offsetObj.getNumber("baseOffsetY", 0f));
		tile.setBgOffset(offsetObj.getNumber("bgOffsetX", 0f), offsetObj.getNumber("bgOffsetY", 0f));
		tile.setGroundOffset(offsetObj.getNumber("groundOffsetX", 0f), offsetObj.getNumber("groundOffsetY", 0f));
		tile.setEffectOffset(offsetObj.getNumber("effectOffsetX", 0f), offsetObj.getNumber("effectOffsetY", 0f));
	}

	private static void parseTransform(Json.Object root, BattleTile tile) {
		Json.Object transObj = root.getObject("transform");
		if (transObj == null) {
			return;
		}
		tile.setScale(transObj.getNumber("scale", 1.0f));
		tile.bgScale = transObj.getNumber("bgScale", 1.0f);
		tile.groundScale = transObj.getNumber("groundScale", 1.0f);
		tile.effectScale = transObj.getNumber("effectScale", 1.0f);
		tile.setRotation(transObj.getNumber("rotation", 0));
		tile.setFlip(transObj.getBoolean("flipX", false), transObj.getBoolean("flipY", false));
	}

	private static void parseRender(Json.Object root, BattleTile tile) {
		Json.Object renderObj = root.getObject("render");
		if (renderObj == null) {
			return;
		}
		tile.setRenderLayer(renderObj.getInt("renderLayer", 0));
		tile.setVisible(renderObj.getBoolean("isVisible", true));
		tile.setBrightness(renderObj.getNumber("brightness", 1.0f));
		tile.setHighlighted(renderObj.getBoolean("isHighlighted", false));
		tile.setBlinking(renderObj.getBoolean("isBlinking", false));
		if (renderObj.isArray("tintColor")) {
			tile.setTintColor(parseColor(renderObj.getArray("tintColor"), LColor.white));
		}
		if (renderObj.isArray("highlightColor")) {
			tile.setHighlightColor(parseColor(renderObj.getArray("highlightColor"), new LColor(1, 1, 0, 0.6f)));
		}
	}

	private static LColor parseColor(Json.Array colorArray, LColor defaultColor) {
		if (colorArray == null || colorArray.length() < 4) {
			return defaultColor;
		}
		float r = colorArray.getNumber(0, 1f);
		float g = colorArray.getNumber(1, 1f);
		float b = colorArray.getNumber(2, 1f);
		float a = colorArray.getNumber(3, 1f);
		return new LColor(r, g, b, a);
	}

	private static void parseInteract(Json.Object root, BattleTile tile) {
		Json.Object interactObj = root.getObject("interact");
		if (interactObj == null) {
			return;
		}
		tile.setPassable(interactObj.getBoolean("passable", true));
		tile.setPathCost(interactObj.getNumber("pathCost", 1.0f));
		tile.setInteractable(interactObj.getBoolean("isInteractable", false));
		tile.setDurability(interactObj.getInt("durability", 100));
		tile.setDestroyed(interactObj.getBoolean("isDestroyed", false));
	}

	private static void parseCommon(Json.Object root, BattleTile tile) {
		tile.setAnimSpeed(root.getNumber("animSpeed", 1.0f));
		tile.setSkillDuration(root.getNumber("skillDuration", 0));
	}
}
