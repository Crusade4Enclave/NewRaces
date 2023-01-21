package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.GuildHistoryType;
import engine.InterestManagement.RealmMap;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.job.JobScheduler;
import engine.jobs.TeleportJob;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;

import java.util.ArrayList;

/*
 * @Author:
 * @Summary: Processes a variety of windows the client can open
 * such as realm blessings and warehouse deposits.
 */

public class MerchantMsgHandler extends AbstractClientMsgHandler {

	public MerchantMsgHandler() {
		super(MerchantMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		MerchantMsg msg;
		PlayerCharacter player;
		NPC npc;
		int msgType;
		Building warehouse;
		Dispatch dispatch;

		// Member variable assignment

		player = SessionManager.getPlayerCharacter(origin);
		msg = (MerchantMsg) baseMsg;
		npc = NPC.getNPC(msg.getNPCID());

		// Early exit if something goes awry

		if ((player == null) || (npc == null))
			return true;

		// Player must be within talking range

		if (player.getLoc().distanceSquared2D(npc.getLoc()) > MBServerStatics.NPC_TALK_RANGE * MBServerStatics.NPC_TALK_RANGE) {
			ErrorPopupMsg.sendErrorPopup(player, 14);
			return true;
		}

		// Process application protocol message

		msgType = msg.getType();

		switch (msgType) {
		case 3:
			break;
		case 5:

			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			requestSwearAsSubGuild(msg, origin, player, npc);
			break;
		case 10:
			teleportRepledgeScreen(msg, origin, player, false, npc);
			break;
		case 11:
			teleportRepledge(msg, origin, player, false, npc);
			break;
		case 12:
			teleportRepledgeScreen(msg, origin, player, true, npc);
			break;
		case 13:
			teleportRepledge(msg, origin, player, true, npc);
			break;
		case 14:
			if (isHermit(npc))
				requestHermitBlessing(msg, origin, player, npc);
			else
				requestBoon(msg, origin, player, npc);
			break;
		case 15:
			LeaderboardMessage lbm = new LeaderboardMessage();
			dispatch = Dispatch.borrow(player, lbm);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;
		case 16:
			ViewResourcesMessage vrm = new ViewResourcesMessage(player);
			warehouse = npc.getBuilding();
			vrm.setGuild(player.getGuild());
			vrm.setWarehouseBuilding(warehouse);
			vrm.configure();
			dispatch = Dispatch.borrow(player, vrm);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
			break;
		case 17:
			Warehouse.warehouseWithdraw(msg, player, npc, origin);
			break;
		case 18:
			Warehouse.warehouseDeposit(msg, player, npc, origin);
			break;
		case 19:
			Warehouse.warehouseLock(msg, player, npc, origin);
			break;
		}

		return true;

	}

	private static void requestSwearAsSubGuild(MerchantMsg msg, ClientConnection origin, PlayerCharacter player, NPC npc) {

		boolean Disabled = true;

		if (Disabled){
			ErrorPopupMsg.sendErrorMsg(player, "Swearing to Safeholds have been temporary disabled."); //Cannot sub as errant guild.
			return;
		}
		
		if (player.getGuild().isEmptyGuild()){
			ErrorPopupMsg.sendErrorMsg(player, "You do not belong to a guild!"); //Cannot sub as errant guild.
			return;
		}

		if (player.getGuild().getNation() != null && !player.getGuild().getNation().isEmptyGuild()){
			ErrorPopupMsg.sendErrorMsg(player, "You already belong to a nation!"); //Cannot sub as errant guild.
			return;
		}

		if (player.getGuild().getGuildLeaderUUID() != player.getObjectUUID()){
			ErrorPopupMsg.sendErrorMsg(player, "You must be a Guild Leader to Swear your guild as a Sub Guild!"); //Cannot sub as errant guild.
			return;
		}

		if (!GuildStatusController.isGuildLeader(player.getGuildStatus())){
			ErrorPopupMsg.sendErrorMsg(player, "You must be a Guild Leader to Swear your guild as a Sub Guild!"); //Cannot sub as errant guild.
			return;
		}

		
		if (!npc.getGuild().isNPCGuild()){
			ErrorPopupMsg.sendErrorMsg(player, "Runemaster does not belong to a safehold!"); //Cannot sub as errant guild.
			return;
		}

		if (npc.getGuild().getRepledgeMin() > player.getLevel()){
			ErrorPopupMsg.sendErrorMsg(player, "You are too low of a level to sub to this guild!"); //Cannot sub as errant guild.
			return;
		}

		if (npc.getGuild().getRepledgeMax() < 75){
			ErrorPopupMsg.sendErrorMsg(player, "Runemaster Guild Cannot Swear in your guild!"); //Cannot sub as errant guild.
			return;
		}





		if (!DbManager.GuildQueries.UPDATE_PARENT(player.getGuild().getObjectUUID(), npc.getGuild().getObjectUUID())) {
			ErrorPopupMsg.sendErrorMsg(player, "A Serious error has occured. Please post details for to ensure transaction integrity");
			return;
		}


		GuildManager.updateAllGuildBinds(player.getGuild(), npc.getGuild().getOwnedCity());



		//update Guild state.
		player.getGuild().setNation(npc.getGuild());
		GuildManager.updateAllGuildTags(player.getGuild());

		//update state twice, errant to petitioner, to sworn.
		player.getGuild().upgradeGuildState(false);//to petitioner
		player.getGuild().upgradeGuildState(false);//to sworn





	}



	private static void requestHermitBlessing(MerchantMsg msg, ClientConnection origin, PlayerCharacter player, NPC npc) {

		Guild guild;
		Realm realm;
		City city;
		Building tol;

		// Validate player can obtain blessing

		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false) {
			ErrorPopupMsg.sendErrorPopup(player, 173); // You must be the leader of a guild to receive a blessing
			return;
		}

		guild = player.getGuild();
		city = guild.getOwnedCity();

		if (city == null) {
			ErrorPopupMsg.sendErrorPopup(player, 179); // Only landed guilds may claim a territory
			return;
		}
		tol = city.getTOL();

		if (tol.getRank() != 7) {
			ErrorPopupMsg.sendErrorPopup(player, 181); // Your tree must be rank 7 before claiming a territory
			return;
		}

		realm = RealmMap.getRealmForCity(city);

		if (realm.getCanBeClaimed() == false) {
			ErrorPopupMsg.sendErrorPopup(player, 180); // This territory cannot be ruled by anyone
			return;
		}

		if (realm.isRuled() == true) {
			ErrorPopupMsg.sendErrorPopup(player, 178); // This territory is already claimed
			return;
		}

		// Everything should be good, apply boon for this hermit

		PowersManager.applyPower(player, player, player.getLoc(), getPowerforHermit(npc).getToken(), 40, false);

	}

	private static void requestBoon(MerchantMsg msg, ClientConnection origin, PlayerCharacter player, NPC npc) {

		Building shrineBuilding;
		Shrine shrine;

		if (npc.getGuild() != player.getGuild())
			return;

		shrineBuilding = npc.getBuilding();

		if (shrineBuilding == null)
			return;

		if (shrineBuilding.getBlueprint() != null && shrineBuilding.getBlueprint().getBuildingGroup() != engine.Enum.BuildingGroup.SHRINE)
			return;

		if (shrineBuilding.getRank() == -1)
			return;

		shrine = Shrine.shrinesByBuildingUUID.get(shrineBuilding.getObjectUUID());

		if (shrine == null)
			return;

		if (shrine.getFavors() == 0) {
			ErrorPopupMsg.sendErrorPopup(player, 172);
			return;
		}

		//already haz boon.

		if (player.containsEffect(shrine.getShrineType().getPowerToken())) {
			ErrorPopupMsg.sendErrorPopup(player, 199);
			return;
		}

		if (!Shrine.canTakeFavor(player, shrine))
			return;

		if (!shrine.takeFavor(player))
			return;

		PowersBase shrinePower = PowersManager.getPowerByToken(shrine.getShrineType().getPowerToken());

		if (shrinePower == null) {
			ChatManager.chatSystemError(player, "FAILED TO APPLY POWER!");
			return;
		}

		int rank = shrine.getRank();
		//R8 trees always get atleast rank 2 boons. rank uses index, where 0 is first place, 1 is second, etc...
		if (shrineBuilding.getCity() != null && shrineBuilding.getCity().getTOL() != null && shrineBuilding.getCity().getTOL().getRank() == 8)
			if (rank != 0)
				rank = 1;
		int trains = 40 - (rank * 10);
		if (trains < 0)
			trains = 0;

		//System.out.println(trains);
		PowersManager.applyPower(player, player, player.getLoc(), shrinePower.getToken(), trains, false);
		ChatManager.chatGuildInfo(player.getGuild(), player.getName() + " has recieved a boon costing " + 1 + " point of favor.");
		shrineBuilding.addEffectBit(1000000 << 2);
		shrineBuilding.updateEffects();

		//remove the effect so players loading shrines dont see the effect go off.
		shrineBuilding.removeEffectBit(1000000 << 2);
	}

	private static void teleportRepledgeScreen(MerchantMsg msg, ClientConnection origin, PlayerCharacter pc, boolean isTeleport, NPC npc) {

		Dispatch dispatch;
		TeleportRepledgeListMsg trlm;

		//verify npc is runemaster

		Contract contract = npc.getContract();

		if (contract == null || !contract.isRuneMaster())
			return;

		if (!isTeleport)
			trlm = new TeleportRepledgeListMsg(pc, false);
		else
			trlm = new TeleportRepledgeListMsg(pc, true);

		trlm.configure();

		dispatch = Dispatch.borrow(pc, trlm);
		DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
	}

	private static void teleportRepledge(MerchantMsg msg, ClientConnection origin, PlayerCharacter player, boolean isTeleport, NPC npc) {

		//verify npc is runemaster

		Contract contract = npc.getContract();
		Dispatch dispatch;

		if (contract == null || !contract.isRuneMaster())
			return;

		//get city to teleport/repledge to and verify valid

		ArrayList<City> cities;

		City targetCity = null;

		if (isTeleport)
			cities = City.getCitiesToTeleportTo(player);
		else
			cities = City.getCitiesToRepledgeTo(player);
		for (City city : cities) {
			if (city.getObjectUUID() == msg.getCityID()) {
				targetCity = city;
				break;
			}
		}

		if (targetCity == null)
			return;

		//verify level required to teleport or repledge

		Guild toGuild = targetCity.getGuild();

		if (toGuild != null)
			if (isTeleport) {
				if (player.getLevel() < toGuild.getTeleportMin() || player.getLevel() > toGuild.getTeleportMax())
					return;
			}
			else if (player.getLevel() < toGuild.getRepledgeMin() || player.getLevel() > toGuild.getRepledgeMax())
				return;

		boolean joinedGuild = false;

		//if repledge, reguild the player

		if (!isTeleport)
			joinedGuild = GuildManager.joinGuild(player, targetCity.getGuild(), targetCity.getObjectUUID(), GuildHistoryType.JOIN);

		int time;

		if (!isTeleport) //repledge
			time = MBServerStatics.REPLEDGE_TIME_IN_SECONDS;
		else
			time = MBServerStatics.TELEPORT_TIME_IN_SECONDS;

		//resend message
		msg.setTeleportTime(time);

		if ((!isTeleport && joinedGuild) || (isTeleport)) {

			dispatch = Dispatch.borrow(player, msg);
			DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
		}

		//teleport player to city

		Vector3fImmutable teleportLoc;

		if (targetCity.getTOL().getRank() == 8)
			teleportLoc = targetCity.getTOL().getStuckLocation();
		else
			teleportLoc = Vector3fImmutable.getRandomPointOnCircle(targetCity.getTOL().getLoc(), MBServerStatics.TREE_TELEPORT_RADIUS);

		if (time > 0) {
			//TODO add timer to teleport
			TeleportJob tj = new TeleportJob(player, npc, teleportLoc, origin, true);
			JobScheduler.getInstance().scheduleJob(tj, time * 1000);
		}
		else if (joinedGuild) {
			player.teleport(teleportLoc);
			player.setSafeMode();
		}
	}

	private static PowersBase getPowerforHermit(NPC npc) {

		int contractID;
		PowersBase power;
		Contract contract;

		contract = npc.getContract();
		contractID = contract.getContractID();
		power = null;

		switch (contractID) {
		case 435579:
			power = PowersManager.getPowerByIDString("BLS-POWER");
			break;
		case 435580:
			power = PowersManager.getPowerByIDString("BLS-FORTUNE");
			break;
		case 435581:
			power = PowersManager.getPowerByIDString("BLS-WISDOM");
			break;

		}
		return power;
	}

	private static boolean isHermit(NPC npc) {

		int contractID;
		boolean retValue = false;

		contractID = npc.getContractID();

		switch (contractID) {
		case 435579:
		case 435580:
		case 435581:
			retValue = true;
			break;
		default:
			break;
		}

		return retValue;
	}

}