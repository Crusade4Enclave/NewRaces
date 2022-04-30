// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.InterestManagement.HeightMap;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.math.Vector2f;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class AuditHeightMapCmd extends AbstractDevCmd {

	public AuditHeightMapCmd() {
        super("auditheightmap");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {

		int count = Integer.parseInt(words[0]);
		long start = System.currentTimeMillis();
		for (int i = 0; i<count;i++){


			Zone currentZone = ZoneManager.findSmallestZone(pcSender.getLoc());



			Vector3fImmutable currentLoc = Vector3fImmutable.getRandomPointInCircle(currentZone.getLoc(), currentZone.getBounds().getHalfExtents().x < currentZone.getBounds().getHalfExtents().y ? currentZone.getBounds().getHalfExtents().x : currentZone.getBounds().getHalfExtents().y );

			Vector2f zoneLoc = ZoneManager.worldToZoneSpace(currentLoc, currentZone);

			if (currentZone != null && currentZone.getHeightMap() != null){
				float altitude = currentZone.getHeightMap().getInterpolatedTerrainHeight(zoneLoc);
				float outsetAltitude = HeightMap.getOutsetHeight(altitude, currentZone, pcSender.getLoc());
			}

		}
		long end = System.currentTimeMillis();

		long delta = end - start;

		this.throwbackInfo(pcSender, "Audit Heightmap took " + delta + " ms to run " + count + " times!");


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
