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
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import org.pmw.tinylog.Logger;

/**
 * @author Eighty
 *
 */
public class AddMobCmd extends AbstractDevCmd {

	public AddMobCmd() {
        super("mob");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		Zone zone = ZoneManager.findSmallestZone(pc.getLoc());

		if (words[0].equals("all")){

			for (AbstractGameObject mobbaseAGO: DbManager.getList(GameObjectType.MobBase)){
				MobBase mb = (MobBase)mobbaseAGO;
				int loadID = mb.getObjectUUID();
				Mob mob = Mob.createMob( loadID, Vector3fImmutable.getRandomPointInCircle(pc.getLoc(), 100),
						null, true, zone, null,0);
				if (mob != null) {
					mob.updateDatabase();
					this.setResult(String.valueOf(mob.getDBID()));
				} else {
					throwbackError(pc, "Failed to create mob of type " + loadID);
					Logger.error( "Failed to create mob of type "
							+ loadID);
				}
			}
			return;
		}


		int loadID;
		try {
			loadID = Integer.parseInt(words[0]);
		} catch (NumberFormatException e) {
			throwbackError(pc, "Supplied type " + words[0]
					+ " failed to parse to an Integer");
			return;
		} catch (Exception e) {
			throwbackError(pc,
					"An unknown exception occurred when trying to use mob command for type "
							+ words[0]);
			return; // NaN
		}


		if (zone == null) {
			throwbackError(pc, "Failed to find zone to place mob in.");
			return;
		}

		if (zone.isPlayerCity()) {
			throwbackError(pc, "Cannot use ./mob on Player cities. Try ./servermob instead.");
			return;
		}


		Mob mob = Mob.createMob( loadID, pc.getLoc(),
				null, true, zone, null,0);
		if (mob != null) {
			mob.updateDatabase();
			ChatManager.chatSayInfo(pc,
					"Mob with ID " + mob.getDBID() + " added");
			this.setResult(String.valueOf(mob.getDBID()));
		} else {
			throwbackError(pc, "Failed to create mob of type " + loadID);
			Logger.error("Failed to create mob of type "
					+ loadID);
		}
	}

	@Override
	protected String _getHelpString() {
        return "Creates a Mob of type 'mobID' at the location your character is standing";
	}

	@Override
	protected String _getUsageString() {
        return "' /mob mobID'";
	}

}
