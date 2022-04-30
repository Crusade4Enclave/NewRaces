// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net;

import engine.Enum.DispatchChannel;
import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.net.client.ClientConnection;
import engine.objects.AbstractWorldObject;
import engine.objects.Item;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.HashSet;

import static engine.net.MessageDispatcher.dispatchCount;
import static engine.net.MessageDispatcher.maxRecipients;

/*
 * Dispatch Message is the main interface to Magicbane's threaded
 * asynch message delivery system.
 */

public class DispatchMessage {

	public static void startMessagePump() {

		Thread messageDispatcher;
		messageDispatcher = new Thread(new MessageDispatcher());

		messageDispatcher.setName("MessageDispatcher");
		messageDispatcher.start();
	}


	public static void sendToAllInRange(AbstractWorldObject obj,
			AbstractNetMsg msg){

		if (obj == null)
			return;

		if (obj.getObjectType() ==  GameObjectType.PlayerCharacter || obj.getObjectType() ==  GameObjectType.Mob || obj.getObjectType() == GameObjectType.NPC || obj.getObjectType() ==  GameObjectType.Corpse)
			dispatchMsgToInterestArea(obj, msg, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		else
			dispatchMsgToInterestArea(obj ,msg, DispatchChannel.PRIMARY, MBServerStatics.STRUCTURE_LOAD_RANGE, false, false);

	}

	// Dispatches a message to a playercharacter's interest area
	// Method includes handling of exclusion rules for visibility, self, etc.

	public static void dispatchMsgToInterestArea(AbstractWorldObject sourceObject, AbstractNetMsg msg, DispatchChannel dispatchChannel, int interestRange, boolean sendToSelf, boolean useIgnore) {

		Dispatch messageDispatch;
		HashSet<AbstractWorldObject> gridList;
		PlayerCharacter gridPlayer;
		AbstractWorldObject dispatchSource;
		PlayerCharacter sourcePlayer = null;
		long recipientCount = 0;

		if (sourceObject == null)
			return;

		// If the source of the message is a structure, item or player
		// setup our method variables accordingly.

		switch (sourceObject.getObjectType()) {
		case Item:
			dispatchSource = (AbstractWorldObject) ((Item) sourceObject).getOwner();
			break;
		case PlayerCharacter:
			dispatchSource = sourceObject;
			sourcePlayer = (PlayerCharacter)sourceObject;
			if (sourcePlayer.getClientConnection() != null && sendToSelf){
				Dispatch dispatch = Dispatch.borrow(sourcePlayer, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
			}


			break;
		default:
			dispatchSource = sourceObject;
		}

		gridList = WorldGrid.getObjectsInRangePartial(dispatchSource.getLoc(), interestRange, MBServerStatics.MASK_PLAYER);

		for (AbstractWorldObject gridObject : gridList) {

			gridPlayer = (PlayerCharacter)gridObject;

			// Apply filter options if source of dispatch is a player

			if ((dispatchSource.getObjectType() == GameObjectType.PlayerCharacter) &&
					(sourcePlayer != null)) {

				if (gridPlayer.getObjectUUID() == sourcePlayer.getObjectUUID())
					continue;

				if ((useIgnore == true) && (gridPlayer.isIgnoringPlayer(sourcePlayer) == true))
					continue;

				if(gridPlayer.canSee(sourcePlayer) == false)
					continue;
			}

			messageDispatch = Dispatch.borrow(gridPlayer, msg);
			MessageDispatcher.send(messageDispatch, dispatchChannel);
			recipientCount++;
		}

		// Update metrics

		if (recipientCount > maxRecipients[dispatchChannel.getChannelID()])
			maxRecipients[dispatchChannel.getChannelID()] = recipientCount;

		dispatchCount[dispatchChannel.getChannelID()].increment();
	}

	public static void dispatchMsgToInterestArea(Vector3fImmutable targetLoc,AbstractWorldObject sourceObject, AbstractNetMsg msg, DispatchChannel dispatchChannel, int interestRange, boolean sendToSelf, boolean useIgnore) {

		Dispatch messageDispatch;
		HashSet<AbstractWorldObject> gridList;
		PlayerCharacter gridPlayer;
		AbstractWorldObject dispatchSource;
		PlayerCharacter sourcePlayer = null;
		long recipientCount = 0;

		if (sourceObject == null)
			return;

		// If the source of the message is a structure, item or player
		// setup our method variables accordingly.

		switch (sourceObject.getObjectType()) {
		case Item:
			dispatchSource = (AbstractWorldObject) ((Item) sourceObject).getOwner();
			break;
		case PlayerCharacter:
			dispatchSource = sourceObject;
			sourcePlayer = (PlayerCharacter)sourceObject;

			if (sourcePlayer.getClientConnection() != null && sendToSelf){
				Dispatch dispatch = Dispatch.borrow(sourcePlayer, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.PRIMARY);
			}


			break;
		default:
			dispatchSource = sourceObject;
		}

		gridList = WorldGrid.getObjectsInRangePartial(targetLoc, interestRange, MBServerStatics.MASK_PLAYER);

		for (AbstractWorldObject gridObject : gridList) {

			gridPlayer = (PlayerCharacter)gridObject;

			// Apply filter options if source of dispatch is a player

			if ((dispatchSource.getObjectType() == GameObjectType.PlayerCharacter) &&
					(sourcePlayer != null)) {

				if (gridPlayer.getObjectUUID() == sourcePlayer.getObjectUUID())
					continue;

				if ((useIgnore == true) && (gridPlayer.isIgnoringPlayer(sourcePlayer) == true))
					continue;

				if(gridPlayer.canSee(sourcePlayer) == false)
					continue;
			}

			messageDispatch = Dispatch.borrow(gridPlayer, msg);
			MessageDispatcher.send(messageDispatch, dispatchChannel);
			recipientCount++;
		}

		// Update metrics

		if (recipientCount > maxRecipients[dispatchChannel.getChannelID()])
			maxRecipients[dispatchChannel.getChannelID()] = recipientCount;

		dispatchCount[dispatchChannel.getChannelID()].increment();
	}

	// Sends a message to all players in the game

	public static void dispatchMsgToAll(AbstractNetMsg msg) {

		Dispatch messageDispatch;
		long recipientCount = 0;

		// Send message to nobody?  No thanks!

		if (SessionManager.getAllActivePlayerCharacters().isEmpty())
			return;

		// Messages to all we will default to the secondary dispatch
		// delivery channel.  They are generally large, or inconsequential.

		for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters()) {
			messageDispatch = Dispatch.borrow(player, msg);
			MessageDispatcher.send(messageDispatch, DispatchChannel.SECONDARY);
			recipientCount++;
		}

		// Update metrics

		if (recipientCount > maxRecipients[DispatchChannel.SECONDARY.getChannelID()])
			maxRecipients[DispatchChannel.SECONDARY.getChannelID()] = recipientCount;

		dispatchCount[DispatchChannel.SECONDARY.getChannelID()].increment();

	}
	
	public static void dispatchMsgToAll(PlayerCharacter source, AbstractNetMsg msg, boolean ignore) {

		Dispatch messageDispatch;
		long recipientCount = 0;

		// Send message to nobody?  No thanks!

		if (SessionManager.getAllActivePlayerCharacters().isEmpty())
			return;

		// Messages to all we will default to the secondary dispatch
		// delivery channel.  They are generally large, or inconsequential.

		for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters()) {
			
			if (ignore && player.isIgnoringPlayer(source))
				continue;
			
			messageDispatch = Dispatch.borrow(player, msg);
			MessageDispatcher.send(messageDispatch, DispatchChannel.SECONDARY);
			recipientCount++;
		}

		// Update metrics

		if (recipientCount > maxRecipients[DispatchChannel.SECONDARY.getChannelID()])
			maxRecipients[DispatchChannel.SECONDARY.getChannelID()] = recipientCount;

		dispatchCount[DispatchChannel.SECONDARY.getChannelID()].increment();

	}

	// Sends a message to an arbitrary distribution list

	public static void dispatchMsgDispatch(Dispatch messageDispatch, DispatchChannel dispatchChannel) {
		
		if (messageDispatch == null){
			Logger.info("DISPATCH Null for DispatchMessage!");
			return;
		}

		// No need to serialize an empty list

		if (messageDispatch.player == null){
			Logger.info("Player Null for Dispatch!");
			messageDispatch.release();
			return;
		}
			

		MessageDispatcher.send(messageDispatch, dispatchChannel);

		dispatchCount[dispatchChannel.getChannelID()].increment();

	}

	protected static void serializeDispatch(Dispatch messageDispatch) {
		ClientConnection connection;

		if (messageDispatch.player == null){
			Logger.info("Player null in serializeDispatch");
			messageDispatch.release();
			return;
		}

		connection = messageDispatch.player.getClientConnection();

		if ((connection == null) || (connection.isConnected() == false)) {
			messageDispatch.release();
			return;
		}

		if (messageDispatch.msg == null) {
			Logger.error("null message sent to " + messageDispatch.player.getName());
			messageDispatch.release();
			return;
		}

		if (!connection.sendMsg(messageDispatch.msg))
			Logger.error(messageDispatch.msg.getProtocolMsg() + " failed sending to " + messageDispatch.player.getName());

		messageDispatch.release();
	}


}
