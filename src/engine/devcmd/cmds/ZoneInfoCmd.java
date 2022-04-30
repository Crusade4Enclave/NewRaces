// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.objects.AbstractGameObject;
import engine.objects.City;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.util.StringUtils;

import java.util.ArrayList;


/**
 * @author
 *
 */
public class ZoneInfoCmd extends AbstractDevCmd {

	public ZoneInfoCmd() {
        super("zoneinfo");
    }

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		Zone zone = null;

		if (player == null) {
			throwbackError(player, "Unable to find the pc making the request.");
			return;
		}

		try {
			int targetID = Integer.parseInt(words[0]);

			//try get zone by objectUUID
			zone = ZoneManager.getZoneByUUID(targetID);

			//that failed, try get by zoneID
			if (zone == null)
				zone = ZoneManager.getZoneByZoneID(targetID);

			//no zone found, so fail
			if (zone == null) {
				throwbackError(player, "Zone with ID " + targetID + "not found");
				return;
			}
		} catch (Exception e) {
			zone = ZoneManager.findSmallestZone(player.getLoc());
		}

		if (zone == null) {
			throwbackError(player, "Zone not found");
			return;
		}

		String newline = "\r\n ";


		int objectUUID = zone.getObjectUUID();
		String output;

		output = "Target Information:" + newline;
		output += StringUtils.addWS("UUID: " + objectUUID, 20);
		output += newline;
		output += "name: " + zone.getName();
		output += newline;
		output += "loadNum: " + zone.getLoadNum();
		if (zone.getParent() != null) {
			output += StringUtils.addWS(", parent: " + zone.getParent().getObjectUUID(), 30);
			output += "Parentabs: x: " + zone.getParent().getAbsX() + ", y: " + zone.getParent().getAbsY() + ", z: " + zone.getParent().getAbsZ();

		} else
			output += StringUtils.addWS(", parent: none", 30);
		output += newline;
		output += "absLoc: x: " + zone.getAbsX() + ", y: " + zone.getAbsY() + ", z: " + zone.getAbsZ();
		output += newline;
		output += "offset: x: " + zone.getXCoord() + ", y: " + zone.getYCoord() + ", z: " + zone.getZCoord();
		output += newline;
		output += "radius: x: " + zone.getBounds().getHalfExtents().x + ", z: " + zone.getBounds().getHalfExtents().y;
		output += newline;

		if (zone.getHeightMap() != null){
			output += "HeightMap ID: " + zone.getHeightMap().getHeightMapID();
			output += newline;
			output += "Bucket Width X : " + zone.getHeightMap().getBucketWidthX();
			output += newline;
			output += "Bucket Width Y : " + zone.getHeightMap().getBucketWidthY();

		}
		output += "radius: x: " + zone.getBounds().getHalfExtents().x + ", z: " + zone.getBounds().getHalfExtents().y;
		output += newline;
		//		output += "minLvl = " + zone.getMinLvl() + " | maxLvl = " + zone.getMaxLvl();
		output += newline;
		output += "Sea Level = " +zone.getSeaLevel();
		output += newline;
		output += "World Altitude = " + zone.getWorldAltitude();
		throwbackInfo(player, output);

		City city = ZoneManager.getCityAtLocation(player.getLoc());

		output += newline;
		output += (city == null)? "None" : city.getParent().getName();

		if (city != null ) {

			if (city.isLocationOnCityGrid(player.getLoc()))
				output += " (Grid)";
			else if (city.isLocationOnCityZone(player.getLoc()))
				output += " (Zone)";
			else if (city.isLocationWithinSiegeBounds(player.getLoc()))
				output += " (Siege)";
		} else {
			output = "children:";

			ArrayList<Zone> nodes = zone.getNodes();

			if (nodes.isEmpty())
				output += " none";

			for (Zone child : nodes) {
				output += newline;
				output += child.getName() + " (" + child.getLoadNum() + ')';
			}
		}
		throwbackInfo(player, output);
	}

	@Override
	protected String _getHelpString() {
		return "Gets information on an Object.";
	}

	@Override
	protected String _getUsageString() {
		return "' /info targetID'";
	}

}
