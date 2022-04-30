// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.gameManager.SessionManager;
import engine.net.*;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.Guild;
import engine.objects.GuildStatusController;
import engine.objects.PlayerCharacter;
import engine.objects.PromotionClass;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class WhoResponseMsg extends ClientNetMsg {

	private int unknown01;
	private static int worldPop;
	private  ArrayList<PlayerCharacter> members = new ArrayList<>();

	/**
	 * This is the general purpose constructor.
	 */
	public WhoResponseMsg() {
		super(Protocol.WHORESPONSE);
		this.unknown01 = 1;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public WhoResponseMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.WHORESPONSE, origin, reader);
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
    @Override
    protected void _serialize(ByteBufferWriter writer) {
        writer.putInt(unknown01);
        writer.putInt(worldPop);
            int size = this.members.size();
        writer.putInt(size);
        //PlayerCharacter pc : this.members
        for (PlayerCharacter pc: this.members) {
            writer.putInt(pc.getObjectType().ordinal());
            writer.putInt(pc.getObjectUUID());
            writer.putString(pc.getFirstName());
            writer.putString(pc.getLastName());
            writer.putInt(pc.getRaceToken());
            writer.putInt(pc.getClassToken());
            writer.putInt(pc.getLevel());
            writer.putInt(0); // unknown 0
            writer.putInt(pc.isMale() ? 1 : 2); //gender?
            writer.putInt(0); // unknown 0
            Guild guild = pc.getGuild();
            if (guild != null) {
                writer.put((byte) 1); // Send Guild Info
                writer.put((byte) 1); // SkipPartTwo
                writer.putString(guild.getName());
                writer.putInt(guild.getCharter()); // Charter Type
                writer.putInt(GuildStatusController.getTitle(pc.getGuildStatus()));
                writer.putString("what"); // City?, Skip if SkipPartTwo = 0x00
            } else {
                writer.put((byte) 0); // Don't Send guild info
                writer.put((byte) 0); // Don't send last string
            }
        }
    }

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.unknown01 = reader.getInt();
		WhoResponseMsg.worldPop = reader.getInt();
		// TODO implement Deserialization
	}

	/**
	 * @return the number of PlayerCharacters
	 */
	public int getSize() {
		return this.members.size();
	}

	/**
	 * @return the unknown01
	 */
	public int getUnknown01() {
		return unknown01;
	}

	/**
	 * @param unknown01
	 *            the unknown01 to set
	 */
	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	/**
	 * @return the worldPop
	 */
	public static int getWorldPop() {
		return WhoResponseMsg.worldPop;
	}

	/**
	 * @param worldPop
	 *            the worldPop to set
	 */
	public static void setWorldPop(int worldPop) {
		WhoResponseMsg.worldPop = worldPop;
	}

	public boolean addMember(PlayerCharacter pc) {
		if (this.members.size() > 100)
			return false;
		this.members.add(pc);
		return true;
	}

	public static void HandleResponse(int set, int filterType, String filter, ClientConnection origin) {

		WhoResponseMsg msg = new WhoResponseMsg();
		WhoResponseMsg.setWorldPop(SessionManager.getAllActivePlayerCharacters().size());

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(origin);

		if (playerCharacter != null) {
			//check threshold
			long currentTime = System.currentTimeMillis();
			long timestamp = playerCharacter.getTimeStamp("whoWindow");
			long dif = currentTime - timestamp;
			if (dif < MBServerStatics.WHO_WINDOW_THRESHOLD)
				return;
			playerCharacter.setTimeStamp("whoWindow", currentTime);
		}

		if (playerCharacter == null) {
		} else if (filterType == 0) { // No Filter, send everyone
			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (player.isActive())
						if (!isAdmin(player))
							if (!HandleSet(set, player, playerCharacter, msg))
								break;
		}

		else if (filterType == 1) { // Race Filter
			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (!isAdmin(player))
						if (player.isActive()) {
							String[] race = player.getRace().getName().split(",");
							if (filter.compareTo(race[0]) == 0)
								if (!HandleSet(set, player, playerCharacter, msg))
									break;
						}
		}

		else if (filterType == 2) { // Class Filter
			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (!isAdmin(player))
						if (player.isActive()) {
							if (filter.compareTo(player.getBaseClass().getName()) ==0 || (player.getPromotionClass() != null && filter.compareTo(player.getPromotionClass().getName()) == 0))
								if (!HandleSet(set, player, playerCharacter, msg))
									break;
							
							
							// TODO Promotion Class needs added to
							// PlayerCharacter
							// else if
							// (filter.compareTo(pc.getPromotionClass().getName())
							// == 0)
							// if (!HandleSet(set, pc, ori, msg))
							// break;
						}
		}

		else if (filterType == 3) { // Level Filter
			String range[] = filter.split(" ");
			int low;
			int high;

			try {
				low = Integer.parseInt(range[0]);
			} catch (NumberFormatException e) {
				low = 1;
				Logger.error("WhoResponseMsg: Low value in filter is not a proper integer. Defaulting to 1. Error = "
						+ e.getMessage());
			}

			try {
				high = Integer.parseInt(range[1]);
			} catch (NumberFormatException e) {
				high = 1;
				Logger.error(
						"WhoResponseMsg: High value in filter is not a proper integer. Defaulting to 75. Error = " + e.getMessage());
			}

			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (!isAdmin(player))
						if (player.isActive())
							if (player.getLevel() >= low && player.getLevel() <= high)
								if (!HandleSet(set, player, playerCharacter, msg))
									break;
		}

		else if (filterType == 4) { // Name Filter
			filter = filter.toLowerCase();
			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (!isAdmin(player))
						if (player.isActive())
							if (player.getName().toLowerCase().indexOf(filter) > -1)
								if (!HandleSet(set, player, playerCharacter, msg))
									break;
		}

		else if (filterType == 6) { // Status Filter
			int type = Integer.parseInt(filter);
			for (PlayerCharacter player : SessionManager.getAllActivePlayerCharacters())
				if (player != null)
					if (!isAdmin(player))
						if (player.isActive()) {
							if (type == 1) {
								if (player.isLFGroup())
									if (!HandleSet(set, player, playerCharacter, msg))
										break;
							} else if (type == 2) {
								if (player.isLFGuild())
									if (!HandleSet(set, player, playerCharacter, msg))
										break;
							} else if (type == 3) {
								if (player.isRecruiting())
									if (!HandleSet(set, player, playerCharacter, msg))
										break;
							} else
								break;
						}
		}

        Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

	}

	private static boolean HandleSet(int set, PlayerCharacter a, PlayerCharacter b, WhoResponseMsg msg) {
		// This function handles the sets. 0 = search all, 1 = search nation,
		// 2 = search guild. Returns true until 100 (max) players found
		
		if (set == 0) { // All
            return msg.addMember(a);
		} else if (set == 1) { // Nation
			if (compareNation(a, b))
                return msg.addMember(a);
		} else if (set == 2) { // Guild
			if (compareGuild(a, b))
                return msg.addMember(a);
		}
		return true;
	}

	private static boolean isAdmin(PlayerCharacter pc) {
		PromotionClass promo = pc.getPromotionClass();
		if (promo == null)
			return false;
        return promo.getObjectUUID() <= 2503 || promo.getObjectUUID() >= 2527;
    }

	private static boolean compareGuild(PlayerCharacter a, PlayerCharacter b) {
		if (a == null || b == null)
			return false;
        return Guild.sameGuild(a.getGuild(), b.getGuild());
    }

	private static boolean compareNation(PlayerCharacter a, PlayerCharacter b) {
		if (a == null || b == null)
			return false;
		Guild aG = a.getGuild();
		Guild bG = b.getGuild();
		if (aG == null || bG == null)
			return false;
        return (aG.getNation() == bG.getNation()) && aG.getNation() != null;
    }


	@Override
	protected int getPowerOfTwoBufferSize() {
		return 14;
	}

}
