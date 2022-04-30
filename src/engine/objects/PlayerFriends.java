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
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.UpdateFriendStatusMessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class PlayerFriends  {

	public int playerUID;
	public int friendUID;
	
	public static HashMap <Integer,HashSet<Integer>> PlayerFriendsMap = new HashMap<>();

	/**
	 * ResultSet Constructor
	 */

	public PlayerFriends(ResultSet rs) throws SQLException {
		this.playerUID = rs.getInt("playerUID");
		this.friendUID = rs.getInt("friendUID");
		
		//cache player friends.
		//hashset already created, just add to set.
		if (PlayerFriendsMap.containsKey(playerUID)){
			HashSet<Integer> playerFriendSet = PlayerFriendsMap.get(playerUID);
			playerFriendSet.add(friendUID);
			//hashset not yet created, create new set, and add to map.
		}else{
			HashSet<Integer> playerFriendSet = new HashSet<>();
			playerFriendSet.add(friendUID);
			PlayerFriendsMap.put(this.playerUID, playerFriendSet);
		}
		
	}

	public PlayerFriends(int playerUID, int friendUID) {
		super();
		this.playerUID = playerUID;
		this.friendUID = friendUID;
	}

	public int getPlayerUID() {
		return playerUID;
	}
	
	public static void AddToFriends(int playerID, int friendID){
		HashSet<Integer> friends = PlayerFriendsMap.get(playerID);
		
		if (friends != null){
			//already in friends list, don't do anything.
			if (friends.contains(friendID))
				return;
			
			DbManager.PlayerCharacterQueries.ADD_FRIEND(playerID, friendID);
			friends.add(friendID);
		}else{
			friends = new HashSet<>();
			DbManager.PlayerCharacterQueries.ADD_FRIEND(playerID, friendID);
			friends.add(friendID);
			PlayerFriendsMap.put(playerID, friends);
		}
	}
	
	public static void RemoveFromFriends(int playerID, int friendID){
		
	if (!CanRemove(playerID, friendID))
		return;
	
HashSet<Integer> friends = PlayerFriendsMap.get(playerID);
		
		if (friends != null){
			DbManager.PlayerCharacterQueries.REMOVE_FRIEND(playerID, friendID);
			friends.remove(friendID);
		}
	}
	
	public static boolean CanRemove(int playerID, int toRemove){
		if (PlayerFriendsMap.get(playerID) == null)
			return false;
		
		if (PlayerFriendsMap.get(playerID).isEmpty())
			return false;
		
		if (!PlayerFriendsMap.get(playerID).contains(toRemove))
			return false;
		
		return true;
	}
	
	public static void SendFriendsStatus(PlayerCharacter player, boolean online ){
		HashSet<Integer> friendsSet = PlayerFriends.PlayerFriendsMap.get(player.getObjectUUID());
		if (friendsSet != null){
			for(int friendID: friendsSet){
				PlayerCharacter friend = SessionManager.getPlayerCharacterByID(friendID);
				if (friend == null)
					continue;
				UpdateFriendStatusMessage outMsg = new UpdateFriendStatusMessage(player);
				outMsg.online = online;
				Dispatch dispatch = Dispatch.borrow(friend, outMsg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			}
		}

	}
}
