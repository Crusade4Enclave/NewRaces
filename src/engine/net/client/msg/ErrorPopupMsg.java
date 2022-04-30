// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.net.*;
import engine.net.client.Protocol;
import engine.objects.PlayerCharacter;

public class ErrorPopupMsg extends ClientNetMsg {

	//1: Sorry, but that individual is not a banker
	//2: Sorry, but you must be closer to the banker to access your account
	//3: Sorry, but you have insufficient funds to access your acount
	//4: The shop is closed
	//5: You must come closer to shop
	//6: You do not have enough money to purchase that
	//7: You cannot carry that item
	//8: The banker cannot carry that item
	//9: You do not have that much gold to drop
	//10: That item cannot be dropped
	//11: You cannot drop what you do not have
	//12: Sorry, but this container is locked
	//13: Sorry, but this container is barred
	//14: You must come closer to me
	//15: You no longer have that item to sell
	//16: I won't buy that kind of item
	//17: I cannot afford that item
	//18: You can't really afford that
	//19: This item is gone from inventory
	//20: Your resurection has been declined
	//21: I cannot carry that weight
	//22: You just dropped that item on the ground
	//23: This corpse has no experience to return
	//24: This player is not in world
	//25: This player is not online
	//26: You selected an invalid location
	//27: You are dead. Try again when you are not so...well, dead
	//28: Your target is dead and cannot be summoned
	//29: Your summons has been declined
	//30: That person cannot carry that item
	//31: An unexpected error has occurred and the trade is being canceled
	//32: You must choose a promotion class before gaining your next level. Speak to a class trainer
	//33: Promotion failed
	//34: Come back when you've gained more experience
	//35: This hireling failed to buy the item
	//36: I don't buy items
	//37: I don't sell items
	//38: You appear to be in a building normally
	//39: There does not appear to be a building where you are
	//40: I don't swear guilds
	//41: I server no sovereign
	//42: Your guild is not errant
	//43: Members of your guild are too high in level
	//44: You cannot afford this service
	//45: Failure to swear guild
	//46: Cannot swear under ruins
	//47: Your guild is the wrong type to swear to this guild
	//48: Hireling could not hire
	//49: I do not hire
	//50: This pet is gone
	//51: You have successfully promoted to a new class
	//52: You have successfully added a new discipline
	//53: You no longer meet the level requirement to stay in this guild
	//54: That item is too advanced
	//55: All production slots are taken
	//56: That enchantment is too advanced
	//57: That formula is too advanced
	//58: The formula is beyond the means of this facility
	//59: This hireling does not have this formula
	//60: Hireling does not possess that item!
	//61: Hireling does not work with such items
	//62: You may only trade with items in your inventory
	//63: You must be within your building to do that
	//64: You are too far from the building to do that
	//65: I cannot enthrall creatures
	//66: I cannot repledge you
	//67: I cannot teleport you
	//68: You have no valid thralls
	//69: You have a thrall that I do not understand
	//70: I cannot afford your thrall
	//71: There are no cities to which you can repledge
	//72: There are no cities to which you can teleport
	//73: You are too low level to repledge
	//74: You are too low level to teleport
	//75: You must leave your current guild before you can repledge
	//76: Failure to repledge
	//77: Failure to teleport
	//78: You do not meet the qualifications to join that city
	//79: There are too many furniture items in this asset
	//80: Attempting to add furniture to bad location
	//81: This deed codes for a bad furniture prop
	//82: Object is not an appropriate furniture deed
	//83: Unable to find corresponding furniture on asset
	//84: The chose game world is not valid <- will kick out of world
	//85: The game world is temporarily unavailable <- will kick out of world
	//86: The runegate is unnaffected by this power
	//87: You are not powerful enough to activate this gate
	//88: This runegate is already on
	//89: You cannot unbanish this one until the timestamp expires
	//90: You cannot trade while either you or the target is invisible
	//91: You cannot trade while in combat mode
	//92: That person is already engaged in a trade
	//93: You must be closer to trade
	//94: The trade was successful
	//95: The trade was not successful
	//96: The trade has failed because one of you would exceed your gold limit
	//97: You must be closer to open that
	//98: You cannot loot while flying
	//99: Your target is not dead
	//100: You cannot loot a trainer
	//101: You cannot loot a shopkeeper
	//102: You cannot loot a banker
	//103: You cannot add this individual to the condemn list
	//104: You cannot add the owner's guild to the condemn list
	//105: You cannot add the owner's nation to the condemn list
	//106: Failure to add to the condemn list
	//107: Unable to find desired group
	//108: Group is at maximum membership
	//109: Failed to add item to hireling
	//110: Failer to remove item from hireling
	//111: This item cannot be removed from inventory
	//112: Your account has no characters
	//113: Failure to start support
	//114: Cannot add another support
	//115: This type of asset cannot receive protection
	//116: This asset already has protection upon it
	//117: Failure to remove support
	//118: Failure to complete support
	//119: Failure to reject support
	//120: This asset is not a banecircle
	//121: You are not a CSR who can advance banecircle stage
	//122: Failure to advance banecircle stage
	//123: You do not have the authority within your guild to modify this banecircle
	//124: Banecircle cannot advance once in final stage
	//125: Failure to repair Asset
	//126: Asset does not require repair
	//127: No gold in asset strongbox
	//128: Insufficient funds for even one point of repair
	//129: You cannot bond where you are killed-on-sight
	//130: You cannot join where you are killed-on-sight
	//131: You do not meet the level required for this SWORN guild
	//132: You are already a member of this guild
	//133: Your banishment from this guild has not yet been lifted
	//134: Your QUIT status from this guild has not yet expired
	//135: Character is considered BANISHED by guild leadership
	//136: Your class is not allowed to teleport here
	//137: You have no affiliation with this tree
	//138: You can never join this type of tree
	//139: You do not meet the safehold level requirement
	//140: Ruined trees are invalid
	//141: Unclaimed trees are invalid
	//142: You are the wrong race for this city
	//143: You are the wrong class for this city
	//144: You are the wrong sex for this city
	//145: You are too low level for this city
	//146: You do not meet the level requirements for this city
	//147: Tree must be rank 5 to open city
	//148: Unable to find a matching petition to complete guild creation
	//149: Guild name fails profanity check
	//150: Guild motto fails profanity check
	//151: Guild name is not unique
	//152: Guild crest is not unique
	//153: Guild crest is reserved
	//154: All three crest colors cannot be the same
	//155: Please choose another name
	//156: You cannot bank and trade at the same time
	//157: You must not move or engage in combat for 10 seconds before stuck will work
	//158: Your gold has been dropped on the ground
	//159: Merchant cannot purchase item without exceeding his reserve
	//160: Rune succesfully applied
	//161: You cannot apply that rune
	//162: You rely too heavily on that rune to remove it
	//163: This shrine does not take offerings of that type
	//164: This hireling cannot grant boons
	//165: This hireling cannot display the leaderboard
	//166: There is no more favor in this shrine to loot
	//167: There are no more resources in this warehouse to loot
	//168: This boon is only for guild members belonging to this shrine
	//169: You do not meet the race/class requirements for this boon
	//170: This shrine is no longer capable of granting boons
	//171: This asset cannot be destroyed during times of war
	//172: This shrine has no favor
	//173: You must be the leader of a guild to receive a blessing
	//174: This siege spire cannot be toggled yet. Please try again later
	//175: You cannot teleport into that zone at the moment
	//176: Only guild leaders can claim a territory
	//177: Your nation has already reached the maximum number of capitals
	//178: This territory is already claimed
	//179: Only landed guilds may claim a territory
	//180: This territory cannot be ruled by anyone
	//181: Your tree must be rank 7 before claiming a territory
	//182: This realm is in turmoil and cannot be claimed yet
	//183: You cannot rule a guild under a different faction then your parent guild
	//184: Insufficient gold or resources to upgrade to capital
	//185: You must seek the blessing of the three sages before you can rule
	//186: Your tree is not inside a territory!
	//187: This realm is in turmoil and cannot yet be claimed!
	//188: You must have a warehouse to become a capital
	//189: You are not the owner of this building
	//190: This building cannot be upgraded further
	//191: You don't have the required funds
	//192: This building is already upgrading
	//193: Production denied: This building must be protected to gain access to warehouse resources..
	//194: The operation failed because you reached your gold limit
	//195: That player is currently busy completing the last trade. Try again in a few moments
	//196: You are currently busy completing the last trade. Try again in a few moments
	//197: You cannot join a guild whose nation has a guild that is currently involved in a siege
	//198: You cannot repledge while you are involved in a siege
	//199: You already have this boon
	//200: Your vault cannot contain that item
	//201: You can't put that much gold there
	//202: You can't carry that much gold
	//203: You don't have that much gold to transfer
	//204: That item is not in the vault
	//205: That item is not in the inventory
	//206: This building can hold no more gold
	private int message;
	private String custom = "";

	/**
	 * This is the general purpose constructor.
	 */
	public ErrorPopupMsg(int message) {
		super(Protocol.STANDARDALERT);
		this.message = message;
	}

	public ErrorPopupMsg(int message, String custom) {
		super(Protocol.STANDARDALERT);
		this.message = message;
		this.custom = custom;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ErrorPopupMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.STANDARDALERT, origin, reader);
	}

	/**
	 * Copy constructor
	 */
	public ErrorPopupMsg(ErrorPopupMsg msg) {
		super(Protocol.STANDARDALERT);
		this.message = msg.message;
	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.message = reader.getInt();
		reader.getInt();
		reader.getInt();
	}

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.message);
		writer.putString(this.custom);
		writer.putInt(0);
	}

	/**
	 * @return unknown01
	 */

	// Popup Window with no title and arbitrary text.
	// Find an Enum for generic ERROR or way to set perhaps?

	public static void sendErrorMsg(PlayerCharacter player, String errorMessage) {

		if (player == null)
			return;


		ErrorPopupMsg popupMessage;
		Dispatch errorDispatch;

		popupMessage = new ErrorPopupMsg(300, errorMessage);

		errorDispatch = Dispatch.borrow(player, popupMessage);
		DispatchMessage.dispatchMsgDispatch(errorDispatch, Enum.DispatchChannel.SECONDARY);

	}

	public static void sendErrorPopup(PlayerCharacter player, int popupID) {

		ErrorPopupMsg errorPopup;
		Dispatch errorDispatch;

		if (player == null)
			return;

		errorPopup = new ErrorPopupMsg(popupID);

		errorDispatch = Dispatch.borrow(player, errorPopup);
		DispatchMessage.dispatchMsgDispatch(errorDispatch, Enum.DispatchChannel.SECONDARY);
	}
}
