﻿package org.test.towerdefense;

import loon.geom.Vector2f;
import loon.utils.reply.ObjRef;

public class AnimatedSpriteTower extends AnimatedSprite {

	public AnimatedSpriteTower(MainGame game, String textureFile, Vector2f position, int spriteCount) {
		super(game, textureFile, position, 6, spriteCount, 80, 80, 1f);
		super.setAnimationSpeedRatio(3);
	}

	public static java.util.ArrayList<AnimatedSpriteTower> GetAllAnimatedSpriteTowers(MainGame game) {

		int num = 10;

		java.util.ArrayList<AnimatedSpriteTower> list = new java.util.ArrayList<AnimatedSpriteTower>();

		ObjRef<Integer> num2 = new ObjRef<Integer>(0);
		list.add(new AnimatedSpriteTower(game, GetTextureFile(TowerType.Axe, "png/", num2),
				new Vector2f((float) num, 18f), num2.get()));
		num2.set(0);

		list.add(new AnimatedSpriteTower(game, GetTextureFile(TowerType.Spear, "png/", num2),
				new Vector2f((float) num, 118f), num2.get()));
		num2.set(0);

		list.add(new AnimatedSpriteTower(game, GetTextureFile(TowerType.AirDefence, "png/", num2),
				new Vector2f((float) num, 218f), num2.get()));
		num2.set(0);

		list.add(new AnimatedSpriteTower(game, GetTextureFile(TowerType.Lur, "png/", num2),
				new Vector2f((float) num, 318f), num2.get()));
		num2.set(0);

		return list;
	}

	public static AnimatedSprite GetAnimatedSpriteTowerForTowerToolbar(MainGame game, Vector2f towerToolbarDrawPosition,
			TowerType towerType, float scale) {

		ObjRef<Integer> num2 = new ObjRef<Integer>(0);
		AnimatedSprite tempVar = new AnimatedSprite(game, GetTextureFile(towerType, "png/", num2),
				towerToolbarDrawPosition.add(-2f, -34f), 6, num2.get(), 80, 80, scale);

		return tempVar;
	}

	private static String GetTextureFile(TowerType towerType, String subDir, ObjRef<Integer> spriteCount) {
		String str = "";
		spriteCount.set(0x24);
		switch (towerType) {
		case Axe:
			str = "towerinfo1";
			break;

		case Spear:
			str = "towerinfo2";
			break;

		case AirDefence:
			str = "towerinfo3";
			break;

		case Lur:
			str = "towerinfo4";
			break;
		}
		return ("assets/" + subDir + str + ".png");
	}
}