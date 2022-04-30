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
import engine.gameManager.DbManager;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.server.MBServerStatics;

import java.util.concurrent.ThreadLocalRandom;

public class GotoCmd extends AbstractDevCmd {

	public GotoCmd() {
        super("goto");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		Vector3fImmutable loc = null;

		// Arg Count Check


		if (target != null && words[0].isEmpty()){
			AbstractWorldObject targetAgo = (AbstractWorldObject)target;
			pc.teleport(targetAgo.getLoc());
			return;
		}

		if (words[0].isEmpty()){
			this.sendUsage(pc);
			return;
		}

		if (words[0].equalsIgnoreCase("playground")){
			if (target instanceof AbstractCharacter){
				loc = new Vector3fImmutable(63276,0,-54718);
			}

			if (loc != null)
				pc.teleport(loc);

			return;
		}
		
		if (words[0].equalsIgnoreCase("coc")){
			if (target instanceof AbstractCharacter){
				loc = new Vector3fImmutable(98561.656f,0,-13353.778f);
			}

			if (loc != null)
				pc.teleport(loc);

			return;
		}

		String cityName = "";
		for (String partial: words){
			cityName += partial + ' ';
		}

		cityName = cityName.substring(0, cityName.length() - 1);

		for (AbstractGameObject cityAgo: DbManager.getList(GameObjectType.City)){
			City city = (City)cityAgo;
			if (city == null)
				continue;
			if (!city.getCityName().equalsIgnoreCase(cityName))
				continue;
			Zone zone = city.getParent();
			if (zone != null){
				if (zone.isNPCCity() || zone.isPlayerCity())
					loc = Vector3fImmutable.getRandomPointOnCircle(zone.getLoc(), MBServerStatics.TREE_TELEPORT_RADIUS);
				else
					loc = zone.getLoc();

				int random = ThreadLocalRandom.current().nextInt(5);
				if (random == 1)
					break;
			}
		}

		if (loc == null){
			for (AbstractGameObject zoneAgo: DbManager.getList(GameObjectType.Zone)){
				Zone zone = (Zone)zoneAgo;
				if (zone == null)
					continue;
				if (!zone.getName().equalsIgnoreCase(cityName))
					continue;
				if (zone != null){
					if (zone.isNPCCity() || zone.isPlayerCity())
						loc = Vector3fImmutable.getRandomPointOnCircle(zone.getLoc(), MBServerStatics.TREE_TELEPORT_RADIUS);
					else
						loc = zone.getLoc();

					int random = ThreadLocalRandom.current().nextInt(5);
					if (random == 1)
						break;
				}
			}
		}
		if (loc == null && words.length == 1){

			try {
				PlayerCharacter pcDest = SessionManager
						.getPlayerCharacterByLowerCaseName(words[0]);
				if (pcDest == null){
					this.throwbackError(pc, "Player or Zone not found by name: "
							+ words[0]);
					this.throwbackInfo(pc, "If you have spaces in the zone name, replace them with '_'");
					return;
				}

				if (pcDest.getCombinedName().equals(pc.getCombinedName())) {
					this
					.throwbackError(pc,
							"Cannot goto yourself.  Well, you can, but you wont go anywhere.");
					return;
				}

				loc = pcDest.getLoc();
			} catch (Exception e) {
				this.throwbackError(pc,
						"An unknown exception occurred while attempting to goto a character named '"
								+ words[0] + '\'');
				return;
			}

		}
		if (loc == null) { // lat lon mode
			if (words.length != 2) {
				throwbackError(pc, this.getUsageString());
				return;
			}
			float lat = 0.0f, lon = 0.0f;
			String latLong = '\'' + words[0] + ", " + words[1] + '\'';

			try {
				lat = Float.parseFloat(words[0]);
				lon = Float.parseFloat(words[1]);
				loc = new Vector3fImmutable(lat, 0f, -lon);
			} catch (NumberFormatException e) {
				this.throwbackError(pc, "Supplied LatLong: " + latLong
						+ " failed to parse to Floats");
				return;

			} catch (Exception e) {
				this.throwbackError(pc,
						"An unknown exception occurred while attempting to goto LatLong of "
								+ latLong);
				return;
			}
		}
		if (loc != null) {
			pc.teleport(loc);
		}
	}

	@Override
	protected String _getHelpString() {
		return  "Alters your characters position TO 'lat' and 'long', or TO the position of 'characterName'.  This does not transport you BY 'lat' and 'long', but rather TO 'lat' and 'long' ";
	}

	@Override
	protected String _getUsageString() {
		return  "'[ /goto lat lon] || [ /goto characterName] || [/goto zoneName  \replace spaces with `_`]`";
	}

}
