// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.DispatchChannel;
import engine.Enum.ItemContainerType;
import engine.gameManager.ConfigManager;
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.ClientMessagePump;
import engine.net.client.msg.*;
import engine.util.ByteUtils;
import org.pmw.tinylog.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Account extends AbstractGameObject {

	private final String uname;
	private String passwd;
	private int lastCharIDUsed;
	private String salt;
	public String discordAccount;
	private byte loginAttempts = 0;
	private long lastLoginFailure = System.currentTimeMillis();
	public HashMap<Integer, PlayerCharacter> characterMap = new HashMap<>();
	public static ConcurrentHashMap<String, Integer> AccountsMap = new ConcurrentHashMap<>();
	private ArrayList<Item> vault = new ArrayList<>();
	public Item vaultGold = null;
	public long lastPasswordCheck = 0;
	public Enum.AccountStatus status;

	public ArrayList<Item> getVault() {
		return vault;
	}

	public Account(ResultSet resultSet) throws SQLException {
		super(resultSet);

		this.uname = resultSet.getString("acct_uname");
		this.passwd = resultSet.getString("acct_passwd");
		this.lastCharIDUsed = resultSet.getInt("acct_lastCharUID");
		this.salt = resultSet.getString("acct_salt");
		this.discordAccount = resultSet.getString("discordAccount");
		this.status = Enum.AccountStatus.valueOf(resultSet.getString("status"));
	}

	public String getUname() {
		return uname;
	}

	public String getPasswd() {
		return passwd;
	}

	public String getSalt() {
		return salt;
	}

	public int getLastCharIDUsed() {
		return lastCharIDUsed;
	}

	public byte getLoginAttempts() {
		return loginAttempts;
	}

	public long getLastLoginFailure() {
		return this.lastLoginFailure;
	}

	public void setLastCharIDUsed(int lastCharIDUsed) {
		this.lastCharIDUsed = lastCharIDUsed;
	}

	public void setLastLoginFailure() {
		this.lastLoginFailure = System.currentTimeMillis();
	}

	public void setLastCharacter(int uuid) {
		this.lastCharIDUsed = uuid;
		//		this.updateDatabase();
	}

	public void incrementLoginAttempts() {
		++this.loginAttempts;
		this.setLastLoginFailure();
	}

	public void resetLoginAttempts() {
		this.loginAttempts = 0;
	}

	/*
	 * on successfully matching the password, this method additionally calls to
	 * associateIpToAccount for IPAddress tracking. dokks
	 */
	public boolean passIsValid(String pw, String ip, String machineID) throws IllegalArgumentException {
		boolean result = false;
		// see if it was entered in plain text first, if the plain text matches,
		// hash it and save to the database.
		try {
			pw = ByteUtils.byteArrayToSafeStringHex(MessageDigest
					.getInstance("md5").digest(pw.getBytes("UTF-8")))
					+ salt;
			pw = ByteUtils.byteArrayToSafeStringHex(MessageDigest
					.getInstance("md5").digest(pw.getBytes()));
			result = this.passwd.equals(pw);
		} catch (    NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Logger.error( e.toString());
		}

		if (result) {
			// TODO: should use an executor here so that we can
			// fire and forget this update.
			// this is a valid user, so let's also update the
			// database with login time and IP.
			if((ip==null)||(ip.length()==0)) {
				throw new IllegalArgumentException();
			}
		}
		return result;
	}

	public ClientConnection getClientConnection() {
		return SessionManager.getClientConnection(this);
	}

	public PlayerCharacter getPlayerCharacter() {
		return SessionManager.getPlayerCharacter(this);
	}

	@Override
	public void updateDatabase() {
		DbManager.AccountQueries.updateDatabase(this);
	}

	//this should be called to handle any after load functions.

	public void runAfterLoad() {

		try {

			if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER)){
				ArrayList<PlayerCharacter> playerList = DbManager.PlayerCharacterQueries.GET_CHARACTERS_FOR_ACCOUNT(this.getObjectUUID());

				for(PlayerCharacter player:playerList) {
					PlayerCharacter.initializePlayer(player);
					this.characterMap.putIfAbsent(player.getObjectUUID(), player);
				}

				playerList.clear();
			}

			if (ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER)) {
				this.vault = DbManager.ItemQueries.GET_ITEMS_FOR_ACCOUNT(this.getObjectUUID());

				for (Item item : this.vault) {
					if (item.getItemBase().getUUID() == 7) {
						this.vaultGold = item;
					}
				}

				if (this.vaultGold == null) {
					this.vaultGold = Item.newGoldItem(this.getObjectUUID(), ItemBase.getItemBase(7), ItemContainerType.VAULT);

					if (this.vaultGold != null)
						this.vault.add(this.vaultGold);
				}
			}

		} catch (Exception e) {
			Logger.error( e);
		}
	}

	public synchronized void transferItemFromInventoryToVault(TransferItemFromInventoryToVaultMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (!ClientMessagePump.NPCVaultBankRangeCheck(player, origin, "vault")) {
			ClientMessagePump.forceTransferFromVaultToInventory(msg, origin, "You are out of range of the vault.");
			return;
		}

		int uuid = msg.getUUID();
		Item item = Item.getFromCache(uuid);

		if (item == null) {
			ClientMessagePump.forceTransferFromVaultToInventory(msg, origin, "Can't find the item.");
			return;
		}

		//dupe check
		if (!item.validForInventory(origin, player, player.getCharItemManager()))
			return;

		if (item.containerType == Enum.ItemContainerType.INVENTORY && player.getCharItemManager().isVaultOpen()) {
			if (!player.getCharItemManager().hasRoomVault(item.getItemBase().getWeight())) {
				ClientMessagePump.forceTransferFromVaultToInventory(msg, origin, "There is no room in your vault.");
				return;
			}

			if (player.getCharItemManager().moveItemToVault(item)) {
				this.vault.add(item);
				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
			} else
				ClientMessagePump.forceTransferFromVaultToInventory(msg, origin, "Failed to transfer item.");
		}
	}

	public synchronized void transferItemFromVaultToInventory(TransferItemFromVaultToInventoryMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		if (!ClientMessagePump.NPCVaultBankRangeCheck(player, origin, "vault")) {
			ClientMessagePump.forceTransferFromInventoryToVault(msg, origin, "You are out of range of the vault.");
			return;
		}

		CharacterItemManager itemManager = player.getCharItemManager();

		if (itemManager == null) {
			ClientMessagePump.forceTransferFromInventoryToVault(msg, origin, "Can't find your item manager.");
			return;
		}

		Item item = Item.getFromCache(msg.getUUID());

		if (item == null) {
			ClientMessagePump.forceTransferFromInventoryToVault(msg, origin, "Can't find the item.");
			return;
		}

		//dupe check
		if (!item.validForVault(origin, player, itemManager))
			return;

		if (item.containerType == Enum.ItemContainerType.VAULT && itemManager.isVaultOpen()) {
			if (!itemManager.hasRoomInventory(item.getItemBase().getWeight())) {
				ClientMessagePump.forceTransferFromInventoryToVault(msg, origin, "There is no room in your inventory.");
				return;
			}
			if (itemManager.moveItemToInventory(item)) {
				this.vault.remove(item);

				dispatch = Dispatch.borrow(player, msg);
				DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

			} else
				ClientMessagePump.forceTransferFromInventoryToVault(msg, origin, "Failed to transfer item.");
		}
	}

	public synchronized void transferGoldFromVaultToInventory(TransferGoldFromVaultToInventoryMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		Account account = player.getAccount();

		if (account == null)
			return;

		if (!ClientMessagePump.NPCVaultBankRangeCheck(player, origin, "vault"))
			return;

		NPC npc = player.getLastNPCDialog();

		if (npc == null)
			return;

		CharacterItemManager itemManager = player.getCharItemManager();

		if (itemManager == null)
			return;

		if (itemManager.isVaultOpen() == false)
			return;

		if (itemManager.moveGoldToInventory(itemManager.getGoldVault(), msg.getAmount()) == false)
			return;

		OpenVaultMsg open = new OpenVaultMsg(player, npc);
		ShowVaultInventoryMsg show = new ShowVaultInventoryMsg(player, account, npc); // 37??

		UpdateGoldMsg ugm = new UpdateGoldMsg(player);
		ugm.configure();
		dispatch = Dispatch.borrow(player, ugm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		UpdateVaultMsg uvm = new UpdateVaultMsg(account);
		dispatch = Dispatch.borrow(player, uvm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		dispatch = Dispatch.borrow(player, open);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		dispatch = Dispatch.borrow(player, show);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}

	public synchronized void transferGoldFromInventoryToVault(TransferGoldFromInventoryToVaultMsg msg, ClientConnection origin) {

		PlayerCharacter player = origin.getPlayerCharacter();
		Dispatch dispatch;

		if (player == null)
			return;

		Account account = player.getAccount();

		if (account == null)
			return;

		if (!ClientMessagePump.NPCVaultBankRangeCheck(player, origin, "vault"))
			return;

		CharacterItemManager itemManager = player.getCharItemManager();

		if (itemManager == null)
			return;

		NPC npc = player.getLastNPCDialog();

		if (npc == null)
			return;

		// Cannot have bank and vault open concurrently
		// Dupe prevention

		if (itemManager.isVaultOpen() == false)
			return;

		// Something went horribly wrong.  Should be log this?

		if (itemManager.moveGoldToVault(itemManager.getGoldInventory(), msg.getAmount()) == false)
			return;

		UpdateGoldMsg ugm = new UpdateGoldMsg(player);
		ugm.configure();
		dispatch = Dispatch.borrow(player, ugm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		UpdateVaultMsg uvm = new UpdateVaultMsg(account);
		dispatch = Dispatch.borrow(player, uvm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

		OpenVaultMsg open = new OpenVaultMsg(player, npc);
		dispatch = Dispatch.borrow(player, open);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
		//
		//
		ShowVaultInventoryMsg show = new ShowVaultInventoryMsg(player, account, npc); // 37??
		dispatch = Dispatch.borrow(player, show);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);

	}
}
