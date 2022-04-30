// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.net.ByteBufferWriter;
import engine.net.client.msg.ErrorPopupMsg;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class CharacterPower extends AbstractGameObject {

	private final PowersBase power;
	private AtomicInteger trains = new AtomicInteger();
	private short grantedTrains;
	private int ownerUID;
	private boolean trained = false;
	private int requiredLevel = 0;




	/**
	 * No Table ID Constructor
	 */
	public CharacterPower(PowersBase power, PlayerCharacter pc) {
		super();
		this.power = power;
		this.trains.set(0);
        this.grantedTrains = this.grantedTrains;
		this.ownerUID = pc.getObjectUUID();

	}

	/**
	 * Normal Constructor
	 */
	public CharacterPower(PowersBase power, PlayerCharacter pc, int newUUID) {
		super(newUUID);
		this.power = power;
		this.trains.set(0);
        this.grantedTrains = this.grantedTrains;
		this.ownerUID = pc.getObjectUUID();


	}


	/**
	 * ResultSet Constructor
	 */
	public CharacterPower(ResultSet rs, PlayerCharacter pc) throws SQLException {
		super(rs);
		int powersBaseToken = rs.getInt("PowersBaseToken");
		this.power = PowersManager.getPowerByToken(powersBaseToken);
		
		if (this.power != null && this.power.isWeaponPower())
			this.trains.set(0);
		else
		this.trains.set(rs.getInt("trains"));
        this.grantedTrains = this.grantedTrains;
		this.ownerUID = pc.getObjectUUID();

	}

	public CharacterPower(ResultSet rs) throws SQLException {
		super(rs);
		int powersBaseToken = rs.getInt("PowersBaseToken");
		this.power = PowersManager.getPowerByToken(powersBaseToken);
		this.trains.set(rs.getInt("trains"));
        this.grantedTrains = this.grantedTrains;
		this.ownerUID = rs.getInt("CharacterID");

		//		this.owner = DbManager.PlayerCharacterQueries.GET_PLAYER_CHARACTER(rs.getInt("CharacterID"));
	}

	private short getGrantedTrains(PlayerCharacter pc) {
		if (this.power != null && pc != null) {
			//			if (this.power.isWeaponPower()) {
			//				SkillsBase sb = null;
			//				try {
			//					sb = SkillsBase.getSkillsBaseByName(this.power.getSkillName());
			//				} catch (SQLException e) {}
			//				if (sb != null) {
			//					return pc.getBonuses().getByte("gt." + sb.getToken());
			//				} else
			//					return pc.getBonuses().getByte("gt." + this.power.getToken());
			//			} else
			//				return pc.getBonuses().getByte("gt." + this.power.getToken());
			return PowerGrant.getGrantedTrains(this.power.getToken(), pc);
		}
		else
			return 0;
	}

	/*
	 * Getters
	 */
	public PowersBase getPower() {
		return power;
	}

	public int getPowerID() {
		return power.getUUID();
	}

	public boolean isTrained() {
		return trained;
	}

	public static PlayerCharacter getOwner(CharacterPower cp) {
		return PlayerCharacter.getFromCache(cp.ownerUID);
	}

	public void setTrained(boolean b) {
		trained = b;
	}

	public int getTrains() {
		return this.trains.get();
	}

	public short getGrantedTrains() {
		return this.grantedTrains;
	}

	public int getTotalTrains() {
		return (this.trains.get() + this.grantedTrains);
	}

	public float  getTrainingCost(PlayerCharacter pc, NPC trainer){
		int charLevel = pc.getLevel();
		int skillRank = this.trains.get() -1  + this.requiredLevel;


		float baseCost = 50 * this.requiredLevel ; //TODO GET BASE COSTS OF SKILLS.



		float sellPercent = -4f; //NOT SELL PERCENT!
		float cost;
		float const5;
		int const2 = 1;
		float const3 = 50;
		float const4 = const3 + const2;
		if (charLevel > 50)
			const5 = 50 / const4;
		else
			const5 = charLevel/const4;

		const5 = 1-const5;
		const5 = (float) (Math.log(const5) / Math.log(2) * .75f);
		float rounded5 = Math.round(const5);
		const5 = rounded5 - const5;

		const5 *= -1;

		const5 = (float) (Math.pow(2, const5) - 1);

		const5 +=1;
		const5 = Math.scalb(const5, (int) rounded5);
        const5 *= (charLevel - skillRank);
        const5 *= sellPercent;

		const5 = (float) (Math.log(const5) / Math.log(2) * 3);
		rounded5 = Math.round(const5);
		const5 = rounded5 - const5;
		const5 *= -1;
		const5 = (float) (Math.pow(2, const5) - 1);
		const5 +=1;


		const5 = Math.scalb(const5, (int) rounded5);
		const5 += 1;
		cost = const5 * baseCost;


		if (Float.isNaN(cost))
			cost = baseCost;
		return cost;
	}

	public synchronized boolean train(PlayerCharacter pc) {
		if (pc == null || this.power == null)
			return false;

		//see if any prereqs to train this power is met
		if (!canTrain(pc))
			return false;

		boolean succeeded=true;
		int oldTrains = this.trains.get();
		int tr = oldTrains + this.grantedTrains;
		if (pc.getTrainsAvailable() <= 0)
			return false;
		if (tr == this.power.getMaxTrains()) //at max, stop here
			return false;
		else if (tr > this.power.getMaxTrains()) //catch incase we somehow go over
			this.trains.set((this.power.getMaxTrains() - this.grantedTrains));
		else //add the train
			succeeded = this.trains.compareAndSet(oldTrains, oldTrains+1);

		if (this.trains.get() > this.power.getMaxTrains()) { //double check not over max trains
			this.trains.set(this.power.getMaxTrains());
			succeeded = false;
		}

		if (succeeded) {
			this.trained = true;

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			//subtract from trains available
			pc.modifyTrainsAvailable(-1);

			pc.calculateSkills();
			return true;
		} else
			return false;
	}
	
	public boolean reset(PlayerCharacter pc) {
		if (pc == null || this.power == null)
			return false;

		//see if any prereqs to refine this power is met
		
		boolean succeeded=true;
		int oldTrains = this.trains.get();
		int tr = oldTrains + this.grantedTrains;
		if (oldTrains < 1)
			return false;
		else //subtract the train
			succeeded = this.trains.compareAndSet(oldTrains, 0);
		if (succeeded) {
			this.trained = true;

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			//subtract from trains available
			pc.modifyTrainsAvailable(oldTrains);

			pc.calculateSkills();
			return true;
		} else
			return false;
	}

	public boolean refine(PlayerCharacter pc) {
		if (pc == null || this.power == null)
			return false;

		//see if any prereqs to refine this power is met
		if (!canRefine(pc))
			return false;

		boolean succeeded=true;
		int oldTrains = this.trains.get();
		int tr = oldTrains + this.grantedTrains;
		if (oldTrains < 1)
			return false;
		else //subtract the train
			succeeded = this.trains.compareAndSet(oldTrains, oldTrains-1);
		if (succeeded) {
			this.trained = true;

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			//subtract from trains available
			pc.modifyTrainsAvailable(1);

			pc.calculateSkills();
			return true;
		} else
			return false;
	}


	/*
	 * Utils
	 */

	/*
	 * This iterates through players runes and adds and removes powers as needed
	 * Don't Call this directly. Instead call pc.calculateSkills().
	 */
	public static void calculatePowers(PlayerCharacter pc) {
		if (pc == null)
			return;

		// First add powers that don't exist
		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		//		ArrayList<PowerReq> genericPowers = PowerReq.getPowerReqsForAll();
		//		CharacterPower.grantPowers(genericPowers, powers, pc);
		Race race = pc.getRace();
		if (race != null) {
			CharacterPower.grantPowers(race.getPowersGranted(), powers, pc);
		} else
			Logger.error( "Failed to find Race for player " + pc.getObjectUUID());
		BaseClass bc = pc.getBaseClass();
		if (bc != null) {
			CharacterPower.grantPowers(bc.getPowersGranted(), powers, pc);
		} else
			Logger.error( "Failed to find BaseClass for player " + pc.getObjectUUID());
		PromotionClass promo = pc.getPromotionClass();
		if (promo != null)
			CharacterPower.grantPowers(promo.getPowersGranted(), powers, pc);
		ArrayList<CharacterRune> runes = pc.getRunes();
		if (runes != null) {
			for (CharacterRune rune : runes) {
				CharacterPower.grantPowers(rune.getPowersGranted(), powers, pc);
			}
		} else
			Logger.error("Failed to find Runes list for player " + pc.getObjectUUID());

		// next remove any skills that no longer belong
		Iterator<Integer> it = powers.keySet().iterator();
		while (it.hasNext()) {
			Integer token = it.next();
			boolean valid = false;
			//			if (CharacterPower.powerAllowed(token, genericPowers, pc))
			//				continue;
			if (CharacterPower.powerAllowed(token, race.getPowersGranted(), pc))
				continue;
			if (CharacterPower.powerAllowed(token, bc.getPowersGranted(), pc))
				continue;
			if (promo != null)
				if (CharacterPower.powerAllowed(token, promo.getPowersGranted(), pc))
					continue;
			for (CharacterRune rune : runes) {
				if (CharacterPower.powerAllowed(token, rune.getPowersGranted(), pc)) {
					valid = true;
					continue;
				}
			}

			// if power doesn't belong to any runes or skill, then remove it
			if (!valid) {
				CharacterPower cp = powers.get(token);
				DbManager.CharacterPowerQueries.DELETE_CHARACTER_POWER(cp.getObjectUUID());
				it.remove();
			}
		}
	}

	/*
	 * This grants powers for specific runes
	 */
	private static void grantPowers(ArrayList<PowerReq> powersGranted, ConcurrentHashMap<Integer, CharacterPower> powers, PlayerCharacter pc) {
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();

		for (PowerReq powerreq : powersGranted) {
			PowersBase powersBase = powerreq.getPowersBase();

			if (powersBase == null)
				continue;
			// skip if player already has power
			if (powers.containsKey(powerreq.getToken())){
				CharacterPower cp = powers.get(powersBase.getToken());
                if (cp != null)
					if (cp.requiredLevel == 0) {
						cp.requiredLevel = (int) powerreq.getLevel();
					}

				continue;
			}

			// If player not high enough level for power, then skip
			if (pc.getLevel() < powerreq.getLevel())
				continue;

			// See if any prereq powers needed
			boolean valid = true;
			ConcurrentHashMap<Integer, Byte> preqs = powerreq.getPowerReqs();
			for (Integer tok : preqs.keySet()) {
				if (!powers.containsKey(tok))
					valid = false;
				else {
					CharacterPower cpp = powers.get(tok);
                    if ((cpp.getTrains() + cpp.grantedTrains) < preqs.get(tok))
						valid = false;
				}
			}
			if (!valid)
				continue;

			// See if any prereq skills needed
			preqs = powerreq.getSkillReqs();
			for (Integer tok : preqs.keySet()) {
				if (tok == 0)
					continue;
				CharacterSkill found = null;
				for (CharacterSkill sk : skills.values()) {
					if (sk.getToken() == tok) {
						found = sk;
						continue;
					}
				}
				if (found != null) {
					if (found.getModifiedAmountBeforeMods() < preqs.get(tok))
						valid = false;
				} else
					valid = false;
			}
			if (!valid)
				continue;


			if (!powers.containsKey(powersBase.getToken())) {
				CharacterPower newPower = new CharacterPower(powersBase, pc);
				CharacterPower cp = null;
				try {
					cp = DbManager.CharacterPowerQueries.ADD_CHARACTER_POWER(newPower);
				} catch (Exception e) {
					cp = null;
				}
				if (cp != null){
					cp.requiredLevel = (int) powerreq.getLevel();
					powers.put(powersBase.getToken(), cp);
				}

				else
					Logger.error("Failed to add CharacterPower to player " + pc.getObjectUUID());
			}else{
				CharacterPower cp = powers.get(powersBase.getToken());
                if (cp != null)
					if (cp.requiredLevel == 0) {
						cp.requiredLevel = (int) powerreq.getLevel();
					}
			}
		}
	}

	public static void grantTrains(PlayerCharacter pc) {
		if (pc == null)
			return;
		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		for (CharacterPower cp : powers.values()) {
			cp.grantedTrains = cp.getGrantedTrains(pc);
		}
	}

	/*
	 * This verifies if a power is valid for a players rune
	 */
	private static boolean powerAllowed(Integer token, ArrayList<PowerReq> powersGranted, PlayerCharacter pc) {
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		if (skills == null || powers == null)
			return false;
		for (PowerReq powerreq : powersGranted) {
			PowersBase pb = powerreq.getPowersBase();
			if (pb != null) {
				if (pb.getToken() == token) {

					//test level requirements
					if (powerreq.getLevel() > pc.getLevel()) {
						return false;
					}

					//test skill requirements are met
					ConcurrentHashMap<Integer, Byte> skillReqs = powerreq.getSkillReqs();
					for (int tok : skillReqs.keySet()) {
						boolean valid = false;
						if (tok == 0)
							continue;
						for (CharacterSkill skill : skills.values()) {
							if (skill.getToken() == tok) {
								if (skill.getModifiedAmountBeforeMods() < skillReqs.get(tok))
									return false;
								valid = true;
								break;
							}
						}
						if (!valid)
							return false;
					}

					//test power prerequisites are met
					ConcurrentHashMap<Integer, Byte> powerReqs = powerreq.getPowerReqs();
					for (int tok : powerReqs.keySet()) {
						if (!powers.containsKey(tok))
							return false;
						CharacterPower cp = powers.get(tok);
						if (cp.getTotalTrains() < powerReqs.get(tok))
							return false;
					}

					//everything passed. power is valid
					return true;
				}
			}
		}
		return false;
	}

	//This verifies the power is not blocked from refining by prereqs on other powers.
	private boolean canRefine(PlayerCharacter pc) {
		if (this.power == null || pc == null)
			return false;

		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		Race race = pc.getRace();
		if (race != null) {
			if (!canRefine(race.getPowersGranted(), powers, pc))
				return false;
		} else
			return false;
		BaseClass bc = pc.getBaseClass();
		if (bc != null) {
			if (!canRefine(bc.getPowersGranted(), powers, pc))
				return false;
		} else
			return false;
		PromotionClass promo = pc.getPromotionClass();
		if (promo != null)
			if (!canRefine(promo.getPowersGranted(), powers, pc))
				return false;
		ArrayList<CharacterRune> runes = pc.getRunes();
		if (runes != null) {
			for (CharacterRune rune : runes) {
				if (!canRefine(rune.getPowersGranted(), powers, pc))
					return false;
			}
		}

		//all tests passed. Can refine
		return true;
	}

	private boolean canRefine(ArrayList<PowerReq> powersGranted, ConcurrentHashMap<Integer, CharacterPower> powers, PlayerCharacter pc) {
		for (PowerReq pr : powersGranted) {
			ConcurrentHashMap<Integer, Byte> powerReqs = pr.getPowerReqs();
			for (int token : powerReqs.keySet()) {
				if (token == this.power.getToken()) {
					//this is a prereq, find the power and make sure it has enough trains
					int trainsReq = (int)powerReqs.get(token);
					for (CharacterPower cp : powers.values()) {
                        if (cp.power.getToken() == pr.getToken()) {
							if (this.getTotalTrains() <= trainsReq && cp.getTrains() > 0) {
                                ErrorPopupMsg.sendErrorMsg(pc, "You must refine " + cp.power.getName() + " to 0 before refining any more from this power.");
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	private boolean canTrain(PlayerCharacter pc) {
		if (this.power == null || pc == null)
			return false;
		int token = this.power.getToken();
		boolean valid = false;
		Race race = pc.getRace();
		if (race != null) {
			if (CharacterPower.powerAllowed(token, race.getPowersGranted(), pc))
				return true;
		} else
			return false;
		BaseClass bc = pc.getBaseClass();
		if (bc != null) {
			if (CharacterPower.powerAllowed(token, bc.getPowersGranted(), pc))
				return true;
		} else
			return false;
		PromotionClass promo = pc.getPromotionClass();
		if (promo != null)
			if (CharacterPower.powerAllowed(token, promo.getPowersGranted(), pc))
				return true;
		ArrayList<CharacterRune> runes = pc.getRunes();
		for (CharacterRune rune : runes)
			if (CharacterPower.powerAllowed(token, rune.getPowersGranted(), pc))
				return true;
		return false;
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(CharacterPower characterPower, ByteBufferWriter writer) {
		if (characterPower.power != null)
			writer.putInt(characterPower.power.getToken());
		else
			writer.putInt(0);
		writer.putInt(characterPower.getTrains());
	}

	public static CharacterPower getPower(int tableId) {
		return DbManager.CharacterPowerQueries.GET_CHARACTER_POWER(tableId);
	}

	@Override
	public void updateDatabase() {
		DbManager.CharacterPowerQueries.updateDatabase(this);
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}

	public void setRequiredLevel(int requiredLevel) {
		this.requiredLevel = requiredLevel;
	}
}
