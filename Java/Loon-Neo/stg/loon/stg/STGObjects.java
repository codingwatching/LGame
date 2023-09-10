package loon.stg;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import loon.LRelease;
import loon.stg.item.Item;
import loon.utils.MathUtils;
import loon.utils.ObjectMap;

class STGObjects extends ObjectMap<Integer, STGObject> implements LRelease {

	private STGScreen stg;

	private int count;

	private int overCount;

	protected int heroPlnNo;

	protected int firstPlnNo;

	protected int score = 0;

	STGObjects(STGScreen stg, int no) {
		this.firstPlnNo = this.count = no;
		this.heroPlnNo = this.firstPlnNo + 1000;
		this.overCount = this.heroPlnNo + 1;
		this.stg = stg;
	}

	boolean clearScore() {
		this.score = 0;
		return true;
	}

	int getScore() {
		return this.score;
	}

	boolean isAliveHero() {
		return this.get(this.heroPlnNo) != null;
	}

	STGHero getHero() {
		return ((STGHero) this.get(heroPlnNo));
	}

	int getHP() {
		return ((STGHero) this.get(heroPlnNo)).getHP();
	}

	int getMP() {
		return ((STGHero) this.get(heroPlnNo)).getMP();
	}

	boolean clearObjects() {
		this.clearScore();
		Iterator<STGObject> e = this.values();
		for (; e.hasNext();) {
			STGObject o = (e.next());
			this.delObj(o.plnNo);
		}
		this.count = this.firstPlnNo;
		return true;
	}

	int addBombHero(String packageName) {
		STGHero hero = (STGHero) this.get(heroPlnNo);
		if (hero != null) {
			final float x = hero.getX();
			final float y = hero.getY();
			int id = this.addPlane(packageName, x, y, this.heroPlnNo, this.overCount);
			STGObject obj = get(id);
			if (obj != null) {
				obj.setX(x + (hero.getHitW() - obj.getHitW()) / 2);
				obj.setY(y + (hero.getHitH() - obj.getHitH()) / 2);
			}
			++this.overCount;
			return this.overCount - 1;
		} else {
			return 0;
		}
	}

	int addBombHero(String packageName, float x, float y) {
		this.addPlane(packageName, x, y, this.heroPlnNo, this.overCount);
		++this.overCount;
		return this.overCount - 1;
	}

	int addHero(String packageName, float x, float y, int no) {
		this.heroPlnNo = no;
		this.overCount = this.heroPlnNo + 1;
		this.addPlane(packageName, x, y, this.heroPlnNo, this.heroPlnNo);
		return no;
	}

	int addClass(String packageName, float x, float y, int tpno) {
		int count = this.count;
		this.addPlane(packageName, x, y, tpno, this.count);
		++this.count;
		if (this.count >= this.heroPlnNo) {
			this.count = this.firstPlnNo;
		}
		return count;
	}

	STGObject newPlane(String packageName, float x, float y, int tpno) {
		STGObject o = this.newPlane(packageName, x, y, tpno, this.count);
		++this.count;
		if (this.count >= this.heroPlnNo) {
			this.count = this.firstPlnNo;
		}
		return o;
	}

	int addPlane(STGObject o) {
		int count = this.count;
		this.addPlane(this.count, o);
		++this.count;
		if (this.count >= this.heroPlnNo) {
			this.count = this.firstPlnNo;
		}
		return count;
	}

	static Class<?> newClass(String packageName) throws ClassNotFoundException {
		return Class.forName(packageName);
	}

	@SuppressWarnings("rawtypes")
	int addPlane(String packageName, float x, float y, int tpno, int no) {
		try {
			Class[] clazz = new Class[] { STGScreen.class, Integer.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE };
			Constructor constructor = newClass(packageName).getConstructor(clazz);
			Object[] args = new Object[] { this.stg, no, x, y, tpno };
			Object newObject = constructor.newInstance(args);
			this.put(no, (STGObject) newObject);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return no;
	}

	@SuppressWarnings("rawtypes")
	STGObject newPlane(String packageName, float x, float y, int tpno, int no) {
		STGObject newObject = null;
		try {
			Class[] clazz = new Class[] { STGScreen.class, Integer.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE };
			Constructor constructor = newClass(packageName).getConstructor(clazz);
			Object[] args = new Object[] { this.stg, no, x, y, tpno };
			newObject = (STGObject) constructor.newInstance(args);
			this.put(no, newObject);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return newObject;
	}

	int addPlane(int no, STGObject o) {
		try {
			this.put(no, o);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return no;
	}

	void delObj(int index) {
		this.stg.deletePlane(index);
		this.remove(index);
	}

	void reset() {
		this.count = 0;
	}

	void running() {
		Iterator<STGObject> e = this.values();
		for (; e.hasNext();) {
			e.next().update();
		}
	}

	void hitCheckHeroShot() {
		STGObject o = null;
		Iterator<STGObject> e = this.values();
		for (; e.hasNext();) {
			STGObject shot = e.next();
			if (shot.attribute == STGScreen.HERO_SHOT) {
				o = shot;
				Iterator<STGObject> enumeration = this.values();
				while (enumeration.hasNext()) {
					shot = enumeration.next();
					if (shot.attribute == STGScreen.ENEMY
							&& MathUtils.abs(this.stg.getPlanePosX(o.plnNo) + o.hitX + o.getHitW() / 2
									- this.stg.getPlanePosX(shot.plnNo) - shot.hitX
									- shot.getHitW() / 2) < (o.getHitW() + shot.getHitW()) / 2
							&& MathUtils.abs(this.stg.getPlanePosY(o.plnNo) + o.hitY + o.getHitH() / 2
									- this.stg.getPlanePosY(shot.plnNo) - shot.hitY
									- shot.getHitH() / 2) < (o.getHitH() + shot.getHitH()) / 2) {
						--o.hitPoint;
						if (o.hitPoint == 0) {
							this.delObj(o.plnNo);
						}
						--shot.hitPoint;
						shot.hitFlag = true;
						if (shot.hitPoint == 0) {
							this.score += shot.scorePoint;
							shot.attribute = STGScreen.ENEMY_SHOT;
						}
						break;
					}
				}
			}
		}

	}

	void hitCheckHero() {
		STGObject o = null;
		Iterator<STGObject> e = this.values();
		STGObject shot;
		for (; e.hasNext();) {
			shot = e.next();
			if (shot.attribute == STGScreen.HERO) {
				o = shot;
				break;
			}
		}
		if (o != null) {
			e = this.values();
			for (; e.hasNext();) {
				shot = e.next();
				if ((shot.attribute == STGScreen.ENEMY || shot.attribute == STGScreen.ITEM
						|| shot.attribute == STGScreen.ENEMY_SHOT || shot.attribute == STGScreen.ALL_HIT)
						&& MathUtils.abs(this.stg.getPlanePosX(o.plnNo) + o.hitX + o.getHitW() / 2
								- this.stg.getPlanePosX(shot.plnNo) - shot.hitX
								- shot.getHitW() / 2) < (o.getHitW() + shot.getHitW()) / 2
						&& MathUtils.abs(this.stg.getPlanePosY(o.plnNo) + o.hitY + o.getHitH() / 2
								- this.stg.getPlanePosY(shot.plnNo) - shot.hitY
								- shot.getHitH() / 2) < (o.getHitH() + shot.getHitH()) / 2) {
					if (shot.attribute != STGScreen.ITEM) {
						--shot.hitPoint;
						shot.hitFlag = true;
						if (shot.hitPoint == 0) {
							this.score += shot.scorePoint;
							shot.attribute = STGScreen.NO_HIT;
							if (shot.attribute == STGScreen.ENEMY_SHOT) {
								this.delObj(shot.plnNo);
							}
						}
						o.attribute = STGScreen.NO_HIT;
					} else {
						Item i = (Item) shot;
						i.giveHeroEvent((STGHero) o);
						i.attribute = STGScreen.NO_HIT;
					}
					break;
				}
			}

		}
	}

	@Override
	public void close() {
		clear();
	}

}
