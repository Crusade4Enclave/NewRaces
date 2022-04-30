// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.objects.CharacterRune;

import java.util.ArrayList;

public class dbCharacterRuneHandler extends dbHandlerBase {

	public dbCharacterRuneHandler() {
		this.localClass = CharacterRune.class;
		this.localObjectType = Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public CharacterRune ADD_CHARACTER_RUNE(final CharacterRune toAdd) {
		prepareCallable("INSERT INTO `dyn_character_rune` (`CharacterID`, `RuneBaseID`) VALUES (?, ?);");
		setLong(1, (long)toAdd.getPlayerID());
		setInt(2, toAdd.getRuneBaseID());
		int runeID = insertGetUUID();
		return GET_CHARACTER_RUNE(runeID);
	}

	public CharacterRune GET_CHARACTER_RUNE(int runeID) {

		CharacterRune charRune = (CharacterRune) DbManager.getFromCache(Enum.GameObjectType.CharacterRune, runeID);
		if (charRune != null)
			return charRune;
		prepareCallable("SELECT * FROM `dyn_character_rune` WHERE `UID`=?");
		setInt(1, runeID);
		return (CharacterRune) getObjectSingle(runeID);
	}


	public boolean DELETE_CHARACTER_RUNE(final CharacterRune cr) {
		prepareCallable("DELETE FROM `dyn_character_rune` WHERE `UID`=?;");
		setLong(1, (long)cr.getObjectUUID());
		return (executeUpdate() != 0);
	}

	public ArrayList<CharacterRune> GET_RUNES_FOR_CHARACTER(final int characterId) {
		prepareCallable("SELECT * FROM `dyn_character_rune` WHERE `CharacterID` = ?");
		setInt(1, characterId);
		return getObjectList();
	}

	public void updateDatabase(final CharacterRune cr) {
		prepareCallable("UPDATE `dyn_character_rune` SET `CharacterID`=?, `RuneBaseID`=? WHERE `UID` = ?");
		setInt(1, cr.getPlayerID());
		setInt(2, cr.getRuneBaseID());
		setLong(3, (long) cr.getObjectUUID());
		executeUpdate();
	}
}
