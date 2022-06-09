// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.ai.utilities;

import engine.Enum;
import engine.Enum.*;
import engine.ai.MobileFSM.STATE;
import engine.gameManager.ChatManager;
import engine.gameManager.CombatManager;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.msg.TargetedActionMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

public class CombatUtilities {

	public static boolean inRangeToAttack(Mob agent,AbstractWorldObject target){

		if (Float.isNaN(agent.getLoc().x))
			return false;

		try{
			Vector3fImmutable sl = agent.getLoc();
			Vector3fImmutable tl = target.getLoc();
			
			//add Hitbox's to range.
			float range = agent.getRange();
			range += CombatManager.calcHitBox(target) + CombatManager.calcHitBox(agent);
			//if (target instanceof AbstractCharacter)
			//				if (((AbstractCharacter)target).isMoving())
			//					range+= 5;

			return !(sl.distanceSquared(tl) > sqr(range));
		}catch(Exception e){
			Logger.error( e.toString());
			return false;
		}

	}
	
	public static boolean inRangeToAttack2D(Mob agent,AbstractWorldObject target){

		if (Float.isNaN(agent.getLoc().x))
			return false;

		try{
			Vector3fImmutable sl = agent.getLoc();
			Vector3fImmutable tl = target.getLoc();
			
			//add Hitbox's to range.
			float range = agent.getRange();
			range += CombatManager.calcHitBox(target) + CombatManager.calcHitBox(agent);
			//if (target instanceof AbstractCharacter)
			//				if (((AbstractCharacter)target).isMoving())
			//					range+= 5;

			return !(sl.distanceSquared2D(tl) > sqr(range));
		}catch(Exception e){
			Logger.error( e.toString());
			return false;
		}

	}

	public static void swingIsBlock(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target, MBServerStatics.COMBAT_SEND_BLOCK);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}

	public static void swingIsParry(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target,  MBServerStatics.COMBAT_SEND_PARRY);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}

	public static void swingIsDodge(Mob agent,AbstractWorldObject target, int animation) {

		if (!target.isAlive())
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,animation, target, MBServerStatics.COMBAT_SEND_DODGE);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);
	}

	public static void swingIsDamage(Mob agent,AbstractWorldObject target, float damage, int animation){
		float trueDamage = 0;

		if (!target.isAlive())
			return;

		if (AbstractWorldObject.IsAbstractCharacter(target))
			trueDamage = ((AbstractCharacter) target).modifyHealth(-damage, agent, false);
		else if (target.getObjectType() == GameObjectType.Building)
			trueDamage = ((Building) target).modifyHealth(-damage, agent);

		//Don't send 0 damage kay thanx.

		if (trueDamage == 0)
			return;

		TargetedActionMsg msg = new TargetedActionMsg(agent,target, damage, animation);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

		//check damage shields
		if(AbstractWorldObject.IsAbstractCharacter(target) && target.isAlive() && target.getObjectType() != GameObjectType.Mob)
			CombatManager.handleDamageShields(agent,(AbstractCharacter)target, damage);
	}

	public static boolean canSwing(Mob agent) {
		return (agent.isAlive() && !agent.getBonuses().getBool(ModType.Stunned, SourceType.None));
	}

	public static void swingIsMiss(Mob agent,AbstractWorldObject target, int animation) {

		TargetedActionMsg msg = new TargetedActionMsg(agent,target, 0f, animation);

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			DispatchMessage.dispatchMsgToInterestArea(target, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true,false);
		else
			DispatchMessage.sendToAllInRange(agent,msg);

	}

	public static boolean triggerDefense(Mob agent, AbstractWorldObject target) {
		int defenseScore = 0;
		int attackScore = agent.getAtrHandOne();
		switch (target.getObjectType()) {
		case PlayerCharacter:
			defenseScore = ((AbstractCharacter) target).getDefenseRating();
			break;
		case Mob:

			Mob mob = (Mob)target;
			if (mob.isSiege())
				defenseScore = attackScore;
			break;
		case Building:
			return false;
		}



		int hitChance;
		if (attackScore > defenseScore || defenseScore == 0)
			hitChance = 94;
		else if (attackScore == defenseScore && target.getObjectType() == GameObjectType.Mob)
			hitChance = 10;
		else {
			float dif = attackScore / defenseScore;
			if (dif <= 0.8f)
				hitChance = 4;
			else
				hitChance = ((int)(450 * (dif - 0.8f)) + 4);
			if (target.getObjectType() == GameObjectType.Building)
				hitChance = 100;
		}
		return ThreadLocalRandom.current().nextInt(100) > hitChance;
	}

	public static boolean triggerBlock(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Block");
	}

	public static boolean triggerParry(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Parry");
	}

	public static boolean triggerDodge(Mob agent,AbstractWorldObject ac) {
		return triggerPassive(agent,ac, "Dodge");
	}

	public static boolean triggerPassive(Mob agent,AbstractWorldObject ac, String type) {
		float chance = 0;
		if (AbstractWorldObject.IsAbstractCharacter(ac))
			chance = ((AbstractCharacter)ac).getPassiveChance(type, agent.getLevel(), true);

		if (chance > 75f)
			chance = 75f;
		if (agent.isSiege() && AbstractWorldObject.IsAbstractCharacter(ac))
			chance = 100;

		return ThreadLocalRandom.current().nextInt(100) < chance;
	}


	public static void combatCycle(Mob agent,AbstractWorldObject target, boolean mainHand, ItemBase wb) {

		if (!agent.isAlive() || !target.isAlive()) return;

		if (target.getObjectType() == GameObjectType.PlayerCharacter)
			if (!((PlayerCharacter)target).isActive())
				return;

		int anim = 75;
		float speed = 30f;
		if (mainHand)
			speed = agent.getSpeedHandOne();
		else
			speed = agent.getSpeedHandTwo();
		DamageType dt = DamageType.Crush;
		if (agent.isSiege())
			dt = DamageType.Siege;
		if (wb != null) {
			anim = CombatManager.getSwingAnimation(wb, null,mainHand);
			dt = wb.getDamageType();
		} else if (!mainHand)
			return;
		Resists res = null;
		PlayerBonuses bonus = null;
		switch(target.getObjectType()){
		case Building:
			res = ((Building)target).getResists();
			break;
		case PlayerCharacter:
			res = ((PlayerCharacter)target).getResists();
			bonus = ((PlayerCharacter)target).getBonuses();
			break;
		case Mob:
			Mob mob = (Mob)target;
			res = mob.getResists();
			bonus = ((Mob)target).getBonuses();
			break;
		}

		//must not be immune to all or immune to attack

		if (bonus != null && !bonus.getBool(ModType.NoMod, SourceType.ImmuneToAttack))
			if (res != null &&(res.immuneToAll() || res.immuneToAttacks() || res.immuneTo(dt)))
				return;

		int passiveAnim =  CombatManager.getSwingAnimation(wb, null,mainHand);
		if(canSwing(agent)) {
			if(triggerDefense(agent,target))
				swingIsMiss(agent,target, passiveAnim);
			else if(triggerDodge(agent,target))
				swingIsDodge(agent,target, passiveAnim);
			else if(triggerParry(agent,target))
				swingIsParry(agent,target, passiveAnim);
			else if(triggerBlock(agent,target))
				swingIsBlock(agent,target, passiveAnim);
			else
				//check for a cast here?
			if(agent.isCasting() == true)
			{
				//force stop if they are already casting
				return;
			}
			if(agent.mobPowers.size() > 0)
			{
				//get cast chance
				int random = ThreadLocalRandom.current().nextInt(agent.mobPowers.size() * 3);
				//allow casting of spell
				if(random <= agent.mobPowers.size())
				{
					//cast a spell
					ActionsBase ab = new ActionsBase();

					return;
				}
			}
			//finished with casting check
				swingIsDamage(agent,target, determineDamage(agent,target, mainHand, speed, dt), anim);

			if (agent.getWeaponPower() != null)
				agent.getWeaponPower().attack(target, MBServerStatics.ONE_MINUTE);
		}
		
		if (target.getObjectType().equals(GameObjectType.PlayerCharacter)){
			PlayerCharacter player = (PlayerCharacter)target;
			if (player.getDebug(64)){
				ChatManager.chatSayInfo(player, "Debug Combat: Mob UUID " + agent.getObjectUUID() + " || Building ID  = " + agent.getBuildingID() + " || Floor = " + agent.getInFloorID() + " || Level = " + agent.getInBuilding() );//combat debug
			}
		}

		//SIEGE MONSTERS DO NOT ATTACK GUARDSs
		if (target.getObjectType() == GameObjectType.Mob)
			if (((Mob)target).isSiege())
				return;

		//handle the retaliate

		if (AbstractWorldObject.IsAbstractCharacter(target))
			CombatManager.handleRetaliate((AbstractCharacter)target, agent);

		if (target.getObjectType() == GameObjectType.Mob){
			Mob targetMob = (Mob)target;
			if (targetMob.isSiege())
				return;

			if (System.currentTimeMillis() < targetMob.getTimeStamp("CallForHelp"))
				return;
			CallForHelp(targetMob);
			targetMob.getTimestamps().put("CallForHelp", System.currentTimeMillis() + 60000);
		}


	}

	public static void CallForHelp(Mob aiAgent) {

		Set<Mob> zoneMobs = aiAgent.getParentZone().zoneMobSet;


		AbstractWorldObject target = aiAgent.getCombatTarget();
		if (target == null) {
			return;
		}

		int count = 0;
		for (Mob mob: zoneMobs){
			if (!mob.isAlive())
				continue;
			if (mob.isSiege() || mob.isPet() || !Enum.MobFlagType.AGGRESSIVE.elementOf(mob.getMobBase().getFlags()))
				continue;
			if (count == 5)
				continue;


			if (mob.getCombatTarget() != null)
				continue;

			if (!aiAgent.isPlayerGuard() && mob.isPlayerGuard())
				continue;

			if (aiAgent.isPlayerGuard() && !mob.isPlayerGuard() )
				continue;

			if (target.getObjectType() == GameObjectType.PlayerCharacter){

				if (!MovementUtilities.inRangeToAggro(mob, (PlayerCharacter)target))
					continue;
				count++;

			}else{

				if (count == 5)
					continue;

				if (aiAgent.getLoc().distanceSquared2D(target.getLoc()) > sqr(aiAgent.getAggroRange()))
					continue;

				count++;

			}






			if (mob.getState() == STATE.Awake || mob.getState() == STATE.Patrol){
				mob.setCombatTarget(target);
				mob.setState(STATE.Attack);
			}
		}

	}

	public static float determineDamage(Mob agent,AbstractWorldObject target, boolean mainHand, float speed, DamageType dt) {

		float min = (mainHand) ? agent.getMinDamageHandOne() : agent.getMinDamageHandTwo();
		float max = (mainHand) ? agent.getMaxDamageHandOne() : agent.getMaxDamageHandTwo();;
		if(agent.isSummonedPet() == true)
		{
			min = 40 * (1 + (agent.getLevel()/10));
			max = 60 * (1 + (agent.getLevel()/8));
			//check if we have powers to cast
			if(agent.mobPowers.isEmpty() == false) {
				//check for power usage
				Random random = new Random();
				int value = random.nextInt(0 + (agent.mobPowers.size() + (agent.mobPowers.size() * 5))) + 0;
				if (value <= agent.mobPowers.size())
				{
					//do power
					int powerId = agent.mobPowers.get(value);
					PowersManager.runPowerAction(agent,target,target.getLoc(),new ActionsBase(),40, PowersManager.getPowerByToken(powerId));
				}
				else
				{
					//do mele damage
					float range = max - min;
					float damage = min + ((ThreadLocalRandom.current().nextFloat() * range) + (ThreadLocalRandom.current().nextFloat() * range)) / 2;
					if (AbstractWorldObject.IsAbstractCharacter(target))
						if (((AbstractCharacter) target).isSit())
							damage *= 2.5f; //increase damage if sitting

					if (AbstractWorldObject.IsAbstractCharacter(target))
						return ((AbstractCharacter) target).getResists().getResistedDamage(agent, (AbstractCharacter) target, dt, damage, 0);

					if (target.getObjectType() == GameObjectType.Building) {
						Building building = (Building) target;
						Resists resists = building.getResists();
						return damage * (1 - (resists.getResist(dt, 0) / 100));
					}
				}
			}

		}
		float range = max - min;
		float damage = min + ((ThreadLocalRandom.current().nextFloat()*range)+(ThreadLocalRandom.current().nextFloat()*range))/2;
//DAMAGE FORMULA FOR PET
		if (AbstractWorldObject.IsAbstractCharacter(target))
			if (((AbstractCharacter)target).isSit())
				damage *= 2.5f; //increase damage if sitting

		if (AbstractWorldObject.IsAbstractCharacter(target))
			return ((AbstractCharacter)target).getResists().getResistedDamage(agent,(AbstractCharacter)target, dt, damage, 0);

		if (target.getObjectType() == GameObjectType.Building){
			Building building = (Building)target;
			Resists resists = building.getResists();
			return damage * (1 - (resists.getResist(dt, 0) / 100));
		}

		return damage;

	}
	
	public static boolean RunAIRandom(){
		int random = ThreadLocalRandom.current().nextInt(4);
		
		if (random == 0)
			return true;
		
		return false;
	}
}
