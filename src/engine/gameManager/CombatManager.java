// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum.*;
import engine.ai.MobileFSM.STATE;
import engine.ai.StaticMobActions;
import engine.exception.MsgSendException;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.AttackJob;
import engine.jobs.DeferredPowerJob;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import engine.powers.DamageShield;
import engine.powers.PowersBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.powers.effectmodifiers.WeaponProcEffectModifier;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

public enum CombatManager {

	COMBATMANAGER;

	/**
	 * Message sent by player to attack something.
	 */
	public static void setAttackTarget(AttackCmdMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		int targetType;
		AbstractWorldObject target;

		if (TargetedActionMsg.un2cnt == 60 || TargetedActionMsg.un2cnt == 70) {
			return;
		}

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null) {

			return;
		}

		//source must match player this account belongs to
		if (player.getObjectUUID() != msg.getSourceID() || player.getObjectType().ordinal() != msg.getSourceType()) {
			Logger.error("Msg Source ID " + msg.getSourceID() + " Does not Match Player ID " + player.getObjectUUID() );

			return;
		}

		targetType = msg.getTargetType();

		if (targetType == GameObjectType.PlayerCharacter.ordinal()) {
			target = PlayerCharacter.getFromCache(msg.getTargetID());
		} else if (targetType == GameObjectType.Building.ordinal()) {
			target = BuildingManager.getBuildingFromCache(msg.getTargetID());
		} else if (targetType == GameObjectType.Mob.ordinal()) {
			target = StaticMobActions.getFromCache(msg.getTargetID());
		}else{
			player.setCombatTarget(null);
			return; //not valid type to attack
		}
		// quit of the combat target is already the current combat target
		// or there is no combat target
		if (target == null) {
			return;
		}

		//set sources target
		player.setCombatTarget(target);

		//put in combat if not already
		if (!player.isCombat()) {
			toggleCombat(true, origin);
		}

		//make character stand if sitting
		if (player.isSit()) {
			toggleSit(false, origin);
		}

		AttackTarget(player, target);

	}

	public static void AttackTarget(PlayerCharacter pc, AbstractWorldObject target) {

		boolean swingOffhand = false;

		//check my weapon can I do an offhand attack
		Item weaponOff = pc.getCharItemManager().getEquipped().get(MBServerStatics.SLOT_OFFHAND);
		Item weaponMain = pc.getCharItemManager().getEquipped().get(MBServerStatics.SLOT_MAINHAND);

		// if you carry something in the offhand thats a weapon you get to swing it
		if (weaponOff != null) {
			if (weaponOff.getItemBase().getType().equals(ItemType.WEAPON)) {
				swingOffhand = true;
			}
		}
		// if you carry  nothing in either hand you get to swing your offhand
		if (weaponOff == null && weaponMain == null) {
			swingOffhand = true;
		}

		//we always swing our mainhand if we are not on timer
		JobContainer main = pc.getTimers().get("Attack" + MBServerStatics.SLOT_MAINHAND);
		if (main == null) {
			// no timers on the mainhand, lets submit a job to swing
			CombatManager.createTimer(pc, MBServerStatics.SLOT_MAINHAND, 1, true); // attack in 0.1 of a second
		}

		if (swingOffhand) {
			/*
            only swing offhand if we have a weapon in it or are unarmed in both hands
            and no timers running
			 */
			JobContainer off = pc.getTimers().get("Attack" + MBServerStatics.SLOT_OFFHAND);
			if (off == null) {
				CombatManager.createTimer(pc, MBServerStatics.SLOT_OFFHAND, 1, true); // attack in 0.1 of a second
			}
		}
	}

	public static void setAttackTarget(PetAttackMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter player;
		Mob pet;
		int targetType;
		AbstractWorldObject target;

		if (TargetedActionMsg.un2cnt == 60 || TargetedActionMsg.un2cnt == 70)
			return;

		player = SessionManager.getPlayerCharacter(origin);

		if (player == null)
			return;

		pet = player.getPet();

		if (pet == null)
			return;

		targetType = msg.getTargetType();

		if (targetType == GameObjectType.PlayerCharacter.ordinal())
			target = PlayerCharacter.getFromCache(msg.getTargetID());
		else if (targetType == GameObjectType.Building.ordinal())
			target = BuildingManager.getBuildingFromCache(msg.getTargetID());
		else if (targetType == GameObjectType.Mob.ordinal())
			target = StaticMobActions.getFromCache(msg.getTargetID());
		else {
			pet.setCombatTarget(null);
			return; //not valid type to attack
		}
		
		if (pet.equals(target))
			return;

		// quit of the combat target is already the current combat target
		// or there is no combat target

		if (target == null || target == pet.getCombatTarget())
			return;



		//set sources target
		pet.setCombatTarget(target);
		pet.state = STATE.Attack;
		//		setFirstHitCombatTarget(player,target);

		//put in combat if not already
		if (!pet.isCombat())
			pet.setCombat(true);

		//make character stand if sitting
		if (pet.isSit())
			toggleSit(false, origin);

	}

	private static void removeAttackTimers(AbstractCharacter ac) {

		JobContainer main;
		JobContainer off;

		if (ac == null)
			return;

		main = ac.getTimers().get("Attack" + MBServerStatics.SLOT_MAINHAND);
		off = ac.getTimers().get("Attack" + MBServerStatics.SLOT_OFFHAND);

		if (main != null)
			JobScheduler.getInstance().cancelScheduledJob(main);

		ac.getTimers().remove("Attack" + MBServerStatics.SLOT_MAINHAND);

		if (off != null)
			JobScheduler.getInstance().cancelScheduledJob(off);

		ac.getTimers().remove("Attack" + MBServerStatics.SLOT_OFFHAND);

		ac.setCombatTarget(null);

	}

	/**
	 * Begin Attacking
	 */
	public static void doCombat(AbstractCharacter ac, int slot) {

		int ret = 0;

		if (ac == null)
			return;

		// Attempt to eat null targets until we can clean
		// up this unholy mess and refactor it into a thread.


		ret = attemptCombat(ac, slot);

		//handle pets
		if (ret < 2 && ac.getObjectType().equals(GameObjectType.Mob)) {
			Mob mob = (Mob) ac;
			if (mob.isPet()) {
				return;
			}
		}

		//ret values
		//0: not valid attack, fail attack
		//1: cannot attack, wrong hand
		//2: valid attack
		//3: cannot attack currently, continue checking

		if (ret == 0 || ret == 1) {

			//Could not attack, clear timer

			ConcurrentHashMap<String, JobContainer> timers = ac.getTimers();

			if (timers != null && timers.containsKey("Attack" + slot))
				timers.remove("Attack" + slot);

			//clear combat target if not valid attack
			if (ret == 0)
				ac.setCombatTarget(null);

		} else if (ret == 3) {
			//Failed but continue checking. reset timer
			createTimer(ac, slot, 5, false);
		}
	}

	/**
	 * Verify can attack target
	 */
	private static int attemptCombat(AbstractCharacter abstractCharacter, int slot) {

		if (abstractCharacter == null) {
			// debugCombat(ac, "Source is null");
			return 0;
		}

		try {
			//Make sure player can attack
			PlayerBonuses bonus = abstractCharacter.getBonuses();

			if (bonus != null && bonus.getBool(ModType.ImmuneToAttack, SourceType.None))
				return 0;

			AbstractWorldObject target = abstractCharacter.getCombatTarget();

			if (target == null){
				return 0;
			}
				

			//target must be valid type
			if (AbstractWorldObject.IsAbstractCharacter(target)) {
				AbstractCharacter tar = (AbstractCharacter) target;
				//must be alive, attackable and in World
				if (!tar.isAlive()) {
					return 0;
				}
				else if (tar.isSafeMode()) {
					return 0;
				}
				else if (!tar.isActive()) {
					return 0;
				}

				if (target.getObjectType().equals(GameObjectType.PlayerCharacter) && abstractCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) && abstractCharacter.getTimers().get("Attack" + slot) == null) {
					if (!((PlayerCharacter) abstractCharacter).canSee((PlayerCharacter) target)) {
						return 0;
					}
				}

				//must not be immune to all or immune to attack
				Resists res = tar.getResists();
				bonus = tar.getBonuses();
				if (bonus != null && !bonus.getBool(ModType.NoMod, SourceType.ImmuneToAttack)) {
					if (res != null) {
						if (res.immuneToAll() || res.immuneToAttacks()) {
							return 0;
						}
					}
				}
			}
			else if (target.getObjectType().equals(GameObjectType.Building)) {
				Building tar = (Building) target;

				// Cannot attack an invuln building

				if (tar.isVulnerable() == false) {
					return 0;
				}

			}
			else {
				return 0; //only characters and buildings may be attacked
			}

			//source must be in world and alive
			if (!abstractCharacter.isActive()) {
				return 0;
			}
			else if (!abstractCharacter.isAlive()) {
				return 0;
			}

			//make sure source is in combat mode
			if (!abstractCharacter.isCombat()) {
				return 0;
			}

			//See if either target is in safe zone
			if (abstractCharacter.getObjectType().equals(GameObjectType.PlayerCharacter) && target.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				if (((PlayerCharacter) abstractCharacter).inSafeZone() || ((PlayerCharacter) target).inSafeZone()) {
					return 0;
				}
			}

			if (!(slot == MBServerStatics.SLOT_MAINHAND || slot == MBServerStatics.SLOT_OFFHAND)) {
				return 0;
			}

			if (abstractCharacter.getCharItemManager() == null) {
				return 0;
			}

			//get equippment
			ConcurrentHashMap<Integer, Item> equipped = abstractCharacter.getCharItemManager().getEquipped();
			boolean hasNoWeapon = false;

			if (equipped == null) {
				return 0;
			}

			//get Weapon
			boolean isWeapon = true;
			Item weapon = equipped.get(slot);
			ItemBase wb = null;
			if (weapon == null) {
				isWeapon = false;
			}
			else {
				ItemBase ib = weapon.getItemBase();
				if (ib == null || !ib.getType().equals(ItemType.WEAPON)) {
					isWeapon = false;
				}
				else {
					wb = ib;
				}
			}

			//if no weapon, see if other hand has a weapon
			if (!isWeapon) {
				//no weapon, see if other hand has a weapon
				if (slot == MBServerStatics.SLOT_MAINHAND) {
					//make sure offhand has weapon, not shield
					Item weaponOff = equipped.get(MBServerStatics.SLOT_OFFHAND);
					if (weaponOff != null) {
						ItemBase ib = weaponOff.getItemBase();
						if (ib == null || !ib.getType().equals(ItemType.WEAPON)) {
							hasNoWeapon = true;
						}
						else {
							// debugCombat(ac, "mainhand, weapon in other hand");
							return 1; //no need to attack with this hand
						}
					}
					else {
						hasNoWeapon = true;
					}
				}
				else {
					if (equipped.get(MBServerStatics.SLOT_MAINHAND) == null) {
						// debgCombat(ac, "offhand, weapon in other hand");
						return 1; //no need to attack with this hand
					}
				}
			}

			//Source can attack.
			//NOTE Don't 'return;' beyond this point until timer created
			boolean attackFailure = false;

			//Target can't attack on move with ranged weapons.
			if ((wb != null) && (wb.getRange() > 35f) && abstractCharacter.isMoving()) {
				// debugCombat(ac, "Cannot attack with throwing weapon while moving");
				attackFailure = true;
			}

			//if not enough stamina, then skip attack
			if (wb == null) {
				if (abstractCharacter.getStamina() < 1) {
					// debugCombat(ac, "Not enough stamina to attack");
					attackFailure = true;
				}
			}
			else if (abstractCharacter.getStamina() < wb.getWeight()) {
				// debugCombat(ac, "Not enough stamina to attack");
				attackFailure = true;
			}

			//skipping for now to test out mask casting.
			//		//if attacker is casting, then skip this attack
			//		if (ac.getLastPower() != null) {
			//			debugCombat(ac, "Cannot attack, curently casting");
			//			attackFailure = true;
			//		}
			//see if attacker is stunned. If so, stop here
			bonus = abstractCharacter.getBonuses();
			if (bonus != null && bonus.getBool(ModType.Stunned,SourceType.None)) {
				// debugCombat(ac, "Cannot attack while stunned");
				attackFailure = true;
			}

			//Get Range of weapon
			float range;
			if (hasNoWeapon) {
				range = MBServerStatics.NO_WEAPON_RANGE;
			}
			else {
				range = getWeaponRange(wb);
				if (bonus != null){
					float buffRange = 1;
					buffRange += bonus.getFloat(ModType.WeaponRange, SourceType.None) *.01f;
					range*= buffRange;
				}
			}

			if (abstractCharacter.getObjectType() == GameObjectType.Mob) {
				Mob minion = (Mob) abstractCharacter;
				if (minion.isSiege) {
					range = 300f;
				}
			}

			//Range check.
			if (NotInRange(abstractCharacter, target, range)) {
				//target is in stealth and can't be seen by source
				if (target.getObjectType().equals(GameObjectType.PlayerCharacter) && abstractCharacter.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					if (!((PlayerCharacter) abstractCharacter).canSee((PlayerCharacter) target)) {
						// debugCombat(ac, "cannot see target.");
						return 0;
					}
				}
				attackFailure = true;
			}

			//handle pet, skip timers (handled by AI)
			if (abstractCharacter.getObjectType().equals(GameObjectType.Mob)) {
				Mob mob = (Mob) abstractCharacter;
				if (mob.isPet()) {
					attack(abstractCharacter, target, weapon, wb, (slot == MBServerStatics.SLOT_MAINHAND) ? true : false);
					return 2;
				}
			}

			//TODO Verify attacker has los (if not ranged weapon).
			if (!attackFailure) {
				if (hasNoWeapon || abstractCharacter.getObjectType().equals(GameObjectType.Mob)) {
					createTimer(abstractCharacter, slot, 20, true); //2 second for no weapon
				}
				else {
					int wepSpeed = (int) (wb.getSpeed());
					if (weapon != null && weapon.getBonusPercent(ModType.WeaponSpeed, SourceType.None) != 0f) //add weapon speed bonus
					{
						wepSpeed *= (1 + weapon.getBonus(ModType.WeaponSpeed, SourceType.None));
					}
					if (abstractCharacter.getBonuses() != null && abstractCharacter.getBonuses().getFloatPercentAll(ModType.AttackDelay, SourceType.None) != 0f) //add effects speed bonus
					{
						wepSpeed *= (1 + abstractCharacter.getBonuses().getFloatPercentAll(ModType.AttackDelay, SourceType.None));
					}
					if (wepSpeed < 10) {
						wepSpeed = 10; //Old was 10, but it can be reached lower with legit buffs,effects.
					}
					createTimer(abstractCharacter, slot, wepSpeed, true);
				}

				if (target == null)
					return 0;

				attack(abstractCharacter, target, weapon, wb, (slot == MBServerStatics.SLOT_MAINHAND) ? true : false);
			}
			else {
				// changed this to half a second to make combat attempts more aggressive than movement sync
				createTimer(abstractCharacter, slot, 5, false); //0.5 second timer if attack fails
				//System.out.println("Attack attempt failed");
			}

		}  catch(Exception e) {
			return 0;
		}
		return 2;
	}

	private static void debugCombat(AbstractCharacter ac, String reason) {
		if (ac == null) {
			return;
		}

		//if DebugMeleeSync is on, then debug reason for melee failure
		if (ac.getDebug(64)) {
			if (ac.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				String out = "Attack Failure: " + reason;
				ChatManager.chatSystemInfo((PlayerCharacter) ac, out);
			}
		}
	}

	private static void debugCombatRange(AbstractCharacter ac, Vector3fImmutable sl, Vector3fImmutable tl, float range, float distance) {
		if (ac == null || sl == null || tl == null) {
			return;
		}

		//if DebugMeleeSync is on, then debug reason for melee failure
		if (ac.getDebug(64)) {
			if (ac.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				String out = "Attack Failure: Out of Range: Range: " + distance + ", weaponRange: " + range;
				out += ", sourceLoc: " + sl.x + ", " + sl.y + ", " + sl.z;
				out += ", targetLoc: " + tl.x + ", " + tl.y + ", " + tl.z;
				ChatManager.chatSystemInfo((PlayerCharacter) ac, out);
			}
		}
	}

	private static void createTimer(AbstractCharacter ac, int slot, int time, boolean success) {
		ConcurrentHashMap<String, JobContainer> timers = ac.getTimers();
		if (timers != null) {
			AttackJob aj = new AttackJob(ac, slot, success);
			JobContainer job;
			job = JobScheduler.getInstance().scheduleJob(aj, (time * 100));
			timers.put("Attack" + slot, job);
		} else {
			Logger.error( "Unable to find Timers for Character " + ac.getObjectUUID());
		}
	}

	/**
	 * Attempt to attack target
	 */
	private static void attack(AbstractCharacter ac, AbstractWorldObject target, Item weapon, ItemBase wb, boolean mainHand) {

		float atr;
		int minDamage, maxDamage;
		int errorTrack = 0;

		try {

			if (ac == null)
				return;

			if (target == null)
				return;

			if (mainHand) {
				atr = ac.getAtrHandOne();
				minDamage = ac.getMinDamageHandOne();
				maxDamage = ac.getMaxDamageHandOne();
			}
			else {
				atr = ac.getAtrHandTwo();
				minDamage = ac.getMinDamageHandTwo();
				maxDamage = ac.getMaxDamageHandTwo();
			}

			boolean tarIsRat = false;

			if (target.getObjectTypeMask() == MBServerStatics.MASK_RAT)
				tarIsRat = true;
			else if (target.getObjectType() == GameObjectType.PlayerCharacter){
				PlayerCharacter pTar = (PlayerCharacter)target;
				for (Effect eff: pTar.getEffects().values()){
					if (eff.getPowerToken() == 429513599 || eff.getPowerToken() == 429415295){
						tarIsRat = true;
					}
				}
			}

			//Dont think we need to do this anymore.
			if (tarIsRat){
				//strip away current % dmg buffs then add with rat %
				if (ac.getBonuses().getFloatPercentAll(ModType.Slay, SourceType.Rat) != 0){
					

					float percent = 1 + ac.getBonuses().getFloatPercentAll(ModType.Slay, SourceType.Rat);

					minDamage *= percent;
					maxDamage *= percent;
				}

			}

			errorTrack = 1;

			//subtract stamina
			if (wb == null) {
				ac.modifyStamina(-0.5f, ac, true);
			}
			else {
				float stam = wb.getWeight() / 3;
				stam = (stam < 1) ? 1 : stam;
				ac.modifyStamina(-(stam), ac, true);
			}

			ac.cancelOnAttackSwing();

			errorTrack = 2;

			//set last time this player has attacked something.
			if (target.getObjectType().equals(GameObjectType.PlayerCharacter) && target.getObjectUUID() != ac.getObjectUUID() && ac.getObjectType() == GameObjectType.PlayerCharacter) {
				ac.setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
				((PlayerCharacter) target).setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
			}
			else {
				ac.setTimeStamp("LastCombatMob", System.currentTimeMillis());
			}

			errorTrack = 3;

			//Get defense for target
			float defense;
			if (target.getObjectType().equals(GameObjectType.Building)) {
				
				if (BuildingManager.getBuildingFromCache(target.getObjectUUID()) == null){
					ac.setCombatTarget(null);
					return;
				}
				defense = 0;

				Building building = (Building)target;
				if (building.getParentZone() != null && building.getParentZone().isPlayerCity()){

					if (System.currentTimeMillis() > building.getTimeStamp("CallForHelp")){
						building.getTimestamps().put("CallForHelp", System.currentTimeMillis() + 15000);
						int count = 0;
						for (Mob mob:building.getParentZone().zoneMobSet){
							if (!mob.isPlayerGuard)
								continue;
							if (mob.getCombatTarget() != null)
								continue;
							if (mob.getGuild() != null && building.getGuild() != null)
								if (!Guild.sameGuild(mob.getGuild().getNation(), building.getGuild().getNation()))
									continue;

							if (mob.getLoc().distanceSquared2D(building.getLoc()) > sqr(300))
								continue;

							if (count == 5)
								count++;

							mob.setCombatTarget(ac);
							mob.state = STATE.Attack;
						}
					}
				}
			}
			else {
				AbstractCharacter tar = (AbstractCharacter) target;
				defense = tar.getDefenseRating();
				//Handle target attacking back if in combat and has no other target
				handleRetaliate(tar, ac);
			}

			errorTrack = 4;

			//Get hit chance
			int chance;
			float dif = atr - defense;
			if (dif > 100) {
				chance = 94;
			}
			else if (dif < -100) {
				chance = 4;
			}
			else {
				chance = (int) ((0.45 * dif) + 49);
			}

			errorTrack = 5;

			//calculate hit/miss
			int roll = ThreadLocalRandom.current().nextInt(100);
			DeferredPowerJob dpj = null;
			if (roll < chance) {
				if (ac.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					updateAttackTimers((PlayerCharacter) ac, target, true);
				}

				boolean skipPassives = false;
				PlayerBonuses bonuses = ac.getBonuses();
				if (bonuses != null && bonuses.getBool(ModType.IgnorePassiveDefense, SourceType.None)) {
					skipPassives = true;
				}

				AbstractCharacter tarAc = null;
				if (AbstractWorldObject.IsAbstractCharacter(target)) {
					tarAc = (AbstractCharacter) target;
				}

				errorTrack = 6;

				// Apply Weapon power effect if any. don't try to apply twice if
				// dual wielding. Perform after passive test for sync purposes.


				if (ac.getObjectType().equals(GameObjectType.PlayerCharacter) && (mainHand || wb.isTwoHanded())) {
					dpj = ((PlayerCharacter) ac).getWeaponPower();
					if (dpj != null) {
						float attackRange = getWeaponRange(wb);

						dpj.attack(target, attackRange);

						if (dpj.getPower() != null && (dpj.getPowerToken() == -1851459567 || dpj.getPowerToken() == -1851489518))
							((PlayerCharacter)ac).setWeaponPower(dpj);
					}
				}
				//check to apply second backstab.
				if (ac.getObjectType().equals(GameObjectType.PlayerCharacter) && !mainHand){
					dpj = ((PlayerCharacter) ac).getWeaponPower();
					if (dpj != null && dpj.getPower() != null && (dpj.getPowerToken() == -1851459567 || dpj.getPowerToken() == -1851489518)) {
						float attackRange = getWeaponRange(wb);
						dpj.attack(target, attackRange);
					}
				}

				errorTrack = 7;

				//Hit, check if passive kicked in
				boolean passiveFired = false;
				if (!skipPassives && tarAc != null) {
					if (target.getObjectType().equals(GameObjectType.PlayerCharacter)) {

						//Handle Block passive
						if (testPassive(ac, tarAc, "Block") && canTestBlock(ac, target)) {

							if (!target.isAlive())
								return;

							sendPassiveDefenseMessage(ac, wb, target, MBServerStatics.COMBAT_SEND_BLOCK, dpj,mainHand);
							passiveFired = true;
						}

						//Handle Parry passive
						if (!passiveFired) {
							if (canTestParry(ac, target) && testPassive(ac, tarAc, "Parry")) {
								if (!target.isAlive())
									return;
								sendPassiveDefenseMessage(ac, wb, target, MBServerStatics.COMBAT_SEND_PARRY, dpj,mainHand);
								passiveFired = true;
							}
						}
					}

					errorTrack = 8;

					//Handle Dodge passive
					if (!passiveFired) {
						if (testPassive(ac, tarAc, "Dodge")) {

							if (!target.isAlive())
								return;

							sendPassiveDefenseMessage(ac, wb, target, MBServerStatics.COMBAT_SEND_DODGE, dpj,mainHand);
							passiveFired = true;
						}
					}
				}

				//return if passive (Block, Parry, Dodge) fired

				if (passiveFired)
					return;

				errorTrack = 9;

				//Hit and no passives
				//if target is player, set last attack timestamp
				if (target.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					updateAttackTimers((PlayerCharacter) target, ac, false);
				}

				//Get damage Type
				DamageType damageType;
				if (wb != null) {
					damageType = wb.getDamageType();
				}
				else if (ac.getObjectType().equals(GameObjectType.Mob) && ((Mob) ac).isSiege) {
					damageType = DamageType.Siege;
				}
				else {
					damageType = DamageType.Crush;
				}

				errorTrack = 10;

				//Get target resists
				Resists resists = null;

				if (tarAc != null) {
					resists = tarAc.getResists();
				}
				else if (target.getObjectType().equals(GameObjectType.Building)) {
					resists = ((Building) target).getResists();
				}

				//make sure target is not immune to damage type;
				if (resists != null && resists.immuneTo(damageType)) {
					sendCombatMessage(ac, target, 0f, wb, dpj,mainHand);
					return;
				}

				//				PowerProjectileMsg ppm = new PowerProjectileMsg(ac,tarAc);
				//				DispatchMessage.dispatchMsgToInterestArea(ac, ppm, DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
				//

				errorTrack = 11;

				//Calculate Damage done

				float damage;

				if (wb != null) {
					damage = calculateDamage(ac, tarAc, minDamage, maxDamage, damageType, resists);
				}
				else {
					damage = calculateDamage(ac, tarAc, minDamage, maxDamage, damageType, resists);
				}

				float d = 0f;

				errorTrack = 12;

				//Subtract Damage from target's health
				if (tarAc != null) {
					if (tarAc.isSit()) {
						damage *= 2.5f; //increase damage if sitting
					}
					if (tarAc.getObjectType() == GameObjectType.Mob) {
						ac.setHateValue(damage * MBServerStatics.PLAYER_COMBAT_HATE_MODIFIER);
						StaticMobActions.handleDirectAggro(((Mob) tarAc),ac);
					}

					if (tarAc.getHealth() > 0)
						d = tarAc.modifyHealth(-damage, ac, false);

				}
				else if (target.getObjectType().equals(GameObjectType.Building)) {
					
					if (BuildingManager.getBuildingFromCache(target.getObjectUUID()) == null){
						ac.setCombatTarget(null);
						return;
					}
					if (target.getHealth() > 0)
						d = ((Building) target).modifyHealth(-damage, ac);
				}

				errorTrack = 13;

				//Test to see if any damage needs done to weapon or armor
				testItemDamage(ac, target, weapon, wb);

				// if target is dead, we got the killing blow, remove attack timers on our weapons
				if (tarAc != null && !tarAc.isAlive()) {
					removeAttackTimers(ac);
				}

				//test double death fix
				if (d != 0) {
					sendCombatMessage(ac, target, damage, wb, dpj,mainHand); //send damage message
				}

				errorTrack = 14;

				//handle procs
				if (weapon != null && tarAc != null && tarAc.isAlive()) {
					ConcurrentHashMap<String, Effect> effects = weapon.getEffects();
					for (Effect eff : effects.values()) {
						if (eff == null) {
							continue;
						}
						HashSet<AbstractEffectModifier> aems = eff.getEffectModifiers();
						if (aems != null) {
							for (AbstractEffectModifier aem : aems) {
								if (!tarAc.isAlive()) {
									break;
								}
								if (aem instanceof WeaponProcEffectModifier) {
									int procChance = ThreadLocalRandom.current().nextInt(100);
									if (procChance < MBServerStatics.PROC_CHANCE) {
										((WeaponProcEffectModifier) aem).applyProc(ac, target);
									}
								}
							}
						}
					}
				}

				errorTrack = 15;

				//handle damage shields
				if (ac.isAlive() && tarAc != null && tarAc.isAlive()) {
					handleDamageShields(ac, tarAc, damage);
				}
			}
			else {
				int animationOverride = 0;
				// Apply Weapon power effect if any.
				// don't try to apply twice if dual wielding.
				if (ac.getObjectType().equals(GameObjectType.PlayerCharacter) && (mainHand || wb.isTwoHanded())) {
					dpj = null;
					dpj = ((PlayerCharacter) ac).getWeaponPower();

					if (dpj != null) {
						PowersBase wp = dpj.getPower();
						if (wp.requiresHitRoll() == false) {
							float attackRange = getWeaponRange(wb);
							dpj.attack(target,attackRange);
						}
						else {
							((PlayerCharacter) ac).setWeaponPower(null);
						}

					}
				}
				if (target.getObjectType() == GameObjectType.Mob) {
					StaticMobActions.handleDirectAggro(((Mob) target),ac);
				}

				errorTrack = 17;

				//miss, Send miss message
				sendCombatMessage(ac, target, 0f, wb, dpj,mainHand);

				//if attacker is player, set last attack timestamp
				if (ac.getObjectType().equals(GameObjectType.PlayerCharacter)) {
					updateAttackTimers((PlayerCharacter) ac, target, true);
				}
			}

			errorTrack = 18;

			//cancel effects that break on attack or attackSwing
			ac.cancelOnAttack();

		} catch (Exception e) {
			Logger.error(ac.getName() + ' ' + errorTrack + ' ' + e.toString());
		}
	}

	public static boolean canTestParry(AbstractCharacter ac, AbstractWorldObject target) {

		if (ac == null || target == null || !AbstractWorldObject.IsAbstractCharacter(target))
			return false;

		AbstractCharacter tar = (AbstractCharacter) target;

		CharacterItemManager acItem = ac.getCharItemManager();
		CharacterItemManager tarItem = tar.getCharItemManager();

		if (acItem == null || tarItem == null)
			return false;

		Item acMain = acItem.getItemFromEquipped(1);
		Item acOff = acItem.getItemFromEquipped(2);
		Item tarMain = tarItem.getItemFromEquipped(1);
		Item tarOff = tarItem.getItemFromEquipped(2);

		return !isRanged(acMain) && !isRanged(acOff) && !isRanged(tarMain) && !isRanged(tarOff);
	}

	public static boolean canTestBlock(AbstractCharacter ac, AbstractWorldObject target) {

		if (ac == null || target == null || !AbstractWorldObject.IsAbstractCharacter(target))
			return false;

		AbstractCharacter tar = (AbstractCharacter) target;

		CharacterItemManager acItem = ac.getCharItemManager();
		CharacterItemManager tarItem = tar.getCharItemManager();

		if (acItem == null || tarItem == null)
			return false;



		Item tarOff = tarItem.getItemFromEquipped(2);


		if (tarOff == null)
			return false;

		return tarOff.getItemBase().isShield() != false;
	}

	private static boolean isRanged(Item item) {

		if (item == null)
			return false;

		ItemBase ib = item.getItemBase();

		if (ib == null)
			return false;

		if (ib.getType().equals(ItemType.WEAPON) == false)
			return false;

		return ib.getRange() > MBServerStatics.RANGED_WEAPON_RANGE;


	}

	private static float calculateDamage(AbstractCharacter source, AbstractCharacter target, float minDamage, float maxDamage, DamageType damageType, Resists resists) {
		//get range between min and max
		float range = maxDamage - minDamage;

		//Damage is calculated twice to average a more central point
		float damage = ThreadLocalRandom.current().nextFloat() * range;
		damage = (damage + (ThreadLocalRandom.current().nextFloat() * range)) *.5f;

		//put it back between min and max
		damage += minDamage;

		//calculate resists in if any
		if (resists != null) {
			return resists.getResistedDamage(source, target, damageType, damage, 0);
		} else {
			return damage;
		}
	}

	private static void sendPassiveDefenseMessage(AbstractCharacter source, ItemBase wb, AbstractWorldObject target, int passiveType, DeferredPowerJob dpj, boolean mainHand) {

		int swingAnimation =  getSwingAnimation(wb, dpj,mainHand);

		if (dpj != null){
			if(PowersManager.AnimationOverrides.containsKey(dpj.getAction().getEffectID()))
				swingAnimation = PowersManager.AnimationOverrides.get(dpj.getAction().getEffectID());
		}
		TargetedActionMsg cmm = new TargetedActionMsg(source,swingAnimation, target, passiveType);
		DispatchMessage.sendToAllInRange(target, cmm);

	}

	private static void sendCombatMessage(AbstractCharacter source, AbstractWorldObject target, float damage, ItemBase wb, DeferredPowerJob dpj, boolean mainHand) {

		int swingAnimation =  getSwingAnimation(wb, dpj,mainHand);

		if (dpj != null){
			if(PowersManager.AnimationOverrides.containsKey(dpj.getAction().getEffectID()))
				swingAnimation = PowersManager.AnimationOverrides.get(dpj.getAction().getEffectID());
		}

		if (source.getObjectType() == GameObjectType.PlayerCharacter){
			for (Effect eff: source.getEffects().values()){
				if (eff.getPower() != null && (eff.getPower().getToken() == 429506943 || eff.getPower().getToken() == 429408639 || eff.getPower().getToken() == 429513599 ||eff.getPower().getToken() ==  429415295))
					swingAnimation = 0;
			}
		}
		TargetedActionMsg cmm = new TargetedActionMsg(source, target, damage, swingAnimation);
		DispatchMessage.sendToAllInRange(target, cmm);
	}

	public static int animation = 0;

	public static int getSwingAnimation(ItemBase wb, DeferredPowerJob dpj, boolean mainHand) {
		int token = 0;
		if (dpj != null) {
			token = (dpj.getPower() != null) ? dpj.getPower().getToken() : 0;
		}

		if (token == 563721004) //kick animation
		{
			return 79;
		}

		if (CombatManager.animation != 0) {
			return CombatManager.animation;
		}

		if (wb == null) {
			return 75;
		}
		if (mainHand){
			if (wb.getAnimations().size() > 0){
				int animation = wb.getAnimations().get(0);
				int random = ThreadLocalRandom.current().nextInt(wb.getAnimations().size());
				try{
					animation = wb.getAnimations().get(random);
					return animation;
				}catch(Exception e){
					Logger.error( e.getMessage());
					return wb.getAnimations().get(0);

				}

			}else if (wb.getOffHandAnimations().size() > 0){
				int animation = wb.getOffHandAnimations().get(0);
				int random = ThreadLocalRandom.current().nextInt(wb.getOffHandAnimations().size());
				try{
					animation = wb.getOffHandAnimations().get(random);
					return animation;
				}catch(Exception e){
					Logger.error( e.getMessage());
					return wb.getOffHandAnimations().get(0);

				}
			}
		}else{
			if (wb.getOffHandAnimations().size() > 0){
				int animation = wb.getOffHandAnimations().get(0);
				int random = ThreadLocalRandom.current().nextInt(wb.getOffHandAnimations().size());
				try{
					animation = wb.getOffHandAnimations().get(random);
					return animation;
				}catch(Exception e){
					Logger.error( e.getMessage());
					return wb.getOffHandAnimations().get(0);

				}
			}else
				if (wb.getAnimations().size() > 0){
					int animation = wb.getAnimations().get(0);
					int random = ThreadLocalRandom.current().nextInt(wb.getAnimations().size());
					try{
						animation = wb.getAnimations().get(random);
						return animation;
					}catch(Exception e){
						Logger.error( e.getMessage());
						return wb.getAnimations().get(0);

					}

				}
		}


		String required = wb.getSkillRequired();
		String mastery = wb.getMastery();
		if (required.equals("Unarmed Combat")) {
			return 75;
		} else if (required.equals("Sword")) {
			if (wb.isTwoHanded()) {
				return 105;
			} else {
				return 98;
			}
		} else if (required.equals("Staff") || required.equals("Pole Arm")) {
			return 85;
		} else if (required.equals("Spear")) {
			return 92;
		} else if (required.equals("Hammer") || required.equals("Axe")) {
			if (wb.isTwoHanded()) {
				return 105;
			} else if (mastery.equals("Throwing")) {
				return 115;
			} else {
				return 100;
			}
		} else if (required.equals("Dagger")) {
			if (mastery.equals("Throwing")) {
				return 117;
			} else {
				return 81;
			}
		} else if (required.equals("Crossbow")) {
			return 110;
		} else if (required.equals("Bow")) {
			return 109;
		} else if (wb.isTwoHanded()) {
			return 105;
		} else {
			return 100;
		}
	}

	private static boolean testPassive(AbstractCharacter source, AbstractCharacter target, String type) {

		float chance = target.getPassiveChance(type, source.getLevel(), true);

		if (chance == 0f)
			return false;


		//max 75% chance of passive to fire
		if (chance > 75f)
			chance = 75f;

		int roll = ThreadLocalRandom.current().nextInt(100);

		//Passive fired
		//Passive did not fire
		return roll < chance;

	}

	private static void updateAttackTimers(PlayerCharacter pc, AbstractWorldObject target, boolean attack) {

		//Set Attack Timers
		if (target.getObjectType().equals(GameObjectType.PlayerCharacter))
			pc.setLastPlayerAttackTime();
		else
			pc.setLastMobAttackTime();
	}

	public static float getWeaponRange(ItemBase weapon) {
		if (weapon == null)
			return 0f;

		return weapon.getRange();
	}

	public static void toggleCombat(ToggleCombatMsg msg, ClientConnection origin) {
		toggleCombat(msg.toggleCombat(), origin);
	}

	public static void toggleCombat(SetCombatModeMsg msg, ClientConnection origin) {
		toggleCombat(msg.getToggle(), origin);
	}

	private static void toggleCombat(boolean toggle, ClientConnection origin) {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);

		if (pc == null)
			return;

		pc.setCombat(toggle);

		if (!toggle) // toggle is move it to false so clear combat target
			pc.setCombatTarget(null); //clear last combat target

		UpdateStateMsg rwss = new UpdateStateMsg();
		rwss.setPlayer(pc);
		DispatchMessage.dispatchMsgToInterestArea(pc, rwss, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, false, false);
	}

	private static void toggleSit(boolean toggle, ClientConnection origin) {

		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);

		if (pc == null)
			return;

		pc.setSit(toggle);

		UpdateStateMsg rwss = new UpdateStateMsg();
		rwss.setPlayer(pc);
		DispatchMessage.dispatchMsgToInterestArea(pc, rwss,  DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
	}

	public static boolean NotInRange(AbstractCharacter ac, AbstractWorldObject target, float range) {
		Vector3fImmutable sl = ac.getLoc();
		Vector3fImmutable tl = target.getLoc();
		//add Hitbox's to range.
		range += (calcHitBox(ac) + calcHitBox(target));

		float magnitudeSquared = tl.distanceSquared(sl);

		return magnitudeSquared > range * range;

	}

	//Called when character takes damage.
	public static void handleRetaliate(AbstractCharacter tarAc, AbstractCharacter ac) {
		if (ac == null || tarAc == null) {
			return;
		}
		if (ac.equals(tarAc)) {
			return;
		}
		
		if (tarAc.isMoving() && tarAc.getObjectType().equals(GameObjectType.PlayerCharacter))
			return;
		
		if (!tarAc.isAlive() || !ac.isAlive())
			return;
		boolean isCombat = tarAc.isCombat();
		//If target in combat and has no target, then attack back
		AbstractWorldObject awoCombTar = tarAc.getCombatTarget();
		if ((tarAc.isCombat() && awoCombTar == null) || (isCombat && awoCombTar != null && (!awoCombTar.isAlive() ||tarAc.isCombat() && NotInRange(tarAc, awoCombTar, tarAc.getRange()))) || (tarAc != null && tarAc.getObjectType() == GameObjectType.Mob && ((Mob) tarAc).isSiege)) {
			// we are in combat with no valid target
			if (tarAc.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				PlayerCharacter pc = (PlayerCharacter) tarAc;
				tarAc.setCombatTarget(ac);
				pc.setLastTarget(ac.getObjectType(), ac.getObjectUUID());
				if (tarAc.getTimers() != null) {
					if (!tarAc.getTimers().containsKey("Attack" + MBServerStatics.SLOT_MAINHAND)) {
						CombatManager.AttackTarget((PlayerCharacter) tarAc, tarAc.getCombatTarget());
					}
				}
			}
		}

		//Handle pet retaliate if assist is on and pet doesn't have a target.
		if (tarAc.getObjectType().equals(GameObjectType.PlayerCharacter)) {
			Mob pet = ((PlayerCharacter) tarAc).getPet();
			if (pet != null && pet.assist() && pet.getCombatTarget() == null) {
				pet.setCombatTarget(ac);
				pet.state = STATE.Retaliate;
			}
		}

		//Handle Mob Retaliate.
		if (tarAc.getObjectType() == GameObjectType.Mob) {
			Mob retaliater = (Mob) tarAc;
			if (retaliater.getCombatTarget() != null && !retaliater.isSiege)
				return;
			if (ac.getObjectType() == GameObjectType.Mob && retaliater.isSiege)
				return;
			retaliater.setCombatTarget(ac);
			retaliater.state = STATE.Retaliate;

		}
	}

	public static void handleDamageShields(AbstractCharacter ac, AbstractCharacter target, float damage) {
		if (ac == null || target == null) {
			return;
		}
		PlayerBonuses bonuses = target.getBonuses();
		if (bonuses != null) {
			ConcurrentHashMap<AbstractEffectModifier, DamageShield> damageShields = bonuses.getDamageShields();
			float total = 0;
			for (DamageShield ds : damageShields.values()) {
				//get amount to damage back
				float amount;
				if (ds.usePercent()) {
					amount = damage * ds.getAmount() / 100;
				} else {
					amount = ds.getAmount();
				}

				//get resisted damage for damagetype
				Resists resists = ac.getResists();
				if (resists != null) {
					amount = resists.getResistedDamage(target, ac, ds.getDamageType(), amount, 0);
				}

				total += amount;
			}
			if (total > 0) {
				//apply Damage back
				ac.modifyHealth(-total, target, true);

				TargetedActionMsg cmm = new TargetedActionMsg(ac,ac, total, 0);
				DispatchMessage.sendToAllInRange(target, cmm);

			}
		}
	}

	public static float calcHitBox(AbstractWorldObject ac) {
		//TODO Figure out how Str Affects HitBox
		float hitBox = 1;
		switch(ac.getObjectType()){
		case PlayerCharacter:
			PlayerCharacter pc = (PlayerCharacter)ac;
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG) {
				Logger.info("Hit box radius for " + pc.getFirstName() + " is " + ((int) pc.statStrBase / 20f));
			}
			hitBox = 1.5f + (int) ((PlayerCharacter) ac).statStrBase / 20f;
			break;

		case Mob:
			Mob mob = (Mob)ac;
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG)
				Logger.info( "Hit box radius for " + mob.getFirstName()
				+ " is " + ((Mob) ac).getMobBase().getHitBoxRadius());

			hitBox = ((Mob) ac).getMobBase().getHitBoxRadius();
			break;
		case Building:
			Building building = (Building)ac;
			if (building.getBlueprint() == null)
				return 32;
			hitBox = Math.max(building.getBlueprint().getBuildingGroup().getExtents().x,
					building.getBlueprint().getBuildingGroup().getExtents().y);
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG)
				Logger.info( "Hit box radius for " + building.getName() + " is " + hitBox);
			break;

		}
		return hitBox;
	}

	private static void testItemDamage(AbstractCharacter ac, AbstractWorldObject awo, Item weapon, ItemBase wb) {
		if (ac == null) {
			return;
		}

		//get chance to damage
		int chance = 4500;
		if (wb != null) {
			if (wb.isGlass()) //glass used weighted so fast weapons don't break faster
			{
				chance = 9000 / wb.getWeight();
			}
		}
		//test damaging attackers weapon
		int takeDamage = ThreadLocalRandom.current().nextInt(chance);
		if (takeDamage == 0 && wb != null && (ac.getObjectType().equals(GameObjectType.PlayerCharacter))) {
			ac.getCharItemManager().damageItem(weapon, 1);
		}

		//test damaging targets gear
		takeDamage = ThreadLocalRandom.current().nextInt(chance);
		if (takeDamage == 0 && awo != null && (awo.getObjectType().equals(GameObjectType.PlayerCharacter))) {
			((AbstractCharacter) awo).getCharItemManager().damageRandomArmor(1);
		}
	}

}
