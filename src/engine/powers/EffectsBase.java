// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers;

import engine.Enum;
import engine.Enum.DamageType;
import engine.Enum.EffectSourceType;
import engine.Enum.GameObjectType;
import engine.Enum.PowerFailCondition;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.job.JobContainer;
import engine.jobs.AbstractEffectJob;
import engine.jobs.DamageOverTimeJob;
import engine.jobs.FinishSpireEffectJob;
import engine.jobs.NoTimeJob;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ApplyEffectMsg;
import engine.objects.*;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class EffectsBase {

	private int UUID;
	private String IDString;
	// private String name;
	private int token;
	private float amount;
	private float amountRamp;

	// flags
	private boolean isItemEffect;
	private boolean isSpireEffect;
	private boolean ignoreMod;
	private boolean dontSave;

	private boolean cancelOnAttack = false;
	private boolean cancelOnAttackSwing = false;
	private boolean cancelOnCast = false;
	private boolean cancelOnCastSpell = false;
	private boolean cancelOnEquipChange = false;
	private boolean cancelOnLogout = false;
	private boolean cancelOnMove = false;
	private boolean cancelOnNewCharm = false;
	private boolean cancelOnSit = false;
	private boolean cancelOnTakeDamage = false;
	private boolean cancelOnTerritoryClaim = false;
	private boolean cancelOnUnEquip = false;
	private boolean useRampAdd;
	private boolean isPrefix = false; //used by items
	private boolean isSuffix = false; //used by items
	private String name = "";
	private float value = 0;
	private  ConcurrentHashMap<ItemBase, Integer> resourceCosts = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Boolean> sourceTypes = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	public static HashMap<Integer,HashSet<EffectSourceType>> effectSourceTypeMap = new HashMap<>();
	public static HashMap<String, HashSet<AbstractEffectModifier>> modifiersMap = new HashMap<>();
	private static ConcurrentHashMap<String, String> itemEffectsByName =  new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static int NewID = 3000;
	public static HashMap<String,HashMap<String,ArrayList<String>>> OldEffectsMap = new HashMap<>();
	public static HashMap<String,HashMap<String,ArrayList<String>>> NewEffectsMap = new HashMap<>();
	public static HashMap<String,HashMap<String,ArrayList<String>>> ChangedEffectsMap = new HashMap<>();
	public static HashMap<String,HashSet<PowerFailCondition>> EffectFailConditions = new HashMap<>();
	public static HashMap<Integer,HashSet<DamageType>> EffectDamageTypes = new HashMap<>();
	
	public static HashSet<AbstractEffectModifier> DefaultModifiers = new HashSet<>();
	/**
	 * No Table ID Constructor
	 */
	public EffectsBase() {

	}

	public EffectsBase(EffectsBase copyEffect, int newToken, String IDString) {
	
		UUID = NewID++;
		this.IDString = IDString;
		this.token = newToken;
		
		//filll 
		if (copyEffect == null){
			int flags = 0;
			this.isItemEffect = ((flags & 1) != 0) ? true : false;
			this.isSpireEffect = ((flags & 2) != 0) ? true : false;
			this.ignoreMod = ((flags & 4) != 0) ? true : false;
			this.dontSave = ((flags & 8) != 0) ? true : false;

			if (this.IDString.startsWith("PRE-"))
				this.isPrefix = true;
			else if (this.IDString.startsWith("SUF-"))
				this.isSuffix = true;
			
		}
		
		
		this.amount = copyEffect.amount;
		this.amountRamp = copyEffect.amountRamp;
		this.isItemEffect = copyEffect.isItemEffect;
		this.isSpireEffect = copyEffect.isSpireEffect;
		this.ignoreMod = copyEffect.ignoreMod;
		this.dontSave = copyEffect.dontSave;
		this.cancelOnAttack = copyEffect.cancelOnAttack;
		this.cancelOnAttackSwing = copyEffect.cancelOnAttackSwing;
		this.cancelOnCast = copyEffect.cancelOnCast;
		this.cancelOnCastSpell = copyEffect.cancelOnCastSpell;
		this.cancelOnEquipChange = copyEffect.cancelOnEquipChange;
		this.cancelOnLogout = copyEffect.cancelOnLogout;
		this.cancelOnMove = copyEffect.cancelOnMove;
		this.cancelOnNewCharm = copyEffect.cancelOnNewCharm;
		this.cancelOnSit = copyEffect.cancelOnSit;
		this.cancelOnTakeDamage = copyEffect.cancelOnTakeDamage;
		this.cancelOnTerritoryClaim = copyEffect.cancelOnTerritoryClaim;
		this.cancelOnUnEquip = copyEffect.cancelOnUnEquip;
		this.useRampAdd = copyEffect.useRampAdd;
		this.isPrefix = copyEffect.isPrefix;
		this.isSuffix = copyEffect.isSuffix;
		this.name = copyEffect.name;
		this.value = copyEffect.value;
		this.resourceCosts = copyEffect.resourceCosts;
		
	}

	/**
	 * ResultSet Constructor
	 */
	public EffectsBase(ResultSet rs) throws SQLException {

		this.UUID = rs.getInt("ID");
		this.IDString = rs.getString("IDString");
		this.name = rs.getString("name");
		this.token = rs.getInt("Token");
		
		//override tokens for some effects like Safemode that use the Action Token instead of the effect Token,
		switch (this.IDString){
		case "INVIS-D":
			this.token = -1661751254;
			break;
		case "SafeMode":
			this.token = -1661750486;
			break;
			
		}
		int flags = rs.getInt("flags");
		this.isItemEffect = ((flags & 1) != 0) ? true : false;
		this.isSpireEffect = ((flags & 2) != 0) ? true : false;
		this.ignoreMod = ((flags & 4) != 0) ? true : false;
		this.dontSave = ((flags & 8) != 0) ? true : false;

		if (this.IDString.startsWith("PRE-"))
			this.isPrefix = true;
		else if (this.IDString.startsWith("SUF-"))
			this.isSuffix = true;
		// getFailConditions();
	}
	
	
	public static EffectsBase createNoDbEffectsBase(EffectsBase copyEffect, int newToken, String IDString){
		EffectsBase cachedEffectsBase = new EffectsBase(copyEffect,newToken,IDString);
		
		if (cachedEffectsBase == null)
			return null;
		
		//add to Lists.
		PowersManager.effectsBaseByIDString.put(cachedEffectsBase.IDString, cachedEffectsBase);
		PowersManager.effectsBaseByToken.put(cachedEffectsBase.token, cachedEffectsBase);
		
		return cachedEffectsBase;
	}



	public static ArrayList<EffectsBase> getAllEffectsBase() {
		PreparedStatementShared ps = null;
		ArrayList<EffectsBase> out = new ArrayList<>();
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_effectbase ORDER BY `IDString` DESC");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				EffectsBase toAdd = new EffectsBase(rs);
				out.add(toAdd);
			}
			rs.close();
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			ps.release();
		}
		//testHash(out);
		return out;
	}
	
	public static ArrayList<EffectsBase> getAllLiveEffectsBase() {
		PreparedStatementShared ps = null;
		ArrayList<EffectsBase> out = new ArrayList<>();
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_effectbase_24 ORDER BY `IDString` DESC");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				EffectsBase toAdd = new EffectsBase(rs);
				out.add(toAdd);
			}
			rs.close();
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			ps.release();
		}
		//testHash(out);
		return out;
	}

	//private static void testHash(ArrayList<EffectsBase> effs) {
	//	int valid = 0, invalid = 0;
	//	for (EffectsBase eff : effs) {
	//		String ids = eff.getIDString();
	//		int tok = eff.getToken();
	//		if (ids.length() != 8 || ids.startsWith("PRE-") || ids.startsWith("SUF-") || ids.endsWith("X") || !ids.substring(3,4).equals("-"))
	//			continue;
	//
	////		if ((tok > 1 || tok < 0) && ids.length() == 8) {
	//			int out = Hash(ids);
	//			if (out != tok) {
	//				System.out.println(ids + ": " + Integer.toHexString(out) + "(" + out + ")");
	//				invalid++;
	//			} else
	//				valid++;
	////		}
	//	}
	//	System.out.println("valid: " + valid + ", invalid: " + invalid);
	//}

	//private static int Hash(String IDString) {
	//	char[] val = IDString.toCharArray();
	//	int out = 360;
	//	out ^= val[0];
	//	out ^= (val[1] << 5);
	//	out ^= (val[2] << 10);
	//	out ^= (val[4] << 23);
	//	out ^= (val[5] << 19);
	//	out ^= (val[6] << 15);
	//	out ^= (val[7] << 26);
	//	out ^= (val[7] >> 6);
	//	out ^= 17;
	//	return out;
	//}


	public static void getFailConditions(HashMap<String, EffectsBase> effects) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_failcondition WHERE powerOrEffect = 'Effect';");
		
			ResultSet rs = ps.executeQuery();
			PowerFailCondition failCondition = null;
	
			Object value;
			while (rs.next()) {
				String fail = rs.getString("type");
				
				
				
				String IDString = rs.getString("IDString");
				int token = DbManager.hasher.SBStringHash(IDString);
				failCondition = PowerFailCondition.valueOf(fail);
				if (failCondition == null){
					Logger.error( "Couldn't Find FailCondition " + fail + " for " + IDString);
					continue;
				}
				
				if (EffectsBase.EffectFailConditions.get(IDString) == null){
				EffectsBase.EffectFailConditions.put(IDString, new HashSet<>());
				}
				
				EffectsBase.EffectFailConditions.get(IDString).add(failCondition);
				EffectsBase eb = effects.get(IDString);
			
					switch (failCondition) {
					
					case TakeDamage:
						
						
						
						// dont go any further.
						if (eb == null){
						break;
						}
						
						eb.cancelOnTakeDamage = true;	
						
						
						
					
						eb.amount = rs.getFloat("amount");
						eb.amountRamp = rs.getFloat("ramp");
						eb.useRampAdd = rs.getBoolean("UseAddFormula");
						
						String damageType1 = rs.getString("damageType1");
						String damageType2 = rs.getString("damageType1");
						String damageType3 = rs.getString("damageType1");
						
						
						if (damageType1.isEmpty() && damageType2.isEmpty() && damageType3.isEmpty())
							break;
						
						if (!EffectsBase.EffectDamageTypes.containsKey(eb.getToken())){
							EffectsBase.EffectDamageTypes.put(eb.getToken(), new HashSet<>());
						}
						if (damageType1.equalsIgnoreCase("Crushing"))
							damageType1 = "Crush";
						if (damageType1.equalsIgnoreCase("Piercing"))
							damageType1 = "Pierce";
						if (damageType1.equalsIgnoreCase("Slashing"))
							damageType1 = "Slash";
						
						if (damageType2.equalsIgnoreCase("Crushing"))
							damageType2 = "Crush";
						if (damageType2.equalsIgnoreCase("Piercing"))
							damageType2 = "Pierce";
						if (damageType2.equalsIgnoreCase("Slashing"))
							damageType2 = "Slash";
						
						if (damageType3.equalsIgnoreCase("Crushing"))
							damageType3 = "Crush";
						if (damageType3.equalsIgnoreCase("Piercing"))
							damageType3 = "Pierce";
						if (damageType3.equalsIgnoreCase("Slashing"))
							damageType3 = "Slash";
						DamageType dt = getDamageType(damageType1);
						if (dt != null)
							EffectsBase.EffectDamageTypes.get(eb.token).add(dt);
							
						 dt = getDamageType(damageType2);
						if (dt != null)
							EffectsBase.EffectDamageTypes.get(eb.token).add(dt);
						 dt = getDamageType(damageType3);
						if (dt != null)
							EffectsBase.EffectDamageTypes.get(eb.token).add(dt);
						break;
					case Attack:
						eb.cancelOnAttack = true;
						break;
					case AttackSwing:
						eb.cancelOnAttackSwing = true;
						break;
					case Cast:
						eb.cancelOnCast = true;
						break;
					case CastSpell:
						eb.cancelOnCastSpell = true;
						break;
					case EquipChange:
						eb.cancelOnEquipChange = true;
						break;
					case Logout:
						eb.cancelOnLogout = true;
						break;
					case Move:
						eb.cancelOnMove = true;
						break;
					case NewCharm:
						eb.cancelOnNewCharm = true;
						break;
					case Sit:
						eb.cancelOnSit = true;
						break;
					case TerritoryClaim:
						eb.cancelOnTerritoryClaim = true;
						break;
					case UnEquip:
						eb.cancelOnUnEquip = true;
						break;
					}
				}
			
			rs.close();
		} catch (Exception e) {
			Logger.error( e);
		} finally {
			ps.release();
		}
		
	}
	
	public float getDamageAmount(int trains) {
		if (useRampAdd)
			return (amount + (amountRamp * trains));
		else
			return (amount * (1 + (amountRamp * trains)));
	}

	public boolean damageTypeSpecific() {
		
		return EffectsBase.EffectDamageTypes.containsKey(this.token);
		
	}

	public boolean containsDamageType(DamageType dt) {
		if (!EffectsBase.EffectDamageTypes.containsKey(this.token))
			return false;
		return EffectsBase.EffectDamageTypes.get(this.token).contains(dt);
	}

	private static DamageType getDamageType(String name) {
		try {
			switch (name) {
			case "Crushing":
				name = "Crush";
				break;
			case "Slashing":
				name = "Slash";
				break;
			case "Piercing":
				name = "Pierce";
				break;
			}
			if (name.isEmpty())
				return null;
			else
				return DamageType.valueOf(name);
		} catch (Exception e) {
			Logger.error(name);
			return null;
		}
	}

	// public String getName() {
	// return this.name;
	// }

	public int getUUID() {
		return this.UUID;
	}

	public String getIDString() {
		return this.IDString;
	}

	public int getToken() {
		return this.token;
	}

	public ConcurrentHashMap<String, Boolean> getSourceTypes() {
		return this.sourceTypes;
	}

	public HashSet<AbstractEffectModifier> getModifiers() {
		
		if (EffectsBase.modifiersMap.containsKey(this.IDString) == false)
			return EffectsBase.DefaultModifiers;
		
		return EffectsBase.modifiersMap.get(this.IDString);
	}

	public boolean isItemEffect() {
		return this.isItemEffect;
	}

	public boolean isSpireEffect() {
		return this.isSpireEffect;
	}

	public boolean ignoreMod() {
		return this.ignoreMod;
	}

	public boolean dontSave() {
		return this.dontSave;
	}

	public boolean isPrefix() {
		return this.isPrefix;
	}

	public boolean isSuffix() {
		return this.isSuffix;
	}

	public void startEffect(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {



		// Add SourceTypes for dispel
	
		if (this.token != 0) {
			if (effect == null) {
				Logger.error("AbstractEffectModifier.applyEffectModifier: missing FinishEffectTimeJob");
				return;
			}
			// AbstractWorldObject source = effect.getSource();
			if (source == null) {
				Logger.error( "AbstractEffectModifier.applyEffectModifier: missing source");
				return;
			}
			PowersBase pb = effect.getPower();
			if (pb == null) {
				Logger.error( "AbstractEffectModifier.applyEffectModifier: missing power");
				return;
			}
			ActionsBase ab = effect.getAction();
			if (ab == null) {
				Logger.error( "AbstractEffectModifier.applyEffectModifier: missing action");
				return;
			}

			//don't send effect if dead, except for death shroud
			if (!awo.isAlive()) {
				if (pb.getToken() != 1672601862)
					return;
			}


			if (!effect.skipSendEffect()) {
				//				float duration = (pb.isChant()) ? pb.getChantDuration() * 1000 : ab.getDuration(trains);
				float duration = ab.getDurationInSeconds(trains);
				if (pb.getToken() == 1672601862){

					Effect eff = awo.getEffects().get("DeathShroud");




					if (eff != null) {
						JobContainer jc = eff.getJobContainer();


						if (jc != null){
							duration = jc.timeOfExection() - System.currentTimeMillis();
							duration *= .001f;
						}
					}
				}
				
				
				
				if (duration > 0f) {
					int removeToken = this.token;
					ApplyEffectMsg pum = new ApplyEffectMsg();
					if (effect.getAction() != null)
					if ( effect.getAction().getPowerAction() != null
							&& PowersManager.ActionTokenByIDString.containsKey(effect.getAction().getPowerAction().getIDString()))
						try{
							removeToken = PowersManager.ActionTokenByIDString.get(effect.getAction().getPowerAction().getIDString());
						}catch(Exception e){
							removeToken = this.token;
						}
						
						pum.setEffectID(removeToken);
					pum.setSourceType(source.getObjectType().ordinal());
					pum.setSourceID(source.getObjectUUID());
					pum.setTargetType(awo.getObjectType().ordinal());
					pum.setTargetID(awo.getObjectUUID());
					pum.setNumTrains(trains);
					pum.setDuration((int) duration);
					//					pum.setDuration((pb.isChant()) ? (int)pb.getChantDuration() : ab.getDurationInSeconds(trains));
					pum.setPowerUsedID(pb.getToken());
					pum.setPowerUsedName(pb.getName());
					DispatchMessage.sendToAllInRange(awo, pum);
				}

				if (awo.getObjectType().equals(GameObjectType.Item)) {
					if (source.getCharItemManager() != null) {
						source.getCharItemManager().updateInventory();
					}
				}
			}

			// call modifiers to do their job
			if (!effect.skipApplyEffect()) {
				for (AbstractEffectModifier em : this.getModifiers())
					em.applyEffectModifier(source, awo, trains, effect);
			}
		}
	}

	// Send end effect message to client
	public void endEffect(AbstractWorldObject source, AbstractWorldObject awo, int trains, PowersBase pb, AbstractEffectJob effect) {
		if (awo == null) {
			Logger.error("endEffect(): Null AWO object passed in.");
			return;
		}
		if (pb == null) {
			Logger.error("endEffect(): Null PowerBase object passed in.");
			return;
		}
		if (!effect.skipCancelEffect() && !effect.isNoOverwrite()) {
			
			int sendToken = this.token;
			
			if (effect.getAction() != null)
			if ( effect.getAction().getPowerAction() != null
					&& PowersManager.ActionTokenByIDString.containsKey(effect.getAction().getPowerAction().getIDString()))
				try{
					sendToken = PowersManager.ActionTokenByIDString.get(effect.getAction().getPowerAction().getIDString());
				}catch(Exception e){
					sendToken = this.token;
				}
			ApplyEffectMsg pum = new ApplyEffectMsg();
			pum.setEffectID(sendToken);
			if (source != null) {
				pum.setSourceType(source.getObjectType().ordinal());
				pum.setSourceID(source.getObjectUUID());
			} else {
				pum.setSourceType(0);
				pum.setSourceID(0);
			}
			pum.setTargetType(awo.getObjectType().ordinal());
			pum.setTargetID(awo.getObjectUUID());
			pum.setUnknown02(2);
			pum.setNumTrains(0);
			pum.setDuration(-1);
			pum.setPowerUsedID(pb.getToken());
			pum.setPowerUsedName(pb.getName());
			DispatchMessage.sendToAllInRange(awo, pum);

		}
	}

	public void endEffectNoPower(int trains, AbstractEffectJob effect) {

		AbstractWorldObject source = effect.getSource();

		if (source == null)
			return;

		if (!effect.skipCancelEffect() && !effect.isNoOverwrite()) {
			ApplyEffectMsg pum = new ApplyEffectMsg();
			pum.setEffectID(this.token);

			pum.setSourceType(source.getObjectType().ordinal());
			pum.setSourceID(source.getObjectUUID());
			pum.setTargetType(source.getObjectType().ordinal());
			pum.setTargetID(source.getObjectUUID());
			pum.setUnknown02(2);
			pum.setNumTrains(0);
			pum.setDuration(-1);
			pum.setUnknown06((byte)1);
			pum.setEffectSourceType(effect.getEffectSourceType());
			pum.setEffectSourceID(effect.getEffectSourceID());
			pum.setPowerUsedID(0);
			pum.setPowerUsedName(this.name);

			if (source.getObjectType() == GameObjectType.PlayerCharacter){
				Dispatch dispatch = Dispatch.borrow((PlayerCharacter)source, pum);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
			}
		}
	}

	public void sendEffect(AbstractEffectJob effect, int duration, ClientConnection conn) {
		if (effect == null && conn != null)
			return;

		if (conn == null)
			return;
		AbstractWorldObject source = effect.getSource();
		AbstractWorldObject awo = effect.getTarget();
		int trains = effect.getTrains();
		if (source == null || awo == null)
			return;

		if (this.token != 0) {
			PowersBase pb = effect.getPower();
			if (pb == null) {
				Logger.error( "AbstractEffectModifier.applyEffectModifier: missing power");
				return;
			}
			ActionsBase ab = effect.getAction();
			if (ab == null) {
				Logger.error("AbstractEffectModifier.applyEffectModifier: missing action");
				return;
			}

			//don't send effect if dead, except for death shroud
			if (!awo.isAlive()) {
				if (pb.getToken() != 1672601862)
					return;
			}

			//duration for damage over times is (total time - (number of ticks x 5 seconds per tick))
			if (effect instanceof DamageOverTimeJob)
				duration = ((DamageOverTimeJob)effect).getTickLength();

			//			float dur = (pb.isChant()) ? pb.getChantDuration() * 1000 : ab.getDuration(trains);
			float dur = ab.getDuration(trains);
			if (dur > 0f) {
				ApplyEffectMsg pum = new ApplyEffectMsg();
				pum.setEffectID(this.token);
				pum.setSourceType(source.getObjectType().ordinal());
				pum.setSourceID(source.getObjectUUID());
				pum.setTargetType(awo.getObjectType().ordinal());
				pum.setTargetID(awo.getObjectUUID());
				pum.setNumTrains(trains);
				pum.setDuration(duration);
				pum.setPowerUsedID(pb.getToken());
				pum.setPowerUsedName(pb.getName());

				Dispatch dispatch = Dispatch.borrow(conn.getPlayerCharacter(), pum);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

			}
		}
	}

	public void sendEffectNoPower(AbstractEffectJob effect, int duration, ClientConnection conn) {

		if (effect == null && conn != null)
			return;

		if (conn == null)
			return;

		AbstractWorldObject source = effect.getSource();
		AbstractWorldObject awo = effect.getTarget();
		int trains = effect.getTrains();

		if (source == null || awo == null)
			return;

		if (this.token != 0) {
			//don't send effect if dead, except for death shroud
			if (!awo.isAlive()) {
				return;
			}

			//duration for damage over times is (total time - (number of ticks x 5 seconds per tick))
			if (effect instanceof DamageOverTimeJob)
				duration = ((DamageOverTimeJob)effect).getTickLength();
			else if (effect instanceof FinishSpireEffectJob)
				duration = 45;
			else if (effect instanceof NoTimeJob)
				duration = -1;

			//			float dur = (pb.isChant()) ? pb.getChantDuration() * 1000 : ab.getDuration(trains);

			ApplyEffectMsg pum = new ApplyEffectMsg();
			pum.setEffectID(this.token);
			pum.setSourceType(source.getObjectType().ordinal());
			pum.setSourceID(source.getObjectUUID());
			pum.setTargetType(source.getObjectType().ordinal());
			pum.setTargetID(source.getObjectUUID());
			pum.setUnknown06((byte)1);
			pum.setEffectSourceType(effect.getEffectSourceType());
			pum.setEffectSourceID(effect.getEffectSourceID());
			pum.setNumTrains(trains);
			pum.setDuration(duration);
			pum.setPowerUsedID(0);
			pum.setPowerUsedName(this.name);

			Dispatch dispatch = Dispatch.borrow(conn.getPlayerCharacter(), pum);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);

		}
	}

	public boolean containsSource(EffectSourceType sourceType) {
		if (EffectsBase.effectSourceTypeMap.containsKey(this.token) == false)
			return false;
		return EffectsBase.effectSourceTypeMap.get(this.token).contains(sourceType);
		
	}

	public boolean cancelOnAttack() {
		return this.cancelOnAttack;
	}

	public boolean cancelOnAttackSwing() {
		return this.cancelOnAttackSwing;
	}

	public boolean cancelOnCast() {
		return this.cancelOnCast;
	}

	public boolean cancelOnCastSpell() {
		return this.cancelOnCastSpell;
	}

	public boolean cancelOnEquipChange() {
		return this.cancelOnEquipChange;
	}

	public boolean cancelOnLogout() {
		return this.cancelOnLogout;
	}

	public boolean cancelOnMove() {
		return this.cancelOnMove;
	}

	public boolean cancelOnNewCharm() {
		return this.cancelOnNewCharm;
	}

	public boolean cancelOnSit() {
		return this.cancelOnSit;
	}

	public boolean cancelOnTakeDamage() {
		return this.cancelOnTakeDamage;
	}

	public boolean cancelOnTerritoryClaim() {
		return this.cancelOnTerritoryClaim;
	}

	public boolean cancelOnUnEquip() {
		return this.cancelOnUnEquip;
	}

	//For Debugging purposes.
	public void setToken(int token) {
		this.token = token;
	}

	public static String getItemEffectsByName(String string) {
		if (EffectsBase.itemEffectsByName.containsKey(string))
			return EffectsBase.itemEffectsByName.get(string);
		return "";
	}

	public static void addItemEffectsByName(String name, String ID) {
		EffectsBase.itemEffectsByName.put(name, ID);
	}

	public String getDamageTypes() {
		String text = "";
		if (!EffectsBase.EffectDamageTypes.containsKey(this.token))
			return text;
		for (DamageType type: EffectsBase.EffectDamageTypes.get(this.token)) {
			text += type.name() + ' ';
		}
		return text;
	}

	public String getName() {
		
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public float getValue() {
		return value;
	}
	
	public void setValue(float Value){
		this.value = Value;
	}
	public ConcurrentHashMap<ItemBase,Integer> getResourcesForEffect() {
		if (this.resourceCosts.isEmpty()){
			ArrayList<EffectsResourceCosts> effectsCostList = DbManager.EffectsResourceCostsQueries.GET_ALL_EFFECT_RESOURCES(this.IDString);
			for (EffectsResourceCosts erc : effectsCostList){
				this.resourceCosts.put(ItemBase.getItemBase(erc.getResourceID()), erc.getAmount());
			}
		}
		return this.resourceCosts;
	}


}
