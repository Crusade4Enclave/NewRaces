// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Heraldry  {

	public int playerUID;
	public int characterUUID;
	public int characterType;
	
	public static HashMap <Integer,HashMap<Integer,Integer>> HeraldyMap = new HashMap<>();

	/**
	 * ResultSet Constructor
	 */

	public Heraldry(ResultSet rs) throws SQLException {
		this.playerUID = rs.getInt("playerUID");
		this.characterUUID = rs.getInt("characterUID");
		this.characterType = rs.getInt("characterType");
		
		//cache player friends.
		//hashset already created, just add to set.
		if (HeraldyMap.containsKey(playerUID)){
			HashMap<Integer,Integer> playerHeraldySet = HeraldyMap.get(playerUID);
			playerHeraldySet.put(characterUUID,characterType);
			//hashset not yet created, create new set, and add to map.
		}else{
			HashMap<Integer,Integer> playerHeraldySet = new HashMap<>();
			playerHeraldySet.put(characterUUID,characterType);
			HeraldyMap.put(this.playerUID, playerHeraldySet);
		}
		
	}

	public Heraldry(int playerUID, int friendUID) {
		super();
		this.playerUID = playerUID;
		this.characterUUID = friendUID;
	}

	public int getPlayerUID() {
		return playerUID;
	}
	
	public static boolean AddToHeraldy(int playerID, AbstractWorldObject character){
		HashMap<Integer,Integer> characters = HeraldyMap.get(playerID);
		
		if (characters != null){
			//already in friends list, don't do anything.
			if (characters.containsKey(character.getObjectUUID()))
				return false;
			
			DbManager.PlayerCharacterQueries.ADD_HERALDY(playerID, character);
			characters.put(character.getObjectUUID(),character.getObjectType().ordinal());
		}else{
			characters = new HashMap<>();
			DbManager.PlayerCharacterQueries.ADD_HERALDY(playerID, character);
			characters.put(character.getObjectUUID(),character.getObjectType().ordinal());
			HeraldyMap.put(playerID, characters);
		}
		return true;
	}
	
	public static boolean RemoveFromHeraldy(int playerID, int characterID){
		
	if (!CanRemove(playerID, characterID))
		return false;
	
	HashMap<Integer,Integer> characters = HeraldyMap.get(playerID);
		
		if (characters != null){
			DbManager.PlayerCharacterQueries.REMOVE_HERALDY(playerID, characterID);
			characters.remove(characterID);
		}
		return true;
	}
	
	public static boolean CanRemove(int playerID, int toRemove){
		if (HeraldyMap.get(playerID) == null)
			return false;

		if (HeraldyMap.get(playerID).isEmpty())
			return false;

		if (!HeraldyMap.get(playerID).containsKey(toRemove))
			return false;

		return true;
	}

	public static void AuditHeraldry() {

		HashMap<Integer, Integer> characterMap;
		ArrayList<Integer> purgeList = new ArrayList<>();

		for (int playerID : Heraldry.HeraldyMap.keySet()) {

			characterMap = Heraldry.HeraldyMap.get(playerID);

			if (characterMap == null || characterMap.isEmpty())
				continue;

			// Loop through map adding deleted characters to our purge map

			purgeList.clear();

			for (int characterID : characterMap.keySet()) {

				int characterType = characterMap.get(characterID);

				if (characterType != GameObjectType.PlayerCharacter.ordinal())
					continue;

				// Player is deleted, add to purge list

				if (PlayerCharacter.getFromCache(characterID) == null)
					purgeList.add(characterID);

			}

			// Run purge

			for (int uuid : purgeList) {

				if (!Heraldry.RemoveFromHeraldy(playerID, uuid))
					continue;

				Logger.info("Removed Deleted Character ID " + uuid + " from PlayerID " + playerID + " heraldry.");

			}
		}
	}

	public static void ValidateHeraldry(int playerUUID) {
		
		HashMap<Integer,Integer> heraldryMap = Heraldry.HeraldyMap.get(playerUUID);
		
		if (heraldryMap == null || heraldryMap.isEmpty())
			return;
		
		for (int characterID : heraldryMap.keySet()){
			int characterType = heraldryMap.get(characterID);
			
			GameObjectType objectType = GameObjectType.values()[characterType];
			
			AbstractGameObject ago = DbManager.getFromCache(objectType, characterID);
			
			if (ago == null)
				heraldryMap.remove(characterID);
			
		}
	}
	
}
