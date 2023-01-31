// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum.*;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.WorldGrid;
import engine.ai.StaticMobActions;
import engine.job.AbstractJob;
import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.*;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import engine.powers.*;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.powers.poweractions.AbstractPowerAction;
import engine.powers.poweractions.TrackPowerAction;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

public enum PowersManager {

	POWERMANAGER;

	public static HashMap<String, PowersBase> powersBaseByIDString = new HashMap<>();
	public static HashMap<Integer, PowersBase> powersBaseByToken = new HashMap<>();
	public static HashMap<String, EffectsBase> effectsBaseByIDString = new HashMap<>();
	public static HashMap<Integer, EffectsBase> effectsBaseByToken = new HashMap<>();
	public static HashMap<String, AbstractPowerAction> powerActionsByIDString = new HashMap<>();
	public static HashMap<Integer, AbstractPowerAction> powerActionsByID = new HashMap<>();
	public static HashMap<String, Integer> ActionTokenByIDString = new HashMap<>();
	public static HashMap<Integer, AbstractEffectModifier> modifiersByToken = new HashMap<>();
	public static HashMap<String,Integer> AnimationOverrides = new HashMap<>();
	private static JobScheduler js;

	public static void initPowersManager(boolean fullPowersLoad) {

		if (fullPowersLoad)
			PowersManager.InitializePowers();
		else
			PowersManager.InitializeLoginPowers();

		PowersManager.js = JobScheduler.getInstance();

	}

	private PowersManager() {

	}

	public static PowersBase getPowerByToken(int token) {
		return PowersManager.powersBaseByToken.get(token);
	}

	public static PowersBase getPowerByIDString(String IDString) {
		return PowersManager.powersBaseByIDString.get(IDString);
	}

	public static EffectsBase getEffectByIDString(String IDString) {
		return PowersManager.effectsBaseByIDString.get(IDString);
	}

	public static AbstractPowerAction getPowerActionByID(Integer id) {
		return PowersManager.powerActionsByID.get(id);
	}

	public static AbstractPowerAction getPowerActionByIDString(String IDString) {
		return PowersManager.powerActionsByIDString.get(IDString);
	}

	public static EffectsBase getEffectByToken(int token) {
		return PowersManager.effectsBaseByToken.get(token);
	}

	// This pre-loads only PowersBase for login
	public static void InitializeLoginPowers() {

		// get all PowersBase
		ArrayList<PowersBase> pbList = PowersBase.getAllPowersBase();

		for (PowersBase pb : pbList) {
			if (pb.getToken() != 0)
				PowersManager.powersBaseByToken.put(pb.getToken(), pb);
		}
	}

	// This pre-loads all powers and effects
	public static void InitializePowers() {

		// Add EffectsBase
		ArrayList<EffectsBase> ebList = EffectsBase.getAllEffectsBase();

		for (EffectsBase eb : ebList) {
			PowersManager.effectsBaseByToken.put(eb.getToken(), eb);
			PowersManager.effectsBaseByIDString.put(eb.getIDString(), eb);

		}
		
		// Add Fail Conditions
		EffectsBase.getFailConditions(PowersManager.effectsBaseByIDString);

		// Add Modifiers to Effects
		AbstractEffectModifier.getAllEffectModifiers();

		// Add Source Types to Effects
		PowersManager.addAllSourceTypes();
		PowersManager.addAllAnimationOverrides();

		// Add PowerActions
		AbstractPowerAction.getAllPowerActions(PowersManager.powerActionsByIDString, PowersManager.powerActionsByID, PowersManager.effectsBaseByIDString);

		// Load valid Item Flags
	//	AbstractPowerAction.loadValidItemFlags(PowersManager.powerActionsByIDString);

		// get all PowersBase
		ArrayList<PowersBase> pbList = PowersBase.getAllPowersBase();
		for (PowersBase pb : pbList) {
			if (pb.getToken() != 0) {
				PowersManager.powersBaseByIDString.put(pb.getIDString(), pb);
				PowersManager.powersBaseByToken.put(pb.getToken(), pb);
			}
		}
		
		// Add Power Prereqs
		PowerPrereq.getAllPowerPrereqs(PowersManager.powersBaseByIDString);
		// Add Fail Conditions
		PowersBase.getFailConditions(PowersManager.powersBaseByIDString);
		// Add Actions Base
		ActionsBase.getActionsBase(PowersManager.powersBaseByIDString,
				PowersManager.powerActionsByIDString);
		
	}

	private static void addAllSourceTypes() {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_sourcetype");
			ResultSet rs = ps.executeQuery();
			String IDString, source;
			while (rs.next()) {
				IDString = rs.getString("IDString");
				int token = DbManager.hasher.SBStringHash(IDString);
				
				
				source = rs.getString("source").replace("-", "").trim();
				EffectSourceType effectSourceType = EffectSourceType.GetEffectSourceType(source);
				
				if (EffectsBase.effectSourceTypeMap.containsKey(token) == false)
					EffectsBase.effectSourceTypeMap.put(token, new HashSet<>());
				
				EffectsBase.effectSourceTypeMap.get(token).add(effectSourceType);
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e);
		} finally {
			ps.release();
		}
	}

	private static void addAllAnimationOverrides() {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_animation_override");
			ResultSet rs = ps.executeQuery();
			String IDString;
			int animation;
			while (rs.next()) {
				IDString = rs.getString("IDString");

				EffectsBase eb = PowersManager.getEffectByIDString(IDString);
				if (eb != null)
					IDString = eb.getIDString();

				animation = rs.getInt("animation");
				PowersManager.AnimationOverrides.put(IDString, animation);

			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e);
		} finally {
			ps.release();
		}
	}

	public static EffectsBase setEffectToken(int ID, int token) {
		for (EffectsBase eb : PowersManager.effectsBaseByIDString.values()) {
			if (eb.getUUID() == ID) {
				eb.setToken(token);
				return eb;
			}
		}
		return null;
	}

	public static void usePower(final PerformActionMsg msg, ClientConnection origin,
			boolean sendCastToSelf) {

		if (usePowerA(msg, origin, sendCastToSelf)) {
			// Cast failed for some reason, reset timer

			RecyclePowerMsg recyclePowerMsg = new RecyclePowerMsg(msg.getPowerUsedID());
			Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), recyclePowerMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

			// Send Fail to cast message
			PlayerCharacter pc = SessionManager
					.getPlayerCharacter(origin);

			if (pc != null) {
				sendPowerMsg(pc, 2, msg);
				if (pc.isCasting()){
					pc.update();
				}
				
				pc.setIsCasting(false);
			}

		}
	}

	public static void useMobPower(Mob caster, AbstractCharacter target, PowersBase pb, int rank) {

		PerformActionMsg msg = createPowerMsg(pb, rank, caster, target);
		msg.setUnknown04(1);

		if (useMobPowerA(msg, caster)) {
			//sendMobPowerMsg(caster,2,msg); //Lol wtf was i thinking sending msg's to mobs... ZZZZ
		}
	}

	public static boolean usePowerA(final PerformActionMsg msg, ClientConnection origin,
			boolean sendCastToSelf) {
		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(
				origin);
		if (playerCharacter == null)
			return false;

		boolean CSRCast = false;


		if (MBServerStatics.POWERS_DEBUG) {
			ChatManager.chatSayInfo(
					playerCharacter,
					"Using Power: " + Integer.toHexString(msg.getPowerUsedID())
					+ " (" + msg.getPowerUsedID() + ')');
			Logger.info( "Using Power: "
					+ Integer.toHexString(msg.getPowerUsedID()) + " ("
					+ msg.getPowerUsedID() + ')');
		}

		//Sending recycle message to player if died while casting.
		if (!playerCharacter.isAlive() && msg.getPowerUsedID() != 428589216) { //succor

			RecyclePowerMsg recyclePowerMsg = new RecyclePowerMsg(msg.getPowerUsedID());
			Dispatch dispatch = Dispatch.borrow(playerCharacter, recyclePowerMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

			return false;
		}
		
		


		// if (!pc.getPowers().contains(msg.getPowerUsedID())) {
		// sendPowerMsg(pc, 10, msg);
		// return false;
		// }
		// verify recycle timer is not active for power, skip for a CSR
		if (playerCharacter.getRecycleTimers().containsKey(msg.getPowerUsedID()) && !playerCharacter.isCSR()) {
			// ChatManager.chatSayInfo(pc, "Recycle timer not finished!");

			Logger.warn("usePowerA(): Cheat attempted? '" + msg.getPowerUsedID() + "' recycle timer not finished " + playerCharacter.getName());
			return false;
		}

		// get power
		PowersBase pb = PowersManager.powersBaseByToken.get(msg.getPowerUsedID());
		if (pb == null) {
			ChatManager.chatSayInfo(playerCharacter,
					"This power is not implemented yet.");

			// Logger.error("usePowerA(): Power '" +
			// msg.getPowerUsedID()
			// + "' was not found on powersBaseByToken map.");
			return true;
			// return false;
		}

		if (playerCharacter.getLastPower() != null)
			return true;
		
		//Check if Power Target is allowed to cast.


		// Check powers for normal users
		if (playerCharacter.getPowers() == null || !playerCharacter.getPowers().containsKey(msg.getPowerUsedID()))
			if (!playerCharacter.isCSR()) {
				if (!MBServerStatics.POWERS_DEBUG) {
					//  ChatManager.chatSayInfo(pc, "You may not cast that spell!");

					 Logger.info("usePowerA(): Cheat attempted? '" + msg.getPowerUsedID() + "' was not associated with " + playerCharacter.getName());
					return true;
				}
			} else
				CSRCast = true;

		// get numTrains for power
		int trains = msg.getNumTrains();

		// can't go over the max trains for the power, unless CSR
		if (trains > pb.getMaxTrains() && !playerCharacter.isCSR()) {
			trains = pb.getMaxTrains();
			msg.setNumTrains(trains);
		}

		// can't go over total trains by player
		if (playerCharacter.getPowers() != null && playerCharacter.getPowers().containsKey(msg.getPowerUsedID())) {
			CharacterPower cp = playerCharacter.getPowers().get(msg.getPowerUsedID());
			if (cp != null) {
				int tot = cp.getTotalTrains();
				if (tot == 0 && !playerCharacter.isCSR())
					return false;
				if (trains != tot && !playerCharacter.isCSR()) {
					trains = tot;
					msg.setNumTrains(trains);
				}
			}
		}

		// get recycle time in ms
		int time = pb.getRecycleTime(trains);

		// verify player is in correct mode (combat/nonCombat)
		if (playerCharacter.isCombat()) {
			if (!pb.allowedInCombat())
				// ChatManager.chatPowerError(pc,
				// "This power is not allowed in combat mode.");
				return true;
		} else if (!pb.allowedOutOfCombat())
			// ChatManager.chatPowerError(pc,
			// "You must be in combat mode to use this power.");
			return true;

		// verify player is not stunned or prohibited from casting
		PlayerBonuses bonus = playerCharacter.getBonuses();
SourceType sourceType = SourceType.GetSourceType(pb.getCategory());
		if (bonus != null && (bonus.getBool(ModType.Stunned,SourceType.None) || bonus.getBool(ModType.CannotCast, SourceType.None) || bonus.getBool(ModType.BlockedPowerType, sourceType)))
			return true;

		// if moving make sure spell valid for movement
		Vector3fImmutable endLoc = playerCharacter.getEndLoc();

		
			if (!pb.canCastWhileMoving())
				if (playerCharacter.isMoving()){

				// if movement left is less then 1 seconds worth then let cast
				// go through.
				float distanceLeftSquared = endLoc.distanceSquared2D(playerCharacter.getLoc());

				if (distanceLeftSquared > sqr(playerCharacter.getSpeed()))
					return true;
			}
		// if flying, make sure spell valid for flying.
		// if (pc.getAltitude() > 0)
		// if (!pb.canCastWhileFlying())
		// return true;

		int targetLiveCounter = -1;

		// get target based on targetType;
		if (pb.targetFromLastTarget() || pb.targetPet()) // use msg's target
			if (pb.isAOE()) {
				if (!pb.usePointBlank()) {
					AbstractWorldObject target = getTarget(msg);
					
					
					
					if (target != null && target.getObjectType() == GameObjectType.Building && !pb.targetBuilding()) {
						PowersManager.sendPowerMsg(playerCharacter, 9, new PerformActionMsg(msg));
						return true;
					}

					if (target == null) {
						if (playerCharacter.getLoc().distanceSquared2D(msg.getTargetLoc()) > sqr(pb
								.getRange()))
							return true;
					} else if (verifyInvalidRange(playerCharacter, target, pb.getRange()))
						// pc.getLoc().distance(target.getLoc()) >
						// pb.getRange())
						return true;


				}
			} else {
				// get target
				AbstractWorldObject target = getTarget(msg);

				if (target == null)
					return true;
				
				if (!target.isAlive() && target.getObjectType().equals(GameObjectType.Building) == false  && msg.getPowerUsedID() != 428589216)
					return true;

				float range = pb.getRange();
				// verify target is in range
			

				if (verifyInvalidRange(playerCharacter, target, range))
					// (pc.getLoc().distance(target.getLoc()) > pb.getRange()) {
					// TODO send message that target is out of range
					return true;

				// verify target is valid type
				if (!validateTarget(target, playerCharacter, pb))
					return true;


				if (AbstractWorldObject.IsAbstractCharacter(target))
					targetLiveCounter = ((AbstractCharacter) target).getLiveCounter();
			}

		// verify regular player can cast spell, otherwise authenticate
		if (!pb.regularPlayerCanCast()) {
            Account a = SessionManager.getAccount(playerCharacter);
            if (a.status.equals(AccountStatus.ADMIN) == false) {
                Logger.warn("Player '" + playerCharacter.getName()
                        + "' is attempting to cast a spell outside of their own access level.");
                return true;
            }
        }

		// verify player has proper effects applied to use power
		if (pb.getEffectPrereqs().size() > 0 && playerCharacter.getEffects() != null) {

			boolean passed = false;
			for (PowerPrereq pp : pb.getEffectPrereqs()) {

				EffectsBase eb = PowersManager.getEffectByIDString(pp.getEffect());

				if (playerCharacter.containsEffect(eb.getToken())) {
					passed = true;
					break;
				}
			}

			if (!passed)
				return true;
		}

		//verify player has proper equipment to use power
		if (pb.getEquipPrereqs().size() > 0) {

			boolean passed = false;

			for (PowerPrereq pp : pb.getEquipPrereqs()) {

				int slot = pp.mainHand() ? MBServerStatics.SLOT_MAINHAND : MBServerStatics.SLOT_OFFHAND;

				if (playerCharacter.validEquip(slot, pp.getMessage())) {
					passed = true; //should have item in slot
					break;
				} else if (!pp.isRequired()) {
					passed = true; //should not have item in slot
					break;
				}
			}
			if (!passed)
				return true;
		}

		// TODO if target immune to powers, cancel unless aoe
		// Also make sure No.ImmuneToPowers is false for target
		// if there's a different power waiting to finish, stop here
	

		//get power cost and calculate any power cost modifiers
		float cost = pb.getCost(trains);
		
		if (playerCharacter.isCSR())
			cost = 0;

		if (bonus != null)
			cost *= (1 + bonus.getFloatPercentAll(ModType.PowerCost, SourceType.None));
		
		if (playerCharacter.getAltitude() > 0)
			cost *= 1.5f;

		// Verify player can afford to use power
		//CCR toons dont use cost
		
		if (!playerCharacter.isCSR()){
			if (cost > 0)
				if ((playerCharacter.getObjectTypeMask() & MBServerStatics.MASK_UNDEAD) != 0)
					if (playerCharacter.getHealth() <= cost)
						return true;
					else {
						playerCharacter.modifyHealth(-cost, playerCharacter, true);
						ModifyHealthMsg mhm = new ModifyHealthMsg(playerCharacter, playerCharacter, -cost,
								0f, 0f, 0, null,
								9999, 0);
						mhm.setOmitFromChat(1);
						DispatchMessage.dispatchMsgToInterestArea(playerCharacter, mhm, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
					}
				else if (pb.useMana())
					if (playerCharacter.getMana() < cost)
						return true;
					else
						playerCharacter.modifyMana(-cost, playerCharacter, true);
				else if (pb.useStamina())
					if (playerCharacter.getStamina() < cost)
						return true;
					else
						playerCharacter.modifyStamina(-cost, playerCharacter, true);
				else if (playerCharacter.getHealth() <= cost)
					return true;
				else
					playerCharacter.modifyHealth(-cost, playerCharacter, true);
		}
		

		// Validity checks passed, move on to casting spell
		//get caster's live counter
		int casterLiveCounter = playerCharacter.getLiveCounter();

		// run recycle job for when cast is available again, don't bother adding the timer for CSRs
		if (time > 0) {
			FinishRecycleTimeJob frtj = new FinishRecycleTimeJob(playerCharacter, msg);
			playerCharacter.getRecycleTimers().put(msg.getPowerUsedID(), js.scheduleJob(frtj, time));
		} else {
			// else send recycle message to unlock power
			RecyclePowerMsg recyclePowerMsg = new RecyclePowerMsg(msg.getPowerUsedID());
			Dispatch dispatch = Dispatch.borrow(playerCharacter, recyclePowerMsg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		}

		//what the fuck?
//		// Send cast to other players
//		if ((playerCharacter.getObjectTypeMask() & MBServerStatics.MASK_UNDEAD) != 0)
//			msg.setUnknown04(2); // Vampire Race, use health?
//		else
//			msg.setUnknown04(1); // Regular Race, use mana?
		int tr = msg.getNumTrains();
		DispatchMessage.dispatchMsgToInterestArea(playerCharacter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, sendCastToSelf, false);

		//Make new msg..
		PerformActionMsg copyMsg = new PerformActionMsg(msg);
		copyMsg.setNumTrains(tr);

		// make person casting stand up if spell (unless they're casting a chant which does not make them stand up)
		if (pb.isSpell() && !pb.isChant() && playerCharacter.isSit()) {
			playerCharacter.update();
			playerCharacter.setSit(false);
			UpdateStateMsg updateStateMsg = new UpdateStateMsg(playerCharacter);
			DispatchMessage.dispatchMsgToInterestArea(playerCharacter, updateStateMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);

		}

		// update cast (use skill) fail condition
		playerCharacter.cancelOnCast();

		// update castSpell (use spell) fail condition if spell
		if (pb.isSpell())
			playerCharacter.cancelOnSpell();

		// get cast time in ms.
		time = pb.getCastTime(trains);

		// set player is casting for regens

		
			if (time > 100){
				playerCharacter.update();
				playerCharacter.setIsCasting(true);
			}
		
	
		playerCharacter.setLastMovementState(playerCharacter.getMovementState());
		// update used power timer
		playerCharacter.setLastUsedPowerTime();

		// run timer job to end cast
		if (time < 1) // run immediately
			finishUsePower(copyMsg, playerCharacter, casterLiveCounter, targetLiveCounter);
		else {
			UsePowerJob upj = new UsePowerJob(playerCharacter, copyMsg, copyMsg.getPowerUsedID(), pb, casterLiveCounter, targetLiveCounter);
			JobContainer jc = js.scheduleJob(upj, time);

			// make lastPower
			playerCharacter.setLastPower(jc);	
		}

		if (CSRCast)
			Logger.info("CSR " + playerCharacter.getName() + " cast power " + msg.getPowerUsedID() + '.');

		return false;
	}

	public static void testPowers(ByteBufferWriter writer) {
		writer.putInt(powersBaseByToken.size());
		for (int token : powersBaseByToken.keySet()) {
			writer.putInt(token);
			writer.putInt(40);
		}
	}

	public static boolean useMobPowerA(PerformActionMsg msg, Mob caster) {
		if (caster == null)
			return false;


		if (!caster.isAlive() && msg.getPowerUsedID() != 428589216) //succor
			return false;

		// get power
		PowersBase pb = PowersManager.powersBaseByToken.get(msg.getPowerUsedID());
		if (pb == null)
			return true;

		// Check powers for normal users
		// get numTrains for power
		int trains = msg.getNumTrains();

		// can't go over the max trains for the power, unless CSR
		// can't go over total trains by player
		// get recycle time in ms
		int time = pb.getRecycleTime(trains);

		// verify player is in correct mode (combat/nonCombat)
		// verify player is not stunned or prohibited from casting
		PlayerBonuses bonus = caster.getBonuses();
		SourceType sourceType = SourceType.GetSourceType(pb.getCategory());
		if (bonus != null && (bonus.getBool(ModType.Stunned, SourceType.None) || bonus.getBool(ModType.CannotCast, SourceType.None) || bonus.getBool(ModType.BlockedPowerType, sourceType)))
			return true;

		// if moving make sure spell valid for movement
		// if flying, make sure spell valid for flying.
		// if (pc.getAltitude() > 0)
		// if (!pb.canCastWhileFlying())
		// return true;
		int targetLiveCounter = -1;

		// get target based on targetType;
		if (pb.targetFromLastTarget() || pb.targetPet()) // use msg's target
			if (pb.isAOE()) {
				if (!pb.usePointBlank()) {
					AbstractWorldObject target = getTarget(msg);


					if (target == null) {

						if (caster.getLoc().distanceSquared2D(msg.getTargetLoc()) > sqr(pb
								.getRange()))
							return true;
					} else if (verifyInvalidRange(caster, target, pb.getRange()))
						// pc.getLoc().distance(target.getLoc()) >
						// pb.getRange())
						return true;
				}
			} else {
				// get target
				AbstractWorldObject target = getTarget(msg);

				if (target == null)
					return true;

				// verify target is in range
				if (verifyInvalidRange(caster, target, pb.getRange()))
					// (pc.getLoc().distance(target.getLoc()) > pb.getRange()) {
					// TODO send message that target is out of range
					return true;

				// verify target is valid type
				if (AbstractWorldObject.IsAbstractCharacter(target))
					targetLiveCounter = ((AbstractCharacter) target).getLiveCounter();
			}

		// TODO if target immune to powers, cancel unless aoe
		// Also make sure No.ImmuneToPowers is false for target
		// if there's a different power waiting to finish, stop here
		if (caster.lastMobPowerToken != 0)
			return true;

		//get power cost and calculate any power cost modifiers
		// Validity checks passed, move on to casting spell
		//get caster's live counter
		int casterLiveCounter = caster.getLiveCounter();

		// run recycle job for when cast is available again, don't bother adding the timer for CSRs
		// Send cast to other players
		if (caster.getObjectTypeMask() == MBServerStatics.MASK_UNDEAD)
			msg.setUnknown05(0); // Regular Race, use mana?
		else
			msg.setUnknown05(0);

		int tr = msg.getNumTrains();

		msg.setNumTrains(9999);

		DispatchMessage.sendToAllInRange(caster, msg);
		DispatchMessage.sendToAllInRange(caster, msg);

		msg.setNumTrains(tr);

		// make person casting stand up if spell (unless they're casting a chant which does not make them stand up)
		// update cast (use skill) fail condition
		caster.cancelOnCast();

		// update castSpell (use spell) fail condition if spell
		if (pb.isSpell())
			caster.cancelOnSpell();

		// get cast time in ms.
		time = pb.getCastTime(trains);

		// set player is casting for regens
		caster.setIsCasting(true);
		caster.lastMobPowerToken = pb.getToken();

		// run timer job to end cast
		if (time < 1 || pb.getToken() == -1994153779){
			// run immediately
			finishUseMobPower(msg, caster, casterLiveCounter, targetLiveCounter);
			caster.lastMobPowerToken = 0;
		}
			
		else {
			caster.lastMobPowerToken = pb.getToken();
			caster.setTimeStamp("FinishCast", System.currentTimeMillis() + (pb.getCastTime(trains)));
		}
		//			finishUseMobPower(msg, caster, casterLiveCounter, targetLiveCounter); //			UseMobPowerJob upj = new UseMobPowerJob(caster, msg, msg.getPowerUsedID(), pb, casterLiveCounter, targetLiveCounter);
		//			 JobContainer jc = js.scheduleJob(upj, time);
		//			// make lastPower


		return false;
	}

	// called when a spell finishes casting. perform actions
	public static void finishUsePower(final PerformActionMsg msg, PlayerCharacter playerCharacter, int casterLiveCounter, int targetLiveCounter) {

		PerformActionMsg performActionMsg;
		Dispatch dispatch;

		if (playerCharacter == null || msg == null)
			return;

		if (playerCharacter.isCasting()){
			playerCharacter.update();
			playerCharacter.updateStamRegen(-100);
		}
		
		playerCharacter.resetLastSetLocUpdate();
		playerCharacter.setIsCasting(false);
		// can't go over total trains by player


		if (!playerCharacter.isAlive() || playerCharacter.getLiveCounter() != casterLiveCounter) {
			playerCharacter.clearLastPower();
			finishRecycleTime(msg.getPowerUsedID(), playerCharacter, true);


			// Let's do good OO.  Clone message don't modify it.

			performActionMsg = new PerformActionMsg(msg);
			performActionMsg.setNumTrains(9999);
			performActionMsg.setUnknown04(2);

			dispatch = Dispatch.borrow(playerCharacter, performActionMsg);
			DispatchMessage.dispatchMsgToInterestArea(playerCharacter, performActionMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
			return;
		}

		// set player is not casting for regens


		// clear power.
		playerCharacter.clearLastPower();

		PowersBase pb = PowersManager.powersBaseByToken.get(msg.getPowerUsedID());

		if (pb == null) {
			Logger.error(
					"finishUsePower(): Power '" + msg.getPowerUsedID()
					+ "' was not found on powersBaseByToken map.");
			return;
		}

		int trains = msg.getNumTrains();

		// update used power timer
		playerCharacter.setLastUsedPowerTime();

		// verify player is not stunned or power type is blocked
		PlayerBonuses bonus = playerCharacter.getBonuses();

		if (bonus != null) {
			if (bonus.getBool(ModType.Stunned, SourceType.None))
				return;
			
			SourceType sourceType = SourceType.GetSourceType(pb.getCategory());
			if (bonus.getBool(ModType.BlockedPowerType, sourceType)) {
				finishRecycleTime(msg.getPowerUsedID(), playerCharacter, true);
				return;
			}
		}

		// get target loc
		Vector3fImmutable targetLoc = msg.getTargetLoc();


		if (pb.targetFromLastTarget() || pb.targetPet()) // use msg's target
			if (pb.isAOE()) {
				if (!pb.usePointBlank()) {
					AbstractWorldObject mainTarget = getTarget(msg);

					float speedRange = 0;
					if (AbstractWorldObject.IsAbstractCharacter(mainTarget)) {
						speedRange = ((AbstractCharacter) mainTarget).getSpeed() * (pb.getCastTime(trains) * .001f);
					}

					if (mainTarget != null && mainTarget.getObjectType() == GameObjectType.Building && !pb.targetBuilding()) {
						PowersManager.sendPowerMsg(playerCharacter, 8, new PerformActionMsg(msg));
						return;
					}

					if (mainTarget == null) {

						if (playerCharacter.getLoc().distanceSquared2D(msg.getTargetLoc()) > sqr(pb
								.getRange())) {
							sendPowerMsg(playerCharacter, 8, msg);
							return;
						}
					} else if (verifyInvalidRange(playerCharacter, mainTarget, speedRange + pb.getRange())) {

						sendPowerMsg(playerCharacter, 8, msg);
						return;
					}
				}
			} else {

				// get target
				AbstractWorldObject mainTarget = getTarget(msg);

				if (mainTarget == null)
					return;

				float speedRange = 0;
				if (AbstractWorldObject.IsAbstractCharacter(mainTarget)) {
					speedRange = ((AbstractCharacter) mainTarget).getSpeed() * (pb.getCastTime(trains) * .001f);
				}
				float range = pb.getRange() + speedRange;
				


				if (verifyInvalidRange(playerCharacter, mainTarget, range)) {

					sendPowerMsg(playerCharacter, 8, msg);
					return;
				}
				// (pc.getLoc().distance(target.getLoc()) > pb.getRange()) {
				// TODO send message that target is out of range


			}

		if (targetLoc.x == 0f || targetLoc.z == 0f) {
			AbstractWorldObject tar = getTarget(msg);
			if (tar != null)
				targetLoc = tar.getLoc();
		}


		// get list of targets
		HashSet<AbstractWorldObject> allTargets = getAllTargets(
				getTarget(msg), msg.getTargetLoc(), playerCharacter, pb);

		// no targets found. send error message

		if (allTargets.size() == 0) {
			sendPowerMsg(playerCharacter, 9, msg);
			return;
		}

		playerCharacter.setHateValue(pb.getHateValue(trains));

		//Send Cast Message.
//		PerformActionMsg castMsg = new PerformActionMsg(msg);
//		castMsg.setNumTrains(9999);
//		castMsg.setUnknown04(3);
//		DispatchMessage.dispatchMsgToInterestArea(playerCharacter, castMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
//		
		boolean msgCasted = false;
		
		for (AbstractWorldObject target : allTargets) {

			if (target == null)
				continue;

			//Hacky Pyschic healing cross heal

			//make sure target hasn't respawned since we began casting
			//skip this if liveCounter = -1 (from aoe)

			if (targetLiveCounter != -1)
				if (AbstractWorldObject.IsAbstractCharacter(target))
					if (targetLiveCounter != ((AbstractCharacter) target).getLiveCounter())
						continue;

			if (!target.isAlive() && target.getObjectType() != GameObjectType.Building && pb.getToken() != 428589216 && pb.getToken() != 429425915)
				continue;

			//make sure mob is awake to respond.
			//if (target instanceof AbstractIntelligenceAgent)
			//((AbstractIntelligenceAgent)target).enableIntelligence();
			// If Hit roll required, test hit

			boolean miss = false;
			if (pb.requiresHitRoll() && !pb.isWeaponPower() && testAttack(playerCharacter, target, pb, msg)) {
				miss = true;
				//aggro mob even on a miss
				if (target.getObjectType() == GameObjectType.Mob) {
					Mob mobTarget = (Mob) target;
					if (pb.isHarmful())
						StaticMobActions.handleDirectAggro(mobTarget,playerCharacter);
				}
				continue;
			}
			if (target.getObjectType() == GameObjectType.Mob) {
				Mob mobTarget = (Mob) target;
				if (pb.isHarmful())
					StaticMobActions.handleDirectAggro(mobTarget,playerCharacter);
			}
			//Power is aiding a target, handle aggro if combat target is a Mob.
			if (!pb.isHarmful() && target.getObjectType() == GameObjectType.PlayerCharacter) {
				PlayerCharacter pcTarget = (PlayerCharacter) target;
				if (!pb.isHarmful())
					StaticMobActions.HandleAssistedAggro(playerCharacter, pcTarget);
			}

			// update target of used power timer

			if (pb.isHarmful())
				if (target.getObjectType().equals(GameObjectType.PlayerCharacter) && target.getObjectUUID() != playerCharacter.getObjectUUID()) {

					((PlayerCharacter) target).setLastTargetOfUsedPowerTime();
					((PlayerCharacter) target).setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
					playerCharacter.setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
				}


			//Player didn't miss power, send finish cast. Miss cast already sent.
			

			// finally Apply actions
			for (ActionsBase ab : pb.getActions()) {
				// get numTrains for power, skip action if invalid

				if (trains < ab.getMinTrains() || trains > ab.getMaxTrains())
					continue;
				// If something blocks the action, then stop

				if (ab.blocked(target, pb, trains)) {

					PowersManager.sendEffectMsg(playerCharacter, 5, ab, pb);
					continue;
				}

				// TODO handle overwrite stack order here
				String stackType = ab.getStackType();
				stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(ab.getUUID()) : stackType;
				//				if (!stackType.equals("IgnoreStack")) {
				if (target.getEffects().containsKey(stackType)) {
					// remove any existing power that overrides
					Effect ef = target.getEffects().get(stackType);
					AbstractEffectJob effect = null;
					if (ef != null) {
						JobContainer jc = ef.getJobContainer();
						if (jc != null)
							effect = (AbstractEffectJob) jc.getJob();
					}
					ActionsBase overwrite = effect.getAction();
					
					if (overwrite == null) {
						Logger.error("NULL ACTION FOR POWER " + effect.getPowerToken());
						continue;
					}
					
						if (ab.getStackOrder() < overwrite.getStackOrder())
							continue; // not high enough to overwrite
						else if (ab.getStackOrder() > overwrite.getStackOrder()) {
							effect.setNoOverwrite(true);
							removeEffect(target, overwrite, true, false);
						} else if (ab.getStackOrder() == overwrite.getStackOrder())
							if (ab.greaterThanEqual()
									&& (trains >= effect.getTrains())) {
								effect.setNoOverwrite(true);
								removeEffect(target, overwrite, true, false);
							}
					
					 else if (ab.always())
							removeEffect(target, overwrite, true, false);
						else if (ab.greaterThan()
								&& (trains > effect.getTrains())) {
							effect.setNoOverwrite(true);
							removeEffect(target, overwrite, true, false);
						} else if (ab.greaterThan() && pb.getToken() == effect.getPowerToken())
							removeEffect(target, overwrite, true, false);

						else
							continue; // trains not high enough to overwrite
						
					}
					
				//				}
				

				runPowerAction(playerCharacter, target, targetLoc, ab, trains, pb);
				if (!miss && !msgCasted){
					PerformActionMsg castMsg = new PerformActionMsg(msg);
					castMsg.setNumTrains(9999);
					castMsg.setUnknown04(2);
					DispatchMessage.dispatchMsgToInterestArea(playerCharacter, castMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
					msgCasted = true;
				}
			}
		}
		
		if ( !msgCasted){
			PerformActionMsg castMsg = new PerformActionMsg(msg);
			castMsg.setNumTrains(9999);
			castMsg.setUnknown04(2);
			DispatchMessage.dispatchMsgToInterestArea(playerCharacter, castMsg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
			msgCasted = true;
		}

		//Handle chant
		if (pb != null && pb.isChant())
			for (ActionsBase ab : pb.getActions()) {
				AbstractPowerAction pa = ab.getPowerAction();
				if (pa != null)
					if (pb.getToken() != 428950414 && pb.getToken() != 428884878)
						pa.handleChant(playerCharacter, playerCharacter, targetLoc, trains, ab, pb);
					else if (PowersManager.getTarget(msg) != null && PowersManager.getTarget(msg).isAlive())
						pa.handleChant(playerCharacter, PowersManager.getTarget(msg), targetLoc, trains, ab, pb);
					else
						pa.handleChant(playerCharacter, null, targetLoc, trains, ab, pb);
			}
		
		

		
		
		//DispatchMessage.dispatchMsgToInterestArea(playerCharacter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);


	}

	public static void finishUseMobPower(PerformActionMsg msg, Mob caster, int casterLiveCounter, int targetLiveCounter) {

		if (caster == null || msg == null)
			return;

		if (!caster.isAlive() || caster.getLiveCounter() != casterLiveCounter)
			return;

		// set player is not casting for regens
		caster.setIsCasting(false);

		
		PowersBase pb = PowersManager.powersBaseByToken.get(msg.getPowerUsedID());
		// clear power.
				caster.lastMobPowerToken = 0;

		if (pb == null) {
			Logger.error(
					"finishUsePower(): Power '" + msg.getPowerUsedID()
					+ "' was not found on powersBaseByToken map.");
			return;
		}
		
		int trains = msg.getNumTrains();

		// update used power timer
		// verify player is not stunned or power type is blocked
		PlayerBonuses bonus = caster.getBonuses();
		if (bonus != null) {
			if (bonus.getBool(ModType.Stunned,SourceType.None))
				return;
			SourceType sourceType = SourceType.GetSourceType(pb.getCategory());
			if (bonus.getBool(ModType.BlockedPowerType, sourceType))
				return;
		}

		msg.setNumTrains(9999);
		msg.setUnknown04(2);
		DispatchMessage.sendToAllInRange(caster, msg);

		// get target loc
		Vector3fImmutable targetLoc = msg.getTargetLoc();
		if (targetLoc.x == 0f || targetLoc.z == 0f) {
			AbstractWorldObject tar = getTarget(msg);
			if (tar != null)
				targetLoc = tar.getLoc();
		}

		// get list of targets
		HashSet<AbstractWorldObject> allTargets = getAllTargets(
				getTarget(msg), msg.getTargetLoc(), caster, pb);
		for (AbstractWorldObject target : allTargets) {

			if (target == null)
				continue;


			//make sure target hasn't respawned since we began casting
			//skip this if liveCounter = -1 (from aoe)
			if (targetLiveCounter != -1)
				if (AbstractWorldObject.IsAbstractCharacter(target))
					if (targetLiveCounter != ((AbstractCharacter) target).getLiveCounter())
						continue;

			//make sure mob is awake to respond.
			//if (target instanceof AbstractIntelligenceAgent)
			//((AbstractIntelligenceAgent)target).enableIntelligence();
			// If Hit roll required, test hit
			if (pb.requiresHitRoll() && !pb.isWeaponPower() && testAttack(caster, target, pb, msg))
				//aggro mob even on a miss
				continue;

			// update target of used power timer

			if (target.getObjectType().equals(GameObjectType.PlayerCharacter)) {

				((PlayerCharacter) target).setLastTargetOfUsedPowerTime();
				((PlayerCharacter) target).setTimeStamp("LastCombatPlayer", System.currentTimeMillis());
			}


			// finally Apply actions
			for (ActionsBase ab : pb.getActions()) {
				// get numTrains for power, skip action if invalid

				if (trains < ab.getMinTrains() || trains > ab.getMaxTrains())
					continue;
				// If something blocks the action, then stop

				if (ab.blocked(target, pb, trains))
					continue;
				// TODO handle overwrite stack order here
				String stackType = ab.getStackType();
				stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(ab.getUUID()) : stackType;
				//				if (!stackType.equals("IgnoreStack")) {
				if (target.getEffects().containsKey(stackType)) {
					// remove any existing power that overrides
					Effect ef = target.getEffects().get(stackType);
					AbstractEffectJob effect = null;
					if (ef != null) {
						JobContainer jc = ef.getJobContainer();
						if (jc != null)
							effect = (AbstractEffectJob) jc.getJob();
					}
					ActionsBase overwrite = effect.getAction();
					
					if (overwrite == null){
						Logger.error("NULL ACTION FOR EFFECT " + effect.getPowerToken());
						continue;
					}
					if (ab.getStackOrder() < overwrite.getStackOrder())
						continue; // not high enough to overwrite
					else if (ab.getStackOrder() > overwrite.getStackOrder()) {
						effect.setNoOverwrite(true);
						removeEffect(target, overwrite, true, false);
					} else if (ab.getStackOrder() == overwrite.getStackOrder())
						if (ab.greaterThanEqual()
								&& (trains >= effect.getTrains())) {
							effect.setNoOverwrite(true);
							removeEffect(target, overwrite, true, false);
						} else if (ab.always())
							removeEffect(target, overwrite, true, false);
						else if (ab.greaterThan()
								&& (trains > effect.getTrains())) {
							effect.setNoOverwrite(true);
							removeEffect(target, overwrite, true, false);
						} else if (ab.greaterThan() && pb.getToken() == effect.getPowerToken())
							removeEffect(target, overwrite, true, false);

						else
							continue; // trains not high enough to overwrite
				}

				//				}
				runPowerAction(caster, target, targetLoc, ab, trains, pb);
			}
		}

		//Handle chant
		if (pb != null && pb.isChant())
			for (ActionsBase ab : pb.getActions()) {
				AbstractPowerAction pa = ab.getPowerAction();
				if (pa != null)
					pa.handleChant(caster, caster, targetLoc, trains, ab, pb);
			}

		// TODO echo power use to everyone else
		msg.setNumTrains(9999);
		msg.setUnknown04(2);
		DispatchMessage.sendToAllInRange(caster, msg);

	}

	// *** Refactor : Wtf is this mess?

	private static boolean validMonsterType(AbstractWorldObject target, PowersBase pb) {

		if (pb == null || target == null)
			return false;

		String mtp = pb.getMonsterTypePrereq();

		if (mtp.length() == 0)
			return true;

		if (target.getObjectType().equals(GameObjectType.PlayerCharacter)) {

			PlayerCharacter pc = (PlayerCharacter) target;
			int raceID = 0;

			if (pc.getRace() != null)
				raceID = pc.getRace().getRaceRuneID();

			switch (mtp) {
			case "Shade":
				return raceID == 2015 || raceID == 2016;
			case "Elf":
				return raceID == 2008 || raceID == 2009;
			case "Dwarf":
				return raceID == 2006;
			case "Aracoix":
				return raceID == 2002 || raceID == 2003;
			case "Irekei":
				return raceID == 2013 || raceID == 2014;
			case "Vampire":
				return raceID == 2028 || raceID == 2029;
			}
		} else if (target.getObjectType().equals(GameObjectType.Mob)) {
			Mob mob = (Mob) target;

			if (pb.targetMob() && !mob.isMob() && !mob.isSiege)
				return false;
			else if (pb.targetPet() && !mob.isPet() && !mob.isSiege)
				return false;

			switch (mtp) {
			case "Animal":
				if ((mob.getObjectTypeMask() & MBServerStatics.MASK_BEAST) == 0)
					return false;
				break;
			case "NPC":
				if ((mob.getObjectTypeMask() & MBServerStatics.MASK_HUMANOID) == 0)
					return false;
				break;
			case "Rat":
				if ((mob.getObjectTypeMask() & MBServerStatics.MASK_RAT) == 0)
					return false;
				break;
			case "Siege":
				if (!mob.isSiege)
					return false;
				break;
			case "Undead":
				if ((mob.getObjectTypeMask() & MBServerStatics.MASK_UNDEAD) == 0)
					return false;
				break;
			}
			return true;
		} else return target.getObjectType().equals(GameObjectType.Building) && mtp.equals("Siege");
		return false;
	}

	public static void summon(SendSummonsRequestMsg msg, ClientConnection origin) {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(
				origin);
		if (pc == null)
			return;

		PlayerCharacter target = SessionManager
				.getPlayerCharacterByLowerCaseName(msg.getTargetName());
		if (target == null || target.equals(pc) || target.isCombat()) {

			if (target == null) // Player not found. Send not found message
				ChatManager.chatInfoError(pc,
						"Cannot find that player to summon.");
			else if (target.isCombat())
				ChatManager.chatInfoError(pc,
						"Cannot summon player in combat.");
			// else trying to summon self, just fail

			// recycle summon
			sendRecyclePower(msg.getPowerToken(), origin);

			// TODO: client already subtracted 200 mana.. need to correct it
			// end cast
			PerformActionMsg pam = new PerformActionMsg(msg.getPowerToken(),
					msg.getTrains(), msg.getSourceType(), msg.getSourceID(), 0,
					0, 0f, 0f, 0f, 1, 0);
			sendPowerMsg(pc, 2, pam);

			return;
		}

		PerformActionMsg pam = new PerformActionMsg(msg.getPowerToken(), msg
				.getTrains(), msg.getSourceType(), msg.getSourceID(), target
				.getObjectType().ordinal(), target.getObjectUUID(), 0f, 0f, 0f, 1, 0);

		// Client removes 200 mana on summon use.. so don't send message to self
		target.addSummoner(pc.getObjectUUID(), System.currentTimeMillis() + MBServerStatics.FOURTYFIVE_SECONDS);
		usePower(pam, origin, false);
	}

	public static void recvSummon(RecvSummonsRequestMsg msg, ClientConnection origin) {
		PlayerCharacter pc = SessionManager.getPlayerCharacter(origin);
		if (pc == null)
			return;

		PlayerCharacter source = PlayerCharacter.getFromCache(msg.getSourceID());
		if (source == null)
			return;

		long tooLate = pc.getSummoner(source.getObjectUUID());
		if (tooLate < System.currentTimeMillis()) {
			ChatManager.chatInfoError(pc, "You waited too long to " + (msg.accepted() ? "accept" : "decline") + " the summons.");
			pc.removeSummoner(source.getObjectUUID());
			return;
		}

		if (pc.getBonuses() != null && pc.getBonuses().getBool(ModType.BlockedPowerType, SourceType.SUMMON)) {
			ErrorPopupMsg.sendErrorMsg(pc, "You have been blocked from receiving summons!");
			ErrorPopupMsg.sendErrorMsg(source, "Target is blocked from receiving summons!");
			pc.removeSummoner(source.getObjectUUID());
			return;
		}
		pc.removeSummoner(source.getObjectUUID());

		// Handle Accepting or Denying a summons.
		// set timer based on summon type.
		boolean wentThrough = false;
		if (msg.accepted())
			// summons accepted, let's move the player if within time
			if (source.isAlive()) {

				//				//make sure summons handled in time
				ConcurrentHashMap<String, JobContainer> timers = source.getTimers();
				//				if (timers == null || !timers.containsKey("SummonSend")) {
				//					ChatManager.chatInfoError(pc, "You waited too long to " + (msg.accepted() ? "accept" : "decline") + " the summons.");
				//					return;
				//				}

				//				// clear last summons accept timer
				//	timers.get("SummonSend").cancelJob();
				//timers.remove("SummonSend");
				// cancel any other summons waiting
				timers = pc.getTimers();
				if (timers != null && timers.containsKey("Summon"))
					timers.get("Summon").cancelJob();

				// get time to wait before summons goes through
				BaseClass base = source.getBaseClass();
				PromotionClass promo = source.getPromotionClass();
				int duration;


				//determine if in combat with another player


				//comment out this block to disable combat timer
				//				if (lastAttacked < 60000) {
				//					if (pc.inSafeZone()) //player in safe zone, no need for combat timer
				//						combat = false;
				//					else if (source.inSafeZone()) //summoner in safe zone, apply combat timer
				//						combat = true;
				//					else if ((source.getLoc().distance2D(pc.getLoc())) > 6144f)
				//						combat = true; //more than 1.5x width of zone, not tactical summons
				//				}

                if (promo != null && promo.getObjectUUID() == 2519)
					duration = 10000; // Priest summons, 10 seconds
				else if (base != null && base.getObjectUUID() == 2501)
					duration = 15000; // Healer Summons, 15 seconds
				else
					duration = 45000; // Belgosh Summons, 45 seconds


				// Teleport to summoners location
				FinishSummonsJob fsj = new FinishSummonsJob(source, pc);
				JobContainer jc = JobScheduler.getInstance().scheduleJob(fsj,
						duration);
				if (timers != null)
					timers.put("Summon", jc);
				wentThrough = true;
			}

		// Summons failed
		if (!wentThrough)
			// summons refused. Let's be nice and reset recycle timer
			if (source != null) {

				// Send summons refused Message
				ErrorPopupMsg.sendErrorPopup(source, 29);

				// recycle summons power
				//finishRecycleTime(428523680, source, true);
			}
	}

	public static void trackWindow(TrackWindowMsg msg, ClientConnection origin) {

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(
				origin);

		if (playerCharacter == null)
			return;

		if (MBServerStatics.POWERS_DEBUG) {
			ChatManager.chatSayInfo(
					playerCharacter,
					"Using Power: " + Integer.toHexString(msg.getPowerToken())
					+ " (" + msg.getPowerToken() + ')');
			Logger.info( "Using Power: "
					+ Integer.toHexString(msg.getPowerToken()) + " ("
					+ msg.getPowerToken() + ')');
		}

		// get track power used
		PowersBase pb = PowersManager.powersBaseByToken.get(msg.getPowerToken());

		if (pb == null || !pb.isTrack())
			return;

		//check track threshold timer to prevent spam
		long currentTime = System.currentTimeMillis();
		long timestamp = playerCharacter.getTimeStamp("trackWindow");
		long dif = currentTime - timestamp;
		if (dif < MBServerStatics.TRACK_WINDOW_THRESHOLD)
			return;
		playerCharacter.setTimeStamp("trackWindow", currentTime);

		ArrayList<ActionsBase> ablist = pb.getActions();
		if (ablist == null)
			return;

		TrackPowerAction tpa = null;
		for (ActionsBase ab : ablist) {
			AbstractPowerAction apa = ab.getPowerAction();
			if (apa != null && apa instanceof TrackPowerAction)
				tpa = (TrackPowerAction) apa;
		}
		if (tpa == null)
			return;

		// Check powers for normal users
		if (playerCharacter.getPowers() == null || !playerCharacter.getPowers().containsKey(msg.getPowerToken()))
			if (!playerCharacter.isCSR())
				if (!MBServerStatics.POWERS_DEBUG) {
					//  ChatManager.chatSayInfo(pc, "You may not cast that spell!");
					//  this.logEXPLOIT("usePowerA(): Cheat attempted? '" + msg.getPowerToken() + "' was not associated with " + pc.getName());
					return;
				}

		// Get search mask for track
		int mask = 0;
		if (pb.targetPlayer())
			if (tpa.trackVampire()) // track vampires
				mask = MBServerStatics.MASK_PLAYER | MBServerStatics.MASK_UNDEAD;
			else
				// track all players
				mask = MBServerStatics.MASK_PLAYER;
		else if (pb.targetCorpse()) // track corpses
			mask = MBServerStatics.MASK_CORPSE;
		else if (tpa.trackNPC()) // Track NPCs
			mask = MBServerStatics.MASK_NPC;
		else if (tpa.trackUndead()) // Track Undead
			mask = MBServerStatics.MASK_MOB | MBServerStatics.MASK_UNDEAD;
		else
			// Track All
			mask = MBServerStatics.MASK_MOB | MBServerStatics.MASK_NPC;

		// Find characters in range
		HashSet<AbstractWorldObject> allTargets;
		allTargets = WorldGrid.getObjectsInRangeContains(playerCharacter.getLoc(),
				pb.getRange(), mask);

		//remove anyone who can't be tracked
		Iterator<AbstractWorldObject> it = allTargets.iterator();
		while (it.hasNext()) {
			AbstractWorldObject awo = it.next();
			if (awo == null)
				continue;
			else if (!awo.isAlive())
				it.remove();
			else if (awo.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				PlayerBonuses bonus = ((PlayerCharacter) awo).getBonuses();
				if (bonus != null && bonus.getBool(ModType.CannotTrack, SourceType.None))
					it.remove();
			}
		}

		// get max charcters for window
		int maxTargets = 20;
		PromotionClass promo = playerCharacter.getPromotionClass();
		if (promo != null) {
			int tableID = promo.getObjectUUID();
			if (tableID == 2512 || tableID == 2514 || tableID == 2515)
				maxTargets = 40;
		}

		// create list of characters
		HashSet<AbstractCharacter> trackChars = RangeBasedAwo.getTrackList(
				allTargets, playerCharacter, maxTargets);

		TrackWindowMsg trackWindowMsg = new TrackWindowMsg(msg);

		// send track window
		trackWindowMsg.setSource(playerCharacter);
		trackWindowMsg.setCharacters(trackChars);

		Dispatch dispatch = Dispatch.borrow(playerCharacter, trackWindowMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	private static void sendRecyclePower(int token, ClientConnection origin) {
		RecyclePowerMsg recyclePowerMsg = new RecyclePowerMsg(token);

		Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), recyclePowerMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

	}

	public static boolean verifyInvalidRange(AbstractCharacter ac,
			AbstractWorldObject target, float range) {
		Vector3fImmutable sl = ac.getLoc();
		Vector3fImmutable tl = target.getLoc();
		 if (target.getObjectType().equals(GameObjectType.Item)) {

			Item item = (Item) target;
			AbstractGameObject owner = item.getOwner();

			if (owner == null || owner.getObjectType().equals(GameObjectType.Account))
				return true;

			if (owner.getObjectType().equals(GameObjectType.PlayerCharacter) || owner.getObjectType().equals(GameObjectType.Mob)) {
				AbstractCharacter acOwner = (AbstractCharacter) owner;
				CharacterItemManager itemMan = acOwner.getCharItemManager();
				if (itemMan == null)
					return true;
				if (itemMan.inventoryContains(item)) {
					tl = acOwner.getLoc();
					return !(sl.distanceSquared(tl) <= sqr(range));
				}
				return true;
			}
			return true;
		}

		range += (calcHitBox(ac) + calcHitBox(target));


		float distanceToTarget = sl.distanceSquared(tl);//distance to center of target

		return distanceToTarget > range * range;

	}

	public static float calcHitBox(AbstractWorldObject ac) {
		//TODO Figure out how Str Affects HitBox
		float hitBox = 1;
		switch (ac.getObjectType()) {
		case PlayerCharacter:
			PlayerCharacter pc = (PlayerCharacter) ac;
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG)
                Logger.info( "Hit box radius for " + pc.getFirstName() + " is " + ((int) pc.statStrBase / 200f));
            hitBox = 2f + (int) ((PlayerCharacter) ac).statStrBase / 50f;
			break;

		case Mob:
			Mob mob = (Mob) ac;
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG)
				Logger.info( "Hit box radius for " + mob.getFirstName()
				+ " is " + ((Mob) ac).getMobBase().getHitBoxRadius());

			hitBox = ((Mob) ac).getMobBase().getHitBoxRadius();
			break;
		case Building:
			Building building = (Building) ac;
			if (building.getBlueprint() == null)
				return 32;
			hitBox = Math.max(building.getBlueprint().getBuildingGroup().getExtents().x,
					building.getBlueprint().getBuildingGroup().getExtents().y);
			if (MBServerStatics.COMBAT_TARGET_HITBOX_DEBUG)
				Logger.info("Hit box radius for " + building.getName() + " is " + hitBox);
			break;

		}
		return hitBox;
	}

	// Apply a power based on it's IDString
	public static void applyPower(AbstractCharacter ac, AbstractWorldObject target,
			Vector3fImmutable targetLoc, String ID, int trains, boolean fromItem) {
		if (ac == null || target == null || !ac.isAlive())
			return;
		PowersBase pb = powersBaseByIDString.get(ID);
		if (pb == null) {
			Logger.error(
					"applyPower(): Got NULL on powersBaseByIDString table lookup for: "
							+ ID);
			return;
		}
		applyPowerA(ac, target, targetLoc, pb, trains, fromItem);
	}

	// Apply a power based on it's Token
	public static void applyPower(AbstractCharacter ac, AbstractWorldObject target,
			Vector3fImmutable targetLoc, int token, int trains, boolean fromItem) {
		if (ac == null || target == null)
			return;

		//Don't apply power if ac is dead, unless death shroud or safe mode
		if (!ac.isAlive())
			if (!(token == -1661758934 || token == 1672601862))
				return;

		PowersBase pb = powersBaseByToken.get(token);
		if (pb == null) {
			Logger.error(
					"applyPower(): Got NULL on powersBaseByToken table lookup for: "
							+ token);
			return;
		}
		applyPowerA(ac, target, targetLoc, pb, trains, fromItem);
	}

	private static void applyPowerA(AbstractCharacter ac, AbstractWorldObject target,
			Vector3fImmutable targetLoc, PowersBase pb, int trains,
			boolean fromItem) {
		int time = pb.getCastTime(trains);
		if (!fromItem)
			finishApplyPowerA(ac, target, targetLoc, pb, trains, false);
		else if (time == 0)
			finishApplyPower(ac, target, targetLoc, pb, trains, ac.getLiveCounter());
		else {

			ac.setItemCasting(true);
			int tarType = (target == null) ? 0 : target.getObjectType().ordinal();
			int tarID = (target == null) ? 0 : target.getObjectUUID();

			// start the action animation
			PerformActionMsg msg = new PerformActionMsg(pb.getToken(),
					trains, ac.getObjectType().ordinal(), ac.getObjectUUID(), tarType, tarID, 0,
					0, 0, 1, 0);
			DispatchMessage.sendToAllInRange(target, msg);


			ConcurrentHashMap<String, JobContainer> timers = ac.getTimers();

			if (timers.containsKey(Integer.toString(pb.getToken()))) {
				JobContainer jc = timers.get(Integer.toString(pb.getToken()));
				if (jc != null)
					jc.cancelJob();
			}

			//				// clear any other items being used
			//				JobContainer jc = ac.getLastItem();
			//				if (jc != null) {
			//					jc.cancelJob();
			//					ac.clearLastItem();
			//				}
			// run timer job to end cast
			UseItemJob uij = new UseItemJob(ac, target, pb, trains, ac.getLiveCounter());
			JobContainer jc = js.scheduleJob(uij, time);

			// make lastItem
			timers.put(Integer.toString(pb.getToken()), jc);
		}
	}

	public static void finishApplyPower(AbstractCharacter ac,
			AbstractWorldObject target, Vector3fImmutable targetLoc,
			PowersBase pb, int trains, int liveCounter) {
		
		if (ac != null)
			ac.setItemCasting(false);
		if (ac == null || target == null || pb == null)
			return;
		
		ac.clearTimer(Integer.toString(pb.getToken()));
		if (liveCounter == ac.getLiveCounter())
			finishApplyPowerA(ac, target, targetLoc, pb, trains, false);
	}

	public static void finishApplyPowerA(AbstractCharacter ac,
			AbstractWorldObject target, Vector3fImmutable targetLoc,
			PowersBase pb, int trains, boolean fromChant) {
		// finally Apply actions
		ArrayList<ActionsBase> actions = pb.getActions();
		for (ActionsBase ab : actions) {
			// get numTrains for power, skip action if invalid
			if (trains < ab.getMinTrains() || trains > ab.getMaxTrains())
				continue;
			// If something blocks the action, then stop
			if (ab.blocked(target, pb, trains))
				// sendPowerMsg(pc, 5, msg);
				continue;
			// TODO handle overwrite stack order here
			String stackType = ab.getStackType();
			stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(ab.getUUID()) : stackType;
			if (target.getEffects().containsKey(stackType)) {
				// remove any existing power that overrides
				Effect ef = target.getEffects().get(stackType);
				AbstractEffectJob effect = null;
				if (ef != null) {
					JobContainer jc = ef.getJobContainer();
					if (jc != null)
						effect = (AbstractEffectJob) jc.getJob();
				}
				ActionsBase overwrite = effect.getAction();
				PowersBase pbOverwrite = effect.getPower();
				if (pbOverwrite != null && pbOverwrite.equals(pb)
						&& (trains >= effect.getTrains()))
					removeEffect(target, overwrite, true, fromChant);
				else if (ab.getStackOrder() < overwrite.getStackOrder())
					continue; // not high enough to overwrite
				else if (ab.getStackOrder() > overwrite.getStackOrder())
					removeEffect(target, overwrite, true, false);
				else if (ab.getStackOrder() == overwrite.getStackOrder())
					if (ab.greaterThanEqual()
							&& (trains >= effect.getTrains()))
						removeEffect(target, overwrite, true, false);
					else if (ab.always())
						removeEffect(target, overwrite, true, false);
					else if (ab.greaterThan()
							&& (trains > effect.getTrains()))
						removeEffect(target, overwrite, true, false);
					else if (ab.greaterThan() && pb.getToken() == effect.getPowerToken())
						removeEffect(target, overwrite, true, false);
					else
						continue; // trains not high enough to overwrite
			}
			if (fromChant)
				targetLoc = Vector3fImmutable.ZERO;
			runPowerAction(ac, target, targetLoc, ab, trains, pb);
		}

		//Handle chant
		if (pb != null && pb.isChant())
			for (ActionsBase ab : pb.getActions()) {
				AbstractPowerAction pa = ab.getPowerAction();
				if (pa != null)
					pa.handleChant(ac, target, targetLoc, trains, ab, pb);
			}

		// for chants, only send the animation if the character is not is not moving or casting
		boolean doAnimation = true;

		if (target.getObjectType().equals(GameObjectType.PlayerCharacter)) {
			PlayerCharacter pc = (PlayerCharacter) target;
			if (pb != null && pb.isChant() && (pc.isMoving() || pc.isCasting()))
				doAnimation = false;
		}

		if (pb.getToken() == 428950414)
			doAnimation = true;

		if (doAnimation) {
			PerformActionMsg msg = new PerformActionMsg(pb.getToken(), 9999, ac
					.getObjectType().ordinal(), ac.getObjectUUID(), target.getObjectType().ordinal(),
					target.getObjectUUID(), 0, 0, 0, 2, 0);

			DispatchMessage.sendToAllInRange(ac, msg);

		}
	}

	public static void runPowerAction(AbstractCharacter source,
			AbstractWorldObject awo, Vector3fImmutable targetLoc,
			ActionsBase ab, int trains, PowersBase pb) {
		AbstractPowerAction pa = ab.getPowerAction();
		if (pa == null) {
			Logger.error(
					"runPowerAction(): PowerAction not found of IDString: "
							+ ab.getEffectID());
			return;
		}
		pa.startAction(source, awo, targetLoc, trains, ab, pb);
	}

	public static void runPowerAction(AbstractCharacter source,
			AbstractWorldObject awo, Vector3fImmutable targetLoc,
			ActionsBase ab, int trains, PowersBase pb, int duration) {
		AbstractPowerAction pa = ab.getPowerAction();
		if (pa == null) {
			Logger.error(
					"runPowerAction(): PowerAction not found of IDString: "
							+ ab.getEffectID());
			return;
		}
		pa.startAction(source, awo, targetLoc, trains, ab, pb, duration);
	}

	public static HashSet<AbstractWorldObject> getAllTargets(
			AbstractWorldObject target, Vector3fImmutable tl,
			PlayerCharacter pc, PowersBase pb) {
		HashSet<AbstractWorldObject> allTargets;
		if (pb.isAOE()) {
			Vector3fImmutable targetLoc = null;
			if (pb.usePointBlank()) {
				targetLoc = pc.getLoc();
			} else {
				if (target != null) {
					targetLoc = target.getLoc();
				} else {
					targetLoc = tl;
					try{
						targetLoc = targetLoc.setY(HeightMap.getWorldHeight(targetLoc)); //on ground
					}catch (Exception e){
						Logger.error(e);
						targetLoc = tl;
					}
				
				}
			}

			if (targetLoc.x == 0f || targetLoc.z == 0f)
				return new HashSet<>(); // invalid loc,
			// return
			// nothing

			//first find targets in range quickly with QTree
			if (pb.targetPlayer() && pb.targetMob())
				// Player and mobs
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_MOBILE);
			else if (pb.targetPlayer())
				// Player only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_PLAYER);
			else if (pb.targetMob())
				// Mob only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_MOB
						| MBServerStatics.MASK_PET);
			else if (pb.targetPet())
				//Pet only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_PET);
			else if (pb.targetNecroPet())
				allTargets = WorldGrid.getObjectsInRangePartialNecroPets(
						targetLoc, pb.getRadius());
			else
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), 0);

			// cleanup self, group and nation targets if needed
			Iterator<AbstractWorldObject> awolist = allTargets.iterator();
			while (awolist.hasNext()) {
				AbstractWorldObject awo = awolist.next();
				if (awo == null) {
					awolist.remove(); // won't hit a null
					continue;
				}

				//see if targets are within 3D range of each other
				Vector3fImmutable tloc = awo.getLoc();

				if (tloc.distanceSquared(targetLoc) > sqr(pb.getRadius())) {
					awolist.remove(); // too far away
					continue;
				}

				if (pb.isCasterFriendly() && pc.equals(awo)) {
					awolist.remove(); // won't hit self
					continue;
				}

				if (!awo.isAlive()) {
					awolist.remove(); // too far away
					continue;
				}

				if (awo.getObjectType().equals(GameObjectType.PlayerCharacter)) {

					PlayerCharacter pcc = (PlayerCharacter) awo;

					if (pb.isGroupFriendly() && GroupManager.getGroup(pc) != null && GroupManager.getGroup(pcc) != null)
						if (GroupManager.getGroup(pc).equals(GroupManager.getGroup(pcc))) {
							awolist.remove(); // Won't hit group members
							continue;
						}
					if (pb.isNationFriendly() && pc.getGuild() != null &&
							pc.getGuild().getNation() != null && pcc.getGuild() != null &&
							pc.getGuild().getNation() != null)
						if (pc.getGuild().getNation().equals(pcc.getGuild().getNation())) {
							awolist.remove(); // Won't hit nation members
							continue;
						}

					// Remove players for non-friendly spells in safe zone
					if (pb.isHarmful() && (pcc.inSafeZone() || pc.inSafeZone())) {
						awolist.remove();
						continue;
					}
				}
			}
			// Trim list down to max size closest targets, limited by max
			// Player/Mob amounts
			allTargets = RangeBasedAwo.getSortedList(allTargets, targetLoc, pb
					.getMaxNumPlayerTargets(), pb.getMaxNumMobTargets());
		} else if (pb.targetGroup()) {

			if (GroupManager.getGroup(pc) != null) {
				allTargets = WorldGrid.getObjectsInRangePartial(pc
						.getLoc(), pb.getRange(), MBServerStatics.MASK_PLAYER);
				Iterator<AbstractWorldObject> awolist = allTargets.iterator();
				while (awolist.hasNext()) {

					AbstractWorldObject awo = awolist.next();

					if (!(awo.getObjectType().equals(GameObjectType.PlayerCharacter))) {
						awolist.remove(); // remove non players if there are any
						continue;
					}
					PlayerCharacter pcc = (PlayerCharacter) awo;

					if (GroupManager.getGroup(pcc) == null)
						awolist.remove(); // remove players not in a group
					else if (!GroupManager.getGroup(pcc).equals(GroupManager.getGroup(pc)))
						awolist.remove(); // remove if not same group

				}
			} else {
				allTargets = new HashSet<>();
				allTargets.add(pc); // no group, use only self
			}
		} else {
			allTargets = new HashSet<>();
			if (pb.targetSelf())
				allTargets.add(pc);
			else if (pb.targetFromLastTarget())
				allTargets.add(target);
			else if (pb.targetFromNearbyMobs())
				allTargets.add(target); // need better way to do this later
			else
				// targetByName
				allTargets.add(target); // need to get name later
			// can't target self if caster friendly
			if (pb.isCasterFriendly() && allTargets.contains(pc))
				allTargets.remove(0);
		}

		Iterator<AbstractWorldObject> awolist = allTargets.iterator();
		while (awolist.hasNext()) {
			AbstractWorldObject awo = awolist.next();

			//See if target is valid type
			if (!validMonsterType(awo, pb)) {
				awolist.remove();
				continue;
			}

			if (awo != null && awo.getObjectType().equals(GameObjectType.PlayerCharacter)) {

				// Remove players who are in safe mode
				PlayerCharacter pcc = (PlayerCharacter) awo;
				PlayerBonuses bonuses = pcc.getBonuses();

				if (bonuses != null && bonuses.getBool(ModType.ImmuneToPowers, SourceType.None)) {
					awolist.remove();
					continue;
				}

				//remove if power is harmful and caster or target is in safe zone
				if (pb.isHarmful() && (pcc.inSafeZone() || pc.inSafeZone())) {
					awolist.remove();
					continue;
				}
			}
		}

		// verify target has proper effects applied to receive power
		if (pb.getTargetEffectPrereqs().size() > 0) {
			Iterator<AbstractWorldObject> it = allTargets.iterator();
			while (it.hasNext()) {
				boolean passed = false;
				AbstractWorldObject awo = it.next();
				if (awo.getEffects() != null) {
					for (PowerPrereq pp : pb.getTargetEffectPrereqs()) {
						EffectsBase eb = PowersManager.getEffectByIDString(pp.getEffect());
						if (awo.containsEffect(eb.getToken())) {
							passed = true;
							break;
						}
					}
					if (!passed)
						it.remove();
				} else
					it.remove(); //awo is missing it's effects list
			}
		}
		return allTargets;
	}

	public static HashSet<AbstractWorldObject> getAllTargets(
			AbstractWorldObject target, Vector3fImmutable tl,
			AbstractCharacter caster, PowersBase pb) {
		HashSet<AbstractWorldObject> allTargets;
		if (pb.isAOE()) {
			Vector3fImmutable targetLoc = tl;
			if (pb.usePointBlank()) {
				targetLoc = caster.getLoc();
			} else {
				if (target != null) {
					targetLoc = target.getLoc();
				} else {
					targetLoc = tl;
					try{
						targetLoc = targetLoc.setY(HeightMap.getWorldHeight(targetLoc)); //on ground
					}catch (Exception e){
						Logger.error(e);
					}
					
				}
			}

			if (targetLoc.x == 0f || targetLoc.z == 0f)
				return new HashSet<>(); // invalid loc,
	
			//first find targets in range quickly with QTree
			if (pb.targetPlayer() && pb.targetMob())
				// Player and mobs
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_MOBILE);
			else if (pb.targetPlayer())
				// Player only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_PLAYER);
			else if (pb.targetMob())
				// Mob only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_MOB
						| MBServerStatics.MASK_PET);
			else if (pb.targetPet())
				//Pet only
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), MBServerStatics.MASK_PET);
			else if (pb.targetNecroPet())
				allTargets = WorldGrid.getObjectsInRangePartialNecroPets(
						targetLoc, pb.getRadius());
			else
				allTargets = WorldGrid.getObjectsInRangePartial(
						targetLoc, pb.getRadius(), 0);

			// cleanup self, group and nation targets if needed
			Iterator<AbstractWorldObject> awolist = allTargets.iterator();
			while (awolist.hasNext()) {
				AbstractWorldObject awo = awolist.next();
				if (awo == null) {
					awolist.remove(); // won't hit a null
					continue;
				}

				//see if targets are within 3D range of each other
				Vector3fImmutable tloc = awo.getLoc();

				if (tloc.distanceSquared(targetLoc) > sqr(pb.getRadius())) {
					awolist.remove(); // too far away
					continue;
				}

				if (pb.isCasterFriendly() && caster.equals(awo)) {
					awolist.remove(); // won't hit self
					continue;
				}

				if (awo.getObjectType() == GameObjectType.Mob) {
					awolist.remove(); // Won't hit other mobs.
					continue;
				}
			}
			// Trim list down to max size closest targets, limited by max
			// Player/Mob amounts
			allTargets = RangeBasedAwo.getSortedList(allTargets, targetLoc, pb
					.getMaxNumPlayerTargets(), pb.getMaxNumMobTargets());
		} else if (pb.targetGroup()) {
			allTargets = new HashSet<>();
			allTargets.add(caster); // no group, use only self
		} else {
			allTargets = new HashSet<>();
			if (pb.targetSelf())
				allTargets.add(caster);
			else if (pb.targetFromLastTarget())
				allTargets.add(target);
			else if (pb.targetFromNearbyMobs())
				allTargets.add(target); // need better way to do this later
			else
				// targetByName
				allTargets.add(target); // need to get name later
			// can't target self if caster friendly
			if (pb.isCasterFriendly() && allTargets.contains(caster))
				allTargets.remove(caster);
		}

		Iterator<AbstractWorldObject> awolist = allTargets.iterator();
		while (awolist.hasNext()) {
			AbstractWorldObject awo = awolist.next();

			//See if target is valid type
			if (!validMonsterType(awo, pb)) {
				awolist.remove();
				continue;
			}

			if (awo != null && awo.getObjectType().equals(GameObjectType.PlayerCharacter)) {
				// Remove players who are in safe mode
				PlayerCharacter pcc = (PlayerCharacter) awo;
				PlayerBonuses bonuses = pcc.getBonuses();
				if (bonuses != null && bonuses.getBool(ModType.ImmuneToPowers, SourceType.None)) {
					awolist.remove();
					continue;
				}
			}
		}

		// verify target has proper effects applied to receive power
		if (pb.getTargetEffectPrereqs().size() > 0) {
			Iterator<AbstractWorldObject> it = allTargets.iterator();
			while (it.hasNext()) {
				boolean passed = false;
				AbstractWorldObject awo = it.next();
				if (awo.getEffects() != null) {
					for (PowerPrereq pp : pb.getTargetEffectPrereqs()) {
						EffectsBase eb = PowersManager.getEffectByIDString(pp.getEffect());
						if (awo.containsEffect(eb.getToken())) {
							passed = true;
							break;
						}
					}
					if (!passed)
						it.remove();
				} else
					it.remove(); //awo is missing it's effects list
			}
		}
		return allTargets;
	}

	// removes an effect before time is finished
	public static void removeEffect(AbstractWorldObject awo, ActionsBase toRemove,
			boolean overwrite, boolean fromChant) {
		if (toRemove == null)
			return;

		String stackType = toRemove.getStackType();
		stackType = (stackType.equals("IgnoreStack")) ? Integer
				.toString(toRemove.getUUID()) : stackType;
				if (fromChant) {
					Effect eff = awo.getEffects().get(stackType);
					if (eff != null)
						eff.cancelJob(true);
				} else
					awo.cancelEffect(stackType, overwrite);
	}

	// removes an effect when timer finishes
	public static void finishEffectTime(AbstractWorldObject source,
			AbstractWorldObject awo, ActionsBase toRemove, int trains) {
		if (awo == null || toRemove == null)
			return;

		// remove effect from player
		String stackType = toRemove.getStackType();
		if (stackType.equals("IgnoreStack"))
			stackType = Integer.toString(toRemove.getUUID());
		awo.endEffect(stackType);
	}

	// removes an effect when timer is canceled
	public static void cancelEffectTime(AbstractWorldObject source,
			AbstractWorldObject awo, PowersBase pb, EffectsBase eb,
			ActionsBase toRemove, int trains, AbstractEffectJob efj) {
		if (awo == null || pb == null || eb == null || toRemove == null)
			return;
		eb.endEffect(source, awo, trains, pb, efj);
	}

	// called when cooldown ends letting player cast next spell
	public static void finishCooldownTime(PerformActionMsg msg, PlayerCharacter pc) {
		// clear spell so player can cast again
		// if (pc != null)
		// pc.clearLastPower();
	}

	// called when recycle time ends letting player cast spell again
	public static void finishRecycleTime(PerformActionMsg msg, PlayerCharacter pc,
			boolean canceled) {
		finishRecycleTime(msg.getPowerUsedID(), pc, canceled);
	}

	public static void finishRecycleTime(int token, PlayerCharacter pc,
			boolean canceled) {
		if (pc == null)
			return;

		ConcurrentHashMap<Integer, JobContainer> recycleTimers = pc
				.getRecycleTimers();
		// clear recycle time
		if (recycleTimers != null)
			if (recycleTimers.containsKey(token)) {
				if (canceled) {
					JobContainer jc = recycleTimers.get(token);
					if (jc != null)
						jc.cancelJob();
				}
				recycleTimers.remove(token);
			}

		// send recycle message to unlock power

		RecyclePowerMsg recyclePowerMsg = new RecyclePowerMsg(token);
		Dispatch dispatch = Dispatch.borrow(pc, recyclePowerMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);

	}

	// Called when a fail condition is met by player
	// such as moving, taking damage, ect.

	public static void cancelUseLastPower(PlayerCharacter pc) {

		if (pc == null)
			return;

		// set player is not casting for regens
		if (pc.isCasting()){
			pc.update();
		}
		pc.setIsCasting(false);

		UsePowerJob lastPower = null;
		JobContainer jc = pc.getLastPower();

		if (jc != null)
			lastPower = ((UsePowerJob) jc.getJob());

		if (lastPower == null)
			return;

		// clear recycle timer
		int token = lastPower.getToken();

		if (pc.getRecycleTimers().contains(token))
			finishRecycleTime(token, pc, true);

		//			pc.getRecycleTimers().remove(token);
		// Cancel power
		js.cancelScheduledJob(lastPower);

		// clear last power
		pc.clearLastPower();

	}

	private static AbstractWorldObject getTarget(PerformActionMsg msg) {

		int type = msg.getTargetType();
		int UUID = msg.getTargetID();

		if (type == -1 || type == 0 || UUID == -1 || UUID == 0)
			return null;

		return (AbstractWorldObject) DbManager.getObject(GameObjectType.values()[type], UUID);
	}

	public static boolean testAttack(PlayerCharacter pc, AbstractWorldObject awo,
			PowersBase pb, PerformActionMsg msg) {
		// Get defense for target
		float atr = CharacterSkill.getATR(pc, pb.getSkillName());
		float defense;

		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter tar = (AbstractCharacter) awo;
			defense = tar.getDefenseRating();
		} else
			defense = 0f;
		// Get hit chance

		if (pc.getDebug(16)) {
			String smsg = "ATR: " + atr + ", Defense: " + defense;
			ChatManager.chatSystemInfo(pc, smsg);
		}

		int chance;

		if (atr > defense || defense == 0)
			chance = 94;
		else {
			float dif = atr / defense;
			if (dif <= 0.8f)
				chance = 4;
			else
				chance = ((int) (450 * (dif - 0.8f)) + 4);
		}

		// calculate hit/miss
		int roll = ThreadLocalRandom.current().nextInt(100);

		boolean disable = true;
		if (roll < chance) {
			// Hit, check if dodge kicked in
			if (awo instanceof AbstractCharacter) {
				AbstractCharacter tarAc = (AbstractCharacter) awo;
				// Handle Dodge passive
				if (testPassive(pc, tarAc, "Dodge")) {
					// Dodge fired, send dodge message
					PerformActionMsg dodgeMsg = new PerformActionMsg(msg);
					dodgeMsg.setTargetType(awo.getObjectType().ordinal());
					dodgeMsg.setTargetID(awo.getObjectUUID());
					sendPowerMsg(pc, 4, dodgeMsg);
					return true;
				}
			}
			return false;
		} else {
			// Miss. Send miss message
			PerformActionMsg missMsg = new PerformActionMsg(msg);

			missMsg.setTargetType(awo.getObjectType().ordinal());
			missMsg.setTargetID(awo.getObjectUUID());
			sendPowerMsg(pc, 3, missMsg);
			return true;
		}
	}

	public static boolean testAttack(Mob caster, AbstractWorldObject awo,
			PowersBase pb, PerformActionMsg msg) {
		// Get defense for target
		float atr = 2000;
		float defense;

		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter tar = (AbstractCharacter) awo;
			defense = tar.getDefenseRating();
		} else
			defense = 0f;
		// Get hit chance

		int chance;

		if (atr > defense || defense == 0)
			chance = 94;
		else {
			float dif = atr / defense;
			if (dif <= 0.8f)
				chance = 4;
			else
				chance = ((int) (450 * (dif - 0.8f)) + 4);
		}

		// calculate hit/miss
		int roll = ThreadLocalRandom.current().nextInt(100);

		if (roll < chance) {
			// Hit, check if dodge kicked in
			if (AbstractWorldObject.IsAbstractCharacter(awo)) {
				AbstractCharacter tarAc = (AbstractCharacter) awo;
				// Handle Dodge passive
				return testPassive(caster, tarAc, "Dodge");
			}
			return false;
		} else
			return true;
	}

	public static void sendPowerMsg(PlayerCharacter playerCharacter, int type, PerformActionMsg msg) {

		if (playerCharacter == null)
			return;

		msg.setUnknown05(type);

		switch (type) {
		case 3:
		case 4:
			msg.setUnknown04(2);
			DispatchMessage.dispatchMsgToInterestArea(playerCharacter, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
			break;
		default:
			msg.setUnknown04(1);
			Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
		}
	}

	public static void sendEffectMsg(PlayerCharacter pc, int type, ActionsBase ab, PowersBase pb) {

		if (pc == null)
			return;

		try {

			EffectsBase eb = PowersManager.effectsBaseByIDString.get(ab.getEffectID());

			if (eb == null)
				return;

			ApplyEffectMsg aem = new ApplyEffectMsg(pc, pc, 0, eb.getToken(), 9, pb.getToken(), pb.getName());
			aem.setUnknown03(type);
			DispatchMessage.dispatchMsgToInterestArea(pc, aem, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);


		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

	}

	public static void sendEffectMsg(PlayerCharacter pc, int type, EffectsBase eb) {

		if (pc == null)
			return;
		try {

			if (eb == null)
				return;
			ApplyEffectMsg aem = new ApplyEffectMsg(pc, pc, 0, eb.getToken(), 0, eb.getToken(), "");
			aem.setUnknown03(type);
			aem.setUnknown05(1);

			DispatchMessage.dispatchMsgToInterestArea(pc, aem, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);


		} catch (Exception e) {
			Logger.error( e.getMessage());
		}

	}

	public static void sendMobPowerMsg(Mob mob, int type, PerformActionMsg msg) {

		msg.setUnknown05(type);
		switch (type) {
		case 3:
		case 4:
			DispatchMessage.sendToAllInRange(mob, msg);

		}
	}

	private static boolean testPassive(AbstractCharacter source,
			AbstractCharacter target, String type) {

		float chance = target.getPassiveChance(type, source.getLevel(), false);

		if (chance == 0f)
			return false;

		// max 75% chance of passive to fire
		if (chance > 75f)
			chance = 75f;

		int roll = ThreadLocalRandom.current().nextInt(100);
		// Passive fired
		// TODO send message
		// Passive did not fire
		return roll < chance;
	}

	private static boolean validateTarget(AbstractWorldObject target,
			PlayerCharacter pc, PowersBase pb) {

		//group target. uses pbaoe rules
		if (pb.targetGroup())
			return true;

		// target is player
		else if ((target.getObjectTypeMask() & MBServerStatics.MASK_PLAYER) != 0) {
			if (pb.targetPlayer())
				if (pb.isGroupOnly()) { //single target group only power
					PlayerCharacter trg = (PlayerCharacter) target;

					if (GroupManager.getGroup(trg) != null && GroupManager.getGroup(pc) != null)
						if (GroupManager.getGroup(trg).getObjectUUID() == GroupManager.getGroup(pc).getObjectUUID())
							return true; // both in same group, good to go
					return trg != null && pc.getObjectUUID() == trg.getObjectUUID();
				} else
					return true; // can target player, good to go.
			else if (target.getObjectUUID() == pc.getObjectUUID() && pb.targetSelf())
				return true; // can target self, good to go
			else if (pb.targetCorpse()) {
				//target is dead player
				PlayerCharacter trg = (PlayerCharacter) target;
				return !trg.isAlive();
			} else {
				PlayerCharacter trg = (PlayerCharacter) target;

				if (pb.targetGroup())
					if (GroupManager.getGroup(trg) != null && GroupManager.getGroup(pc) != null)
						if (GroupManager.getGroup(trg).getObjectUUID() == GroupManager.getGroup(pc)
						.getObjectUUID())
							return true; // both in same group, good to go
				if (pb.targetGuildLeader())
					if (pc.getGuild() != null)
						if (pc.getGuild().getGuildLeaderUUID() == trg.getObjectUUID())
							return true; // can hit guild leader, good to go
			}
			String outmsg = "Invalid Target";
			ChatManager.chatSystemInfo(pc, outmsg);
			return false; // can't target player, stop here
		} // target is mob
		else if ((target.getObjectTypeMask() & MBServerStatics.MASK_MOB) != 0)
			return pb.targetMob();

		// target is pet
		else if ((target.getObjectTypeMask() & MBServerStatics.MASK_PET) != 0)
			return pb.targetPet();

		// target is Building
		else if ((target.getObjectTypeMask() & MBServerStatics.MASK_BUILDING) != 0)
			return pb.targetBuilding();

		else if (target.getObjectType().equals(GameObjectType.Item)) {
			Item item = (Item) target;
			if (pb.targetItem())
				return true;
			// TODO add these checks later
			else if (pb.targetArmor() && item.getItemBase().getType().equals(ItemType.ARMOR))
				return true;
			else if (pb.targetJewelry() && item.getItemBase().getType().equals(ItemType.JEWELRY))
				return true;
			else return pb.targetWeapon() && item.getItemBase().getType().equals(ItemType.WEAPON);
		} // How did we get here? all valid targets have been covered
		else
			return false;
	}

	/*
	 * Cancel spell upon actions
	 */
	public static void cancelOnAttack(AbstractCharacter ac) {
		ac.cancelTimer("Stuck");
	}

	public static void cancelOnAttackSwing(AbstractCharacter ac) {
	}

	public static void cancelOnCast(AbstractCharacter ac) {

	}

	public static void cancelOnSpell(AbstractCharacter ac) {

		PowersBase power = getLastPower(ac);

		if (power != null && power.cancelOnCastSpell())
			cancelPower(ac, false);
		ac.cancelLastChant();
	}

	public static void cancelOnEquipChange(AbstractCharacter ac) {

	}

	public static void cancelOnLogout(AbstractCharacter ac) {

	}

	public static void cancelOnMove(AbstractCharacter ac) {

		PowersBase power = getLastPower(ac);

		if (power != null && !power.canCastWhileMoving())
			cancelPower(ac, false);

		//cancel items
		cancelItems(ac, true, false);
		ac.cancelTimer("Stuck");
	}
	
	

	public static void cancelOnNewCharm(AbstractCharacter ac) {

	}

	public static void cancelOnSit(AbstractCharacter ac) {
		cancelPower(ac, false); // Always cancel casts on sit
	}

	public static void cancelOnTakeDamage(AbstractCharacter ac) {

		PowersBase power = getLastPower(ac);

		if (power != null && power.cancelOnTakeDamage())
			cancelPower(ac, true);
		cancelItems(ac, false, true);
		ac.cancelTimer("Stuck");
	}

	public static void cancelOnTerritoryClaim(AbstractCharacter ac) {

	}

	public static void cancelOnUnEquip(AbstractCharacter ac) {

	}

	public static void cancelOnStun(AbstractCharacter ac) {

	}

	private static PowersBase getLastPower(AbstractCharacter ac) {
		if (ac == null)
			return null;

		JobContainer jc = ac.getLastPower();

		if (jc == null)
			return null;

		AbstractJob aj = jc.getJob();

		if (aj == null)
			return null;

		if (aj instanceof UsePowerJob) {
			UsePowerJob upj = (UsePowerJob) aj;
			return upj.getPowersBase();
		}
		return null;
	}

	private static PowersBase getLastItem(AbstractCharacter ac) {

		if (ac == null)
			return null;

		JobContainer jc = ac.getLastItem();

		if (jc == null)
			return null;

		AbstractJob aj = jc.getJob();

		if (aj == null)
			return null;

		if (aj instanceof UseItemJob) {
			UseItemJob uij = (UseItemJob) aj;
			return uij.getPowersBase();
		}
		return null;
	}

	//cancels last casted power
	private static void cancelPower(AbstractCharacter ac, boolean cancelCastAnimation) {

		if (ac == null)
			return;

		JobContainer jc = ac.getLastPower();

		if (jc == null)
			return;

		AbstractJob aj = jc.getJob();

		if (aj == null)
			return;

		if (aj instanceof AbstractScheduleJob)
			((AbstractScheduleJob) aj).cancelJob();

		ac.clearLastPower();

		//clear cast animation for everyone in view range
		if (aj instanceof UsePowerJob && cancelCastAnimation) {

			PerformActionMsg pam = ((UsePowerJob) aj).getMsg();

			if (pam != null) {
				pam.setNumTrains(9999);
				pam.setUnknown04(2);
				DispatchMessage.sendToAllInRange(ac, pam);
			}
		}
	}

	public static PerformActionMsg createPowerMsg(PowersBase pb, int trains, AbstractCharacter source, AbstractCharacter target) {
		return new PerformActionMsg(pb.getToken(), trains, source.getObjectType().ordinal(), source.getObjectUUID(), target.getObjectType().ordinal(), target.getObjectUUID(), target.getLoc().x, target.getLoc().y, target.getLoc().z, 0, 0);

	}

	//cancels any casts from using an item

	private static void cancelItems(AbstractCharacter ac, boolean cancelOnMove, boolean cancelOnTakeDamage) {
		JobContainer jc;
		AbstractJob aj;
		ConcurrentHashMap<String, JobContainer> timers;
		UseItemJob uij;
		PowersBase pb;
		AbstractWorldObject target;

		if (ac == null)
			return;

		timers = ac.getTimers();

		if (timers == null)
			return;

		for (String name : timers.keySet()) {

			jc = timers.get(name);

			if (jc == null)
				continue;

			aj = jc.getJob();

			if (aj != null && aj instanceof UseItemJob) {
				uij = (UseItemJob) aj;
				pb = uij.getPowersBase();

				if (pb == null)
					continue;

				if (!pb.canCastWhileMoving() && cancelOnMove) {
					uij.cancelJob();
					timers.remove(name);
					continue;
				}

				if ((pb.cancelOnTakeDamage() == false) &&
						(cancelOnTakeDamage == false))
					continue;

				uij.cancelJob();
				timers.remove(name);

				//clear cast animation for everyone in view range
				target = uij.getTarget();

				if (target != null) {
					PerformActionMsg pam = new PerformActionMsg(pb.getToken(), 9999, ac
							.getObjectType().ordinal(), ac.getObjectUUID(), target.getObjectType().ordinal(),
							target.getObjectUUID(), 0, 0, 0, 2, 0);
					DispatchMessage.sendToAllInRange(ac, pam);

				}
			}
		}
	}
}



