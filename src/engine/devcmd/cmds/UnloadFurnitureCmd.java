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
import engine.net.client.msg.LoadStructureMsg;
import engine.net.client.msg.MoveToPointMsg;
import engine.net.client.msg.UnloadObjectsMsg;
import engine.objects.AbstractGameObject;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

/**
 * @author
 *
 */
public class UnloadFurnitureCmd extends AbstractDevCmd {

	public UnloadFurnitureCmd() {
        super("furniture");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		if (target.getObjectType() != GameObjectType.Building){
			this.throwbackError(pc, "Must be targeting a building to load/unload furniture.");
			return;
		}
		if (words[0].equalsIgnoreCase("unload")){

			UnloadObjectsMsg uom = new UnloadObjectsMsg();
			for (AbstractWorldObject awo: pc.getLoadedStaticObjects()){
				if (awo.getObjectType() != GameObjectType.Building)
					continue;

				Building awoBuilding = (Building)awo;

                if (!awoBuilding.isFurniture)
					continue;

				if (awoBuilding.parentBuildingID != target.getObjectUUID())
					continue;
				
				MoveToPointMsg msg = new MoveToPointMsg(awoBuilding);
				pc.getClientConnection().sendMsg(msg);

				uom.addObject(awoBuilding);


			}

			pc.getClientConnection().sendMsg(uom);

		}else if (words[0].equalsIgnoreCase("load")){
			LoadStructureMsg lsm = new LoadStructureMsg();

			for (AbstractWorldObject awo: pc.getLoadedStaticObjects()){
				if (awo.getObjectType() != GameObjectType.Building)
					continue;

				Building awoBuilding = (Building)awo;

                if (!awoBuilding.isFurniture)
					continue;

				if (awoBuilding.parentBuildingID != target.getObjectUUID())
					continue;

				lsm.addObject(awoBuilding);


			}

			pc.getClientConnection().sendMsg(lsm);

		}
	}

	@Override
	protected String _getHelpString() {
		String help = "Enchants an item with a prefix and suffix";
		return help;
	}

	@Override
	protected String _getUsageString() {
		String usage = "' /enchant clear/Enchant1 Enchant2 Enchant3 ...'";
		return usage;
	}

}
