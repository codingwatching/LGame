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
package loon.action.map.items;

import loon.LSystem;
import loon.utils.MathUtils;

/**
 * 一个基本的游戏角色数值模板,可以套用其扩展自己的游戏属性以及属性变更算法(和RoleInfo的关系在于这个更接近角色参数)
 *
 */
public abstract class RoleValue {

	private final int _id;
	private boolean _locked;
	private String _roleName;

	protected int actionPriority;
	protected int maxHealth;
	protected int maxMana;
	protected int maxExp;
	protected int health;
	protected int mana;
	protected int exp;
	protected int attack;
	protected int defence;
	protected int strength;
	protected int intelligence;
	protected int agility;
	protected int fitness;
	protected int dexterity;
	protected int level;
	protected int team = Team.Unknown;
	protected int movePoints;
	protected int turnPoints;
	protected int actionPoints;

	protected boolean isAttack;
	protected boolean isDefense;
	protected boolean isSkill;
	protected boolean isMoved;
	protected boolean isDead;
	protected boolean isInvincible;

	protected RoleEquip info;

	public RoleValue(int id, RoleEquip info, int maxHealth, int maxMana, int attack, int defence, int strength,
			int intelligence, int fitness, int dexterity, int agility, int lv) {
		this(id, LSystem.UNKNOWN, info, maxHealth, maxMana, attack, defence, strength, intelligence, fitness, dexterity,
				agility, lv);
	}

	public RoleValue(int id, String name, RoleEquip info, int maxHealth, int maxMana, int attack, int defence,
			int strength, int intelligence, int fitness, int dexterity, int agility, int lv) {
		this._id = id;
		this._roleName = name;
		this.info = info;
		this.maxHealth = maxHealth;
		this.maxMana = maxMana;
		this.health = maxHealth;
		this.mana = maxMana;
		this.agility = agility;
		this.attack = attack;
		this.defence = defence;
		this.strength = strength;
		this.intelligence = intelligence;
		this.fitness = fitness;
		this.dexterity = dexterity;
		this.level = lv;
	}

	public RoleValue setActionPriority(int a) {
		this.actionPriority = a;
		return this;
	}

	public int getActionPriority() {
		return actionPriority;
	}

	public float updateTurnPoints() {
		int randomBuffer = MathUtils.nextInt(100);
		this.turnPoints += this.fitness + randomBuffer / 100;
		if (this.turnPoints > 100) {
			this.turnPoints = 100;
		}
		return this.turnPoints;
	}

	public int calculateDamage(int enemyDefence, int damageBufferMax) {
		float damage = this.attack + 0.5f * this.strength - 0.5f * enemyDefence;
		if ((damage = MathUtils.ceil(this.variance(damage, damageBufferMax, true))) < 1f) {
			damage = 1f;
		}
		return MathUtils.ifloor(damage);
	}

	public int calculateDamage(int enemyDefence) {
		return calculateDamage(enemyDefence, 20);
	}

	public int hit(int enemyDex, int enemyAgi, int enemyFitness) {
		return hit(enemyDex, enemyAgi, enemyFitness, 95, 15, 55f);
	}

	public int hit(int enemyDex, int enemyAgi, int enemyFitness, int maxChance, int minChance, float hitChance) {
		hitChance += (this.dexterity - enemyDex) + 0.5 * (this.fitness - enemyFitness) - enemyAgi;
		if ((hitChance = this.variance(hitChance, 10, true)) > maxChance) {
			hitChance = maxChance;
		} else if (hitChance < minChance) {
			hitChance = minChance;
		}
		return MathUtils.ceil(hitChance);
	}

	public RoleValue damage(float damageTaken) {
		this.health = MathUtils.ifloor(this.health - damageTaken);
		return this;
	}

	public boolean flee(int enemyLevel, int enemyFitness) {
		return flee(enemyLevel, enemyFitness, 95, 5, 55);
	}

	public boolean flee(int enemyLevel, int enemyFitness, int maxChance, int minChance, int hitChance) {
		int fleeChance = hitChance - 3 * (enemyFitness - this.fitness);
		if (fleeChance > maxChance) {
			fleeChance = maxChance;
		} else if (fleeChance < minChance) {
			fleeChance = minChance;
		}
		int fleeRoll = MathUtils.nextInt(100);
		if (fleeRoll <= fleeChance) {
			return true;
		}
		return false;
	}

	public int getID() {
		return _id;
	}

	public RoleValue changeHeal(int healChange) {
		if (healChange != 0) {
			if (healChange + health > maxHealth) {
				health = maxHealth;
			} else if (healChange + health < 1) {
				health = 0;
			} else {
				health += healChange;
			}
		}
		return this;
	}

	public RoleValue heal(int healCost, int healAmount) {
		if (this.getMana() >= healCost) {
			healAmount = MathUtils.ifloor(this.variance(healAmount, 20, true));
			this.health += healAmount;
			if (this.health > this.maxHealth) {
				this.health = this.maxHealth;
			}
			this.mana -= healCost;
		}
		return this;

	}

	public RoleValue heal() {
		return heal(5, 20);
	}

	public int regenerateMana() {
		return regenerateMana(2, 50);
	}

	public int regenerateMana(int minRegen, int maxRegen) {
		int regen = intelligence / 4;
		if (regen < minRegen) {
			regen = minRegen;
		}
		if (regen > maxRegen) {
			regen = maxRegen;
		}

		return regen;
	}

	private float variance(float base, int variance, boolean negativeAllowed) {
		if (variance < 1) {
			variance = 1;
		} else if (variance > 100) {
			variance = 100;
		}
		int buffer = MathUtils.nextInt(++variance);
		if (MathUtils.nextBoolean() && negativeAllowed) {
			buffer = -buffer;
		}
		float percent = (float) (100 - buffer) / 100f;
		float variedValue = base * percent;
		return variedValue;
	}

	public RoleValue updateAttack(float attackModifier) {
		this.info.updateAttack(attackModifier);
		return this;
	}

	public RoleValue updateDefence(float defenceModifier) {
		this.info.updateDefence(defenceModifier);
		return this;
	}

	public RoleValue updateStrength(float strengthModifier) {
		this.info.updateStrength(strengthModifier);
		return this;
	}

	public RoleValue updateIntelligence(float intelligenceModifier) {
		this.info.updateIntelligence(intelligenceModifier);
		return this;
	}

	public RoleValue updateFitness(float fitnessModifier) {
		this.info.updateFitness(fitnessModifier);
		return this;
	}

	public RoleValue updateDexterity(float dexterityModifier) {
		this.info.updateDexterity(dexterityModifier);
		return this;
	}

	public RoleValue updateMaxHealth(float maxHealthModifier) {
		this.info.updateMaxHealth(maxHealthModifier);
		return this;
	}

	public RoleValue updateSkillPoints(float skillModifier) {
		this.info.updateSkillPoints(skillModifier);
		return this;
	}

	public RoleValue updateManaPoints(float manaModifier) {
		this.info.updateManaPoints(manaModifier);
		return this;
	}

	public RoleValue updateAgility(float agilityModifier) {
		this.info.updateAgility(agilityModifier);
		return this;
	}

	public boolean fellow(RoleValue c) {
		if (c == null) {
			return false;
		}
		return this.team == c.team;
	}

	public boolean fellow(Team team) {
		if (team == null) {
			return false;
		}
		return this.team == team.getTeam();
	}

	public int getAttack() {
		return this.attack;
	}

	public RoleValue setAttack(int attack) {
		this.attack = attack;
		return this;
	}

	public int getMaxMana() {
		return this.maxMana;
	}

	public RoleValue setMaxMana(int maxMana) {
		this.maxMana = maxMana;
		return this;
	}

	public int getDefence() {
		return this.defence;
	}

	public RoleValue setDefence(int defence) {
		this.defence = defence;
		return this;
	}

	public int getStrength() {
		return this.strength;
	}

	public RoleValue setStrength(int strength) {
		this.strength = strength;
		return this;
	}

	public int getIntelligence() {
		return this.intelligence;
	}

	public RoleValue setIntelligence(int intelligence) {
		this.intelligence = intelligence;
		return this;
	}

	public int getFitness() {
		return this.fitness;
	}

	public RoleValue setFitness(int fitness) {
		this.fitness = fitness;
		return this;
	}

	public int getDexterity() {
		return this.dexterity;
	}

	public RoleValue setDexterity(int dexterity) {
		this.dexterity = dexterity;
		return this;
	}

	public RoleValue setHealth(int health) {
		this.health = health;
		if (this.health <= 0) {
			this.isDead = true;
		}
		return this;
	}

	public RoleValue setMana(int mana) {
		this.mana = mana;
		return this;
	}

	public float getTurnPoints() {
		return this.turnPoints;
	}

	public RoleValue setTurnPoints(int turnPoints) {
		this.turnPoints = turnPoints;
		return this;
	}

	public int getLevel() {
		return this.level;
	}

	public int getHealth() {
		return this.health;
	}

	public int getMana() {
		return this.mana;
	}

	public int getBaseMaxHealth() {
		return this.info.getBaseMaxHealth();
	}

	public RoleValue setBaseMaxHealth(int baseMaxHealth) {
		this.info.setBaseMaxHealth(baseMaxHealth);
		return this;
	}

	public int getEquipMaxHealth() {
		return this.info.getEquipMaxHealth();
	}

	public RoleValue setEquipMaxHealth(int equipMaxHealth) {
		this.info.setEquipMaxHealth(equipMaxHealth);
		return this;
	}

	public boolean isFullHealth() {
		return health == maxHealth;
	}

	public int getMaxHealth() {
		return this.maxHealth;
	}

	public RoleValue setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
		return this;
	}

	public int getAgility() {
		return agility;
	}

	public RoleValue setAgility(int agility) {
		this.agility = agility;
		return this;
	}

	public int getTeam() {
		return team;
	}

	public RoleValue setTeam(int team) {
		this.team = team;
		return this;
	}

	public int getMovePoints() {
		return movePoints;
	}

	public RoleValue setMovePoints(int movePoints) {
		this.movePoints = movePoints;
		return this;
	}

	public boolean isAllDoneAction() {
		return isAttack && isDefense && isMoved && isSkill;
	}

	public boolean isAllUnDoneAction() {
		return !isAttack && !isDefense && !isMoved && !isSkill;
	}

	public RoleValue undoneAction() {
		setAttack(false);
		setDefense(false);
		setSkill(false);
		setMoved(false);
		return this;
	}

	public RoleValue doneAction() {
		setAttack(true);
		setDefense(true);
		setSkill(true);
		setMoved(true);
		return this;
	}

	public boolean isAttack() {
		return isAttack;
	}

	public RoleValue setAttack(boolean isAttack) {
		this.isAttack = isAttack;
		return this;
	}

	public boolean isDefense() {
		return isDefense;
	}

	public RoleValue setDefense(boolean defense) {
		this.isDefense = defense;
		return this;
	}

	public boolean isSkill() {
		return isSkill;
	}

	public RoleValue setSkill(boolean skill) {
		this.isSkill = skill;
		return this;
	}

	public boolean isMoved() {
		return isMoved;
	}

	public RoleValue setMoved(boolean moved) {
		this.isMoved = moved;
		return this;
	}

	public boolean isDead() {
		return this.isDead;
	}

	public boolean isAlive() {
		return !isDead;
	}

	public RoleValue setDead(boolean dead) {
		this.isDead = dead;
		return this;
	}

	public RoleValue die() {
		this.isDead = true;
		return this;
	}

	public boolean isInvincible() {
		return isInvincible;
	}

	public RoleValue setInvincible(boolean i) {
		this.isInvincible = i;
		return this;
	}

	public RoleEquip getInfo() {
		return info;
	}

	public RoleValue setInfo(RoleEquip info) {
		this.info = info;
		return this;
	}

	public RoleValue setLevel(int level) {
		this.level = level;
		return this;
	}

	public int getActionPoints() {
		return actionPoints;
	}

	public RoleValue setActionPoints(int actionPoints) {
		this.actionPoints = actionPoints;
		return this;
	}

	public String getRoleName() {
		return _roleName;
	}

	public RoleValue setRoleName(String n) {
		this._roleName = n;
		return this;
	}

	public RoleValue setLocked(boolean l) {
		this._locked = l;
		return this;
	}

	public boolean isLocked() {
		if (isDead) {
			return _locked;
		} else {
			return false;
		}
	}

	public int getMaxExp() {
		return maxExp;
	}

	public RoleValue setMaxExp(int maxExp) {
		this.maxExp = maxExp;
		return this;
	}

	public int getExp() {
		return exp;
	}

	public RoleValue setExp(int exp) {
		this.exp = exp;
		return this;
	}

	public float getUpLevelMaxExp() {
		return getUpLevelMaxExp(0f);
	}

	public float getUpLevelMaxExp(float offset) {
		return (2f * level * (MathUtils.pow(1.3f, level / 3f)) + offset) + 4f;
	}

	public float getEnemyExpEarned(int enemyLevel) {
		return getEnemyExpEarned(enemyLevel, 0f);
	}

	public float getEnemyExpEarned(int enemyLevel, float offset) {
		return (MathUtils.pow(enemyLevel, 0.95f)) + offset;
	}

	public RoleValue reset() {
		this.isAttack = false;
		this.isDefense = false;
		this.isSkill = false;
		this.isMoved = false;
		this.isDead = false;
		this.isInvincible = false;
		this._locked = false;
		return this;
	}

	public RoleValue setStatus(int v) {
		this.maxHealth = v;
		this.maxMana = v;
		this.health = v;
		this.mana = v;
		this.attack = v;
		this.defence = v;
		this.strength = v;
		this.intelligence = v;
		this.agility = v;
		this.fitness = v;
		this.dexterity = v;
		return this;
	}

	public RoleValue setStatus(RoleEquip e) {
		if (e == null) {
			return this;
		}
		this.maxHealth = (e.getBaseMaxHealth() + e.getEquipMaxHealth());
		this.maxMana = (e.getBaseManaPoint() + e.getEquipManaPoint());
		this.health = maxHealth;
		this.mana = maxMana;
		this.attack = (e.getBaseAttack() + e.getEquipAttack());
		this.defence = (e.getBaseDefence() + e.getEquipDefence());
		this.strength = (e.getBaseStrength() + e.getEquipStrength());
		this.intelligence = (e.getBaseIntelligence() + e.getEquipIntelligence());
		this.agility = (e.getBaseAgility() + e.getEquipAgility());
		this.fitness = (e.getBaseFitness() + e.getEquipFitness());
		this.dexterity = (e.getBaseDexterity() + e.getEquipDexterity());
		return this;
	}

	public RoleValue addEquip(RoleEquip e) {
		if (e == null) {
			return this;
		}
		this.maxHealth = (e.getBaseMaxHealth() + e.getEquipMaxHealth());
		this.maxMana = (e.getBaseManaPoint() + e.getEquipManaPoint());
		this.health = maxHealth;
		this.mana = maxMana;
		this.attack += e.getEquipAttack();
		this.defence += e.getEquipDefence();
		this.strength += e.getEquipStrength();
		this.intelligence += e.getEquipIntelligence();
		this.agility += e.getEquipAgility();
		this.fitness += e.getEquipFitness();
		this.dexterity += e.getEquipDexterity();
		return this;
	}

	public RoleValue mulStatus(int v) {
		this.maxHealth *= v;
		this.maxMana *= v;
		this.health *= v;
		this.mana *= v;
		this.attack *= v;
		this.defence *= v;
		this.strength *= v;
		this.intelligence *= v;
		this.agility *= v;
		this.fitness *= v;
		this.dexterity *= v;
		return this;
	}

	public RoleValue addStatus(int v) {
		this.maxHealth += v;
		this.maxMana += v;
		this.health += v;
		this.mana += v;
		this.attack += v;
		this.defence += v;
		this.strength += v;
		this.intelligence += v;
		this.agility += v;
		this.fitness += v;
		this.dexterity += v;
		return this;
	}

	public RoleValue subStatus(int v) {
		this.maxHealth -= v;
		this.maxMana -= v;
		this.health -= v;
		this.mana -= v;
		this.attack -= v;
		this.defence -= v;
		this.strength -= v;
		this.intelligence -= v;
		this.agility -= v;
		this.fitness -= v;
		this.dexterity -= v;
		return this;
	}

	public RoleValue divStatus(int v) {
		this.maxHealth /= v;
		this.maxMana /= v;
		this.health /= v;
		this.mana /= v;
		this.attack /= v;
		this.defence /= v;
		this.strength /= v;
		this.intelligence /= v;
		this.agility /= v;
		this.fitness /= v;
		this.dexterity /= v;
		return this;
	}

	public RoleValue clear() {
		this.actionPriority = 0;
		this.maxHealth = 0;
		this.maxMana = 0;
		this.maxExp = 0;
		this.health = 0;
		this.mana = 0;
		this.exp = 0;
		this.attack = 0;
		this.defence = 0;
		this.strength = 0;
		this.intelligence = 0;
		this.agility = 0;
		this.fitness = 0;
		this.dexterity = 0;
		this.level = 0;
		this.team = Team.Unknown;
		this.movePoints = 0;
		this.turnPoints = 0;
		this.actionPoints = 0;
		this.isAttack = false;
		this.isDefense = false;
		this.isSkill = false;
		this.isMoved = false;
		this.isDead = false;
		this.isInvincible = false;
		return this;
	}

}
