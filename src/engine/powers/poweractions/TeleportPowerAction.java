// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.MovementManager;
import engine.gameManager.PowersManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;


public class TeleportPowerAction extends AbstractPowerAction {

	private boolean ignoreNoTeleSpire;

	public TeleportPowerAction(ResultSet rs) throws SQLException {
		super(rs);

		int flags = rs.getInt("flags");
		this.ignoreNoTeleSpire = ((flags & 32768) != 0) ? true : false;
	}

	public boolean ignoreNoTeleSpire() {
		return this.ignoreNoTeleSpire;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (!AbstractWorldObject.IsAbstractCharacter(awo))
			return;

		AbstractCharacter awoac = (AbstractCharacter) awo;

		//verify targetLoc within range

		if (awo.getLoc().distanceSquared2D(targetLoc) >  MBServerStatics.MAX_TELEPORT_RANGE *  MBServerStatics.MAX_TELEPORT_RANGE) {
			if (awo.equals(source))
				failTeleport(pb, awoac);
			return;
		}
		
		if (source.getBonuses().getBool(ModType.BlockedPowerType, SourceType.TELEPORT))
			return;

		City city = ZoneManager.getCityAtLocation(targetLoc);

		// Intentionally fail if target location is not on
		// the actual city zone.
		if (city != null)
		if (city.isLocationOnCityZone(targetLoc) == false)
			city = null;

		if (city != null){

			for (String eff : city.getEffects().keySet()){

				Effect spireEffect = city.getEffects().get(eff);

				for (AbstractEffectModifier aem : spireEffect.getEffectModifiers()){

					if (aem.getType().equals("TELEPORT") &&  !this.ignoreNoTeleSpire){
						if (awo.equals(source))
							failTeleport(pb, awoac);
						return;
					}
				}
			}
		}

		//TODO verify target loc is valid loc
		
		Regions region = Regions.GetRegionForTeleport(targetLoc);

		if (region != null && !region.isOutside())
			return;

		MovementManager.translocate(awoac,targetLoc, region);
	}

	private static void failTeleport(PowersBase pb, AbstractCharacter awo) {

		if (pb == null || awo == null || (!(awo.getObjectType().equals(Enum.GameObjectType.PlayerCharacter))))
			return;

		//teleport failed. Reset teleport power
		PowersManager.finishRecycleTime(pb.getToken(), (PlayerCharacter) awo, true);
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub
	}
}