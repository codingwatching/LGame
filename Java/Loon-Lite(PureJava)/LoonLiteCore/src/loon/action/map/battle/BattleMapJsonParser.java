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
import loon.Json.TypedArray;
import loon.action.map.Field2D;
import loon.action.map.TileMapConfig;
import loon.action.map.battle.BattleMap.IsoTileLayer;
import loon.utils.ISOUtils.IsoConfig;
import loon.utils.StringUtils;

public class BattleMapJsonParser {

	private BattleMap battleMap;

	public BattleMapJsonParser(BattleMap map) {
		setBattleMap(map);
	}

	public void setBattleMap(BattleMap map) {
		battleMap = map;
	}

	public BattleMap getBattleMap() {
		return battleMap;
	}

	public void loadFromJson(Json.Object rootJson, BattleTile.EffectService effectService,
			BattleTile.SkillService skillService) {
		if (battleMap == null || rootJson == null) {
			return;
		}

		battleMap.clearAllIsoLayers();
		battleMap.clearStairTiles();
		battleMap.clearStrategicPoint();

		parseMapBaseConfig(rootJson);
		parseMapLayers(rootJson, effectService, skillService);
		parseStairTiles(rootJson);
		parseStrategicPoints(rootJson);
		battleMap.rebuildPathFinderUsingSelectedLayer();
		battleMap.markLayerSortDirty();
	}

	private void parseMapBaseConfig(Json.Object root) {
		Field2D map = battleMap.getField2D();
		int mapWidth = root.getInt("mapWidth", map.getWidth());
		int mapHeight = root.getInt("mapHeight", map.getHeight());
		map.setSize(mapWidth, mapHeight);
		Json.Object isoConfigJson = root.getObject("isoConfig");
		if (isoConfigJson != null) {
			IsoConfig cfg = battleMap.getIsoConfig();
			cfg.tileWidth = isoConfigJson.getInt("tileWidth", (int) cfg.tileWidth);
			cfg.tileHeight = isoConfigJson.getInt("tileHeight", (int) cfg.tileHeight);
			cfg.scaleX = isoConfigJson.getNumber("scaleX", cfg.scaleX);
			cfg.scaleY = isoConfigJson.getNumber("scaleY", cfg.scaleY);
			cfg.offsetX = isoConfigJson.getNumber("offsetX", cfg.offsetX);
			cfg.offsetY = isoConfigJson.getNumber("offsetY", cfg.offsetY);
		}
		Json.Object bgJson = root.getObject("background");
		if (bgJson != null) {
			String bgPath = bgJson.getString("path");
			if (!StringUtils.isEmpty(bgPath)) {
				battleMap.setBackground(bgPath);
			}
			battleMap.setBackgroundOffset(bgJson.getNumber("offsetX", 0), bgJson.getNumber("offsetY", 0));
			battleMap.setBackgroundSize(bgJson.getNumber("width", 0), bgJson.getNumber("height", 0));
		}
		battleMap.setDefaultIsoLayerStepOffset(root.getNumber("defaultLayerStepOffsetX", 8f),
				root.getNumber("defaultLayerStepOffsetY", 4f));
		battleMap.setPathFinderLayerIndex(root.getInt("pathFinderLayerIndex", 0));

		Json.Object idMapping = root.getObject("tileIdTypeMapping");
		if (idMapping != null) {
			TypedArray<String> names = idMapping.keys();
			for (int i = 0; i < names.length(); i++) {
				String key = names.get(i);
				int tileId = Integer.parseInt(key);
				int typeId = idMapping.getInt(key, 0);
				try {
					BattleTileType type = BattleTileType.getById(typeId);
					battleMap.registerTileTypeId(tileId, type);
				} catch (Throwable ignored) {
				}
			}
		}

		if (root.isArray("tileIdMap")) {
			Json.Array idMapArray = root.getArray("tileIdMap");
			if (idMapArray != null) {
				int w = idMapArray.length();
				int h = w > 0 ? idMapArray.getArray(0).length() : 0;
				int[][] idMap = new int[w][h];
				for (int x = 0; x < w; x++) {
					Json.Array row = idMapArray.getArray(x);
					for (int y = 0; y < h; y++) {
						idMap[x][y] = row.getInt(y, 0);
					}
				}
				map.set(TileMapConfig.reversalXandY(idMap));
			}
		}
	}

	private void parseMapLayers(Json.Object root, BattleTile.EffectService effectService,
			BattleTile.SkillService skillService) {
		Json.Array layersArray = root.getArray("layers");
		if (layersArray == null || layersArray.length() == 0) {
			return;
		}
		int layerCount = layersArray.length();
		for (int i = 0; i < layerCount; i++) {
			Json.Object layerJson = layersArray.getObject(i);
			if (layerJson == null) {
				continue;
			}
			String layerName = layerJson.getString("name", "layer_" + i);
			boolean layerVisible = layerJson.getBoolean("visible", true);
			float layerOffsetX = layerJson.getNumber("offsetX", 0);
			float layerOffsetY = layerJson.getNumber("offsetY", 0);
			BattleTile[][] layerTiles = parseLayerTiles(layerJson, effectService, skillService);
			if (layerTiles == null) {
				continue;
			}
			battleMap.addIsoLayer(layerTiles, layerName);
			IsoTileLayer layer = battleMap.getIsoLayer(battleMap.getIsoLayerCount() - 1);
			layer.visible = layerVisible;
			layer.offsetX = layerOffsetX;
			layer.offsetY = layerOffsetY;
		}
	}

	private BattleTile[][] parseLayerTiles(Json.Object layerJson, BattleTile.EffectService effectService,
			BattleTile.SkillService skillService) {
		Json.Array tilesArray = layerJson.getArray("tiles");
		if (tilesArray == null || tilesArray.length() == 0) {
			return null;
		}
		Field2D map = battleMap.getField2D();
		int layerW = map.getWidth();
		int layerH = map.getHeight();
		BattleTile[][] tiles = new BattleTile[layerW][layerH];
		for (int j = 0; j < tilesArray.length(); j++) {
			Json.Object tileJson = tilesArray.getObject(j);
			if (tileJson == null) {
				continue;
			}
			int gx = tileJson.getInt("gridX", 0);
			int gy = tileJson.getInt("gridY", 0);
			if (gx < 0 || gx >= layerW || gy < 0 || gy >= layerH) {
				continue;
			}
			BattleTile battleTile = parseSingleTile(tileJson, effectService, skillService);
			tiles[gx][gy] = battleTile;
		}
		for (int x = 0; x < layerW; x++) {
			for (int y = 0; y < layerH; y++) {
				if (tiles[x][y] == null) {
					tiles[x][y] = new BattleTile(x, y, map.getTileWidth(), map.getTileHeight(),
							battleMap.getIsoConfig(), BattleTileType.PLAIN, effectService, skillService);
				}
			}
		}
		return tiles;
	}

	private BattleTile parseSingleTile(Json.Object tileJson, BattleTile.EffectService effectService,
			BattleTile.SkillService skillService) {
		return BattleTileJsonParser.parseTile(tileJson, battleMap.getIsoConfig(), effectService, skillService);
	}

	private void parseStairTiles(Json.Object root) {
		Json.Array stairArray = root.getArray("stairTiles");
		if (stairArray == null) {
			return;
		}
		for (int i = 0; i < stairArray.length(); i++) {
			Json.Object pointJson = stairArray.getObject(i);
			if (pointJson == null) {
				continue;
			}
			int x = pointJson.getInt("x", 0);
			int y = pointJson.getInt("y", 0);
			battleMap.addStairTile(x, y);
		}
	}

	private void parseStrategicPoints(Json.Object root) {
		Json.Array strategicArray = root.getArray("strategicPoints");
		if (strategicArray == null) {
			return;
		}
		for (int i = 0; i < strategicArray.length(); i++) {
			Json.Object pointJson = strategicArray.getObject(i);
			if (pointJson == null) {
				continue;
			}
			int x = pointJson.getInt("x", 0);
			int y = pointJson.getInt("y", 0);
			battleMap.addStrategicPoint(x, y);
		}
	}

}
