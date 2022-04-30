// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.DispatchChannel;
import engine.Enum.GuildHistoryType;
import engine.exception.MsgSendException;
import engine.gameManager.BuildingManager;
import engine.gameManager.DbManager;
import engine.gameManager.GuildManager;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.net.*;
import engine.net.client.ClientConnection;
import engine.net.client.Protocol;
import engine.objects.*;
import engine.server.MBServerStatics;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class VendorDialogMsg extends ClientNetMsg {

	public static final int MSG_TYPE_VENDOR = 0;
	public static final int MSG_TYPE_TRAINER = 1;
	public static int cnt = 1;
	private int messageType;
	private String language;
	private int vendorObjectType;
	private int vendorObjectID;
	private int unknown01;
	private int unknown02;
	private int unknown03;
	private int unknown04;
	private String dialogType = "TrainerDialog";
	private String intro = "FighterIntro";
	private String introCode = " [ FighterIntro ] ";
	private String merchantCode = " [ Merchant options ] ";
	private int menuType = 1; // 0: close, 1: normal, 2: train, 3: untrain
	private VendorDialog vd;

	/**
	 * This is the general purpose constructor.
	 */
	public VendorDialogMsg(int messageType, int vendorObjectType, int vendorObjectID, String dialogType, String intro, int menuType) {
		super(Protocol.VENDORDIALOG);
		this.messageType = messageType;
		this.vendorObjectType = vendorObjectType;
		this.vendorObjectID = vendorObjectID;
		this.dialogType = dialogType;
		this.intro = intro;
		this.introCode = " [ " + intro + " ] ";
		this.menuType = menuType;
	}


	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public VendorDialogMsg(AbstractConnection origin, ByteBufferReader reader)  {
		super(Protocol.VENDORDIALOG, origin, reader);
	}

	public static void replyDialog(VendorDialogMsg msg, ClientConnection origin) throws MsgSendException {

		PlayerCharacter playerCharacter = SessionManager.getPlayerCharacter(origin);

		if (playerCharacter == null)
			return;

		if (playerCharacter.getTimeStamp("lastvendorwindow") > System.currentTimeMillis()) {
			return;
		}

		// Get NPC that player is talking to
		NPC npc = NPC.getFromCache(msg.vendorObjectID);
		int npcClassID;

		if (npc == null)
			return;

		// test within talking range

		if (playerCharacter.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
			ErrorPopupMsg.sendErrorPopup(playerCharacter, 14);
			return;
		}

		// Restrict disc trainers to only characters who have
		// tht disc applied.

		npcClassID = npc.getContract().getClassID();

		if  (npc.getContract() != null &&
				ApplyRuneMsg.isDiscipline(npcClassID)) {

			if (playerCharacter.getRune(npcClassID) == null) {
				ErrorPopupMsg.sendErrorPopup(playerCharacter, 49);
				return;
			}

		}
		playerCharacter.setLastNPCDialog(npc);

		VendorDialog vd = null;
		Contract contract = npc.getContract();

		if (contract == null)
			vd = VendorDialog.getHostileVendorDialog();
		else if (npc.getBuilding() != null) {
			if (BuildingManager.IsPlayerHostile(npc.getBuilding(), playerCharacter))
				vd = VendorDialog.getHostileVendorDialog();
			else vd = contract.getVendorDialog();
		}
		else
			vd = contract.getVendorDialog();
		if (vd == null)
			vd = VendorDialog.getHostileVendorDialog();

		if (msg.messageType == 1 || msg.unknown03 == vd.getObjectUUID()) {
			msg.updateMessage(3, vd);
		}
		else {
			if (VendorDialogMsg.handleSpecialCase(msg, npc, playerCharacter, vd, origin))
				return;

			vd = VendorDialog.getVendorDialog(msg.unknown03);
			msg.updateMessage(3, vd);
		}

		Dispatch dispatch = Dispatch.borrow(playerCharacter, msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	//	protected void serializeMerchantMenu(ByteBufferWriter writer) {
	//		writer.putInt(2);
	//		writer.putInt(14);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.put((byte) 0);
	//		writer.put((byte) 1);
	//		writer.putString(" [ Merchant options ] ");
	//		for (int i = 0; i < 4; i++)
	//			writer.putInt(0);
	//		writer.putInt(10);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.put((byte) 0);
	//		writer.put((byte) 1);
	//		writer.putString("Done");
	//		for (int i = 0; i < 8; i++)
	//			writer.putInt(0);
	//	}

	//	protected void serializeForTrain(ByteBufferWriter writer, boolean train) {
	//		writer.putInt(0);
	//		writer.putInt(0x364AF0D0); // 0x325695C0
	//		writer.putInt(this.unknown03);
	//		writer.putInt(1);
	//		writer.put((byte) 0);
	//		writer.putInt(this.unknown03);
	//		writer.put((byte) 0);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.putInt(2); // 2
	//		if (train)
	//			writer.putInt(3); // 3=buy/sell/trade, 4=untrain, 5 closes
	//		else
	//			writer.putInt(4); //refine
	//		writer.putInt(10); // 10
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//	}

	//	protected void serializeClose(ByteBufferWriter writer) {
	//		writer.putInt(0);
	//		writer.putInt(0x364AF0D0); // 0x325695C0
	//		writer.putInt(this.unknown03);
	//		writer.putInt(1);
	//		writer.put((byte) 0);
	//		writer.putInt(this.unknown03);
	//		writer.put((byte) 0);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.putInt(2); // 2
	//		writer.putInt(5); // 3=buy/sell/trade, 4=untrain, 5 closes
	//		writer.putInt(10); // 10
	//		writer.putInt(0);
	//		writer.putInt(0);
	//		writer.putInt(0);
	//	}

	// Handles special case menu selections, such as promote, train, ect.
	private static boolean handleSpecialCase(VendorDialogMsg msg, NPC npc, PlayerCharacter playerCharacter, VendorDialog vd, ClientConnection origin)
			throws MsgSendException {

		Dispatch dispatch;
		int menuID = msg.unknown03; // aka menuoptions.optionID
		Vector3fImmutable loc;

		switch (menuID) {
		case 0: // Close Dialog
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 180: // Promote to class
			VendorDialogMsg.promote(playerCharacter, npc);
			msg.updateMessage(4, 0); // <-0 closes dialog
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 1000: // TrainSkill skills and powers
			msg.updateMessage(4, 2); // <-2 sends trainer screen
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 1001: // Open Bank
			getBank(playerCharacter, npc, origin);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 1002: // Open Vault
			getVault(playerCharacter, npc, origin);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 1003: //Refine
			msg.updateMessage(4, 3); // <-3 sends refine screen
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;

			// Mainland Teleports
		case 100011: // teleport me to Aeldreth
			City city = City.getCity(25);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 10, 75, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100012: // teleport me to SDR
			city = City.getCity(24);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 10, 75, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100013: // teleport me to Erkeng Hold
			city = City.getCity(26);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 10, 75, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100014: // teleport me to Khan
			city = City.getCity(36);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 75, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;

		case 100015: // teleport me to Maelstrom
			loc = new Vector3fImmutable(105100f, 40f, -25650f);
			handleTeleport(playerCharacter, loc, 10, 75, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100016: // teleport me to Oblivion
			loc = new Vector3fImmutable(108921f, 167f, -51590f);
			handleTeleport(playerCharacter, loc, 10, 75, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100017: // teleport me to Vander's Doom
			loc = new Vector3fImmutable(42033f, 46f, -54471f);
			handleTeleport(playerCharacter, loc, 10, 75, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100018: // teleport me to The Sinking Isle
			loc = new Vector3fImmutable(67177f, 36f, -31940f);
			handleTeleport(playerCharacter, loc, 10, 75, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;

			// MainLand Repledges
		case 100030: // repledge me to Aeldreth
			city = City.getCity(25);
			if (city != null)
				handleRepledge(playerCharacter, city, 10, 55, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100031: // repledge me to Sea Dog's Rest
			city = City.getCity(24);
			if (city != null)
				handleRepledge(playerCharacter, city, 10, 75, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100032: // repledge me to Erkeng Hold
			city = City.getCity(26);
			if (city != null)
				handleRepledge(playerCharacter, city, 10, 55, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100033: // repledge me to Khan\'Of Srekel
			city = City.getCity(36);
			if (city != null)
				handleRepledge(playerCharacter, city, 1, 75, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100035: // repledge me to Starkholm
			city = City.getCity(27);
			if (city != null)
				handleRepledge(playerCharacter, city, 1, 20, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;

			// Noob Isle Teleports
		case 100040: // teleport me to Starkholm
			city = City.getCity(27);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100041: // teleport me to All-Father's Rest
			city = City.getCity(28);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100042: // teleport me to Hengest
			city = City.getCity(33);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100043: // teleport me to Hrimdal
			city = City.getCity(30);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100044: // teleport me to Hothor's Doom
			city = City.getCity(29);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100045: // teleport me to Scraefahl
			city = City.getCity(32);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, false);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;
		case 100046: // teleport me to Valkirch
			city = City.getCity(31);
			if (city != null)
				handleTeleport(playerCharacter, city.getLoc(), 1, 20, true);
			msg.updateMessage(4, 0);
			dispatch = Dispatch.borrow(playerCharacter, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			return true;



		default:
		}
		return false;
	}

	private static boolean finishMessage(VendorDialogMsg msg, ClientConnection origin) throws MsgSendException {
		msg.updateMessage(4, 0);
		Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), msg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		return true;
	}

	private static void handleTeleport(PlayerCharacter pc, Vector3fImmutable loc, int minLevel, int maxLevel, boolean useSquare) {
		if (pc == null)
			return;

		int level = pc.getLevel();
		if (level >= minLevel && level <= maxLevel) {
			if (useSquare)
				loc = getSquare(loc);
			pc.teleport(loc);
			pc.setSafeMode();
			// PowersManager.applyPower(pc, pc, new Vector3f(0f,
			// 0f, 0f), -1661758934, 40, false);
		}
		else {
			if (level < minLevel)
				ErrorPopupMsg.sendErrorPopup(pc, 74);
			else
				ErrorPopupMsg.sendErrorPopup(pc, 139);

		}
	}

	private static void handleRepledge(PlayerCharacter pc, City city, int minLevel, int maxLevel, boolean useSquare) {
		if (pc == null || city == null)
			return;

		Vector3fImmutable loc = city.getLoc();
		int level = pc.getLevel();
		if (level >= minLevel && level <= maxLevel) {
			// set guild
			Guild guild = city.getGuild();
			if (guild != null) {
				// teleport player
				if (useSquare)
					loc = getSquare(loc);
				pc.teleport(loc);
				pc.setSafeMode();
				// PowersManager.applyPower(pc, pc, new
				// Vector3f(0f, 0f, 0f), -1661758934, 40, false);

				// join guild
				GuildManager.joinGuild(pc, guild, GuildHistoryType.JOIN);
				
				pc.resetGuildStatuses();
				
				if (guild.isNPCGuild())
					pc.setFullMember(true);
				
				if (useSquare)
					loc = loc.add(30, 0, 0);
				pc.setBindLoc(loc);
			}
			else {
				// guild not found, just teleport
				if (useSquare)
					loc = getSquare(loc);
				pc.teleport(loc);
				pc.setSafeMode();
				// PowersManager.applyPower(pc, pc, new
				// Vector3f(0f, 0f, 0f), -1661758934, 50, false);
			}
		}
		else {

			if (level < minLevel)
				ErrorPopupMsg.sendErrorPopup(pc, 74);
			else
				ErrorPopupMsg.sendErrorPopup(pc, 139);
		}
	}

	// randomly place around a tol using a square (+- 30 units in x and z
	// direction)
	public static Vector3fImmutable getSquare(Vector3fImmutable cityLoc) {
		Vector3fImmutable loc = cityLoc;
		// get direction
		int roll = ThreadLocalRandom.current().nextInt(4);
		if (roll == 0) { // north
			loc = loc.add((ThreadLocalRandom.current().nextInt(60) - 30), 0, -30);
		}
		else if (roll == 1) { // south
			loc = loc.add((ThreadLocalRandom.current().nextInt(60) - 30), 0, 30);
		}
		else if (roll == 2) { // east
			loc = loc.add(30, 0, (ThreadLocalRandom.current().nextInt(60) - 30));
		}
		else { // west
			loc = loc.add(-30, 0, (ThreadLocalRandom.current().nextInt(60) - 30));
		}

		// Make sure no one gets stuck in the tree.

		if (loc.distanceSquared2D(cityLoc) < 250) {
			loc = cityLoc;
			loc = loc.add(30, 0, 0);
		}

		return loc;
	}

	// Handle promotion
	private static void promote(PlayerCharacter pc, NPC npc) {

		if (npc == null || pc == null)
			return;

		// test level 10
		if (pc.getLevel() < 10) {
			// TODO send client promotion error
			while (pc.getLevel() > 65)
				pc.setLevel((short) 65);
			return;
		}

		// verify player not already promoted
		if (pc.getPromotionClass() != null) {
			// TODO send client promotion error
			return;
		}

		// Get promotion class for npc
		Contract contract = npc.getContract();
		if (contract == null)
			return;
		int promoID = contract.getPromotionClass();
		if (promoID == 0)
			return;
		PromotionClass promo = DbManager.PromotionQueries.GET_PROMOTION_CLASS(promoID);
		if (promo == null) {
			// TODO log error here
			return;
		}

		// verify race valid for profession
		Race race = pc.getRace();
		if (race == null || !promo.isAllowedRune(race.getToken())) {
			// TODO send client promotion error
			return;
		}

		// verify baseclass valid for profession
		BaseClass bc = pc.getBaseClass();
		if (bc == null || !promo.isAllowedRune(bc.getToken())) {
			// TODO send client promotion error
			return;
		}

		// verify gender
		if (promoID == 2511 && pc.isMale()) // Fury
			return;
		if (promoID == 2512 && pc.isMale()) // Huntress
			return;
		if (promoID == 2517 && !pc.isMale()) // Warlock
			return;

		// Everything valid. Let's promote
		pc.setPromotionClass(promo.getObjectUUID());

		//pc.setLevel((short) 65);


		promo = pc.getPromotionClass();
		if (promo == null) {
			// TODO log error here
			return;
		}

		// recalculate all bonuses/formulas/skills/powers
		pc.recalculate();

		// send the rune application to the clients
		ApplyRuneMsg arm = new ApplyRuneMsg(pc.getObjectType().ordinal(), pc.getObjectUUID(), promo.getObjectUUID(), promo.getObjectType().ordinal(), promo
				.getObjectUUID(), true);
		DispatchMessage.dispatchMsgToInterestArea(pc, arm, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);


	}

	/**
	 * Load and send vault to player. This is public only so a DevCmd can hook
	 * into it.
	 *
	 * @param playerCharacter     - Player Character requesting vault
	 * @param target - NPC vaultkeeper
	 * @param cc     - Client Connection
	 */
	public static void getVault(PlayerCharacter playerCharacter, NPC target, ClientConnection cc) {
		if (playerCharacter == null || cc == null || target == null)
			return;

		Account ac = playerCharacter.getAccount();
		if (ac == null)
			return;

		CharacterItemManager itemManager = playerCharacter.getCharItemManager();
		if (itemManager == null)
			return;

		// TODO uncomment this block after we determine when we
		// setBankOpen(false)
		/*
		 * // cannot have bank and vault open at the same time if
		 * (itemManager.isBankOpen()) return;
		 */

		if (itemManager.getTradingWith() != null) {
			return;
			// TODO close trade window here - simple once this is moved to WS
		}

		itemManager.setVaultOpen(true);

		// TODO for public test - remove this afterwards
		//		DevCmd.fillVault(pc, itemManager);

		// TODO When do we setVaultOpen(false)? I don't think the client sends a
		// "CloseVault" message.

		OpenVaultMsg openVaultMsg = new OpenVaultMsg(playerCharacter, target);
		Dispatch dispatch = Dispatch.borrow(playerCharacter, openVaultMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ShowVaultInventoryMsg showVaultInventoryMsg = new ShowVaultInventoryMsg(playerCharacter, ac, target); // 37??
		dispatch = Dispatch.borrow(playerCharacter, showVaultInventoryMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		// All recordings have "open - show - open."
		// Seems to work fine with just "show - open" as well.

	}

	/**
	 * Load and send Bank to player. This is public only so a DevCmd can hook
	 * into it.
	 *
	 * @param playerCharacter     - Player Character requesting vault
	 * @param target - NPC vaultkeeper
	 * @param cc     - Client Connection
	 */
	public static void getBank(PlayerCharacter playerCharacter, NPC target, ClientConnection cc) {

		if (playerCharacter == null)
			return;

		if (cc == null)
			return;

		CharacterItemManager itemManager = playerCharacter.getCharItemManager();

		if (itemManager == null)
			return;

		// TODO uncomment this block after we determine when we
		// setVaultOpen(false)
		/*
		 * // cannot have bank and vault open at the same time if
		 * (itemManager.isVaultOpen()) return;
		 */

		if (itemManager.getTradingWith() != null) {
			return;
			// TODO close trade window here - simple once this is moved to WS
		}

		itemManager.setBankOpen(true);
		// TODO When do we setBankOpen(false)? I don't think the client sends a
		// "CloseBank" message.

		AckBankWindowOpenedMsg ackBankWindowOpenedMsg = new AckBankWindowOpenedMsg(playerCharacter, 0L, 0L);

		Dispatch dispatch = Dispatch.borrow(playerCharacter, ackBankWindowOpenedMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ReqBankInventoryMsg reqBankInventoryMsg = new ReqBankInventoryMsg(playerCharacter, 0L);
		dispatch = Dispatch.borrow(playerCharacter, reqBankInventoryMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		ShowBankInventoryMsg showBankInventoryMsg = new ShowBankInventoryMsg(playerCharacter, 0L);
		dispatch = Dispatch.borrow(playerCharacter, showBankInventoryMsg);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	/**
	 * Serializes the subclass specific items to the supplied ByteBufferWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		writer.putInt(this.messageType);
		for (int i = 0; i < 3; i++)
			writer.putInt(0);
		if (messageType == 1)
			writer.putString(this.language);
		else
			writer.putString("");
		writer.putInt(this.vendorObjectType);
		writer.putInt(this.vendorObjectID);

		for (int i = 0; i < 3; i++)
			writer.putInt(0);
		writer.put((byte) 0);
		writer.put((byte) 0);
		if (this.messageType == 1) {
			writer.putInt(this.unknown01);
			writer.putInt(this.unknown02);
		}
		else if (this.messageType == 4) {
			writer.putInt(0);
			writer.putInt(0x364AF0D0); // 0x325695C0
			if (this.vd != null)
				writer.putInt(vd.getObjectUUID());
			else
				writer.putInt(this.unknown03);
			writer.putInt(1);
			writer.put((byte) 0);
			writer.putInt(this.unknown03);
			writer.put((byte) 0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(2); // 2
			if (menuType == 2)
				writer.putInt(3);
			else if (menuType == 3)
				writer.putInt(4);
			else
				writer.putInt(5);
			//			writer.putInt(3); // 3=buy/sell/trade, 4=untrain, 5 closes
			writer.putInt(10); // 10
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			return;

		}
		writer.putInt(15);
		writer.putInt(5);
		if (this.vd != null)
			writer.putInt(vd.getObjectUUID());
		else
			writer.putInt(this.unknown03);

		//filename datablock
		writer.putInt(1);
		writer.put((byte) 1);
		writer.putInt(this.unknown03);
		writer.put((byte) 1);
		writer.putString(vd.getDialogType());
		writer.putInt(0);
		writer.putInt(1);
		writer.putInt(4);
		writer.putInt(0);
		writer.put((byte) 1);

		//vendor dialog datablock
		writer.putString(vd.getDialogType());
		writer.putString(vd.getIntro());
		writer.put((byte) 0);

		//menu options datablock
		writer.putInt(1);
		writer.putString(" [ " + vd.getIntro() + " ] ");
		ArrayList<MenuOption> options = vd.getOptions();
		writer.putInt((options.size() + 1));
		for (MenuOption option : options) {
			if (option.getMessage().equals(" [ Merchant options ] ")) {
				writer.putInt(16);
			}
			else {
				writer.putInt(14);
			}
			writer.put((byte) 0);
			writer.putInt(0);
			writer.put((byte) 0);
			writer.putInt(1);
			writer.putString(option.getMessage());
			writer.putInt(option.getOptionID());
			for (int i = 0; i < 3; i++)
				writer.putInt(0);
		}
		writer.putInt(10);
		writer.put((byte) 0);
		writer.putInt(0);
		writer.put((byte) 0);
		writer.putInt(1);
		writer.putString("Done");
		for (int i = 0; i < 4; i++)
			writer.putInt(0);
		//		writer.putInt(1);
		//		writer.putInt(2);
		for (int i = 0; i < 4; i++)
			writer.putInt(0);
	}

	/**
	 * Deserializes the subclass specific items from the supplied
	 * ByteBufferReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		this.messageType = reader.getInt();
		for (int i = 0; i < 3; i++)
			reader.getInt();
		this.language = reader.getString();
		this.vendorObjectType = reader.getInt();
		this.vendorObjectID = reader.getInt();
		for (int i = 0; i < 3; i++)
			reader.getInt();
		reader.get();
		reader.get();
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		reader.getInt();
		// if (this.messageType == 1) {
		reader.get();
		reader.getInt();
		reader.get();
		reader.getInt();
		reader.getShort();
		// return;
		// }
		// reader.get();
		// this.unknown04 = reader.getInt();
		// reader.get();
		// TODO more message to go here
	}

	public int getMessageType() {
		return this.messageType;
	}

	public void setMessageType(int value) {
		this.messageType = value;
	}

	public int getVendorObjectType() {
		return this.vendorObjectType;
	}

	public int getVendorObjectID() {
		return this.vendorObjectID;
	}

	public int getUnknown01() {
		return this.unknown01;
	}

	public int getUnknown02() {
		return this.unknown02;
	}

	public int getUnknown03() {
		return this.unknown03;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}

	public void setLanguage(String value) {
		this.language = value;
	}

	public void updateMessage(int messageType, int menuType) {
		this.messageType = messageType;
		this.menuType = menuType;
	}

	public void updateMessage(int messageType, String dialogType, String intro, int menuType) {
		this.messageType = messageType;
		this.dialogType = dialogType;
		this.intro = intro;
		this.introCode = " [ " + this.intro + " ] ";
		this.menuType = menuType;
	}

	public void updateMessage(int messageType, VendorDialog vd) {
		this.messageType = messageType;
		this.vd = vd;
	}
}
