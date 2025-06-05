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
package org.test;

import loon.Stage;
import loon.action.ActionBind;
import loon.action.ActionListener;
import loon.action.FlashTo;
import loon.action.RotateTo;
import loon.action.map.Config;
import loon.action.map.TileMap;
import loon.action.sprite.Animation;
import loon.action.sprite.JumpObject;
import loon.action.sprite.ActionObject;
import loon.canvas.LColor;
import loon.component.LPad;
import loon.events.ActionKey;
import loon.events.SysKey;
import loon.geom.BooleanValue;
import loon.geom.Vector2f;

public class GameMapTest extends Stage {

	// 敌人用类
	class Enemy extends ActionObject {

		private static final float SPEED = 1;

		public Enemy(float x, float y, Animation animation, TileMap tiles) {
			super(x, y, 32, 32, animation, tiles);
			velocityX = -SPEED;
			velocityY = 0;
		}

		@Override
		public void onManagedUpdate(long e) {

			// 获得当前精灵与地图碰撞后坐标
			Vector2f pos = collisionTileMap(0f, 0.6f);
			// 注入新坐标
			setLocation(pos.x, pos.y);
		}

	}

	// 二次跳跃用类（物品）
	class JumperTwo extends ActionObject {

		public JumperTwo(float x, float y, Animation animation, TileMap tiles) {
			super(x, y, 32, 32, animation, tiles);
		}

		public void use(JumpObject hero) {
			hero.setJumperTwo(true);
		}
	}

	// 加速用类（物品）
	class Accelerator extends ActionObject {

		public Accelerator(float x, float y, Animation animation, TileMap tiles) {
			super(x, y, 32, 32, animation, tiles);
		}

		public void use(JumpObject hero) {
			hero.setSpeed(hero.getSpeed() * 2);
		}
	}

	// 金币用类（物品）
	class Coin extends ActionObject {

		public Coin(float x, float y, Animation animation, TileMap tiles) {
			super(x, y, 32, 32, animation, tiles);
		}
	}

	// 锁定主角操作
	private BooleanValue heroLocked = refBool();

	private JumpObject hero;

	// PS：如果具体游戏开发时用到多动画切换，则建议使用AnimationStorage这个Animation的子类
	// 金币用动画图
	private Animation coinAnimation;

	// 敌人用动画图(过滤掉黑色)
	private Animation enemyAnimation;

	// 加速道具动画图
	private Animation accelAnimation;

	// 二级跳动画图
	private Animation jumpertwoAnimation;

	@Override
	public void create() {
		
		// 最先绘制用户画面
		// setFristOrder(DRAW_USER_PAINT());
		// 其次绘制精灵
		// setSecondOrder(DRAW_SPRITE_PAINT());
		// 最后绘制桌面
		// setLastOrder(DRAW_DESKTOP_PAINT());
		// 先绘制用户画面,然后绘制精灵，最后绘制桌面组件
		lastDesktopDraw();
		add(MultiScreenTest.getBackButton(this, 0));
		// 使用GLEx而非SpriteBacth渲染画面
		// setUseGLEx(true);

		// 设置游戏滚动背景,向左方移动
		setScrollBackground(Config.LEFT, "assets/tile_clouds.png");

		// 不锁定角色操作
		this.heroLocked.set(false);
		// 以指定图片创建动画
		this.coinAnimation = Animation.getDefaultAnimation("assets/coin.png", 32, 32, 200);
		this.enemyAnimation = Animation.getDefaultAnimation("assets/enemy.gif", 32, 32, 200, LColor.black);
		this.accelAnimation = Animation.getDefaultAnimation("assets/accelerator.gif", 32, 32, 200);
		this.jumpertwoAnimation = Animation.getDefaultAnimation("assets/jumper_two.gif", 32, 32, 200);

		// 注销Screen时释放下列资源
		putReleases(coinAnimation, enemyAnimation, accelAnimation, jumpertwoAnimation, hero);

		// 加载一张由字符串形成的地图（如果不使用此方式加载，则默认使用标准的数组地图）
		final TileMap indexMap = TileMap.loadCharsMap("assets/map.chr", 32, 32);
		// 如果有配置好的LTexturePack文件，可于此注入
		// indexMap.setImagePack(file);
		// 设定无法穿越的区域(如果不设置此项，所有索引不等于"-1"的区域都可以穿越)
		indexMap.setLimit(new int[] { 'B', 'C', 'i', 'c' });
		indexMap.putTile('B', "assets/block.png");
		int imgId = indexMap.putTile('C', "assets/coin_block.gif");
		// 因为两块瓦片对应同一地图字符，所以此处使用了已载入的图片索引
		indexMap.putTile('i', imgId);
		indexMap.putTile('c', "assets/coin_block2.gif");

		// 加载此地图到窗体中
		putTileMap(indexMap);

		// 遍历二维数组地图，并以此为基础添加角色到窗体之上
		indexMap.switchMap((mapId, x, y) -> {
			switch (mapId) {
			case 'o':
				Coin coin = new Coin(x, y, new Animation(coinAnimation), indexMap);
				addTileObject(coin);
				break;
			case 'k':
				Enemy enemy = new Enemy(x, y, new Animation(enemyAnimation), indexMap);
				addTileObject(enemy);
				break;
			case 'a':
				Accelerator accelerator = new Accelerator(x, y, new Animation(accelAnimation), indexMap);
				addTileObject(accelerator);
				break;
			case 'j':
				JumperTwo jump = new JumperTwo(x, y, new Animation(jumpertwoAnimation), indexMap);
				addTileObject(jump);
				break;
			}
		});

		// 获得主角动作图
		Animation animation = Animation.getDefaultAnimation("assets/hero.png", 20, 20, 150, LColor.black);

		// 在像素坐标位置(192,32)放置角色，大小为32x32，动画为针对hero.png的分解图
		hero = addJumpObject(192, 32, 32, 32, animation);
		// 像素计算上角色高偏移2个像素
		hero.setFixedHeightOffset(2);
		// 让地图跟随指定对象产生移动（无论插入有多少张数组地图，此跟随默认对所有地图生效）
		// 另外请注意，此处能产生跟随的对像是任意LObject，并不局限于游戏角色。
		follow(hero);

		// 监听跳跃事件
		hero.listener = (x, y) -> {

			if (indexMap.getTileID(x, y) == 'C') {
				indexMap.setTileID(x, y, 'c');
				Enemy enemy = new Enemy(indexMap.tilesToPixelsX(x), indexMap.tilesToPixelsY(y - 1),
						new Animation(enemyAnimation), indexMap);
				add(enemy);
				// 标注地图已脏，强制缓存刷新
				indexMap.setDirty(true);
			} else if (indexMap.getTileID(x + 1, y) == 'C') {
				indexMap.setTileID(x + 1, y, 'c');
				indexMap.setDirty(true);
			}

		};

		// 对应向左行走的键盘事件
		keyPress("left", () -> {
			if (!heroLocked.get()) {
				hero.setMirror(true);
				hero.accelerateLeft();
			}
		});

		// 对应向右行走的键盘事件
		keyPress("right", () -> {
			if (!heroLocked.get()) {
				hero.setMirror(false);
				hero.accelerateRight();
			}
		});

		// 对应跳跃的键盘事件（DETECT_INITIAL_PRESS_ONLY表示在放开之前，此按键不会再次触发）
		ActionKey jumpKey = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY) {
			@Override
			public void act(long e) {
				if (!heroLocked.get()) {
					hero.jump();
				}
			}
		};

		addActionKey(SysKey.UP, jumpKey);

		LPad pad = new LPad(10, 180);
		LPad.ClickListener click = new LPad.ClickListener() {

			public void up() {
				pressActionKey(SysKey.UP);
			}

			public void right() {
				pressActionKey(SysKey.RIGHT);
			}

			public void left() {
				pressActionKey(SysKey.LEFT);
			}

			public void down() {
				pressActionKey(SysKey.DOWN);
			}

			public void other() {
				releaseActionKeys();
			}

		};
		pad.setListener(click);
		add(pad);

		// 地图中角色事件监听(每帧都会触发一次此监听)
		setUpdateListener((sprite, elapsedTime) -> {

			// 如果主角与地图上其它对象发生碰撞（以下分别验证）
			if (hero.isCollision(sprite)) {
				// 与敌人
				if (sprite instanceof Enemy) {
					Enemy e = (Enemy) sprite;
					if (hero.y() < e.y()) {
						hero.setForceJump(true);
						hero.jump();
						removeTileObject(e);
					} else {
						damage();
					}
					// 与金币
				} else if (sprite instanceof Coin) {
					Coin coin = (Coin) sprite;
					removeTileObject(coin);
					// 与加速道具
				} else if (sprite instanceof Accelerator) {
					removeTileObject(sprite);
					Accelerator accelerator = (Accelerator) sprite;
					accelerator.use(hero);
					// 与二次弹跳道具
				} else if (sprite instanceof JumperTwo) {
					removeTileObject(sprite);
					JumperTwo jumperTwo = (JumperTwo) sprite;
					jumperTwo.use(hero);
				}
			}

		});
		selfAction().shakeTo(3f, 3f).start();

		loop(() -> {
			if (hero != null) {
				hero.stop();
			}
		});
	}

	private RotateTo rotate;

	public void damage() {
		// 主角与敌人碰撞时(而非踩到了敌人)，触发一个旋转动作(其实效果可以做的更有趣一些，
		// 比如先反弹到某一方向(FireTo)，然后再弹回等等，此处仅仅举个例子)
		// 旋转360度，时间2秒
		if (rotate == null) {
			rotate = new RotateTo(360f, 2f);
			rotate.setActionListener(new ActionListener() {

				@Override
				public void stop(ActionBind o) {
					hero.setColor(LColor.white);
					hero.setRotation(0);
					// 解除锁定
					heroLocked.set(false);
					rotate = null;
				}

				@Override
				public void start(ActionBind o) {
					hero.setColor(LColor.red);
					hero.jump();
					// 锁定操作
					heroLocked.set(true);
				}

				@Override
				public void process(ActionBind o) {
					heroLocked.set(true);
				}
			});
			// 让角色闪烁并且翻转
			hero.selfAction().parallelTo(new FlashTo(), rotate).start();
		}
	}

}
