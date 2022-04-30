// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;


import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.net.DispatchMessage;
import engine.net.client.msg.ApplyBuildingEffectMsg;
import engine.net.client.msg.UpdateCharOrMobMessage;
import engine.objects.*;

public class SetSubRaceCmd extends AbstractDevCmd {

	public SetSubRaceCmd() {
        super("setSubRace");
        this.addCmdString("subrace");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		
		if (target instanceof AbstractCharacter){

			if (words[0].equals("race")){
				if (target.getObjectType() != GameObjectType.PlayerCharacter)
					return;
				PlayerCharacter player = (PlayerCharacter)target;
				int raceID = Integer.parseInt(words[1]);
				player.setSubRaceID(raceID);
				if (raceID == 0)
					raceID = player.getRaceID();
				UpdateCharOrMobMessage ucm = new UpdateCharOrMobMessage(player, 1,raceID);
				DispatchMessage.sendToAllInRange(player, ucm);
				return;
			}
			if (words[0].equals("all")){
				for (int i = 15999; i< 16337;i++){
					ApplyBuildingEffectMsg applyBuildingEffectMsg = new ApplyBuildingEffectMsg(4, 0, target.getObjectType().ordinal(), target.getObjectUUID(), i);
					DispatchMessage.sendToAllInRange((AbstractWorldObject) target, applyBuildingEffectMsg);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();	
					}
				}

			}else{
				ApplyBuildingEffectMsg applyBuildingEffectMsg = new ApplyBuildingEffectMsg(4, 0, target.getObjectType().ordinal(), target.getObjectUUID(), Integer.parseInt(words[0]));
				DispatchMessage.sendToAllInRange((AbstractWorldObject) target, applyBuildingEffectMsg);
			}

			return;
		}

		Building building = (Building)target;

		building.removeAllVisualEffects();
		building.addEffectBit(1<<Integer.parseInt(words[0]));
		building.updateEffects();
		//63535 38751
		//		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());
		//		//CityZoneMsg czm = new CityZoneMsg(3, zone.getLoc().x, zone.getLoc().y, zone.getLoc().z, "balls", zone, 0, 0);
		//		pc.getClientConnection().sendMsg(new DeleteItemMsg(zone.getObjectType().ordinal(), zone.getObjectUUID()));

		//		UpdateInventoryMsg updateInventoryMsg = new UpdateInventoryMsg(new ArrayList<>(), new ArrayList<>(), null, true);
		//		pc.getClientConnection().sendMsg(updateInventoryMsg);

		//pc.getCharItemManager().updateInventory();



		//		Mob mob = (Mob)target;
		//
		//		if (mob == null)
		//			return;
		//
		//		MobLoot mobLoot = new MobLoot(mob, ItemBase.getItemBase(Integer.parseInt(words[0])), false);
		//
		//		mob.getCharItemManager().addItemToInventory(mobLoot);




		//		if (target.getObjectType() != GameObjectType.Building)
		//			return;
		//
		//		Building warehouseBuilding = (Building)target;
		//
		//		if (Warehouse.warehouseByBuildingUUID.get(warehouseBuilding.getObjectUUID()) == null)
		//			return;
		//
		//		Warehouse warehouse = Warehouse.warehouseByBuildingUUID.get(warehouseBuilding.getObjectUUID());
		//
		//		for (int ibID: Warehouse.getMaxResources().keySet()){
		//			ItemBase ib = ItemBase.getItemBase(ibID);
		//			warehouse.depositFromMine(null, ib, Warehouse.getMaxResources().get(ibID));
		//		}


		//		int raceID = Integer.parseInt(words[0]);
		//
		//		UpdateCharOrMobMessage ucm = new UpdateCharOrMobMessage(pc, raceID);
		//
		//		pc.getClientConnection().sendMsg(ucm);

		//		LoadCharacterMsg lcm = new LoadCharacterMsg((AbstractCharacter)null,false);
		//		try {
		//			DispatchMessage.sendToAllInRange(pc, lcm);
		//		} catch (MsgSendException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		ModifyHealthMsg mhm =new ModifyHealthMsg(pc, pc, -50f, 0f, 0f, 0, null, 9999, 0);
		//		mhm.setOmitFromChat(1);
		//		pc.getClientConnection().sendMsg(mhm);
		//
		//		int temp = 0;
		//		boolean start = false;
		//
		//		for (EffectsBase eb: EffectsBase.getAllEffectsBase()){
		//
		//
		//
		//			if (!pc.getClientConnection().isConnected()){
		//				Logger.info("", "PLAYER DC!" + eb.getIDString());
		//				break;
		//			}
		//
		//			eb = PowersManager.getEffectByIDString("WLR-018A");
		//
		//
		//			NoTimeJob ntj = new NoTimeJob(pc, "NONE", eb, 40);
		//			pc.addEffect(String.valueOf(eb.getUUID()), 1000, ntj, eb, 40);
		//			eb.sendEffectNoPower(ntj, 1000, pc.getClientConnection());
		//
		//			ThreadUtils.sleep(500);
		//			pc.clearEffects();
		//
		//			//WorldServer.updateObject((AbstractWorldObject)target, pc);
		//			this.throwbackInfo(pc, eb.getIDString());
		//			break;
		//			//ThreadUtils.sleep(500);
		//
		//		}









		//		for (EffectsBase eb : EffectsBase.getAllEffectsBase()){
		//			if (eb.getToken() == 0)
		//				continue;
		//			int token = eb.getToken();
		//			ApplyEffectMsg pum = new ApplyEffectMsg();
		//			pum.setEffectID(token);
		//			pum.setSourceType(pc.getObjectType().ordinal());
		//			pum.setSourceID(pc.getObjectUUID());
		//			pum.setTargetType(pc.getObjectType().ordinal());
		//			pum.setTargetID(pc.getObjectUUID());
		//			pum.setNumTrains(40);
		//			pum.setDuration(-1);
		////			pum.setDuration((pb.isChant()) ? (int)pb.getChantDuration() : ab.getDurationInSeconds(trains));
		//			pum.setPowerUsedID(0);
		//		//	pum.setPowerUsedName("Inflict Poison");
		//			this.throwbackInfo(pc, eb.getName() + "Token = " + eb.getToken());
		//			pc.getClientConnection().sendMsg(pum);
		//			ThreadUtils.sleep(200);
		//		}
		//

		//		 UpdateObjectMsg uom = new UpdateObjectMsg(pc, 4);
		//         try {
		//             Location.sendToAllInRange(pc, uom);
		//         } catch (MsgSendException e) {
		//             // TODO Auto-generated catch block
		//             e.printStackTrace();
		//         }




	}

	@Override
	protected String _getHelpString() {
		return "Temporarily Changes SubRace";
	}

	@Override
	protected String _getUsageString() {
		return "' /subrace mobBaseID";
	}

}
