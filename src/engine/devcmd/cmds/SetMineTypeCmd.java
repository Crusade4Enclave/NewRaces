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
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.objects.*;

/**
 * 
 * @author Eighty
 * 
 */
public class SetMineTypeCmd extends AbstractDevCmd {

	public SetMineTypeCmd() {
        super("setminetype");
        this.addCmdString("setminetype");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		
		MineProduction mineType = MineProduction.valueOf(args[0].toUpperCase());
		if (mineType == null)
			return;
		
		if (target.getObjectType() != GameObjectType.Building)
			return;
		Building mineBuilding = BuildingManager.getBuilding(target.getObjectUUID());
		if (mineBuilding == null)
			return;
		Mine mine = Mine.getMineFromTower(mineBuilding.getObjectUUID());
		if (mine == null)
			return;
		if (!DbManager.MineQueries.CHANGE_TYPE(mine, mineType))
			return;
		mine.setMineType(mineType.name());
		ChatManager.chatSystemInfo(pcSender, "The mine in " + mine.getZoneName() + " is now a(n) " + mine.getMineType().name);
		Mine.setLastChange(System.currentTimeMillis());
	}

	@Override
	protected String _getUsageString() {
        return "' /setminetype gold|ore|magic|lumber";
	}

	@Override
	protected String _getHelpString() {
        return "Changes the mine type of the current mine.";
	}

}
