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
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.Mine;
import engine.objects.PlayerCharacter;

/**
 * 
 * @author Eighty
 * 
 */
public class SetMineExpansion extends AbstractDevCmd {

	public SetMineExpansion() {
        super("setexpansion");
        this.addCmdString("setexpansion");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		
		
		if (target.getObjectType() != GameObjectType.Building)
			return;
		Building mineBuilding = BuildingManager.getBuilding(target.getObjectUUID());
		if (mineBuilding == null)
			return;
		Mine mine = Mine.getMineFromTower(mineBuilding.getObjectUUID());
		if (mine == null)
			return;
		int bit = 2;
		switch (args[0].toUpperCase()) {
		case "ON":

			bit |= mine.getFlags();
			if (!DbManager.MineQueries.SET_FLAGS(mine, bit))
				return;
			mine.setFlags(bit);
			ChatManager.chatSystemInfo(pcSender, mine.getZoneName() + "'s " + mine.getMineType().name + " is now an expansion mine.");
			Mine.setLastChange(System.currentTimeMillis());
			break;
			
		case "OFF":
			bit &= ~mine.getFlags();
			if (!DbManager.MineQueries.SET_FLAGS(mine, bit))
				return;
			mine.setFlags(bit);
			ChatManager.chatSystemInfo(pcSender, mine.getZoneName() + "'s " + mine.getMineType().name + " is no longer an expansion mine.");
			Mine.setLastChange(System.currentTimeMillis());
			break;
			
		}

		
		
		

	}

	@Override
	protected String _getUsageString() {
        return "' /setmineexpansion on|off";
	}

	@Override
	protected String _getHelpString() {
        return "enables or disables an expansion type for a mine.";
	}

}
