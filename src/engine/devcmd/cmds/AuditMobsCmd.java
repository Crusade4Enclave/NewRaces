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
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class AuditMobsCmd extends AbstractDevCmd {

	public AuditMobsCmd() {
        super("auditmobs");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		//get Zone to check mobs against

		Zone zone;

		if (words.length == 2){
			if (words[0].equals("all")){
				int plusplus = 0;
				int count = Integer.parseInt(words[1]);
				for (Zone zoneMicro: ZoneManager.getAllZones()){
					int size = zoneMicro.zoneMobSet.size();

					if (size >= count){
						plusplus++;
						throwbackInfo(pcSender, zoneMicro.getName() + " at location  " + zoneMicro.getLoc().toString()  + " has " + size + " mobs. ");
						System.out.println(zoneMicro.getName() + " at location  " + zoneMicro.getLoc().toString()  + " has " + size + " mobs. ");
					}


				}
				throwbackInfo(pcSender," there are " +plusplus + " zones with at least " + count + " mobs in each.");
			}
			return;
		}
		if (words.length > 1) {
			this.sendUsage(pcSender);
			return;
		} else if (words.length == 1) {
			int uuid;
			try {
				uuid = Integer.parseInt(words[0]);
				zone = ZoneManager.getZoneByUUID(uuid);
			} catch (NumberFormatException e) {
				zone = ZoneManager.findSmallestZone(pcSender.getLoc());
			}
		} else
			zone = ZoneManager.findSmallestZone(pcSender.getLoc());

		if (zone == null) {
			throwbackError(pcSender, "Unable to find the zone");
			return;
		}

		//get list of mobs for zone

		if (zone.zoneMobSet.isEmpty()) {
			throwbackError(pcSender, "No mobs found for this zone.");
			return;
		}

		//	ConcurrentHashMap<Integer, Mob> spawnMap = Mob.getSpawnMap();
		//ConcurrentHashMap<Mob, Long> respawnMap = Mob.getRespawnMap();
		//		ConcurrentHashMap<Mob, Long> despawnMap = Mob.getDespawnMap();

		throwbackInfo(pcSender, zone.getName() + ", numMobs: " + zone.zoneMobSet.size());
		throwbackInfo(pcSender, "UUID, dbID, inRespawnMap, isAlive, activeAI, Loc");



		//mob not found in spawn map, check respawn
		boolean inRespawn = false;



	}


	@Override
	protected String _getUsageString() {
		return "' /auditmobs [zone.UUID]'";
	}

	@Override
	protected String _getHelpString() {
		return "Audits all the mobs in a zone.";

	}

}
