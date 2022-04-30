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
import engine.gameManager.ChatManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class convertLoc extends AbstractDevCmd {

	public convertLoc() {
        super("convertLoc");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {


		if (target == null){
			Vector3fImmutable convertLoc = ZoneManager.findSmallestZone(pc.getLoc()).getLoc().subtract(pc.getLoc());
			ChatManager.chatSystemInfo(pc, Vector3fImmutable.toString(convertLoc));
			return;
		}


		if (target.getObjectType() != GameObjectType.Building)
			return;
		Building toConvert = (Building)target;
		Vector3fImmutable convertedLoc = ZoneManager.convertWorldToLocal(toConvert, pc.getLoc());
		ChatManager.chatSystemInfo(pc, Vector3fImmutable.toString(convertedLoc));




	}

	@Override
	protected String _getHelpString() {
		return "Temporarily Changes SubRace";
	}

	@Override
	protected String _getUsageString() {
		return "' /setBuildingCollidables add/remove 'add creates a collision line.' needs 4 integers. startX, endX, startY, endY";

	}

}
