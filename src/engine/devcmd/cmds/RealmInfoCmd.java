// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;


import engine.InterestManagement.RealmMap;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Realm;
import engine.objects.Zone;

public class RealmInfoCmd extends AbstractDevCmd {

	public RealmInfoCmd() {
        super("realminfo");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {

		Zone serverZone;
		Zone parentZone;
		Realm serverRealm;
		int realmID;
		String outString = "";

		if (pc == null) {
			throwbackError(pc, "Unable to find the pc making the request.");
			return;
		}

		serverZone = ZoneManager.findSmallestZone(pc.getLoc());

		if (serverZone == null) {
			throwbackError(pc, "Zone not found");
			return;
		}

		parentZone = serverZone.getParent();

		realmID = RealmMap.getRealmIDAtLocation(pc.getLoc());

		String newline = "\r\n ";

		outString = newline;
		outString += "RealmID: " + realmID;

		serverRealm = Realm.getRealm(realmID);

		if (serverRealm == null)
			outString += " Name: SeaFloor";
		else
			outString += serverRealm.getRealmName();

		outString += newline;

		outString += " Zone: " + serverZone.getName();

		outString += newline;

		if (serverZone.getParent() != null)
			outString += " Parent: " + serverZone.getParent().getName();
		else
			outString += "Parent: NONE";

		outString += newline;

		throwbackInfo(pc, outString);
	}

	@Override
	protected String _getHelpString() {
        return "Returns info on realm.";
	}

	@Override
	protected String _getUsageString() {
        return "' /info targetID'";
	}

}
