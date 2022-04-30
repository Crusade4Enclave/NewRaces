// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.DispatchChannel;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;
import engine.net.DispatchMessage;
import engine.net.client.msg.ApplyRuneMsg;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CharacterRune extends AbstractGameObject {

	private final RuneBase runeBase;
	private final int player;
	private final ArrayList<SkillReq> skillsGranted;
	private final ArrayList<PowerReq> powersGranted;
	private final ArrayList<RuneBaseEffect> effectsGranted;

	/**
	 * No Table ID Constructor
	 */
	public CharacterRune(RuneBase runeBase, int characterID) {
		super();
		this.runeBase = runeBase;
		this.player = characterID;
		if (this.runeBase != null) {
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
			this.powersGranted = PowerReq.getPowerReqsForRune(this.runeBase.getObjectUUID());
		} else {
			this.skillsGranted = new ArrayList<>();
			this.powersGranted = new ArrayList<>();
		}
		if (this.runeBase != null)
			this.effectsGranted = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE(this.runeBase.getObjectUUID());
		else
			this.effectsGranted = new ArrayList<>();
	}

	/**
	 * Normal Constructor
	 */
	public CharacterRune(RuneBase runeBase, int characterID, int newUUID) {
		super(newUUID);
		this.runeBase = runeBase;
		this.player = characterID;
		if (this.runeBase != null) {
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
			this.powersGranted = PowerReq.getPowerReqsForRune(this.runeBase.getObjectUUID());
		} else {
			this.skillsGranted = new ArrayList<>();
			this.powersGranted = new ArrayList<>();
		}
		if (this.runeBase != null)
			this.effectsGranted = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE(this.runeBase.getObjectUUID());
		else
			this.effectsGranted = new ArrayList<>();
	}

	/**
	 * ResultSet Constructor
	 */
	public CharacterRune(ResultSet rs) throws SQLException {
		super(rs);

		this.runeBase = RuneBase.getRuneBase(rs.getInt("RuneBaseID"));
		this.player = rs.getInt("CharacterID");
		if (this.runeBase != null) {
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
			this.powersGranted = PowerReq.getPowerReqsForRune(this.runeBase.getObjectUUID());
			this.effectsGranted = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE(this.runeBase.getObjectUUID());
		} else {
			Logger.error("Failed to find RuneBase for CharacterRune " + this.getObjectUUID());
			this.skillsGranted = new ArrayList<>();
			this.powersGranted = new ArrayList<>();
			this.effectsGranted =  new ArrayList<>();
		}
	}

	/*
	 * Getters
	 */
	public RuneBase getRuneBase() {
		return this.runeBase;
	}

	public int getRuneBaseID() {
		if (this.runeBase != null)
			return this.runeBase.getObjectUUID();
		return 0;
	}

	public int getPlayerID() {
		return this.player;
	}

	public ArrayList<SkillReq> getSkillsGranted() {
		return this.skillsGranted;
	}

	public ArrayList<PowerReq> getPowersGranted() {
		return this.powersGranted;
	}

	public ArrayList<RuneBaseEffect> getEffectsGranted() {
		return this.effectsGranted;
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(CharacterRune characterRune, ByteBufferWriter writer) {
		if (characterRune.runeBase != null) {
			int idd = characterRune.runeBase.getObjectUUID();
			if (idd > 3000 && idd < 3050)
				writer.putInt(4);
			else
				writer.putInt(5);
			//			writer.putInt(this.runeBase.getMessageType());
			writer.putInt(0);
			writer.putInt(characterRune.runeBase.getObjectUUID());
			writer.putInt(characterRune.getObjectType().ordinal());
			writer.putInt(characterRune.getObjectUUID());
		} else {
			for (int i = 0; i < 5; i++)
				writer.putInt(0);
		}
	}

	public static boolean grantRune(PlayerCharacter pc, int runeID) {
		//Verify not too many runes
		ArrayList<CharacterRune> runes = pc.getRunes();
		boolean worked = false;
		synchronized (runes) {
			if (runes == null || runes.size() > 12)
				return false;

			//Verify player doesn't already have rune
			for (CharacterRune rune : runes) {
                RuneBase rb = rune.runeBase;
				if (rb == null || rb.getObjectUUID() == runeID)
					return false;
			}

			RuneBase rb = RuneBase.getRuneBase(runeID);
			if (rb == null)
				return false;

			//Attempt to add rune to database
			CharacterRune toAdd = new CharacterRune(rb, pc.getObjectUUID());
			CharacterRune rune = null;
			try {
				rune = DbManager.CharacterRuneQueries.ADD_CHARACTER_RUNE(toAdd);
			} catch (Exception e) {
				return false;
			}
			if (rune == null)
				return false;

			//attempt add rune to player
			worked = pc.addRune(rune);

			//worked, send ApplyRuneMsg
			if (worked) {
				ApplyRuneMsg arm = new ApplyRuneMsg(pc.getObjectType().ordinal(), pc.getObjectUUID(), runeID, rune.getObjectType().ordinal(), rune.getObjectUUID(), true);
				DispatchMessage.dispatchMsgToInterestArea(pc, arm, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
				CharacterSkill.calculateSkills(pc);
				pc.applyBonuses();
				return true;
			} else
				return false;
		}

	}

	public static boolean removeRune(PlayerCharacter pc, int runeID) {
		ArrayList<CharacterRune> runes = pc.getRunes();
		synchronized (runes) {
			for (CharacterRune rune : runes) {
                RuneBase rb = rune.runeBase;
				if (rb == null)
					continue;
				if (rb.getObjectUUID() == runeID && DbManager.CharacterRuneQueries.DELETE_CHARACTER_RUNE(rune)) {
					runes.remove(runes.indexOf(rune));
					CharacterSkill.calculateSkills(pc);
					pc.applyBonuses();
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void updateDatabase() {
		DbManager.CharacterRuneQueries.updateDatabase(this);
	}
}
