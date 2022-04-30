// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class NPCRune extends AbstractGameObject {

	private final RuneBase runeBase;
	private final int player;
	private final ArrayList<SkillReq> skillsGranted;
	private final ArrayList<RuneBaseEffect> effectsGranted;

	/**
	 * No Table ID Constructor
	 */
	public NPCRune(RuneBase runeBase, int characterID) {
		super();
		this.runeBase = runeBase;
		this.player = characterID;
		if (this.runeBase != null)
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
		else
			this.skillsGranted = new ArrayList<>();
		this.effectsGranted = new ArrayList<>();
	}

	/**
	 * Normal Constructor
	 */
	public NPCRune(RuneBase runeBase, int characterID, int newUUID) {
		super(newUUID);
		this.runeBase = runeBase;
		this.player = characterID;
		if (this.runeBase == null)
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
		else
			this.skillsGranted = new ArrayList<>();
		this.effectsGranted = new ArrayList<>();
	}
	/**
	 * ResultSet Constructor
	 */
	public NPCRune(ResultSet rs) throws SQLException {
		super(rs);

		this.runeBase = RuneBase.getRuneBase(rs.getInt("RuneBaseID"));
		this.player = rs.getInt("NpcID");
		if (this.runeBase != null) {
			this.skillsGranted = DbManager.SkillReqQueries.GET_REQS_FOR_RUNE(this.runeBase.getObjectUUID());
			this.effectsGranted = DbManager.RuneBaseEffectQueries.GET_EFFECTS_FOR_RUNEBASE(this.runeBase.getObjectUUID());
		} else {
			Logger.error("Failed to find RuneBase for NPCRune " + this.getObjectUUID());
			this.skillsGranted = new ArrayList<>();
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

	public ArrayList<RuneBaseEffect> getEffectsGranted() {
		return this.effectsGranted;
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(NPCRune npcRune,ByteBufferWriter writer) {
		if (npcRune.runeBase != null) {
			writer.putInt(npcRune.runeBase.getType());
			writer.putInt(0);
			writer.putInt(npcRune.runeBase.getObjectUUID());
			writer.putInt(npcRune.getObjectType().ordinal());
			writer.putInt(npcRune.getObjectUUID());
		} else {
			for (int i = 0; i < 5; i++)
				writer.putInt(0);
		}
	}

	@Override
	public void updateDatabase() {

	}
}
